/*
 *  Mask-Android
 *
 *  Copyright (C) 2022  DimensionDev and Contributors
 *
 *  This file is part of Mask-Android.
 *
 *  Mask-Android is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Mask-Android is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with Mask-Android.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dimension.maskbook.wallet.walletconnect.v1

import org.walletconnect.Session
import org.walletconnect.impls.WCSessionStore
import org.walletconnect.nullOnThrow
import org.walletconnect.types.extractPeerData
import org.walletconnect.types.intoMap
import org.walletconnect.types.toStringList
import java.util.Collections
import java.util.Random
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// origin WCSession has some bugs parsing response, so we created a new one
class WCSessionV1(
    private val config: Session.FullyQualifiedConfig,
    private val payloadAdapter: Session.PayloadAdapter,
    private val sessionStore: WCSessionStore,
    transportBuilder: Session.Transport.Builder,
    clientMeta: Session.PeerMeta,
    clientId: String? = null
) : Session {
    val id = config.handshakeTopic
    private val keyLock = Any()

    // Persisted state
    private var currentKey: String

    private var approvedAccounts: List<String>? = null
    private var chainId: Long? = null
    private var handshakeId: Long? = null
    private var peerId: String? = null
    private var peerMeta: Session.PeerMeta? = null

    private val clientData: Session.PeerData

    // Getters
    private val encryptionKey: String
        get() = currentKey

    private val decryptionKey: String
        get() = currentKey

    // Non-persisted state
    private val transport = transportBuilder.build(config.bridge, ::handleStatus, ::handleMessage)
    private val requests: MutableMap<Long, (Session.MethodCall.Response) -> Unit> =
        ConcurrentHashMap()
    private val sessionCallbacks: MutableSet<Session.Callback> =
        Collections.newSetFromMap(ConcurrentHashMap<Session.Callback, Boolean>())

    init {
        currentKey = config.key
        clientData = sessionStore.load(id)?.let {
            currentKey = it.currentKey
            approvedAccounts = it.approvedAccounts
            chainId = it.chainId
            handshakeId = it.handshakeId
            peerId = it.peerData?.id
            peerMeta = it.peerData?.meta
            if (clientId != null && clientId != it.clientData.id)
                throw IllegalArgumentException("Provided clientId is different from stored clientId")
            it.clientData
        } ?: run {
            Session.PeerData(clientId ?: UUID.randomUUID().toString(), clientMeta)
        }
        storeSession()
    }

    override fun addCallback(cb: Session.Callback) {
        sessionCallbacks.add(cb)
    }

    override fun removeCallback(cb: Session.Callback) {
        sessionCallbacks.remove(cb)
    }

    override fun clearCallbacks() {
        sessionCallbacks.clear()
    }

    private fun propagateToCallbacks(action: Session.Callback.() -> Unit) {
        sessionCallbacks.forEach {
            try {
                it.action()
            } catch (t: Throwable) {
                // If error propagation fails, don't try again
                nullOnThrow { it.onStatus(Session.Status.Error(t)) }
            }
        }
    }

    override fun peerMeta(): Session.PeerMeta? = peerMeta

    override fun approvedAccounts(): List<String>? = approvedAccounts

    override fun init() {
        if (transport.connect()) {
            // Register for all messages for this client
            transport.send(
                Session.Transport.Message(
                    config.handshakeTopic, "sub", ""
                )
            )
        }
    }

    override fun offer() {
        if (transport.connect()) {
            val requestId = createCallId()
            send(
                Session.MethodCall.SessionRequest(requestId, clientData),
                topic = config.handshakeTopic,
                callback = { resp ->
                    (resp.result as? Map<String, *>)?.correctExtractSessionParams()?.let { params ->
                        peerId = params.peerData?.id
                        peerMeta = params.peerData?.meta
                        approvedAccounts = params.accounts
                        chainId = params.chainId
                        storeSession()
                        propagateToCallbacks { onStatus(if (params.approved) Session.Status.Approved else Session.Status.Closed) }
                    }
                }
            )
            handshakeId = requestId
        }
    }

    override fun approve(accounts: List<String>, chainId: Long) {
        val handshakeId = handshakeId ?: return
        approvedAccounts = accounts
        this.chainId = chainId
        // We should not use classes in the Response, since this will not work with proguard
        val params = Session.SessionParams(true, chainId, accounts, clientData).intoMap()
        send(Session.MethodCall.Response(handshakeId, params))
        storeSession()
        propagateToCallbacks { onStatus(Session.Status.Approved) }
    }

    override fun update(accounts: List<String>, chainId: Long) {
        val params = Session.SessionParams(true, chainId, accounts, clientData)
        send(Session.MethodCall.SessionUpdate(createCallId(), params))
    }

    override fun reject() {
        handshakeId?.let {
            // We should not use classes in the Response, since this will not work with proguard
            val params = Session.SessionParams(false, null, null, null).intoMap()
            send(Session.MethodCall.Response(it, params))
        }
        endSession()
    }

    override fun approveRequest(id: Long, response: Any) {
        send(Session.MethodCall.Response(id, response))
    }

    override fun rejectRequest(id: Long, errorCode: Long, errorMsg: String) {
        send(
            Session.MethodCall.Response(
                id,
                result = null,
                error = Session.Error(errorCode, errorMsg)
            )
        )
    }

    override fun performMethodCall(
        call: Session.MethodCall,
        callback: ((Session.MethodCall.Response) -> Unit)?
    ) {
        send(call, callback = callback)
    }

    private fun handleStatus(status: Session.Transport.Status) {
        when (status) {
            Session.Transport.Status.Connected -> {
                // Register for all messages for this client
                transport.send(
                    Session.Transport.Message(
                        clientData.id, "sub", ""
                    )
                )
            }
            Session.Transport.Status.Disconnected -> Unit
            is Session.Transport.Status.Error -> Unit
        }
        propagateToCallbacks {
            onStatus(
                when (status) {
                    Session.Transport.Status.Connected -> Session.Status.Connected
                    Session.Transport.Status.Disconnected -> Session.Status.Disconnected
                    is Session.Transport.Status.Error -> Session.Status.Error(
                        Session.TransportError(
                            status.throwable
                        )
                    )
                }
            )
        }
    }

    private fun handleMessage(message: Session.Transport.Message) {
        if (message.type != "pub") return
        val data: Session.MethodCall
        synchronized(keyLock) {
            try {
                data = payloadAdapter.parse(message.payload, decryptionKey)
            } catch (e: Exception) {
                handlePayloadError(e)
                return
            }
        }
        var accountToCheck: String? = null
        when (data) {
            is Session.MethodCall.SessionRequest -> {
                handshakeId = data.id
                peerId = data.peer.id
                peerMeta = data.peer.meta
                storeSession()
            }
            is Session.MethodCall.SessionUpdate -> {
                if (!data.params.approved) {
                    endSession()
                }
            }
            is Session.MethodCall.SendTransaction -> {
                accountToCheck = data.from
            }
            is Session.MethodCall.SignMessage -> {
                accountToCheck = data.address
            }
            is Session.MethodCall.Response -> {
                val callback = requests[data.id] ?: return
                callback(data)
            }
            is Session.MethodCall.Custom -> Unit
        }

        if (accountToCheck?.let { accountCheck(data.id(), it) } != false) {
            propagateToCallbacks { onMethodCall(data) }
        }
    }

    private fun accountCheck(id: Long, address: String): Boolean {
        approvedAccounts?.find { it.equals(address, ignoreCase = true) } ?: run {
            handlePayloadError(Session.MethodCallException.InvalidAccount(id, address))
            return false
        }
        return true
    }

    private fun handlePayloadError(e: Exception) {
        propagateToCallbacks { Session.Status.Error(e) }
        (e as? Session.MethodCallException)?.let {
            rejectRequest(it.id, it.code, it.message ?: "Unknown error")
        }
    }

    private fun endSession() {
        sessionStore.remove(id)
        approvedAccounts = null
        chainId = null
        internalClose()
        propagateToCallbacks { onStatus(Session.Status.Closed) }
    }

    private fun storeSession() {
        sessionStore.store(
            id,
            WCSessionStore.State(
                config,
                clientData,
                peerId?.let { Session.PeerData(it, peerMeta) },
                handshakeId,
                currentKey,
                approvedAccounts,
                chainId
            )
        )
    }

    // Returns true if method call was handed over to transport
    private fun send(
        msg: Session.MethodCall,
        topic: String? = peerId,
        callback: ((Session.MethodCall.Response) -> Unit)? = null
    ): Boolean {
        topic ?: return false

        val payload: String
        synchronized(keyLock) {
            payload = payloadAdapter.prepare(msg, encryptionKey)
        }
        callback?.let {
            requests[msg.id()] = callback
        }
        transport.send(Session.Transport.Message(topic, "pub", payload))
        return true
    }

    private fun createCallId() = System.currentTimeMillis() * 1000 + Random().nextInt(999)

    private fun internalClose() {
        transport.close()
    }

    override fun kill() {
        val params = Session.SessionParams(false, null, null, null)
        send(Session.MethodCall.SessionUpdate(createCallId(), params))
        endSession()
    }
}

fun Map<String, *>.correctExtractSessionParams(): Session.SessionParams {
    val approved =
        this["approved"] as? Boolean ?: throw IllegalArgumentException("approved missing")
    val chainId = try {
        (this["chainId"] as? Number)?.toLong()
    } catch (e: Throwable) {
        null
    }
    val accounts = nullOnThrow { (this["accounts"] as? List<*>)?.toStringList() }

    return Session.SessionParams(
        approved,
        chainId,
        accounts,
        nullOnThrow { this.extractPeerData() }
    )
}
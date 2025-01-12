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
package com.dimension.maskbook.wallet.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.dimension.maskbook.common.bigDecimal.BigDecimal
import com.dimension.maskbook.common.okhttp.okHttpClient
import com.dimension.maskbook.common.repository.JSMethod
import com.dimension.maskbook.debankapi.model.ChainID
import com.dimension.maskbook.debankapi.model.Token
import com.dimension.maskbook.wallet.db.AppDatabase
import com.dimension.maskbook.wallet.db.model.CoinPlatformType
import com.dimension.maskbook.wallet.db.model.DbChainData
import com.dimension.maskbook.wallet.db.model.DbStoredKey
import com.dimension.maskbook.wallet.db.model.DbToken
import com.dimension.maskbook.wallet.db.model.DbWallet
import com.dimension.maskbook.wallet.db.model.DbWalletBalance
import com.dimension.maskbook.wallet.db.model.DbWalletToken
import com.dimension.maskbook.wallet.db.model.WalletSource
import com.dimension.maskbook.wallet.export.model.ChainType
import com.dimension.maskbook.wallet.export.model.DbWalletBalanceType
import com.dimension.maskbook.wallet.export.model.TokenData
import com.dimension.maskbook.wallet.export.model.WalletData
import com.dimension.maskbook.wallet.ext.ether
import com.dimension.maskbook.wallet.ext.gwei
import com.dimension.maskbook.wallet.paging.mediator.CollectibleMediator
import com.dimension.maskbook.wallet.services.WalletServices
import com.dimension.maskbook.wallet.walletconnect.WalletConnectClientManager
import com.dimension.maskwalletcore.WalletKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.ens.EnsResolver
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import java.util.UUID
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private val CurrentCoinPlatformTypeKey = stringPreferencesKey("coin_platform_type")
private val CurrentWalletKey = stringPreferencesKey("current_wallet")
private val ChainTypeKey = stringPreferencesKey("chain_type")
val Context.walletDataStore: DataStore<Preferences> by preferencesDataStore(name = "wallet")

private fun Token.toDbToken(chainId: ChainID?) = DbToken(
    id = id ?: "",
    address = id ?: "",
    chainType = chainId?.chainType ?: ChainType.unknown,
    name = name ?: "",
    symbol = symbol ?: "",
    decimals = decimals ?: 0L,
    logoURI = logoURL,
    price = BigDecimal(price ?: 0.0)
)

class WalletRepository(
    private val dataStore: DataStore<Preferences>,
    private val database: AppDatabase,
    private val services: WalletServices,
    private val walletConnectManager: WalletConnectClientManager
) : IWalletRepository {
    private val tokenScope = CoroutineScope(Dispatchers.IO)
    private val scope = CoroutineScope(Dispatchers.IO)

    @OptIn(ExperimentalTime::class)
    override fun init() {
        tokenScope.launch {
            refreshChainData()
            while (true) {
                delay(12.seconds)
                refreshCurrentWalletToken()
                refreshCurrentWalletCollectibles()
                refreshNativeTokens()
            }
        }
    }

    override val dWebData: Flow<DWebData>
        get() = dataStore.data.map {
            val coinPlatformType = it[CurrentCoinPlatformTypeKey]?.let {
                CoinPlatformType.valueOf(it)
            } ?: CoinPlatformType.Ethereum
            val chainType = it[ChainTypeKey]?.let { ChainType.valueOf(it) } ?: ChainType.eth
            DWebData(
                coinPlatformType,
                chainType
            )
        }

    override fun setActiveCoinPlatformType(platformType: CoinPlatformType) {
        scope.launch {
            dataStore.edit {
                it[CurrentCoinPlatformTypeKey] = platformType.name
            }
        }
    }

    override fun setChainType(networkType: ChainType, notifyJS: Boolean) {
        scope.launch {
            dataStore.edit {
                it[ChainTypeKey] = networkType.name
            }
            if (notifyJS) {
                JSMethod.Wallet.updateEthereumChainId(networkType.chainId)
            }
        }
    }

    override suspend fun findWalletByAddress(address: String): WalletData? {
        return database.walletDao().getByAddress(address)?.let { WalletData.fromDb(it) }
    }

    private suspend fun refreshCurrentWalletCollectibles() {
        val currentWallet = currentWallet.firstOrNull() ?: return
        try {
            CollectibleMediator(
                walletId = currentWallet.id,
                database = database,
                openSeaServices = services.openSeaServices,
                walletAddress = currentWallet.address,
            ).load(LoadType.REFRESH, PagingState(emptyList(), null, PagingConfig(pageSize = 10), 0))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private suspend fun refreshCurrentWalletToken() {
        val currentWallet = currentWallet.firstOrNull() ?: return
        try {
            val token = services.debankServices.tokenList(
                currentWallet.address,
                is_all = true,
                has_balance = false
            ).filter { it.isVerified == true }
            val balance =
                services.debankServices.totalBalance(currentWallet.address).let { balance ->
                    balance.chainList?.map { chain ->
                        chain.id?.let { it1 -> runCatching { ChainID.valueOf(it1) }.getOrNull() }
                            ?.let {
                                when (it) {
                                    ChainID.eth -> DbWalletBalanceType.eth
                                    ChainID.bsc -> DbWalletBalanceType.bsc
                                    ChainID.xdai -> DbWalletBalanceType.xdai
                                    ChainID.matic -> DbWalletBalanceType.polygon
                                    ChainID.op -> DbWalletBalanceType.optimism
                                    ChainID.arb -> DbWalletBalanceType.arbitrum
                                    else -> null
                                }
                            }?.let {
                                DbWalletBalance(
                                    UUID.randomUUID().toString(),
                                    currentWallet.id,
                                    it,
                                    chain.usdValue?.toBigDecimal() ?: BigDecimal.ZERO,
                                )
                            }
                    }?.mapNotNull { it }?.let {
                        it + listOf(
                            DbWalletBalance(
                                UUID.randomUUID().toString(),
                                currentWallet.id,
                                DbWalletBalanceType.all,
                                balance.totalUsdValue?.toBigDecimal() ?: BigDecimal.ZERO,
                            )
                        )
                    } ?: emptyList()
                }

            val tokens = token.map {
                val chainId =
                    kotlin.runCatching { it.chain?.let { it1 -> ChainID.valueOf(it1) } }.getOrNull()
                it.toDbToken(chainId)
            }
            val walletTokens = token.map {
                DbWalletToken(
                    id = UUID.randomUUID().toString(),
                    walletId = currentWallet.id,
                    count = BigDecimal(it.amount ?: 0.0),
                    tokenId = it.id ?: ""
                )
            }
            database.withTransaction {
                database.walletBalanceDao().add(balance)
                database.tokenDao().add(tokens)
                database.walletTokenDao().add(walletTokens)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private suspend fun refreshChainData() {
        try {
            val chains = services.debankServices.getChainList().mapNotNull {
                it.id?.let { id ->
                    try {
                        val chainID = ChainID.valueOf(id)
                        if (chainID.chainType != ChainType.unknown) {
                            DbChainData(
                                chainId = chainID.chainType.chainId,
                                name = chainID.chainType.name,
                                fullName = it.name ?: chainID.chainType.name,
                                nativeTokenID = it.nativeTokenID ?: "",
                                logoURL = it.logoURL ?: ""
                            )
                        } else null
                    } catch (e: Throwable) {
                        null
                    }
                }
            }
            database.chainDao().add(chains)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private suspend fun refreshNativeTokens() {
        try {
            val chains = database.chainDao().getAll().map { it.chain }.ifEmpty {
                // in case init refreshChainData failed
                refreshChainData()
                database.chainDao().getAll().map { it.chain }
            }
            val tokens = chains.mapNotNull {
                try {
                    val chainId = ChainType.valueOf(it.name).dbank
                    services.debankServices.token(
                        id = it.nativeTokenID,
                        chainId = ChainType.valueOf(it.name).dbank
                    ).toDbToken(chainId)
                } catch (e: Throwable) {
                    null
                }
            }

            database.withTransaction {
                database.tokenDao().add(tokens)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override val wallets: Flow<List<WalletData>>
        get() = database
            .walletDao()
            .getAllFlow()
            .map { list ->
                list.map {
                    WalletData.fromDb(it)
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentWallet: Flow<WalletData?>
        get() = dataStore.data.map {
            it[CurrentWalletKey]
        }.mapNotNull { it }.flatMapLatest {
            database.walletDao().getByIdFlow(it)
        }.map {
            it?.let { it1 -> WalletData.fromDb(it1) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentChain: Flow<ChainData?>
        get() = dWebData.flatMapLatest {
            database.chainDao().getByIdFlow(it.chainType.chainId)
        }.map {
            it?.let {
                ChainData(
                    chainId = it.chain.chainId,
                    name = it.chain.name,
                    fullName = it.chain.fullName,
                    nativeTokenID = it.chain.nativeTokenID,
                    logoURL = it.chain.logoURL,
                    nativeToken = it.token?.let { token -> TokenData.fromDb(token) },
                    chainType = ChainType.valueOf(it.chain.name)
                )
            }
        }

    override fun setCurrentWallet(walletData: WalletData?) {
        if (walletData?.id != null) {
            setCurrentWallet(walletData.id)
        }
    }

    override fun setCurrentWallet(walletId: String) {
        scope.launch {
            database.walletDao().getById(walletId)?.let {
                setCurrentWallet(it.wallet)
            }
        }
    }

    fun setCurrentWallet(dbWallet: DbWallet?) {
        scope.launch {
            dataStore.edit {
                it[CurrentWalletKey] = dbWallet?.id.orEmpty()
            }
            JSMethod.Wallet.updateEthereumAccount(dbWallet?.address.orEmpty())
        }
    }

    override fun generateNewMnemonic(): List<String> {
        return createNewMnemonic().split(" ")
    }

    override suspend fun createWallet(
        mnemonic: List<String>,
        name: String,
        platformType: CoinPlatformType,
    ) {
        val wallet = WalletKey.fromMnemonic(mnemonic = mnemonic.joinToString(" "), "")
        val account = wallet.addNewAccountAtPath(
            platformType.coinType,
            platformType.derivationPath.toString(),
            name,
            ""
        )
        val storeKey = DbStoredKey(
            id = UUID.randomUUID().toString(),
            hash = wallet.hash,
            source = WalletSource.Created,
            data = wallet.data,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        val db = DbWallet(
            id = UUID.randomUUID().toString(),
            address = account.address,
            name = name,
            storeKeyId = storeKey.id,
            derivationPath = account.derivationPath,
            extendedPublicKey = account.extendedPublicKey,
            coin = account.coin,
            platformType = platformType,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        database.storedKeyDao().add(listOf(storeKey))
        database.walletDao().add(listOf(db))
        setCurrentWallet(db)
    }

    override suspend fun importWallet(
        mnemonicCode: List<String>,
        name: String,
        path: List<String>,
        platformType: CoinPlatformType,
    ) {
        scope.launch {
            val wallet = WalletKey.fromMnemonic(mnemonic = mnemonicCode.joinToString(" "), "")
            val accounts = path.map {
                wallet.addNewAccountAtPath(platformType.coinType, it, name, "")
            }
            val storeKey = DbStoredKey(
                id = UUID.randomUUID().toString(),
                hash = wallet.hash,
                source = WalletSource.ImportedMnemonic,
                data = wallet.data,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            val dbWallets = accounts.map { account ->
                DbWallet(
                    id = UUID.randomUUID().toString(),
                    address = account.address,
                    name = name,
                    storeKeyId = storeKey.id,
                    derivationPath = account.derivationPath,
                    extendedPublicKey = account.extendedPublicKey,
                    coin = account.coin,
                    platformType = platformType,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            }
            database.storedKeyDao().add(listOf(storeKey))
            database.walletDao().add(dbWallets)
            dbWallets.firstOrNull()?.let { setCurrentWallet(it) }
        }
    }

    override suspend fun importWallet(
        name: String,
        keyStore: String,
        password: String,
        platformType: CoinPlatformType,
    ) {
        val wallet = WalletKey.fromJson(
            json = keyStore,
            name = name,
            coinType = platformType.coinType,
            password = "",
            keyStoreJsonPassword = password
        )
        val account = wallet.addNewAccountAtPath(
            platformType.coinType,
            platformType.derivationPath.toString(),
            name,
            ""
        )
        val storeKey = DbStoredKey(
            id = UUID.randomUUID().toString(),
            hash = wallet.hash,
            source = WalletSource.ImportedKeyStore,
            data = wallet.data,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        val db = DbWallet(
            id = UUID.randomUUID().toString(),
            address = account.address,
            name = name,
            storeKeyId = storeKey.id,
            derivationPath = account.derivationPath,
            extendedPublicKey = account.extendedPublicKey,
            coin = account.coin,
            platformType = platformType,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        database.storedKeyDao().add(listOf(storeKey))
        database.walletDao().add(listOf(db))
        setCurrentWallet(db)
    }

    override suspend fun importWallet(
        name: String,
        privateKey: String,
        platformType: CoinPlatformType,
    ) {
        scope.launch {
            val wallet = WalletKey.fromPrivateKey(
                privateKey = privateKey,
                name = name,
                coinType = platformType.coinType,
                password = "",
            )
            val account =
                wallet.addNewAccountAtPath(
                    platformType.coinType,
                    platformType.derivationPath.toString(),
                    name,
                    ""
                )
            val storeKey = DbStoredKey(
                id = UUID.randomUUID().toString(),
                hash = wallet.hash,
                source = WalletSource.ImportedPrivateKey,
                data = wallet.data,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            val db = DbWallet(
                id = UUID.randomUUID().toString(),
                address = account.address,
                name = name,
                storeKeyId = storeKey.id,
                derivationPath = account.derivationPath,
                extendedPublicKey = account.extendedPublicKey,
                coin = account.coin,
                platformType = platformType,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            database.storedKeyDao().add(listOf(storeKey))
            database.walletDao().add(listOf(db))
            setCurrentWallet(db)
        }
    }

    override suspend fun getKeyStore(
        walletData: WalletData,
        platformType: CoinPlatformType,
        paymentPassword: String,
    ): String {
        return database.walletDao().getById(walletData.id)?.let {
            val walletKey = WalletKey.load(it.storedKey.data).firstOrNull() ?: return@let ""
            when (it.storedKey.source) {
                WalletSource.ImportedKeyStore, WalletSource.ImportedPrivateKey -> walletKey.exportKeyStoreJsonOfAddress(
                    platformType.coinType,
                    it.wallet.address,
                    "",
                    paymentPassword
                )
                WalletSource.Created, WalletSource.ImportedMnemonic -> {
                    walletKey.exportKeyStoreJsonOfPath(
                        platformType.coinType,
                        platformType.derivationPath.toString(),
                        "",
                        paymentPassword
                    )
                }
                WalletSource.WalletConnect -> ""
            }
        } ?: ""
    }

    override suspend fun getPrivateKey(
        walletData: WalletData,
        platformType: CoinPlatformType,
    ): String {
        return database.walletDao().getById(walletData.id)?.let {
            WalletKey.load(it.storedKey.data).firstOrNull()
        }?.exportPrivateKey(platformType.coinType, "") ?: ""
    }

    override suspend fun getTotalBalance(address: String): Double {
        return services.debankServices.totalBalance(address).totalUsdValue ?: 0.0
    }

    override fun deleteCurrentWallet() {
        scope.launch {
            val currentWallet = currentWallet.firstOrNull() ?: return@launch
            deleteWallet(currentWallet.id)
        }
    }

    override fun deleteWallet(id: String) {
        scope.launch {
            // get it before remove
            val currentWallet = currentWallet.firstOrNull()

            val tokenWallet = database.walletDao().getById(id) ?: return@launch
            database.walletDao().deleteById(tokenWallet.wallet.id)
            database.storedKeyDao().deleteById(tokenWallet.storedKey.id)
            database.walletBalanceDao().deleteByWalletId(tokenWallet.wallet.id)
            database.walletTokenDao().deleteByWalletId(tokenWallet.wallet.id)

            if (currentWallet?.id == id) {
                val next = database.walletDao().getAll().firstOrNull { it.wallet.id != id }
                setCurrentWallet(next?.wallet)
            }
        }
    }

    override fun renameWallet(value: String, id: String) {
        scope.launch {
            database.walletDao().getById(id)?.wallet?.copy(name = value)?.let {
                database.walletDao().add(listOf(it))
            }
        }
    }

    override fun renameCurrentWallet(value: String) {
        scope.launch {
            currentWallet.firstOrNull()?.let { wallet ->
                database.walletDao().getById(wallet.id)?.wallet
            }?.copy(name = value)?.let {
                database.walletDao().add(listOf(it))
            }
        }
    }

    override fun sendTokenWithCurrentWallet(
        amount: BigDecimal,
        address: String,
        tokenData: TokenData,
        gasLimit: Double,
        maxFee: Double,
        maxPriorityFee: Double,
        data: String,
        onDone: (String?) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        sendTokenWithCurrentWalletAndChainType(
            amount = amount,
            address = address,
            chainType = tokenData.chainType,
            gasLimit = gasLimit,
            maxFee = maxFee,
            maxPriorityFee = maxPriorityFee,
            onDone = onDone,
            onError = onError,
            data = data,
        )
    }

    override fun sendTokenWithCurrentWalletAndChainType(
        amount: BigDecimal,
        address: String,
        chainType: ChainType,
        gasLimit: Double,
        maxFee: Double,
        maxPriorityFee: Double,
        data: String,
        onDone: (String?) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        scope.launch {
            try {
                val hash = currentWallet.firstOrNull()?.let { wallet ->
                    if (wallet.fromWalletConnect) {
                        walletConnectManager.sendToken(
                            amount = amount,
                            fromAddress = wallet.address,
                            toAddress = address,
                            data = data,
                            gasLimit = gasLimit,
                            gasPrice = maxFee.toBigDecimal().gwei.ether + maxPriorityFee.toBigDecimal().gwei.ether,
                            onResponse = { response, error ->
                                error?.let { onError(it) } ?: onDone(response.toString())
                            }
                        )
                        return@launch
                    }
                    val credentials = Credentials.create(
                        getPrivateKey(
                            wallet,
                            CoinPlatformType.Ethereum
                        )
                    )
                    val actualAmount = if (chainType == ChainType.eth) {
                        amount.ether.wei.toBigInteger()
                    } else {
                        null
                    }
                    val web3 = Web3j.build(chainType.httpService)
                    val manager = RawTransactionManager(web3, credentials, chainType.chainId)
                    val result = if (chainType.supportEip25519) {
                        manager.sendEIP1559Transaction(
                            chainType.chainId,
                            maxPriorityFee.gwei.wei.toBigInteger(),
                            maxFee.gwei.wei.toBigInteger(),
                            gasLimit.toBigDecimal().toBigInteger(),
                            address,
                            data,
                            actualAmount,
                        )
                    } else {
                        manager.sendTransaction(
                            maxPriorityFee.gwei.wei.toBigInteger(),
                            gasLimit.toBigDecimal().toBigInteger(),
                            address,
                            data,
                            amount.ether.wei.toBigInteger()
                        )
                    }
                    if (result.hasError()) {
                        Log.e(
                            "WalletRepository",
                            "sendTokenWithCurrentWallet: ${result.error?.code}: ${result.error?.message}",
                        )
                        Log.e(
                            "WalletRepository",
                            "sendTokenWithCurrentWallet: ${result.error?.data}",
                        )
                        throw Exception(result.error?.message ?: "")
                    }
                    web3.shutdown()
                    result.transactionHash
                }
                onDone.invoke(hash)
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }

    override fun validatePrivateKey(privateKey: String) =
        WalletKey.validate(privateKey = privateKey)

    override fun validateMnemonic(mnemonic: String) = WalletKey.validate(mnemonic = mnemonic)

    override fun validateKeystore(keyStore: String) = WalletKey.validate(keyStoreJSON = keyStore)

    override fun sendTokenWithCurrentWallet(
        amount: BigDecimal,
        address: String,
        tokenData: TokenData,
        gasLimit: Double,
        maxFee: Double,
        maxPriorityFee: Double,
        onDone: (String?) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        scope.launch {
            val isNativeToken =
                tokenData.address == database.chainDao().getByIdFlow(tokenData.chainType.chainId)
                    .firstOrNull()?.token?.address
            val realAddress = if (isNativeToken) {
                if (EnsResolver.isValidEnsName(address)) {
                    val web3 = Web3j.build(HttpService(tokenData.chainType.endpoint, okHttpClient))
                    val ensResolver = EnsResolver(web3)
                    ensResolver.resolve(address).also {
                        web3.shutdown()
                    }
                } else {
                    address
                }
            } else {
                // use token's contract address
                tokenData.address
            }
            val realAmount = if (isNativeToken) amount else BigDecimal.ZERO
            val data = if (isNativeToken) {
                ""
            } else {
                Function(
                    "transfer",
                    listOf(
                        Address(address),
                        Uint256((amount * (10.0.pow(tokenData.decimals.toInt())).toBigDecimal()).toBigInteger())
                    ),
                    listOf(),
                ).let {
                    FunctionEncoder.encode(it)
                }
            }
            sendTokenWithCurrentWallet(
                amount = realAmount,
                address = realAddress,
                tokenData = tokenData,
                gasLimit = gasLimit,
                maxFee = maxFee,
                maxPriorityFee = maxPriorityFee,
                data = data,
                onDone = onDone,
                onError = onError
            )
        }
    }

    private suspend fun getWalletKey(walletData: WalletData): WalletKey? {
        return database.walletDao().getById(walletData.id)?.storedKey?.data?.let {
            WalletKey.load(it)
        }?.firstOrNull()
    }

    private fun createNewMnemonic(password: String = ""): String {
        return WalletKey.create(password).mnemonic
    }

    override suspend fun getEnsAddress(chainType: ChainType, name: String): String {
        return withContext(Dispatchers.IO) {
            val web3 = Web3j.build(chainType.httpService)
            EnsResolver(web3).resolve(name).apply {
                web3.shutdown()
            }
        }
    }
}

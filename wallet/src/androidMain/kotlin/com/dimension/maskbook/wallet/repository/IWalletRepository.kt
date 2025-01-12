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

import com.dimension.maskbook.common.bigDecimal.BigDecimal
import com.dimension.maskbook.common.okhttp.okHttpClient
import com.dimension.maskbook.debankapi.model.ChainID
import com.dimension.maskbook.wallet.db.model.CoinPlatformType
import com.dimension.maskbook.wallet.db.model.DbCollectible
import com.dimension.maskbook.wallet.db.model.DbWalletTokenTokenWithWallet
import com.dimension.maskbook.wallet.db.model.DbWalletTokenWithToken
import com.dimension.maskbook.wallet.db.model.WalletSource
import com.dimension.maskbook.wallet.export.model.ChainType
import com.dimension.maskbook.wallet.export.model.TokenData
import com.dimension.maskbook.wallet.export.model.WalletData
import com.dimension.maskbook.wallet.export.model.WalletTokenData
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.web3j.protocol.http.HttpService

data class WalletCreateOrImportResult(
    val type: Type,
    val wallet: WalletData? = null,
    val title: String? = null, // Error, Create Wallet Success...
    val message: String? = null
) {
    enum class Type {
        SUCCESS,
        ERROR,
        WARNING // e.g wallet already exists
    }
}

fun WalletData.Companion.fromDb(data: DbWalletTokenTokenWithWallet) = with(data) {
    WalletData(
        id = wallet.id,
        name = wallet.name,
        address = wallet.address,
        imported = storedKey.source == WalletSource.ImportedKeyStore || storedKey.source == WalletSource.ImportedMnemonic || storedKey.source == WalletSource.ImportedPrivateKey,
        fromWalletConnect = storedKey.source == WalletSource.WalletConnect,
        tokens = items.map {
            WalletTokenData.fromDb(it)
        },
        balance = balance.associate { it.type to it.value },
        walletConnectChainType = wallet.walletConnectChainType,
        walletConnectDeepLink = wallet.walletConnectDeepLink
    )
}

fun WalletTokenData.Companion.fromDb(data: DbWalletTokenWithToken) = with(data) {
    WalletTokenData(
        count = reference.count,
        tokenAddress = token.address,
        tokenData = TokenData.fromDb(token)
    )
}

data class WalletCollectibleData(
    val id: String,
    val chainType: ChainType,
    val icon: String?,
    val name: String,
    val items: List<WalletCollectibleItemData>,
) {
    companion object {
        fun fromDb(data: DbCollectible) = with(data) {
            WalletCollectibleData(
                id = _id,
                chainType = chainType,
                icon = this.collection.imageURL,
                name = collection.name ?: name,
                items = listOf(
                    WalletCollectibleItemData(
                        id = _id,
                        link = this.permalink ?: this.externalLink ?: "",
                        imageUrl = this.url.imageURL ?: this.url.imageOriginalURL ?: "",
                        previewUrl = this.url.imagePreviewURL ?: this.url.imageThumbnailURL ?: "",
                        videoUrl = this.url.animationOriginalURL ?: this.url.animationURL ?: "",
                    )
                ),
            )
        }
    }
}

data class WalletCollectibleItemData(
    val id: String,
    val link: String,
    val previewUrl: String?,
    val imageUrl: String?,
    val videoUrl: String?,
)

enum class TransactionType {
    Swap,
    Receive,
    Send,
    Approve,
    Cancel,
    Unknown,
}

enum class TransactionStatus {
    Success,
    Failure,
    Pending,
}

data class TransactionData(
    val id: String,
    val type: TransactionType,
    val count: BigDecimal,
    val status: TransactionStatus,
    val message: String,
    val createdAt: Long,
    val updatedAt: Long,
    val tokenData: TokenData,
)

data class SearchAddressResult(
    val query: String, // bind  result to query
    val success: Boolean,
    val errorMsg: String? = null,
    val data: ISearchAddressResultData? = null
)

interface ISearchAddressResultData

data class MultipleAddressResultData(
    val contacts: List<SearchAddressData>,
    val suggestions: List<SearchAddressData>
) : ISearchAddressResultData

data class SingleAddressResultData(
    val address: SearchAddressData
) : ISearchAddressResultData

enum class GasPriceEditMode {
    LOW,
    MEDIUM,
    HIGH,
    CUSTOM
}

enum class UnlockType {
    PASSWORD,
    BIOMETRIC
}

fun String.toChainType(): ChainType {
    return kotlin.runCatching { ChainType.valueOf(this) }.getOrNull() ?: ChainType.unknown
}

val ChainType.httpService: HttpService
    get() = HttpService(endpoint, okHttpClient)

val ChainType.dbank: ChainID
    get() = when (this) {
        ChainType.eth -> ChainID.eth
        ChainType.rinkeby -> ChainID.eth
        ChainType.bsc -> ChainID.bsc
        ChainType.polygon -> ChainID.matic
        ChainType.arbitrum -> ChainID.arb
        ChainType.xdai -> ChainID.xdai
        ChainType.optimism -> ChainID.op
        ChainType.polka -> ChainID.eth
        else -> throw NotImplementedError("ChainType $this not supported")
    }

val ChainID.chainType: ChainType
    get() = when (this) {
        ChainID.eth -> ChainType.eth
        ChainID.bsc -> ChainType.bsc
        ChainID.matic -> ChainType.polygon
        ChainID.arb -> ChainType.arbitrum
        ChainID.xdai -> ChainType.xdai
        ChainID.op -> ChainType.optimism
        else -> ChainType.unknown
    }

data class DWebData(
    val coinPlatformType: CoinPlatformType,
    val chainType: ChainType,
)

data class SendTokenConfirmData(
    val data: SendTransactionData,
    val id: Any,
    val onDone: (String?) -> Unit,
    val onCancel: () -> Unit,
    val onError: (Throwable) -> Unit,
)

data class ChainData(
    val chainId: Long,
    val name: String,
    val fullName: String,
    val nativeTokenID: String,
    val logoURL: String,
    val nativeToken: TokenData?,
    val chainType: ChainType
)

@Serializable
data class SendTransactionData(
    val from: String? = null,
    val to: String? = null,
    val value: String? = null,
    val gas: String? = null,
    val gasPrice: String? = null,
    val data: String? = null,
    val nonce: Long? = null,
    val chainId: Long? = null,
    val common: SendTransactionDataCommon? = null,
    val chain: String? = null,
    val hardfork: String? = null,
)

@Serializable
data class SendTransactionDataCommon(
    val customChain: CustomChainParams?,
    val baseChain: String?,
    val hardfork: String?,
)

@Serializable
data class CustomChainParams(
    val name: String?,
    val networkId: Long?,
    val chainId: Long?,
)

interface IWalletRepository {
    fun init()
    val dWebData: Flow<DWebData>
    fun setActiveCoinPlatformType(platformType: CoinPlatformType)
    fun setChainType(networkType: ChainType, notifyJS: Boolean = true)
    suspend fun findWalletByAddress(address: String): WalletData?
    val wallets: Flow<List<WalletData>>
    val currentWallet: Flow<WalletData?>
    val currentChain: Flow<ChainData?>
    fun setCurrentWallet(walletData: WalletData?)
    fun setCurrentWallet(walletId: String)
    fun generateNewMnemonic(): List<String>
    suspend fun createWallet(mnemonic: List<String>, name: String, platformType: CoinPlatformType)
    suspend fun importWallet(mnemonicCode: List<String>, name: String, path: List<String>, platformType: CoinPlatformType)
    suspend fun importWallet(name: String, keyStore: String, password: String, platformType: CoinPlatformType)
    suspend fun importWallet(name: String, privateKey: String, platformType: CoinPlatformType)
    suspend fun getKeyStore(walletData: WalletData, platformType: CoinPlatformType, paymentPassword: String): String
    suspend fun getPrivateKey(walletData: WalletData, platformType: CoinPlatformType): String
    suspend fun getTotalBalance(address: String): Double
    fun deleteCurrentWallet()
    fun deleteWallet(id: String)
    fun renameWallet(value: String, id: String)
    fun renameCurrentWallet(value: String)
    fun sendTokenWithCurrentWallet(
        amount: BigDecimal,
        address: String,
        tokenData: TokenData,
        gasLimit: Double,
        maxFee: Double,
        maxPriorityFee: Double,
        onDone: (String?) -> Unit = {},
        onError: (Throwable) -> Unit = {},
    )
    fun sendTokenWithCurrentWallet(
        amount: BigDecimal,
        address: String,
        tokenData: TokenData,
        gasLimit: Double,
        maxFee: Double,
        maxPriorityFee: Double,
        data: String,
        onDone: (String?) -> Unit = {},
        onError: (Throwable) -> Unit = {},
    )
    fun sendTokenWithCurrentWalletAndChainType(
        amount: BigDecimal,
        address: String,
        chainType: ChainType,
        gasLimit: Double,
        maxFee: Double,
        maxPriorityFee: Double,
        data: String,
        onDone: (String?) -> Unit,
        onError: (Throwable) -> Unit
    )
    fun validatePrivateKey(privateKey: String): Boolean
    fun validateMnemonic(mnemonic: String): Boolean
    fun validateKeystore(keyStore: String): Boolean
    suspend fun getEnsAddress(chainType: ChainType, name: String): String
}

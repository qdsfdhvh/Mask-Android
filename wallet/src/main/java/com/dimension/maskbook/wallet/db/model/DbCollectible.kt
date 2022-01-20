package com.dimension.maskbook.wallet.db.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dimension.maskbook.wallet.repository.ChainType

@Entity(
    indices = [Index(value = ["walletId", "tokenId", "id"], unique = true)],
)
data class DbCollectible(
    @PrimaryKey val _id: String,
    val walletId: String,
    val chainType: ChainType,
    val tokenId: String,
    val externalLink: String? = null,
    val permalink: String? = null,
    val id: Long,
    val description: String? = null,
    val name: String,
    @Embedded(prefix = "creator_")
    val creator: DbCollectibleCreator,
    @Embedded(prefix = "collection_")
    val collection: DbCollectibleCollection,
    @Embedded(prefix = "contract_")
    val contract: DbCollectibleContract,
    @Embedded(prefix = "url_")
    val url: DbCollectibleUrl,
)

data class DbCollectibleCollection(
    val imageURL: String? = null,
    val name: String? = null,
)

data class DbCollectibleCreator(
    val userName: String? = null,
    val profileImgURL: String? = null,
    val address: String? = null,
    val config: String? = null
)

data class DbCollectibleUrl(
    val imageURL: String? = null,
    val imagePreviewURL: String? = null,
    val imageThumbnailURL: String? = null,
    val imageOriginalURL: String? = null,
    val animationURL: String? = null,
    val animationOriginalURL: String? = null,
)

data class DbCollectibleContract(
    val address: String,
    val imageUrl: String,
    val name: String,
    val symbol: String,
)
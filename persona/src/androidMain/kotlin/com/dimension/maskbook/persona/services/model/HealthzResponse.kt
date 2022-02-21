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
package com.dimension.maskbook.persona.services.model

import com.dimension.maskbook.persona.export.model.Platform
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthzResponse(
    val hello: String? = null,
    val platforms: List<Platform>? = null
)

@Serializable
data class ProofPayloadBody(
    val action: Action,
    val platform: Platform,
    val identity: String,
    @SerialName("public_key")
    val publicKey: String
)

@Serializable
enum class Action {
    @SerialName("create")
    Create,

    @SerialName("delete")
    Delete,
}

@Serializable
data class ProofPayloadResponse(
    @SerialName("post_content")
    val postContent: String? = null,

    @SerialName("sign_payload")
    val signPayload: String? = null
)

@Serializable
data class ProofModifyBody(
    val action: Action,
    val platform: Platform,
    val identity: String,
    @SerialName("proof_location")
    val proofLocation: String? = null,
    @SerialName("public_key")
    val publicKey: String,
    val extra: Extra? = null
) {
    @Serializable
    data class Extra(
        @SerialName("wallet_signature")
        val walletSignature: String? = null,
        val signature: String? = null
    )
}

@Serializable
data class ProofQueryResponse(
    val ids: List<ID>? = null
)

@Serializable
data class ID(
    val persona: String? = null,
    val proofs: List<Proof>? = null
)

@Serializable
data class Proof(
    val platform: Platform? = null,
    val identity: String? = null
)

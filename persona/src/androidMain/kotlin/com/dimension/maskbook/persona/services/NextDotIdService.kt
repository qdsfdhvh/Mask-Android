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
package com.dimension.maskbook.persona.services

import com.dimension.maskbook.persona.services.model.HealthzResponse
import com.dimension.maskbook.persona.services.model.ProofModifyBody
import com.dimension.maskbook.persona.services.model.ProofPayloadBody
import com.dimension.maskbook.persona.services.model.ProofPayloadResponse
import com.dimension.maskbook.persona.services.model.ProofQueryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NextDotIdService {
    @GET("/healthz")
    suspend fun healthz(): HealthzResponse

    @POST("/v1/proof/payload")
    suspend fun payload(@Body body: ProofPayloadBody): ProofPayloadResponse

    @POST("/v1/proof")
    suspend fun modifyProof(@Body body: ProofModifyBody)

    @GET("/v1/proof")
    suspend fun queryProof(
        @Query("identity") identity: String,
        @Query("platform") platform: String? = null,
    ): ProofQueryResponse
}

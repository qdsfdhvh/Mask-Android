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
package com.dimension.maskbook.persona.repository

import android.util.Base64
import com.dimension.maskbook.persona.db.PersonaDatabase
import com.dimension.maskbook.persona.export.model.ConnectedNextIdProfile
import com.dimension.maskbook.persona.export.model.Platform
import com.dimension.maskbook.persona.services.NextDotIdService
import com.dimension.maskbook.persona.services.model.Action
import com.dimension.maskbook.persona.services.model.ProofPayloadBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.math.BigInteger

class NextIdRepository(
    private val database: PersonaDatabase,
    private val service: NextDotIdService,
    private val personaRepository: PersonaRepository,
) {
    fun getConnectedNextId(personaId: String): Flow<List<ConnectedNextIdProfile>> {
        return database.nextIdDao().getConnectedNextId(personaId).map {
            it.map {
                it.export()
            }
        }
    }
    suspend fun createProof(identity: String, platform: Platform, personaId: String): String {
        val persona = personaRepository.currentPersona.firstOrNull() ?: throw IllegalStateException("No persona is selected")
        val publicKey = personaRepository.backupPrivateKey(persona.id).let {
            Base64.decode(it, Base64.URL_SAFE).let {
                BigInteger(it)
            }.let {
                Sign.publicKeyFromPrivate(it)
            }.let {
                Numeric.toHexStringWithPrefix(it)
            }
        }
        val response = service.payload(
            ProofPayloadBody(
                identity = identity,
                action = Action.Create,
                platform = platform,
                publicKey = publicKey,
            )
        )
        TODO()
    }
}

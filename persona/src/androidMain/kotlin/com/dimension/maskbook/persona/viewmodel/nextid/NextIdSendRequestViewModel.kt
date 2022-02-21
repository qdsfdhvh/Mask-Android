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
package com.dimension.maskbook.persona.viewmodel.nextid

import android.util.Base64
import androidx.lifecycle.ViewModel
import com.dimension.maskbook.persona.export.model.Platform
import com.dimension.maskbook.persona.repository.IPersonaRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.math.BigInteger

class NextIdSendRequestViewModel(
    private val input: String,
    private val platform: Platform,
    private val personaRepository: IPersonaRepository,
) : ViewModel() {
    val data by lazy {
        personaRepository.currentPersona.mapNotNull { it }.map {
            personaRepository.backupPrivateKey(it.id)
        }.map {
            Base64.decode(it, Base64.URL_SAFE).let {
                BigInteger(it)
            }.let {
                Sign.publicKeyFromPrivate(it)
            }.let {
                Numeric.toHexStringWithPrefix(it)
            }.let {
            }
        }
    }
}

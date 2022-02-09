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
package com.dimension.maskbook.persona.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimension.maskbook.persona.repository.IPersonaRepository
import com.dimension.maskbook.wallet.ext.asStateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class PersonaViewModel(
    private val repository: IPersonaRepository
) : ViewModel() {

    val currentPersona by lazy {
        repository.currentPersona.asStateIn(viewModelScope, null)
    }

    val socialList by lazy {
        combine(
            currentPersona,
            repository.twitter,
            repository.facebook
        ) { persona, twitterList, facebookList ->
            val isEmpty = twitterList.isEmpty() && facebookList.isEmpty()
            if (isEmpty) {
                return@combine emptyList()
            }

            val allList = twitterList + facebookList
            if (persona == null) {
                return@combine allList
            }

            allList.filter { it.personaId == persona.id }
        }.flowOn(Dispatchers.IO).asStateIn(viewModelScope, null)
    }

    init {
        loadPersona()
    }

    private fun loadPersona() = viewModelScope.launch {
    }
}
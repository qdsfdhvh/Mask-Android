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

import androidx.lifecycle.ViewModel
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dimension.maskbook.persona.repository.IPersonaRepository
import com.dimension.maskbook.persona.services.NextDotIdService
import com.dimension.maskbook.persona.services.model.Proof
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull

class NextIdHomeViewModel(
    private val personaRepository: IPersonaRepository
) : ViewModel() {
    val source by lazy {
        personaRepository.currentPersona.mapNotNull { it }.flatMapLatest {
            personaRepository.getConnectedNextId(it.id)
        }
    }
    // val source by lazy {
    //     personaRepository.currentPersona.mapNotNull { it }.map {
    //         personaRepository.backupPrivateKey(it.id)
    //     }.map {
    //         Base64.decode(it, Base64.URL_SAFE).let {
    //             BigInteger(it)
    //         }.let {
    //             Sign.publicKeyFromPrivate(it)
    //         }.let {
    //             Numeric.toHexStringWithPrefix(it)
    //         }.let {
    //             NextIdProofSource(nextDotIdService, it)
    //         }
    //     }
    // }
    // val pager by lazy {
    //     source.flatMapLatest {
    //         Pager(
    //             config = PagingConfig(pageSize = 10),
    //             pagingSourceFactory = { it }
    //         ).flow.cachedIn(viewModelScope)
    //     }
    // }
}

class NextIdProofSource(
    private val service: NextDotIdService,
    private val id: String,
) : PagingSource<Int, Proof>() {
    override fun getRefreshKey(state: PagingState<Int, Proof>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Proof> {
        return try {
            service.queryProof(identity = id).ids?.let {
                it.firstOrNull { it.persona == id }
            }?.proofs?.let {
                LoadResult.Page(
                    data = it,
                    prevKey = null,
                    nextKey = null,
                )
            } ?: LoadResult.Error(Exception("No proofs found"))
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

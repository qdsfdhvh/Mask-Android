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
package com.dimension.maskbook.wallet.usecase.token

import com.dimension.maskbook.common.bigDecimal.BigDecimal
import com.dimension.maskbook.common.ext.ifNullOrEmpty
import com.dimension.maskbook.wallet.export.model.TokenData
import com.dimension.maskbook.wallet.repository.IWalletRepository
import com.dimension.maskbook.wallet.usecase.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface SendTokenUseCase {
    operator fun invoke(
        amount: BigDecimal,
        address: String,
        tokenData: TokenData,
        gasLimit: Double,
        maxFee: Double,
        maxPriorityFee: Double,
    ): Flow<Result<String>>
}

class SendTokenUseCaseImpl(
    val repository: IWalletRepository,
) : SendTokenUseCase {
    val result = MutableStateFlow<Result<String>>(Result.Loading())
    override fun invoke(
        amount: BigDecimal,
        address: String,
        tokenData: TokenData,
        gasLimit: Double,
        maxFee: Double,
        maxPriorityFee: Double
    ): Flow<Result<String>> {
        try {
            result.value = Result.Loading()
            repository.sendTokenWithCurrentWallet(
                amount = amount,
                address = address,
                tokenData = tokenData,
                gasLimit = gasLimit,
                maxFee = maxFee,
                maxPriorityFee = maxPriorityFee,
                onError = {
                    result.value = Result.Failed(it)
                },
                onDone = {
                    result.value = Result.Success(it.ifNullOrEmpty { "" })
                }
            )
        } catch (e: Throwable) {
            result.value = Result.Failed(e)
        }
        return result
    }
}

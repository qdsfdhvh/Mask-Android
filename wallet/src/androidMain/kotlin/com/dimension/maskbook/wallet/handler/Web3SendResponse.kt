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
package com.dimension.maskbook.wallet.handler

import com.dimension.maskbook.common.ext.encodeMap
import com.dimension.maskbook.extension.export.model.ExtensionId
import com.dimension.maskbook.extension.export.model.buildExtensionResponse

class Web3SendResponse {
    @kotlinx.serialization.Serializable
    data class ErrorResult(
        val id: ExtensionId,
        val jsonrpc: String,
        val error: String,
    )

    companion object {
        inline fun <reified T : Any> success(
            messageId: ExtensionId,
            jsonrpc: String,
            payloadId: ExtensionId,
            result: T?
        ) = buildExtensionResponse(
            id = messageId,
            jsonrpc = jsonrpc,
            result = buildExtensionResponse(
                id = payloadId,
                jsonrpc = jsonrpc,
                result = result
            )
        )

        fun error(
            messageId: ExtensionId,
            jsonrpc: String,
            payloadId: ExtensionId,
            error: String
        ) = buildExtensionResponse(
            id = messageId,
            jsonrpc = jsonrpc,
            result = ErrorResult(
                id = payloadId,
                jsonrpc = jsonrpc,
                error = error,
            ).encodeMap()
        )
    }
}

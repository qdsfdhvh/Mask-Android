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
package com.dimension.maskbook.common.ext

import android.util.Base64

fun ByteArray.encodeBase64(): ByteArray {
    return Base64.encode(this, Base64.DEFAULT)
}

fun ByteArray.encodeBase64String(): String {
    return String(encodeBase64())
}

fun ByteArray.decodeBase64(): ByteArray {
    return Base64.decode(this, Base64.DEFAULT)
}

fun ByteArray.decodeBase64String(): String {
    return String(decodeBase64())
}

fun String.encodeBase64Bytes(): ByteArray {
    return toByteArray().encodeBase64()
}

fun String.encodeBase64(): String {
    return String(encodeBase64Bytes())
}

fun String.decodeBase64Bytes(): ByteArray {
    return toByteArray().decodeBase64()
}

fun String.decodeBase64(): String {
    return String(decodeBase64Bytes())
}

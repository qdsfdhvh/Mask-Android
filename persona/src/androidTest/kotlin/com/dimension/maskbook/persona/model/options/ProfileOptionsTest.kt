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
package com.dimension.maskbook.persona.model.options

import com.dimension.maskbook.common.ext.decodeJson
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ProfileOptionsTest {

    @Test
    fun test_attach_profile_options_decodeJson() {
        val jsonData = "{\"profileIdentifier\":\"person:twitter.com\\/AAA\",\"personaIdentifier\":\"aaa:asdasdasd\",\"state\":{\"connectionConfirmState\":\"confirmed\"}}"
        val options: AttachProfileOptions = jsonData.decodeJson()
        assertEquals(options.profileIdentifier, "person:twitter.com/AAA")
        assertEquals(options.personaIdentifier, "aaa:asdasdasd")
    }
}
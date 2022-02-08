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
package com.dimension.maskbook.setting.ui.tab

import androidx.compose.runtime.Composable
import com.dimension.maskbook.common.ui.tab.TabScreen
import com.dimension.maskbook.setting.R
import com.dimension.maskbook.wallet.ui.scenes.settings.SettingsScene

class SettingsTabScreen : TabScreen {
    override val route = "Settings"
    override val title: Int = R.string.tab_setting
    override val icon: Int = R.drawable.ic_settings

    @Composable
    override fun Content(onBack: () -> Unit) {
        SettingsScene(onBack = onBack)
    }
}
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
package com.dimension.maskbook.wallet.ui.scenes.register

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.dimension.maskbook.common.route.navigationComposeBottomSheet
import com.dimension.maskbook.common.route.navigationComposeBottomSheetPackage
import com.dimension.maskbook.common.routeProcessor.annotations.NavGraphDestination
import com.dimension.maskbook.wallet.route.WalletRoute

@NavGraphDestination(
    route = WalletRoute.Register.Recovery.IdentityPersona,
    packageName = navigationComposeBottomSheetPackage,
    functionName = navigationComposeBottomSheet,
)
@Composable
fun IdentityPersona(
    navController: NavController,
) {
    CreatePersonaModal(
        onDone = { name ->
            navController.navigate(WalletRoute.Register.Recovery.Identity(name))
        }
    )
}

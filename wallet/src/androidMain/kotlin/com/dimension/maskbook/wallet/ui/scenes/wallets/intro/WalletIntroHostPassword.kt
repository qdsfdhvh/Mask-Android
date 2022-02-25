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
package com.dimension.maskbook.wallet.ui.scenes.wallets.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.dimension.maskbook.common.ext.observeAsState
import com.dimension.maskbook.common.route.navigationComposeBottomSheet
import com.dimension.maskbook.common.route.navigationComposeBottomSheetPackage
import com.dimension.maskbook.common.routeProcessor.annotations.NavGraphDestination
import com.dimension.maskbook.common.routeProcessor.annotations.Path
import com.dimension.maskbook.common.viewmodel.BiometricEnableViewModel
import com.dimension.maskbook.setting.export.SettingServices
import com.dimension.maskbook.wallet.route.WalletRoute
import com.dimension.maskbook.wallet.ui.scenes.wallets.create.CreateType
import com.dimension.maskbook.wallet.ui.scenes.wallets.intro.password.SetUpPaymentPassword
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@NavGraphDestination(
    route = WalletRoute.WalletIntroHostPassword.path,
    packageName = navigationComposeBottomSheetPackage,
    functionName = navigationComposeBottomSheet,
)
@Composable
fun WalletIntroHostPassword(
    navController: NavController,
    @Path("type") typeString: String,
) {
    val type = remember(typeString) { CreateType.valueOf(typeString) }
    val enableBiometric by get<SettingServices>().biometricEnabled.observeAsState(initial = false)
    val biometricEnableViewModel: BiometricEnableViewModel = getViewModel()
    val context = LocalContext.current
    SetUpPaymentPassword(
        onNext = {
            if (!enableBiometric && biometricEnableViewModel.isSupported(context)) {
                navController.navigate(WalletRoute.WalletIntroHostFaceId(type.name))
            } else {
                navController.navigate(WalletRoute.CreateOrImportWallet(type.name))
            }
        }
    )
}

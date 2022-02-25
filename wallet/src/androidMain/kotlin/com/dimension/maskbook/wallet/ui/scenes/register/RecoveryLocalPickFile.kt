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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.dimension.maskbook.common.route.navigationComposeAnimComposable
import com.dimension.maskbook.common.route.navigationComposeAnimComposablePackage
import com.dimension.maskbook.common.routeProcessor.annotations.NavGraphDestination
import com.dimension.maskbook.wallet.route.WalletRoute

@NavGraphDestination(
    route = WalletRoute.Register.Recovery.LocalBackup.LocalBackup_PickFile,
    packageName = navigationComposeAnimComposablePackage,
    functionName = navigationComposeAnimComposable,
)
@Composable
fun RecoveryLocalPickFile(
    navController: NavController,
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = {
            if (it != null) {
                navController.navigate(
                    WalletRoute.Register.Recovery.LocalBackup.RemoteBackupRecovery_RecoveryLocal(it.toString())

                ) {
                    popUpTo(WalletRoute.Register.Recovery.LocalBackup.LocalBackup_PickFile) {
                        inclusive = true
                    }
                }
            } else {
                navController.popBackStack()
            }
        },
    )
    LaunchedEffect(Unit) {
        filePickerLauncher.launch(arrayOf("*/*"))
    }
}

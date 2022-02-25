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
package com.dimension.maskbook.wallet.ui.scenes.wallets.management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dimension.maskbook.common.ext.observeAsState
import com.dimension.maskbook.common.route.navigationComposeDialog
import com.dimension.maskbook.common.route.navigationComposeDialogPackage
import com.dimension.maskbook.common.routeProcessor.annotations.Back
import com.dimension.maskbook.common.routeProcessor.annotations.NavGraphDestination
import com.dimension.maskbook.common.routeProcessor.annotations.Path
import com.dimension.maskbook.wallet.export.model.ChainType
import com.dimension.maskbook.wallet.route.WalletRoute
import com.dimension.maskbook.wallet.viewmodel.wallets.management.WalletSwitchViewModel
import org.koin.androidx.compose.getViewModel

@NavGraphDestination(
    route = WalletRoute.WalletNetworkSwitch.path,
    packageName = navigationComposeDialogPackage,
    functionName = navigationComposeDialog,
)
@Composable
fun WalletNetworkSwitch(
    @Back onBack: () -> Unit,
    @Path("target") chainTypeString: String,
) {
    val chainType = remember(chainTypeString) { ChainType.valueOf(chainTypeString) }
    val viewModel = getViewModel<WalletSwitchViewModel>()
    val currentNetwork by viewModel.network.observeAsState(initial = ChainType.eth)
    WalletNetworkSwitchWarningDialog(
        currentNetwork = currentNetwork.name,
        connectingNetwork = chainType.name,
        onCancel = onBack,
        onSwitch = {
            viewModel.setChainType(chainType)
            onBack.invoke()
        }
    )
}

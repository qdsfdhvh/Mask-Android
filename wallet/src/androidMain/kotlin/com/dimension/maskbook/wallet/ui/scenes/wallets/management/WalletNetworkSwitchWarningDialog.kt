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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.common.ext.observeAsState
import com.dimension.maskbook.common.route.navigationComposeDialog
import com.dimension.maskbook.common.route.navigationComposeDialogPackage
import com.dimension.maskbook.common.routeProcessor.annotations.Back
import com.dimension.maskbook.common.routeProcessor.annotations.NavGraphDestination
import com.dimension.maskbook.common.ui.widget.MaskDialog
import com.dimension.maskbook.common.ui.widget.button.PrimaryButton
import com.dimension.maskbook.common.ui.widget.button.SecondaryButton
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.export.model.ChainType
import com.dimension.maskbook.wallet.route.WalletRoute
import com.dimension.maskbook.wallet.viewmodel.wallets.management.WalletSwitchViewModel
import org.koin.androidx.compose.getViewModel

@NavGraphDestination(
    route = WalletRoute.WalletNetworkSwitchWarningDialog,
    packageName = navigationComposeDialogPackage,
    functionName = navigationComposeDialog,
)
@Composable
fun WalletNetworkSwitchWarningDialog(
    @Back onBack: () -> Unit,
) {
    val viewModel = getViewModel<WalletSwitchViewModel>()
    val currentNetwork by viewModel.network.observeAsState(initial = ChainType.eth)
    val currentWallet by viewModel.currentWallet.observeAsState(initial = null)
    val wallet = currentWallet ?: return

    if (!wallet.fromWalletConnect ||
        wallet.walletConnectChainType == currentNetwork ||
        wallet.walletConnectChainType == null
    ) {
        onBack.invoke()
    }

    WalletNetworkSwitchWarningDialog(
        currentNetwork = currentNetwork.name,
        connectingNetwork = wallet.walletConnectChainType?.name ?: "",
        onCancel = onBack,
        onSwitch = {
            wallet.walletConnectChainType?.let { type ->
                viewModel.setChainType(type)
            }
            onBack.invoke()
        }
    )
}

@Composable
fun WalletNetworkSwitchWarningDialog(
    currentNetwork: String,
    connectingNetwork: String,
    onCancel: () -> Unit,
    onSwitch: () -> Unit,
) {
    MaskDialog(
        onDismissRequest = onCancel,
        icon = {
            Image(
                painter = painterResource(id = R.drawable.ic_warn),
                contentDescription = "warning",
                Modifier.size(60.dp)
            )
        },
        text = {
            Column {
                Text(
                    text = buildAnnotatedString {
                        append("The current network ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("($currentNetwork)")
                        }
                        append(" is different from the connecting network ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("($connectingNetwork)")
                        }
                        append(". Do you want to switch your current network to ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(connectingNetwork)
                        }
                        append("?.")
                    },
                    style = MaterialTheme.typography.body1
                )
            }
        },
        buttons = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondaryButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Cancel")
                    }
                    PrimaryButton(
                        onClick = onSwitch,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Ok")
                    }
                }
            }
        }
    )
}

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
package com.dimension.maskbook.wallet.ui.scenes.register.recovery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dimension.maskbook.common.ext.observeAsState
import com.dimension.maskbook.common.route.navigationComposeAnimComposable
import com.dimension.maskbook.common.route.navigationComposeAnimComposablePackage
import com.dimension.maskbook.common.routeProcessor.annotations.Back
import com.dimension.maskbook.common.routeProcessor.annotations.NavGraphDestination
import com.dimension.maskbook.common.routeProcessor.annotations.Path
import com.dimension.maskbook.common.ui.widget.MaskInputField
import com.dimension.maskbook.common.ui.widget.MaskScaffold
import com.dimension.maskbook.common.ui.widget.MaskScene
import com.dimension.maskbook.common.ui.widget.MaskTopAppBar
import com.dimension.maskbook.common.ui.widget.ScaffoldPadding
import com.dimension.maskbook.common.ui.widget.button.MaskBackButton
import com.dimension.maskbook.common.ui.widget.button.PrimaryButton
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.route.WalletRoute
import com.dimension.maskbook.wallet.viewmodel.recovery.IdentityViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@NavGraphDestination(
    route = WalletRoute.Register.Recovery.Identity.path,
    packageName = navigationComposeAnimComposablePackage,
    functionName = navigationComposeAnimComposable,
)
@Composable
fun IdentityScene(
    navController: NavController,
    @Back onBack: () -> Unit,
    @Path("name") name: String,
) {
    val viewModel: IdentityViewModel = getViewModel {
        parametersOf(name)
    }
    val identity by viewModel.identity.observeAsState()
    val canConfirm by viewModel.canConfirm.observeAsState()

    MaskScene {
        MaskScaffold(
            topBar = {
                MaskTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.scene_identity_mnemonic_import_title))
                    },
                    navigationIcon = {
                        MaskBackButton(
                            onBack = onBack,
                        )
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(ScaffoldPadding),
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                MaskInputField(
                    value = identity,
                    onValueChange = {
                        viewModel.setIdentity(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder = {
                        Text(text = stringResource(R.string.scene_identity_mnemonic_import_placeholder))
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canConfirm,
                    onClick = {
                        viewModel.onConfirm()
                        navController.navigate(WalletRoute.Register.Recovery.Complected) {
                            popUpTo(WalletRoute.Register.Init) {
                                inclusive = true
                            }
                        }
                    },
                ) {
                    Text(text = stringResource(R.string.common_controls_confirm))
                }
            }
        }
    }
}

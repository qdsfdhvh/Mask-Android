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

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.dimension.maskbook.common.ext.observeAsState
import com.dimension.maskbook.common.route.CommonRoute
import com.dimension.maskbook.common.route.Deeplinks
import com.dimension.maskbook.common.route.navigationComposeAnimComposable
import com.dimension.maskbook.common.route.navigationComposeAnimComposablePackage
import com.dimension.maskbook.common.routeProcessor.annotations.NavGraphDestination
import com.dimension.maskbook.common.ui.widget.MaskScaffold
import com.dimension.maskbook.common.ui.widget.MaskScene
import com.dimension.maskbook.common.ui.widget.ScaffoldPadding
import com.dimension.maskbook.common.ui.widget.button.PrimaryButton
import com.dimension.maskbook.common.ui.widget.button.SecondaryButton
import com.dimension.maskbook.persona.export.PersonaServices
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.route.WalletRoute
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.get

@NavGraphDestination(
    route = WalletRoute.Register.Init,
    packageName = navigationComposeAnimComposablePackage,
    functionName = navigationComposeAnimComposable,
)
@Composable
fun RegisterScene(
    navController: NavController,
) {
    val repository = get<PersonaServices>()
    val persona by repository.currentPersona.observeAsState(initial = null)

    LaunchedEffect(Unit) {
        snapshotFlow { persona }
            .distinctUntilChanged()
            .collect {
                if (it != null) {
                    navController.navigate(
                        Uri.parse(Deeplinks.Main.Home(CommonRoute.Main.Tabs.Persona)),
                        navOptions {
                            popUpTo(WalletRoute.Register.Init) {
                                inclusive = true
                            }
                        }
                    )
                }
            }
    }

    MaskScene {
        MaskScaffold {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ScaffoldPadding),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painterResource(id = R.drawable.ic_group_132),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painterResource(id = R.drawable.ic_mask_logo),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(9.dp))
                    Text(
                        text = stringResource(R.string.scene_identity_empty_description),
                        style = LocalTextStyle.current.copy(color = MaterialTheme.colors.primary)
                    )
                }
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate(WalletRoute.Register.WelcomeCreatePersona)
                    },
                ) {
                    Text(text = stringResource(R.string.scene_identity_empty_create_an_identity))
                }
                Spacer(modifier = Modifier.height(16.dp))
                SecondaryButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        navController.navigate(WalletRoute.Register.Recovery.Home)
                    },
                ) {
                    Text(text = stringResource(R.string.scene_identity_empty_recovery_sign_in))
                }
//                Spacer(modifier = Modifier.height(16.dp))
//                SecondaryButton(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    onClick = { onSynchronization.invoke() },
//                ) {
//                    Text(text = stringResource(R.string.scene_identity_empty_synchronization))
//                }
            }
        }
    }
}

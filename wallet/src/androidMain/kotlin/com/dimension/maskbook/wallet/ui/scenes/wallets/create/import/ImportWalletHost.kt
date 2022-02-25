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
package com.dimension.maskbook.wallet.ui.scenes.wallets.create.import

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.dimension.maskbook.common.ext.encodeUrl
import com.dimension.maskbook.common.navHostAnimationDurationMillis
import com.dimension.maskbook.common.route.CommonRoute
import com.dimension.maskbook.common.route.Deeplinks
import com.dimension.maskbook.common.route.navigationComposeAnimComposable
import com.dimension.maskbook.common.route.navigationComposeAnimComposablePackage
import com.dimension.maskbook.common.routeProcessor.annotations.Back
import com.dimension.maskbook.common.routeProcessor.annotations.NavGraphDestination
import com.dimension.maskbook.common.routeProcessor.annotations.Path
import com.dimension.maskbook.wallet.route.WalletRoute
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@NavGraphDestination(
    route = WalletRoute.ImportWallet.path,
    packageName = navigationComposeAnimComposablePackage,
    functionName = navigationComposeAnimComposable,
)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ImportWalletHost(
    rootNavController: NavController,
    @Path("wallet") wallet: String,
    @Back onBack: () -> Unit,
) {
    val onDone = {
        rootNavController.navigate(
            Uri.parse(Deeplinks.Main.Home(CommonRoute.Main.Tabs.Wallet)),
            navOptions = navOptions {
                launchSingleTop = true
                popUpTo(CommonRoute.Main.Home) {
                    inclusive = false
                }
            }
        )
    }

    val navController = rememberAnimatedNavController()
    AnimatedNavHost(
        navController = navController,
        startDestination = "Import",
        route = "ImportWalletHost",
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(navHostAnimationDurationMillis))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(navHostAnimationDurationMillis))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(navHostAnimationDurationMillis))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(navHostAnimationDurationMillis))
        },
    ) {
        composable("Import") {
            ImportWalletScene(
                onBack = { onBack.invoke() },
                onMnemonic = { navController.navigate("Mnemonic") },
                onPassword = { navController.navigate("PrivateKey") },
                onKeystore = { navController.navigate("Keystore") }
            )
        }

        composable("Mnemonic") {
            ImportWalletMnemonicScene(
                onBack = { navController.popBackStack() },
                wallet = wallet,
                onDone = { navController.navigate("DerivationPath/${it.encodeUrl()}") }
            )
        }

        composable("PrivateKey") {
            ImportWalletPrivateKeyScene(
                onBack = { navController.popBackStack() },
                wallet = wallet,
                onDone = { onDone.invoke() }
            )
        }

        composable("Keystore") {
            ImportWalletKeyStoreScene(
                onBack = { navController.popBackStack() },
                wallet = wallet,
                onDone = { onDone.invoke() }
            )
        }

        composable(
            "DerivationPath/{mnemonicCode}",
            arguments = listOf(navArgument("mnemonicCode") { type = NavType.StringType })
        ) {
            ImportWalletDerivationPathScene(
                onBack = { navController.popBackStack() },
                onDone = { onDone.invoke() },
                wallet = wallet,
                code = it.arguments?.getString("mnemonicCode")?.split(" ").orEmpty(),
            )
        }
    }
}

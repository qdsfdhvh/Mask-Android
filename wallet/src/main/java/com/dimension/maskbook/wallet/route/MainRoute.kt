package com.dimension.maskbook.wallet.route

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.dimension.maskbook.wallet.ext.encodeUrl
import com.dimension.maskbook.wallet.ext.observeAsState
import com.dimension.maskbook.wallet.repository.AppKey
import com.dimension.maskbook.wallet.repository.IPersonaRepository
import com.dimension.maskbook.wallet.repository.ISettingsRepository
import com.dimension.maskbook.wallet.repository.Network
import com.dimension.maskbook.wallet.repository.PlatformType
import com.dimension.maskbook.wallet.ui.scenes.MainHost
import com.dimension.maskbook.wallet.ui.scenes.app.PluginSettingsScene
import com.dimension.maskbook.wallet.ui.scenes.app.settings.MarketTrendSettingsModal
import com.dimension.maskbook.wallet.ui.scenes.persona.BackUpPasswordModal
import com.dimension.maskbook.wallet.ui.scenes.persona.ExportPrivateKeyScene
import com.dimension.maskbook.wallet.ui.scenes.persona.LogoutDialog
import com.dimension.maskbook.wallet.ui.scenes.persona.PersonaMenuScene
import com.dimension.maskbook.wallet.ui.scenes.persona.RenamePersonaModal
import com.dimension.maskbook.wallet.ui.scenes.persona.SwitchPersonaModal
import com.dimension.maskbook.wallet.ui.scenes.persona.social.ConnectSocialModal
import com.dimension.maskbook.wallet.ui.scenes.persona.social.DisconnectSocialDialog
import com.dimension.maskbook.wallet.ui.scenes.persona.social.SelectPlatformModal
import com.dimension.maskbook.wallet.ui.scenes.wallets.collectible.CollectibleDetailScene
import com.dimension.maskbook.wallet.viewmodel.persona.RenamePersonaViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.SwitchPersonaViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.social.DisconnectSocialViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.UnlockWalletViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.collectible.CollectibleDetailViewModel
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@ExperimentalAnimationApi
@ExperimentalMaterialNavigationApi
fun NavGraphBuilder.mainRoute(
    navController: NavController,
    onBack: () -> Unit,
    builder: NavGraphBuilder.() -> Unit,
) {
    navigation(route = "Main", startDestination = "Home") {
        composable(
            "Home",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "maskwallet://Home/{tab}"
                }
            ),
            arguments = listOf(
                navArgument("tab") { type = NavType.StringType }
            )
        ) {
            MainHost(
                initialTab = it.arguments?.getString("tab").orEmpty(),
                onBack = onBack,
                onPersonaCreateClick = {
                    navController.navigate("WelcomeCreatePersona")
                },
                onPersonaRecoveryClick = {
                    navController.navigate("Recovery")
                },
                onPersonaNameClick = {
                    navController.navigate("PersonaMenu")
                },
                onAddSocialClick = { persona, network ->
                    val platform = when (network) {
                        Network.Twitter -> PlatformType.Twitter
                        Network.Facebook -> PlatformType.Facebook
                        else -> null // TODO support other network
                    }
                    if (platform == null) {
                        navController.navigate("SelectPlatform/${persona.id.encodeUrl()}")
                    } else {
                        navController.navigate("ConnectSocial/${persona.id.encodeUrl()}/${platform}")
                    }
                },
                onRemoveSocialClick = { persona, social ->
                    val platform = when (social.network) {
                        Network.Twitter -> PlatformType.Twitter
                        Network.Facebook -> PlatformType.Facebook
                        else -> null // TODO support other network
                    }
                    if (platform != null) {
                        navController.navigate("DisconnectSocial/${persona.id.encodeUrl()}/${platform}/${social.id.encodeUrl()}" +
                            "?personaName=${persona.name.encodeUrl()}&socialName=${social.name.encodeUrl()}")
                    }
                },
                onLabsSettingClick = {
                    navController.navigate("PluginSettings")
                },
                onLabsItemClick = { appKey ->
                    when (appKey) {
                        AppKey.Swap -> {
                            navController.navigate("MarketTrendSettings")
                        }
                        else -> Unit
                    }
                }
            )
        }
        composable("PluginSettings") {
            PluginSettingsScene(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        dialog("Logout") {
            val repository = get<IPersonaRepository>()
            LogoutDialog(
                onBack = {
                    navController.popBackStack()
                },
                onDone = {
                    repository.logout()
                    navController.popBackStack("Home", inclusive = false)
                }
            )
        }
        bottomSheet(
            "BackUpPassword/{target}",
            arguments = listOf(
                navArgument("target") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val target = backStackEntry.arguments?.getString("target")
            val viewModel = getViewModel<UnlockWalletViewModel>()
            val biometricEnable by viewModel.biometricEnabled.observeAsState(initial = false)
            val password by viewModel.password.observeAsState(initial = "")
            val passwordValid by viewModel.passwordValid.observeAsState(initial = false)
            val context = LocalContext.current
            BackUpPasswordModal(
                biometricEnabled = biometricEnable,
                password = password,
                onPasswordChanged = { viewModel.setPassword(it) },
                passwordValid = passwordValid,
                onConfirm = {
                    if (biometricEnable) {
                        viewModel.authenticate(
                            context = context,
                            onSuccess = {
                                target?.let {
                                    navController.navigate(it, navOptions {
                                        popUpTo("BackUpPassword") {
                                            inclusive = true
                                        }
                                    })
                                } ?: navController.popBackStack()
                            }
                        )
                    } else {
                        target?.let {
                            navController.navigate(it, navOptions {
                                popUpTo("BackUpPassword") {
                                    inclusive = true
                                }
                            })
                        } ?: navController.popBackStack()
                    }
                }
            )
        }

        bottomSheet("MarketTrendSettings") {
            MarketTrendSettingsModal()
        }
        composable("PersonaMenu") {
            val persona by get<IPersonaRepository>().currentPersona.observeAsState(initial = null)
            val repository = get<ISettingsRepository>()
            val backupPassword by repository.backupPassword.observeAsState(initial = "")
            val paymentPassword by repository.paymentPassword.observeAsState(initial = "")
            persona?.let {
                PersonaMenuScene(
                    personaData = it,
                    backupPassword = backupPassword,
                    paymentPassword = paymentPassword,
                    navController = navController,
                    onBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
        bottomSheet("SwitchPersona") {
            val viewModel = getViewModel<SwitchPersonaViewModel>()
            val current by viewModel.current.observeAsState(initial = null)
            val items by viewModel.items.observeAsState(initial = emptyList())
            SwitchPersonaModal(
                currentPersonaData = current,
                items = items,
                onAdd = {
                    navController.navigate("CreatePersona")
                },
                onItemClicked = {
                    viewModel.switch(it)
                }
            )
        }
        bottomSheet(
            "RenamePersona/{personaId}",
            arguments = listOf(
                navArgument("personaId") { type = NavType.StringType },
            )
        ) {
            val personaId = it.arguments?.getString("personaId")
            if (personaId != null) {
                val viewModel = getViewModel<RenamePersonaViewModel> {
                    parametersOf(personaId)
                }
                val name by viewModel.name.observeAsState(initial = "")
                RenamePersonaModal(
                    name = name,
                    onNameChanged = { value ->
                        viewModel.setName(value)
                    },
                    onDone = {
                        viewModel.confirm()
                        navController.popBackStack()
                    },
                )
            }
        }
        composable("ExportPrivateKey") {
            ExportPrivateKeyScene(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        bottomSheet(
            route = "SelectPlatform/{personaId}",
            arguments = listOf(
                navArgument("personaId") { type = NavType.StringType },
            ),
        ) {
            val personaId = it.arguments?.getString("personaId").orEmpty()
            SelectPlatformModal(
                onDone = { platform ->
                    navController.navigate("ConnectSocial/${personaId.encodeUrl()}/${platform}")
                }
            )
        }
        bottomSheet(
            route = "ConnectSocial/{personaId}/{platform}",
            arguments = listOf(
                navArgument("personaId") { type = NavType.StringType },
                navArgument("platform") { type = NavType.StringType },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "maskwallet://ConnectSocial/{personaId}/{platform}"
                }
            )
        ) {
            val personaId = it.arguments?.getString("personaId")
            val platform = it.arguments?.getString("platform")
            if (personaId != null && platform != null) {
                val repository = get<IPersonaRepository>()
                ConnectSocialModal(
                    onDone = {
                        repository.beginConnectingProcess(
                            personaId = personaId,
                            platformType = PlatformType.valueOf(platform),
                        )
                        onBack.invoke()
                    }
                )
            }
        }
        dialog(
            "DisconnectSocial/{personaId}/{platform}/{socialId}?personaName={personaName}&socialName={socialName}",
            arguments = listOf(
                navArgument("personaId") { type = NavType.StringType },
                navArgument("platform") { type = NavType.StringType },
                navArgument("socialId") { type = NavType.StringType },
            )
        ) {
            val personaId = it.arguments?.getString("personaId")
            val personaName = it.arguments?.getString("personaName").orEmpty()
            val platform = it.arguments?.getString("platform")?.let { PlatformType.valueOf(it) }
            val socialId = it.arguments?.getString("socialId")
            val socialName = it.arguments?.getString("socialName").orEmpty()
            val viewModel = getViewModel<DisconnectSocialViewModel>()
            if (personaId != null && platform != null && socialId != null) {
                DisconnectSocialDialog(
                    personaName = personaName,
                    socialName = socialName,
                    onBack = {
                        navController.popBackStack()
                    },
                    onConfirm = {
                        when (platform) {
                            PlatformType.Twitter ->
                                viewModel.disconnectTwitter(
                                    personaId = personaId,
                                    socialId = socialId
                                )
                            PlatformType.Facebook ->
                                viewModel.disconnectFacebook(
                                    personaId = personaId,
                                    socialId = socialId
                                )
                        }
                        navController.popBackStack()
                    },
                )
            }
        }
        builder()
    }
}
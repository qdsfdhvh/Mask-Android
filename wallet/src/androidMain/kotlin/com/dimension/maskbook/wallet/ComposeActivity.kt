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
package com.dimension.maskbook.wallet

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.room.Room
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.decode.SvgDecoder
import com.dimension.maskbook.wallet.db.AppDatabase
import com.dimension.maskbook.wallet.db.RoomMigrations
import com.dimension.maskbook.wallet.services.WalletServices
import com.dimension.maskbook.wallet.ui.MaskTheme
import com.dimension.maskbook.wallet.ui.Route
import com.dimension.maskbook.wallet.viewmodel.WelcomeViewModel
import com.dimension.maskbook.wallet.viewmodel.app.LabsViewModel
import com.dimension.maskbook.wallet.viewmodel.app.MarketTrendSettingsViewModel
import com.dimension.maskbook.wallet.viewmodel.app.PluginSettingsViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.ExportPrivateKeyViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.PersonaViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.RenamePersonaViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.SwitchPersonaViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.contacts.ContactsViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.post.PostViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.social.DisconnectSocialViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.social.FaceBookConnectSocialViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.social.FacebookSocialViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.social.TwitterConnectSocialViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.social.TwitterSocialViewModel
import com.dimension.maskbook.wallet.viewmodel.recovery.IdentityViewModel
import com.dimension.maskbook.wallet.viewmodel.recovery.PrivateKeyViewModel
import com.dimension.maskbook.wallet.viewmodel.recovery.RecoveryLocalViewModel
import com.dimension.maskbook.wallet.viewmodel.register.CreateIdentityViewModel
import com.dimension.maskbook.wallet.viewmodel.register.EmailRemoteBackupRecoveryViewModel
import com.dimension.maskbook.wallet.viewmodel.register.PhoneRemoteBackupRecoveryViewModel
import com.dimension.maskbook.wallet.viewmodel.register.RemoteBackupRecoveryViewModelBase
import com.dimension.maskbook.wallet.viewmodel.register.UserNameModalViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.BackUpPasswordViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.BiometricViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.SetUpPaymentPasswordViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.TokenDetailViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.TouchIdEnableViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.UnlockWalletViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.WalletBalancesViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.WalletConnectManagementViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.WalletConnectViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.WalletManagementModalViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.collectible.CollectibleDetailViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.create.CreateWalletRecoveryKeyViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.import.ImportWalletDerivationPathViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.import.ImportWalletKeystoreViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.import.ImportWalletMnemonicViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.import.ImportWalletPrivateKeyViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.management.WalletBackupViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.management.WalletDeleteViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.management.WalletRenameViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.management.WalletSwitchViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.management.WalletTransactionHistoryViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.send.AddContactViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.send.GasFeeViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.send.SearchAddressViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.send.SendConfirmViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.send.SendTokenDataViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.send.SendTokenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ComposeActivity : AppCompatActivity() {
    companion object {
        object Destination {
            val register = "Register"
            val main = "Main"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startDestination = intent.getStringExtra("startDestination") ?: Destination.register
        setContent {
            CompositionLocalProvider(
                LocalImageLoader provides ImageLoader.Builder(this).componentRegistry {
                    add(SvgDecoder(this@ComposeActivity))
                }.build()
            ) {
                MaskTheme {
                    App(
                        onBack = {
                            finish()
                        },
                        startDestination = startDestination,
                    )
                }
            }
        }
    }
}

@Composable
fun App(
    startDestination: String = "Register",
    onBack: () -> Unit,
) {
    Route(onBack = onBack, startDestination = startDestination)
}

val walletModules = module {
//    single<IPersonaRepository> { FakePersonaRepository() }
//    single<IPostRepository> { FakePostRepository() }
//    single<IContactsRepository> { FakeContactsRepository() }
//    single<IAppRepository> { FakeAppRepository() }
//    single<IWalletRepository> { FakeWalletRepository() }
//    single<ISettingsRepository> { FakeSettingsRepository() }

    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "maskbook")
            .setQueryExecutor(Dispatchers.IO.asExecutor())
            .setTransactionExecutor(Dispatchers.IO.asExecutor())
            .addMigrations(
                RoomMigrations.MIGRATION_6_7,
            )
            .build()
    }

    viewModel { (uri: Uri) -> RecoveryLocalViewModel(get(), uri, get<Context>().contentResolver) }
    viewModel { IdentityViewModel(get()) }
    viewModel { PrivateKeyViewModel(get()) }
    viewModel { (personaName: String) -> CreateIdentityViewModel(personaName, get()) }
    viewModel { PersonaViewModel(get()) }
    viewModel { TwitterSocialViewModel(get()) }
    viewModel { FacebookSocialViewModel(get()) }
    viewModel { WelcomeViewModel(get()) }
    viewModel { TwitterConnectSocialViewModel(get()) }
    viewModel { FaceBookConnectSocialViewModel(get()) }
    viewModel { DisconnectSocialViewModel(get()) }
    viewModel { SwitchPersonaViewModel(get()) }
    viewModel { (personaId: String) -> RenamePersonaViewModel(get(), personaId) }
    viewModel { ExportPrivateKeyViewModel(get()) }
    viewModel { PostViewModel(get(), get()) }
    viewModel { ContactsViewModel(get(), get()) }
    viewModel { LabsViewModel(get(), get()) }
    viewModel { PluginSettingsViewModel(get(), get()) }
    viewModel { MarketTrendSettingsViewModel(get()) }
    viewModel { (requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit) ->
        EmailRemoteBackupRecoveryViewModel(
            requestNavigate,
            get(),
        )
    }
    viewModel { (requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit) ->
        PhoneRemoteBackupRecoveryViewModel(
            requestNavigate,
            get()
        )
    }
    viewModel { UserNameModalViewModel(get()) }
    viewModel { CreateWalletRecoveryKeyViewModel(get()) }
    viewModel { SetUpPaymentPasswordViewModel(get()) }
    viewModel { TouchIdEnableViewModel() }
    viewModel { (wallet: String) -> ImportWalletKeystoreViewModel(wallet, get()) }
    viewModel { (wallet: String) -> ImportWalletPrivateKeyViewModel(wallet, get()) }
    viewModel { (wallet: String) -> ImportWalletMnemonicViewModel(wallet, get()) }
    viewModel { (wallet: String, mnemonicCode: List<String>) ->
        ImportWalletDerivationPathViewModel(
            wallet,
            mnemonicCode,
            get()
        )
    }
    viewModel { WalletTransactionHistoryViewModel(get(), get()) }
    viewModel { (id: String) -> WalletRenameViewModel(id, get()) }
    viewModel { WalletBalancesViewModel(get(), get()) }
    viewModel { WalletManagementModalViewModel(get()) }
    viewModel { WalletBackupViewModel(get(), get()) }
    viewModel { (id: String) -> WalletDeleteViewModel(id, get(), get()) }
    viewModel { WalletSwitchViewModel(get()) }
    viewModel { SearchAddressViewModel(get(), get(), get()) }
    viewModel { (id: String) -> TokenDetailViewModel(id, get(), get(), get()) }
    viewModel { (initialGasLimit: Double) -> GasFeeViewModel(initialGasLimit = initialGasLimit, get(), get()) }
    viewModel { (toAddress: String) ->
        SendTokenViewModel(
            toAddress = toAddress,
            get(),
            get(),
        )
    }
    viewModel { AddContactViewModel(get()) }
    viewModel { (toAddress: String) ->
        SendConfirmViewModel(toAddress, get(), get())
    }
    viewModel { BiometricViewModel(get(), get()) }
    viewModel { WalletConnectManagementViewModel(get(), get()) }
    viewModel { (onResult: (success: Boolean, needToSwitchNetwork: Boolean) -> Unit) -> WalletConnectViewModel(get(), get(), get(), onResult) }
    viewModel { UnlockWalletViewModel(get(), get()) }
    viewModel { BackUpPasswordViewModel(get(), get()) }
    viewModel { (id: String) -> CollectibleDetailViewModel(id, get()) }
    viewModel { (tokenAddress: String) -> SendTokenDataViewModel(tokenAddress, get(), get()) }
}

val servicesModule = module {
    single { WalletServices() }
}
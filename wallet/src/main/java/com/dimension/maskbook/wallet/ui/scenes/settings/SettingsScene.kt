package com.dimension.maskbook.wallet.ui.scenes.settings

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.ext.encodeUrl
import com.dimension.maskbook.wallet.ext.observeAsState
import com.dimension.maskbook.wallet.repository.Appearance
import com.dimension.maskbook.wallet.repository.DataProvider
import com.dimension.maskbook.wallet.repository.IPersonaRepository
import com.dimension.maskbook.wallet.repository.ISettingsRepository
import com.dimension.maskbook.wallet.repository.Language
import com.dimension.maskbook.wallet.ui.LocalRootNavController
import com.dimension.maskbook.wallet.ui.widget.IosSwitch
import com.dimension.maskbook.wallet.ui.widget.MaskCard
import com.dimension.maskbook.wallet.ui.widget.MaskScaffold
import com.dimension.maskbook.wallet.ui.widget.MaskTopAppBar
import com.dimension.maskbook.wallet.viewmodel.wallets.BiometricEnableViewModel
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScene(
    onBack: () -> Unit,
) {
    val repository = get<ISettingsRepository>()
    val language by repository.language.observeAsState(initial = Language.auto)
    val appearance by repository.appearance.observeAsState(initial = Appearance.default)
    val dataProvider by repository.dataProvider.observeAsState(initial = DataProvider.UNISWAP_INFO)
    val backupPassword by repository.backupPassword.observeAsState(initial = "")
    val paymentPassword by repository.paymentPassword.observeAsState(initial = "")
    val biometricEnabled by repository.biometricEnabled.observeAsState(initial = false)
    val personaRepository = get<IPersonaRepository>()
    val persona by personaRepository.currentPersona.observeAsState(initial = null)
    val biometricEnableViewModel = getViewModel<BiometricEnableViewModel>()
    val context = LocalContext.current
    MaskScaffold(
        topBar = {
            MaskTopAppBar(
                actions = {
                    MaskCard(
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        IconButton(onClick = { onBack.invoke() }) {
                            Image(
                                painter = painterResource(id = R.drawable.twitter_1),
                                contentDescription = null,
                            )
                        }
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsTitle(
                title = stringResource(R.string.scene_setting_general_title)
            )
            SettingsCard {
                SettingsItem(
                    targetRoute = "LanguageSettings",
                    title = stringResource(R.string.scene_setting_general_language),
                    icon = R.drawable.ic_settings_language,
                    trailingText = languageMap[language],
                )
                SettingsDivider()
                SettingsItem(
                    targetRoute = "AppearanceSettings",
                    title = stringResource(R.string.scene_setting_general_appearance),
                    icon = R.drawable.ic_settings_appearance,
                    trailingText = appearanceMap[appearance]?.let { it1 -> stringResource(it1) },
                )
                SettingsDivider()
                SettingsItem(
                    targetRoute = "DataSourceSettings",
                    title = "DataSource",
                    icon = R.drawable.ic_settings_datasource,
                    trailingText = dataProviderMap[dataProvider],
                )
                SettingsDivider()
                if (paymentPassword.isEmpty()) {
                    SettingsItem(
                        targetRoute = "PaymentPasswordSettings",
                        title = stringResource(R.string.scene_setting_general_setup_payment_password),
                        icon = R.drawable.ic_settings_change_payment_password,
                    )
                } else {
                    SettingsItem(
                        targetRoute = "PaymentPasswordSettings",
                        title = stringResource(R.string.scene_setting_general_change_payment_password),
                        icon = R.drawable.ic_settings_change_payment_password,
                    )
                }
                if (biometricEnableViewModel.isSupported(context)) {
                    SettingsDivider()
                    SettingsItem(
                        title = stringResource(R.string.scene_setting_general_unlock_wallet_with_face_id),
                        icon = R.drawable.ic_settings_face_id,
                        trailing = {
                            IosSwitch(checked = biometricEnabled, onCheckedChange = {
                                enableBiometric(
                                    !biometricEnabled,
                                    context,
                                    biometricEnableViewModel,
                                    repository
                                )
                            })
                        },
                        onClicked = {
                            enableBiometric(
                                !biometricEnabled,
                                context,
                                biometricEnableViewModel,
                                repository
                            )
                        },
                    )
                }
            }
            Spacer(Modifier.height(SettingsSceneDefault.spacerHeight))
            SettingsTitle(
                title = stringResource(R.string.scene_setting_backup_recovery_title)
            )
            SettingsCard {
                SettingsItem(
                    title = stringResource(R.string.scene_setting_backup_recovery_restore_data),
                    icon = R.drawable.ic_settings_restore_data,
                    targetRoute = stringResource(R.string.scene_personas_action_recovery)
                )
                SettingsDivider()
                SettingsItem(
                    title = stringResource(R.string.scene_setting_backup_recovery_back_up_data),
                    icon = R.drawable.ic_settings_backup_data,
                    targetRoute = if (backupPassword.isEmpty() || paymentPassword.isEmpty()) "SetupPasswordDialog" else "BackupData"
                )
                SettingsDivider()
                if (backupPassword.isEmpty()) {
                    SettingsItem(
                        title = stringResource(R.string.scene_setting_backup_recovery_back_up_password),
                        icon = R.drawable.ic_settings_backup_password,
                        targetRoute = "ChangeBackUpPassword",
                        secondaryText = stringResource(R.string.scene_setting_backup_recovery_back_up_password_empty)
                    )
                } else {
                    SettingsItem(
                        title = stringResource(R.string.scene_setting_backup_recovery_change_backup_password),
                        icon = R.drawable.ic_settings_backup_password,
                        targetRoute = "ChangeBackUpPassword"
                    )
                }
                SettingsDivider()
                val email = persona?.email
                if (email == null) {
                    SettingsItem(
                        title = stringResource(R.string.scene_backup_backup_verify_field_email),
                        icon = R.drawable.ic_settings_email,
                        secondaryText = stringResource(R.string.scene_setting_profile_email_empty),
                        targetRoute = "Settings_ChangeEmail_Setup"
                    )
                } else {
                    SettingsItem(
                        title = stringResource(R.string.scene_backup_backup_verify_field_email),
                        icon = R.drawable.ic_settings_email,
                        secondaryText = email,
                        targetRoute = "Settings_ChangeEmail_Change_Code/${email.encodeUrl()}"
                    )
                }
                SettingsDivider()
                val phone = persona?.phone
                if (phone == null) {
                    SettingsItem(
                        title = stringResource(R.string.scene_setting_profile_phone_number),
                        icon = R.drawable.ic_settings_phone_number,
                        secondaryText = stringResource(R.string.scene_setting_profile_phone_number_empty),
                        targetRoute = "Settings_ChangePhone_Setup"
                    )
                } else {
                    SettingsItem(
                        title = stringResource(R.string.scene_setting_profile_phone_number),
                        icon = R.drawable.ic_settings_phone_number,
                        secondaryText = phone,
                        targetRoute = "Settings_ChangePhone_Change_Code/${phone.encodeUrl()}"
                    )
                }
            }
            Spacer(Modifier.height(SettingsSceneDefault.spacerHeight))
        }
    }
}

private fun enableBiometric(
    enable: Boolean,
    context: Context,
    viewModel: BiometricEnableViewModel,
    repository: ISettingsRepository
) {
    if (enable) {
        viewModel.enable(
            context = context,
            title = R.string.scene_setting_general_unlock_wallet_with_face_id,
            negativeButton = R.string.common_controls_cancel,
        )
    } else {
        repository.setBiometricEnabled(enable)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsItem(
    targetRoute: String? = null,
    title: String,
    @DrawableRes icon: Int,
    secondaryText: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    trailingText: String? = null,
    onClicked: (() -> Unit)? = null,
) {
    val rootNavController = LocalRootNavController.current
    ListItem(
        modifier = Modifier
            .clickable {
                if (targetRoute != null) {
                    rootNavController.navigate(targetRoute)
                } else onClicked?.invoke()
            },
        text = {
            Text(text = title)
        },
        icon = {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
            )
        },
        trailing = {
            if (trailing != null) {
                trailing.invoke()
            } else {
                Row(
                    modifier = Modifier.alpha(ContentAlpha.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (trailingText != null) {
                        Text(text = trailingText)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        },
        secondaryText = secondaryText?.let {
            {
                Text(text = it)
            }
        }
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = SettingsSceneDefault.contentHorizontalPadding),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.h6,
        modifier = Modifier.padding(
            vertical = SettingsSceneDefault.titleVerticalPadding,
            horizontal = SettingsSceneDefault.contentHorizontalPadding,
        )
    )
}

@Composable
private fun SettingsDivider() {
    Divider(
        color = MaterialTheme.colors.background
    )
}

private object SettingsSceneDefault {
    val contentHorizontalPadding = 22.5f.dp
    val titleVerticalPadding = 16.dp
    val spacerHeight = 16.dp
}

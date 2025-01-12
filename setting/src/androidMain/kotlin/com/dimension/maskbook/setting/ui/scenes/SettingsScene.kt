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
package com.dimension.maskbook.setting.ui.scenes

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.common.ext.observeAsState
import com.dimension.maskbook.common.route.Deeplinks
import com.dimension.maskbook.common.ui.LocalRootNavController
import com.dimension.maskbook.common.ui.widget.IosSwitch
import com.dimension.maskbook.common.ui.widget.MaskCard
import com.dimension.maskbook.common.ui.widget.MaskListItem
import com.dimension.maskbook.common.ui.widget.MaskScaffold
import com.dimension.maskbook.common.ui.widget.MaskTopAppBar
import com.dimension.maskbook.common.ui.widget.button.MaskButton
import com.dimension.maskbook.common.ui.widget.button.MaskIconCardButton
import com.dimension.maskbook.common.viewmodel.BiometricEnableViewModel
import com.dimension.maskbook.localization.R
import com.dimension.maskbook.persona.export.PersonaServices
import com.dimension.maskbook.setting.export.model.Appearance
import com.dimension.maskbook.setting.export.model.DataProvider
import com.dimension.maskbook.setting.export.model.Language
import com.dimension.maskbook.setting.repository.ISettingsRepository
import com.dimension.maskbook.setting.route.SettingRoute
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
    val personaRepository = get<PersonaServices>()
    val persona by personaRepository.currentPersona.observeAsState(initial = null)
    val biometricEnableViewModel = getViewModel<BiometricEnableViewModel>()
    val context = LocalContext.current
    MaskScaffold(
        topBar = {
            MaskTopAppBar(
                actions = {
                    MaskIconCardButton(onClick = onBack) {
                        Image(
                            painter = painterResource(id = R.drawable.twitter_1),
                            contentDescription = null,
                        )
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
                    targetRoute = SettingRoute.LanguageSettings,
                    title = stringResource(R.string.scene_setting_general_language),
                    icon = R.drawable.ic_settings_language,
                    trailingText = languageMap[language],
                )
                SettingsDivider()
                SettingsItem(
                    targetRoute = SettingRoute.AppearanceSettings,
                    title = stringResource(R.string.scene_setting_general_appearance),
                    icon = R.drawable.ic_settings_appearance,
                    trailingText = appearanceMap[appearance]?.let { it1 -> stringResource(it1) },
                )
                SettingsDivider()
                SettingsItem(
                    targetRoute = SettingRoute.DataSourceSettings,
                    title = stringResource(R.string.scene_setting_general_data_source),
                    icon = R.drawable.ic_settings_datasource,
                    trailingText = dataProviderMap[dataProvider],
                )
                SettingsDivider()
                if (paymentPassword.isEmpty()) {
                    SettingsItem(
                        targetRoute = SettingRoute.PaymentPasswordSettings,
                        title = stringResource(R.string.scene_setting_general_setup_payment_password),
                        icon = R.drawable.ic_settings_change_payment_password,
                    )
                } else {
                    SettingsItem(
                        targetRoute = SettingRoute.PaymentPasswordSettings,
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
                val rootNavController = LocalRootNavController.current
                SettingsItem(
                    title = stringResource(R.string.scene_setting_backup_recovery_restore_data),
                    icon = R.drawable.ic_settings_restore_data,
                    onClicked = {
                        rootNavController.navigate(Uri.parse(Deeplinks.Wallet.Recovery))
                    }
                )
                SettingsDivider()
                SettingsItem(
                    title = stringResource(R.string.scene_setting_backup_recovery_back_up_data),
                    icon = R.drawable.ic_settings_backup_data,
                    targetRoute = if (backupPassword.isEmpty() || paymentPassword.isEmpty()) SettingRoute.SetupPasswordDialog else SettingRoute.BackupData.BackupSelection
                )
                SettingsDivider()
                if (backupPassword.isEmpty()) {
                    SettingsItem(
                        title = stringResource(R.string.scene_setting_backup_recovery_back_up_password),
                        icon = R.drawable.ic_settings_backup_password,
                        targetRoute = SettingRoute.ChangeBackUpPassword,
                        secondaryText = stringResource(R.string.scene_setting_backup_recovery_back_up_password_empty)
                    )
                } else {
                    SettingsItem(
                        title = stringResource(R.string.scene_setting_backup_recovery_change_backup_password),
                        icon = R.drawable.ic_settings_backup_password,
                        targetRoute = SettingRoute.ChangeBackUpPassword,
                    )
                }
                SettingsDivider()
                val email = persona?.email
                if (email == null) {
                    SettingsItem(
                        title = stringResource(R.string.scene_backup_backup_verify_field_email),
                        icon = R.drawable.ic_settings_email,
                        secondaryText = stringResource(R.string.scene_setting_profile_email_empty),
                        targetRoute = SettingRoute.Settings_ChangeEmail.Settings_ChangeEmail_Setup
                    )
                } else {
                    SettingsItem(
                        title = stringResource(R.string.scene_backup_backup_verify_field_email),
                        icon = R.drawable.ic_settings_email,
                        secondaryText = email,
                        targetRoute = SettingRoute.Settings_ChangeEmail.Settings_ChangeEmail_Change_Code(email)
                    )
                }
                SettingsDivider()
                val phone = persona?.phone
                if (phone == null) {
                    SettingsItem(
                        title = stringResource(R.string.scene_setting_profile_phone_number),
                        icon = R.drawable.ic_settings_phone_number,
                        secondaryText = stringResource(R.string.scene_setting_profile_phone_number_empty),
                        targetRoute = SettingRoute.Settings_ChangePhone.Settings_ChangePhone_Setup,
                    )
                } else {
                    SettingsItem(
                        title = stringResource(R.string.scene_setting_profile_phone_number),
                        icon = R.drawable.ic_settings_phone_number,
                        secondaryText = phone,
                        targetRoute = SettingRoute.Settings_ChangePhone.Settings_ChangePhone_Change_Code(phone)
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
    MaskButton(
        onClick = {
            if (targetRoute != null) {
                rootNavController.navigate(targetRoute)
            } else onClicked?.invoke()
        }
    ) {
        MaskListItem(
            text = {
                Text(text = title)
            },
            icon = {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
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
                            Text(
                                text = trailingText,
                                style = MaterialTheme.typography.body1,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            },
            secondaryText = secondaryText?.let { text ->
                {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.body1,
                    )
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    MaskCard(
        modifier = Modifier
            .padding(horizontal = SettingsSceneDefault.contentHorizontalPadding),
        shape = MaterialTheme.shapes.medium,
        elevation = 0.dp,
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.h3,
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

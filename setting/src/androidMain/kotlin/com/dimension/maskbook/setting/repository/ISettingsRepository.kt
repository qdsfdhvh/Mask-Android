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
package com.dimension.maskbook.setting.repository

import com.dimension.maskbook.setting.export.model.Appearance
import com.dimension.maskbook.setting.export.model.BackupMeta
import com.dimension.maskbook.setting.export.model.DataProvider
import com.dimension.maskbook.setting.export.model.Language
import com.dimension.maskbook.setting.export.model.NetworkType
import com.dimension.maskbook.setting.export.model.TradeProvider
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    val biometricEnabled: Flow<Boolean>
    val language: Flow<Language>
    val appearance: Flow<Appearance>
    val dataProvider: Flow<DataProvider>
    val paymentPassword: Flow<String>
    val backupPassword: Flow<String>
    val tradeProvider: Flow<Map<NetworkType, TradeProvider>>
    val shouldShowLegalScene: Flow<Boolean>
    fun setBiometricEnabled(value: Boolean)
    fun setTradeProvider(networkType: NetworkType, tradeProvider: TradeProvider)
    fun setLanguage(language: Language)
    fun setAppearance(appearance: Appearance)
    fun setDataProvider(dataProvider: DataProvider)
    fun setPaymentPassword(value: String)
    fun setBackupPassword(value: String)
    suspend fun provideBackupMeta(): BackupMeta?
    suspend fun provideBackupMetaFromJson(value: String): BackupMeta?
    suspend fun restoreBackupFromJson(value: String)
    suspend fun createBackupJson(
        noPosts: Boolean = false,
        noWallets: Boolean = false,
        noPersonas: Boolean = false,
        noProfiles: Boolean = false,
        hasPrivateKeyOnly: Boolean = false,
    ): String
    fun setShouldShowLegalScene(value: Boolean)
    fun saveEmailForCurrentPersona(value: String)
    fun savePhoneForCurrentPersona(value: String)
}

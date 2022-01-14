package com.dimension.maskbook.wallet.viewmodel.wallets.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimension.maskbook.wallet.ext.asStateIn
import com.dimension.maskbook.wallet.repository.ISettingsRepository
import com.dimension.maskbook.wallet.repository.IWalletRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WalletDeleteViewModel(
    private val id: String,
    private val settingsRepository: ISettingsRepository,
    private val walletRepository: IWalletRepository,
): ViewModel() {
    val wallet by lazy {
        walletRepository.wallets.map {
            it.firstOrNull { it.id == id }
        }.asStateIn(viewModelScope, null).mapNotNull { it }
    }
    private val _password = MutableStateFlow("")
    val password = _password.asStateIn(viewModelScope, "")
    fun setPassword(value: String) {
        _password.value = value
    }
    val canConfirm by lazy {
        combine(_password, settingsRepository.paymentPassword) { input, actual ->
            input == actual
        }
    }

    fun confirm() {
        viewModelScope.launch {
            wallet.firstOrNull()?.let {
                walletRepository.deleteWallet(it)
            }
        }
    }
}
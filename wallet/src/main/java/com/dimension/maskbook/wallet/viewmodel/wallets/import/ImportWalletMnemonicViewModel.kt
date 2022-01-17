package com.dimension.maskbook.wallet.viewmodel.wallets.import

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimension.maskbook.wallet.ext.asStateIn
import com.dimension.maskbook.wallet.repository.IWalletRepository
import com.dimension.maskbook.wallet.repository.WalletCreateOrImportResult
import com.dimension.maskbook.wallet.repository.WalletData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

class ImportWalletMnemonicViewModel(
    private val wallet: String,
    private val repository: IWalletRepository,
) : ViewModel() {
    private val _words = MutableStateFlow("")
    val words = _words.asStateIn(viewModelScope, "")
    val hintWords by lazy {
        _words.map {
            generateHintWords(it)
        }
    }

    private fun generateHintWords(inputWords: String): List<String> {
        return if (inputWords.isEmpty()) emptyList() else inputWords.split(" ")
    }

    fun setWords(words: String) {
        _words.value = words
    }

    val canConfirm by lazy {
        _words.map { it.isNotEmpty() && repository.validateMnemonic(it) }
    }

    fun confirm(onResult: (WalletCreateOrImportResult) -> Unit) {
        /*TODO Logic:import wallet with keystore, post result*/
        onResult(
            WalletCreateOrImportResult(
                type = WalletCreateOrImportResult.Type.SUCCESS,
                wallet = WalletData(
                    id = UUID.randomUUID().toString(),
                    name = wallet,
                    address = "",
                    fromWalletConnect = false,
                    imported = false,
                    tokens = emptyList(),
                    balance = emptyMap(),
                    collectibles = emptyList()
                )
            )
        )
    }

    fun applyHintWords(hint: String) {
        viewModelScope.launch {
            _words.value = _words.first().split(" ").toMutableList().apply {
                removeAt(lastIndex)
                add(hint)
            }.joinToString(separator = " ")
        }
    }
}
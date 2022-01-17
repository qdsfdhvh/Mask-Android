package com.dimension.maskbook.wallet.ui.scenes.wallets.intro.password

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.wallet.ext.observeAsState
import com.dimension.maskbook.wallet.ui.widget.MaskModal
import com.dimension.maskbook.wallet.ui.widget.MaskPasswordInputField
import com.dimension.maskbook.wallet.ui.widget.PrimaryButton
import com.dimension.maskbook.wallet.ui.widget.ScaffoldPadding
import com.dimension.maskbook.wallet.viewmodel.wallets.SetUpPaymentPasswordViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun SetUpPaymentPassword(
    onNext: () -> Unit,
) {
    val viewModel: SetUpPaymentPasswordViewModel = getViewModel()
    val newPassword by viewModel.newPassword.observeAsState(initial = "")
    val newPasswordConfirm by viewModel.newPasswordConfirm.observeAsState(initial = "")
    val canConfirm by viewModel.canConfirm.observeAsState(initial = false)
    MaskModal {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(ScaffoldPadding),
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.dimension.maskbook.wallet.R.string.scene_set_password_title),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = androidx.compose.ui.res.stringResource(com.dimension.maskbook.wallet.R.string.scene_setting_general_setup_payment_password))
            Spacer(modifier = Modifier.height(8.dp))
            MaskPasswordInputField(
                value = newPassword,
                onValueChange = {
                    viewModel.setNewPassword(it)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = androidx.compose.ui.res.stringResource(com.dimension.maskbook.wallet.R.string.scene_set_password_repeat_payment_password))
            Spacer(modifier = Modifier.height(8.dp))
            MaskPasswordInputField(
                value = newPasswordConfirm,
                onValueChange = {
                    viewModel.setNewPasswordConfirm(it)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(com.dimension.maskbook.wallet.R.string.scene_change_password_password_demand),
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.confirm()
                    onNext.invoke()
                },
                enabled = canConfirm
            ) {
                Text(text = androidx.compose.ui.res.stringResource(com.dimension.maskbook.wallet.R.string.common_controls_next))
            }
        }
    }
}
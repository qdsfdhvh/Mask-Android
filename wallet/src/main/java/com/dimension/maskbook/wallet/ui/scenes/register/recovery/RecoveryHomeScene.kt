package com.dimension.maskbook.wallet.ui.scenes.register.recovery

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.ui.MaskTheme
import com.dimension.maskbook.wallet.ui.widget.MaskBackButton
import com.dimension.maskbook.wallet.ui.widget.MaskScaffold
import com.dimension.maskbook.wallet.ui.widget.MaskTopAppBar
import com.dimension.maskbook.wallet.ui.widget.PrimaryButton
import com.dimension.maskbook.wallet.ui.widget.ScaffoldPadding

@Composable
fun RecoveryHomeScene(
    onBack: () -> Unit,
    onIdentity: () -> Unit,
    onPrivateKey: () -> Unit,
    onLocalBackup: () -> Unit,
    onRemoteBackup: () -> Unit,
) {
    MaskTheme {
        MaskScaffold(
            topBar = {
                MaskTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.scene_identity_empty_recovery_sign_in))
                    },
                    navigationIcon = {
                        MaskBackButton(
                            onBack = onBack,
                        )
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ScaffoldPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painterResource(id = R.drawable.ic_rectangle_361),
                    modifier = Modifier.weight(1f),
                    contentDescription = null,
                )
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onIdentity,
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_profile2),
                        contentDescription = null,
                        tint = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.scene_identity_mnemonic_import_title))
                }
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPrivateKey,
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_key),
                        contentDescription = null,
                        tint = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.scene_identity_privatekey_import_title))
                }
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onLocalBackup,
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_iphone),
                        contentDescription = null,
                        tint = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.scene_identity_recovery_local_backup_recovery_button))
                }
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRemoteBackup,
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_icloud),
                        contentDescription = null,
                        tint = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.scene_identity_recovery_cloud_backup_recovery_button_title))
                }
            }
        }
    }
}
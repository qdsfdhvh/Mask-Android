package com.dimension.maskbook.wallet.ui.scenes.settings.backup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.ui.widget.MaskModal
import com.dimension.maskbook.wallet.ui.widget.PrimaryButton
import com.dimension.maskbook.wallet.ui.widget.ScaffoldPadding

@Composable
fun BackupSelectionModal(
    onLocal: () -> Unit,
    onRemote: () -> Unit,
) {
    MaskModal {
        Column(
            modifier = Modifier.padding(ScaffoldPadding)
        ) {
            Text(text = stringResource(R.string.scene_setting_backup_data_title), style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(21.dp))
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onRemote.invoke()
                },
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_icloud),
                    contentDescription = null,
                    tint = LocalContentColor.current
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.common_controls_back_up_to_cloud))
            }
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onLocal.invoke()
                },
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_iphone),
                    contentDescription = null,
                    tint = LocalContentColor.current
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.common_controls_back_up_locally))
            }
        }
    }
}
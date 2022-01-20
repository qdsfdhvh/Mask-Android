package com.dimension.maskbook.wallet.ui.scenes.register

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.ui.MaskTheme
import com.dimension.maskbook.wallet.ui.widget.MaskBackButton
import com.dimension.maskbook.wallet.ui.widget.MaskDialog
import com.dimension.maskbook.wallet.ui.widget.MaskScaffold
import com.dimension.maskbook.wallet.ui.widget.MaskTopAppBar
import com.dimension.maskbook.wallet.ui.widget.PrimaryButton
import com.dimension.maskbook.wallet.ui.widget.ScaffoldPadding
import com.dimension.maskbook.wallet.ui.widget.itemsGridIndexed

@Composable
fun BackupIdentityScene(
    words: List<String>,
    onRefreshWords: () -> Unit,
    onVerify: () -> Unit,
    onBack: () -> Unit,
) {
    var showDialog by rememberSaveable {
        mutableStateOf(true)
    }
    if (showDialog) {
        MaskDialog(
            onDismissRequest = {
                showDialog = false
            },
            buttons = {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDialog = false },
                ) {
                    Text(text = stringResource(R.string.common_controls_ok))
                }
            },
            title = {
                Text(text = stringResource(R.string.common_alert_identity_phrase_title))
            },
            text = {
                Text(text = stringResource(R.string.common_alert_identity_phrase_description))
            }
        )
    }
    BackupContent(
        words,
        onRefreshWords,
        onBack,
        onVerify,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BackupContent(
    words: List<String>,
    onRefreshWords: () -> Unit,
    onBack: () -> Unit,
    onVerify: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onRefreshWords.invoke()
    }
    MaskTheme {
        MaskScaffold(
            topBar = {
                MaskTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.scene_identify_verify_title))
                    },
                    navigationIcon = {
                        MaskBackButton {
                            onBack.invoke()
                        }
                    },
                    subTitle = {
                        Row {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.scene_identity_create_description),
                            )
                            Icon(
                                Icons.Default.Refresh,
                                modifier = Modifier.clickable {
                                    onRefreshWords.invoke()
                                },
                                contentDescription = null,
                                tint = Color(0XFF1C68F3)
                            )
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ScaffoldPadding),
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                ) {
                    itemsGridIndexed(words, rowSize = 3, spacing = 8.dp) { index, it ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 0.dp,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                modifier = Modifier.padding(12.dp),
                                text = "$index $it",
                            )
                        }
                    }
                }
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onVerify.invoke() },
                ) {
                    Text(text = stringResource(R.string.common_controls_verify))
                }
            }
        }
    }
}
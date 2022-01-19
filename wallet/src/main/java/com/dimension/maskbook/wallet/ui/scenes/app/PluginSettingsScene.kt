package com.dimension.maskbook.wallet.ui.scenes.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimension.maskbook.wallet.ui.widget.IosSwitch
import com.dimension.maskbook.wallet.ui.widget.MaskBackButton
import com.dimension.maskbook.wallet.ui.widget.MaskButton
import com.dimension.maskbook.wallet.ui.widget.MaskListItem
import com.dimension.maskbook.wallet.ui.widget.MaskScaffold
import com.dimension.maskbook.wallet.ui.widget.MaskSingleLineTopAppBar
import com.dimension.maskbook.wallet.ui.widget.ScaffoldPadding
import com.dimension.maskbook.wallet.viewmodel.app.PluginDisplayData
import com.dimension.maskbook.wallet.viewmodel.app.PluginSettingsViewModel
import org.koin.androidx.compose.viewModel

@Composable
fun PluginSettingsScene(
    onBack: () -> Unit
) {
    val viewModel by viewModel<PluginSettingsViewModel>()
    val apps by viewModel.apps.collectAsState()
    var isShowTipDialog by remember { mutableStateOf(true) }
    MaskScaffold(
        topBar = {
            MaskSingleLineTopAppBar(
                title = {
                    Text(text = PluginSettingsItemDefault.title)
                },
                navigationIcon = {
                    MaskBackButton(onBack = onBack)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.padding(ScaffoldPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(apps) { item ->
                    PluginSettingsItem(
                        item = item,
                        onClick = {
                            viewModel.setEnabled(item.key, !item.enabled)
                        },
                    )
                }
            }
            if (isShowTipDialog) {
                TipDialog(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onClose = {
                        isShowTipDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PluginSettingsItem(
    item: PluginDisplayData,
    onClick: () -> Unit,
) {
    MaskButton(onClick = onClick) {
        MaskListItem(
            modifier = Modifier.height(56.dp),
            icon = {
                Image(
                    painter = painterResource(id = item.onIcon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            },
            text = {
                Text(
                    text = stringResource(item.name),
                )
            },
            trailing = {
                IosSwitch(
                    checked = item.enabled,
                    onCheckedChange = null,
                )
            }
        )
    }
}

@Composable
private fun TipDialog(
    modifier: Modifier,
    onClose: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(horizontal = 22.dp, vertical = 44.dp)
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .background(
                brush = PluginSettingsItemDefault.tipDialogBackGround,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(start = 16.dp, end = 0.dp, top = 10.dp, bottom = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = PluginSettingsItemDefault.tipMessage,
            color = Color.White,
            lineHeight = 21.sp,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}

private object PluginSettingsItemDefault {
    const val title = "Plugin Settings"
    const val tipMessage =
        "If you turn off a plugin, the plugin function can no longer be rendered on timeline when browsing social media."
    val tipDialogBackGround = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1C68F3),
            Color(0xFF499DFF)
        ),
    )
}

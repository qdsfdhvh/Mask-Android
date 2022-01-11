package com.dimension.maskbook.wallet.ui.scenes.wallets.management

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.ext.observeAsState
import com.dimension.maskbook.wallet.ui.scenes.persona.social.tabIndicatorOffset3
import com.dimension.maskbook.wallet.ui.widget.MaskModal
import com.dimension.maskbook.wallet.ui.widget.PrimaryButton
import com.dimension.maskbook.wallet.ui.widget.ScaffoldPadding
import com.dimension.maskbook.wallet.viewmodel.wallets.WalletConnectViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.koin.androidx.compose.getViewModel

enum class WalletConnectType {
    Manually,
    QRCode,
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WalletConnectModal() {
    val navController = rememberAnimatedNavController()
    val viewModel = getViewModel<WalletConnectViewModel>()
    val qrCode by viewModel.qrCode.observeAsState(initial = "")
    MaskModal {
        Column(
            modifier = Modifier
                .padding(ScaffoldPadding),
        ) {
            Text(
                text = "WalletConnect",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            AnimatedNavHost(
                navController = navController,
                startDestination = "WalletConnectTypeSelect"
            ) {
                composable("WalletConnectTypeSelect") {
                    TypeSelectScene(qrCode = qrCode)
                }

                composable("WalletConnectConnecting") {
                    Connecting()
                }

                composable("WalletConnectFailed") {
                    WalletConnectFailure(
                        onRetry = {

                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WalletConnectFailure(
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFFF5F5F)),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(id = R.drawable.ic_close_square), contentDescription = null)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Connection failed.", color = Color(0xFFFF5F5F))
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(onClick = onRetry) {
            Text(text = "Try Again")
        }
    }
}

@Composable
fun Connecting() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painterResource(id = R.drawable.ic_mask1), contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(26.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Image(painterResource(id = R.drawable.mask1), contentDescription = null)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Connecting...")
    }
}


@Composable
private fun TypeSelectScene(qrCode: String) {
    Column {
        var selectedTabIndex by remember {
            mutableStateOf(0)
        }
        TabRow(
            selectedTabIndex = selectedTabIndex,
            backgroundColor = MaterialTheme.colors.background,
            divider = {
                TabRowDefaults.Divider(thickness = 0.dp)
            },
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .height(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(0.1f)
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colors.primary,
                                shape = RoundedCornerShape(99.dp)
                            )
                    )
                }
            },
        ) {
            WalletConnectType.values().forEachIndexed { index, type ->
                Tab(
                    text = { Text(type.name) },
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                    },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = MaterialTheme.colors.onBackground.copy(
                        alpha = ContentAlpha.medium
                    ),
                )
            }
        }
        when (WalletConnectType.values()[selectedTabIndex]) {
            WalletConnectType.Manually -> WalletConnectManually()
            WalletConnectType.QRCode -> WalletConnectQRCode(qrCode = qrCode)
        }
    }
}

@Composable
fun WalletConnectQRCode(qrCode: String) {
    Text(text = "Use a WalletConnect compatiable wallet to scan the QR Code")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        Icon(
            Icons.Default.Home,
            contentDescription = null,
        )// TODO: Display qr code
        Text(text = "qrCode:$qrCode")
    }
    Text(text = "Tap to copy to clipboard")// TODO: Copy
}

@Composable
fun WalletConnectManually() {
    var selectedTabIndex by remember {
        mutableStateOf(0)
    }
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        backgroundColor = MaterialTheme.colors.background,
        indicator = { tabPositions ->
            Box(
                Modifier
                    .tabIndicatorOffset3(tabPositions[selectedTabIndex])
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        },
        edgePadding = 14.dp,
        divider = { },
        modifier = Modifier.padding(vertical = 20.dp)
    ) {
        supportedChainType.forEachIndexed { index, type ->
            val selected = selectedTabIndex == index
            Tab(
                text = { Text(type.name) },
                selected = selected,
                onClick = {
                    selectedTabIndex = index
                },
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.onBackground.copy(
                    alpha = ContentAlpha.medium
                ),
            )
        }

    }
}

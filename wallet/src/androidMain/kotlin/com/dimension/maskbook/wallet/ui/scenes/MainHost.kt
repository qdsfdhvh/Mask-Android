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
package com.dimension.maskbook.wallet.ui.scenes

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.repository.AppKey
import com.dimension.maskbook.wallet.repository.Network
import com.dimension.maskbook.wallet.repository.PersonaData
import com.dimension.maskbook.wallet.repository.SocialData
import com.dimension.maskbook.wallet.ui.MaskTheme
import com.dimension.maskbook.wallet.ui.scenes.app.LabsScene
import com.dimension.maskbook.wallet.ui.scenes.persona.PersonaScene
import com.dimension.maskbook.wallet.ui.scenes.settings.SettingsScene
import com.dimension.maskbook.wallet.ui.scenes.wallets.intro.WalletIntroHost
import com.dimension.maskbook.wallet.ui.widget.MaskScaffold
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

private enum class HomeScreen(val route: String, @StringRes val title: Int, @DrawableRes val icon: Int) {
    Personas("Personas", R.string.tab_personas, R.drawable.ic_persona),
    Wallets("Wallets", R.string.tab_wallet, R.drawable.ic_wallet),
    Labs("Labs", R.string.tab_labs, R.drawable.ic_labs),
    Settings("Settings", R.string.tab_setting, R.drawable.ic_settings),
}

private val items = HomeScreen.values()

@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun MainHost(
    initialTab: String,
    onBack: () -> Unit,
    onPersonaCreateClick: () -> Unit,
    onPersonaRecoveryClick: () -> Unit,
    onPersonaNameClick: () -> Unit,
    onAddSocialClick: (PersonaData, Network?) -> Unit,
    onRemoveSocialClick: (PersonaData, SocialData) -> Unit,
    onLabsSettingClick: () -> Unit,
    onLabsItemClick: (AppKey) -> Unit,
) {
    val initialPage = remember(initialTab) {
        if (initialTab.isEmpty()) return@remember 0
        when (HomeScreen.valueOf(initialTab)) {
            HomeScreen.Personas -> 0
            HomeScreen.Wallets -> 1
            HomeScreen.Labs -> 2
            HomeScreen.Settings -> 3
        }
    }
    val pagerState = rememberPagerState(initialPage = initialPage)
    val scope = rememberCoroutineScope()
    MaskTheme {
        MaskScaffold(
            bottomBar = {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface)
                        .height(56.dp)
                ) {
                    items.forEachIndexed { index, screen ->
                        BottomNavigationItem(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(stringResource(screen.title))
                            },
                            icon = {
                                Icon(
                                    painterResource(id = screen.icon),
                                    contentDescription = null
                                )
                            },
                            selectedContentColor = MaterialTheme.colors.primary,
                            unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                        )
                    }
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                contentPadding = innerPadding,
                count = items.size,
                state = pagerState,
            ) {
                when (items[it]) {
                    HomeScreen.Labs -> LabsScene(
                        onSettingClick = onLabsSettingClick,
                        onItemClick = onLabsItemClick,
                    )
                    HomeScreen.Personas -> PersonaScene(
                        onBack = onBack,
                        onPersonaCreateClick = onPersonaCreateClick,
                        onPersonaRecoveryClick = onPersonaRecoveryClick,
                        onPersonaNameClick = onPersonaNameClick,
                        onAddSocialClick = onAddSocialClick,
                        onRemoveSocialClick = onRemoveSocialClick,
                    )
                    HomeScreen.Settings -> SettingsScene(onBack = onBack)
                    HomeScreen.Wallets -> WalletIntroHost(onBack = onBack)
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavigationItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    enabled: Boolean = true,
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium),
    onClick: () -> Unit,
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
) {
    val color = if (selected) {
        selectedContentColor
    } else {
        unselectedContentColor
    }
    CompositionLocalProvider(
        LocalContentColor provides color.copy(alpha = 1f),
        LocalContentAlpha provides color.alpha,
        LocalTextStyle provides MaterialTheme.typography.caption.copy(color = color),
    ) {
        Box(
            modifier
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    enabled = enabled,
                    role = Role.Tab,
                )
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (selected) {
                    text.invoke()
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(color, shape = CircleShape)
                    )
                } else {
                    icon.invoke()
                }
            }
        }
    }
}
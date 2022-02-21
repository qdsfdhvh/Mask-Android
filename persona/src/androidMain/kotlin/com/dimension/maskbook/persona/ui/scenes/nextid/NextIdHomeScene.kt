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
package com.dimension.maskbook.persona.ui.scenes.nextid

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.dimension.maskbook.common.ui.widget.MaskListItem
import com.dimension.maskbook.common.ui.widget.MaskScaffold
import com.dimension.maskbook.common.ui.widget.MaskScene
import com.dimension.maskbook.common.ui.widget.MaskSingleLineTopAppBar
import com.dimension.maskbook.common.ui.widget.ScaffoldPadding
import com.dimension.maskbook.common.ui.widget.button.MaskBackButton
import com.dimension.maskbook.common.ui.widget.button.MaskIconCardButton
import com.dimension.maskbook.persona.R
import com.dimension.maskbook.persona.export.model.ConnectedNextIdProfile
import com.dimension.maskbook.persona.export.model.Platform

@Composable
fun NextIdHomeScene(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    items: List<ConnectedNextIdProfile>,
) {
    MaskScene {
        MaskScaffold(
            topBar = {
                MaskSingleLineTopAppBar(
                    navigationIcon = {
                        MaskBackButton(
                            onBack = onBack,
                        )
                    },
                    title = {
                        Text("Next ID Proof Management")
                    },
                    actions = {
                        MaskIconCardButton(onClick = {
                            onAdd.invoke()
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = ScaffoldPadding,
            ) {
                items(items) {
                    NextIdItem(it)
                }
            }
        }
    }
}

val Platform.icon: Int
    @Composable
    get() = when (this) {
        Platform.Twitter -> R.drawable.ic_twitter
        Platform.Keybase -> R.drawable.keystore
        Platform.Ethereum -> R.drawable.ethereum_o
        Platform.Github -> R.drawable.mask1
    }

@Composable
private fun NextIdItem(it: ConnectedNextIdProfile?) {
    MaskListItem(
        icon = {
            Icon(
                painter = painterResource(it?.platform?.icon ?: 0),
                contentDescription = null,
            )
        },
        text = {
            Text(it?.identity ?: "")
        },
    )
}

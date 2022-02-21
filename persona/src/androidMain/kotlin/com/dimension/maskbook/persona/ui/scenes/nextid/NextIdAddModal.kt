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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dimension.maskbook.common.ui.widget.MaskInputField
import com.dimension.maskbook.common.ui.widget.MaskListItem
import com.dimension.maskbook.common.ui.widget.MaskModal
import com.dimension.maskbook.common.ui.widget.button.PrimaryButton
import com.dimension.maskbook.persona.export.model.Platform

@Composable
fun NextIdAddModal(
    selected: Platform,
    input: String,
    setSelected: (Platform) -> Unit,
    setInput: (String) -> Unit,
    onDone: () -> Unit,
) {
    MaskModal(
        title = {
            Text("Add Proof of Identity")
        }
    ) {
        var expanded by remember { mutableStateOf(false) }
        Column {
            Text("Select a platform to add a proof of identity")
            Spacer(modifier = Modifier.height(8.dp))
            MaskListItem(
                modifier = Modifier.clickable {
                    expanded = !expanded
                }
            ) {
                Text(
                    text = selected.name,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                Platform.values().forEach {
                    DropdownMenuItem(
                        onClick = {
                            setSelected(it)
                            expanded = false
                        }
                    ) {
                        Text(
                            text = it.name,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            MaskInputField(
                value = input,
                onValueChange = {
                    setInput(it)
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onDone.invoke()
                },
            ) {
                Text("Add")
            }
        }
    }
}

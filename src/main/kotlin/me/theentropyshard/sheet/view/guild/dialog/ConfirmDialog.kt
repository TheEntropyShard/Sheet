/*
 * Sheet - https://github.com/TheEntropyShard/Sheet
 * Copyright (C) 2025 TheEntropyShard
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.sheet.view.guild.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConfirmDialog(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    onClick: (Boolean) -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { false },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = text)
        },
        dismissButton = {
            Button(
                onClick = {
                    onClick(false)
                }
            ) {
                Text(text = "No")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onClick(true)
                }
            ) {
                Text(text = "Yes")
            }
        },
    )
}
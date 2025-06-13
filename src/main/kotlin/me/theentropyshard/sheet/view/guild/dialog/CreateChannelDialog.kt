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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun CreateChannelDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onNameSubmitted: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Create a new channel")
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = {
                    Text(text = "Channel name")
                },
                placeholder = {
                    Text(text = "Enter channel name")
                }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismissRequest()
                    onNameSubmitted(text)
                }
            ) {
                Text(text = "Create")
            }
        },
    )
}
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

package me.theentropyshard.sheet.view.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.view.SimpleTextField

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    onSendMessage: (String) -> Unit
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var isShifting by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SimpleTextField(
            modifier = Modifier
                .height(48.dp)
                .weight(1f)
                .onPreviewKeyEvent { event ->
                    when (event.key) {
                        Key.ShiftLeft, Key.ShiftRight -> {
                            isShifting = event.type == KeyEventType.KeyDown

                            false
                        }

                        Key.Enter -> {
                            if (event.type == KeyEventType.KeyDown) {
                                return@onPreviewKeyEvent true
                            }

                            if (!isShifting && event.type == KeyEventType.KeyUp) {
                                val text = textState.text
                                textState = TextFieldValue("")
                                onSendMessage(text)

                                true
                            } else if (isShifting && event.type == KeyEventType.KeyUp) {
                                textState = TextFieldValue(text = buildString {
                                    if (textState.selection.collapsed) {
                                        append(textState.text, 0, textState.selection.start)
                                        append('\n')
                                        append(textState.text, textState.selection.start, textState.text.length)
                                    } else {
                                        append(textState.text, 0, textState.selection.min)
                                        append('\n')
                                        append(textState.text, textState.selection.max, textState.text.length)
                                    }
                                }, selection = TextRange(textState.selection.min + 1))

                                true
                            } else {
                                false
                            }
                        }

                        else -> false
                    }
                },
            value = textState,
            onValueChange = { textState = it },
            placeholder = { Text("Send a message...") },
        )

        IconButton(
            onClick = {
                onSendMessage(textState.text)
                textState = TextFieldValue("")
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Click to send a message"
            )
        }
    }
}
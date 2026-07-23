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

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.generated.resources.attach_24dp
import me.theentropyshard.sheet.generated.resources.send_24dp
import me.theentropyshard.sheet.generated.resources.Res
import me.theentropyshard.sheet.view.Fonts
import org.jetbrains.compose.resources.painterResource

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    onAddAttachmentClick: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    val requester = remember { FocusRequester() }
    val state = rememberTextFieldState()

    val sendMessage: () -> Unit = {
        if (state.text.trim().isNotBlank()) {
            var text: String? = null

            if (state.text.trim().isNotBlank()) text = state.text.toString()

            onSendMessage(text!!)
        }

        state.edit { delete(start = 0, end = length) }
        requester.requestFocus()
    }

    ChatInputBase(
        modifier = modifier,
        state = state,
        focusRequester = requester,
        onSend = sendMessage,
        placeholder = {
            Text(
                text = "Write a message...",
                fontFamily = Fonts.googleSans(),
            )
        },
        leadingIcon = {
            IconButton(onClick = onAddAttachmentClick) {
                Icon(
                    modifier = Modifier.graphicsLayer { rotationZ = 45.0f },
                    painter = painterResource(Res.drawable.attach_24dp),
                    contentDescription = "Add an attachment",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingIcon = {
            IconButton(onClick = sendMessage) {
                Icon(
                    painter = painterResource(Res.drawable.send_24dp),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
private fun ChatInputBase(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    focusRequester: FocusRequester,
    onSend: () -> Unit,
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    placeholder: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.animateContentSize(alignment = Alignment.BottomStart),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon()

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(
                        visible = state.text.isEmpty(),
                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
                    ) {
                        placeholder()
                    }
                }

                BasicTextField(
                    modifier = Modifier
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when (event.key) {
                                    Key.Enter -> {
                                        if (!event.isShiftPressed) {
                                            onSend()
                                        } else {
                                            state.edit { append('\n') }
                                        }

                                        true
                                    }

                                    else -> {
                                        false
                                    }
                                }
                            } else {
                                false
                            }
                        }
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 8),
                    textStyle = TextStyle(fontFamily = Fonts.googleSans()),
                    state = state,
                )
            }

            trailingIcon()
        }
    }
}

/*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.view.Fonts
import me.theentropyshard.sheet.view.SimpleTextField

val minHeight = 48.dp
val maxHeight = (48 * 5).dp

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    onAddAttachmentClick: () -> Unit,
    onSendMessage: (String) -> Unit
) {
}*/

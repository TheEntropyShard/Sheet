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

package me.theentropyshard.sheet.view.guild.invite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.awtClipboard
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateInviteDialog(
    modifier: Modifier = Modifier,
    guildId: String,
    onDismissRequest: () -> Unit
) {
    val model = viewModel { CreateInviteDialogViewModel() }

    val inviteCode by model.inviteCode.collectAsState()

    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        model.createInviteCode(guildId)
    }

    AlertDialog(
        modifier = modifier.size(400.dp, 250.dp),
        onDismissRequest = { false },
        title = {
            Text(text = "Create an invite")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (inviteCode.isEmpty()) {
                    CircularProgressIndicator()
                } else {
                    val text = buildAnnotatedString {
                        append("Code: ")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(inviteCode)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectionContainer {
                            Text(text = text)
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        val clipboard = LocalClipboard.current

                        TextButton(
                            onClick = {
                                clipboard.awtClipboard!!.setContents(StringSelection(inviteCode), null)
                                copied = true
                            }
                        ) {
                            if (copied) {
                                Text(text = "Copied!")
                            } else {
                                Text(text = "Copy")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = "Ok")
            }
        },
    )
}
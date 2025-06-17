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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel

enum class Tab(val index: Int, val title: String, val hint: String, val label: String, val placeholder: String) {
    Join(
        0,
        "Join",
        "Please enter your invite code in the text field below to join the guild",
        "Invite code",
        "code@example.com"
    ),
    Create(
        1,
        "Create",
        "Please enter desired guild name in the text field below",
        "Guild name",
        "My Super Guild"
    )
}

typealias State = JoinOrCreateGuildViewModel.State

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinOrCreateGuildDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    val model = viewModel { JoinOrCreateGuildViewModel() }

    val state by model.state.collectAsState()

    var currentTab by remember { mutableStateOf(Tab.Join) }
    var currentText by remember { mutableStateOf("") }

    if (state == State.Ready) {
        onDismissRequest()
        model.reset()
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PrimaryTabRow(selectedTabIndex = currentTab.index) {
                    for (tab in Tab.entries) {
                        Tab(
                            selected = currentTab == tab,
                            onClick = {
                                model.tabChanged()
                                currentTab = tab
                            },
                            enabled = state != State.Loading,
                            text = {
                                Text(
                                    text = tab.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

                Column(
                    modifier = modifier,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = currentTab.hint)

                        Spacer(modifier = Modifier.height(48.dp))

                        OutlinedTextField(
                            value = currentText,
                            onValueChange = { currentText = it },
                            maxLines = 1,
                            enabled = state != State.Loading,
                            isError = state == State.Error,
                            label = {
                                Text(text = currentTab.label)
                            },
                            placeholder = {
                                Text(text = currentTab.placeholder)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 20.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            enabled = state != State.Loading,
                            onClick = {
                                if (currentTab == Tab.Join) {
                                    model.joinGuild(currentText)
                                } else {
                                    model.createGuild(currentText)
                                }
                            }
                        ) {
                            if (state == State.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text = currentTab.title)
                            }
                        }
                    }
                }
            }
        }
    }
}
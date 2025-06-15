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

package me.theentropyshard.sheet.view.guild.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.api.model.PublicGuild

@Composable
fun GuildList(
    modifier: Modifier = Modifier,
    guilds: List<PublicGuild>,
    isGuildSelected: (PublicGuild) -> Boolean,
    onMeClick: () -> Unit,
    onAddGuildClick: () -> Unit,
    onChannelClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            GuildItem(selected = false, onClick = onMeClick) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Click to show private chats"
                )
            }
        }

        item {
            Divider()
        }

        items(guilds) {
            GuildItem(selected = isGuildSelected(it), onClick = { onChannelClick(it.id) }) {
                Text(text = "${it.name[0]}")
            }
        }

        item {
            Divider()
        }

        item {
            GuildItem(selected = false, onClick = onAddGuildClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Click to add a new guild"
                )
            }
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier.width(56.dp).fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        HorizontalDivider(
            modifier = Modifier.width(40.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        )
    }
}

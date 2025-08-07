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

package me.theentropyshard.sheet.view.guild.channel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.api.model.PublicGuildTextChannel

@Composable
fun GuildChannelList(
    modifier: Modifier = Modifier,
    channels: List<PublicGuildTextChannel>,
    guildName: String,
    isChannelSelected: (PublicGuildTextChannel) -> Boolean,
    onGuildMenuAction: (GuildMenuItemAction) -> Unit,
    onChannelMenuItemClick: (ChannelMenuItemAction, PublicGuildTextChannel) -> Unit,
    onClick: (String) -> Unit
) {
    var menuVisible by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.TopStart) {
        Column {
            GuildMenu(
                visible = menuVisible,
                onDismissRequest = { menuVisible = false }
            ) { action ->
                onGuildMenuAction(action)
            }
        }

        Column(modifier = modifier) {
            GuildHeader(
                guildName = guildName,
                menuVisible = menuVisible,
                onClick = { menuVisible = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.surfaceContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = channels) {
                    ChannelItem(
                        name = it.name,
                        onMenuItemClick = { action -> onChannelMenuItemClick(action, it) },
                        selected = isChannelSelected(it)
                    ) { onClick(it.mention) }
                }
            }
        }
    }
}
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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.api.model.PublicGuildTextChannel

@Composable
fun ChannelList(
    modifier: Modifier = Modifier,
    channels: List<PublicGuildTextChannel>,
    guildName: String,
    isChannelSelected: (PublicGuildTextChannel) -> Boolean,
    onCreateChannelClick: () -> Unit,
    onClick: (String) -> Unit
) {
    var menuVisible by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (menuVisible) 270f else 90f)

    Box(modifier = modifier, contentAlignment = Alignment.TopStart) {
        Column {
            GuildMenu(
                visible = menuVisible,
                onDismissRequest = { menuVisible = false },
                onCreateChannelClick = onCreateChannelClick
            )
        }

        Column {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .fillMaxWidth()
                    .pointerHoverIcon(icon = PointerIcon.Hand)
                    .clickable { menuVisible = !menuVisible }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    text = guildName
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotation
                    },
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = ""
                )
            }

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
                items(channels) {
                    ChannelItem(
                        name = it.name,
                        selected = isChannelSelected(it)
                    ) { onClick(it.id) }
                }
            }
        }
    }
}
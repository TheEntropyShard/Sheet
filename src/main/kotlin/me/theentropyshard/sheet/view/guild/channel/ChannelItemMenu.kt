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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.theentropyshard.sheet.view.components.contextmenu.Separator
import me.theentropyshard.sheet.view.components.contextmenu.menuItemHeight

enum class ChannelMenuItemAction {
    Rename,
    Delete,
}

@Composable
fun ChannelItemMenu(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onClick: (ChannelMenuItemAction) -> Unit
) {
    DropdownMenu(
        modifier = modifier,
        expanded = visible,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            modifier = Modifier.fillMaxWidth().height(menuItemHeight),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = ""
                )
            },
            text = {
                Text(
                    text = "Rename channel"
                )
            },
            onClick = {
                onDismissRequest()
                onClick(ChannelMenuItemAction.Rename)
            }
        )

        Separator(color = MaterialTheme.colorScheme.surfaceContainerHighest)

        DropdownMenuItem(
            modifier = Modifier.fillMaxWidth().height(menuItemHeight),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = ""
                )
            },
            text = {
                Text(
                    text = "Delete channel",
                    color = MaterialTheme.colorScheme.error
                )
            },
            onClick = {
                onDismissRequest()
                onClick(ChannelMenuItemAction.Delete)
            }
        )
    }
}
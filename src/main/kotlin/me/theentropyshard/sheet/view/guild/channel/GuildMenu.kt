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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.view.components.contextmenu.Separator
import me.theentropyshard.sheet.view.components.contextmenu.menuItemHeight

enum class GuildMenuItemAction {
    CreateChannel,
    CreateInvite,
    DeleteGuild
}

@Composable
fun GuildMenu(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onClick: (GuildMenuItemAction) -> Unit
) {
    DropdownMenu(
        modifier = modifier.width(200.dp),
        expanded = visible,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            modifier = Modifier.fillMaxWidth().height(menuItemHeight),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = ""
                )
            },
            text = {
                Text(text = "Create channel")
            },
            onClick = {
                onDismissRequest()
                onClick(GuildMenuItemAction.CreateChannel)
            }
        )

        DropdownMenuItem(
            modifier = Modifier.fillMaxWidth().height(menuItemHeight),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = ""
                )
            },
            text = {
                Text(
                    text = "Create an invite"
                )
            },
            onClick = {
                onDismissRequest()
                onClick(GuildMenuItemAction.CreateInvite)
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
                    text = "Delete guild",
                    color = MaterialTheme.colorScheme.error
                )
            },
            onClick = {
                onDismissRequest()
                onClick(GuildMenuItemAction.DeleteGuild)
            }
        )
    }
}
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

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import me.theentropyshard.sheet.view.components.contextmenu.Separator
import me.theentropyshard.sheet.view.components.contextmenu.menuItemHeight

enum class MessageContextMenuAction {
    Edit,
    Forward,
    CopyText,
    Delete
}

@Composable
fun MessageContextMenu(
    modifier: Modifier = Modifier,
    visible: Boolean,
    position: Offset,
    onDismissRequest: () -> Unit,
    isActionEnabled: (MessageContextMenuAction) -> Boolean,
    onClick: (MessageContextMenuAction) -> Unit
) {
    DropdownMenu(
        modifier = modifier,
        expanded = visible,
        onDismissRequest = onDismissRequest,
        offset = with(LocalDensity.current) {
            DpOffset(position.x.toDp(), position.y.toDp())
        }
    ) {
        DropdownMenuItem(
            modifier = Modifier.height(menuItemHeight),
            enabled = isActionEnabled(MessageContextMenuAction.Edit),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit message"
                )
            },
            text = {
                Text("Edit")
            },
            onClick = {
                onDismissRequest()
                onClick(MessageContextMenuAction.Edit)
            }
        )

        DropdownMenuItem(
            modifier = Modifier.height(menuItemHeight),
            enabled = isActionEnabled(MessageContextMenuAction.Forward),
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Forward,
                    contentDescription = "Forward message"
                )
            },
            text = {
                Text("Forward")
            },
            onClick = {
                onDismissRequest()
                onClick(MessageContextMenuAction.Forward)
            }
        )

        DropdownMenuItem(
            modifier = Modifier.height(menuItemHeight),
            enabled = isActionEnabled(MessageContextMenuAction.CopyText),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copy message text"
                )
            },
            text = {
                Text("Copy Text")
            },
            onClick = {
                onDismissRequest()
                onClick(MessageContextMenuAction.CopyText)
            }
        )

        Separator(color = MaterialTheme.colorScheme.surfaceContainerHighest)

        DropdownMenuItem(
            modifier = Modifier.height(menuItemHeight),
            enabled = isActionEnabled(MessageContextMenuAction.Delete),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete message",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            },
            onClick = {
                onDismissRequest()
                onClick(MessageContextMenuAction.Delete)
            }
        )
    }
}
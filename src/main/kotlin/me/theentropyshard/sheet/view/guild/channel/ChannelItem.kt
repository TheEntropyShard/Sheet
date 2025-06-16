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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAll

@Composable
fun ChannelItem(
    modifier: Modifier = Modifier,
    name: String,
    selected: Boolean,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    var menuShown by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .fillMaxWidth()
            .pointerHoverIcon(icon = PointerIcon.Hand)
            .drawBehind {
                if (selected) {
                    drawRoundRect(
                        color = scheme.secondaryContainer,
                        topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                        size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    )
                }
            }
            .clickable { onClick() }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val event = awaitEventFirstDown()
                    if (event.buttons.isSecondaryPressed) {
                        event.changes.forEach { it.consume() }
                        menuShown = true
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        DropdownMenu(
            modifier = Modifier.width(200.dp),
            expanded = menuShown,
            onDismissRequest = { menuShown = false }
        ) {
            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth().height(32.dp),
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
                    menuShown = false
                    onRename()
                }
            )

            Separator(color = MaterialTheme.colorScheme.surfaceContainerHighest)

            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth().height(32.dp),
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
                    menuShown = false
                    onDelete()
                }
            )
        }

        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            text = name
        )
    }
}

private suspend fun AwaitPointerEventScope.awaitEventFirstDown(): PointerEvent {
    var event: PointerEvent
    do {
        event = awaitPointerEvent()
    } while (
        !event.changes.fastAll { it.changedToDown() }
    )
    return event
}
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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.asyncPainterResource
import me.theentropyshard.sheet.Sheet
import me.theentropyshard.sheet.api.model.PublicMessage
import me.theentropyshard.sheet.utils.painterResource
import me.theentropyshard.sheet.view.components.NoMaxSizeImage
import me.theentropyshard.sheet.view.components.contextmenu.Separator
import me.theentropyshard.sheet.view.components.contextmenu.menuItemHeight
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss")
private val pingColor = Color(0xFFFAD6A5)
private val pingColorHover = Color(0xFFD1A364)

private fun PublicMessage.isPing(user: String): Boolean {
    return this.content != null && this.content.startsWith("$user: ")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: PublicMessage
) {
    val source = remember { MutableInteractionSource() }
    val isHovered by source.collectIsHoveredAsState()

    var menuVisible by remember { mutableStateOf(false) }

    var offset by remember { mutableStateOf(Offset.Zero) }

    Row(
        modifier = modifier
            .background(
                when {
                    message.isPing(Sheet.user.name) && isHovered -> pingColorHover
                    message.isPing(Sheet.user.name) -> pingColor
                    isHovered || menuVisible -> MaterialTheme.colorScheme.surface
                    else -> Color.Unspecified
                }
            )
            .hoverable(source)
            .pointerInput(Unit) {
                detectTapGestures(
                    matcher = PointerMatcher.pointer(
                        pointerType = PointerType.Mouse,
                        button = PointerButton.Secondary
                    )
                ) { offs ->
                    offset = offs - Offset(-5.0f, size.height.toFloat() - 5.0f)
                    menuVisible = true
                }
            }
            .onGloballyPositioned { layoutCoordinates ->

            }
    ) {
        Avatar()

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            MessageHeader(authorId = message.authorId, date = message.published)

            MessageBody(message = message)
        }

        val density = LocalDensity.current

        DisableSelection {
            DropdownMenu(
                expanded = menuVisible,
                onDismissRequest = { menuVisible = false },
                offset = with(density) {
                    DpOffset(offset.x.toDp(), offset.y.toDp())
                }
            ) {
                DropdownMenuItem(
                    modifier = Modifier.height(menuItemHeight),
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

                    }
                )

                DropdownMenuItem(
                    modifier = Modifier.height(menuItemHeight),
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

                    }
                )

                DropdownMenuItem(
                    modifier = Modifier.height(menuItemHeight),
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

                    }
                )

                Separator(color = MaterialTheme.colorScheme.surfaceContainerHighest)

                DropdownMenuItem(
                    modifier = Modifier.height(menuItemHeight),
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

                    }
                )
            }
        }
    }
}

@Composable
private fun Avatar(
    modifier: Modifier = Modifier
) {
    Icon(imageVector = Icons.Filled.Person, contentDescription = "")
}

@Composable
fun MessageHeader(
    modifier: Modifier = Modifier,
    authorId: String,
    date: String
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = authorId.split("@")[0],
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.0.sp,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = formatter.format(ZonedDateTime.parse(date).withZoneSameInstant(ZoneOffset.systemDefault())),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MessageBody(
    modifier: Modifier = Modifier,
    message: PublicMessage
) {
    if (!message.hasText() && !message.hasAttachments()) {
        return
    }

    Column(
        modifier = modifier
    ) {
        if (message.hasText()) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (message.hasAttachments()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (attachment in message.files) {
                    if (attachment.type.startsWith("image/")) {
                        ImageAttachment(
                            name = attachment.name,
                            hash = attachment.hash,
                            channelId = message.channelId
                        )

                        // TODO: download image
                    } else {
                        FileAttachment(
                            name = attachment.name,
                            size = attachment.size
                        ) {
                            // TODO: download file
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageAttachment(
    modifier: Modifier = Modifier,
    name: String,
    hash: String,
    channelId: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        NoMaxSizeImage(
            modifier = Modifier.padding(6.dp).clip(RoundedCornerShape(6.dp)),
            contentDescription = name,
            resource = {
                asyncPainterResource(data = "${Sheet.instance}/channel/$channelId/attachments/$hash")
            },
            onLoading = {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            },
            onFailure = { throwable ->
                Text(text = "Error: cannot load the image: ${throwable.toString()}")
            }
        )
    }
}

@Composable
fun FileAttachment(
    modifier: Modifier = Modifier,
    name: String,
    size: Long,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource("/icons/draft_24px.xml"),
            contentDescription = ""
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(verticalArrangement = Arrangement.SpaceEvenly) {
            Text(
                text = name,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "$size B",
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
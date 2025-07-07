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

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.asyncPainterResource
import me.theentropyshard.sheet.Sheet
import me.theentropyshard.sheet.model.Message
import me.theentropyshard.sheet.utils.painterResource
import me.theentropyshard.sheet.view.components.NoMaxSizeImage
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss")
private val pingColor = Color(0xFFFAD6A5)
private val pingColorHover = Color(0xFFD1A364)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: Message,
    sameAuthorPrev: Boolean,
    sameAuthorNext: Boolean,
    onContextMenuAction: (MessageContextMenuAction, Message) -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val isHovered by source.collectIsHoveredAsState()

    var menuVisible by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val padding = if (sameAuthorPrev) {
        PaddingValues(2.dp)
    } else {
        if (sameAuthorNext) {
            PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 2.dp)
        } else {
            PaddingValues(8.dp)
        }
    }

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
            .padding(padding)
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
    ) {
        if (!sameAuthorPrev) {
            Avatar()

            Spacer(modifier = Modifier.width(6.dp))
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column {
            if (!sameAuthorPrev) {
                MessageHeader(
                    authorName = message.authorId.substringBefore('@'),
                    date = formatter.format(
                        ZonedDateTime
                            .parse(message.published)
                            .withZoneSameInstant(ZoneOffset.systemDefault())
                    )
                )
            }

            MessageBody(message = message)
        }

        DisableSelection {
            MessageContextMenu(
                visible = menuVisible,
                onDismissRequest = { menuVisible = false },
                position = offset,
                isActionEnabled = { action ->
                    when (action) {
                        MessageContextMenuAction.Edit -> false
                        MessageContextMenuAction.Forward -> false
                        MessageContextMenuAction.CopyText -> message.hasText()
                        MessageContextMenuAction.Delete -> true
                    }
                },
                onClick = { onContextMenuAction(it, message) }
            )
        }
    }
}

@Composable
private fun Avatar(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .pointerHoverIcon(icon = PointerIcon.Hand)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = Icons.Filled.Person, contentDescription = "")
    }
}

@Composable
fun MessageHeader(
    modifier: Modifier = Modifier,
    authorName: String,
    date: String
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = authorName,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.0.sp,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MessageBody(
    modifier: Modifier = Modifier,
    message: Message
) {
    if (!message.hasText() && !message.hasAttachments()) {
        return
    }

    Column(modifier = modifier) {
        if (message.hasText()) {
            Text(
                text = message.text!!,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (message.hasAttachments()) {
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (attachment in message.attachments!!) {
                    if (attachment.type.startsWith("image/")) {
                        ImageAttachment(
                            name = attachment.name,
                            url = "${Sheet.instance}/channel/${message.channelId}/attachments/${attachment.hash}"
                        ) {
                            // TODO: download image
                        }
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
    url: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSystemInDarkTheme()) Color.Black else Color.White)
        ) {
            NoMaxSizeImage(
                modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                contentDescription = name,
                resource = {
                    asyncPainterResource(data = url)
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
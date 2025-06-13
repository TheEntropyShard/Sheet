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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.asyncPainterResource
import me.theentropyshard.sheet.Sheet
import me.theentropyshard.sheet.api.model.PublicMessage
import me.theentropyshard.sheet.view.chat.attachment.AttachmentItem
import me.theentropyshard.sheet.view.components.NoMaxSizeImage
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss")
private val pingColor = Color(0xFFFAD6A5)

private fun PublicMessage.isPing(user: String): Boolean {
    return this.content != null && this.content.startsWith("$user: ")
}

@Composable
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: PublicMessage
) {
    Row(
        modifier = if (message.isPing(Sheet.user.name)) {
            modifier.background(pingColor)
        } else {
            modifier
        }
    ) {
        Avatar()

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            MessageHeader(authorId = message.authorId, date = message.published)

            MessageBody(message = message)
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
            fontSize = 16.sp
        )

        Text(text = formatter.format(ZonedDateTime.parse(date).withZoneSameInstant(ZoneOffset.systemDefault())))
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
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (message.hasText()) {
            Text(text = message.content)
        }

        if (message.hasAttachments()) {
            for (attachment in message.files) {
                if (attachment.type.startsWith("image/")) {
                    ImageAttachment(
                        name = attachment.name,
                        hash = attachment.hash,
                        channelId = message.channelId
                    )

                    // TODO: download image
                } else {
                    FileAttachment(name = attachment.name, size = attachment.size) {
                        // TODO: download file
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
        color = MaterialTheme.colorScheme.surface
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
    AttachmentItem(
        modifier = modifier,
        name = name,
        size = size,
        onClick = onClick
    )
}
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

package me.theentropyshard.sheet.model

import me.theentropyshard.sheet.api.model.PublicAttachment
import me.theentropyshard.sheet.api.model.PublicMessage

data class Message(
    val id: String,
    val authorId: String,
    val channelId: String,
    val published: String,
    val updated: String,
    val text: String?,
    val attachments: List<PublicAttachment>?,
) {
    fun hasText(): Boolean {
        return this.text != null && this.text.isNotEmpty()
    }

    fun hasAttachments(): Boolean {
        return this.attachments != null && this.attachments.isNotEmpty()
    }

    // this is not an official way to do mentions
    fun isPing(user: String): Boolean {
        return this.text != null && this.text.startsWith("$user: ")
    }
}

fun PublicMessage.toMessage(): Message {
    return Message(
        id = this.id,
        authorId = this.authorId,
        channelId = this.channelId,
        published = this.published,
        updated = this.updated,
        text = this.content,
        attachments = this.files
    )
}
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

import me.theentropyshard.sheet.api.model.PrivateDmChannel
import me.theentropyshard.sheet.api.model.PublicGuildTextChannel

data class Channel(
    val mention: String,
    val name: String,

    // These will contain owner mention and recipients mentions if this channel is a private DM channel
    val owner: String? = null,
    val recipients: List<String>? = null,

    // This will contain a mention if this channel belongs to a guild
    val guild: String? = null
) {
    fun isGuildChannel(): Boolean {
        return this.guild != null
    }

    fun isDMChannel(): Boolean {
        return this.owner != null && this.recipients != null
    }
}

fun PrivateDmChannel.toChannel(): Channel {
    return Channel(
        mention = mention,
        name = name,
        owner = owner,
        recipients = recipients
    )
}

fun PublicGuildTextChannel.toChannel(): Channel {
    return Channel(
        mention = mention,
        name = name,
        guild = guild,
    )
}

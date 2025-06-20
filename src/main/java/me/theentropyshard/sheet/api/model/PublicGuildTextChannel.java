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

package me.theentropyshard.sheet.api.model;

import com.google.gson.annotations.SerializedName;

public class PublicGuildTextChannel {
    public String id;
    public String name;
    public String domain;
    @SerializedName("guild_id")
    public String guildId;

    public PublicGuildTextChannel() {

    }

    public PublicGuildTextChannel(PublicGuildTextChannel channel) {
        this.id = channel.id;
        this.name = channel.name;
        this.domain = channel.domain;
        this.guildId = channel.guildId;
    }

    public String completeId() {
        return this.id + "@" + this.domain;
    }

    @Override
    public String toString() {
        return "PublicGuildTextChannel{" +
            "id='" + this.id + '\'' +
            ", name='" + this.name + '\'' +
            ", domain='" + this.domain + '\'' +
            ", guildId='" + this.guildId + '\'' +
            '}';
    }
}

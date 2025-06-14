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

import java.util.List;

public class PublicMessage {
    public String id;
    public String content;
    public String published;
    public String updated;
    @SerializedName("author_id")
    public String authorId;
    @SerializedName("channel_id")
    public String channelId;
    public List<PublicAttachment> files;

    public boolean hasText() {
        return this.content != null;
    }

    public boolean hasAttachments() {
        return !this.files.isEmpty();
    }
}

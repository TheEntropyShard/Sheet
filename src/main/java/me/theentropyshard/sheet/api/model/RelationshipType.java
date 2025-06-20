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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public enum RelationshipType {
    PENDING,
    ACCEPTED,
    BLOCKED;

    public static final class Adapter extends TypeAdapter<RelationshipType> {
        @Override
        public void write(JsonWriter writer, RelationshipType type) throws IOException {
            int value = switch (type) {
                case PENDING -> 0;
                case ACCEPTED -> 1;
                case BLOCKED -> 2;
            };

            writer.value(value);
        }

        @Override
        public RelationshipType read(JsonReader reader) throws IOException {
            int value = reader.nextInt();

            return switch (value) {
                case 0 -> RelationshipType.PENDING;
                case 1 -> RelationshipType.ACCEPTED;
                case 2 -> RelationshipType.BLOCKED;

                default -> throw new RuntimeException("Unknown relationship type: " + value);
            };
        }
    }
}

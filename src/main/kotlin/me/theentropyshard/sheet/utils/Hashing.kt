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

package me.theentropyshard.sheet.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun File.md5Hex(): String {
    val b: ByteArray = this.md5Bytes()

    return buildString {
        for (i in b.indices) {
            this.append(((b[i].toInt() and 0xff) + 0x100).toString(16).substring(1))
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun File.md5Base64(): String {
    val b: ByteArray = this.md5Bytes()

    return Base64.encode(b)
}

fun File.md5Bytes(): ByteArray {
    val data = Files.readAllBytes(Paths.get(this.absolutePath))
    val hash = MessageDigest.getInstance("MD5").digest(data)

    return hash
}
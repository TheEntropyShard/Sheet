package me.theentropyshard.sheet.utils

import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

private val mimeTypes = mutableMapOf<String, String>()

private fun loadMimeTypes() {
    ByteArrayInputStream(readResourceBytes("/mime-types-to-extensions.txt"))
        .bufferedReader(StandardCharsets.UTF_8).use { reader ->
            reader.lines().forEach { line ->
                val list = line.split(" ")
                mimeTypes.put(list[0], list[1])
            }
        }
}

fun File.mimeType(charset: String? = "utf-8"): String {
    if (mimeTypes.isEmpty()) {
        loadMimeTypes()
    }

    val mime = mimeTypes[this.extension.lowercase()]

    return if (mime == null) {
        "application/octet-stream"
    } else {
        if (mime.startsWith("text/")) {
            mime + if (charset != null && charset.trim().isNotEmpty()) "; charset=$charset" else ""
        } else {
            mime
        }
    }
}

fun File.isImage(): Boolean {
    val mime = this.mimeType()

    return mime.startsWith("image/")
}
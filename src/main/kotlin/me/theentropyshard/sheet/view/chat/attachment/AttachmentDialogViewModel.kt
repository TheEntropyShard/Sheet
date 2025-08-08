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

package me.theentropyshard.sheet.view.chat.attachment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.theentropyshard.sheet.Sheet.gson
import me.theentropyshard.sheet.Sheet.httpClient
import me.theentropyshard.sheet.Sheet.instance
import me.theentropyshard.sheet.Sheet.token
import me.theentropyshard.sheet.toRequestBody
import me.theentropyshard.sheet.utils.isImage
import me.theentropyshard.sheet.utils.md5Base64
import me.theentropyshard.sheet.utils.md5Hex
import me.theentropyshard.sheet.utils.mimeType
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class AttachmentDialogViewModel : ViewModel() {
    private val logger = LogManager.getLogger()

    private val _files = MutableStateFlow(listOf<File>())
    val files = _files.asStateFlow()

    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    fun setFiles(files: List<File>) {
        _files.update { files }
        _count.update { _files.value.size }
    }

    fun addFiles(newFiles: List<File>) {
        _files.update { files -> files + newFiles }
        _count.update { _files.value.size }
    }

    fun send(channelId: String, caption: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val hashes = mutableMapOf<String, File>()

            for (file in files.value) {
                hashes.put(file.md5Hex(), file)
            }

            val items = mutableListOf<JsonObject>()

            for (entry in hashes) {
                items.add(JsonObject().apply {
                    addProperty("id", entry.key)
                    addProperty("name", entry.value.name)
                    addProperty("md5", entry.value.md5Base64())
                    addProperty("mime", entry.value.mimeType())
                    addProperty("size", entry.value.length())

                    if (entry.value.isImage()) {
                        val img = ImageIO.read(entry.value)
                        addProperty("width", img.width)
                        addProperty("height", img.height)
                    }
                })
            }

            val createAttachmentsRequest = Request.Builder()
                .url("${instance}/channel/$channelId/attachments")
                .header("Authorization", "Bearer $token")
                .post(items.toRequestBody())
                .build()

            val attachmentsData: List<JsonObject>

            httpClient.newCall(createAttachmentsRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.error(
                        "Could not create attachments. Code: {}, Message: {}",
                        response.code, response.body.string()
                    )

                    return@launch
                }

                val type = object : TypeToken<List<JsonObject>>() {}.type
                attachmentsData = gson.fromJson(response.body.string(), type)
            }

            val uploadedHashes = mutableMapOf<String, String>()

            for (obj in attachmentsData) {
                val uploadUrl = obj["url"].asString

                uploadedHashes[hashes[obj["id"].asString]!!.name] = obj["hash"].asString

                val uploadRequest = Request.Builder()
                    .url(uploadUrl)
                    .header("Content-MD5", hashes[obj["id"].asString]!!.md5Base64())
                    .header("Content-Type", hashes[obj["id"].asString]!!.mimeType())
                    .put(hashes[obj["id"].asString]!!.readBytes().toRequestBody())
                    .build()

                httpClient.newCall(uploadRequest).execute().use { response ->
                    println(response.code)
                }
            }

            val data = JsonObject()

            if (caption.trim().isNotEmpty()) {
                data.addProperty("content", caption)
            }

            val files = JsonArray()

            for (entry in uploadedHashes) {
                val file = JsonObject()
                file.addProperty("name", entry.key)
                file.addProperty("hash", entry.value)
                files.add(file)
            }

            data.add("files", files)

            val request = Request.Builder()
                .url("${instance}/channel/$channelId/messages")
                .header("Authorization", "Bearer $token")
                .post(data.toRequestBody())
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        println(response.code)
                        println(response.body.string())
                    }
                }
            })
        }
    }
}
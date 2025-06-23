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

package me.theentropyshard.sheet.view.main

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import me.theentropyshard.sheet.Sheet.webSocket
import me.theentropyshard.sheet.api.model.PrivateRelationship
import me.theentropyshard.sheet.api.model.PublicGuild
import me.theentropyshard.sheet.api.model.PublicGuildTextChannel
import me.theentropyshard.sheet.api.model.PublicMessage
import me.theentropyshard.sheet.fromJson
import me.theentropyshard.sheet.toRequestBody
import okhttp3.*
import okhttp3.internal.closeQuietly
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.util.*

class MainViewModel : ViewModel() {
    private val logger = LogManager.getLogger()

    private val _isLoadingInitial = MutableStateFlow(true)
    val isLoadingInitial = _isLoadingInitial.asStateFlow()

    private val _currentGuild: MutableStateFlow<PublicGuild?> = MutableStateFlow(null)
    val currentGuild = _currentGuild.asStateFlow()

    private val _currentChannel: MutableStateFlow<PublicGuildTextChannel?> = MutableStateFlow(null)
    val currentChannel = _currentChannel.asStateFlow()

    val guilds = mutableStateListOf<PublicGuild>()
    val channels = mutableStateListOf<PublicGuildTextChannel>()
    val messages = mutableStateListOf<PublicMessage>()
    val members = mutableStateListOf<JsonObject>()
    val relationships = mutableStateListOf<PrivateRelationship>()

    private var sequence: Int = 0

    fun loggedIn() {
        createWebSocket()
    }

    fun startHeartbeat() {
        Timer(true).schedule(
            object : TimerTask() {
                override fun run() {
                    webSocket.send("{ \"t\": \"heartbeat\", \"s\": $sequence }")
                }
            }, 0, 4500L
        )
    }

    fun createWebSocket() {
        val request = Request.Builder()
            .url(instance.replace("https", "wss"))
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val message = JsonObject()
                message.addProperty("t", "identify")
                message.addProperty("token", token)

                webSocket.send(gson.toJson(message))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val seq = sequence++

                val message = gson.fromJson(text, JsonObject::class.java)

                if (message["t"].asString != "HEARTBEAT_ACK") {
                    println(message)
                }

                when (message["t"].asString) {
                    "READY" -> {
                        val guildsArray = message["d"].asJsonObject["guilds"].asJsonArray

                        for (guildElement in guildsArray) {
                            val guild = gson.fromJson(guildElement, PublicGuild::class)

                            guilds += guild

                            for (channel in (guildElement as JsonObject)["channels"].asJsonArray) {
                                val parsedChannel = gson.fromJson(channel, PublicGuildTextChannel::class)
                                parsedChannel.guildId = guild.completeId()

                                channels += parsedChannel
                            }
                        }

                        val relationshipsArray = message["d"].asJsonObject["relationships"].asJsonArray

                        for (relationshipElement in relationshipsArray) {
                            relationships += gson.fromJson(relationshipElement, PrivateRelationship::class)
                        }

                        viewModelScope.launch {
                            selectGuild(guilds[0].id)
                        }

                        startHeartbeat()

                        _isLoadingInitial.update { false }
                    }

                    "HEARTBEAT_ACK" -> {
                        val recSeq = message["s"].asInt

                        if (recSeq != seq) {
                            throw RuntimeException("Out of sync. Sequence mismatch: received: $recSeq, our: $seq")
                        }
                    }

                    "INVITE_CREATE" -> {
                        println("warn: unhandled message: INVITE_CREATE")
                    }

                    "MESSAGE_CREATE" -> {
                        val shootMessage =
                            gson.fromJson(message["d"].asJsonObject["message"], PublicMessage::class.java)

                        messages.add(0, shootMessage)
                    }

                    "MESSAGE_DELETE" -> {
                        val channelId = message["d"].asJsonObject["channel_id"].asString
                        val messageId = message["d"].asJsonObject["message_id"].asString

                        val shootMessage = messages.find { message ->
                            message.channelId == channels.find { channel ->
                                channel.id == channelId
                            }?.completeId() && message.id == messageId
                        }

                        if (shootMessage != null) {
                            messages -= shootMessage
                        }
                    }

                    "RELATIONSHIP_CREATE" -> {
                        println("warn: unhandled message: RELATIONSHIP_CREATE")
                    }

                    "RELATIONSHIP_DELETE" -> {
                        println("warn: unhandled message: RELATIONSHIP_DELETE")
                    }

                    "GUILD_CREATE" -> {
                        val guild =
                            gson.fromJson(message["d"].asJsonObject["guild"], PublicGuild::class.java)

                        guilds += guild
                        _currentGuild.update { guild }
                    }

                    "GUILD_DELETE" -> {
                        val guildId = message["d"].asJsonObject["guild_id"].asString

                        val guild = guilds.find { guild -> guild.id == guildId }

                        if (guild != null) {
                            guilds -= guild
                        }
                    }

                    "CHANNEL_CREATE" -> {
                        val channel = gson.fromJson(message["d"].asJsonObject["channel"], PublicGuildTextChannel::class)
                        channel.guildId += "@${channel.domain}"

                        val guild = guilds.find { guild -> guild.completeId() == channel.guildId }!!
                        guild.channels.add(channel)

                        channels += channel
                        _currentChannel.update { channel }
                    }

                    "CHANNEL_DELETE" -> {
                        val channelId = message["d"].asJsonObject["channel_id"].asString
                        val guildId = message["d"].asJsonObject["guild_id"].asString

                        channels.removeIf { channel ->
                            channel.completeId() == channelId && channel.guildId == guildId
                        }
                    }

                    "CHANNEL_UPDATE" -> {
                        val updatedChannel =
                            gson.fromJson(message["d"].asJsonObject["channel"], PublicGuildTextChannel::class)

                        val foundChannel =
                            channels.find { channel -> channel.completeId() == updatedChannel.completeId() }

                        if (foundChannel != null) {
                            channels[channels.indexOf(foundChannel)] = PublicGuildTextChannel(updatedChannel)
                        }
                    }

                    "MEMBERS_CHUNK" -> {
                        val array = message["d"].asJsonObject["items"].asJsonArray

                        members.apply {
                            clear()

                            addAll(array.filter { it.isJsonObject }.map { it.asJsonObject })
                        }
                    }

                    "MEMBER_JOIN" -> {
                        println("warn: unhandled message: MEMBER_JOIN")
                    }

                    "MEMBER_LEAVE" -> {
                        println("warn: unhandled message: MEMBER_LEAVE")
                    }

                    "ROLE_CREATE" -> {
                        println("warn: unhandled message: ROLE_ADD")
                    }

                    "ROLE_MEMBER_ADD" -> {
                        println("warn: unhandled message: ROLE_MEMBER_ADD")
                    }

                    "ROLE_MEMBER_LEAVE" -> {
                        println("warn: unhandled message: ROLE_MEMBER_LEAVE")
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                when (code) {
                    1000 -> logger.info("WebSocket was closed successfully")
                    else -> logger.error("WebSocket was closed with code $code because $reason")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                logger.error("WebSocket failure", t)
            }
        }

        webSocket = httpClient.newWebSocket(request, listener)
    }

    fun subscribeForChannelMembersRange(min: Int = 0, max: Int = 100) {
        viewModelScope.launch(Dispatchers.IO) {
            webSocket.send(
                "{ \"t\": \"members\", \"channel_id\": \"${_currentChannel.value?.completeId()}\", " +
                        "\"range\": [$min, $max] }"
            )
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = JsonObject()
            data.addProperty("content", text)

            val request = Request.Builder()
                .url("${instance}/channel/${_currentChannel.value?.completeId()}/messages")
                .header("Authorization", "Bearer $token")
                .post(data.toRequestBody())
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request to send message", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to send message: {}", response.body!!.string())
                    }

                    response.closeQuietly()
                }
            })
        }
    }

    fun selectGuild(id: String) {
        for (guild in guilds) {
            if (guild.id == id) {
                _currentGuild.value = guild
                selectChannel(guild.channels[0].id)

                break
            }
        }
    }

    fun selectChannel(id: String) {
        for (channel in _currentGuild.value!!.channels) {
            if (channel.id == id) {
                _currentChannel.value = channel
                loadMessages("$id@${channel.domain}", false)
                subscribeForChannelMembersRange()

                break
            }
        }
    }

    fun loadMessages(channelId: String, add: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${instance}/channel/$channelId/messages")
                .header("Authorization", "Bearer $token")
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request to load messages", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to load messages: {}", response.body!!.string())

                        response.closeQuietly()

                        return
                    }

                    val receivedMessages: MutableList<PublicMessage> = gson.fromJson(
                        response.body!!.string(),
                        object : TypeToken<MutableList<PublicMessage>>() {}.type
                    )

                    viewModelScope.launch {
                        //_messages.update { list -> if (add) messages + list else messages }

                        if (add) {
                            messages += receivedMessages
                        } else {
                            messages.apply {
                                clear()

                                addAll(receivedMessages)
                            }
                        }
                    }
                }
            })
        }
    }

    fun createChannel(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = JsonObject()
            data.addProperty("name", name)

            val request = Request.Builder()
                .url("${instance}/guild/${_currentGuild.value?.id}@${_currentGuild.value?.domain}/channel")
                .header("Authorization", "Bearer $token")
                .post(data.toRequestBody())
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request for to create channel", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to create channel: {}", response.body!!.string())
                    }

                    response.closeQuietly()
                }
            })
        }
    }

    fun renameChannel(channelId: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = JsonObject()
            data.addProperty("name", name)

            val request = Request.Builder()
                .url("${instance}/channel/$channelId")
                .header("Authorization", "Bearer $token")
                .patch(data.toRequestBody())
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request for to rename channel", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to rename channel: {}", response.body!!.string())
                    }

                    response.closeQuietly()
                }
            })
        }
    }

    fun deleteGuild() {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${instance}/guild/${_currentGuild.value?.completeId()}")
                .header("Authorization", "Bearer $token")
                .delete()
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request for to delete guild", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to delete guild: {}", response.body!!.string())
                    }

                    response.closeQuietly()
                }
            })
        }
    }

    fun deleteChannel(completeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${instance}/channel/$completeId")
                .header("Authorization", "Bearer $token")
                .delete()
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request for to delete channel", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to delete channel: {}", response.body!!.string())
                    }

                    response.closeQuietly()
                }
            })
        }
    }

    fun deleteMessage(channelId: String, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${instance}/channel/$channelId/messages/$id")
                .header("Authorization", "Bearer $token")
                .delete()
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request for to delete message", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to delete message: {}", response.body!!.string())
                    }

                    response.closeQuietly()
                }
            })
        }
    }
}
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
import me.theentropyshard.sheet.api.model.PrivateDmChannel
import me.theentropyshard.sheet.api.model.PrivateRelationship
import me.theentropyshard.sheet.api.model.PublicGuild
import me.theentropyshard.sheet.api.model.PublicGuildTextChannel
import me.theentropyshard.sheet.api.model.PublicMessage
import me.theentropyshard.sheet.fromJson
import me.theentropyshard.sheet.model.Channel
import me.theentropyshard.sheet.model.Message
import me.theentropyshard.sheet.model.toChannel
import me.theentropyshard.sheet.model.toMessage
import me.theentropyshard.sheet.toRequestBody
import okhttp3.*
import okhttp3.internal.closeQuietly
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.util.*

enum class CurrentView {
    Friends,
    Private,
    Guild
}

class MainViewModel : ViewModel() {
    private val logger = LogManager.getLogger()

    private val _isLoadingInitial = MutableStateFlow(true)
    val isLoadingInitial = _isLoadingInitial.asStateFlow()

    private val _currentGuild: MutableStateFlow<PublicGuild?> = MutableStateFlow(null)
    val currentGuild = _currentGuild.asStateFlow()

    private val _currentChannel: MutableStateFlow<Channel?> = MutableStateFlow(null)
    val currentChannel = _currentChannel.asStateFlow()

    private val _currentView = MutableStateFlow(CurrentView.Guild)
    val currentView = _currentView.asStateFlow()

    val guilds = mutableStateListOf<PublicGuild>()
    val guildChannels = mutableStateListOf<PublicGuildTextChannel>()
    val messages = mutableStateListOf<Message>()
    val members = mutableStateListOf<JsonObject>()
    val relationships = mutableStateListOf<PrivateRelationship>()
    val privateChannels = mutableStateListOf<PrivateDmChannel>()

    private var sequence: Int = 0
    private var reconnectAttempts: Int = 0

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
        val url = if (instance.startsWith("http://")) {
            instance.replace("http", "ws")
        } else if (instance.startsWith("https://")) {
            instance.replace("https", "wss")
        } else {
            throw RuntimeException("Wrong instance url: $instance")
        }

        val request = Request.Builder()
            .url(url)
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

                        if (guildsArray.size() > 0) {
                            for (guildElement in guildsArray) {
                                val guild = gson.fromJson(guildElement, PublicGuild::class)

                                guilds += guild

                                for (channel in (guildElement as JsonObject)["channels"].asJsonArray) {
                                    val parsedChannel = gson.fromJson(channel, PublicGuildTextChannel::class)
                                    parsedChannel.guild = guild.mention

                                    guildChannels += parsedChannel
                                }
                            }

                            viewModelScope.launch {
                                selectGuild(guilds[0].mention)
                            }
                        }

                        val relationshipsArray = message["d"].asJsonObject["relationships"].asJsonArray

                        for (relationshipElement in relationshipsArray) {
                            relationships += gson.fromJson(relationshipElement, PrivateRelationship::class)
                        }

                        val channelsArray = message["d"].asJsonObject["channels"].asJsonArray

                        for (channelElement in channelsArray) {
                            privateChannels += gson.fromJson(channelElement, PrivateDmChannel::class)
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

                        messages.add(0, shootMessage.toMessage())
                    }

                    "MESSAGE_DELETE" -> {
                        val channelMention = message["d"].asJsonObject["channel"].asString
                        val messageMention = message["d"].asJsonObject["message"].asString

                        val shootMessage = messages.find { message ->
                            message.channelId == guildChannels.find { channel ->
                                channel.mention == channelMention
                            }?.mention && message.id == messageMention
                        }

                        if (shootMessage != null) {
                            messages -= shootMessage
                        }
                    }

                    "RELATIONSHIP_CREATE" -> {
                        val relationship =
                            gson.fromJson(message["d"].asJsonObject["relationship"], PrivateRelationship::class)
                        relationships += relationship
                    }

                    "RELATIONSHIP_DELETE" -> {
                        val userMention = message["d"].asJsonObject["user"].asString
                        relationships.removeIf { it.user.mention == userMention }
                    }

                    "RELATIONSHIP_UPDATE" -> {
                        val relationship =
                            gson.fromJson(message["d"].asJsonObject["relationship"], PrivateRelationship::class)
                        relationships.removeIf { it.user.mention == relationship.user.mention }
                        relationships += relationship
                    }

                    "GUILD_CREATE" -> {
                        val guild =
                            gson.fromJson(message["d"].asJsonObject["guild"], PublicGuild::class.java)

                        guilds += guild
                        _currentGuild.update { guild }
                    }

                    "GUILD_DELETE" -> {
                        val guildMention = message["d"].asJsonObject["guild"].asString

                        val guild = guilds.find { guild -> guild.mention == guildMention }

                        if (guild != null) {
                            guilds -= guild
                        }
                    }

                    "CHANNEL_CREATE" -> {
                        val channelObj = gson.fromJson(message["d"].asJsonObject["channel"], JsonObject::class)

                        if (channelObj.has("guild")) {
                            val channel = gson.fromJson(channelObj, PublicGuildTextChannel::class)

                            val guild = guilds.find { guild -> guild.mention == channel.guild }!!
                            guild.channels.add(channel)

                            guildChannels += channel
                            _currentChannel.update {
                                Channel(
                                    mention = channel.mention,
                                    name = channel.name,
                                    guild = guild.mention
                                )
                            }
                        } else {
                            val channel = gson.fromJson(channelObj, PrivateDmChannel::class)

                            privateChannels += channel
                            _currentChannel.update {
                                Channel(
                                    mention = channel.mention,
                                    name = channel.name,
                                    owner = channel.owner,
                                    recipients = channel.recipients
                                )
                            }
                        }
                    }

                    "CHANNEL_DELETE" -> {
                        val channelMention = message["d"].asJsonObject["channel"].asString
                        val guildMention = message["d"].asJsonObject["guild"].asString

                        guildChannels.removeIf { channel ->
                            channel.mention == channelMention && channel.guild == guildMention
                        }
                    }

                    "CHANNEL_UPDATE" -> {
                        val updatedChannel =
                            gson.fromJson(message["d"].asJsonObject["channel"], PublicGuildTextChannel::class)

                        val foundChannel =
                            guildChannels.find { channel -> channel.mention == updatedChannel.mention }

                        if (foundChannel != null) {
                            guildChannels[guildChannels.indexOf(foundChannel)] = PublicGuildTextChannel(updatedChannel)
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
                        println("warn: unhandled message: ROLE_CREATE")
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

                reconnectAttempts++

                if (reconnectAttempts <= 5) {
                    logger.info("Trying to reconnect WebSocket. Attempt: $reconnectAttempts")
                    createWebSocket()
                } else {
                    logger.warn("Not trying to reconnect WebSocket, too many attempts: $reconnectAttempts")
                }
            }
        }

        webSocket = httpClient.newWebSocket(request, listener)
    }

    fun switchToFriendsView() {
        _currentView.value = CurrentView.Friends
    }

    fun switchToPrivateView() {
        _currentView.value = CurrentView.Private
    }

    fun switchToGuildView() {
        _currentView.value = CurrentView.Guild
    }

    fun subscribeForChannelMembersRange(min: Int = 0, max: Int = 100) {
        viewModelScope.launch(Dispatchers.IO) {
            webSocket.send(
                "{ \"t\": \"members\", \"channel_id\": \"${_currentChannel.value?.mention}\", " +
                        "\"range\": [$min, $max] }"
            )
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = JsonObject()
            data.addProperty("content", text)

            val request = Request.Builder()
                .url("${instance}/channel/${_currentChannel.value?.mention}/messages")
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
            if (guild.mention == id) {
                _currentGuild.value = guild

                if (guild.channels.isNotEmpty()) {
                    selectChannel(guild.channels[0].mention)
                }

                break
            }
        }
    }

    fun selectChannel(id: String) {
        val channel =
            _currentGuild.value?.channels?.find { it.mention == id }?.toChannel() ?:
            privateChannels.find { it.mention == id }?.toChannel()

        channel?.let {
            _currentChannel.value = it
            loadMessages(channelId = it.mention, replace = true)
            subscribeForChannelMembersRange()
        }
    }

    fun loadMessages(channelId: String, replace: Boolean = false) {
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

                    val shootMessages: MutableList<PublicMessage> = gson.fromJson(
                        response.body!!.string(),
                        object : TypeToken<MutableList<PublicMessage>>() {}.type
                    )

                    viewModelScope.launch {
                        messages.apply {
                            if (replace) {
                                clear()
                            }

                            shootMessages.forEach { this += it.toMessage() }
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
                .url("${instance}/guild/${_currentGuild.value?.mention}/channel")
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
                .url("${instance}/guild/${_currentGuild.value?.mention}")
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
                    logger.error("Failed to send request to delete message", e)
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

    fun createDMChannel(user: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = JsonObject()
            data.addProperty("name", name)

            val request = Request.Builder()
                .url("${instance}/users/${user}/channels")
                .header("Authorization", "Bearer $token")
                .post(data.toRequestBody())
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request to create DM channel $user", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to create DM channel: {}", response.body!!.string())

                        return
                    }

                    println(response.body!!.string())
                }
            })
        }

    }

    fun addFriend(user: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = JsonObject()
            data.addProperty("type", "pending")

            val request = Request.Builder()
                .url("${instance}/users/${user}/relationship")
                .header("Authorization", "Bearer $token")
                .post(data.toRequestBody())
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request to add friend for $user", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to add friend: {}", response.body!!.string())
                    }

                    response.closeQuietly()
                }
            })
        }
    }

    fun acceptRelationship(user: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${instance}/users/${user}/relationship")
                .header("Authorization", "Bearer $token")
                .post(FormBody.Builder().build())
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request to accept relationship for $user", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to accept relationship: {}", response.body!!.string())
                    }

                    response.closeQuietly()
                }
            })
        }
    }

    fun removeRelationship(user: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${instance}/users/${user}/relationship")
                .header("Authorization", "Bearer $token")
                .delete()
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Failed to send request to remove relationship for $user", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Failed to remove relationship: {}", response.body!!.string())
                    }

                    response.closeQuietly()
                }
            })
        }
    }
}
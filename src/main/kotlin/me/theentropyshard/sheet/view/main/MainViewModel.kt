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
import me.theentropyshard.sheet.Sheet.webSocket
import me.theentropyshard.sheet.api.model.PublicGuild
import me.theentropyshard.sheet.api.model.PublicGuildTextChannel
import me.theentropyshard.sheet.api.model.PublicMessage
import me.theentropyshard.sheet.toRequestBody
import okhttp3.*
import java.io.IOException
import java.util.*

class MainViewModel : ViewModel() {
    private val _isLoadingInitial = MutableStateFlow(true)
    val isLoadingInitial = _isLoadingInitial.asStateFlow()

    private val _guilds: MutableStateFlow<List<PublicGuild>> = MutableStateFlow(listOf())
    val guilds = _guilds.asStateFlow()

    private val _currentGuild: MutableStateFlow<PublicGuild?> = MutableStateFlow(null)
    val currentGuild = _currentGuild.asStateFlow()

    private val _currentChannel: MutableStateFlow<PublicGuildTextChannel?> = MutableStateFlow(null)
    val currentChannel = _currentChannel.asStateFlow()

    private val _messages: MutableStateFlow<List<PublicMessage>> = MutableStateFlow(listOf())
    val messages = _messages.asStateFlow()

    private val _members: MutableStateFlow<List<JsonObject>> = MutableStateFlow(listOf())
    val members = _members.asStateFlow()

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
                sequence++

                val message = gson.fromJson(text, JsonObject::class.java)

                when (message["t"].asString) {
                    "READY" -> {
                        startHeartbeat()
                    }

                    "MESSAGE_CREATE" -> {
                        val shootMessage =
                            gson.fromJson(message["d"].asJsonObject["message"], PublicMessage::class.java)

                        _messages.update { list -> listOf(shootMessage) + list }
                    }

                    "RELATIONSHIP_CREATE" -> {

                    }

                    "GUILD_CREATE" -> {

                    }

                    "CHANNEL_CREATE" -> {
                        val channel =
                            gson.fromJson(message["d"].asJsonObject["channel"], PublicGuildTextChannel::class.java)

                        _currentGuild.value!!.channels.add(channel)
                        _currentChannel.update { channel }
                    }

                    "MEMBERS_CHUNK" -> {
                        val array = message["d"].asJsonObject["items"].asJsonArray

                        _members.update { array.filter { it.isJsonObject }.map { it.asJsonObject } }
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("socket was closed: $code")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
            }
        }

        webSocket = httpClient.newWebSocket(request, listener)
    }

    fun subscribeForChannelMembersRange(min: Int = 0, max: Int = 100) {
        viewModelScope.launch(Dispatchers.IO) {
            webSocket.send("{ \"t\": \"members\", \"channel_id\": \"${_currentChannel.value?.completeId()}\", " +
                    "\"range\": [$min, $max] }")
        }
    }

    fun loadGuilds() {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${instance}/users/@me/guild")
                .header("Authorization", "Bearer $token")
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val type = object : TypeToken<List<PublicGuild>>() {}.type
                    val guilds = gson.fromJson<List<PublicGuild>>(response.body!!.string(), type)

                    _guilds.value = guilds
                    _currentGuild.value = guilds[0]
                    _currentChannel.value = _currentGuild.value!!.channels[0]

                    _isLoadingInitial.value = false
                }
            })
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
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {  }
                }
            })
        }
    }

    fun selectGuild(id: String) {
        for (guild in _guilds.value) {
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
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val messages: MutableList<PublicMessage> = gson.fromJson(
                        response.body!!.string(),
                        object : TypeToken<MutableList<PublicMessage>>() {}.type
                    )

                    viewModelScope.launch {
                        _messages.update { list -> if (add) messages + list else messages }
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
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {  }
                }
            })
        }
    }
}
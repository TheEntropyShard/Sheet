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

package me.theentropyshard.sheet.view.guild.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.theentropyshard.sheet.Sheet.httpClient
import me.theentropyshard.sheet.Sheet.instance
import me.theentropyshard.sheet.Sheet.token
import me.theentropyshard.sheet.toRequestBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class JoinOrCreateGuildViewModel : ViewModel() {
    enum class State {
        Initial,
        Loading,
        Ready,
        Error
    }

    private val _state = MutableStateFlow(State.Initial)
    val state = _state.asStateFlow()

    fun tabChanged() {

    }

    fun reset() {
        _state.value = State.Initial
    }

    fun joinGuild(inviteCode: String) {
        _state.value = State.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${instance}/invite/$inviteCode")
                .header("Authorization", "Bearer $token")
                .post("{}".toRequestBody())
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()

                    _state.value = State.Error
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { }

                    _state.value = if (response.isSuccessful) State.Ready else State.Error
                }
            })
        }
    }

    fun createGuild(name: String) {
        _state.value = State.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val data = JsonObject()
            data.addProperty("name", name)

            val request = Request.Builder()
                .url("${instance}/guild")
                .header("Authorization", "Bearer $token")
                .post(data.toRequestBody())
                .build()

            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()

                    _state.value = State.Error
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { }

                    _state.value = if (response.isSuccessful) State.Ready else State.Error
                }
            })
        }
    }
}
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

package me.theentropyshard.sheet.view.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.theentropyshard.sheet.Sheet
import me.theentropyshard.sheet.Sheet.gson
import me.theentropyshard.sheet.fromJson
import me.theentropyshard.sheet.toRequestBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.apache.logging.log4j.LogManager
import java.io.IOException

enum class RegisterStage {
    EnterInstance,
    EnterInvite,
    EnterCredentials
}

class RegisterViewModel : ViewModel() {
    private val logger = LogManager.getLogger()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered = _isRegistered.asStateFlow()

    private val _state = MutableStateFlow(RegisterStage.EnterInstance)
    val state = _state.asStateFlow()

    private lateinit var instance: String
    private lateinit var invite: String

    fun onInstanceEnter(instance: String) {
        _isLoading.value = true

        this.instance = instance

        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$instance/.well-known/nodeinfo/2.0")
                .build()

            Sheet.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Error response", e)

                    _isLoading.value = false

                    _isError.value = true
                }

                override fun onResponse(call: Call, response: Response) {
                    _isLoading.value = false

                    if (response.isSuccessful) {
                        val obj = gson.fromJson(response.body!!.string(), JsonObject::class)

                        if (obj["openRegistrations"].asBoolean) {
                            _state.value = RegisterStage.EnterCredentials
                        } else {
                            _state.value = RegisterStage.EnterInvite
                        }
                    } else {
                        _isError.value = true
                    }
                }
            })
        }
    }

    fun onInviteCodeEnter(invite: String) {
        this.invite = invite

        _state.value = RegisterStage.EnterCredentials
    }

    fun register(username: String, password: String) {
        _isLoading.value = true

        val body = JsonObject().apply {
            addProperty("username", username)
            addProperty("password", password)
            if (::invite.isInitialized) {
                addProperty("invite", invite)
            }
        }

        val request = Request.Builder()
            .url("$instance/auth/register")
            .post(body.toRequestBody())
            .build()

        Sheet.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error("Error response", e)

                _isLoading.value = false

                _isError.value = true
            }

            override fun onResponse(call: Call, response: Response) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    val obj = gson.fromJson(response.body!!.string(), JsonObject::class)

                    Sheet.token = obj["token"].asString

                    _isRegistered.value = true
                } else {
                    _isError.value = true
                }
            }
        })
    }
}
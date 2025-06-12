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

package me.theentropyshard.sheet.view.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.theentropyshard.sheet.Sheet
import me.theentropyshard.sheet.api.login.LoginRequest
import me.theentropyshard.sheet.api.login.LoginResponse
import me.theentropyshard.sheet.toRequestBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.apache.logging.log4j.LogManager
import java.io.IOException

class LoginViewModel : ViewModel() {
    private val logger = LogManager.getLogger()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    fun login(instance: String, username: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val body = LoginRequest(username, password).toRequestBody()

            val request = Request.Builder()
                .url("$instance/auth/login")
                .post(body)
                .build()

            Sheet.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("Error response", e)

                    _isLoading.value = false

                    _isError.value = true
                }

                override fun onResponse(call: Call, response: Response) {
                    _isLoading.value = false

                    val loginResponse = Sheet.gson.fromJson(response.body!!.string(), LoginResponse::class.java)
                    Sheet.token = loginResponse.token
                    Sheet.user = loginResponse.user



                    _isLoggedIn.value = true
                }
            })
        }
    }
}
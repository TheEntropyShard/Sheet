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

package me.theentropyshard.sheet

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import me.theentropyshard.sheet.api.model.PublicMessage
import me.theentropyshard.sheet.api.model.PublicUser
import me.theentropyshard.sheet.view.login.LoginView
import me.theentropyshard.sheet.view.login.LoginViewModel
import me.theentropyshard.sheet.view.main.MainView
import me.theentropyshard.sheet.view.main.MainViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object Sheet {
    val httpClient = OkHttpClient()
    val gson: Gson = GsonBuilder().disableJdkUnsafe().create()

    lateinit var instance: String
    lateinit var token: String
    lateinit var user: PublicUser

    lateinit var webSocket: WebSocket

    fun closeWebSocket() {
        webSocket.close(1000, null)
    }
}

fun Any.toRequestBody(): RequestBody {
    return Sheet.gson.toJson(this).toRequestBody("application/json; charset=utf-8".toMediaType())
}

@Composable
private fun Sheet() {
    val navController = rememberNavController()

    val envInstance = System.getenv("SHOOT_INSTANCE") ?: ""
    val envUsername = System.getenv("SHOOT_USERNAME") ?: ""
    val envPassword = System.getenv("SHOOT_PASSWORD") ?: ""

    var instance by rememberSaveable(TextFieldValue.Saver) { mutableStateOf(TextFieldValue(envInstance)) }
    var username by rememberSaveable(TextFieldValue.Saver) { mutableStateOf(TextFieldValue(envUsername)) }
    var password by rememberSaveable(TextFieldValue.Saver) { mutableStateOf(TextFieldValue(envPassword)) }

    val loginViewModel: LoginViewModel = viewModel()
    val isLoggedIn by loginViewModel.isLoggedIn.collectAsState()

    val mainViewModel: MainViewModel = viewModel()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            mainViewModel.loggedIn()
            navController.navigate("main")
        }
    }

    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
            composable("login") {
                LoginView(
                    model = loginViewModel,
                    instanceTextFieldValue = instance,
                    onInstanceTextFieldValue = { instance = it },
                    usernameTextFieldValue = username,
                    onUsernameTextFieldValue = { username = it },
                    passwordTextFieldValue = password,
                    onPasswordTextFieldValue = { password = it },
                ) {
                    Sheet.instance = instance.text
                    loginViewModel.login(instance.text, username.text, password.text)
                }
            }

            composable("main") {
                MainView(model = mainViewModel)
            }
        }
    }
}

fun main() = application {
    val state = rememberWindowState(size = DpSize(1280.dp, 720.dp))

    Window(
        state = state,
        onCloseRequest = ::exitApplication
    ) {
        Sheet()
    }
}

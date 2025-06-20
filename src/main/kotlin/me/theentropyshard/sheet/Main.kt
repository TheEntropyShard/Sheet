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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import me.theentropyshard.sheet.api.model.PrivateRelationship
import me.theentropyshard.sheet.api.model.PublicUser
import me.theentropyshard.sheet.api.model.RelationshipType
import me.theentropyshard.sheet.view.login.LoginView
import me.theentropyshard.sheet.view.login.LoginViewModel
import me.theentropyshard.sheet.view.main.MainView
import me.theentropyshard.sheet.view.main.MainViewModel
import me.theentropyshard.sheet.view.theme.SheetTheme
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlin.reflect.KClass

object Sheet {
    val httpClient = OkHttpClient()
    val gson: Gson = GsonBuilder()
        .disableJdkUnsafe()
        .registerTypeAdapter(RelationshipType::class.java, RelationshipType.Adapter())
        .create()

    lateinit var instance: String
    lateinit var token: String
    lateinit var user: PublicUser

    lateinit var webSocket: WebSocket

    fun closeWebSocket() {
        if (this::webSocket.isInitialized) {
            webSocket.close(1000, null)
        }
    }
}

fun <T : Any> Gson.fromJson(json: JsonElement?, klass: KClass<T>): T {
    return this.fromJson(json, klass.java)
}

fun <T : Any> Gson.fromJson(json: String?, klass: KClass<T>): T {
    return this.fromJson(json, klass.java)
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

    SheetTheme { darkTheme ->
        Surface(
            color = if (darkTheme) Color.Black else Color.White,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
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
}

lateinit var parent: Frame

fun main() = application {
    val state = rememberWindowState(size = DpSize(1280.dp, 720.dp))

    Window(
        state = state,
        title = "Sheet",
        onCloseRequest = {
            Sheet.closeWebSocket()
            exitApplication()
        }
    ) {
        parent = window

        Sheet()
    }
}

@Composable
fun FileDialog(onCloseRequest: (result: List<File>) -> Unit) {
    AwtWindow(
        create = {
            object : FileDialog(parent, "Choose a file", LOAD) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)

                    if (value) {
                        onCloseRequest(files.map { file -> file.absoluteFile })
                    }
                }
            }.apply {
                isMultipleMode = true
            }
        },
        dispose = FileDialog::dispose
    )
}
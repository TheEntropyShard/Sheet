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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
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
import me.theentropyshard.sheet.api.model.PublicUser
import me.theentropyshard.sheet.api.model.RelationshipType
import me.theentropyshard.sheet.view.login.LoginView
import me.theentropyshard.sheet.view.login.LoginViewModel
import me.theentropyshard.sheet.view.main.MainView
import me.theentropyshard.sheet.view.main.MainViewModel
import me.theentropyshard.sheet.view.register.RegisterView
import me.theentropyshard.sheet.view.register.RegisterViewModel
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

    val loginViewModel: LoginViewModel = viewModel()
    val isLoggedIn by loginViewModel.isLoggedIn.collectAsState()

    val registerViewModel: RegisterViewModel = viewModel()
    val isRegistered by registerViewModel.isRegistered.collectAsState()

    val mainViewModel: MainViewModel = viewModel()

    LaunchedEffect(isLoggedIn, isRegistered) {
        if (isLoggedIn || isRegistered) {
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
                        navigateToRegister = {
                            navController.navigate("register")
                        }
                    ) { instance, username, password ->
                        Sheet.instance = instance
                        loginViewModel.login(instance, username, password)
                    }
                }

                composable("register") {
                    RegisterView(
                        model = registerViewModel,
                        navigateToLogin = {
                            navController.navigate("login")
                        }
                    ) { instance, username, password ->
                        Sheet.instance = instance
                        registerViewModel.register(username, password)
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
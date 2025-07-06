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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginView(
    modifier: Modifier = Modifier,
    model: LoginViewModel,
    navigateToRegister: () -> Unit,
    onLogin: (String, String, String) -> Unit
) {
    val isLoading by model.isLoading.collectAsState()
    val isError by model.isError.collectAsState()

    val envInstance = System.getenv("SHOOT_INSTANCE") ?: ""
    val envUsername = System.getenv("SHOOT_USERNAME") ?: ""
    val envPassword = System.getenv("SHOOT_PASSWORD") ?: ""

    var instance by rememberSaveable(TextFieldValue.Saver) { mutableStateOf(TextFieldValue(envInstance)) }
    var username by rememberSaveable(TextFieldValue.Saver) { mutableStateOf(TextFieldValue(envUsername)) }
    var password by rememberSaveable(TextFieldValue.Saver) { mutableStateOf(TextFieldValue(envPassword)) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Login",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                Text(
                    text = buildAnnotatedString {
                        append("New here?")

                        append(" ")

                        withLink(
                            link = LinkAnnotation.Clickable(
                                tag = "register",
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ),
                                linkInteractionListener = {
                                    navigateToRegister()
                                }
                            )) {
                            append("Register")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = instance,
                    onValueChange = { instance = it },
                    singleLine = true,
                    isError = isError,
                    label = {
                        Text("Instance")
                    }
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    singleLine = true,
                    isError = isError,
                    label = {
                        Text("Username")
                    }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = isError,
                    label = {
                        Text("Password")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        if (!isLoading) {
                            onLogin(instance.text, username.text, password.text)
                        }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Login")
                    }
                }
            }
        }
    }
}
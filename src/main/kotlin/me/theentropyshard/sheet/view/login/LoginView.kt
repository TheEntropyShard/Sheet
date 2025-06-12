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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginView(
    modifier: Modifier = Modifier,
    model: LoginViewModel,
    instanceTextFieldValue: TextFieldValue,
    onInstanceTextFieldValue: (TextFieldValue) -> Unit,
    usernameTextFieldValue: TextFieldValue,
    onUsernameTextFieldValue: (TextFieldValue) -> Unit,
    passwordTextFieldValue: TextFieldValue,
    onPasswordTextFieldValue: (TextFieldValue) -> Unit,
    onLogin: () -> Unit
) {
    val isLoading by model.isLoading.collectAsState()
    val isError by model.isError.collectAsState()

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
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Login",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = instanceTextFieldValue,
                    onValueChange = onInstanceTextFieldValue,
                    singleLine = true,
                    isError = isError,
                    label = {
                        Text("Instance")
                    }
                )

                OutlinedTextField(
                    value = usernameTextFieldValue,
                    onValueChange = onUsernameTextFieldValue,
                    singleLine = true,
                    isError = isError,
                    label = {
                        Text("Username")
                    }
                )

                OutlinedTextField(
                    value = passwordTextFieldValue,
                    onValueChange = onPasswordTextFieldValue,
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
                            onLogin()
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
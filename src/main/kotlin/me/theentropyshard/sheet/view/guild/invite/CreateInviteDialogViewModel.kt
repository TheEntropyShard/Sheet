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

package me.theentropyshard.sheet.view.guild.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.theentropyshard.sheet.Sheet.gson
import me.theentropyshard.sheet.Sheet.httpClient
import me.theentropyshard.sheet.Sheet.instance
import me.theentropyshard.sheet.Sheet.token
import me.theentropyshard.sheet.fromJson
import me.theentropyshard.sheet.toRequestBody
import okhttp3.Request

class CreateInviteDialogViewModel : ViewModel() {
    private val _inviteCode = MutableStateFlow("")
    val inviteCode = _inviteCode.asStateFlow()

    fun createInviteCode(guildId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${instance}/guild/$guildId/invite")
                .header("Authorization", "Bearer $token")
                .post(JsonObject().toRequestBody())
                .build()

            httpClient.newCall(request).execute().use { response ->
                val obj = gson.fromJson(response.body!!.string(), JsonObject::class)
                _inviteCode.update { obj["code"].asString }
            }
        }
    }
}
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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.theentropyshard.sheet.view.chat.ChatView
import me.theentropyshard.sheet.view.guild.channel.ChannelList
import me.theentropyshard.sheet.view.guild.list.GuildList
import me.theentropyshard.sheet.view.guild.members.MemberList

@Composable
fun MainView(
    modifier: Modifier = Modifier,
    model: MainViewModel
) {
    val isLoadingInitial by model.isLoadingInitial.collectAsState()
    val guilds by model.guilds.collectAsState()
    val currentGuild by model.currentGuild.collectAsState()
    val currentChannel by model.currentChannel.collectAsState()
    val messages by model.messages.collectAsState()

    val scope = rememberCoroutineScope()
    val state = rememberLazyListState()

    LaunchedEffect(Unit) {
        model.loadGuilds()
    }

    LaunchedEffect(messages) {
        scope.launch {
            if (messages.isNotEmpty()) {
                state.animateScrollToItem(messages.size - 1)
            }
        }
    }

    if (isLoadingInitial) {
        CircularProgressIndicator()
    } else {
        Row(modifier = modifier) {
            Spacer(modifier = Modifier.width(16.dp))

            GuildList(
                modifier = Modifier.fillMaxHeight(),
                guilds = guilds,
                onMeClick = {

                },
                onAddGuildClick = {

                }
            ) {
                model.selectGuild(it)
            }

            Spacer(modifier = Modifier.width(16.dp))

            ChannelList(
                modifier = Modifier.fillMaxHeight().width(200.dp),
                channels = currentGuild!!.channels
            ) {
                model.selectChannel(it)
            }

            Spacer(modifier = Modifier.width(16.dp))

            ChatView(
                modifier = Modifier.fillMaxSize().weight(1f),
                state = state,
                messages = messages.filter { message -> message.channelId == currentChannel?.completeId() }.reversed()
            ) { message ->
                model.sendMessage(message)
            }

            Spacer(modifier = Modifier.width(16.dp))

            MemberList(
                modifier = Modifier.fillMaxHeight().width(200.dp),
                members = listOf("darkcat09", "doesnm", "theentropyshard")
            )

            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}
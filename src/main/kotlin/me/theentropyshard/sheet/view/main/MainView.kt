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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.awtClipboard
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.theentropyshard.sheet.FileDialog
import me.theentropyshard.sheet.api.model.PublicGuildTextChannel
import me.theentropyshard.sheet.view.chat.ChatView
import me.theentropyshard.sheet.view.chat.MessageContextMenuAction
import me.theentropyshard.sheet.view.chat.attachment.AttachmentDialog
import me.theentropyshard.sheet.view.dialog.ConfirmDialog
import me.theentropyshard.sheet.view.dialog.InputDialog
import me.theentropyshard.sheet.view.guild.channel.ChannelList
import me.theentropyshard.sheet.view.guild.channel.ChannelMenuItemAction
import me.theentropyshard.sheet.view.guild.channel.GuildMenuItemAction
import me.theentropyshard.sheet.view.guild.dialog.JoinOrCreateGuildDialog
import me.theentropyshard.sheet.view.guild.invite.CreateInviteDialog
import me.theentropyshard.sheet.view.guild.list.GuildList
import me.theentropyshard.sheet.view.guild.members.MemberList
import java.awt.datatransfer.StringSelection
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainView(
    modifier: Modifier = Modifier,
    model: MainViewModel
) {
    val isLoadingInitial by model.isLoadingInitial.collectAsState()
    val guilds by model.guilds.collectAsState()
    val currentGuild by model.currentGuild.collectAsState()
    val currentChannel by model.currentChannel.collectAsState()
    val channels by model.channels.collectAsState()
    val messages by model.messages.collectAsState()
    val members by model.members.collectAsState()

    val scope = rememberCoroutineScope()
    val state = rememberLazyListState()
    val clipboard = LocalClipboard.current

    var createChannelDialogVisible by remember { mutableStateOf(false) }
    var renameChannelDialogVisible by remember { mutableStateOf(false) }
    var createInviteDialogVisible by remember { mutableStateOf(false) }
    var deleteChannelDialogVisible by remember { mutableStateOf(false) }
    var createGuildDialogVisible by remember { mutableStateOf(false) }
    var deleteGuildDialogVisible by remember { mutableStateOf(false) }

    var channelForRename by remember { mutableStateOf<PublicGuildTextChannel?>(null) }
    var channelForDeletion by remember { mutableStateOf<PublicGuildTextChannel?>(null) }

    LaunchedEffect(messages) {
        scope.launch {
            if (messages.isNotEmpty()) {
                state.animateScrollToItem(messages.size - 1)
            }
        }
    }

    if (createChannelDialogVisible) {
        InputDialog(
            title = "Create a new channel",
            label = "Channel name",
            placeholder = "Enter channel name...",
            onDismissRequest = { createChannelDialogVisible = false }
        ) { name ->
            model.createChannel(name)
        }
    }

    if (renameChannelDialogVisible) {
        InputDialog(
            title = "Rename a channel",
            label = "Channel name",
            placeholder = "Enter a new name...",
            initial = channelForRename!!.name,
            onDismissRequest = { renameChannelDialogVisible = false }
        ) { name ->
            model.renameChannel(channelForRename!!.completeId(), name)
            channelForDeletion = null
        }
    }

    if (createInviteDialogVisible) {
        CreateInviteDialog(guildId = currentGuild!!.completeId()) {
            createInviteDialogVisible = false
        }
    }

    if (deleteChannelDialogVisible) {
        ConfirmDialog(
            title = "Delete channel",
            text = "Are you sure you want to delete channel «${channelForDeletion!!.name}»?"
        ) { yes ->
            if (yes) {
                model.deleteChannel(channelForDeletion!!.completeId())
                channelForDeletion = null
            }

            deleteChannelDialogVisible = false
        }
    }

    if (createGuildDialogVisible) {
        JoinOrCreateGuildDialog { createGuildDialogVisible = false }
    }

    if (deleteGuildDialogVisible) {
        ConfirmDialog(
            title = "Delete guild",
            text = "Are you sure you want to delete guild «${currentGuild?.name}»?"
        ) { yes ->
            if (yes) {
                model.deleteGuild()
            }

            deleteGuildDialogVisible = false
        }
    }

    if (isLoadingInitial) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Row(modifier = modifier) {
            Spacer(modifier = Modifier.width(16.dp))

            GuildList(
                modifier = Modifier.fillMaxHeight(),
                guilds = guilds,
                isGuildSelected = { it.id == currentGuild?.id },
                onMeClick = {

                },
                onAddGuildClick = {
                    createGuildDialogVisible = true
                }
            ) {
                model.selectGuild(it)
            }

            Spacer(modifier = Modifier.width(16.dp))

            key(channels) {
                if (currentGuild != null) {
                    ChannelList(
                        modifier = Modifier.fillMaxHeight().width(200.dp),
                        channels = channels.filter { channel -> channel.guildId == currentGuild?.completeId() },
                        guildName = currentGuild!!.name,
                        isChannelSelected = { channel ->
                            channel.id == currentChannel?.id
                        },
                        onGuildMenuItemClick = { action ->
                            when (action) {
                                GuildMenuItemAction.CreateChannel -> createChannelDialogVisible = true
                                GuildMenuItemAction.CreateInvite -> createInviteDialogVisible = true
                                GuildMenuItemAction.DeleteGuild -> deleteGuildDialogVisible = true
                            }
                        },
                        onChannelMenuItemClick = { action, channel ->
                            when (action) {
                                ChannelMenuItemAction.Rename -> {
                                    channelForRename = channel
                                    renameChannelDialogVisible = true
                                }

                                ChannelMenuItemAction.Delete -> {
                                    channelForDeletion = channel
                                    deleteChannelDialogVisible = true
                                }
                            }
                        },
                    ) {
                        model.selectChannel(it)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            var isFileChooserOpen by remember { mutableStateOf(false) }
            var isAttachmentOpen by remember { mutableStateOf(false) }
            var selectedFiles by remember { mutableStateOf(listOf<File>()) }

            key(currentChannel) {
                ChatView(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    state = state,
                    messages = messages.filter { message -> message.channelId == currentChannel?.completeId() }
                        .reversed(),
                    onAddAttachmentClick = {
                        isFileChooserOpen = true
                    },
                    onContextMenuAction = { action, message ->
                        when (action) {
                            MessageContextMenuAction.Edit -> {}
                            MessageContextMenuAction.Forward -> {}
                            MessageContextMenuAction.CopyText -> {
                                clipboard.awtClipboard!!.setContents(
                                    StringSelection(message.content), null
                                )
                            }
                            MessageContextMenuAction.Delete -> {
                                model.deleteMessage(currentChannel!!.completeId(), message.id)
                            }
                        }
                    }
                ) { message ->
                    model.sendMessage(message)
                }
            }

            if (isFileChooserOpen) {
                FileDialog { files ->
                    isFileChooserOpen = false
                    isAttachmentOpen = true
                    selectedFiles = files
                }
            }

            if (isAttachmentOpen) {
                AttachmentDialog(currentChannel!!.completeId(), selectedFiles = selectedFiles) {
                    isAttachmentOpen = false
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            MemberList(
                modifier = Modifier.fillMaxHeight().width(200.dp),
                members = members.map { obj -> obj["name"].asString }
            )

            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}
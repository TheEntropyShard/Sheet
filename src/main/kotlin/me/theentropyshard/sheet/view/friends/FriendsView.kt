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

package me.theentropyshard.sheet.view.friends

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.api.model.RelationshipType
import me.theentropyshard.sheet.view.main.MainViewModel

enum class ShowingFriends(val text: String) {
    All("All"),
    Online("Online"),
    Friends("Friends"),
    Pending("Pending"),
    Blocked("Blocked"),
    AddFriend("Add a friend")
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FriendsView(
    modifier: Modifier = Modifier,
    model: MainViewModel
) {
    val relationships = model.relationships
    /*val relationships = listOf(
        PrivateRelationship().apply {
            type = RelationshipType.PENDING
            user = PublicUser().apply {
                displayName = "John"
            }
        },
        PrivateRelationship().apply {
            type = RelationshipType.ACCEPTED
            user = PublicUser().apply {
                displayName = "Alex"
            }
        },
        PrivateRelationship().apply {
            type = RelationshipType.BLOCKED
            user = PublicUser().apply {
                displayName = "Peter"
            }
        }
    )*/

    var showingFriends by remember { mutableStateOf(ShowingFriends.Online) }
    var searchValue by remember { mutableStateOf("") }
    var addFriendText by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Friends")

            for (friends in ShowingFriends.entries) {
                FilterChip(
                    selected = showingFriends == friends,
                    onClick = { showingFriends = friends },
                    label = {
                        Text(friends.text)
                    }
                )
            }
        }

        if (showingFriends == ShowingFriends.AddFriend) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BaseTextField(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    value = addFriendText,
                    onValueChange = { addFriendText = it },
                    placeholder = { Text(text = "peter@example.com") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    model.addFriend(addFriendText)
                }) {
                    Text(text = "Add")
                }
            }
        } else {
            BaseTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                value = searchValue,
                onValueChange = { searchValue = it },
                placeholder = { Text(text = "Search") },
                trailingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search") }
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (showingFriends) {
                    ShowingFriends.All, ShowingFriends.Online -> {
                        val pendingRelationships = relationships.filter { it.type == RelationshipType.PENDING }
                        val acceptedRelationships = relationships.filter { it.type == RelationshipType.ACCEPTED }
                        val blockedRelationships = relationships.filter { it.type == RelationshipType.BLOCKED }

                        if (pendingRelationships.isNotEmpty()) {
                            item {
                                Text("Pending - ${pendingRelationships.size}")
                            }

                            items(pendingRelationships) {
                                FriendItem(relationship = it, model = model)
                            }
                        }

                        if (acceptedRelationships.isNotEmpty()) {
                            item {
                                Text(
                                    modifier = if (pendingRelationships.isNotEmpty()) {
                                        Modifier.padding(top = 8.dp)
                                    } else {
                                        Modifier
                                    },
                                    text = "Accepted - ${acceptedRelationships.size}"
                                )
                            }

                            items(acceptedRelationships) {
                                FriendItem(relationship = it, model = model)
                            }
                        }

                        if (blockedRelationships.isNotEmpty()) {
                            item {
                                Text(
                                    modifier = if (pendingRelationships.isNotEmpty() || acceptedRelationships.isNotEmpty()) {
                                        Modifier.padding(top = 8.dp)
                                    } else {
                                        Modifier
                                    },
                                    text = "Blocked - ${blockedRelationships.size}"
                                )
                            }

                            items(blockedRelationships) {
                                FriendItem(relationship = it, model = model)
                            }
                        }
                    }

                    else -> {
                        items(relationships.filter {
                            when (showingFriends) {
                                ShowingFriends.Pending -> it.type == RelationshipType.PENDING
                                ShowingFriends.Friends -> it.type == RelationshipType.ACCEPTED
                                ShowingFriends.Blocked -> it.type == RelationshipType.BLOCKED

                                else -> true
                            }
                        }) {
                            FriendItem(relationship = it, model = model)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BaseTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable RowScope.() -> Unit = {},
    trailingIcon: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                modifier = Modifier.fillMaxWidth(1f),
                value = value,
                onValueChange = onValueChange,
                maxLines = 1
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(
                    visible = value.isEmpty(),
                    enter = fadeIn(tween(100)) + slideInHorizontally { it },
                    exit = fadeOut(tween(100)) + slideOutHorizontally { it }
                ) {
                    placeholder()
                }
            }
        }

        trailingIcon()
    }
}
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.api.model.PrivateRelationship
import me.theentropyshard.sheet.api.model.RelationshipType
import me.theentropyshard.sheet.view.main.MainViewModel

@Composable
fun FriendItem(
    modifier: Modifier = Modifier,
    relationship: PrivateRelationship,
    model: MainViewModel
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .height(52.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = relationship.user.displayName
            )

            Spacer(modifier = Modifier.weight(1f))

            when (relationship.type) {
                RelationshipType.PENDING -> {
                    TextButton(
                        onClick = {
                            model.acceptRelationship(relationship.user.mention)
                        }
                    ) {
                        Text(text = "Accept")
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    TextButton(
                        onClick = {
                            model.removeRelationship(relationship.user.mention)
                        }
                    ) {
                        Text(
                            text = "Decline",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                RelationshipType.ACCEPTED -> {
                    TextButton(
                        onClick = {
                            model.createDMChannel(
                                relationship.user.mention,
                                relationship.user.displayName ?: relationship.user.mention
                            )
                        }
                    ) {
                        Text(text = "Message")
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    TextButton(
                        onClick = {
                            model.removeRelationship(relationship.user.mention)
                        }
                    ) {
                        Text(
                            text = "Remove",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                RelationshipType.BLOCKED -> {
                    TextButton(
                        onClick = {
                            model.removeRelationship(relationship.user.mention)
                        }
                    ) {
                        Text(
                            text = "Remove",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
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

package me.theentropyshard.sheet.view.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.model.Message

fun <E> List<E>.safeIdx(idx: Int): E {
    return this[idx.coerceIn(0, this.size - 1)]
}

@Composable
fun ChatView(
    modifier: Modifier = Modifier,
    state: LazyListState,
    messages: List<Message>,
    onAddAttachmentClick: () -> Unit,
    onContextMenuAction: (MessageContextMenuAction, Message) -> Unit,
    onSendMessage: (String) -> Unit
) {
    Column(modifier = modifier) {
        SelectionContainer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state
            ) {
                itemsIndexed(items = messages, key = { _, m -> m.id }) { index, message ->
                    var sameAuthorPrev = messages.safeIdx(index - 1).authorId == message.authorId

                    if (index - 1 < 0) {
                        sameAuthorPrev = false
                    }

                    var sameAuthorNext = messages.safeIdx(index + 1).authorId == message.authorId

                    if (index + 1 >= messages.size) {
                        sameAuthorNext = false
                    }

                    if (index != 0) {
                        Spacer(modifier = modifier.height(if (sameAuthorPrev) 0.dp else 16.dp))
                    }

                    ChatMessage(
                        modifier = Modifier.fillMaxWidth(),
                        message = message,
                        sameAuthorPrev = sameAuthorPrev,
                        sameAuthorNext = sameAuthorNext,
                        onContextMenuAction = onContextMenuAction
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ChatInput(
            modifier = Modifier.fillMaxWidth(),
            onAddAttachmentClick = onAddAttachmentClick
        ) {
            onSendMessage(it)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
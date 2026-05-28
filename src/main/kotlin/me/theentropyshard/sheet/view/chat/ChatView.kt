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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.model.Message
import kotlin.math.abs

fun <E> List<E>.safeIdx(idx: Int): E {
    return this[idx.coerceIn(0, this.size - 1)]
}

@Composable
fun ChatView(
    modifier: Modifier = Modifier,
    chatName: String,
    state: LazyListState,
    messages: List<Message>,
    onAddAttachmentClick: () -> Unit,
    onContextMenuAction: (MessageContextMenuAction, Message) -> Unit,
    onSendMessage: (String) -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        ChatViewHeader(
            modifier = Modifier.fillMaxWidth(),
            chatName = chatName
        )

        SelectionContainer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                reverseLayout = true,
            ) {
                itemsIndexed(items = messages, key = { _, m -> m.id }) { index, message ->
                    var sameAuthorNext = messages.safeIdx(index - 1).authorId == message.authorId

                    if (index - 1 < 0) {
                        sameAuthorNext = false
                    }

                    var sameAuthorPrev = messages.safeIdx(index + 1).authorId == message.authorId

                    if (index + 1 >= messages.size) {
                        sameAuthorPrev = false
                    }

                    if (index != 0) {
                        Spacer(modifier = modifier.height(if (sameAuthorNext) 0.dp else 16.dp))
                    }

                    if (abs(messages.safeIdx(index + 1).published.minute - message.published.minute) >= 5) {
                        sameAuthorPrev = false
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
    }
}

fun LazyListState.isAtBottom(): Boolean {
    return if (layoutInfo.totalItemsCount == 0) {
        false
    } else {
        val firstVisibleItem = layoutInfo.visibleItemsInfo.first()
        val viewportHeight = layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset

        // Check if the first visible item is the first item in the list and fully visible
        // This indicates that the user has scrolled to the bottom

        firstVisibleItem.index == 0 && firstVisibleItem.offset + firstVisibleItem.size <= viewportHeight
    }
}

fun LazyListState.reachedTop(): Boolean {
    return if (layoutInfo.totalItemsCount == 0) {
        false
    } else {
        val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
        val viewportHeight = layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset

        // Check if the last visible item is the last item in the list and fully visible
        // This indicates that the user has scrolled to the top
        (lastVisibleItem.index + 1 == layoutInfo.totalItemsCount &&
                lastVisibleItem.offset - lastVisibleItem.size <= viewportHeight)
    }
}
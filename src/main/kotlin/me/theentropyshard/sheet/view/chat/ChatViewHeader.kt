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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.generated.resources.Res
import me.theentropyshard.sheet.generated.resources.group_24dp
import me.theentropyshard.sheet.view.Fonts
import org.jetbrains.compose.resources.painterResource

@Composable
fun ChatViewHeader(
    modifier: Modifier = Modifier,
    chatName: String
) {
    CompositionLocalProvider(LocalContentColor provides contentColorFor(MaterialTheme.colorScheme.secondaryContainer)) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = chatName,
                fontFamily = Fonts.googleSans()
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(Res.drawable.group_24dp),
                    contentDescription = ""
                )
            }
        }
    }
}
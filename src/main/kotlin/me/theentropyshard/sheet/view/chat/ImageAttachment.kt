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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import me.theentropyshard.sheet.utils.PlatformImage
import me.theentropyshard.sheet.view.Fonts
import kotlin.math.max
import kotlin.math.min

@Composable
fun ImageAttachment(
    modifier: Modifier = Modifier,
    name: String,
    width: Int,
    height: Int,
    url: String,
    onClick: () -> Unit = {},
) {
    val aspectRatio = width.toFloat() / height.toFloat()

    val desiredWidth = if (aspectRatio > 1.0f) 640 else 360
    val desiredHeight = if (aspectRatio > 1.0f) 360 else 640

    val scaleX = desiredWidth.toFloat() / width.toFloat()
    val scaleY = desiredHeight.toFloat() / height.toFloat()
    var scale = min(scaleX, scaleY)

    var newWidth = (width * scale).dp
    var newHeight = (height * scale).dp

    var contentScale = ContentScale.Crop

    if (
        width.dp < 640.dp &&
        height.dp < 360.dp &&
        aspectRatio > 1.0f
    ) {
        newWidth = width.dp
        newHeight = height.dp
        scale = 1.0f
        contentScale = ContentScale.Fit
    } else if (
        width.dp < 360.dp &&
        height.dp < 640.dp &&
        aspectRatio < 1.0f
    ) {
        newHeight = width.dp
        newWidth = height.dp
        scale = 1.0f
        contentScale = ContentScale.Fit
    }

    Surface(
        modifier = modifier
            .size(newWidth, newHeight)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        PlatformImage(
            modifier = Modifier.scale(max(scale, 1.0f)),
            model = url,
            contentDescription = name,
            contentScale = contentScale,
            onError = {
                val message = it?.message

                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Could not load image: $message",
                        fontFamily = Fonts.googleSans()
                    )
                }
            }
        )
    }
}
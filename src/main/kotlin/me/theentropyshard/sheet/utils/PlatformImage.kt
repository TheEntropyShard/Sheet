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

package me.theentropyshard.sheet.utils

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastRoundToInt
import coil3.BitmapImage
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.asPainter
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.toBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.FilterMode
import org.jetbrains.skia.MipmapMode
import org.jetbrains.skia.Image as SkiaImage

// Copied from https://github.com/coil-kt/coil/issues/2883#issuecomment-3726278317 and added ContentScale

@Composable
fun PlatformImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onError: @Composable (t: Throwable?) -> Unit = {},
) {
    val context = LocalPlatformContext.current

    var imageResult by remember { mutableStateOf<ImageResult?>(null) }
    var imagePainter by remember { mutableStateOf<Painter?>(null) }

    LaunchedEffect(model) {
        val request = ImageRequest.Builder(context).data(model).build()
        val result = SingletonImageLoader.get(context).execute(request)
        imageResult = result

        imagePainter = when (val image = result.image) {
            is BitmapImage -> ScaledBitmapPainter(image)
            else -> image?.asPainter(context)
        }
    }

    when (imageResult) {
        is SuccessResult -> {
            Image(
                imagePainter ?: ColorPainter(Color.Unspecified),
                contentDescription,
                modifier,
                contentScale = contentScale
            )
        }

        is ErrorResult -> onError((imageResult as ErrorResult).throwable)

        null -> onError(null)
    }
}


class ScaledBitmapPainter(
    val image: coil3.Image,
    val filterQuality: FilterQuality = FilterQuality.Low
) : Painter() {
    override val intrinsicSize: Size
        get() = Size(image.width.toFloat(), image.height.toFloat())

    override fun DrawScope.onDraw() {
        val size = IntSize(
            size.width.fastRoundToInt(),
            size.height.fastRoundToInt(),
        )
        val bitmap = SkiaImage.makeFromBitmap(
            image.toBitmap().asComposeImageBitmap().asSkiaBitmap()
        ).scale(size.width, size.height)

        drawImage(
            bitmap,
            IntOffset.Zero,
            size,
            dstSize = size,
            alpha = 1.0f,
            colorFilter = null,
            filterQuality = filterQuality,
        )
    }

    fun SkiaImage.scale(width: Int, height: Int): ImageBitmap {
        val bitmap = Bitmap()
        bitmap.allocN32Pixels(width, height)
        scalePixels(bitmap.peekPixels()!!, FilterMipmap(FilterMode.LINEAR, MipmapMode.LINEAR), false)
        return SkiaImage.makeFromBitmap(bitmap).toComposeImageBitmap()
    }
}
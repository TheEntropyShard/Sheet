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

package me.theentropyshard.sheet.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import me.theentropyshard.sheet.generated.resources.*
import org.jetbrains.compose.resources.Font

object Fonts {
    private lateinit var googleSansFamily: FontFamily

    // @formatter:off

    @Composable
    fun googleSans(): FontFamily {
        if (!Fonts::googleSansFamily.isInitialized) {
            googleSansFamily = FontFamily(
                Font(Res.font.googlesans_regular,         FontWeight.Normal,   FontStyle.Normal),
                Font(Res.font.googlesans_italic,          FontWeight.Normal,   FontStyle.Italic),

                Font(Res.font.googlesans_medium,          FontWeight.Medium,   FontStyle.Normal),
                Font(Res.font.googlesans_medium_italic,   FontWeight.Medium,   FontStyle.Italic),

                Font(Res.font.googlesans_semibold,        FontWeight.SemiBold, FontStyle.Normal),
                Font(Res.font.googlesans_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),

                Font(Res.font.googlesans_bold,            FontWeight.Bold,     FontStyle.Normal),
                Font(Res.font.googlesans_bold_italic,     FontWeight.Bold,     FontStyle.Italic),
            )
        }

        return googleSansFamily
    }

    // @formatter:on
}
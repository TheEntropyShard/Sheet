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

package me.theentropyshard.sheet.view.chat.attachment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import me.theentropyshard.sheet.FileDialog
import java.io.File

@Composable
fun AttachmentDialog(
    channelId: String,
    selectedFiles: List<File>,
    onDismissRequest: () -> Unit
) {
    val model = viewModel { AttachmentDialogViewModel() }
    val files by model.files.collectAsState()
    val count by model.count.collectAsState()

    LaunchedEffect(selectedFiles) {
        model.setFiles(selectedFiles)
    }

    var caption by remember { mutableStateOf("") }
    var isFileChooserOpen by remember { mutableStateOf(false) }

    if (isFileChooserOpen) {
        FileDialog { files ->
            isFileChooserOpen = false
            model.addFiles(files)
        }
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp).align(Alignment.Start),
                    text = "$count ${if (count % 10 == 1) "file" else "files"} selected",
                    fontSize = 22.sp
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files) {
                        AttachmentItem(
                            modifier = Modifier.fillMaxWidth(),
                            name = it.name.substringAfterLast('\\'),
                            size = it.length()
                        )
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    value = caption,
                    onValueChange = { caption = it },
                    label = {
                        Text(text = "Caption")
                    }
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { isFileChooserOpen = true },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Add")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Cancel")
                    }

                    TextButton(
                        onClick = { model.send(channelId, caption); onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}
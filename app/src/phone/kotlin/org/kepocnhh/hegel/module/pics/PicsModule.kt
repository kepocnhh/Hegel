package org.kepocnhh.hegel.module.pics

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.entity.FileDelegate
import org.kepocnhh.hegel.util.toHEX
import sp.kx.storages.Payload
import java.io.File
import java.util.UUID

@Composable
internal fun PicsScreen(
    onBack: () -> Unit,
    state: PicsLogics.State,
    items: PicsLogics.Items,
    onDelete: (UUID) -> Unit,
    onAdd: () -> Unit,
    onSetFile: (UUID, ByteArray) -> Unit,
    onDeleteFile: (UUID) -> Unit,
) {
    val context = LocalContext.current
    val logger = remember { App.injection.loggers.create("[Pics]") }
    val fileState = remember { mutableStateOf<File?>(null) }
    val bitmapState = remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(fileState.value) {
        val file = fileState.value ?: return@LaunchedEffect
        bitmapState.value = withContext(Dispatchers.Default) {
            BitmapFactory.decodeFile(file.absolutePath)
        }
        fileState.value = null
    }
    val bitmap = bitmapState.value
    if (bitmap != null) {
        Dialog(
            onDismissRequest = {
                bitmapState.value = null
            },
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "file:id:$bitmap",
            )
        }
    }
    val requestedState = remember { mutableStateOf<UUID?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val id = requestedState.value
        if (id != null) {
            if (uri != null) {
                logger.debug("uri: $uri")
                val bytes = context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
                onSetFile(id, bytes)
            }
            requestedState.value = null
        }
    }
    LaunchedEffect(requestedState.value) {
        val id = requestedState.value
        if (id != null) {
            val input = PickVisualMediaRequest(
                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
            )
            launcher.launch(input)
        }
    }
    BackHandler(onBack = onBack)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val insets = WindowInsets.systemBars.asPaddingValues()
        LazyColumn(
            contentPadding = insets,
        ) {
            items.list.forEachIndexed { index, payload ->
                item(key = payload.meta.id) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // todo
                            },
                    ) {
                        BasicText(
                            modifier = Modifier
                                .weight(1f),
                            text = "$index] ${payload.value.title}",
                        )
                        val fd = payload.value.fd
                        if (fd == null) {
                            BasicText(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .background(Color.Black)
                                    .padding(8.dp)
                                    .clickable(enabled = !state.loading) {
                                        requestedState.value = payload.meta.id
                                    },
                                text = "+f",
                                style = TextStyle(color = Color.White),
                            )
                        } else {
                            val name = "${payload.meta.id}-${fd.hash.copyOf(16).toHEX()}"
                            val file = context.filesDir.resolve(name)
                            BasicText(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .background(Color.Black)
                                    .padding(8.dp)
                                    .clickable(enabled = !state.loading) {
                                        onDeleteFile(payload.meta.id)
                                    },
                                text = "-f",
                                style = TextStyle(color = Color.White),
                            )
                            if (file.exists()) {
                                BasicText(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .background(Color.Black)
                                        .padding(8.dp)
                                        .clickable(enabled = !state.loading) {
                                            fileState.value = file
                                        },
                                    text = "open",
                                    style = TextStyle(color = Color.White),
                                )
                            } else {
                                BasicText(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .background(Color.Black)
                                        .padding(8.dp)
                                        .clickable(enabled = !state.loading) {
                                            // todo
                                        },
                                    text = "download",
                                    style = TextStyle(color = Color.White),
                                )
                            }
                        }
                        BasicText(
                            modifier = Modifier
                                .background(Color.Black)
                                .padding(16.dp)
                                .clickable(enabled = !state.loading) {
                                    onDelete(payload.meta.id)
                                },
                            text = "x",
                            style = TextStyle(color = Color.White),
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
            ) {
                if (state.loading) {
                    BasicText(
                        text = "loading...",
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                BasicText(
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(16.dp)
                        .clickable(enabled = !state.loading) {
                            onAdd()
                        },
                    text = "+",
                    style = TextStyle(color = Color.White),
                )
            }
        }
    }
}

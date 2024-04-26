package org.kepocnhh.hegel.module.foo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.module.receiver.ReceiverService
import org.kepocnhh.hegel.util.http.HttpService
import java.util.Date
import java.util.UUID

@Composable
private fun FooScreen(
    state: FooLogics.State,
    onDelete: (UUID) -> Unit,
    onAdd: () -> Unit,
    onUpdate: (UUID) -> Unit,
    onTransmitter: () -> Unit,
    onReceiver: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        LazyColumn(
            contentPadding = App.Theme.insets,
        ) {
            state.items.forEachIndexed { index, described ->
                item(
                    key = described.id,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdate(described.id)
                            },
                    ) {
                        val text = """
                            $index) "${described.item.text}"
                            id: ${described.id}
                            created: ${Date(described.info.created.inWholeMilliseconds)}
                            updated: ${Date(described.info.updated.inWholeMilliseconds)}
                        """.trimIndent()
                        BasicText(
                            modifier = Modifier
                                .weight(1f),
                            text = text,
                        )
                        BasicText(
                            modifier = Modifier
                                .background(Color.Black)
                                .padding(16.dp)
                                .clickable(enabled = !state.loading) {
                                    onDelete(described.id)
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
                .padding(App.Theme.insets),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BasicText(
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(16.dp)
                        .clickable(enabled = !state.loading) {
                            onTransmitter()
                        },
                    text = "T -->",
                    style = TextStyle(color = Color.White),
                )
                BasicText(
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(16.dp)
                        .clickable(enabled = !state.loading) {
                            onReceiver()
                        },
                    text = "<-- R",
                    style = TextStyle(color = Color.White),
                )
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

@Composable
internal fun FooScreen() {
    val logics = App.logics<FooLogics>()
    val state = logics.state.collectAsState().value
    LaunchedEffect(Unit) {
        if (state == null) {
            logics.requestItems()
        }
    }
    if (state == null) return
    val context = LocalContext.current
    FooScreen(
        state = state,
        onDelete = logics::deleteItem,
        onAdd = {
            logics.addItem(text = "foo:" + System.currentTimeMillis() % 256 + ":text")
        },
        onUpdate = { id: UUID ->
            logics.updateItem(id = id, text = "foo:" + System.currentTimeMillis() % 256 + ":updated")
        },
        onTransmitter = {
            logics.syncItems()
        },
        onReceiver = {
            HttpService.startService<ReceiverService>(context, HttpService.Action.StartServer)
        },
    )
}

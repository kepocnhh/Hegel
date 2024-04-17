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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.module.receiver.ReceiverService
import org.kepocnhh.hegel.util.http.HttpService
import java.util.Date
import java.util.UUID

@Composable
private fun FooScreen(
    items: List<Foo>,
    onDelete: (UUID) -> Unit,
    onAdd: () -> Unit,
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
            items.forEachIndexed { index, item ->
                item(
                    key = item.id,
                ) {
                    BasicText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDelete(item.id)
                            },
                        text = "$index) ${item.text}\nid: ${item.id}\ndate: ${Date(item.created.inWholeMilliseconds)}",
                    )
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
                        .clickable {
                            onTransmitter()
                        },
                    text = "T -->",
                    style = TextStyle(color = Color.White),
                )
                BasicText(
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(16.dp)
                        .clickable {
                            onReceiver()
                        },
                    text = "<-- R",
                    style = TextStyle(color = Color.White),
                )
                Spacer(modifier = Modifier.weight(1f))
                BasicText(
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(16.dp)
                        .clickable {
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
        items = state.items,
        onDelete = logics::deleteItem,
        onAdd = {
            logics.addItem(text = "foo:" + System.currentTimeMillis() % 64 + ":text")
        },
        onTransmitter = {
            TODO()
        },
        onReceiver = {
            HttpService.startService<ReceiverService>(context, HttpService.Action.StartServer)
        },
    )
}

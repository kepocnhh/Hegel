package org.kepocnhh.hegel.module.bar

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.compose.BackHandler
import java.util.Date
import java.util.UUID

@Composable
private fun BarScreen(
    state: BarLogics.State,
    items: BarLogics.Items,
    onDelete: (UUID) -> Unit,
    onAdd: () -> Unit,
    onUpdate: (UUID) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        LazyColumn(
            contentPadding = App.Theme.insets,
        ) {
            items.list.forEachIndexed { index, described ->
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
                            $index) |${described.item.count}|
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
internal fun BarScreen(onBack: () -> Unit) {
    BackHandler(block = onBack)
    val logics = App.logics<BarLogics>()
    val state = logics.state.collectAsState().value
    val items = logics.items.collectAsState().value
    LaunchedEffect(Unit) {
        if (items == null) {
            logics.requestItems()
        }
    }
    if (items == null) return
    BarScreen(
        state = state,
        items = items,
        onDelete = logics::deleteItem,
        onAdd = {
            logics.addItem(count = System.currentTimeMillis().toInt() % 1024)
        },
        onUpdate = { id: UUID ->
            logics.updateItem(id = id, count = System.currentTimeMillis().toInt() % 1024)
        },
    )
}

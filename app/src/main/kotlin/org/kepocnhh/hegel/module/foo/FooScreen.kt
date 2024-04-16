package org.kepocnhh.hegel.module.foo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.entity.Foo
import java.util.Date
import java.util.UUID

@Composable
private fun FooScreen(
    items: List<Foo>,
    onClick: (UUID) -> Unit,
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
                                onClick(item.id)
                            },
                        text = "$index) ${item.text}\nid: ${item.id}\ndate: ${Date(item.created.inWholeMilliseconds)}",
                    )
                }
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
    if (state != null) {
        FooScreen(
            items = state.items,
            onClick = logics::deleteItem,
        )
    }
}

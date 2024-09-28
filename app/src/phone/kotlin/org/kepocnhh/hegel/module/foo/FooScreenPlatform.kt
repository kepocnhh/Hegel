package org.kepocnhh.hegel.module.foo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import java.util.Date
import java.util.UUID

@Composable
internal fun FooScreen(
    onBack: () -> Unit,
    state: FooLogics.State,
    items: FooLogics.Items,
    onDelete: (UUID) -> Unit,
    onAdd: () -> Unit,
    onUpdate: (UUID) -> Unit,
) {
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
                item(
                    key = payload.meta.id,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdate(payload.meta.id)
                            },
                    ) {
                        val text = """
                            $index) "${payload.value.text}"
                            id: ${payload.meta.id}
                            created: ${Date(payload.meta.created.inWholeMilliseconds)}
                            updated: ${Date(payload.meta.info.updated.inWholeMilliseconds)}
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

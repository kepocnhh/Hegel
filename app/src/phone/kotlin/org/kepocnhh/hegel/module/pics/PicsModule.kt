package org.kepocnhh.hegel.module.pics

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun PicsScreen(
    onBack: () -> Unit,
    state: PicsLogics.State,
    items: PicsLogics.Items,
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
                item(key = payload.meta.id) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // todo
                            },
                    ) {
                        BasicText(
                            modifier = Modifier.height(42.dp),
                            text = "$index] ${payload.value.title}",
                        )
                    }
                }
            }
        }
    }
}

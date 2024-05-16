package org.kepocnhh.hegel.module.router

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun RouterScreen(onState: (RouterScreen.State) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            RouterScreen.State.entries.withIndex()
                .groupBy { (index: Int, _) -> index / 2 }
                .forEach { (_, columns) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        columns.forEach { (_, state) ->
                            BasicText(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clickable {
                                        onState(state)
                                    }
                                    .wrapContentSize(),
                                text = state.name,
                            )
                        }
                    }
                }
        }
    }
}

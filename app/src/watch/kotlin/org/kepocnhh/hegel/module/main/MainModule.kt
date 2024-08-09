package org.kepocnhh.hegel.module.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.util.compose.ListPlatform

@Composable
internal fun MainScreen(
    onState: (MainScreen.State) -> Unit,
    onLock: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        ListPlatform(
            modifier = Modifier.fillMaxSize(),
        ) {
            MainScreen.State.entries.forEach { state ->
                item(key = state.name) {
                    BasicText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clickable {
                                onState(state)
                            }
                            .wrapContentSize(),
                        text = state.name,
                    )
                }
            }
            item(key = "lock") {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clickable(onClick = onLock)
                        .wrapContentSize(),
                    text = "lock",
                )
            }
        }
    }
}

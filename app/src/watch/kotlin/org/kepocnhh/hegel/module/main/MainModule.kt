package org.kepocnhh.hegel.module.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.compose.ListPlatform
import org.kepocnhh.hegel.util.toHEX

@Composable
internal fun MainScreen(
    onState: (MainScreen.State) -> Unit,
    onLock: () -> Unit,
) {
    val logics = App.logics<MainLogics>()
    val state = logics.states.collectAsState().value
    LaunchedEffect(Unit) {
        if (state == null) logics.requestState()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        ListPlatform(
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "publicKeyHash") {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .wrapContentSize(),
                    text = state?.publicKeyHash?.toHEX().orEmpty(),
                    style = TextStyle(fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center),
                )
            }
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

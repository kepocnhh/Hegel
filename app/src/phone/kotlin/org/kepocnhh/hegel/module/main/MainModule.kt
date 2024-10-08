package org.kepocnhh.hegel.module.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.compose.Button
import org.kepocnhh.hegel.util.toHEX

@Composable
internal fun MainScreen(
    onState: (MainScreen.State) -> Unit,
    onLock: () -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            MainScreen.State.entries.forEach { state ->
                Button(
                    text = state.name,
                    onClick = {
                        onState(state)
                    },
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(insets),
    ) {
        val publicKeyHash = state?.publicKeyHash?.toHEX()?.let { text ->
            text.slice(0 until text.length / 2) + "\n" + text.slice(text.length / 2 until text.length)
        }
        BasicText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .wrapContentSize(),
            text = publicKeyHash.orEmpty(),
            style = TextStyle(fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center),
        )
        BasicText(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter)
                .clickable(onClick = onLock)
                .wrapContentSize(),
            text = "lock",
        )
    }
}

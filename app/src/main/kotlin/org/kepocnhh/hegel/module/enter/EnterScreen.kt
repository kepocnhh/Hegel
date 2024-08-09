package org.kepocnhh.hegel.module.enter

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import java.security.PrivateKey

@Composable
internal fun EnterScreen(
    onEnter: (PrivateKey) -> Unit,
    onExit: () -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    val logics = App.logics<EnterLogics>()
    val state = logics.states.collectAsState().value
    val pinState = remember { mutableStateOf("0202") } // todo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            ) {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(enabled = !state.loading) {
                            onExit()
                        }
                        .wrapContentSize(),
                    text = "exit",
                )
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(enabled = !state.loading) {
                            val pin = pinState.value
                            pinState.value = ""
                            logics.enter(pin = pin)
                        }
                        .wrapContentSize(),
                    text = "enter",
                )
            }
        }
    }
}

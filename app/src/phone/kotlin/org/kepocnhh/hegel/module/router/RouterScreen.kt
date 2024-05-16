package org.kepocnhh.hegel.module.router

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.kepocnhh.hegel.util.compose.Button

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
            RouterScreen.State.entries.forEach { state ->
                Button(
                    text = state.name,
                    onClick = {
                        onState(state)
                    },
                )
            }
        }
    }
}

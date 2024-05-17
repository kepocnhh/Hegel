package org.kepocnhh.hegel.module.transmitter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.compose.BackHandler
import org.kepocnhh.hegel.util.compose.Button

@Composable
internal fun TransmitterScreen(
    onBack: () -> Unit,
    state: TransmitterLogics.State,
) {
    BackHandler(block = onBack)
    val logics = App.logics<TransmitterLogics>()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Box(
            modifier = Modifier
                .padding(App.Theme.insets)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    text = "sync",
                    enabled = !state.loading,
                    onClick = {
                        logics.syncItems()
                    },
                )
            }
        }
    }
}

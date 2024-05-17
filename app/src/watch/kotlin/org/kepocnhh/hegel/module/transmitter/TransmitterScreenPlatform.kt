package org.kepocnhh.hegel.module.transmitter

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
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.compose.ListPlatform
import org.kepocnhh.hegel.util.compose.STDBox

@Composable
internal fun TransmitterScreen(
    onBack: () -> Unit,
    state: TransmitterLogics.State,
) {
    val logics = App.logics<TransmitterLogics>()
    STDBox(
        modifier = Modifier.fillMaxSize(),
        onDismissed = onBack,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            ListPlatform(
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "sync") {
                    BasicText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clickable(enabled = !state.loading) {
                                logics.syncItems()
                            }
                            .wrapContentSize(),
                        text = "sync",
                    )
                }
            }
        }
    }
}

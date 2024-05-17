package org.kepocnhh.hegel.module.router

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState

@Composable
internal fun RouterScreen(onState: (RouterScreen.State) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = rememberScalingLazyListState(0, 0),
            contentPadding = PaddingValues(),
            autoCentering = AutoCenteringParams(0, 0),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RouterScreen.State.entries.forEach { state ->
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
        }
    }
}

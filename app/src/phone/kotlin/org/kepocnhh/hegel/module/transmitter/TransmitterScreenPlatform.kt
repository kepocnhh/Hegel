package org.kepocnhh.hegel.module.transmitter

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.compose.Button

@Composable
internal fun TransmitterScreen(
    onBack: () -> Unit,
    state: TransmitterLogics.State,
    savedSpec: String?,
    onSync: (String) -> Unit,
) {
    BackHandler(onBack = onBack)
    val insets = WindowInsets.systemBars.asPaddingValues()
    val addressState = remember { mutableStateOf("192.168.88.217:40631") }
    LaunchedEffect(savedSpec) {
        if (!savedSpec.isNullOrBlank()) {
            addressState.value = savedSpec
        }
    }
    val focusRequester = remember { FocusRequester() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Box(
            modifier = Modifier
                .padding(insets)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.Center),
            ) {
                BasicText(
                    text = "address:",
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .focusRequester(focusRequester),
                    enabled = !state.loading,
                    readOnly = state.loading,
                    value = addressState.value,
                    onValueChange = { addressState.value = it },
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            ) {
                Button(
                    text = "sync",
                    enabled = !state.loading,
                    onClick = {
                        onSync(addressState.value)
                    },
                )
            }
        }
    }
}

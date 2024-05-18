package org.kepocnhh.hegel.module.transmitter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.kepocnhh.hegel.util.compose.STDBox

@Composable
internal fun TransmitterScreen(
    onBack: () -> Unit,
    state: TransmitterLogics.State,
    savedSpec: String?,
    onSync: (String) -> Unit,
) {
    val addressState = remember { mutableStateOf("") }
    LaunchedEffect(savedSpec) {
        if (!savedSpec.isNullOrBlank()) {
            addressState.value = savedSpec
        }
    }
    val focusRequester = remember { FocusRequester() }
    STDBox(
        modifier = Modifier.fillMaxSize(),
        onDismissed = onBack,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.Center),
            ) {
                BasicText(
                    text = "address:",
                    style = TextStyle(fontSize = 12.sp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .focusRequester(focusRequester),
                    enabled = !state.loading,
                    readOnly = state.loading,
//                    keyboardOptions = KeyboardOptions(
//                        keyboardType = KeyboardType.Uri,
//                        imeAction = ImeAction.Done,
//                    ),
                    textStyle = TextStyle(fontSize = 14.sp),
                    singleLine = true,
                    value = addressState.value,
                    onValueChange = { addressState.value = it },
                )
            }
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = !state.loading) {
                        focusRequester.freeFocus()
                        onSync(addressState.value)
                    }
                    .wrapContentSize(),
                text = "sync",
                style = TextStyle(color = Color.White),
            )
        }
    }
}

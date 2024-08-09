package org.kepocnhh.hegel.module.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthScreen(
    state: AuthLogics.State,
    fileState: MutableState<String>,
    passwordState: MutableState<String>,
    aliasState: MutableState<String>,
    pinState: MutableState<String>,
    onAuth: () -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(insets),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BasicText(text = "file")
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                singleLine = true,
                value = fileState.value,
                onValueChange = { fileState.value = it },
            )
            BasicText(text = "password")
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                singleLine = true,
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
            )
            BasicText(text = "alias")
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                singleLine = true,
                value = aliasState.value,
                onValueChange = { aliasState.value = it },
            )
            BasicText(text = "pin")
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                singleLine = true,
                value = pinState.value,
                onValueChange = { text ->
                    val digits = "0123456789"
                    val contains = text.all { digits.contains(it) }
                    if (text.length < 5 && contains) {
                        pinState.value = text
                    }
                },
            )
        }
        BasicText(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter)
                .clickable(enabled = !state.loading, onClick = onAuth)
                .wrapContentSize(),
            text = "auth",
        )
    }
}

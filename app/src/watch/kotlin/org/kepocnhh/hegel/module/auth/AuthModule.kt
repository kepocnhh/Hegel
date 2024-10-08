package org.kepocnhh.hegel.module.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.util.compose.ListPlatform

@Composable
private fun TextField(key: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicText(text = key)
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            singleLine = true,
            value = value,
            onValueChange = onValueChange,
        )
    }
}

@Composable
internal fun AuthScreen(
    state: AuthLogics.State,
    fileState: MutableState<String>,
    passwordState: MutableState<String>,
    aliasState: MutableState<String>,
    pinState: MutableState<String>,
    onAuth: () -> Unit,
) {
    ListPlatform(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        mapOf(
            "file" to fileState,
            "password" to passwordState,
            "alias" to aliasState,
        ).forEach { (key, _state) ->
            item(key) {
                TextField(key = key, value = _state.value, onValueChange = { _state.value = it })
            }
        }
        item("pin") {
            TextField(
                key = "pin",
                value = pinState.value,
                onValueChange = { value ->
                    val digits = "0123456789"
                    val contains = value.all { digits.contains(it) }
                    if (value.length < 5 && contains) pinState.value = value
                },
            )
        }
        item("auth") {
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clickable(enabled = !state.loading, onClick = onAuth)
                    .wrapContentSize(),
                text = "auth",
            )
        }
    }
}

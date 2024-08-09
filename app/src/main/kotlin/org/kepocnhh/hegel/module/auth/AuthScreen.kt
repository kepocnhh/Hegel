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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.entity.Keys
import org.kepocnhh.hegel.util.showToast
import java.security.PrivateKey

@Composable
internal fun AuthScreen(
    onAuth: (Keys, PrivateKey) -> Unit,
) {
    val context = LocalContext.current
    val logger = remember { App.injection.loggers.create("[Auth]") }
    val insets = WindowInsets.systemBars.asPaddingValues()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val logics = App.logics<AuthLogics>()
        LaunchedEffect(Unit) {
            logics.events.collect { event ->
                when (event) {
                    is AuthLogics.Event.OnAuth -> event.result.fold(
                        onSuccess = { (keys, privateKey) ->
                            onAuth(keys, privateKey)
                        },
                        onFailure = { error ->
                            logger.warning("auth error: $error")
                            context.showToast("auth error: $error")
                        },
                    )
                }
            }
        }
        val state = logics.states.collectAsState().value
        val fileState = remember { mutableStateOf("a202.pkcs12") }
        val passwordState = remember { mutableStateOf("qwe202") }
        val aliasState = remember { mutableStateOf("a202") }
        val pinState = remember { mutableStateOf("0202") }
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
                    .clickable(enabled = !state.loading) {
                        logics.auth(
                            file = fileState.value,
                            password = passwordState.value,
                            alias = aliasState.value,
                            pin = pinState.value,
                        )
                    }
                    .wrapContentSize(),
                text = "auth",
            )
        }
    }
}

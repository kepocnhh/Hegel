package org.kepocnhh.hegel.module.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val state = logics.states.collectAsState().value
        val fileState = remember { mutableStateOf("a202.pkcs12") } // todo
        val passwordState = remember { mutableStateOf("qwe202") } // todo
        val aliasState = remember { mutableStateOf("a202") } // todo
        val pinState = remember { mutableStateOf("0202") } // todo
        AuthScreen(
            state = state,
            fileState = fileState,
            passwordState = passwordState,
            aliasState = aliasState,
            pinState = pinState,
            onAuth = {
                logics.auth(
                    file = fileState.value,
                    password = passwordState.value,
                    alias = aliasState.value,
                    pin = pinState.value,
                )
            },
        )
    }
}

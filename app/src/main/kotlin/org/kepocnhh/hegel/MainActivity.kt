package org.kepocnhh.hegel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import org.kepocnhh.hegel.module.foo.FooScreen
import org.kepocnhh.hegel.module.receiver.ReceiverScreen
import org.kepocnhh.hegel.module.receiver.ReceiverService
import org.kepocnhh.hegel.util.compose.BackHandler
import org.kepocnhh.hegel.util.http.HttpService

internal class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = ComposeView(this)
        setContentView(view)
        view.setContent {
            App.Theme.Composition(
                onBackPressedDispatcher = onBackPressedDispatcher,
            ) {
                BackHandler {
                    finish()
                }
                val receiver = ReceiverService.state.collectAsState().value != HttpService.State.Stopped
                if (receiver) {
                    ReceiverScreen()
                } else {
                    FooScreen()
                }
            }
        }
    }
}

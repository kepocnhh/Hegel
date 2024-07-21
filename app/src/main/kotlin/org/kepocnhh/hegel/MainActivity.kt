package org.kepocnhh.hegel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import org.kepocnhh.hegel.module.router.RouterScreen
import org.kepocnhh.hegel.util.compose.BackHandler

internal class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = ComposeView(this)
        setContentView(view)
        // todo post notifications
        view.setContent {
            App.Theme.Composition(
                onBackPressedDispatcher = onBackPressedDispatcher,
            ) {
                RouterScreen(
                    onBack = {
                        finish()
                    },
                )
            }
        }
    }
}

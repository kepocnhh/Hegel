package org.kepocnhh.hegel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import org.kepocnhh.hegel.util.compose.BackHandler

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
                Box(modifier = Modifier.fillMaxSize()) {
                    BasicText(
                        modifier = Modifier
                            .align(Alignment.Center),
                        text = "${BuildConfig.APPLICATION_ID}\n${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}",
                    )
                }
            }
        }
    }
}

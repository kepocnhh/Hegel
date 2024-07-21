package org.kepocnhh.hegel.module.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.kepocnhh.hegel.App

@Composable
internal fun PreviewComposition(
    content: @Composable BoxScope.() -> Unit,
) {
    App.Theme.Composition {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            content = content,
        )
    }
}

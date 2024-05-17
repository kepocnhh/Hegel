package org.kepocnhh.hegel.util.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
internal fun Button(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    BasicText(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .wrapContentSize(),
        text = text,
        style = TextStyle(color = Color.Black)
    )
}

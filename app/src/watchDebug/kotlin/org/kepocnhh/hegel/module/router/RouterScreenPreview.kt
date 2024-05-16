package org.kepocnhh.hegel.module.router

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import android.content.res.Configuration

@Preview(device = WearDevices.LARGE_ROUND, uiMode = Configuration.UI_MODE_TYPE_WATCH)
@Preview(name = "small", device = WearDevices.SMALL_ROUND)
@Composable
private fun RouterScreenPreview() {
    RouterScreen(
        onState = {
            // todo
        },
    )
}

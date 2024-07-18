package org.kepocnhh.hegel.module.router

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices

@Preview(device = WearDevices.LARGE_ROUND)
@Preview(name = "small", device = WearDevices.SMALL_ROUND)
@Composable
private fun RouterScreenPreview() {
    RouterScreen(
        onBack = {
            // noop
        },
    )
}
package org.kepocnhh.hegel.module.transmitter

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import org.kepocnhh.hegel.module.app.PreviewComposition

@Composable
private fun TransmitterScreenPreview(
    state: TransmitterLogics.State,
    savedSpec: String?,
) {
    PreviewComposition {
        TransmitterScreen(
            onBack = {
                // noop
            },
            state = state,
            savedSpec = savedSpec,
            onSync = {
                // noop
            },
        )
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Preview(name = "small", device = WearDevices.SMALL_ROUND)
@Composable
private fun TransmitterScreenPreview() {
    TransmitterScreenPreview(
        state = TransmitterLogics.State(loading = false),
        savedSpec = null,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Preview(name = "small", device = WearDevices.SMALL_ROUND)
@Composable
private fun TransmitterScreenSpecPreview() {
    TransmitterScreenPreview(
        state = TransmitterLogics.State(loading = false),
        savedSpec = "http://123.456.78.910:12345",
    )
}

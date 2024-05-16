package org.kepocnhh.hegel.module.foo

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.module.app.PreviewComposition
import sp.kx.storages.Described
import sp.kx.storages.ItemInfo
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Composable
private fun FooScreenPreview(
    state: FooLogics.State,
    items: FooLogics.Items,
) {
    PreviewComposition {
        FooScreen(
            state = state,
            items = items,
            onDelete = {
                // noop
            },
            onAdd = {
                // noop
            },
            onUpdate = {
                // noop
            },
        )
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Preview(name = "small", device = WearDevices.SMALL_ROUND)
@Composable
private fun FooScreenPreview() {
    val state = FooLogics.State(loading = false)
    val created = 1.seconds
    val list = (0..3).map { index ->
        Described(
            id = UUID.fromString("6bd3361a-ddc9-4bb2-8d02-e4782c0c267$index"),
            info = ItemInfo(
                created = created + index.seconds,
                updated = created + index.seconds + 1.seconds,
                hash = "hash:$index",
            ),
            item = Foo(text = "foo:$index"),
        )
    }
    val items = FooLogics.Items(list = list)
    FooScreenPreview(
        state = state,
        items = items,
    )
}

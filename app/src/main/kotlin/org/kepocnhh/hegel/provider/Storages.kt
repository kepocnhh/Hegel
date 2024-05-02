package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.entity.Foo
import sp.kx.storages.SyncStorage

internal class Storages(
    val foo: SyncStorage<Foo>,
    val bar: SyncStorage<Bar>,
)

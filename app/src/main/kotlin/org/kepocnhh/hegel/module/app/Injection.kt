package org.kepocnhh.hegel.module.app

import org.kepocnhh.hegel.provider.Contexts
import org.kepocnhh.hegel.provider.Locals
import org.kepocnhh.hegel.provider.Loggers
import org.kepocnhh.hegel.provider.Remotes
import org.kepocnhh.hegel.provider.Serializer
import sp.kx.storages.SyncStreamsStorages

internal data class Injection(
    val contexts: Contexts,
    val loggers: Loggers,
    val locals: Locals,
    val storages: SyncStreamsStorages,
    val remotes: Remotes,
    val serializer: Serializer,
)

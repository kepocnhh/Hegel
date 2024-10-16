package org.kepocnhh.hegel.module.app

import org.kepocnhh.hegel.provider.Assets
import org.kepocnhh.hegel.provider.Contexts
import org.kepocnhh.hegel.provider.Dirs
import org.kepocnhh.hegel.provider.Locals
import org.kepocnhh.hegel.provider.Loggers
import org.kepocnhh.hegel.provider.Remotes
import org.kepocnhh.hegel.provider.Secrets
import org.kepocnhh.hegel.provider.Serializer
import org.kepocnhh.hegel.provider.Sessions
import sp.kx.http.TLSEnvironment
import sp.kx.storages.SyncStreamsStorages
import java.io.File

internal data class Injection(
    val contexts: Contexts,
    val loggers: Loggers,
    val locals: Locals,
    val storages: SyncStreamsStorages,
    val remotes: Remotes,
    val serializer: Serializer,
    val sessions: Sessions,
    val assets: Assets,
    val secrets: Secrets,
    val tls: TLSEnvironment,
    val dirs: Dirs,
)

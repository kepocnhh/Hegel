package org.kepocnhh.hegel.provider

import java.net.URL

internal interface Remotes {
    fun items(address: URL): ItemsRemotes
    fun files(address: URL): FilesRemotes
}

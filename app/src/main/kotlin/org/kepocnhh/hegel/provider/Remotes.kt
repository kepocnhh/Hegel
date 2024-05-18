package org.kepocnhh.hegel.provider

import java.net.URL

internal interface Remotes {
    fun items(url: URL): ItemsRemotes
}

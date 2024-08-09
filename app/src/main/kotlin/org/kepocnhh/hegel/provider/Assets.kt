package org.kepocnhh.hegel.provider

import java.io.InputStream

internal interface Assets {
    fun getAsset(name: String): InputStream
}

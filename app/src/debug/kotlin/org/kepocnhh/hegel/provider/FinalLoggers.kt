package org.kepocnhh.hegel.provider

import android.util.Log

internal object FinalLoggers : Loggers {
    override fun create(tag: String): Logger {
        return AndroidLoggers(tag = tag)
    }
}

private class AndroidLoggers(
    private val tag: String,
) : Logger {
    override fun debug(message: String) {
        Log.d(tag, message)
    }

    override fun warning(message: String) {
        Log.w(tag, message)
    }
}

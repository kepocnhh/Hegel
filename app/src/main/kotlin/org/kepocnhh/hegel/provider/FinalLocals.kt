package org.kepocnhh.hegel.provider

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.kepocnhh.hegel.BuildConfig
import org.kepocnhh.hegel.entity.Keys
import org.kepocnhh.hegel.entity.Session
import java.net.URL
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class FinalLocals(
    context: Context,
    private val secrets: Secrets,
) : Locals {
    private val prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    override var session: Session?
        get() {
            val exists = prefs.getBoolean("session", false)
            return if (exists) {
                Session(
                    id = UUID.fromString(prefs.getString("session:id", null) ?: TODO()),
                    expires = prefs.getLong("session:expires", -1).milliseconds,
                )
            } else {
                null
            }
        }
        set(value) {
            if (value == null) {
                prefs.edit()
                    .putBoolean("session", false)
                    .remove("session:id")
                    .remove("session:expires")
                    .commit()
            } else {
                prefs.edit()
                    .putBoolean("session", true)
                    .putString("session:id", value.id.toString())
                    .putLong("session:expires", value.expires.inWholeMilliseconds)
                    .commit()
            }
        }

    override var address: URL?
        get() {
            val spec = prefs.getString("address", null)
            return if (spec == null) {
                null
            } else {
                URL(spec)
            }
        }
        set(value) {
            if (value == null) {
                prefs.edit()
                    .remove("address")
                    .commit()
            } else {
                prefs.edit()
                    .putString("address", value.toString())
                    .commit()
            }
        }

    override var keys: Keys?
        get() {
            val exists = prefs.getBoolean("keys", false)
            return if (exists) {
                Keys(
                    publicKey = secrets.toPublicKey(prefs.getBytes("keys:publicKey")),
                    privateKeyEncrypted = prefs.getBytes("keys:privateKeyEncrypted"),
                )
            } else {
                null
            }
        }
        set(value) {
            if (value == null) {
                prefs.edit()
                    .putBoolean("keys", false)
                    .remove("keys:publicKey")
                    .remove("keys:privateKeyEncrypted")
                    .commit()
            } else {
                prefs.edit()
                    .putBoolean("keys", true)
                    .putString("keys:publicKey", value.publicKey.encoded)
                    .putString("keys:privateKeyEncrypted", value.privateKeyEncrypted)
                    .commit()
            }
        }

    override var requested: Map<UUID, Duration>
        get() {
            return prefs.getStringSet("requested", null)?.associate {
                val split = it.split(",")
                check(split.size == 2)
                UUID.fromString(split[0]) to split[1].toLong().milliseconds
            }.orEmpty()
        }
        set(value) {
            if (value.isEmpty()) {
                prefs.edit().remove("requested").commit()
            } else {
                prefs.edit().putStringSet("requested", value.map { (id, time) -> "$id,${time.inWholeMilliseconds}" }.toSet()).commit()
            }
        }

    private fun SharedPreferences.Editor.putString(key: String, bytes: ByteArray): SharedPreferences.Editor {
        return putString(key, Base64.encodeToString(bytes, Base64.DEFAULT))
    }

    private fun SharedPreferences.getBytes(key: String): ByteArray {
        val text = getString(key, null) ?: error("No \"$key\"!")
        return Base64.decode(text, Base64.DEFAULT)
    }
}

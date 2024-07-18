package org.kepocnhh.hegel.provider

import android.content.Context
import org.kepocnhh.hegel.BuildConfig
import org.kepocnhh.hegel.entity.Session
import java.net.URL
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

internal class FinalLocals(
    context: Context,
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
}
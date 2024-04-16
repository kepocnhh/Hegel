package org.kepocnhh.hegel.module.app

import org.kepocnhh.hegel.provider.Contexts
import org.kepocnhh.hegel.provider.Locals

internal data class Injection(
    val contexts: Contexts,
    val locals: Locals,
)

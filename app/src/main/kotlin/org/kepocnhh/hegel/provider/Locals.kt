package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Keys
import org.kepocnhh.hegel.entity.Session
import java.net.URL

internal interface Locals {
    var session: Session?
    var address: URL?
    var keys: Keys?
}

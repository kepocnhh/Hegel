package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.Session

internal interface Locals {
    val foo: Storage<Foo>
    var session: Session?
}

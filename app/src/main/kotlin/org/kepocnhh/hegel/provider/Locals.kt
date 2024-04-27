package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.Session

internal interface Locals {
    val foo: Storage<Foo>
    val bar: Storage<Bar>
    var session: Session?
}

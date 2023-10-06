package scala.scalanative.libc

import scala.scalanative.unsafe.extern

@extern
trait stdatomicExt { self: stdatomic.type => }

package scala.collection.immutable

import scala.scalanative.libc.stdatomic._
import scala.scalanative.libc.stdatomic.memory_order._

// Backport from scala.runtime moved into s.c.immutable and made package
// private to avoid the need for MiMa whitelisting.
/* private[immutable] */ object VM {
  def releaseFence(): Unit = atomic_thread_fence(memory_order_release)
}

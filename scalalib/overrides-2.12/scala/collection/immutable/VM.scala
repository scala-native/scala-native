package scala.collection.immutable

// Backport from scala.runtime moved into s.c.immutable and made package
// private to avoid the need for MiMa whitelisting.
/* private[immutable] */ object VM {
  def releaseFence(): Unit = ()
}

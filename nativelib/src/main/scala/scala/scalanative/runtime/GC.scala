package scala.scalanative
package runtime

import scalanative.unsafe._
import scalanative.unsigned._

/**
 * The Boehm GC conservative garbage collector
 *
 * @see [[http://hboehm.info/gc/gcinterface.html C Interface]]
 */
@extern
object GCExtern {

  @name("scalanative_init")
  def init(): Unit = extern
  @name("scalanative_alloc")
  def alloc(rawty: RawPtr, size: CSize): RawPtr = extern
  @name("scalanative_alloc_small")
  def alloc_small(rawty: RawPtr, size: Long): RawPtr = extern
  @name("scalanative_alloc_large")
  def alloc_large(rawty: RawPtr, size: Long): RawPtr = extern
  @name("scalanative_alloc_atomic")
  def alloc_atomic(rawty: RawPtr, size: CSize): RawPtr = extern
  @name("scalanative_collect")
  def collect(): Unit = extern
}

object GC {
  def init(): Unit = {
    GCExtern.init()
  }
  def alloc(rawty: RawPtr, size: CSize): RawPtr = GCExtern.alloc(rawty, size)
  def alloc_small(rawty: RawPtr, size: CLong): RawPtr = {
    GCExtern.alloc_small(rawty, size)
  }
  def alloc_large(rawty: RawPtr, size: CLong): RawPtr = {
    GCExtern.alloc_large(rawty, size)
  }
  def alloc_atomic(rawty: RawPtr, size: CSize): RawPtr =
    GCExtern.alloc_atomic(rawty, size)
  def collect(): Unit = GCExtern.collect()
}

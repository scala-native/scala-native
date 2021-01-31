package scala.scalanative
package runtime

import scalanative.unsafe._

/**
 * The Boehm GC conservative garbage collector
 *
 * @see [[http://hboehm.info/gc/gcinterface.html C Interface]]
 */
@extern
object GC {
  @name("scalanative_alloc")
  def alloc(cls: Class[_], size: CSize): RawPtr = extern
  @name("scalanative_alloc_atomic")
  def alloc_atomic(cls: Class[_], size: CSize): RawPtr = extern
  @name("scalanative_collect")
  def collect(): Unit = extern
}

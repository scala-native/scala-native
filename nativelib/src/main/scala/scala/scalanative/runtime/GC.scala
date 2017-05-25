package scala.scalanative
package runtime

import native._

/**
 * The Boehm GC conservative garbage collector
 *
 * @see [[http://hboehm.info/gc/gcinterface.html C Interface]]
 */
@extern
object GC {
  @name("scalanative_alloc")
  def alloc(info: Ptr[ClassType], size: CSize): Ptr[Byte] = extern
  @name("scalanative_alloc_atomic")
  def alloc_atomic(info: Ptr[ClassType], size: CSize): Ptr[Byte] = extern
  @name("scalanative_collect")
  def collect(): Unit = extern
}

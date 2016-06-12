package scala.scalanative
package runtime

import native._

/**
 * The Boehm GC conservative garbage collector
 *
 * @see [[http://hboehm.info/gc/gcinterface.html C Interface]]
 */
@link("gc")
@extern
object GC {
  @name("GC_malloc")
  def malloc(size: CSize): Ptr[_] = extern
  @name("GC_malloc_atomic")
  def malloc_atomic(size: CSize): Ptr[_] = extern
  @name("GC_init")
  def init(): Unit = extern
}

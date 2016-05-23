package scala.scalanative
package runtime

import native._

@link("gc")
@extern object GC {
  @name("GC_malloc")
  def malloc(size: CSize): Ptr[_] = extern
  @name("GC_init")
  def init(): Unit = extern
}

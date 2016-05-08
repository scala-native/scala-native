package scala.scalanative
package runtime

import native._

@extern object GC {
  def GC_malloc(size: CSize): Ptr[_] = extern
  def GC_init(): Unit = extern
}

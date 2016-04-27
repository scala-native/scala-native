package scala.scalanative
package runtime

import native._

@extern
object cxxabi {
  def `__cxa_allocate_exception`(size: Size): Ptr[Byte] = extern
  def `__cxa_free_exception`(exc: Ptr[Byte]): Unit = extern
  def `__cxa_throw`(exc: Ptr[Byte], tinfo: Ptr[Byte], dest: Ptr[Byte]): Nothing = extern
  def `__cxa_begin_catch`(exc: Ptr[Byte]): Unit = extern
  def `__cxa_end_catch`(): Unit = extern
}

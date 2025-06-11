/* Derived, with thanks & appreciation, from
 * "plokhotnyuk/jsoniter-scala multiply_high.c",
 * URL: https://github.com/plokhotnyuk/jsoniter-scala
 *
 * commit: 71fef04 dated: 2025-06-07
 *
 * Used under the original's permissive MIT license as reproduced in
 * this projects LICENSE.md.
 */

package java.lang

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.unsafe._

@define("__SCALANATIVE_JAVALIB_LANG_MATHIMPL_H")
@extern
object MathImpl {

  @alwaysinline
  @name("scalanative_javalib_multiply_high")
  def multiplyHighImpl(x: scala.Long, y: scala.Long): scala.Long = extern

  @alwaysinline
  @name("scalanative_javalib_unsigned_multiply_high")
  def unsignedMultiplyHighImpl(x: scala.Long, y: scala.Long): scala.Long =
    extern

}

package scala.scalanative
package libc

import scalanative.native._

/**
 * Bindings for stdbool.h
 */
@extern
object stdbool {
  @name("scalanative_bool_true")
  def `true`: CBool = extern
  @name("scalanative_bool_false")
  def `false`: CBool = extern
}

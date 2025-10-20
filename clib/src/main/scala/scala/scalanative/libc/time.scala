package scala.scalanative
package libc

import scalanative.unsafe.*

@extern object time extends time

/** See https://en.cppreference.com/w/c/chrono */
@extern private[scalanative] trait time {

  @name("scalanative_clocks_per_sec")
  def CLOCKS_PER_SEC: CInt = extern
}

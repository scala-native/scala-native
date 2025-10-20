package scala.scalanative
package posix

import scala.scalanative.unsafe.*

@extern object fenv extends fenv

@extern trait fenv extends libc.fenv {
  // no extensions yet
}

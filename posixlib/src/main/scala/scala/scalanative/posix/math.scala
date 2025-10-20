package scala.scalanative
package posix

import scalanative.unsafe.*

@extern object math extends math

@extern trait math extends libc.math {
  // no extensions yet
}

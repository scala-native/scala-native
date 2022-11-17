package scala.scalanative
package posix

import scala.scalanative.unsafe._

@extern object stddef extends stddef

@extern trait stddef extends libc.stddef {
  // no extensions yet
}

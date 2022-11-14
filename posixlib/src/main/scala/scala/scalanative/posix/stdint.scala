package scala.scalanative
package posix

import scalanative.unsafe._

@extern object stdint extends stdint

@extern trait stdint extends libc.stdint {
  // no extensions
}

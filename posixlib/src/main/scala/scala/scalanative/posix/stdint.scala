package scala.scalanative
package posix

import scalanative.unsafe.*

@extern object stdint extends stdint

@extern trait stdint extends libc.stdint {
  // no extensions
}

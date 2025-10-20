package scala.scalanative
package posix

import scala.scalanative.unsafe.*

@extern object float extends float

@extern trait float extends libc.float {
  // no extensions yet
}

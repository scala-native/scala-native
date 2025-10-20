package scala.scalanative
package posix

import scala.scalanative.unsafe.*

@extern object ctype extends ctype

@extern trait ctype extends libc.ctype {
  // no extensions yet
}

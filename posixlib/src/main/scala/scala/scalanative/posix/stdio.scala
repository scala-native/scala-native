package scala.scalanative
package posix

import scalanative.unsafe._

@extern object stdio extends stdio

@extern trait stdio extends libc.stdio {
  // no extensions
}

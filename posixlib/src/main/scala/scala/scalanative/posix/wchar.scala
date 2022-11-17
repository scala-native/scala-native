package scala.scalanative
package posix

import scalanative.unsafe._

@extern object wchar extends wchar

@extern trait wchar extends libc.wchar {
  // no extensions
}

package scala.scalanative
package libc

import native._

@extern object string {
  def strncmp(s1: CString, s2: CString, n: CSize): CInt = extern
  def strcmp(s1: CString, s2: CString): CInt = extern
}

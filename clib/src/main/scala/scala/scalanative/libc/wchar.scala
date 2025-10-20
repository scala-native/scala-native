package scala.scalanative
package libc

import scalanative.unsafe.*

@extern object wchar extends wchar

@extern private[scalanative] trait wchar {
  type wchar_t = CWideString

  def wcscpy(dest: wchar_t, src: wchar_t): wchar_t = extern
}

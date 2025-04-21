package scala.scalanative
package runtime

import scalanative.unsafe._

@extern
object Constants {
  def snErrorPrefix: CString = extern
  def snFatalErrorPrefix: CString = extern
}

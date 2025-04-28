package scala.scalanative
package runtime

import scalanative.unsafe._

@extern
object StringConstants {
  def snErrorPrefix: CString = extern
  def snFatalErrorPrefix: CString = extern
}

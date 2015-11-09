package native
package internal

import scala.reflect.macros.blackbox.Context

class Macros(val c: Context) {
  import c.universe._

  val ffi = q"_root_.native.ffi"

  def cquote(args: Tree*): Tree =
    q"$ffi.Ptr.Null[$ffi.Char8]"
}

package native
package plugin

import scala.tools.nsc.Global

trait NativeDefinitions {
  val global: Global
  import global._, definitions._, rootMirror._

  lazy val ForeignExternClass = getRequiredClass("native.ffi.extern")
}

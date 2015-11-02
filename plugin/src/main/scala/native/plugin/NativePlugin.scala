package native
package plugin

import scala.tools.nsc._
import scala.tools.nsc.plugins._

class NativePlugin(val global: Global) extends Plugin { self =>
  val name = "native"
  val description = "Generate Native IR"
  val components = List[PluginComponent](GenNativeComponent)

  object GenNativeComponent extends {
    val global: self.global.type = self.global
    override val runsAfter = List("mixin")
    override val runsBefore = List("delambdafy", "cleanup", "terminal")
  } with GenNative
}

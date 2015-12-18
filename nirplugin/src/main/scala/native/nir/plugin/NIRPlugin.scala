package native
package nir
package plugin

import scala.tools.nsc._
import scala.tools.nsc.plugins._

class NIRPlugin(val global: Global) extends Plugin { self =>
  val name = "nir"
  val description = "Compile to NIR"
  val components = List[PluginComponent](GenNIRComponent)

  object GenNIRComponent extends {
    val global: self.global.type = self.global
    override val runsAfter = List("mixin")
    override val runsBefore = List("delambdafy", "cleanup", "terminal")
  } with GenNIR
}

package salty.tools
package compiler

import scala.tools.nsc._
import scala.tools.nsc.plugins._

class SaltyPlugin(val global: Global) extends Plugin { self =>
  val name = "salty"
  val description = "Compile to Salty"
  val components = List[PluginComponent](GenSaltyCodeComponent)

  object GenSaltyCodeComponent extends {
    val global: self.global.type = self.global
    override val runsAfter = List("mixin")
    override val runsBefore = List("delambdafy", "cleanup", "terminal")
  } with GenSaltyCode
}

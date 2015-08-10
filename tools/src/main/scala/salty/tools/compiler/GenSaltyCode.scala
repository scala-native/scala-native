package salty.tools
package compiler

import scala.tools.nsc._
import scala.tools.nsc.plugins._

abstract class GenSaltyCode extends PluginComponent {
  import global._

  val phaseName = "saltycode"

  override def newPhase(prev: Phase): StdPhase =
    new SaltyCodePhase(prev)

  class SaltyCodePhase(prev: Phase) extends StdPhase(prev) {
    override def apply(cunit: CompilationUnit): Unit = ???
  }
}

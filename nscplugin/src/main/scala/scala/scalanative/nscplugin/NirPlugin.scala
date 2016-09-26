package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.tools.nsc.plugins._

class NirPlugin(val global: Global) extends Plugin { self =>
  val name        = "nir"
  val description = "Compile to Scala Native IR (NIR)"
  val components  = List[PluginComponent](nirCodeGen)

  private var enableMethodCallProfiling: Boolean = false

  def nirOptions: NirOptions =
    NirOptions(enableMethodCallProfiling)

  override def init(options: List[String], error: String => Unit): Boolean = {
    for (option <- options)
      if (option == "methodCallProfiling") enableMethodCallProfiling = true
    true
  }

  object nirAddons extends {
    val global: NirPlugin.this.global.type = NirPlugin.this.global
  } with NirGlobalAddons

  object nirCodeGen extends {
    val global: self.global.type                 = self.global
    val nirAddons: NirPlugin.this.nirAddons.type = NirPlugin.this.nirAddons
    override val runsAfter                       = List("mixin")
    override val runsBefore                      = List("delambdafy", "cleanup", "terminal")
  } with NirCodeGen(nirOptions)
}

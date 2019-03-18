package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.codegen.Lower

object Whitelist {
  val constantModules = {
    val out = collection.mutable.Set.empty[Global]
    out += Global.Top("scala.scalanative.runtime.BoxedUnit$")
    out
  }

  val pure = {
    val out = mutable.Set.empty[Global]
    out += Global.Top("scala.Predef$")
    out += Global.Top("scala.runtime.BoxesRunTime$")
    out += Global.Top("scala.scalanative.runtime.Boxes$")
    out += Global.Top("scala.scalanative.runtime.package$")
    out += Global.Top("scala.scalanative.native.package$")
    out += Global.Top("scala.collection.immutable.Range$")
    out ++= Lower.BoxTo.values
    out ++= constantModules
    out
  }
}

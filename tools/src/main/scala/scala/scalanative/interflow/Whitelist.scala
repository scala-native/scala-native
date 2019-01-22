package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.codegen.Lower

object Whitelist {
  val constantModules = {
    val out = collection.mutable.Set.empty[Global]
    out += Global.Top("scala.scalanative.runtime.BoxedUnit$")
    out += Global.Top("scala.scalanative.native.Tag$")
    out += Global.Top("scala.scalanative.native.Tag$Unit$")
    out += Global.Top("scala.scalanative.native.Tag$Boolean$")
    out += Global.Top("scala.scalanative.native.Tag$Char$")
    out += Global.Top("scala.scalanative.native.Tag$Byte$")
    out += Global.Top("scala.scalanative.native.Tag$UByte$")
    out += Global.Top("scala.scalanative.native.Tag$Short$")
    out += Global.Top("scala.scalanative.native.Tag$UShort$")
    out += Global.Top("scala.scalanative.native.Tag$Int$")
    out += Global.Top("scala.scalanative.native.Tag$UInt$")
    out += Global.Top("scala.scalanative.native.Tag$Long$")
    out += Global.Top("scala.scalanative.native.Tag$ULong$")
    out += Global.Top("scala.scalanative.native.Tag$Float$")
    out += Global.Top("scala.scalanative.native.Tag$Double$")
    out += Global.Top("scala.scalanative.native.Tag$Nat0$")
    out += Global.Top("scala.scalanative.native.Tag$Nat1$")
    out += Global.Top("scala.scalanative.native.Tag$Nat2$")
    out += Global.Top("scala.scalanative.native.Tag$Nat3$")
    out += Global.Top("scala.scalanative.native.Tag$Nat4$")
    out += Global.Top("scala.scalanative.native.Tag$Nat5$")
    out += Global.Top("scala.scalanative.native.Tag$Nat6$")
    out += Global.Top("scala.scalanative.native.Tag$Nat7$")
    out += Global.Top("scala.scalanative.native.Tag$Nat8$")
    out += Global.Top("scala.scalanative.native.Tag$Nat9$")
    out += Global.Top("java.lang.Math$")
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

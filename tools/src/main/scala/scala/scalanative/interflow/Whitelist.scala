package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.codegen.Lower

object Whitelist {
  val constantModules = {
    val out = collection.mutable.Set.empty[Global]
    out += Global.Top("scala.scalanative.runtime.BoxedUnit$")
    out += Global.Top("scala.scalanative.unsafe.Tag$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Unit$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Boolean$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Char$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Byte$")
    out += Global.Top("scala.scalanative.unsafe.Tag$UByte$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Short$")
    out += Global.Top("scala.scalanative.unsafe.Tag$UShort$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Int$")
    out += Global.Top("scala.scalanative.unsafe.Tag$UInt$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Long$")
    out += Global.Top("scala.scalanative.unsafe.Tag$ULong$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Float$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Double$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Nat0$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Nat1$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Nat2$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Nat3$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Nat4$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Nat5$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Nat6$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Nat7$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Nat8$")
    out += Global.Top("scala.scalanative.unsafe.Tag$Nat9$")
    out += Global.Top("java.lang.Math$")
    out
  }

  val pure = {
    val out = mutable.Set.empty[Global]
    out += Global.Top("scala.Predef$")
    out += Global.Top("scala.runtime.BoxesRunTime$")
    out += Global.Top("scala.scalanative.runtime.Boxes$")
    out += Global.Top("scala.scalanative.runtime.package$")
    out += Global.Top("scala.scalanative.unsafe.package$")
    out += Global.Top("scala.collection.immutable.Range$")
    out ++= Lower.BoxTo.values
    out ++= constantModules
    out
  }
}

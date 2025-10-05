package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.codegen.Lower

private[scalanative] object Allowlist {

  val constantModules = {
    val out = collection.mutable.Set.empty[nir.Global]
    out += nir.Global.Top("scala.scalanative.runtime.BoxedUnit$")
    out += nir.Global.Top("scala.scalanative.runtime.LazyVals$")
    out += nir.Global.Top("scala.scalanative.runtime.MemoryLayout$")
    out += nir.Global.Top("scala.scalanative.runtime.MemoryLayout$Array$")
    out += nir.Global.Top("scala.scalanative.runtime.MemoryLayout$Object$")
    out += nir.Global.Top("scala.scalanative.runtime.MemoryLayout$Rtti$")
    out += nir.Global.Top("scala.scalanative.runtime.monitor.BasicMonitor$")
    out += nir.Global.Top("scala.scalanative.runtime.monitor.package$LockWord")
    out += nir.Global.Top("scala.scalanative.runtime.monitor.package$LockWord$")
    out += nir.Global.Top(
      "scala.scalanative.runtime.monitor.package$LockWord32$"
    )
    out += nir.Global.Top("scala.scalanative.runtime.monitor.package$LockType$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Unit$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Boolean$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Char$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Byte$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$UByte$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Short$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$UShort$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Int$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$UInt$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Long$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$ULong$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Float$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Double$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Size$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$USize$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Nat0$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Nat1$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Nat2$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Nat3$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Nat4$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Nat5$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Nat6$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Nat7$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Nat8$")
    out += nir.Global.Top("scala.scalanative.unsafe.Tag$Nat9$")
    out += nir.Global.Top("java.lang.Math$")
    out
  }

  val pure = {
    val out = mutable.Set.empty[nir.Global]
    out += nir.Global.Top("scala.Predef$")
    out += nir.Global.Top("scala.runtime.BoxesRunTime$")
    out += nir.Global.Top("scala.scalanative.runtime.Boxes$")
    out += nir.Global.Top("scala.scalanative.runtime.package$")
    out += nir.Global.Top("scala.scalanative.unsafe.package$")
    out += nir.Global.Top("scala.collection.immutable.Range$")
    out ++= Lower.BoxTo.values
    out ++= constantModules
    out
  }
}

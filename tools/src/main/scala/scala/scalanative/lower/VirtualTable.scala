package scala.scalanative
package lower

import scala.collection.mutable
import scalanative.nir._
import scalanative.sema._

class VirtualTable(meta: Metadata, cls: Class) {
  private val entries: mutable.UnrolledBuffer[Method] =
    cls.parent.fold {
      mutable.UnrolledBuffer.empty[Method]
    } { parent =>
      meta.vtable(parent).entries.clone
    }
  private val values: mutable.UnrolledBuffer[Val] =
    cls.parent.fold {
      mutable.UnrolledBuffer.empty[Val]
    } { parent =>
      meta.vtable(parent).values.clone
    }
  locally {
    // Go through all methods and update vtable entries and values
    // according to override annotations. Additionally, discover
    // if Java's hashCode/equals and Scala's ==/## are overriden.
    var javaEqualsOverride: Option[Val]    = None
    var javaHashCodeOverride: Option[Val]  = None
    var scalaEqualsOverride: Option[Val]   = None
    var scalaHashCodeOverride: Option[Val] = None
    cls.methods.foreach { meth =>
      meth.overrides
        .collect {
          case ovmeth if ovmeth.inClass =>
            values(index(ovmeth)) = meth.value
            if (ovmeth eq meta.javaEquals) {
              javaEqualsOverride = Some(meth.value)
            }
            if (ovmeth eq meta.javaHashCode) {
              javaHashCodeOverride = Some(meth.value)
            }
            if (ovmeth eq meta.scalaEquals) {
              scalaEqualsOverride = Some(meth.value)
            }
            if (ovmeth eq meta.scalaHashCode) {
              scalaHashCodeOverride = Some(meth.value)
            }
        }
        .headOption
        .getOrElse {
          if (meth.isVirtual) {
            entries += meth
            values += meth.value
          }
        }
    }
    // We short-circuit scala_== and scala_## to immeditately point to the
    // equals and hashCode implementation for the reference types to avoid
    // double virtual dispatch overhead.
    if (javaEqualsOverride.nonEmpty
        && scalaEqualsOverride.isEmpty
        && meta.scalaEquals.isVirtual) {
      values(index(meta.scalaEquals)) = javaEqualsOverride.get
    }
    if (javaHashCodeOverride.nonEmpty
        && scalaHashCodeOverride.isEmpty
        && meta.scalaHashCode.isVirtual) {
      values(index(meta.scalaHashCode)) = javaHashCodeOverride.get
    }
  }
  val ty: Type =
    Type.Array(Type.Ptr, values.length)
  val value: Val =
    Val.Array(Type.Ptr, values)
  def index(meth: Method): Int =
    meth.overrides
      .collectFirst {
        case ovmeth if ovmeth.inClass =>
          index(ovmeth)
      }
      .getOrElse {
        entries.indexOf(meth)
      }
}

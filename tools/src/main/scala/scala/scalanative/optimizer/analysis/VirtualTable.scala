package scala.scalanative
package optimizer
package analysis

import scala.collection.mutable
import ClassHierarchy._
import nir._

class VirtualTable(cls: Class) {
  val entries: mutable.UnrolledBuffer[Method] =
    cls.parent.fold {
      mutable.UnrolledBuffer.empty[Method]
    } { parent =>
      parent.vtable.entries.clone
    }
  val values: mutable.UnrolledBuffer[Val] =
    cls.parent.fold {
      mutable.UnrolledBuffer.empty[Val]
    } { parent =>
      parent.vtable.values.clone
    }
  cls.methods.foreach { meth =>
    meth.overrides
      .collect {
        case ovmeth if ovmeth.inClass =>
          values(index(ovmeth)) = meth.value
      }
      .headOption
      .getOrElse {
        if (meth.isVirtual) {
          entries += meth
          values += meth.value
        }
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

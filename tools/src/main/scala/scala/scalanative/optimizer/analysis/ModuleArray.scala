package scala.scalanative
package optimizer
package analysis

import scala.collection.mutable
import ClassHierarchy._
import nir._

class ModuleArray(top: Top) {
  val index   = mutable.Map.empty[Class, Int]
  val modules = mutable.UnrolledBuffer.empty[Class]
  top.classes.foreach { cls =>
    if (cls.isModule) {
      index(cls) = modules.size
      modules += cls
    }
  }
  val size: Int = modules.size
  val value: Val =
    Val.Array(Type.Ptr, Array.fill[Val](modules.length)(Val.Null))
}

package scala.scalanative
package lower

import scala.collection.mutable
import scalanative.nir._
import scalanative.sema._

class Metadata(top: Top, val dyns: Seq[String]) {
  import Metadata._

  val javaEquals    = top.nodes(javaEqualsName).asInstanceOf[Method]
  val javaHashCode  = top.nodes(javaHashCodeName).asInstanceOf[Method]
  val scalaEquals   = top.nodes(scalaEqualsName).asInstanceOf[Method]
  val scalaHashCode = top.nodes(scalaHashCodeName).asInstanceOf[Method]

  val rtti   = mutable.Map.empty[sema.Node, RuntimeTypeInformation]
  val vtable = mutable.Map.empty[sema.Class, VirtualTable]
  val layout = mutable.Map.empty[sema.Class, FieldLayout]
  val dynmap = mutable.Map.empty[sema.Class, DynamicHashMap]

  locally {
    top.traits.foreach { node =>
      rtti(node) = new RuntimeTypeInformation(this, node)
    }
    top.structs.foreach { node =>
      rtti(node) = new RuntimeTypeInformation(this, node)
    }
    top.classes.foreach { node =>
      vtable(node) = new VirtualTable(this, node)
      layout(node) = new FieldLayout(this, node)
      dynmap(node) = new DynamicHashMap(this, node, dyns)
      rtti(node) = new RuntimeTypeInformation(this, node)
    }
  }

  val tables      = new TraitDispatchTables(top)
  val moduleArray = new ModuleArray(top)
}

object Metadata {
  val javaEqualsName =
    Global.Member(Global.Top("java.lang.Object"),
                  "equals_java.lang.Object_bool")
  val javaHashCodeName =
    Global.Member(Global.Top("java.lang.Object"), "hashCode_i32")
  val scalaEqualsName =
    Global.Member(Global.Top("java.lang.Object"),
                  "scala$underscore$==_java.lang.Object_bool")
  val scalaHashCodeName =
    Global.Member(Global.Top("java.lang.Object"), "scala$underscore$##_i32")

  val depends =
    Seq(javaEqualsName, javaHashCodeName, scalaEqualsName, scalaHashCodeName)
}

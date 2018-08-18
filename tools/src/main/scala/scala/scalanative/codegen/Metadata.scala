package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir._
import scalanative.sema._
import scalanative.util.Stats

class Metadata(top: Top, val dyns: Seq[String]) {
  val rtti   = mutable.Map.empty[sema.Node, RuntimeTypeInformation]
  val vtable = mutable.Map.empty[sema.Class, VirtualTable]
  val layout = mutable.Map.empty[sema.Class, FieldLayout]
  val dynmap = mutable.Map.empty[sema.Class, DynamicHashMap]
  val ids    = mutable.Map.empty[sema.Scope, Int]
  val ranges = mutable.Map.empty[sema.Class, Range]

  initTraitIds()
  initStructIds()
  initClassIdsAndRanges()

  val tables      = new TraitDispatchTables(this, top)
  val moduleArray = new ModuleArray(top)

  initClassMetadata()
  initTraitMetadata()
  initStructMetadata()

  def initTraitIds(): Unit = {
    top.traits.zipWithIndex.foreach {
      case (node, id) =>
        ids(node) = id
    }
  }

  def initStructIds(): Unit = {
    top.structs.zipWithIndex.foreach {
      case (node, id) =>
        ids(node) = id
    }
  }

  def initClassIdsAndRanges(): Unit = {
    var id = 0

    def loop(node: Class): Unit = {
      val start = id
      id += 1
      node.subclasses.foreach { subcls =>
        if (subcls.parent == Some(node)) loop(subcls)
      }
      val end = id - 1
      ids(node) = start
      ranges(node) = start to end
    }

    loop(top.nodes(Rt.Object.name).asInstanceOf[Class])
  }

  def initClassMetadata(): Unit = {
    top.classes.foreach { node =>
      vtable(node) = new VirtualTable(this, node)
      layout(node) = new FieldLayout(this, node)
      dynmap(node) = new DynamicHashMap(this, node, dyns)
      rtti(node) = new RuntimeTypeInformation(this, node)
    }
  }

  def initTraitMetadata(): Unit = {
    top.traits.foreach { node =>
      rtti(node) = new RuntimeTypeInformation(this, node)
    }
  }

  def initStructMetadata(): Unit = {
    top.structs.foreach { node =>
      rtti(node) = new RuntimeTypeInformation(this, node)
    }
  }
}

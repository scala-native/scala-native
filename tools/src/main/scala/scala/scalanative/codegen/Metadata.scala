package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir._
import scalanative.sema._
import scalanative.util.Stats
import scalanative.linker.Class

class Metadata(val linked: linker.Result, proxies: Seq[Defn]) {
  val rtti   = mutable.Map.empty[linker.Info, RuntimeTypeInformation]
  val vtable = mutable.Map.empty[linker.Class, VirtualTable]
  val layout = mutable.Map.empty[linker.Class, FieldLayout]
  val dynmap = mutable.Map.empty[linker.Class, DynamicHashMap]
  val ids    = mutable.Map.empty[linker.ScopeInfo, Int]
  val ranges = mutable.Map.empty[linker.Class, Range]

  initTraitIds()
  initStructIds()
  initClassIdsAndRanges()

  val tables      = new TraitDispatchTables(this)
  val moduleArray = new ModuleArray(this)

  initClassMetadata()
  initTraitMetadata()
  initStructMetadata()

  def initTraitIds(): Unit = {
    linked.traits.zipWithIndex.foreach {
      case (node, id) =>
        ids(node) = id
    }
  }

  def initStructIds(): Unit = {
    linked.structs.zipWithIndex.foreach {
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

    loop(linked.infos(Rt.Object.name).asInstanceOf[Class])
  }

  def initClassMetadata(): Unit = {
    linked.classes.toArray.sortBy(ids(_)).foreach { node =>
      vtable(node) = new VirtualTable(this, node)
      layout(node) = new FieldLayout(this, node)
      dynmap(node) = new DynamicHashMap(this, node, proxies)
      rtti(node) = new RuntimeTypeInformation(this, node)
    }
  }

  def initTraitMetadata(): Unit = {
    linked.traits.foreach { node =>
      rtti(node) = new RuntimeTypeInformation(this, node)
    }
  }

  def initStructMetadata(): Unit = {
    linked.structs.foreach { node =>
      rtti(node) = new RuntimeTypeInformation(this, node)
    }
  }
}

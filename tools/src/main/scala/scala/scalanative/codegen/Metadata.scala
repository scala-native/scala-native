package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker.{Trait, Class}

class Metadata(val linked: linker.Result, proxies: Seq[Defn])(implicit
    val platform: PlatformInfo
) {
  implicit private def self: Metadata = this

  val layouts = new CommonMemoryLayouts()
  val rtti = mutable.Map.empty[linker.Info, RuntimeTypeInformation]
  val vtable = mutable.Map.empty[linker.Class, VirtualTable]
  val layout = mutable.Map.empty[linker.Class, FieldLayout]
  val dynmap = mutable.Map.empty[linker.Class, DynamicHashMap]
  val ids = mutable.Map.empty[linker.ScopeInfo, Int]
  val ranges = mutable.Map.empty[linker.Class, Range]

  val classes = initClassIdsAndRanges()
  val traits = initTraitIds()
  val moduleArray = new ModuleArray(this)
  val dispatchTable = new TraitDispatchTable(this)
  val hasTraitTables = new HasTraitTables(this)

  initClassMetadata()
  initTraitMetadata()

  def initTraitIds(): Seq[Trait] = {
    val traits =
      linked.infos.valuesIterator
        .collect { case info: Trait => info }
        .toIndexedSeq
        .sortBy(_.name.show)
    traits.zipWithIndex.foreach {
      case (node, id) =>
        ids(node) = id
    }
    traits
  }

  def initClassIdsAndRanges(): Seq[Class] = {
    val out = mutable.UnrolledBuffer.empty[Class]
    var id = 0

    def loop(
        node: Class,
        topLevelSubclassOrdering: Array[Class] => Array[Class]
    ): Unit = {
      out += node
      val start = id
      id += 1
      topLevelSubclassOrdering(
        node.subclasses
          .filter(_.parent.contains(node))
          .toArray
      ).foreach(loop(_, identity))
      val end = id - 1
      ids(node) = start
      ranges(node) = start to end
    }

    def fromRootClass(
        symbol: nir.Global.Top,
        ordering: Array[Class] => Array[Class] = identity
    ) =
      loop(
        node = linked.infos(symbol).asInstanceOf[Class],
        topLevelSubclassOrdering = ordering
      )

    Rt.PrimitiveTypes.foreach(fromRootClass(_))
    fromRootClass(
      Rt.Object.name.asInstanceOf[Global.Top],
      ordering = subclasses => {
        val (arrays, other) =
          subclasses.partition(_.name == Rt.GenericArray.name)
        arrays ++ other
      }
    )

    out.toSeq
  }

  def initClassMetadata(): Unit = {
    classes.foreach { node =>
      vtable(node) = new VirtualTable(node)
      layout(node) = new FieldLayout(node)
      if (layouts.ClassRtti.usesDynMap) {
        dynmap(node) = new DynamicHashMap(node, proxies)
      }
      rtti(node) = new RuntimeTypeInformation(node)
    }
  }

  def initTraitMetadata(): Unit = {
    traits.foreach { node =>
      rtti(node) = new RuntimeTypeInformation(node)
    }
  }
}

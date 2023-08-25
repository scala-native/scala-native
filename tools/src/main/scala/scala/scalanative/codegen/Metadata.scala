package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker.{Trait, Class, ReachabilityAnalysis}

class Metadata(
    val analysis: ReachabilityAnalysis.Result,
    val config: build.NativeConfig,
    proxies: Seq[Defn]
)(implicit val platform: PlatformInfo) {
  implicit private def self: Metadata = this

  final val usesLockWords = platform.isMultithreadingEnabled
  val lockWordType = if (usesLockWords) Some(Type.Ptr) else None
  private[codegen] val lockWordVals = lockWordType.map(_ => Val.Null).toList

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
      analysis.infos.valuesIterator
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

    def loop(node: Class): Unit = {
      out += node
      val start = id
      id += 1
      val directSubclasses =
        node.subclasses.filter(_.parent == Some(node)).toArray
      directSubclasses.sortBy(_.name.show).foreach { subcls => loop(subcls) }
      val end = id - 1
      ids(node) = start
      ranges(node) = start to end
    }

    loop(analysis.infos(Rt.Object.name).asInstanceOf[Class])

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

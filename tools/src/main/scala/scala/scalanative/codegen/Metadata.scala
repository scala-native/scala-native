package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.linker.{Trait, Class, ReachabilityAnalysis}

private[scalanative] class Metadata(
    val analysis: ReachabilityAnalysis.Result,
    val buildConfig: build.Config,
    proxies: Seq[nir.Defn]
)(implicit val platform: PlatformInfo) {
  def config: build.NativeConfig = buildConfig.compilerConfig
  implicit private def self: Metadata = this

  final val usesLockWords = platform.isMultithreadingEnabled
  val lockWordType = if (usesLockWords) Some(nir.Type.Ptr) else None
  private[codegen] val lockWordVals = lockWordType.map(_ => nir.Val.Null).toList

  val layouts = new CommonMemoryLayouts()
  val rtti = mutable.Map.empty[linker.Info, RuntimeTypeInformation]
  val vtable = mutable.Map.empty[linker.Class, VirtualTable]
  val itable = mutable.Map.empty[linker.Class, ITable]
  val layout = mutable.Map.empty[linker.Class, FieldLayout]
  val dynmap = mutable.Map.empty[linker.Class, DynamicHashMap]
  val ids = mutable.Map.empty[linker.ScopeInfo, Int]
  val ranges = mutable.Map.empty[linker.Class, Range]

  val classes = initClassIdsAndRanges()
  val traits = initTraitIds()
  val moduleArray = new ModuleArray(this)

  initTraitMetadata()
  initClassMetadata()

  def initTraitIds(): Seq[Trait] = {
    val traits = analysis.infos.valuesIterator.collect {
      case info: Trait => info
    }.toIndexedSeq
    new TraitsUniverse(traits).assignIds(ids)
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
        node = analysis.infos(symbol).asInstanceOf[Class],
        topLevelSubclassOrdering = ordering
      )

    nir.Rt.PrimitiveTypes.foreach(fromRootClass(_))
    fromRootClass(
      nir.Rt.Object.name,
      ordering = subclasses => {
        val (arrays, other) =
          subclasses.partition(_.name == nir.Rt.GenericArray.name)
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
      itable(node) = ITable.build(node)
      rtti(node) = new RuntimeTypeInformation(node)
    }
  }

  def initTraitMetadata(): Unit = {
    traits.foreach { node =>
      rtti(node) = new RuntimeTypeInformation(node)
    }
  }
}

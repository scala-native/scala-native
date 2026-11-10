package scala.scalanative
package codegen

import scala.collection.mutable

import scalanative.linker.{Class, ReachabilityAnalysis, ScopeInfo, Trait}

private[scalanative] class Metadata(
    val analysis: ReachabilityAnalysis.Result,
    val buildConfig: build.Config,
    proxies: Seq[nir.Defn]
)(implicit val platform: PlatformInfo) {
  def config: build.NativeConfig = buildConfig.compilerConfig
  private implicit def self: Metadata = this

  final val usesLockWords = platform.isMultithreadingEnabled
  val lockWordType = if (usesLockWords) Some(nir.Type.Ptr) else None
  private[codegen] val lockWordVals = lockWordType.map(_ => nir.Val.Null).toList

  val layouts = new CommonMemoryLayouts()
  val globalSections = new GlobalSections
  val rtti = mutable.Map.empty[linker.Info, RuntimeTypeInformation]
  val vtable = mutable.Map.empty[linker.Class, VirtualTable]
  val itable = mutable.Map.empty[linker.Class, ITable]
  val layout = mutable.Map.empty[linker.Class, FieldLayout]
  val dynmap = mutable.Map.empty[linker.Class, DynamicHashMap]
  private val atHomeSlotCache = mutable.Map.empty[Trait, Boolean]

  val classes: Seq[Class] = initClasses()
  val traits: Seq[Trait] = analysis.infos.valuesIterator
    .collect { case info: Trait => info }
    .toIndexedSeq
    .sortBy(_.name.mangle)

  checkDynsigKeys()
  initTraitMetadata()
  initClassMetadata()

  /** Number of superclasses: 0 for java.lang.Object and primitives */
  def superclassCount(cls: Class): Int =
    cls.parent.fold(0)(superclassCount(_) + 1)

  /** The class followed by its ancestors, up to the root class */
  def ancestors(cls: Class): List[Class] =
    cls :: cls.parent.fold(List.empty[Class])(ancestors)

  /** Every class of the program stores `trt` in the itable slot where its
   *  lookup starts, so a single probe decides any lookup. A whole-program fact,
   *  used by generated code and recorded in the trait's RTTI flags.
   */
  def alwaysAtHomeSlot(trt: Trait): Boolean =
    atHomeSlotCache.getOrElseUpdate(
      trt,
      trt.implementors.forall(cls =>
        itable.get(cls).forall(_.atHomeSlot.contains(trt))
      )
    )

  /** Traits implemented by the class or trait, ordered by name */
  def interfaces(info: ScopeInfo): Seq[Trait] =
    info.linearized
      .collect { case trt: Trait if trt != info => trt }
      .distinct
      .sortBy(_.name.mangle)

  // Classes in pre-order of the class hierarchy, primitive types first
  private def initClasses(): Seq[Class] = {
    val out = mutable.UnrolledBuffer.empty[Class]

    def loop(node: Class): Unit = {
      out += node
      node.subclasses
        .filter(_.parent.contains(node))
        .toSeq
        .sortBy(_.name.mangle)
        .foreach(loop)
    }

    nir.Rt.PrimitiveTypes.foreach { name =>
      loop(analysis.infos(name).asInstanceOf[Class])
    }
    loop(analysis.infos(nir.Rt.Object.name).asInstanceOf[Class])
    out.toSeq
  }

  /* Reflective calls look methods up by a hash of their signature, so two
   * signatures with the same key could dispatch to a wrong method.
   */
  private def checkDynsigKeys(): Unit = {
    val sigs = (analysis.dynsigs ++ proxies.map(_.name).collect {
      case name: nir.Global.Member => name.sig
    }).distinct
    sigs.groupBy(StableHash.dynsigKey).foreach {
      case (key, colliding) if colliding.size > 1 =>
        throw new build.BuildException(
          s"Reflective method signatures ${colliding.map(_.show).mkString(", ")} share the dispatch key $key"
        )
      case _ => ()
    }
  }

  def initClassMetadata(): Unit = {
    classes.foreach { node =>
      vtable(node) = new VirtualTable(node)
      layout(node) = new FieldLayout(node)
      dynmap(node) = new DynamicHashMap(node, proxies)
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

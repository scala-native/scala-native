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

  // Superclasses from the root, then the class itself, see lineage
  private val lineages = mutable.Map.empty[Class, Vector[Class]]
  val classes: Seq[Class] = initClasses()
  val traits: Seq[Trait] = analysis.infos.valuesIterator
    .collect { case info: Trait => info }
    .toIndexedSeq
    .sortBy(_.name.mangle)
  private val traitKeys: Map[Trait, Int] =
    traits.map(trt => trt -> StableHash.traitKey(trt)).toMap

  checkDynsigKeys()
  initTraitMetadata()
  initClassMetadata()

  // Traits some class does not store in the itable slot where their lookup
  // starts. Computed before lowering, which reads it from several threads.
  private val displacedTraits: Set[Trait] =
    itable.valuesIterator.flatMap(_.displaced).toSet
  // Traits implemented by some class whose itable shifts the trait keys
  private val shiftedTraits: Set[Trait] =
    classes.iterator
      .filter(itable(_).shift != 0)
      .flatMap(interfaces(_))
      .toSet

  /** Proxies of reflectively called methods, registered at startup */
  lazy val reflectiveProxies: nir.Defn.Const =
    ReflectiveProxyTable.defn(proxies)

  /** Superclasses of the class from the root, then the class itself */
  def lineage(cls: Class): Vector[Class] = lineages(cls)

  /** Number of superclasses: 0 for java.lang.Object and primitives */
  def superclassCount(cls: Class): Int = lineage(cls).size - 1

  /** Whether `cls` is `ancestor` or one of its subclasses: `ancestor` has
   *  `depth` superclasses.
   */
  def isAt(cls: Class, depth: Int, ancestor: nir.Global.Top): Boolean =
    lineage(cls).lift(depth).exists(_.name == ancestor)

  /** WeakReference, SoftReference or PhantomReference for reference objects:
   *  java.lang.ref.Reference, a direct subclass of Object, is extended only by
   *  them.
   */
  def referenceKind(cls: Class): Option[nir.Global.Top] =
    if (!isAt(cls, 1, FieldLayout.Reference)) None
    else lineage(cls).lift(2).map(_.name)

  /** Every class of the program stores `trt` in the itable slot where its
   *  lookup starts, so a single probe decides any lookup. A whole-program fact,
   *  used by generated code and recorded in the trait's RTTI flags.
   */
  def alwaysAtHomeSlot(trt: Trait): Boolean = !displacedTraits.contains(trt)

  /** No class of the program shifts the key of `trt` in its itable, so lookups
   *  start at key & mask. A whole-program fact used by generated code.
   */
  def neverShifted(trt: Trait): Boolean = !shiftedTraits.contains(trt)

  /** Key of a trait in itables, see StableHash.traitKey */
  def traitKey(trt: Trait): Int = traitKeys(trt)

  /** Traits implemented by the class or trait, ordered by name */
  def interfaces(info: ScopeInfo): Seq[Trait] =
    info.linearized
      .collect { case trt: Trait if trt != info => trt }
      .distinct
      .sortBy(_.name.mangle)

  // Classes in pre-order of the class hierarchy, primitive types first
  private def initClasses(): Seq[Class] = {
    val out = mutable.UnrolledBuffer.empty[Class]

    def loop(node: Class, parentLineage: Vector[Class]): Unit = {
      out += node
      val lineage = parentLineage :+ node
      lineages(node) = lineage
      node.subclasses
        .filter(_.parent.contains(node))
        .toSeq
        .sortBy(_.name.mangle)
        .foreach(loop(_, lineage))
    }

    nir.Rt.PrimitiveTypes.foreach { name =>
      loop(analysis.infos(name).asInstanceOf[Class], Vector.empty)
    }
    loop(analysis.infos(nir.Rt.Object.name).asInstanceOf[Class], Vector.empty)
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

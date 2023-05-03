package scala.scalanative.nscplugin

import scala.scalanative.util
import scala.scalanative.nir

import dotty.tools.dotc.{CompilationUnit, report}
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.Trees._
import dotty.tools.dotc.core
import core.Contexts._
import core.Symbols._

import scala.collection.mutable
import scala.language.implicitConversions

class NirCodeGen(val settings: GenNIR.Settings)(using ctx: Context)
    extends NirGenStat
    with NirGenExpr
    with NirGenType
    with NirGenName
    with NirGenUtil
    with GenReflectiveInstantisation
    with GenNativeExports:
  import tpd._
  import nir._

  protected val defnNir = NirDefinitions.get
  protected val nirPrimitives = new NirPrimitives()
  protected val positionsConversions = new NirPositions(settings.sourceURIMaps)
  protected val cachedMethodSig =
    collection.mutable.Map.empty[(Symbol, Boolean), nir.Type.Function]

  protected val curClassSym = new util.ScopedVar[ClassSymbol]
  protected val curClassFresh = new util.ScopedVar[nir.Fresh]

  protected val curMethodSym = new util.ScopedVar[Symbol]
  protected val curMethodSig = new util.ScopedVar[nir.Type]
  protected val curMethodInfo = new util.ScopedVar[CollectMethodInfo]
  protected val curMethodEnv = new util.ScopedVar[MethodEnv]
  protected val curMethodLabels = new util.ScopedVar[MethodLabelsEnv]
  protected val curMethodThis = new util.ScopedVar[Option[nir.Val]]
  protected val curMethodIsExtern = new util.ScopedVar[Boolean]
  protected var curMethodUsesLinktimeResolvedValues = false

  protected val curFresh = new util.ScopedVar[nir.Fresh]
  protected val curUnwindHandler = new util.ScopedVar[Option[nir.Local]]

  protected val lazyValsAdapter = AdaptLazyVals(defnNir)

  protected def unwind(implicit fresh: Fresh): Next =
    curUnwindHandler.get
      .fold[Next](Next.None) { handler =>
        val exc = Val.Local(fresh(), nir.Rt.Object)
        Next.Unwind(exc, Next.Label(handler, Seq(exc)))
      }

  def run(): Unit = {
    try {
      genCompilationUnit(ctx.compilationUnit)
    } finally {
      generatedDefns.clear()
      generatedMirrorClasses.clear()
      reflectiveInstantiationBuffers.clear()
      cachedMethodSig.clear()
    }
  }

  private def genCompilationUnit(cunit: CompilationUnit): Unit = {
    lazyValsAdapter.clean()
    def collectTypeDefs(tree: Tree): List[TypeDef] = {
      tree match {
        case EmptyTree            => Nil
        case PackageDef(_, stats) => stats.flatMap(collectTypeDefs)
        case cd: TypeDef          => cd :: Nil
        case _: ValDef            => Nil // module instance
      }
    }

    collectTypeDefs(cunit.tpdTree)
      .foreach(genClass)

    generatedDefns.toSeq
      .groupBy(defn => getFileFor(defn.name.top))
      .foreach(genIRFile(_, _))

    reflectiveInstantiationBuffers
      .groupMapReduce(buf => getFileFor(buf.name.top))(_.toSeq)(_ ++ _)
      .foreach(genIRFile(_, _))

    if (generatedMirrorClasses.nonEmpty) {
      // Ported from Scala.js
      /* #4148 Add generated static forwarder classes, except those that
       * would collide with regular classes on case insensitive file systems.
       */

      /* I could not find any reference anywhere about what locale is used
       * by case insensitive file systems to compare case-insensitively.
       * In doubt, force the English locale, which is probably going to do
       * the right thing in virtually all cases (especially if users stick
       * to ASCII class names), and it has the merit of being deterministic,
       * as opposed to using the OS' default locale.
       * The JVM backend performs a similar test to emit a warning for
       * conflicting top-level classes. However, it uses `toLowerCase()`
       * without argument, which is not deterministic.
       */
      def caseInsensitiveNameOf(classDef: nir.Defn.Class): String =
        classDef.name.mangle.toLowerCase(java.util.Locale.ENGLISH)

      val generatedCaseInsensitiveNames =
        generatedDefns.collect {
          case cls: Defn.Class => caseInsensitiveNameOf(cls)
        }.toSet

      for ((site, staticCls) <- generatedMirrorClasses) {
        val MirrorClass(classDef, forwarders) = staticCls
        val caseInsensitiveName = caseInsensitiveNameOf(classDef)
        if (!generatedCaseInsensitiveNames.contains(caseInsensitiveName)) {
          val file = getFileFor(classDef.name)
          val defs = classDef +: forwarders
          genIRFile(file, defs)
        } else {
          report.warning(
            s"Not generating the static forwarders of ${classDef.name} " +
              "because its name differs only in case from the name of another class or trait in this compilation unit.",
            site.srcPos
          )
        }
      }
    }
  }

  private def genIRFile(
      outfile: dotty.tools.io.AbstractFile,
      defns: Seq[nir.Defn]
  ): Unit = {
    import scalanative.nir.serialization.serializeBinary
    val output = outfile.bufferedOutput
    try {
      serializeBinary(defns, output)
    } finally {
      output.close()
    }
  }

  private def getFileFor(ownerName: nir.Global): dotty.tools.io.AbstractFile = {
    val nir.Global.Top(className) = ownerName: @unchecked
    val outputDirectory = ctx.settings.outputDir.value
    val pathParts = className.split('.')
    val dir = pathParts.init.foldLeft(outputDirectory)(_.subdirectoryNamed(_))
    val filename = pathParts.last
    dir.fileNamed(filename + ".nir")
  }

  class MethodLabelsEnv(val fresh: nir.Fresh) {
    private val entries, exits = mutable.Map.empty[Symbol, Local]
    private val exitTypes = mutable.Map.empty[Local, nir.Type]

    def enterLabel(ld: Labeled): (nir.Local, nir.Local) = {
      val sym = ld.bind.symbol
      val entry, exit = fresh()
      entries += sym -> entry
      exits += sym -> exit
      (entry, exit)
    }

    def resolveEntry(sym: Symbol): nir.Local = entries(sym)
    def resolveEntry(label: Labeled): nir.Local = entries(label.bind.symbol)

    def resolveExit(sym: Symbol): nir.Local = exits(sym)
    def resolveExit(label: Labeled): nir.Local = exits(label.bind.symbol)

    def enterExitType(local: nir.Local, exitType: nir.Type): Unit =
      exitTypes += local -> exitType
    def resolveExitType(local: nir.Local): nir.Type = exitTypes(local)
  }

  class MethodEnv(val fresh: nir.Fresh) {
    private val env = mutable.Map.empty[Symbol, nir.Val]

    def enter(sym: Symbol, value: nir.Val): Unit = env += sym -> value
    def enterLabel(ld: Labeled): nir.Local = {
      val local = fresh()
      enter(ld.bind.symbol, nir.Val.Local(local, nir.Type.Ptr))
      local
    }

    def resolve(sym: Symbol): Val = env(sym)
    def resolveLabel(ld: Labeled): Local = {
      val Val.Local(n, Type.Ptr) = resolve(ld.bind.symbol): @unchecked
      n
    }
  }

  class CollectMethodInfo extends TreeTraverser {
    var mutableVars = Set.empty[Symbol]
    var labels = Set.empty[Labeled]

    override def traverse(tree: Tree)(using Context): Unit = {
      tree match {
        case label: Labeled =>
          labels += label
        case Assign(ident: Ident, _) =>
          desugarIdent(ident) match {
            case id: Ident => mutableVars += id.symbol
            case _         => ()
          }
        case _ => ()
      }
      traverseChildren(tree)
    }

    def collect(tree: Tree): CollectMethodInfo = {
      traverse(tree)
      this
    }
  }

end NirCodeGen

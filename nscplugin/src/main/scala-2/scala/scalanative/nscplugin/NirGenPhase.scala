package scala.scalanative
package nscplugin

import java.nio.file.{Path => JPath, Paths => JPaths}
import java.util.stream.{Stream => JStream}
import java.util.function.{Consumer => JConsumer}
import scala.collection.mutable
import scala.language.implicitConversions
import nir.Defn.Define.DebugInfo
import scala.scalanative.util.ScopedVar.scoped
import scala.tools.nsc.plugins._
import scala.tools.nsc.{Global, util => _, _}
import scala.reflect.internal.util.{SourceFile => CompilerSourceFile}
import scala.tools.nsc

abstract class NirGenPhase[G <: Global with Singleton](override val global: G)
    extends NirPhase[G](global)
    with NirGenStat[G]
    with NirGenExpr[G]
    with NirGenUtil[G]
    with NirGenFile[G]
    with NirGenType[G]
    with NirGenName[G]
    with NirCompat[G]
    with NirGenExports[G] {

  import global._
  import definitions._
  import nirAddons._

  val phaseName = "scalanative-genNIR"

  protected val curClassSym = new util.ScopedVar[Symbol]
  protected val curClassFresh = new util.ScopedVar[nir.Fresh]
  protected val curMethodSym = new util.ScopedVar[Symbol]
  protected val curMethodSig = new util.ScopedVar[nir.Type]
  protected val curMethodInfo = new util.ScopedVar[CollectMethodInfo]
  protected val curMethodEnv = new util.ScopedVar[MethodEnv]
  protected val curMethodThis = new util.ScopedVar[Option[nir.Val]]
  protected val curMethodLocalNames =
    new util.ScopedVar[mutable.Map[nir.Local, nir.LocalName]]
  protected val curMethodIsExtern = new util.ScopedVar[Boolean]
  protected val curFresh = new util.ScopedVar[nir.Fresh]
  protected val curUnwindHandler = new util.ScopedVar[Option[nir.Local]]
  protected val curStatBuffer = new util.ScopedVar[StatBuffer]
  protected val cachedMethodSig =
    collection.mutable.Map.empty[(Symbol, Boolean), nir.Type.Function]

  protected var curScopes =
    new util.ScopedVar[mutable.Set[DebugInfo.LexicalScope]]
  protected val curFreshScope = new util.ScopedVar[nir.Fresh]
  protected val curScopeId = new util.ScopedVar[nir.ScopeId]
  implicit protected def getScopeId: nir.ScopeId = curScopeId.get
  protected def initFreshScope(rhs: Tree) = nir.Fresh(rhs match {
    case _: Block => -1L // Conpensate the top-level block
    case _        => 0L
  })

  protected def unwind(implicit fresh: nir.Fresh): nir.Next =
    curUnwindHandler.get.fold[nir.Next](nir.Next.None) { handler =>
      val exc = nir.Val.Local(fresh(), nir.Rt.Object)
      nir.Next.Unwind(exc, nir.Next.Label(handler, Seq(exc)))
    }

  override def newPhase(prev: Phase): StdPhase =
    new NirCodePhase(prev)

  class NirCodePhase(prev: Phase) extends StdPhase(prev) {
    override def run(): Unit = {
      scalaPrimitives.init()
      nirPrimitives.init()
      super.run()
    }

    override def apply(cunit: CompilationUnit): Unit = try {
      val classDefs = mutable.UnrolledBuffer.empty[ClassDef]

      def collectClassDefs(tree: Tree): Unit = tree match {
        case EmptyTree =>
          ()
        case PackageDef(_, stats) =>
          stats.foreach(collectClassDefs)
        case cd: ClassDef =>
          val sym = cd.symbol
          if (isPrimitiveValueClass(sym) || (sym == ArrayClass)) {
            ()
          } else {
            classDefs += cd
          }
      }

      collectClassDefs(cunit.body)

      val statBuffer = new StatBuffer

      scoped(
        curStatBuffer := statBuffer
      ) {
        classDefs.foreach(cd => statBuffer.genClass(cd))
      }

      val reflectiveInstFiles = reflectiveInstantiationInfo.map {
        reflectiveInstBuf =>
          val path = genPathFor(cunit, reflectiveInstBuf.name)
          (path, reflectiveInstBuf.toSeq)
      }.toMap

      val allRegularDefns = if (generatedMirrorClasses.isEmpty) {
        /* Fast path, applicable under -Xno-forwarders, as well as when all
         * the `object`s of a compilation unit have a companion class.
         */
        statBuffer.toSeq
      } else {
        val regularDefns = statBuffer.toSeq.toList

        /* #4148 Add generated static forwarder classes, except those that
         * would collide with regular classes on case insensitive file
         * systems.
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
          regularDefns.collect {
            case cls: nir.Defn.Class => caseInsensitiveNameOf(cls)
          }.toSet

        val staticForwarderDefns: List[nir.Defn] =
          generatedMirrorClasses
            .collect {
              case (site, MirrorClass(classDef, forwarders)) =>
                val name = caseInsensitiveNameOf(classDef)
                if (!generatedCaseInsensitiveNames.contains(name)) {
                  classDef +: forwarders
                } else {
                  global.reporter.warning(
                    site.pos,
                    s"Not generating the static forwarders of ${classDef.name.show} " +
                      "because its name differs only in case from the name of another class or " +
                      "trait in this compilation unit."
                  )
                  Nil
                }
            }
            .flatten
            .toList

        regularDefns ::: staticForwarderDefns
      }

      val regularFiles = allRegularDefns.toSeq
        .groupBy(_.name.top)
        .map {
          case (ownerName, defns) =>
            (genPathFor(cunit, ownerName), defns)
        }
      val allFiles = regularFiles ++ reflectiveInstFiles

      JStream
        .of(allFiles.toSeq: _*)
        .parallel()
        .forEach { case (path, stats) => genIRFile(path, stats) }
    } finally {
      generatedMirrorClasses.clear()
      cachedMethodSig.clear()
    }
  }

  protected implicit def toNirPosition(pos: global.Position): nir.Position = {
    if (!pos.isDefined) nir.Position.NoPosition
    else
      nir.Position(
        source = nirPositionCachedConverter.toNIRSource(pos.source),
        line = pos.line - 1,
        column = pos.column - 1
      )
  }

  private[this] object nirPositionCachedConverter {
    import scala.reflect.internal.util._
    private[this] var lastNscSource: CompilerSourceFile = _
    private[this] var lastNIRSource: nir.SourceFile = _

    def toNIRSource(nscSource: CompilerSourceFile): nir.SourceFile = {
      if (nscSource != lastNscSource) {
        lastNIRSource = convert(nscSource)
        lastNscSource = nscSource
      }
      lastNIRSource
    }

    /** Returns the relative path of `source` within the `reference` path
     *
     *  It returns the absolute path of `source` if it is not contained in
     *  `reference`.
     */
    def relativePath(source: CompilerSourceFile, reference: JPath): String = {
      val file = source.file
      val jfile = file.file
      if (jfile eq null)
        file.path // repl and other custom tests use abstract files with no path
      else {
        val sourcePath = jfile.toPath.toAbsolutePath.normalize
        val refPath = reference.normalize
        if (sourcePath.startsWith(refPath)) {
          val path = refPath.relativize(sourcePath)
          import scala.collection.JavaConverters._
          path.iterator.asScala.mkString("/"): @scala.annotation.nowarn
        } else sourcePath.toString
      }
    }

    private val sourceRoot = JPaths
      .get {
        val sourcePath = settings.sourcepath.value
        if (sourcePath.isEmpty) settings.rootdir.value
        else sourcePath
      }
      .toAbsolutePath()

    private[this] def convert(
        nscSource: CompilerSourceFile
    ): nir.SourceFile = {
      if (nscSource.file.isVirtual) nir.SourceFile.Virtual
      else {
        val absSourcePath = nscSource.file.absolute.file.toPath()
        val relativeTo = scalaNativeOpts.positionRelativizationPaths
          .find(absSourcePath.startsWith(_))
          .map(_.toAbsolutePath())
          .getOrElse(sourceRoot)
        nir.SourceFile.Relative(relativePath(nscSource, relativeTo))
      }
    }
  }
}

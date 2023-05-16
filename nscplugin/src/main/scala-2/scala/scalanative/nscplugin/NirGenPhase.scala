package scala.scalanative
package nscplugin

import java.nio.file.{Path => JPath}
import java.util.stream.{Stream => JStream}
import java.util.function.{Consumer => JConsumer}
import scala.collection.mutable
import scala.language.implicitConversions
import scala.scalanative.nir._
import scala.scalanative.util.ScopedVar.scoped
import scala.tools.nsc.plugins._
import scala.tools.nsc.{Global, util => _, _}

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
  protected val curMethodThis = new util.ScopedVar[Option[Val]]
  protected val curMethodIsExtern = new util.ScopedVar[Boolean]
  protected val curFresh = new util.ScopedVar[nir.Fresh]
  protected val curUnwindHandler = new util.ScopedVar[Option[nir.Local]]
  protected val curStatBuffer = new util.ScopedVar[StatBuffer]
  protected val cachedMethodSig =
    collection.mutable.Map.empty[(Symbol, Boolean), nir.Type.Function]
  protected var curMethodUsesLinktimeResolvedValues = false

  protected def unwind(implicit fresh: Fresh): Next =
    curUnwindHandler.get.fold[Next](Next.None) { handler =>
      val exc = Val.Local(fresh(), nir.Rt.Object)
      Next.Unwind(exc, Next.Label(handler, Seq(exc)))
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

  protected implicit def toNirPosition(pos: Position): nir.Position = {
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
    private[this] var lastNscSource: SourceFile = _
    private[this] var lastNIRSource: nir.Position.SourceFile = _

    def toNIRSource(nscSource: SourceFile): nir.Position.SourceFile = {
      if (nscSource != lastNscSource) {
        lastNIRSource = convert(nscSource)
        lastNscSource = nscSource
      }
      lastNIRSource
    }

    private[this] def convert(
        nscSource: SourceFile
    ): nir.Position.SourceFile = {
      nscSource.file.file match {
        case null =>
          new java.net.URI(
            "virtualfile", // Pseudo-Scheme
            nscSource.file.path, // Scheme specific part
            null // Fragment
          )
        case file =>
          val srcURI = file.toURI
          def matches(pat: java.net.URI) = pat.relativize(srcURI) != srcURI

          scalaNativeOpts.sourceURIMaps
            .collectFirst {
              case ScalaNativeOptions.URIMap(from, to) if matches(from) =>
                val relURI = from.relativize(srcURI)
                to.fold(relURI)(_.resolve(relURI))
            }
            .getOrElse(srcURI)
      }
    }
  }
}

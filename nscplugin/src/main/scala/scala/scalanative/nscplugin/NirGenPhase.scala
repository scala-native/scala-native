package scala.scalanative
package nscplugin

import java.nio.file.Path

import scala.collection.mutable
import scala.collection.parallel.mutable.ParArray
import scala.scalanative.nir._
import scala.scalanative.util.ScopedVar.scoped
import scala.tools.nsc.plugins._
import scala.tools.nsc.{util => _, _}

abstract class NirGenPhase
    extends PluginComponent
    with NirGenStat
    with NirGenExpr
    with NirGenUtil
    with NirGenFile
    with NirGenType
    with NirGenName
    with NirCompat {
  val nirAddons: NirGlobalAddons {
    val global: NirGenPhase.this.global.type
  }

  import global._
  import definitions._
  import nirAddons._

  val phaseName = "nir"

  protected val curClassSym       = new util.ScopedVar[Symbol]
  protected val curClassFresh     = new util.ScopedVar[nir.Fresh]
  protected val curMethodSym      = new util.ScopedVar[Symbol]
  protected val curMethodSig      = new util.ScopedVar[nir.Type]
  protected val curMethodInfo     = new util.ScopedVar[CollectMethodInfo]
  protected val curMethodEnv      = new util.ScopedVar[MethodEnv]
  protected val curMethodThis     = new util.ScopedVar[Option[Val]]
  protected val curMethodIsExtern = new util.ScopedVar[Boolean]
  protected val curFresh          = new util.ScopedVar[nir.Fresh]
  protected val curUnwindHandler  = new util.ScopedVar[Option[nir.Local]]
  protected val curStatBuffer     = new util.ScopedVar[StatBuffer]

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

    override def apply(cunit: CompilationUnit): Unit = {
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

      val files = statBuffer.toSeq.groupBy(defn => defn.name.top).map {
        case (ownerName, defns) =>
          (genPathFor(cunit, ownerName), defns)
      }

      val reflectiveInstFiles = reflectiveInstantiationInfo.map {
        reflectiveInstBuf =>
          val path = genPathFor(cunit, reflectiveInstBuf.name.id)
          (path, reflectiveInstBuf.toSeq)
      }.toMap

      ParArray.handoff((files ++ reflectiveInstFiles).toArray).foreach {
        case (path, stats) =>
          genIRFile(path, stats)
      }
    }
  }
}

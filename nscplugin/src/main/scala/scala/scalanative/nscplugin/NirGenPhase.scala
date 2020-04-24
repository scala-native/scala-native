package scala.scalanative
package nscplugin

import java.nio.file.Path

import scala.collection.mutable
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
    with NirGenName {
  val nirAddons: NirGlobalAddons {
    val global: NirGenPhase.this.global.type
  }

  import global._
  import definitions._
  import nirAddons._

  val phaseName = "nir"

  protected val curClassSym       = new util.ScopedVar[Symbol]
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
      val files     = mutable.UnrolledBuffer.empty[(Path, Seq[nir.Defn])]

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

      def genClass(cd: ClassDef): Unit = {
        val path   = genPathFor(cunit, cd.symbol)
        val buffer = new StatBuffer

        scoped(
          curStatBuffer := buffer
        ) {
          buffer.genClass(cd)
          files += ((path, buffer.toSeq))
        }
      }

      collectClassDefs(cunit.body)
      classDefs.foreach(genClass)

      // Add the reflective instantiation loaders to the file list
      reflectiveInstantiationInfo.foreach { reflInstBuf =>
        val path = genPathFor(cunit, reflInstBuf.name.id)
        files += ((path, reflInstBuf.toSeq))
      }

      files.par.foreach {
        case (path, stats) =>
          genIRFile(path, stats)
      }
    }
  }
}

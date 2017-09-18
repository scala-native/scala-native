package scala.scalanative
package nscplugin

import scala.tools.nsc.plugins._
import scala.tools.nsc.{Phase, transform}
import scala.reflect.internal.Flags._
import util.ScopedVar.scoped
import util.ScopedVar

abstract class PreNirSanityCheck
  extends NirPhase with NirPluginComponent {

  import global._
  import definitions._

  val nirAddons: NirGlobalAddons {
    val global: PreNirSanityCheck.this.global.type
  }

  import nirAddons._
  import nirDefinitions._
  import SimpleType.{fromType, fromSymbol}

  val phaseName: String = "sanitycheck"
  override def description: String = "prepare ASTs for NIR"

  override def newPhase(prev: Phase): StdPhase =
    new SanityCheckPhase(prev)



  class SanityCheckPhase(prev: Phase) extends StdPhase(prev) {
    private val curClassSym   = new util.ScopedVar[Symbol]
    private val curMethSym = new util.ScopedVar[Symbol]
    private val curValSym = new util.ScopedVar[Symbol]

    override def apply(cunit: CompilationUnit): Unit = {
      def verifyDefs(tree: Tree): List[Tree] = {
        tree match {
          case EmptyTree => Nil
          case PackageDef(_, stats) => stats flatMap verifyDefs
          case cd: ClassDef => cd :: Nil
          case md: ModuleDef => md :: Nil
        }
      }

      //println("CUNIT: " + cunit.body)

      cunit.body foreach verify
    }

    def verify(tree: Tree): Unit = tree match {
      case cd: ClassDef =>
        if (cd.symbol.isExternNonModule) {
          val nonExternParents =
            (cd.symbol.tpe.parents.zip(cd.impl.parents)).
              filterNot(p => (p._1 == AnyRefTpe) || p._2.symbol.isExternNonModule)
          nonExternParents foreach { parent =>
            reporter.error(
              parent._2.pos,
              s"extern ${symToName(cd.symbol)} may only have extern parents")
          }
        }
        verifyClass(cd)
      case md@ModuleDef(_, _, impl) =>
        if (md.symbol.isExternModule) {
          val nonExternParents =
            (md.symbol.tpe.parents.zip(impl.parents)).
              filterNot(p => (p._1 == AnyRefTpe) || p._2.symbol.isExternNonModule)
          nonExternParents foreach { parent =>
            reporter.error(
              parent._2.pos,
              "extern objects may only have extern parents")
          }
        }
        verifyClass(md)
      case _ =>
    }

    def verifyClass(cd: ImplDef): Unit = scoped(
      curClassSym := cd.symbol
    ) {
      cd.impl.body.foreach {
        case dd: DefDef =>
          verifyMethod(dd)
        case vd: ValDef =>
          verifyVal(vd)
        case _ =>
      }
    }

    def verifyMethod(dd: DefDef): Unit = scoped(
      curMethSym := dd.symbol
    ) {
      dd.rhs match {
        case rhs: Block if dd.symbol.isConstructor =>
          // We don't care about the constructor
          // at this phase
        case rhs if curClassSym.get.isExtern =>
          verifyExternMethod(dd)
        case _ =>
      }
    }

    def verifyVal(dd: ValDef): Unit = scoped(
      curValSym := dd.symbol
    ) {
      if (curClassSym.get.isExtern) {
        dd.rhs match {
          case sel: Select if sel.symbol == ExternMethod =>
            externMemberHasTpeAnnotation(dd)
          case _ if curValSym.isLazy =>
            reporter.error(dd.pos, s"(limitation) fields in extern ${symToName(curClassSym)} must not be lazy")
          case _ if curValSym.hasFlag(PARAMACCESSOR) =>
            // params are not allowed
            reporter.error(dd.pos, s"parameters in extern ${symToName(curClassSym)} are not allowed - only extern fields and methods are allowed")
          case rhs =>
            reporter.error(rhs.pos, s"fields in extern ${symToName(curClassSym)} must have extern body")
        }
      }
    }

    def verifyExternMethod(ddef: DefDef): Unit = {
      ddef.rhs match {
        case Apply(ref: RefTree, Seq()) if ref.symbol == ExternMethod =>
          // TOOD: Remove
          ()
        case _ if curMethSym.hasFlag(ACCESSOR) =>
          ()
        case sel: Select if sel.symbol == ExternMethod =>
          externMemberHasTpeAnnotation(ddef)
          ()
        case rhs =>
          reporter.error(rhs.pos.focus, s"methods in extern ${symToName(curClassSym)} must have extern body")
      }
    }

    def externMemberHasTpeAnnotation(df: ValOrDefDef): Unit = {
      df.tpt match {
        case t@TypeTree() if t.original == null =>
          reporter.error(df.pos, s"extern members must have an explicit type annotation")
        case t@TypeTree() =>
          ()
      }
    }


    private def symToName(sym: Symbol): String =
      if (sym.isClass)
        if (sym.asClass.isTrait) "traits" else "classes"
      else "objects"

  }
}

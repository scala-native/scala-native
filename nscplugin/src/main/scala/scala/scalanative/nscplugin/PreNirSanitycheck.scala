package scala.scalanative
package nscplugin

import scala.tools.nsc.plugins._
import scala.tools.nsc.{Phase, transform}
import scala.reflect.internal.Flags._
import util.ScopedVar.scoped
import util.ScopedVar

abstract class PreNirSanitycheck
  extends PluginComponent
  with NirTypeEncoding
  with NirNameEncoding
  with NirPluginComponent {

  import global._
  import definitions._

  val nirAddons: NirGlobalAddons {
    val global: PreNirSanitycheck.this.global.type
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

    override def apply(cunit: CompilationUnit): Unit = {
      def verifyDefs(tree: Tree): List[Tree] = {
        tree match {
          case EmptyTree => Nil
          case PackageDef(_, stats) => stats flatMap verifyDefs
          case cd: ClassDef => cd :: Nil
          case md: ModuleDef => md :: Nil
        }
      }

      cunit.body foreach verify
    }

    def verify(tree: Tree): Unit = tree match {
      case cd: ClassDef =>
        verifyClass(cd)
      case _ =>
    }

    def verifyClass(cd: ClassDef): Unit = scoped(
      curClassSym := cd.symbol
    ) {
      cd.impl.body.foreach {
        case dd: DefDef =>
          verifyMethod(dd)
        case _ =>
      }
    }

    def verifyMethod(dd: DefDef): Unit = scoped(
      curMethSym := dd.symbol
    ) {
      dd.rhs match {
        case rhs if dd.name == nme.CONSTRUCTOR && curClassSym.get.isExternModule =>
          verifyExternCtor(rhs)
        case rhs if curClassSym.get.isExternModule =>
          verifyExternMethod(rhs)
        case _ =>
      }
    }

    def verifyExternMethod(rhs: Tree): Unit = {
      rhs match {
        case Apply(ref: RefTree, Seq()) if ref.symbol == ExternMethod =>
          ()
        case _ if curMethSym.hasFlag(ACCESSOR) =>
          ()
        case rhs =>
          reporter.error(rhs.pos.focus, "methods in extern objects must have extern body")
      }
    }


    def verifyExternCtor(rhs: Tree): Unit = {
      val Block(_ +: init, _) = rhs
      val externs = init.flatMap {
        case t@Assign(ref: RefTree, Apply(extern, Seq()))
          if extern.symbol == ExternMethod =>
          List(ref.symbol)
        case Apply(extern, Seq()) if extern.symbol == ExternMethod =>
          Nil
        case t if t.symbol == null || !t.symbol.isConstructor =>
          reporter.error(t.pos,
            s"extern objects may only contain extern fields and methods")
          Nil
        case _ => Nil
      }.toSet
      for {
        f <- curClassSym.info.decls if f.isField
        if !externs.contains(f)
      } {
        reporter.error(f.pos, "extern objects may only contain extern fields")
      }
    }


  }
}

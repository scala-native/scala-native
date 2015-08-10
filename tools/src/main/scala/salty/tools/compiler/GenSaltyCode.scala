package salty.tools
package compiler

import scala.tools.nsc._
import scala.tools.nsc.plugins._
import salty.ir

abstract class GenSaltyCode extends PluginComponent {
  import global._
  import global.definitions._

  val phaseName = "saltycode"

  override def newPhase(prev: Phase): StdPhase =
    new SaltyCodePhase(prev)

  class SaltyCodePhase(prev: Phase) extends StdPhase(prev) {
    override def apply(cunit: CompilationUnit): Unit = {
      def collectClassDefs(tree: Tree): List[ClassDef] = {
        tree match {
          case EmptyTree => Nil
          case PackageDef(_, stats) => stats flatMap collectClassDefs
          case cd: ClassDef => cd :: Nil
        }
      }
      val classDefs = collectClassDefs(cunit.body)

      val irClasses = classDefs.flatMap { cd =>
        val sym = cd.symbol
        if (isPrimitiveValueClass(sym) || (sym == ArrayClass)) Nil
        else List(genClass(cd))
      }

      println("Input:")
      classDefs.foreach(c => println(c.toString))
      println("\nOutput:")
      irClasses.foreach(c => println(c.show.build))
    }

    def genClass(cd: ClassDef): ir.Stat = {
      val ClassDef(mods, name, _, impl) = cd
      val sym = cd.symbol
      val irName = encodeClassName(sym)
      val irParent = encodeClassName(sym.superClass)
      val irInterfaces = genClassInterfaces(sym)
      val irBody = impl.body.flatMap(genClassStat(_))

      if (sym.isModuleClass)
        ir.Stat.Module(irName, irParent, irInterfaces, irBody)
      else if (sym.isInterface)
        ir.Stat.Interface(irName, irInterfaces, irBody)
      else
        ir.Stat.Class(irName, irParent, irInterfaces, irBody)
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol
        if psym.isInterface
      } yield {
        encodeClassName(psym)
      }

    def genClassStat(stat: Tree): Seq[ir.Stat] = stat match {
      case EmptyTree  => Seq()
      case dd: DefDef => Seq(genMethod(dd))
    }

    def genMethod(dd: DefDef): ir.Stat = {
      val sym = dd.symbol
      val name = encodeMethodName(dd)
      val params = genParams(dd)
      val ty = encodeType(sym.tpe.resultType)
      val body = genBody(dd.rhs)
      ir.Stat.Define(name, params, ty, body)
    }

    def genParams(dd: DefDef): Seq[ir.LabeledType] = {
      val vp = dd.vparamss
      if (vp.isEmpty)
        Seq()
      else
        vp.head.map(_.symbol).map { sym =>
          val name = encodeParamName(sym)
          val ty = encodeType(sym.tpe)
          ir.LabeledType(name, ty)
        }.toSeq
    }

    def genBody(t: Tree): ir.Expr = ???

    def encodeType(t: Type): ir.Type = ???

    def encodeParamName(sym: Symbol): ir.Name = ???

    def encodeMethodName(dd: DefDef): ir.Name = ???

    def encodeClassName(sym: Symbol) = ir.Name("@" + sym.fullName)
  }
}

package scala.scalanative.nscplugin.check
import scala.reflect.internal.Flags._
import scala.scalanative.util.ScopedVar.scoped
import scala.tools.nsc

trait NirCheckStat[G <: nsc.Global with Singleton] {
  self: PrepSanityCheck[G] =>

  import SimpleType._
  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._

  def isStaticModule(sym: Symbol): Boolean =
    sym.isModuleClass && !isImplClass(sym) && !sym.isLifted

  def checkClassOrModule(impl: ImplDef): Unit = {
    scoped(
      curClassSym := impl.symbol
    ) {
      if (impl.symbol.isExtern) {
        checkExternClassOrModule(impl)
      }
      impl.impl.body.foreach {
        case dd: DefDef => checkMethod(dd)
        case vd: ValDef => checkValDef(vd)
        case _          => Ok
      }
    }
  }

  def checkExternClassOrModule(impl: ImplDef): Unit = {
    impl.symbol.tpe.parents
      .zip(impl.impl.parents)
      .foreach {
        case (parentTpe, implParent) =>
          val parentIsAnyRef = parentTpe == AnyRefTpe
          val parentIsExtern = implParent.symbol.isExtern

          if (parentIsAnyRef || parentIsExtern) Ok
          else {
            val thisKind   = symToKindPlural(impl.symbol)
            val parentKind = symToKind(implParent.symbol)
            reporter.error(
              impl.pos,
              s"extern $thisKind may only have extern parents, $parentKind ${implParent.symbol.nameString} is not extern")
          }
      }
  }

  def checkMethod(dd: DefDef): Unit = {
    scoped(
      curMethodSym := dd.symbol
    ) {
      val owner          = curClassSym.get
      val isStatic       = owner.isExternModule || isImplClass(owner)
      val isSynchronized = dd.symbol.hasFlag(SYNCHRONIZED)
      val isConstructor =
        dd.name == nme.CONSTRUCTOR || dd.name == nme.MIXIN_CONSTRUCTOR

      if (isSynchronized && isStatic) {
        reporter.error(dd.pos,
                       s"cannot generate `synchronized` for static method")
      }

      if (owner.isExtern) {
        if (isConstructor) Ok // Constructors are not handled in this phase
        else checkExternMethod(dd)
      } else checkExpr(dd.rhs)
    }
  }

  def checkExternMethod(ddef: DefDef): Unit = {
    ddef.rhs match {
      case Apply(ref: RefTree, Seq()) if ref.symbol == ExternMethod =>
        checkExternMemberHasTpeAnnotation(ddef)
      case sel: Select if sel.symbol == ExternMethod =>
        checkExternMemberHasTpeAnnotation(ddef)
      case _ if curMethodSym.hasFlag(ACCESSOR) => Ok
      case _ =>
        reporter.error(
          ddef.rhs.pos.focus,
          s"methods in extern ${symToKindPlural(curClassSym)} must have extern body")
    }
  }
}

package scala.scalanative.nscplugin.check
import scala.reflect.internal.Flags._
import scala.scalanative.util.ScopedVar.scoped
import scala.tools.nsc
import scala.tools.nsc.Properties

trait NirCheckStat[G <: nsc.Global with Singleton] {
  self: PreNirSanityCheck[G] =>

  import SimpleType._
  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._

  protected val isScala211 = Properties.versionNumberString.startsWith("2.11")

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

  def checkJavaDefaultMethodBody(dd: DefDef): Unit = {
    val sym               = dd.symbol
    val implClassFullName = sym.owner.fullName + "$class"

    val implClassSym = findMemberFromRoot(TermName(implClassFullName))

    val implMethodSym = implClassSym.info
      .member(sym.name)
      .suchThat { s =>
        s.isMethod &&
        s.tpe.params.size == sym.tpe.params.size + 1 &&
        s.tpe.params.head.tpe =:= sym.owner.toTypeConstructor &&
        s.tpe.params.tail.zip(sym.tpe.params).forall {
          case (sParam, symParam) =>
            sParam.tpe =:= symParam.tpe
        }
      }
    // TODO check if no symbol?
  }

  //  scoped(
  //    curMethSym := dd.symbol
  //  ) {
  //    dd.rhs match {
  //      // We don't care about the constructor at this phase
  //      case _: Block if dd.symbol.isConstructor => Skip
  //      case _ if curClassSym.get.isExtern       => verifyExternMethod(dd)
  //      case rhs                                 => verifyExpr(rhs)
  //    }
  //  }

  def checkMethod(dd: DefDef): Unit = {
    scoped(
      curMethodSym := dd.symbol
    ) {
      val sym            = dd.symbol
      val owner          = curClassSym.get
      val isStatic       = owner.isExternModule || isImplClass(owner)
      val isSynchronized = dd.symbol.hasFlag(SYNCHRONIZED)
      val isJavaDefaultMethod =
        isScala211 && sym.hasAnnotation(JavaDefaultMethodAnnotation)

      if (isSynchronized && isStatic) {
        reporter.error(dd.pos,
                       s"cannot generate `synchronized` for static method")
      }

      if (owner.isExtern) {
        // Constructors are not handled in this phase
        if (dd.name == nme.CONSTRUCTOR || dd.name == nme.MIXIN_CONSTRUCTOR) Ok
        else checkExternMethod(dd)
      } else if (isJavaDefaultMethod) {
        checkJavaDefaultMethodBody(dd)
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

//  def checkExternCtor(rhs: Tree): Unit = {
//    val Block(_ +: init, _) = rhs
//    val externs = init.flatMap {
//      case Assign(ref: RefTree, Apply(extern, Seq()))
//          if extern.symbol == ExternMethod =>
//        Some(ref.symbol)
//      case _ =>
//        reporter.error(
//          rhs.pos,
//          s"extern ${symToKindPlural(curClassSym)} may only contain extern fields and methods")
//        None
//    }.toSet
//
//    for {
//      f <- curClassSym.info.decls if f.isField
//      if !externs.contains(f)
//    } {
//      reporter.error(
//        rhs.pos,
//        s"extern ${symToKindPlural(curClassSym)} may only contain extern fields")
//    }
//  }

}

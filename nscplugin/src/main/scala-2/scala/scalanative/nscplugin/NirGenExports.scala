package scala.scalanative
package nscplugin

import scala.language.implicitConversions
import scala.tools.nsc

trait NirGenExports[G <: nsc.Global with Singleton] {
  self: NirGenPhase[G] with NirGenType[G] =>
  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._
  import SimpleType._

  case class ExportedSymbol(symbol: Symbol, defn: nir.Defn.Define)

  def isExported(s: Symbol) = {
    s.hasAnnotation(ExportedClass) ||
    s.hasAnnotation(ExportAccessorsClass)
  }

  def genTopLevelExports(cd: ClassDef): Seq[nir.Defn] = {
    val owner = cd.symbol
    val generated =
      for {
        member <- owner.info.members
        if isExported(member)
        if !owner.isExternType
        // Externs combined with exports are not allowed, exception is handled in externs
        exported <-
          if (owner.isScalaModule) genModuleMember(owner, member)
          else genClassExport(member)
      } yield exported

    generated.groupBy(_.defn.name).foreach {
      case (name, exported) if exported.size > 1 =>
        val duplicatedSymbols = exported.map(_.symbol)
        val showDuplicates = duplicatedSymbols.mkString(" and ")
        duplicatedSymbols.foreach { sym =>
          reporter.error(
            sym.pos,
            s"Names of the exported functions needs to be unique, found duplicated generating name $name in $showDuplicates"
          )
        }
      case (_, _) => ()
    }
    generated.map(_.defn).toSeq
  }

  private def genClassExport(member: Symbol): Seq[ExportedSymbol] = {
    // In the future we might implement also class exports, by assuming that given class instance can be passed as an opaque pointer
    // In such case extern method would take an opaque pointer to an instance and arguments
    reporter.error(
      member.pos,
      "Exported members must be statically reachable, definition within class or trait is currently unsupported"
    )
    Nil
  }

  private def isField(s: Symbol): Boolean =
    !s.isMethod && s.isTerm && !s.isModule

  private def checkIsPublic(s: Symbol): Unit =
    if (!s.isPublic) {
      reporter.error(
        s.pos,
        "Exported members needs to be defined in public scope"
      )
    }

  private def checkMethodAnnotation(s: Symbol): Unit =
    if (!s.hasAnnotation(ExportedClass)) {
      reporter.error(
        s.pos,
        "Incorrect annotation found, to export method use `@exported` annotation"
      )
    }

  private def checkAccessorAnnotation(s: Symbol): Unit =
    if (!s.hasAnnotation(ExportAccessorsClass)) {
      reporter.error(
        s.pos,
        "Cannot export field, use `@exportAccessors()` annotation to generate external accessors"
      )
    }

  private def genModuleMember(
      owner: Symbol,
      member: Symbol
  ): Seq[ExportedSymbol] = {
    if (isField(member)) {
      checkAccessorAnnotation(member)
      member.getAnnotation(ExportAccessorsClass) match {
        case None => Nil
        case Some(annotation) =>
          def accessorExternSig(prefix: String) = {
            val nir.Sig.Extern(id) = genExternSig(member)
            nir.Sig.Extern(prefix + id)
          }

          def getterName = annotation
            .stringArg(0)
            .map(nir.Sig.Extern(_))
            .getOrElse(accessorExternSig("get_"))
          def setterName = annotation
            .stringArg(1)
            .map(nir.Sig.Extern(_))
            .getOrElse(accessorExternSig("set_"))

          def externGetter = genModuleMethod(owner, member.getter, getterName)
          def externSetter = genModuleMethod(owner, member.setter, setterName)

          if (member.isVar) Seq(externGetter, externSetter)
          else if (!member.getterIn(owner).exists) {
            // this can only happend in case of private val
            checkIsPublic(member)
            Nil
          } else {
            if (annotation.stringArg(1).isDefined) {
              reporter.warning(
                member.pos,
                "Unused explicit setter name, annotated field in not mutable it would never use its explicit exported setter name"
              )
            }
            Seq(externGetter)
          }
      }
    } else {
      checkMethodAnnotation(member)
      val name = member
        .getAnnotation(ExportedClass)
        .flatMap(_.stringArg(0))
        .map(nir.Sig.Extern(_))
        .getOrElse(genExternSig(member))
      Seq(genModuleMethod(owner, member, name))
    }
  }

  private def genModuleMethod(
      owner: Symbol,
      member: Symbol,
      externSig: nir.Sig.Extern
  ): ExportedSymbol = {
    checkIsPublic(member)
    implicit val pos: nir.SourcePosition = member.pos
    val originalName = genMethodName(member)
    val externName = originalName.top.member(externSig)

    val nir.Type.Function(_ +: paramTypes, retType) = genMethodSig(member)
    val exportedFunctionType @ nir.Type.Function(
      externParamTypes,
      externRetType
    ) = genExternMethodSig(member)

    val defn = nir.Defn.Define(
      attrs = nir.Attrs(inlineHint = nir.Attr.NoInline, isExtern = true),
      name = externName,
      ty = exportedFunctionType,
      insts = curStatBuffer.withFreshExprBuffer { implicit buf: ExprBuffer =>
        val fresh = curFresh.get
        util.ScopedVar.scoped(
          curUnwindHandler := None,
          curMethodThis := None,
          curScopeId := nir.ScopeId.TopLevel
        ) {
          val entryParams = externParamTypes.map(nir.Val.Local(fresh(), _))
          buf.label(fresh(), entryParams)
          val boxedParams = paramTypes
            .zip(entryParams)
            .map((buf.fromExtern _).tupled(_))
          val argsp = boxedParams.map(ValTree(_))
          val res = buf.genApplyModuleMethod(owner, member, argsp)
          val unboxedRes = buf.toExtern(externRetType, res)
          buf.ret(unboxedRes)
        }
        buf.toSeq
      }
    )
    ExportedSymbol(member, defn)
  }
}

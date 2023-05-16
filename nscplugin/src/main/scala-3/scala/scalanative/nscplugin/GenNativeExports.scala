package scala.scalanative.nscplugin

import scala.language.implicitConversions

import dotty.tools.dotc.ast.tpd._
import dotty.tools.dotc.core
import core.Contexts._
import core.Symbols._
import core.Flags._
import core.Annotations.*
import dotty.tools.dotc.report
import dotty.tools.dotc.transform.SymUtils.*

import scala.scalanative.nir
import nir._
import scala.scalanative.util.ScopedVar.scoped

trait GenNativeExports(using Context):
  self: NirCodeGen =>
  import self.positionsConversions.given

  opaque type OwnerSymbol = Symbol
  case class ExportedSymbol(symbol: Symbol, defn: Defn.Define)

  def isExported(s: Symbol) =
    s.hasAnnotation(defnNir.ExportedClass) ||
      s.hasAnnotation(defnNir.ExportAccessorsClass)

  def genTopLevelExports(td: TypeDef): Seq[nir.Defn] =
    given owner: OwnerSymbol = td.symbol
    val generated =
      for
        member <- owner.denot.info.allMembers.map(_.symbol)
        if isExported(member)
        if !checkAndReportWhenIsExtern(member)
        // Externs combined with exports are not allowed, exception is handled in externs
        exported <-
          if owner.isStaticModule then genModuleMember(member)
          else genClassExport(member)
      yield exported

    generated.groupBy(_.defn.name).foreach {
      case (name, exported) if exported.size > 1 =>
        val duplicatedSymbols = exported.map(_.symbol)
        val showDuplicates = duplicatedSymbols.map(_.show).mkString(" and ")
        duplicatedSymbols.foreach { sym =>
          report.error(
            s"Names of the exported functions needs to be unique, found duplicated generating name $name in $showDuplicates",
            sym.srcPos
          )
        }
      case (_, _) => ()
    }
    generated.map(_.defn)
  end genTopLevelExports

  private def genClassExport(member: Symbol): Seq[ExportedSymbol] =
    // In the future we might implement also class exports, by assuming that given class instance can be passed as an opaque pointer
    // In such case extern method would take an opaque pointer to an instance and arguments
    report.error(
      "Exported members must be statically reachable, definition within class or trait is currently unsupported",
      member.srcPos
    )
    Nil

  private def isMethod(s: Symbol): Boolean =
    s.isOneOf(Method | Module) && s.isTerm

  private def checkAndReportWhenIsExtern(s: Symbol) =
    val isExtern = s.isExtern
    if isExtern then
      report.error(
        "Member cannot be defined both exported and extern, use `@extern` for symbols with external definition, and `@exported` for symbols that should be accessible via library",
        s.srcPos
      )
    isExtern

  private def checkIsPublic(s: Symbol): Unit =
    if !s.isPublic then
      report.error(
        "Exported members needs to be defined in public scope",
        s.srcPos
      )

  private def checkMethodAnnotation(s: Symbol): Unit =
    if !s.hasAnnotation(defnNir.ExportedClass) then
      report.error(
        "Incorrect annotation found, to export method use `@exported` annotation",
        s.srcPos
      )

  private def checkAccessorAnnotation(s: Symbol): Unit =
    if !s.hasAnnotation(defnNir.ExportAccessorsClass) then
      report.error(
        "Cannot export field, use `@exportAccessors()` annotation to generate external accessors",
        s.srcPos
      )

  private def genModuleMember(
      member: Symbol
  )(using owner: OwnerSymbol): Seq[ExportedSymbol] =
    if isMethod(member) then
      checkMethodAnnotation(member)
      val name = member
        .getAnnotation(defnNir.ExportedClass)
        .flatMap(_.argumentConstantString(0))
        .map(Sig.Extern(_))
        .getOrElse(genExternSig(member))
      Seq(genModuleMethod(member, name))
    else
      checkAccessorAnnotation(member)
      member.getAnnotation(defnNir.ExportAccessorsClass) match {
        case None => Nil
        case Some(annotation) =>
          def accessorExternSig(prefix: String) =
            val Sig.Extern(id) = genExternSig(member)
            Sig.Extern(prefix + id)

          def getterName = annotation
            .argumentConstantString(0)
            .map(Sig.Extern(_))
            .getOrElse(accessorExternSig("get_"))
          def setterName = annotation
            .argumentConstantString(1)
            .map(Sig.Extern(_))
            .getOrElse(accessorExternSig("set_"))

          def externGetter = genModuleMethod(member.getter, getterName)
          def externSetter = genModuleMethod(member.setter, setterName)

          if member.is(Mutable) then Seq(externGetter, externSetter)
          else if !member.getter.exists then
            // this can only happend in case of private val
            checkIsPublic(member)
            Nil
          else
            if annotation.argument(1).isDefined then
              report.warning(
                "Unused explicit setter name, annotated field in not mutable it would never use its explicit exported setter name",
                member.srcPos
              )
            Seq(externGetter)
      }
  end genModuleMember

  private def genModuleMethod(member: Symbol, externSig: Sig.Extern)(using
      owner: OwnerSymbol
  ): ExportedSymbol =
    checkIsPublic(member)
    given nir.Position = member.span
    val originalName = genMethodName(member)
    val externName = originalName.top.member(externSig)

    val Type.Function(_ +: paramTypes, retType) =
      genMethodSig(member): @unchecked
    val exportedFunctionType @ Type.Function(
      externParamTypes,
      externRetType
    ) = genExternMethodSig(member)

    val defn = Defn.Define(
      attrs = Attrs(inlineHint = nir.Attr.NoInline, isExtern = true),
      name = externName,
      ty = exportedFunctionType,
      insts = withFreshExprBuffer { buf ?=>
        val fresh = curFresh.get
        scoped(
          curUnwindHandler := None,
          curMethodThis := None
        ) {
          val entryParams = externParamTypes.map(Val.Local(fresh(), _))
          buf.label(fresh(), entryParams)
          val boxedParams = paramTypes
            .zip(entryParams)
            .map(buf.fromExtern(_, _))
          val argsp = boxedParams.map(ValTree(_))
          val res = buf.genApplyModuleMethod(owner, member, argsp)
          val unboxedRes = buf.toExtern(externRetType, res)
          buf.ret(unboxedRes)
        }
        buf.toSeq
      }
    )
    ExportedSymbol(member, defn)

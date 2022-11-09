package scala.scalanative.nscplugin

import scala.language.implicitConversions

import dotty.tools.dotc.ast.tpd._
import dotty.tools.dotc.core
import core.Contexts._
import core.Symbols._
import core.Constants._
import core.StdNames._
import core.Flags._
import core.Phases._
import dotty.tools.dotc.transform.SymUtils._

import scala.collection.mutable
import scala.scalanative.nir
import nir._
import scala.scalanative.util.ScopedVar
import scala.scalanative.util.ScopedVar.{scoped, toValue}
import scala.scalanative.util.unsupported
import dotty.tools.FatalError
import dotty.tools.dotc.report

trait NirGenStat(using Context) {
  self: NirCodeGen =>
  import positionsConversions.fromSpan

  protected val generatedDefns = mutable.UnrolledBuffer.empty[nir.Defn]
  protected val generatedMirrorClasses =
    mutable.Map.empty[Symbol, MirrorClass]

  protected case class MirrorClass(
      defn: nir.Defn.Class,
      forwarders: Seq[nir.Defn.Define]
  )

  def genClass(td: TypeDef)(using Context): Unit = {
    val sym = td.symbol.asClass
    scoped(
      curClassSym := sym,
      curClassFresh := nir.Fresh()
    ) {
      if (sym.isStruct) genStruct(td)
      else genNormalClass(td)
    }
  }

  private def genNormalClass(td: TypeDef): Unit = {
    lazyValsAdapter.prepareForTypeDef(td)
    implicit val pos: nir.Position = td.span
    val sym = td.symbol.asClass
    val attrs = genClassAttrs(td)
    val name = genTypeName(sym)

    def parent = genClassParent(sym)
    def traits = sym.info.parents
      .map(_.classSymbol)
      .filter(_.isTraitOrInterface)
      .map(genTypeName)

    generatedDefns += {
      if (sym.isStaticModule) Defn.Module(attrs, name, parent, traits)
      else if (sym.isTraitOrInterface) Defn.Trait(attrs, name, traits)
      else Defn.Class(attrs, name, parent, traits)
    }
    genClassFields(td)
    genMethods(td)
    genReflectiveInstantiation(td)
    genMirrorClass(td)
  }

  private def genClassAttrs(td: TypeDef): nir.Attrs = {
    val sym = td.symbol.asClass
    val annotationAttrs = sym.annotations.collect {
      case ann if ann.symbol == defnNir.ExternClass => Attr.Extern
      case ann if ann.symbol == defnNir.StubClass   => Attr.Stub
      case ann if ann.symbol == defnNir.LinkClass =>
        val Apply(_, Seq(Literal(Constant(name: String)))) =
          ann.tree: @unchecked
        Attr.Link(name)
    }
    val isAbstract = Option.when(sym.is(Abstract))(Attr.Abstract)
    Attrs.fromSeq(annotationAttrs ++ isAbstract)
  }

  private def genClassParent(sym: ClassSymbol): Option[nir.Global] = {
    if (sym == defnNir.NObjectClass)
      None
    else
      Some {
        val superClass = sym.superClass
        if (superClass == NoSymbol || superClass == defn.ObjectClass)
          genTypeName(defnNir.NObjectClass)
        else
          genTypeName(superClass)
      }
  }

  private def genClassFields(td: TypeDef): Unit = {
    val classSym = td.symbol.asClass
    assert(
      curClassSym.get == classSym,
      "genClassFields called with a ClassDef other than the current one"
    )

    // Term members that are neither methods nor modules are fields
    for
      f <- classSym.info.decls.toList
      if !f.isOneOf(Method | Module) && f.isTerm
    do
      given nir.Position = f.span

      val isStatic = f.is(JavaStatic) || f.isScalaStatic
      val isExtern = f.isExtern
      val mutable = isStatic || f.is(Mutable)
      val attrs = nir.Attrs(isExtern = f.isExtern)
      val ty = genType(f.info.resultType)
      val fieldName @ Global.Member(owner, sig) = genFieldName(f): @unchecked
      generatedDefns += Defn.Var(attrs, fieldName, ty, Val.Zero(ty))

      if (isStatic) {
        // Here we are generating a public static getter for the static field,
        // this is its API for other units. This is necessary for singleton
        // enum values, which are backed by static fields.
        generatedDefns += Defn.Define(
          attrs = Attrs(inlineHint = nir.Attr.InlineHint),
          name = genStaticMemberName(f, classSym),
          ty = Type.Function(Nil, ty),
          insts = withFreshExprBuffer { buf ?=>
            val fresh = curFresh.get
            buf.label(fresh())
            val module = buf.module(genModuleName(classSym), Next.None)
            val value = buf.fieldload(ty, module, fieldName, Next.None)
            buf.ret(value)

            buf.toSeq
          }
        )
      }
  }

  private def genMethods(td: TypeDef): Unit = {
    val tpl = td.rhs.asInstanceOf[Template]
    val methods = (tpl.constr :: tpl.body).flatMap {
      case EmptyTree  => Nil
      case _: ValDef  => Nil // handled in genClassFields
      case _: TypeDef => Nil
      case dd: DefDef =>
        lazyValsAdapter.transformDefDef(dd) match {
          case dd: DefDef => genMethod(dd)
          case _          => Nil // erased
        }
      case tree =>
        throw new FatalError("Illegal tree in body of genMethods():" + tree)
    }

    generatedDefns ++= methods
    generatedDefns ++= genStaticMethodForwarders(td, methods)
    generatedDefns ++= genTopLevelExports(td)
  }

  private def genMethod(dd: DefDef): Option[Defn] = {
    implicit val pos: nir.Position = dd.span
    val fresh = Fresh()

    scoped(
      curMethodSym := dd.symbol,
      curMethodEnv := new MethodEnv(fresh),
      curMethodLabels := new MethodLabelsEnv(fresh),
      curMethodInfo := CollectMethodInfo().collect(dd.rhs),
      curFresh := fresh,
      curUnwindHandler := None
    ) {
      val sym = dd.symbol
      val owner = curClassSym.get

      val attrs = genMethodAttrs(sym)
      val name = genMethodName(sym)
      val sig = genMethodSig(sym)

      dd.rhs match {
        case EmptyTree => Some(Defn.Declare(attrs, name, sig))
        case _ if sym.isClassConstructor && sym.isExtern =>
          validateExternCtor(dd.rhs)
          None

        case _ if sym.isClassConstructor && owner.isStruct =>
          None

        case rhs if sym.isExtern =>
          checkExplicitReturnTypeAnnotation(dd, "extern method")
          genExternMethod(attrs, name, sig, rhs)

        case _ if sym.hasAnnotation(defnNir.ResolvedAtLinktimeClass) =>
          genLinktimeResolved(dd, name)

        case rhs =>
          scoped(
            curMethodSig := sig
          ) {
            val defn = Defn.Define(
              attrs,
              name,
              sig,
              genMethodBody(dd, rhs)
            )
            Some(defn)
          }
      }
    }
  }

  private def genMethodAttrs(sym: Symbol): nir.Attrs = {
    val inlineAttrs =
      if (sym.is(Bridge) || sym.is(Accessor)) Seq(Attr.AlwaysInline)
      else Nil

    val annotatedAttrs =
      sym.annotations.map(_.symbol.typeRef).collect {
        case defnNir.NoInlineType     => Attr.NoInline
        case defnNir.AlwaysInlineType => Attr.AlwaysInline
        case defnNir.InlineType       => Attr.InlineHint
        case defnNir.NoOptimizeType   => Attr.NoOpt
        case defnNir.NoSpecializeType => Attr.NoSpecialize
        case defnNir.StubType         => Attr.Stub
        case defnNir.ExternType       => Attr.Extern
      }

    Attrs.fromSeq(inlineAttrs ++ annotatedAttrs)
  }

  protected val curExprBuffer = ScopedVar[ExprBuffer]()
  private def genMethodBody(
      dd: DefDef,
      bodyp: Tree
  ): Seq[nir.Inst] = {
    given nir.Position = bodyp.span
    given fresh: nir.Fresh = curFresh.get
    val buf = ExprBuffer()
    val isExtern = dd.symbol.isExtern
    val isStatic = dd.symbol.isStaticInNIR
    val isSynchronized = dd.symbol.is(Synchronized)

    val sym = curMethodSym.get
    val argParamSyms = for {
      paramList <- dd.paramss.take(1)
      param <- paramList
    } yield param.symbol
    val argParams = argParamSyms.map { sym =>
      val tpe = sym.info.resultType
      val ty = genType(tpe)
      val param = Val.Local(fresh(), ty)
      curMethodEnv.enter(sym, param)
      param
    }
    val thisParam = Option.unless(isStatic) {
      Val.Local(fresh(), genType(curClassSym.get))
    }
    val outerParam = argParamSyms
      .find(_.name == nme.OUTER)
    val params = thisParam.toList ::: argParams

    def genEntry(): Unit = {
      buf.label(fresh(), params)
    }

    def genVars(): Unit = {
      val vars = curMethodInfo.mutableVars
        .foreach { sym =>
          val ty = genType(sym.info)
          val slot = buf.var_(ty, unwind(fresh))
          curMethodEnv.enter(sym, slot)
        }
    }

    def withOptSynchronized(bodyGen: ExprBuffer => Val): Val = {
      if (!isSynchronized) bodyGen(buf)
      else {
        val syncedIn = curMethodThis.getOrElse {
          unsupported(
            s"cannot generate `synchronized` for method ${curMethodSym.name}, curMethodThis was empty"
          )
        }
        buf.genSynchronized(ValTree(syncedIn))(bodyGen)
      }
    }
    def genBody(): Unit = {
      if (curMethodSym.get == defnNir.NObject_init)
        scoped(
          curMethodIsExtern := isExtern
        ) {
          buf.genReturn(Val.Unit)
        }
      else
        scoped(
          curMethodThis := thisParam,
          curMethodIsExtern := isExtern
        ) {
          buf.genReturn(withOptSynchronized(_.genExpr(bodyp)) match {
            case Val.Zero(_) =>
              Val.Zero(genType(curMethodSym.get.info.resultType))
            case v => v
          })
        }
    }

    scoped(curExprBuffer := buf) {
      genEntry()
      genVars()
      genBody()
      ControlFlow.removeDeadBlocks(buf.toSeq)
    }
  }

  private def genStruct(td: TypeDef): Unit = {
    given nir.Position = td.span

    val sym = td.symbol
    val attrs = Attrs.None
    val name = genTypeName(sym)

    generatedDefns += Defn.Class(attrs, name, None, Seq.empty)
    genMethods(td)
  }

  protected def checkExplicitReturnTypeAnnotation(
      externDef: ValOrDefDef,
      methodKind: String
  ): Unit = {
    if (externDef.tpt.symbol == defn.NothingClass)
      report.error(
        s"$methodKind ${externDef.name} needs result type",
        externDef.sourcePos
      )
  }

  protected def genLinktimeResolved(dd: DefDef, name: Global)(using
      nir.Position
  ): Option[Defn] = {
    if (dd.symbol.isField) {
      report.error(
        "Link-time property cannot be constant value, it would be inlined by scalac compiler",
        dd.sourcePos
      )
    }

    if (dd.rhs.symbol == defnNir.UnsafePackage_resolved) {
      checkExplicitReturnTypeAnnotation(dd, "value resolved at link-time")
      dd match {
        case LinktimeProperty(propertyName, _) =>
          val retty = genType(dd.tpt.tpe)
          val defn = genLinktimeResolvedMethod(retty, propertyName, name)
          Some(defn)
        case _ => None
      }
    } else {
      report.error(
        s"Link-time resolved property must have ${defnNir.UnsafePackage_resolved.fullName} as body",
        dd.sourcePos
      )
      None
    }
  }

  /* Generate stub method that can be used to get value of link-time property at runtime */
  private def genLinktimeResolvedMethod(
      retty: nir.Type,
      propertyName: String,
      methodName: nir.Global
  )(using nir.Position): Defn = {
    given fresh: Fresh = Fresh()
    val buf = new ExprBuffer()

    buf.label(fresh())
    val value = buf.call(
      Linktime.PropertyResolveFunctionTy(retty),
      Linktime.PropertyResolveFunction(retty),
      Val.String(propertyName) :: Nil,
      Next.None
    )
    buf.ret(value)

    Defn.Define(
      Attrs(inlineHint = Attr.AlwaysInline),
      methodName,
      Type.Function(Seq(), retty),
      buf.toSeq
    )
  }

  def genExternMethod(
      attrs: nir.Attrs,
      name: nir.Global,
      origSig: nir.Type,
      rhs: Tree
  ): Option[Defn] = {
    given nir.Position = rhs.span
    rhs match {
      case Apply(ref: RefTree, Seq())
          if ref.symbol == defnNir.UnsafePackage_extern =>
        val moduleName = genTypeName(curClassSym)
        val externAttrs = Attrs(isExtern = true)
        val externSig = genExternMethodSig(curMethodSym)
        Some(Defn.Declare(externAttrs, name, externSig))
      case _ if curMethodSym.get.isOneOf(Accessor | Synthetic) =>
        None
      case rhs =>
        report.error(
          s"methods in extern objects must have extern body  - ${rhs}",
          rhs.sourcePos
        )
        None
    }
  }

  def validateExternCtor(rhs: Tree): Unit = {
    val Block(_ +: init, _) = rhs: @unchecked
    val externs = init.map {
      case Assign(ref: RefTree, Apply(extern, Seq()))
          if extern.symbol == defnNir.UnsafePackage_extern =>
        ref.symbol
      case _ =>
        report.error(
          "extern objects may only contain extern fields and methods",
          rhs.sourcePos
        )
    }.toSet
    for {
      f <- curClassSym.get.info.decls.toList if f.isField
      if !externs.contains(f)
    } report.error(
      "extern objects may only contain extern fields",
      f.sourcePos
    )
  }

  // Static forwarders -------------------------------------------------------

  // Ported from Scala.js
  /* It is important that we always emit forwarders, because some Java APIs
   * actually have a public static method and a public instance method with
   * the same name. For example the class `Integer` has a
   * `def hashCode(): Int` and a `static def hashCode(Int): Int`. The JVM
   * back-end considers them as colliding because they have the same name,
   * but we must not.
   *
   * By default, we only emit forwarders for top-level objects, like the JVM
   * back-end. However, if requested via a compiler option, we enable them
   * for all static objects. This is important so we can implement static
   * methods of nested static classes of JDK APIs (see scala-js/#3950).
   */

  /** Is the given Scala class, interface or module class a candidate for static
   *  forwarders?
   *
   *    - the flag `-XnoForwarders` is not set to true, and
   *    - the symbol is static, and
   *    - either of both of the following is true:
   *      - the plugin setting `GenStaticForwardersForNonTopLevelObjects` is set
   *        to true, or
   *      - the symbol was originally at the package level
   *
   *  Other than the the fact that we also consider interfaces, this performs
   *  the same tests as the JVM back-end.
   */
  private def isCandidateForForwarders(sym: Symbol): Boolean = {
    !ctx.settings.XnoForwarders.value && sym.isStatic && {
      settings.genStaticForwardersForNonTopLevelObjects ||
      atPhase(flattenPhase) {
        toDenot(sym).owner.is(PackageClass)
      }
    }
  }

  /** Gen the static forwarders to the members of a class or interface for
   *  methods of its companion object.
   *
   *  This is only done if there exists a companion object and it is not a JS
   *  type.
   *
   *  Precondition: `isCandidateForForwarders(sym)` is true
   */
  private def genStaticForwardersForClassOrInterface(
      existingMembers: Seq[Defn],
      sym: Symbol
  ): Seq[Defn.Define] = {
    val module = sym.companionModule
    if (!module.exists) Nil
    else {
      val moduleClass = module.moduleClass
      if (moduleClass.isExternModule) Nil
      else genStaticForwardersFromModuleClass(existingMembers, moduleClass)
    }
  }

  /** Gen the static forwarders for the methods of a module class.
   *
   *  Precondition: `isCandidateForForwarders(moduleClass)` is true
   */
  private def genStaticForwardersFromModuleClass(
      existingMembers: Seq[Defn],
      moduleClass: Symbol
  ): Seq[Defn.Define] = {
    assert(moduleClass.is(ModuleClass), moduleClass)

    val existingStaticMethodNames: Set[Global] = existingMembers.collect {
      case Defn.Define(_, name @ Global.Member(_, sig), _, _) if sig.isStatic =>
        name
    }.toSet
    val members = {
      moduleClass.info
        .membersBasedOnFlags(
          required = Method,
          excluded = ExcludedForwarder
        )
        .map(_.symbol)
    }

    def isExcluded(m: Symbol): Boolean = {
      def hasAccessBoundary = m.accessBoundary(defn.RootClass) ne defn.RootClass
      m.isExtern || m.isConstructor ||
        m.is(Deferred) || hasAccessBoundary ||
        (m.owner eq defn.ObjectClass)
    }

    for {
      sym <- members if !isExcluded(sym)
    } yield {
      given nir.Position = sym.span

      val methodName = genMethodName(sym)
      val forwarderName = genStaticMemberName(sym, moduleClass)
      val Type.Function(_ +: paramTypes, retType) =
        genMethodSig(sym): @unchecked
      val forwarderParamTypes = paramTypes
      val forwarderType = Type.Function(forwarderParamTypes, retType)

      if (existingStaticMethodNames.contains(forwarderName)) {
        report.error(
          "Unexpected situation: found existing public static method " +
            s"${sym.show} in the companion class of " +
            s"${moduleClass.fullName}; cannot generate a static forwarder " +
            "the method of the same name in the object." +
            "Please report this as a bug in the Scala Native support.",
          curClassSym.get.sourcePos
        )
      }

      Defn.Define(
        attrs = Attrs(inlineHint = nir.Attr.InlineHint),
        name = forwarderName,
        ty = forwarderType,
        insts = withFreshExprBuffer { buf ?=>
          val fresh = curFresh.get
          scoped(
            curUnwindHandler := None,
            curMethodThis := None
          ) {
            val entryParams = forwarderParamTypes.map(Val.Local(fresh(), _))
            val args = entryParams.map(ValTree(_))
            buf.label(fresh(), entryParams)
            val res = buf.genApplyModuleMethod(moduleClass, sym, args)
            buf.ret(res)
          }
          buf.toSeq
        }
      )
    }
  }

  private def genStaticMethodForwarders(
      td: TypeDef,
      existingMethods: Seq[Defn]
  ): Seq[Defn] = {
    val sym = td.symbol
    if !isCandidateForForwarders(sym) then Nil
    else if sym.isStaticModule then Nil
    else genStaticForwardersForClassOrInterface(existingMethods, sym)
  }

  /** Create a mirror class for top level module that has no defined companion
   *  class. A mirror class is a class containing only static methods that
   *  forward to the corresponding method on the MODULE instance of the given
   *  Scala object. It will only be generated if there is no companion class: if
   *  there is, an attempt will instead be made to add the forwarder methods to
   *  the companion class.
   */
  private def genMirrorClass(td: TypeDef): Unit = {
    given pos: nir.Position = td.span
    val sym = td.symbol
    val isTopLevelModuleClass = sym.is(ModuleClass) &&
      atPhase(flattenPhase) {
        toDenot(sym).owner.is(PackageClass)
      }
    if isTopLevelModuleClass && sym.companionClass == NoSymbol then {
      val classDefn = Defn.Class(
        attrs = Attrs.None,
        name = Global.Top(genTypeName(sym).id.stripSuffix("$")),
        parent = Some(Rt.Object.name),
        traits = Nil
      )
      generatedMirrorClasses += sym -> MirrorClass(
        classDefn,
        genStaticForwardersFromModuleClass(Nil, sym)
      )
    }
  }
}

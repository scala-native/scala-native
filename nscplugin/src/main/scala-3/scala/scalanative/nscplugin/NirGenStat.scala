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
import dotty.tools.dotc.core.NameKinds

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
    def traits = genClassInterfaces(sym)
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
      case ann if ann.symbol == defnNir.ExternClass =>
        Attr.Extern(sym.isBlocking)
      case ann if ann.symbol == defnNir.StubClass => Attr.Stub
      case ann if ann.symbol == defnNir.LinkClass =>
        val Apply(_, Seq(Literal(Constant(name: String)))) =
          ann.tree: @unchecked
        Attr.Link(name)
    }
    val isAbstract = Option.when(sym.is(Abstract))(Attr.Abstract)
    Attrs.fromSeq(annotationAttrs ++ isAbstract)
  }

  private def genClassParent(sym: ClassSymbol): Option[nir.Global] = {
    if sym.isExternType && sym.superClass != defn.ObjectClass then
      report.error("Extern object can only extend extern traits", sym.sourcePos)

    Option.unless(sym == defnNir.NObjectClass) {
      val superClass = sym.superClass
      if superClass == NoSymbol || superClass == defn.ObjectClass
      then genTypeName(defnNir.NObjectClass)
      else genTypeName(superClass)
    }
  }

  private def genClassInterfaces(sym: ClassSymbol): Seq[nir.Global] = {
    val isExtern = sym.isExternType
    def validate(clsSym: ClassSymbol) = {
      val parentIsExtern = clsSym.isExternType
      if isExtern && !parentIsExtern then
        report.error(
          "Extern object can only extend extern traits",
          clsSym.sourcePos
        )

      if !isExtern && parentIsExtern then
        report.error(
          "Extern traits can be only mixed with extern traits or objects",
          sym.sourcePos
        )
    }

    for
      sym <- sym.info.parents
      clsSym = sym.classSymbol.asClass
      if clsSym.isTraitOrInterface
      _ = validate(clsSym)
    yield genTypeName(clsSym)
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
      if (isExtern && !mutable) {
        report.error("`extern` cannot be used in val definition")
      }
      // That what JVM backend does
      // https://github.com/lampepfl/dotty/blob/786ad3ff248cca39e2da80c3a15b27b38eec2ff6/compiler/src/dotty/tools/backend/jvm/BTypesFromSymbols.scala#L340-L347
      val attrs = nir.Attrs(
        isExtern = isExtern,
        isVolatile = f.isVolatile,
        isFinal = !f.is(Mutable)
      )
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
        case _ if sym.isConstructor && sym.isExtern =>
          validateExternCtor(dd.rhs)
          None

        case _ if sym.isClassConstructor && owner.isStruct =>
          None

        case rhs if sym.isExtern =>
          checkExplicitReturnTypeAnnotation(dd, "extern method")
          genExternMethod(attrs, name, sig, dd)

        case _ if sym.hasAnnotation(defnNir.ResolvedAtLinktimeClass) =>
          genLinktimeResolved(dd, name)

        case rhs =>
          scoped(
            curMethodSig := sig
          ) {
            curMethodUsesLinktimeResolvedValues = false
            val body = genMethodBody(dd, rhs)
            val methodAttrs =
              if (curMethodUsesLinktimeResolvedValues)
                attrs.copy(isLinktimeResolved = true)
              else attrs
            val defn = Defn.Define(methodAttrs, name, sig, body)
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
      sym.annotations.map(_.symbol).collect {
        case defnNir.NoInlineClass     => Attr.NoInline
        case defnNir.AlwaysInlineClass => Attr.AlwaysInline
        case defnNir.InlineClass       => Attr.InlineHint
        case defnNir.NoOptimizeClass   => Attr.NoOpt
        case defnNir.NoSpecializeClass => Attr.NoSpecialize
        case defnNir.StubClass         => Attr.Stub
      }
    val externAttrs = Option.when(sym.isExtern) {
      Attr.Extern(sym.isBlocking || sym.owner.isBlocking)
    }

    Attrs.fromSeq(inlineAttrs ++ annotatedAttrs ++ externAttrs)
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
    val retty = genType(dd.tpt.tpe)

    import LinktimeProperty.Type._
    dd match {
      case LinktimeProperty(propertyName, Provided, _) =>
        if (dd.rhs.symbol == defnNir.UnsafePackage_resolved) Some {
          checkExplicitReturnTypeAnnotation(dd, "value resolved at link-time")
          genLinktimeResolvedMethod(dd, retty, name) {
            _.call(
              Linktime.PropertyResolveFunctionTy(retty),
              Linktime.PropertyResolveFunction(retty),
              Val.String(propertyName) :: Nil,
              Next.None
            )
          }
        }
        else {
          report.error(
            s"Link-time resolved property must have ${defnNir.UnsafePackage_resolved.fullName} as body",
            dd.sourcePos
          )
          None
        }

      case LinktimeProperty(_, Calculated, _) =>
        Some {
          genLinktimeResolvedMethod(dd, retty, name) { buf =>
            def resolve(tree: Tree): nir.Val = tree match {
              case Literal(Constant(_)) =>
                buf.genExpr(tree)
              case If(cond, thenp, elsep) =>
                buf.genIf(retty, cond, thenp, elsep, ensureLinktime = true)
              case tree: Apply if retty == nir.Type.Bool =>
                val True = ValTree(nir.Val.True)
                val False = ValTree(nir.Val.False)
                buf.genIf(retty, tree, True, False, ensureLinktime = true)
              case Block(stats, expr) =>
                stats.foreach { v =>
                  report.error(
                    "Linktime resolved block can only contain other linktime resolved def defintions",
                    v.srcPos
                  )
                  // unused, generated to prevent compiler plugin crash when referencing ident
                  buf.genExpr(v)
                }
                expr match {
                  case Typed(Ident(_), _) | Ident(_) =>
                    report.error(
                      "Non-inlined terms are not allowed in linktime resolved methods",
                      expr.srcPos
                    )
                    Val.Zero(retty)
                  case Typed(tree, _) => resolve(tree)
                  case tree           => resolve(tree)
                }
            }
            resolve(dd.rhs)
          }
        }

      case _ =>
        report.error(
          "Cannot transform to linktime resolved expression",
          dd.srcPos
        )
        None
    }
  }

  private def genLinktimeResolvedMethod(
      dd: DefDef,
      retty: nir.Type,
      methodName: nir.Global
  )(genValue: ExprBuffer => nir.Val)(using nir.Position): nir.Defn = {
    implicit val fresh: Fresh = Fresh()
    val buf = new ExprBuffer()

    scoped(
      curFresh := fresh,
      curMethodSym := dd.symbol,
      curMethodThis := None,
      curMethodEnv := new MethodEnv(fresh),
      curMethodInfo := new CollectMethodInfo,
      curUnwindHandler := None
    ) {
      buf.label(fresh())
      val value = genValue(buf)
      buf.ret(value)
    }

    Defn.Define(
      Attrs(inlineHint = Attr.AlwaysInline, isLinktimeResolved = true),
      methodName,
      Type.Function(Seq.empty, retty),
      buf.toSeq
    )
  }

  def genExternMethod(
      attrs: nir.Attrs,
      name: nir.Global,
      origSig: nir.Type,
      dd: DefDef
  ): Option[Defn] = {
    val rhs: Tree = dd.rhs
    given nir.Position = rhs.span
    def externMethodDecl() = {
      val externSig = genExternMethodSig(curMethodSym)
      val externDefn = Defn.Declare(attrs, name, externSig)
      Some(externDefn)
    }

    def isExternMethodAlias(target: Symbol) = (name, genName(target)) match {
      case (Global.Member(_, lsig), Global.Member(_, rsig)) => lsig == rsig
      case _                                                => false
    }
    val defaultArgs = dd.paramss.flatten.filter(_.symbol.is(HasDefault))

    rhs match {
      case _
          if defaultArgs.nonEmpty || dd.name.is(NameKinds.DefaultGetterName) =>
        report.error("extern method cannot have default argument")
        None
      case Apply(ref: RefTree, Seq())
          if ref.symbol == defnNir.UnsafePackage_extern =>
        externMethodDecl()

      case _ if curMethodSym.get.isOneOf(Accessor | Synthetic) => None

      case Apply(target, args) if target.symbol.isExtern =>
        val sym = target.symbol
        val Global.Member(_, selfSig) = name: @unchecked
        def isExternMethodForwarder =
          genExternSig(sym) == selfSig &&
            genExternMethodSig(sym) == origSig

        if isExternMethodForwarder then externMethodDecl()
        else {
          report.error(
            "Referencing other extern symbols in not supported",
            dd.sourcePos
          )
          None
        }

      case _ =>
        report.error(
          s"methods in extern objects must have extern body",
          rhs.sourcePos
        )
        None
    }
  }

  def validateExternCtor(rhs: Tree): Unit = {
    val Block(exprs, _) = rhs: @unchecked
    val classSym = curClassSym.get

    val externs = collection.mutable.Set.empty[Symbol]
    def isExternCall(tree: Tree): Boolean = tree match
      case Apply(extern, _) =>
        extern.symbol == defnNir.UnsafePackage_extern
      case _ => false

    def isCurClassSetter(sym: Symbol) =
      sym.isSetter && sym.owner.typeRef <:< classSym.typeRef

    exprs.foreach {
      case Assign(ref: RefTree, rhs) if isExternCall(rhs) =>
        externs += ref.symbol

      case Apply(ref: RefTree, Seq(arg))
          if isCurClassSetter(ref.symbol) && isExternCall(arg) =>
        externs += ref.symbol

      case tree @ Apply(ref, _) if ref.symbol.isConstructor =>
        ()

      case tree =>
        report.error(
          s"extern objects may only contain extern fields and methods",
          rhs.sourcePos
        )
    }

    def isInheritedField(f: Symbol) =
      classSym.directlyInheritedTraits.exists {
        _.info.decls.exists(_ matches f.getter)
      }

    for f <- classSym.info.decls
    do {
      // Exclude fields derived from extern trait
      if (f.isField && !isInheritedField(f) && !f.is(Module)) {
        if !(externs.contains(f) || externs.contains(f.setter)) then
          report.error(
            s"extern objects may only contain extern fields",
            f.sourcePos
          )
      }
    }
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
      if (moduleClass.isExternType) Nil
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

  protected object LinktimeProperty {
    enum Type:
      case Provided, Calculated

    def unapply(tree: Tree): Option[(String, Type, nir.Position)] = {
      if (tree.symbol == null) None
      else {
        tree.symbol
          .getAnnotation(defnNir.ResolvedAtLinktimeClass)
          .flatMap { annot =>
            val pos = positionsConversions.fromSpan(tree.span)
            if annot.arguments.isEmpty then
              val syntheticName = genName(tree.symbol).mangle
              Some(syntheticName, Type.Calculated, pos)
            else
              annot
                .argumentConstantString(0)
                .map((_, Type.Provided, pos))
                .orElse {
                  report.error(
                    "Name used to resolve link-time property needs to be non-null literal constant",
                    tree.sourcePos
                  )
                  None
                }
          }
      }
    }
  }
}

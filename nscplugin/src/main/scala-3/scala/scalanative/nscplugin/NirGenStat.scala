package scala.scalanative
package nscplugin

import scala.language.implicitConversions

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.tpd._
import dotty.tools.dotc.core
import core.Contexts._
import core.Symbols._
import core.Constants._
import core.StdNames._
import core.Flags._
import core.Phases._
import scala.scalanative.nscplugin.CompilerCompat.SymUtilsCompat._

import scala.collection.mutable
import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.nir.Defn.Define.DebugInfo._
import scala.scalanative.util.ScopedVar
import scala.scalanative.util.ScopedVar.{scoped, toValue}
import scala.scalanative.util.unsupported
import dotty.tools.FatalError
import dotty.tools.dotc.report
import dotty.tools.dotc.core.NameKinds
import dotty.tools.dotc.core.Annotations.Annotation

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
    implicit val pos: nir.SourcePosition = td.span
    val sym = td.symbol.asClass
    val attrs = genClassAttrs(td)
    val name = genTypeName(sym)

    def parent = genClassParent(sym)
    def traits = genClassInterfaces(sym)
    generatedDefns += {
      if (sym.isStaticModule) nir.Defn.Module(attrs, name, parent, traits)
      else if (sym.isTraitOrInterface) nir.Defn.Trait(attrs, name, traits)
      else nir.Defn.Class(attrs, name, parent, traits)
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
        nir.Attr.Extern(sym.isBlocking)
      case ann if ann.symbol == defnNir.StubClass => nir.Attr.Stub
      case ann if ann.symbol == defnNir.LinkClass =>
        val Apply(_, Seq(Literal(Constant(name: String)))) =
          ann.tree: @unchecked
        nir.Attr.Link(name)
      case ann if ann.symbol == defnNir.DefineClass =>
        val Apply(_, Seq(Literal(Constant(name: String)))) =
          ann.tree: @unchecked
        nir.Attr.Define(name)
    }
    val isAbstract = Option.when(sym.is(Abstract))(nir.Attr.Abstract)
    nir.Attrs.fromSeq(annotationAttrs ++ isAbstract)
  }

  private def genClassParent(sym: ClassSymbol): Option[nir.Global.Top] = {
    if sym.isExternType && sym.superClass != defn.ObjectClass then
      report.error("Extern object can only extend extern traits", sym.sourcePos)

    Option.unless(
      sym == defnNir.NObjectClass ||
      defnNir.RuntimePrimitiveTypes.contains(sym)
    ) {
      val superClass = sym.superClass
      if superClass == NoSymbol || superClass == defn.ObjectClass
      then genTypeName(defnNir.NObjectClass)
      else genTypeName(superClass)
    }
  }

  private def genClassInterfaces(sym: ClassSymbol): Seq[nir.Global.Top] = {
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

  private def getAlignmentAttr(sym: Symbol): Option[nir.Attr.Alignment] =
    sym.getAnnotation(defnNir.AlignClass).map { annot =>
      val groupName = annot
        .argumentConstantString(1)
        .orElse(annot.argumentConstantString(0))

      def getFixedAlignment() = annot
        .argumentConstant(0)
        .filter(_.isIntRange)
        .map(_.intValue)
        .map { value =>
          if value % 8 != 0 || value <= 0 || value > 8192
          then
            report.error(
              "Alignment must be positive integer literal, multiple of 8, and less then 8192 (inclusive)",
              annot.tree.srcPos
            )
          value
        }
      def linktimeResolvedAlignment = annot
        .argument(0)
        .collectFirst {
          // explicitly @align(contendedPaddingWidth)
          case LinktimeProperty(
                "scala.scalanative.meta.linktimeinfo.contendedPaddingWidth",
                _,
                _
              ) =>
            nir.Attr.Alignment.linktimeResolved
        }
        .getOrElse(
          // implicitly, @align() or @align(group)
          nir.Attr.Alignment.linktimeResolved
        )

      nir.Attr.Alignment(
        size = getFixedAlignment().getOrElse(linktimeResolvedAlignment),
        group = groupName.filterNot(_.isEmpty())
      )
    }

  private def genClassFields(td: TypeDef): Unit = {
    val classSym = td.symbol.asClass
    assert(
      curClassSym.get == classSym,
      "genClassFields called with a ClassDef other than the current one"
    )

    val classAlignment = getAlignmentAttr(td.symbol)
    // Term members that are neither methods nor modules are fields
    for
      f <- classSym.info.decls.toList
      if !f.isOneOf(Method | Module) && f.isTerm
    do
      given nir.SourcePosition = f.span.orElse(td.span)
      val isStatic = f.is(JavaStatic) || f.isScalaStatic
      val isExtern = f.isExtern
      val mutable = isStatic || f.is(Mutable)
      if (isExtern && !mutable) {
        report.error("`extern` cannot be used in val definition")
      }
      // That what JVM backend does
      // https://github.com/lampepfl/dotty/blob/786ad3ff248cca39e2da80c3a15b27b38eec2ff6/compiler/src/dotty/tools/backend/jvm/BTypesFromSymbols.scala#L340-L347
      val isFinal = !f.is(Mutable)
      val attrs = nir.Attrs(
        isExtern = isExtern,
        isVolatile = f.isVolatile,
        isFinal = isFinal,
        isSafePublish = isFinal && {
          settings.forceStrictFinalFields ||
          f.hasAnnotation(defnNir.SafePublishClass) ||
          f.owner.hasAnnotation(defnNir.SafePublishClass)
        },
        align = getAlignmentAttr(f).orElse(classAlignment)
      )
      val ty = genType(f.info.resultType)
      val fieldName @ nir.Global.Member(owner, sig) = genFieldName(
        f
      ): @unchecked
      generatedDefns += nir.Defn.Var(attrs, fieldName, ty, nir.Val.Zero(ty))

      if (isStatic) {
        // Here we are generating a public static getter for the static field,
        // this is its API for other units. This is necessary for singleton
        // enum values, which are backed by static fields.
        generatedDefns += new nir.Defn.Define(
          attrs = nir.Attrs(inlineHint = nir.Attr.InlineHint),
          name = genStaticMemberName(f, classSym),
          ty = nir.Type.Function(Nil, ty),
          insts = withFreshExprBuffer { buf ?=>
            given nir.ScopeId = nir.ScopeId.TopLevel
            val fresh = curFresh.get
            buf.label(fresh())
            val module = buf.module(genModuleName(classSym), nir.Next.None)
            val value = buf.fieldload(ty, module, fieldName, nir.Next.None)
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
          case dd: DefDef =>
            genMethod(dd) ++ genInterfaceMethodBridgeForDefDef(dd)
          case _ => Nil // erased
        }
      case tree =>
        throw new FatalError("Illegal tree in body of genMethods():" + tree)
    }

    generatedDefns ++= methods
    generatedDefns ++= genStaticMethodForwarders(td, methods)
    generatedDefns ++= genTopLevelExports(td)
  }

  private def genMethod(dd: DefDef): Option[nir.Defn] = {
    implicit val pos: nir.SourcePosition = dd.span
    if (pos.isEmpty) println(dd.name -> dd.span)
    val fresh = nir.Fresh()
    val freshScope = initFreshScope(dd.rhs)
    val scopes = mutable.Set.empty[DebugInfo.LexicalScope]
    scopes += DebugInfo.LexicalScope.TopLevel(dd.rhs.span)

    scoped(
      curMethodSym := dd.symbol,
      curMethodEnv := new MethodEnv(fresh),
      curMethodLabels := new MethodLabelsEnv(fresh),
      curMethodInfo := CollectMethodInfo().collect(dd.rhs),
      curFresh := fresh,
      curFreshScope := freshScope,
      curScopeId := nir.ScopeId.TopLevel,
      curScopes := scopes,
      curUnwindHandler := None,
      curMethodLocalNames := localNamesBuilder()
    ) {
      val sym = dd.symbol
      val owner = curClassSym.get

      val isExtern = sym.isExtern

      val attrs = genMethodAttrs(sym, isExtern)
      val name = genMethodName(sym)
      val sig = genMethodSig(sym)

      dd.rhs match {
        case EmptyTree => Some(nir.Defn.Declare(attrs, name, sig))
        case _ if sym.isConstructor && isExtern =>
          validateExternCtor(dd.rhs)
          None

        case _ if sym.isClassConstructor && owner.isStruct =>
          None

        case rhs if isExtern =>
          checkExplicitReturnTypeAnnotation(dd, "extern method")
          genExternMethod(attrs, name, sig, dd)

        case _ if sym.hasAnnotation(defnNir.ResolvedAtLinktimeClass) =>
          genLinktimeResolved(dd, name)

        case rhs =>
          scoped(
            curMethodSig := sig
          ) {
            val body = genMethodBody(dd, rhs, isExtern)
            val env = curMethodEnv.get
            val methodAttrs =
              if (env.isUsingLinktimeResolvedValue || env.isUsingIntrinsics)
                attrs.copy(
                  isLinktimeResolved = env.isUsingLinktimeResolvedValue,
                  isUsingIntrinsics = env.isUsingIntrinsics
                )
              else attrs
            val defn = nir.Defn.Define(
              methodAttrs,
              name,
              sig,
              insts = body,
              debugInfo = nir.Defn.Define.DebugInfo(
                localNames = curMethodLocalNames.get.toMap,
                lexicalScopes = scopes.toList
              )
            )
            Some(defn)
          }
      }
    }
  }

  private def genMethodAttrs(
      sym: Symbol,
      isExtern: Boolean
  ): nir.Attrs = {
    val attrs = Seq.newBuilder[nir.Attr]

    if (sym.is(Bridge) || sym.is(Accessor))
      attrs += nir.Attr.AlwaysInline
    if (isExtern)
      attrs += nir.Attr.Extern(sym.isBlocking || sym.owner.isBlocking)

    def requireLiteralStringAnnotation(annotation: Annotation): Option[String] =
      annotation.tree match {
        case Apply(_, Seq(Literal(Constant(name: String)))) => Some(name)
        case tree =>
          report.error(
            s"Invalid usage of ${annotation.symbol.show}, expected literal constant string argument, got ${tree}",
            tree.srcPos
          )
          None
      }
    sym.annotations.foreach { ann =>
      ann.symbol match {
        case defnNir.NoInlineClass     => attrs += nir.Attr.NoInline
        case defnNir.AlwaysInlineClass => attrs += nir.Attr.AlwaysInline
        case defnNir.InlineClass       => attrs += nir.Attr.InlineHint
        case defnNir.NoOptimizeClass   => attrs += nir.Attr.NoOpt
        case defnNir.NoSpecializeClass => attrs += nir.Attr.NoSpecialize
        case defnNir.StubClass         => attrs += nir.Attr.Stub
        case defnNir.LinkClass =>
          requireLiteralStringAnnotation(ann)
            .foreach(attrs += nir.Attr.Link(_))
        case defnNir.DefineClass =>
          requireLiteralStringAnnotation(ann)
            .foreach(attrs += nir.Attr.Define(_))
        case _ => ()
      }
    }
    nir.Attrs.fromSeq(attrs.result())
  }

  protected val curExprBuffer = ScopedVar[ExprBuffer]()
  private def genMethodBody(
      dd: DefDef,
      bodyp: Tree,
      isExtern: Boolean
  ): Seq[nir.Inst] = {
    given nir.SourcePosition = bodyp.span.orElse(dd.span).orElse(dd.symbol.span)
    given fresh: nir.Fresh = curFresh.get
    val buf = ExprBuffer()
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
      val name = genLocalName(sym)
      val param = nir.Val.Local(fresh.namedId(genLocalName(sym)), ty)
      curMethodEnv.enter(sym, param)
      param
    }
    val thisParam = Option.unless(isStatic) {
      nir.Val.Local(
        fresh.namedId("this"),
        genType(curClassSym.get)
      )
    }
    val params = thisParam.toList ::: argParams

    def genEntry(): Unit = {
      buf.label(fresh(), params)
    }

    def genVars(): Unit = {
      val vars = curMethodInfo.mutableVars
        .foreach { sym =>
          val ty = genType(sym.info)
          val name = genLocalName(sym)
          val slot = buf.let(fresh.namedId(name), nir.Op.Var(ty), unwind(fresh))
          curMethodEnv.enter(sym, slot)
        }
    }

    def withOptSynchronized(bodyGen: ExprBuffer => nir.Val): nir.Val = {
      if (!isSynchronized) bodyGen(buf)
      else {
        val syncedIn = curMethodThis.getOrElse {
          unsupported(
            s"cannot generate `synchronized` for method ${curMethodSym.name}, curMethodThis was empty"
          )
        }
        buf.genSynchronized(ValTree(dd)(syncedIn))(bodyGen)
      }
    }
    def genBody(): Unit = {
      if (curMethodSym.get == defnNir.NObject_init)
        scoped(
          curMethodIsExtern := isExtern
        ) {
          buf.genReturn(nir.Val.Unit)
        }
      else
        scoped(curMethodThis := thisParam, curMethodIsExtern := isExtern) {
          buf.genReturn(withOptSynchronized(_.genExpr(bodyp)) match {
            case nir.Val.Zero(_) =>
              nir.Val.Zero(genType(curMethodSym.get.info.resultType))
            case v => v
          })
        }
    }

    scoped(curExprBuffer := buf) {
      genEntry()
      genVars()
      genBody()
      nir.ControlFlow.removeDeadBlocks(buf.toSeq)
    }
  }

  private def genStruct(td: TypeDef): Unit = {
    given nir.SourcePosition = td.span

    val sym = td.symbol
    val attrs = nir.Attrs.None
    val name = genTypeName(sym)

    generatedDefns += nir.Defn.Class(attrs, name, None, Seq.empty)
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

  protected def genLinktimeResolved(dd: DefDef, name: nir.Global.Member)(using
      nir.SourcePosition
  ): Option[nir.Defn] = {
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
              nir.Linktime.PropertyResolveFunctionTy(retty),
              nir.Linktime.PropertyResolveFunction(retty),
              nir.Val.String(propertyName) :: Nil,
              nir.Next.None
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
                val True = ValTree(dd)(nir.Val.True)
                val False = ValTree(dd)(nir.Val.False)
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
                    nir.Val.Zero(retty)
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
      methodName: nir.Global.Member
  )(genValue: ExprBuffer => nir.Val)(using nir.SourcePosition): nir.Defn = {
    implicit val fresh: nir.Fresh = nir.Fresh()
    val freshScopes = initFreshScope(dd.rhs)
    val buf = new ExprBuffer()

    scoped(
      curFresh := fresh,
      curFreshScope := freshScopes,
      curScopeId := nir.ScopeId.TopLevel,
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

    new nir.Defn.Define(
      nir.Attrs(inlineHint = nir.Attr.AlwaysInline, isLinktimeResolved = true),
      methodName,
      nir.Type.Function(Seq.empty, retty),
      buf.toSeq
    )
  }

  private object ApplyExtern {
    def unapply(tree: Tree): Boolean = tree match {
      case Apply(ref: RefTree, Seq()) =>
        ref.symbol == defnNir.UnsafePackage_extern
      case _ => false
    }
  }
  def genExternMethod(
      attrs: nir.Attrs,
      name: nir.Global.Member,
      origSig: nir.Type,
      dd: DefDef
  ): Option[nir.Defn] = {
    val rhs: Tree = dd.rhs
    given nir.SourcePosition = rhs.span
    def externMethodDecl() = {
      val externSig = genExternMethodSig(curMethodSym)
      val externDefn = nir.Defn.Declare(attrs, name, externSig)
      Some(externDefn)
    }

    def isExternMethodAlias(target: Symbol) = (name, genName(target)) match {
      case (nir.Global.Member(_, lsig), nir.Global.Member(_, rsig)) =>
        lsig == rsig
      case _ => false
    }
    val defaultArgs = dd.paramss.flatten.filter(_.symbol.is(HasDefault))

    rhs match {
      case _
          if defaultArgs.nonEmpty || dd.name.is(NameKinds.DefaultGetterName) =>
        report.error("extern method cannot have default argument")
        None

      case ApplyExtern() => externMethodDecl()

      case _ if curMethodSym.get.isOneOf(Accessor | Synthetic) => None

      case Apply(target, args) if target.symbol.isExtern =>
        val sym = target.symbol
        val nir.Global.Member(_, selfSig) = name: @unchecked
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
        _.info.decls.exists(_.matches(f.getter))
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
      existingMembers: Seq[nir.Defn],
      sym: Symbol
  ): Seq[nir.Defn.Define] = {
    val module = sym.companionModule
    if (!module.exists) Nil
    else {
      val moduleClass = module.moduleClass
      if (moduleClass.isExternType) Nil
      else genStaticForwardersFromModuleClass(existingMembers, moduleClass)
    }
  }

  /** Gen the static forwarders for the methods of a module class. l
   *  Precondition: `isCandidateForForwarders(moduleClass)` is true
   */
  private def genStaticForwardersFromModuleClass(
      existingMembers: Seq[nir.Defn],
      moduleClass: Symbol
  ): Seq[nir.Defn.Define] = {
    assert(moduleClass.is(ModuleClass), moduleClass)

    val existingStaticMethodNames: Set[nir.Global] = existingMembers.collect {
      case nir.Defn.Define(_, name @ nir.Global.Member(_, sig), _, _, _)
          if sig.isStatic =>
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
      given nir.SourcePosition = sym.span.orElse(moduleClass.span)

      val methodName = genMethodName(sym)
      val forwarderName = genStaticMemberName(sym, moduleClass)
      val nir.Type.Function(_ +: paramTypes, retType) =
        genMethodSig(sym): @unchecked
      val forwarderParamTypes = paramTypes
      val forwarderType = nir.Type.Function(forwarderParamTypes, retType)

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

      new nir.Defn.Define(
        attrs = nir.Attrs(inlineHint = nir.Attr.InlineHint),
        name = forwarderName,
        ty = forwarderType,
        insts = withFreshExprBuffer { buf ?=>
          val fresh = curFresh.get
          scoped(
            curUnwindHandler := None,
            curMethodThis := None,
            curScopeId := nir.ScopeId.TopLevel
          ) {
            val entryParams = forwarderParamTypes.map(nir.Val.Local(fresh(), _))
            val args = entryParams.map(ValTree(_)(sym.span))
            buf.label(fresh(), entryParams)
            val res = buf.genApplyModuleMethod(moduleClass, sym, args)
            buf.ret(res)
          }
          buf.toSeq
        }
      )
    }
  }

  private def genInterfaceMethodBridgeForDefDef(dd: DefDef): Seq[nir.Defn] =
    val sym = dd.symbol
    sym.owner.directlyInheritedTraits
      .flatMap { parent =>
        val inheritedSym = parent.info.decl(sym.name)
        Option.when(
          inheritedSym.exists &&
          inheritedSym.symbol.is(Deferred) &&
          sym.signature != inheritedSym.signature &&
          sym.info <:< inheritedSym.info
        )(inheritedSym.symbol.asTerm)
      }
      .distinctBy(_.signature)
      .flatMap(genInterfaceMethodBridge(sym.asTerm, _))

  private def genInterfaceMethodBridge(
      sym: TermSymbol,
      inheritedSym: TermSymbol
  ): Option[nir.Defn] = {
    assert(sym.name == inheritedSym.name, "Not an override")
    val owner = sym.owner.asClass
    val bridgeSym = inheritedSym.copy(owner = owner, flags = sym.flags).asTerm
    val bridge = tpd
      .DefDef(
        bridgeSym,
        { paramss =>
          val params = paramss.head
          tpd.Apply(tpd.This(owner).select(sym), params)
        }
      )
      .withSpan(sym.span)
    genMethod(bridge)
  }

  private def genStaticMethodForwarders(
      td: TypeDef,
      existingMethods: Seq[nir.Defn]
  ): Seq[nir.Defn] = {
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
    given pos: nir.SourcePosition = td.span
    val sym = td.symbol
    val isTopLevelModuleClass = sym.is(ModuleClass) &&
      atPhase(flattenPhase) {
        toDenot(sym).owner.is(PackageClass)
      }
    if isTopLevelModuleClass && sym.companionClass == NoSymbol then {
      val classDefn = nir.Defn.Class(
        attrs = nir.Attrs.None,
        name = nir.Global.Top(genTypeName(sym).id.stripSuffix("$")),
        parent = Some(nir.Rt.Object.name),
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

    def unapply(tree: Tree): Option[(String, Type, nir.SourcePosition)] = {
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

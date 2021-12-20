package scala.scalanative.nscplugin

import scala.language.implicitConversions

import dotty.tools.dotc.ast.tpd._
import dotty.tools.dotc.core
import core.Contexts._
import core.Symbols._
import core.Constants._
import core.StdNames._
import core.Flags._
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
    implicit val pos: nir.Position = td.span
    val sym = td.symbol.asClass
    val attrs = genClassAttrs(td)
    val name = genName(sym)

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
  }

  private def genClassAttrs(td: TypeDef): nir.Attrs = {
    val sym = td.symbol.asClass
    val annotationAttrs = sym.annotations.collect {
      case ann if ann.symbol == defnNir.ExternClass => Attr.Extern
      case ann if ann.symbol == defnNir.StubClass   => Attr.Stub
      case ann if ann.symbol == defnNir.LinkClass =>
        val Apply(_, Seq(Literal(Constant(name: String)))) = ann.tree
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

      val isStaticField =
        f.is(JavaStatic) || f.hasAnnotation(defn.StaticAnnotationClass)
      val mutable = isStaticField || f.is(Mutable)
      val isExternModule = classSym.isExternModule
      val attrs = nir.Attrs(isExtern = isExternModule)
      val ty = genType(f.info.resultType)
      val fieldName @ Global.Member(owner, sig) = genName(f)
      generatedDefns += Defn.Var(attrs, fieldName, ty, Val.Zero(ty))

      if (isStaticField) {
        // Here we are generating a public static getter for the static field,
        // this is its API for other units. This is necessary for singleton
        // enum values, which are backed by static fields.
        generatedDefns += Defn.Define(
          attrs = Attrs(inlineHint = nir.Attr.InlineHint),
          name = genMethodName(f),
          ty = Type.Function(Nil, ty),
          insts = {
            given fresh: Fresh = Fresh()
            given buf: ExprBuffer = ExprBuffer()

            buf.label(fresh())
            val module = buf.module(owner, Next.None)
            val value = buf.fieldload(ty, module, fieldName, Next.None)
            buf.ret(value)

            buf.toSeq
          }
        )
      }
  }

  private def genMethods(td: TypeDef): Unit = {
    val tpl = td.rhs.asInstanceOf[Template]
    (tpl.constr :: tpl.body).foreach {
      case EmptyTree  => ()
      case _: ValDef  => () // handled in genClassFields
      case _: TypeDef => ()
      case dd: DefDef => genMethod(dd)
      case tree =>
        throw new FatalError("Illegal tree in body of genMethods():" + tree)
    }

    scoped(
      curUnwindHandler := None
    ) {
      genStaticMethodForwarders(td)
    }
  }

  // In Scala Native we don't have a limited access for static members.
  // Make sure that we can always call static methods, by generating a forwarder
  // inside a companion module. It is need to handle methods created by compiler
  // with JavaStatic flag, eg main method using @main annotation
  private def genStaticMethodForwarders(td: TypeDef): Unit = {
    val sym = td.symbol.asClass
    val moduleName = genModuleName(sym)
    val staticMethods =
      sym.info.allMembers
        .map(_.symbol)
        .filter(_.isStaticMethod)

    if (!sym.isStaticModule && staticMethods.nonEmpty) {
      val module = sym.companionModule
      if (!module.exists) {
        given nir.Position = td.span
        generatedDefns += Defn.Module(
          attrs = Attrs.None,
          name = moduleName,
          parent = Some(Rt.Object.name),
          traits = Nil
        )
      }

      staticMethods.foreach { sym =>
        given nir.Position = sym.span

        val methodName @ Global.Member(_, methodSig) = genMethodName(sym)
        val methodType @ Type.Function(origOwner +: paramTypes, retType) =
          genMethodSig(sym)

        val selfType = Type.Ref(moduleName)
        val forwarderName = moduleName.member(methodSig)
        val forwarderParamTypes = selfType +: paramTypes
        val forwarderSig = Type.Function(forwarderParamTypes, retType)

        generatedDefns += Defn.Define(
          attrs = Attrs(inlineHint = nir.Attr.InlineHint),
          name = forwarderName,
          ty = forwarderSig,
          insts = {
            given fresh: Fresh = Fresh()
            given buf: ExprBuffer = ExprBuffer()

            val entryParams @ (self +: params) =
              forwarderParamTypes.map(Val.Local(fresh(), _))
            buf.label(fresh(), entryParams)
            val result = buf.call(
              methodType,
              Val.Global(methodName, Type.Ptr),
              Val.Null +: params,
              Next.None
            )
            buf.ret(result)

            buf.toSeq
          }
        )
      }
    }
  }

  private def genMethod(dd: DefDef): Unit = {
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
      val isStatic = sym.isExtern

      dd.rhs match {
        case EmptyTree =>
          generatedDefns += Defn.Declare(attrs, name, sig)
        case _ if sym.isClassConstructor && sym.isExtern =>
          validateExternCtor(dd.rhs)
          ()

        case _ if sym.isClassConstructor && owner.isStruct => ()

        case rhs if sym.isExtern =>
          checkExplicitReturnTypeAnnotation(dd, "extern method")
          genExternMethod(attrs, name, sig, rhs)

        case _ if sym.hasAnnotation(defnNir.ResolvedAtLinktimeClass) =>
          genLinktimeResolved(dd, name)

        case rhs =>
          scoped(
            curMethodSig := sig
          ) {
            generatedDefns += Defn.Define(
              attrs,
              name,
              sig,
              genMethodBody(dd, rhs, isStatic, isExtern = false)
            )
          }
      }
    }
  }

  private def genMethodAttrs(sym: Symbol): nir.Attrs = {
    val inlineAttrs =
      if (sym.is(Bridge) || sym.is(Accessor)) {
        Seq(Attr.AlwaysInline)
      } else {
        sym.annotations.map(_.symbol).collect {
          case s if s == defnNir.NoInlineClass     => Attr.NoInline
          case s if s == defnNir.AlwaysInlineClass => Attr.AlwaysInline
          case s if s == defnNir.InlineClass       => Attr.InlineHint
        }
      }

    val optAttrs =
      sym.annotations.collect {
        case ann if ann.symbol == defnNir.NoOptimizeClass   => Attr.NoOpt
        case ann if ann.symbol == defnNir.NoSpecializeClass => Attr.NoSpecialize
      }

    val isStub = sym.hasAnnotation(defnNir.StubClass)
    val isExtern = sym.owner.hasAnnotation(defnNir.ExternClass)

    Attrs
      .fromSeq(inlineAttrs ++ optAttrs)
      .copy(
        isExtern = isExtern,
        isStub = isStub
      )
  }

  protected val curExprBuffer = ScopedVar[ExprBuffer]()
  private def genMethodBody(
      dd: DefDef,
      bodyp: Tree,
      isStatic: Boolean,
      isExtern: Boolean
  ): Seq[nir.Inst] = {
    given nir.Position = bodyp.span
    given fresh: nir.Fresh = curFresh.get
    val buf = ExprBuffer()

    val sym = curMethodSym.get
    val argParamSyms = for {
      paramList <- dd.paramss.take(1)
      param <- paramList
    } yield param.symbol
    val argParams = argParamSyms.map { sym =>
      val tpe = sym.info.resultType
      val ty =
        if (isExtern) genExternType(tpe)
        else genType(tpe)
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

    val isSynchronized = dd.symbol.is(Synchronized)

    def genEntry(): Unit = {
      buf.label(fresh(), params)
      if (isExtern) {
        argParamSyms.zip(argParams).foreach {
          case (sym, param) =>
            val tpe = sym.info.resultType
            val ty = genType(tpe)
            val value = buf.fromExtern(ty, param)
            curMethodEnv.enter(sym, value)
        }
      }
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
          curMethodOuterSym := outerParam,
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

  private def checkExplicitReturnTypeAnnotation(
      externMethodDd: DefDef,
      methodKind: String
  ): Unit = {
    if (externMethodDd.tpt.symbol == defn.NothingClass)
      report.error(
        s"$methodKind ${externMethodDd.name} needs result type",
        externMethodDd.sourcePos
      )
  }

  protected def genLinktimeResolved(dd: DefDef, name: Global)(using
      nir.Position
  ): Unit = {
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
          genLinktimeResolvedMethod(retty, propertyName, name)

        case _ => () // no-op
      }
    } else
      report.error(
        s"Link-time resolved property must have ${defnNir.UnsafePackage_resolved.fullName} as body",
        dd.sourcePos
      )
  }

  /* Generate stub method that can be used to get value of link-time property at runtime */
  private def genLinktimeResolvedMethod(
      retty: nir.Type,
      propertyName: String,
      methodName: nir.Global
  )(using nir.Position): Unit = {
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

    generatedDefns += Defn.Define(
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
  ): Unit = {
    given nir.Position = rhs.span
    rhs match {
      case Apply(ref: RefTree, Seq())
          if ref.symbol == defnNir.UnsafePackage_extern =>
        val moduleName = genTypeName(curClassSym)
        val externAttrs = Attrs(isExtern = true)
        val externSig = genExternMethodSig(curMethodSym)
        generatedDefns += Defn.Declare(externAttrs, name, externSig)
      case _ if curMethodSym.get.isOneOf(Accessor | Synthetic) =>
        () // no-op
      case rhs =>
        report.error(
          s"methods in extern objects must have extern body  - ${rhs}",
          rhs.sourcePos
        )
    }
  }

  def validateExternCtor(rhs: Tree): Unit = {
    val Block(_ +: init, _) = rhs
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

}

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

trait NirGenDefn(using Context) {
  self: NirCodeGen =>
  import positionsConversions.fromSpan

  protected val generatedDefns = mutable.UnrolledBuffer.empty[nir.Defn]

  def genClass(td: TypeDef)(using Context): Unit = {
    scoped(
      curClassSym := td.symbol,
      curClassFresh := nir.Fresh()
    ) {
      genNormalClass(td)
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
      if (sym.isScalaModule)
        Defn.Module(attrs, name, parent, traits)
      else if (sym.isTraitOrInterface)
        Defn.Trait(attrs, name, traits)
      else
        Defn.Class(attrs, name, parent, traits)
    }
    genClassFields(td)
    genMethods(td)

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
    val sym = td.symbol.asClass
    val isExternModule = sym.isExternModule
    val attrs = nir.Attrs(isExtern = isExternModule)

    for
      sym <- sym.info.decls.toList
      if sym.isField && !sym.is(Module)
    do
      given nir.Position = sym.span
      val ty = genType(sym.info.resultType)
      val name = genFieldName(sym)
      generatedDefns += Defn.Var(attrs, name, ty, Val.Zero(ty))
    end for
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
      val isStatic = owner.isExternModule || dd.symbol.is(JavaStatic)

      dd.rhs match {
        case EmptyTree =>
          generatedDefns += Defn.Declare(attrs, name, sig)
        case _ if dd.name == nme.CONSTRUCTOR && owner.isExternModule =>
          // validateExternCtor(dd.rhs)
          ()

        case _ if dd.name == nme.CONSTRUCTOR && owner.isStruct =>
          ()

        case rhs if owner.isExternModule =>
        // checkExplicitReturnTypeAnnotation(dd, "extern method")
        // genExternMethod(attrs, name, sig, rhs)

        case _ if sym.hasAnnotation(defnNir.ResolvedAtLinktimeClass) =>
          ???
        // genLinktimeResolved(dd, name)

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
        sym.annotations.collect {
          case ann if ann.symbol == defnNir.NoInlineClass => Attr.NoInline
          case ann if ann.symbol == defnNir.AlwaysInlineClass =>
            Attr.AlwaysInline
          case ann if ann.symbol == defnNir.InlineClass => Attr.InlineHint
        }
      }

    val stubAttrs =
      sym.annotations.collect {
        case ann if ann.symbol == defnNir.StubClass => Attr.Stub
      }

    val optAttrs =
      sym.annotations.collect {
        case ann if ann.symbol == defnNir.NoOptimizeClass   => Attr.NoOpt
        case ann if ann.symbol == defnNir.NoSpecializeClass => Attr.NoSpecialize
      }

    Attrs.fromSeq(inlineAttrs ++ stubAttrs ++ optAttrs)
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

    val paramSyms = genParamSyms(dd, isStatic)
    val params = paramSyms.map {
      case None =>
        val ty = genType(curClassSym.get)
        Val.Local(fresh(), ty)
      case Some(sym) =>
        val tpe = sym.info.resultType
        val ty =
          if (isExtern) genExternType(tpe)
          else genType(tpe)
        val param = Val.Local(fresh(), ty)
        curMethodEnv.enter(sym, param)
        param
    }

    val isSynchronized = dd.symbol.is(Synchronized)

    def genEntry(): Unit = {
      buf.label(fresh(), params)

      if (isExtern) {
        paramSyms.zip(params).foreach {
          case (Some(sym), param) if isExtern =>
            val tpe = sym.info.resultType
            val ty = genType(tpe)
            val value = ??? //buf.fromExtern(ty, param)
            curMethodEnv.enter(sym, value)
          case _ =>
            ()
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

    def genBody(): Val = bodyp match {
      case _ if curMethodSym.get == defnNir.NObject_init =>
        scoped(
          curMethodIsExtern := isExtern
        ) {
          buf.genReturn(nir.Val.Unit)
        }

      case _ =>
        scoped(
          curMethodThis := {
            if (isStatic) None
            else Some(Val.Local(params.head.name, params.head.ty))
          },
          curMethodIsExtern := isExtern
        ) {
          buf.genReturn(
            withOptSynchronized(_.genExpr(bodyp))
          )
        }
    }
    scoped(curExprBuffer := buf) {
      genEntry()
      genVars()
      genBody()
      ControlFlow.removeDeadBlocks(buf.toSeq)
    }
  }
}

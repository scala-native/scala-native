package scala.scalanative
package nscplugin

import scala.collection.mutable
import scala.reflect.internal.Flags._
import scala.scalanative.nir._
import scala.scalanative.util.unsupported
import scala.scalanative.util.ScopedVar.scoped
import scalanative.nir.ControlFlow.removeDeadBlocks

trait NirGenStat { self: NirGenPhase =>

  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._
  import SimpleType.{fromType, fromSymbol}

  class MethodEnv(val fresh: Fresh) {
    private val env = mutable.Map.empty[Symbol, Val]

    def enter(sym: Symbol, value: Val): Unit = {
      env += ((sym, value))
    }

    def enterLabel(ld: LabelDef): Local = {
      val local = fresh()
      enter(ld.symbol, Val.Local(local, Type.Ptr))
      local
    }

    def resolve(sym: Symbol): Val = {
      env(sym)
    }

    def resolveLabel(ld: LabelDef): Local = {
      val Val.Local(n, Type.Ptr) = resolve(ld.symbol)
      n
    }
  }

  class CollectMethodInfo extends Traverser {
    var mutableVars = Set.empty[Symbol]
    var labels      = Set.empty[LabelDef]

    override def traverse(tree: Tree) = {
      tree match {
        case label: LabelDef =>
          labels += label
        case Assign(id @ Ident(_), _) =>
          mutableVars += id.symbol
        case _ =>
          ()
      }
      super.traverse(tree)
    }

    def collect(tree: Tree) = {
      traverse(tree)
      this
    }
  }

  class StatBuffer {
    private val buf          = mutable.UnrolledBuffer.empty[nir.Defn]
    def toSeq: Seq[nir.Defn] = buf

    def genClass(cd: ClassDef): Unit = {
      scoped(
        curClassSym := cd.symbol
      ) {
        if (cd.symbol.isStruct) genStruct(cd)
        else genNormalClass(cd)
      }
    }

    def genStruct(cd: ClassDef): Unit = {
      val sym    = cd.symbol
      val attrs  = genStructAttrs(sym)
      val name   = genTypeName(sym)
      val fields = genStructFields(sym)
      val body   = cd.impl.body

      buf += Defn.Class(attrs, name, None, Seq.empty)
      genMethods(cd)
    }

    def genStructAttrs(sym: Symbol): Attrs = Attrs.None

    def genNormalClass(cd: ClassDef): Unit = {
      val sym    = cd.symbol
      def attrs  = genClassAttrs(cd)
      def name   = genTypeName(sym)
      def parent = genClassParent(sym)
      def traits = genClassInterfaces(sym)

      genClassFields(sym)
      genMethods(cd)

      buf += {
        if (sym.isScalaModule) Defn.Module(attrs, name, parent, traits)
        else if (sym.isInterface) Defn.Trait(attrs, name, traits)
        else Defn.Class(attrs, name, parent, traits)
      }
    }

    def genClassParent(sym: Symbol): Option[nir.Global] =
      if (sym == NObjectClass) None
      else if (sym.superClass == NoSymbol || sym.superClass == ObjectClass)
        Some(genTypeName(NObjectClass))
      else Some(genTypeName(sym.superClass))

    def genClassAttrs(cd: ClassDef): Attrs = {
      val sym = cd.symbol
      val annotationAttrs = sym.annotations.collect {
        case ann if ann.symbol == ExternClass =>
          Attr.Extern
        case ann if ann.symbol == LinkClass =>
          val Apply(_, Seq(Literal(Constant(name: String)))) = ann.tree
          Attr.Link(name)
        case ann if ann.symbol == StubClass =>
          Attr.Stub
      }
      val abstractAttr =
        if (sym.isAbstract) Seq(Attr.Abstract) else Seq()

      Attrs.fromSeq(annotationAttrs ++ abstractAttr)
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol if psym.isInterface
      } yield {
        genTypeName(psym)
      }

    def genClassFields(sym: Symbol): Unit = {
      val attrs = nir.Attrs(isExtern = sym.isExternModule)

      for (f <- sym.info.decls if f.isField) {
        val ty   = genType(f.tpe, box = false)
        val name = genFieldName(f)

        buf += Defn.Var(attrs, name, ty, Val.Zero(ty))
      }
    }

    def genMethods(cd: ClassDef): Unit =
      cd.impl.body.foreach {
        case dd: DefDef =>
          genMethod(dd)
        case _ =>
          ()
      }

    def genMethod(dd: DefDef): Unit = {
      val fresh = Fresh()
      val env   = new MethodEnv(fresh)

      scoped(
        curMethodSym := dd.symbol,
        curMethodEnv := env,
        curMethodInfo := (new CollectMethodInfo).collect(dd.rhs),
        curFresh := fresh,
        curUnwindHandler := None
      ) {
        val sym      = dd.symbol
        val owner    = curClassSym.get
        val attrs    = genMethodAttrs(sym)
        val name     = genMethodName(sym)
        val isStatic = owner.isExternModule || owner.isImplClass
        val sig      = genMethodSig(sym, isStatic)
        val params   = genParams(dd, isStatic)

        dd.rhs match {
          case EmptyTree =>
            buf += Defn.Declare(attrs, name, sig)

          case _ if dd.name == nme.CONSTRUCTOR && owner.isExternModule =>
            validateExternCtor(dd.rhs)
            ()

          case _ if dd.name == nme.CONSTRUCTOR && owner.isStruct =>
            ()

          case rhs if owner.isExternModule =>
            checkExplicitReturnTypeAnnotation(dd)
            genExternMethod(attrs, name, sig, params, rhs)

          case rhs =>
            val body = genNormalMethodBody(dd, params, rhs, isStatic)
            buf += Defn.Define(attrs, name, sig, body)
        }
      }
    }

    def genExternMethod(attrs: nir.Attrs,
                        name: nir.Global,
                        sig: nir.Type,
                        params: Seq[nir.Val.Local],
                        rhs: Tree): Unit = {
      rhs match {
        case Apply(ref: RefTree, Seq()) if ref.symbol == ExternMethod =>
          val moduleName  = genTypeName(curClassSym)
          val externAttrs = Attrs(isExtern = true)
          val externDefn  = Defn.Declare(externAttrs, name, sig)

          buf += externDefn

        case _ if curMethodSym.hasFlag(ACCESSOR) =>
          ()

        case rhs =>
          unsupported("methods in extern objects must have extern body")
      }
    }

    def validateExternCtor(rhs: Tree): Unit = {
      val Block(_ +: init, _) = rhs
      val externs = init.map {
        case Assign(ref: RefTree, Apply(extern, Seq()))
            if extern.symbol == ExternMethod =>
          ref.symbol
        case _ =>
          unsupported(
            "extern objects may only contain extern fields and methods")
      }.toSet
      for {
        f <- curClassSym.info.decls if f.isField
        if !externs.contains(f)
      } {
        unsupported("extern objects may only contain extern fields")
      }
    }

    def genMethodAttrs(sym: Symbol): Attrs = {
      val inlineAttrs =
        if (sym.isBridge || sym.hasFlag(ACCESSOR)) {
          Seq(Attr.AlwaysInline)
        } else {
          sym.annotations.collect {
            case ann if ann.symbol == NoInlineClass     => Attr.NoInline
            case ann if ann.symbol == AlwaysInlineClass => Attr.AlwaysInline
            case ann if ann.symbol == InlineHintClass   => Attr.InlineHint
            case ann if ann.symbol == InlineClass       => Attr.InlineHint
          }
        }
      val stubAttrs =
        sym.annotations.collect {
          case ann if ann.symbol == StubClass => Attr.Stub
        }
      val optAttrs =
        sym.annotations.collect {
          case ann if ann.symbol == NoOptimizeClass   => Attr.NoOpt
          case ann if ann.symbol == NoSpecializeClass => Attr.NoSpecialize
        }

      Attrs.fromSeq(inlineAttrs ++ stubAttrs ++ optAttrs)
    }

    def genParams(dd: DefDef, isStatic: Boolean): Seq[Val.Local] =
      genParamSyms(dd, isStatic).map {
        case None =>
          val fresh = curFresh.get
          Val.Local(fresh(), genType(curClassSym.tpe, box = true))
        case Some(sym) =>
          val fresh = curFresh.get
          val name  = fresh()
          val ty    = genType(sym.tpe, box = false)
          val param = Val.Local(name, ty)
          curMethodEnv.enter(sym, param)
          param
      }

    def genNormalMethodBody(dd: DefDef,
                            params: Seq[Val.Local],
                            bodyp: Tree,
                            isStatic: Boolean): Seq[nir.Inst] = {
      val fresh = curFresh.get
      val buf   = new ExprBuffer()(fresh)

      def genPrelude(): Unit = {
        val vars = curMethodInfo.mutableVars.toSeq
        buf.label(fresh(), params)
        vars.foreach { sym =>
          val ty   = genType(sym.info, box = false)
          val slot = buf.var_(ty, unwind(fresh))
          curMethodEnv.enter(sym, slot)
        }
      }

      def genBody(): Val = bodyp match {
        // Tailrec emits magical labeldefs that can hijack this reference is
        // current method. This requires special treatment on our side.
        case Block(List(ValDef(_, nme.THIS, _, _)),
                   label @ LabelDef(name, Ident(nme.THIS) :: _, rhs)) =>
          val local  = curMethodEnv.enterLabel(label)
          val values = params.take(label.params.length)

          buf.jump(local, values)
          scoped(
            curMethodThis := {
              if (isStatic) None
              else Some(Val.Local(params.head.name, params.head.ty))
            }
          ) {
            buf.genTailRecLabel(dd, isStatic, label)
          }

        case _ if curMethodSym.get == NObjectInitMethod =>
          nir.Val.Unit

        case _ =>
          scoped(
            curMethodThis := {
              if (isStatic) None
              else Some(Val.Local(params.head.name, params.head.ty))
            }
          ) {
            buf.genExpr(bodyp)
          }
      }

      genPrelude()
      buf.ret(genBody())
      removeDeadBlocks(buf.toSeq)
    }

    def genFunctionPtrForwarder(sym: Symbol): Val = {
      val anondef = consumeLazyAnonDef(sym)
      val body    = anondef.impl.body
      val apply = body
        .collectFirst {
          case dd: DefDef if dd.symbol.hasFlag(SPECIALIZED) =>
            dd
        }
        .getOrElse {
          body.collectFirst {
            case dd: DefDef
                if dd.name == nme.apply && !dd.symbol.hasFlag(BRIDGE) =>
              dd
          }.get
        }

      apply match {
        case ExternForwarder(tosym) =>
          Val.Global(genMethodName(tosym), Type.Ptr)

        case _ =>
          val attrs  = Attrs(isExtern = true)
          val name   = genAnonName(curClassSym, anondef.symbol)
          val sig    = genMethodSig(apply.symbol, forceStatic = true)
          val params = genParams(apply, isStatic = true)
          val body =
            genNormalMethodBody(apply, params, apply.rhs, isStatic = true)

          buf += Defn.Define(attrs, name, sig, body)

          Val.Global(name, Type.Ptr)
      }
    }
  }

  private def checkExplicitReturnTypeAnnotation(
      externMethodDd: DefDef): Unit = {
    externMethodDd.tpt match {
      case resultTypeTree: global.TypeTree if resultTypeTree.wasEmpty =>
        global.reporter.error(
          externMethodDd.pos,
          "extern method " + externMethodDd.name + " needs result type")
      case other =>
    }
  }
}

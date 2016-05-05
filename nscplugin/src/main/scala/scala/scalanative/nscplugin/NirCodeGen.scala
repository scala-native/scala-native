package scala.scalanative
package nscplugin

import scala.collection.mutable
import scala.tools.nsc.{util => _, _}
import scala.tools.nsc.plugins._
import scala.util.{Either, Left, Right}
import scala.reflect.internal.Flags._
import util._, util.ScopedVar.scoped
import nir.Focus, Focus.sequenced
import nir._, Shows._
import NirPrimitives._

abstract class NirCodeGen
    extends PluginComponent with NirFiles with NirTypeEncoding
    with NirNameEncoding {
  val nirAddons: NirGlobalAddons {
    val global: NirCodeGen.this.global.type
  }

  import global._
  import definitions._
  import treeInfo.hasSynthCaseSymbol
  import nirAddons._
  import nirDefinitions._

  val phaseName = "nir"

  override def newPhase(prev: Phase): StdPhase =
    new SaltyCodePhase(prev)

  case class ValTree(val value: nir.Val) extends Tree

  class Env(val fresh: Fresh) {
    private val env = mutable.Map.empty[Symbol, Val]

    def enter(sym: Symbol, value: Val): Unit =
      env += ((sym, value))

    def enterLabel(ld: LabelDef): Local = {
      val local = fresh()
      enter(ld.symbol, Val.Local(local, Type.Label))
      local
    }

    def resolve(sym: Symbol): Val = env(sym)

    def resolveLabel(ld: LabelDef): Local = {
      val Val.Local(n, Type.Label) = resolve(ld.symbol)
      n
    }
  }

  class CollectLocalInfo extends Traverser {
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

  class SaltyCodePhase(prev: Phase) extends StdPhase(prev) {
    val curLocalInfo = new util.ScopedVar[CollectLocalInfo]
    val curClassSym  = new util.ScopedVar[Symbol]
    val curMethodSym = new util.ScopedVar[Symbol]
    val curEnv       = new util.ScopedVar[Env]
    val curThis      = new util.ScopedVar[Val]

    override def run(): Unit = {
      scalaPrimitives.init()
      nirPrimitives.init()
      super.run()
    }

    override def apply(cunit: CompilationUnit): Unit = {
      def collectClassDefs(tree: Tree): List[ClassDef] = {
        tree match {
          case EmptyTree            => Nil
          case PackageDef(_, stats) => stats flatMap collectClassDefs
          case cd: ClassDef         => cd :: Nil
        }
      }
      val classDefs = collectClassDefs(cunit.body)

      classDefs.foreach { cd =>
        val sym = cd.symbol
        if (isPrimitiveValueClass(sym) || (sym == ArrayClass)) ()
        else {
          //println(cd)
          val defn =
            if (isStruct(cd.symbol)) genStruct(cd)
            else genClass(cd)

          //println(sh"$defn")
          genIRFile(cunit, sym, defn)
        }
      }
    }

    def genStruct(cd: ClassDef): Seq[Defn] = {
      val sym = cd.symbol

      val attrs  = genStructAttrs(sym)
      val name   = genTypeName(sym)
      val fields = genStructFields(sym)

      Seq(Defn.Struct(attrs, name, fields))
    }

    def genStructAttrs(sym: Symbol): Attrs = Attrs.None

    def genStructFields(sym: Symbol): Seq[nir.Type] = {
      for {
        f <- sym.info.decls if isField(f)
      } yield {
        genType(f.tpe)
      }
    }.toSeq

    def genClass(cd: ClassDef): Seq[Defn] =
      scoped(
          curClassSym := cd.symbol
      ) {
        val sym = cd.symbol

        def attrs   = genClassAttrs(sym)
        def name    = genTypeName(sym)
        def parent  = genClassParent(sym)
        def traits  = genClassInterfaces(sym)
        def fields  = genClassFields(sym).toSeq
        def body    = cd.impl.body
        def methods = body.collect { case dd: DefDef => genMethod(dd) }.flatten
        def members = fields ++ methods

        if (isModule(sym)) Defn.Module(attrs, name, parent, traits) +: members
        else if (sym.isInterface) Defn.Trait(attrs, name, traits) +: members
        else Defn.Class(attrs, name, parent, traits) +: members
      }

    def genClassParent(sym: Symbol): Option[nir.Global] =
      if (sym == NObjectClass) None
      else if (sym.superClass == NoSymbol || sym.superClass == ObjectClass)
        Some(genTypeName(NObjectClass))
      else Some(genTypeName(sym.superClass))

    def genClassAttrs(sym: Symbol): Attrs = {
      def pinned =
        if (!isModule(sym) || isExternModule(sym))
          Seq()
        else
          Seq(Attr.PinAlways(genMethodName(sym.asClass.primaryConstructor)))

      Attrs.fromSeq(pinned ++ sym.annotations.collect {
        case ann if ann.symbol == ExternClass => Attr.Extern
        case ann if ann.symbol == PureClass   => Attr.Pure
      })
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol if psym.isInterface
      } yield {
        genTypeName(psym)
      }

    def genClassFields(sym: Symbol) = {
      val attrs = nir.Attrs(isExtern = isExternModule(sym))

      for {
        f <- sym.info.decls if isField(f)
      } yield {
        val ty   = genType(f.tpe)
        val name = genFieldName(f)
        val rhs  = if (isExternModule(sym)) Val.None else Val.Zero(ty)

        Defn.Var(attrs, name, ty, rhs)
      }
    }

    def genMethod(dd: DefDef): Seq[Defn] = {
      //println(s"gen method $dd")
      val fresh  = new Fresh("src")
      val env    = new Env(fresh)

      scoped(
          curMethodSym := dd.symbol,
          curEnv := env,
          curLocalInfo := (new CollectLocalInfo).collect(dd.rhs)
      ) {
        val sym    = dd.symbol
        val attrs  = genMethodAttrs(sym)
        val name   = genMethodName(sym)
        val sig    = genMethodSig(sym)
        val params = genParams(defParamSymbols(dd))

        dd.rhs match {
          case _ if sym.isDeferred =>
            Seq(Defn.Declare(attrs, name, sig))

          case rhs
              if dd.name == nme.CONSTRUCTOR
              && isExternModule(curClassSym) =>
            validateExternCtor(rhs)
            Seq()

          case rhs if isExternModule(curClassSym) =>
            genExternMethod(attrs, name, sig, params, rhs)

          case rhs =>
            Seq(Defn.Define(attrs, name, sig, genNormalMethodBody(params, rhs)))
        }
      }
    }

    def genExternMethod(attrs: nir.Attrs,
                        name: nir.Global,
                        sig: nir.Type,
                        params: Seq[nir.Val.Local],
                        rhs: Tree): Seq[nir.Defn] = {
      val Type.Function(_ +: paramtys, resty) = sig

      val moduleName  = genTypeName(curClassSym)
      val externName  = name tag "extern"
      val externSig   = Type.Function(paramtys, resty)
      val externAttrs = Attrs(isExtern = true)

      rhs match {
        case Apply(ref: RefTree, Seq())
            if ref.symbol == ExternMethod =>
          val externVal  = Val.Global(externName, Type.Ptr)
          val externDefn =
            Defn.Declare(externAttrs, externName, externSig)

          val methodBody = {
            val entry = curEnv.fresh()
            val ret   = Val.Local(curEnv.fresh(), resty)
            val insts = Seq(
              Inst(ret.name, Op.Call(externSig, externVal, params.tail))
            )
            Seq(nir.Block(entry, params, insts, Cf.Ret(ret)))
          }
          val methodDefn = Defn.Define(attrs, name, sig, methodBody)

          Seq(methodDefn, externDefn)

        case rhs =>
          val methodVal  = Val.Global(name, Type.Ptr)
          val methodDefn =
            Defn.Define(attrs, name, sig, genNormalMethodBody(params, rhs))

          val externBody = {
            val entry = curEnv.fresh()
            val ret   = Val.Local(curEnv.fresh(), resty)
            val mod   = Val.Local(curEnv.fresh(), Type.Module(moduleName))
            val insts = Seq(
              Inst(mod.name, Op.Module(moduleName)),
              Inst(ret.name, Op.Call(sig, methodVal, mod +: params.tail))
            )
            Seq(nir.Block(entry, params.tail, insts, Cf.Ret(ret)))
          }
          val externDefn =
            Defn.Define(externAttrs, externName, externSig, externBody)

          if (curMethodSym.hasFlag(ACCESSOR))
            Seq(methodDefn)
          else
            Seq(methodDefn, externDefn)
      }
    }

    def validateExternCtor(rhs: Tree): Unit = {
      val Block(_ +: init, _) = rhs
      val externs = init.map {
        case Assign(ref: RefTree, Apply(extern, Seq()))
            if extern.symbol == ExternMethod =>
          ref.symbol
        case _ =>
          unsupported("extern objects may only contain " +
                      "extern fields and methods")
      }.toSet
      for {
        f <- curClassSym.info.decls
        if isField(f)
        if !externs.contains(f)
      } {
        unsupported("extern objects may only contain extern fields")
      }
    }

    def genMethodAttrs(sym: Symbol): Attrs =
      Attrs.fromSeq(
        sym.overrides.map {
          case sym => Attr.Override(genMethodName(sym))
        } ++ sym.annotations.collect {
          case ann if ann.symbol == InlineClass     => Attr.InlineHint
          case ann if ann.symbol == NoInlineClass   => Attr.NoInline
          case ann if ann.symbol == MustInlineClass => Attr.MustInline
          case ann if ann.symbol == PureClass       => Attr.Pure
        } ++ {
          val owner = sym.owner
          if (owner.primaryConstructor eq sym)
            owner.info.declarations.collect {
              case decl if decl.overrides.nonEmpty =>
                decl.overrides.map {
                  case ov =>
                    Attr.PinIf(genMethodName(decl), genMethodName(ov))
                }
            }.toSeq.flatten
          else Seq()
        }
      )

    def genMethodSig(sym: Symbol): nir.Type = sym match {
      case sym: ModuleSymbol =>
        val MethodType(params, res) = sym.tpe

        val paramtys = params.map(p => genType(p.tpe))
        val selfty   = genType(sym.owner.tpe)
        val retty    = genType(res, retty = true)

        Type.Function(Seq(selfty), retty)

      case sym: MethodSymbol =>
        val params   = sym.paramLists.flatten
        val paramtys = params.map(p => genType(p.tpe))
        val owner    = sym.owner
        val selfty = genType(sym.owner.tpe)
        val retty =
          if (sym.isClassConstructor) Type.Void
          else genType(sym.tpe.resultType, retty = true)

        Type.Function(selfty +: paramtys, retty)
    }

    def defParamSymbols(dd: DefDef): List[Symbol] = {
      val vp = dd.vparamss
      if (vp.isEmpty) Nil else vp.head.map(_.symbol)
    }

    def genParams(paramSyms: Seq[Symbol]): Seq[Val.Local] = {
      val self = Val.Local(curEnv.fresh(), genType(curClassSym.tpe))
      val params = paramSyms.map { sym =>
        val name  = curEnv.fresh()
        val ty    = genType(sym.tpe)
        val param = Val.Local(name, ty)
        curEnv.enter(sym, param)
        param
      }

      self +: params
    }

    def notMergeableGuard(f: => Focus): Focus =
      try f catch {
        case Focus.NotMergeable(focus) => focus
      }

    def genNormalMethodBody(params: Seq[Val.Local], bodyp: Tree): Seq[nir.Block] =
      bodyp match {
        // Tailrec emits magical labeldefs that can hijack this reference is
        // current method. This requires special treatment on our side.
        case Block(List(ValDef(_, nme.THIS, _, _)),
                   label @ LabelDef(name, Ident(nme.THIS) :: _, rhs)) =>
          val local  = curEnv.enterLabel(label)
          val values = params.take(label.params.length)
          val entry  = Focus.entry(params)(curEnv.fresh)
          val body   = notMergeableGuard(genLabel(label, hijackThis = true))

          (entry finish Cf.Jump(Next.Label(local, values))).blocks ++
          (body finish Cf.Ret(body.value)).blocks

        case _ if curMethodSym.get == NObjectInitMethod =>
          Seq(nir.Block(curEnv.fresh(), params, Seq(),
                        nir.Cf.Ret(nir.Val.Unit)))

        case _ =>
          val body = scoped(
              curThis := Val.Local(params.head.name, params.head.ty)
          ) {
            notMergeableGuard(
                genExpr(bodyp, Focus.entry(params)(curEnv.fresh)))
          }

          (body finish Cf.Ret(body.value)).blocks
      }

    def genExpr(tree: Tree, focus: Focus): Focus = tree match {
      case ValTree(value) =>
        focus withValue value

      case label: LabelDef =>
        assert(label.params.length == 0)
        curEnv.enterLabel(label)
        genLabel(label)

      case vd: ValDef =>
        // TODO: attribute valdef name to the rhs node
        val rhs       = genExpr(vd.rhs, focus)
        val isMutable = curLocalInfo.mutableVars.contains(vd.symbol)
        if (!isMutable) {
          curEnv.enter(vd.symbol, rhs.value)
          rhs withValue Val.None
        } else {
          val ty     = genType(vd.symbol.tpe)
          val alloca = rhs.withOp(Op.Alloca(ty))
          curEnv.enter(vd.symbol, alloca.value)
          alloca withOp Op.Store(ty, alloca.value, rhs.value)
        }

      case If(cond, thenp, elsep) =>
        genIf(cond, thenp, elsep, genType(tree.tpe), focus)

      case Return(exprp) =>
        val expr = genExpr(exprp, focus)
        expr.finish(Cf.Ret(expr.value))

      case Try(expr, catches, finalizer)
          if catches.isEmpty && finalizer.isEmpty =>
        genExpr(expr, focus)

      case Try(expr, catches, finalizer) =>
        genTry(genType(tree.tpe), expr, catches, finalizer, focus)

      case Throw(exprp) =>
        val expr = genExpr(exprp, focus)
        expr finish Cf.Throw(expr.value)

      case app: Apply =>
        genApply(app, focus)

      case app: ApplyDynamic =>
        genApplyDynamic(app, focus)

      case This(qual) =>
        focus.withValue {
          if (tree.symbol == curClassSym.get) curThis.get
          else Val.Global(genTypeName(tree.symbol), genType(tree.tpe))
        }

      case Select(qualp, selp) =>
        val sym = tree.symbol
        if (isModule(sym)) focus withOp Op.Module(genTypeName(sym))
        else if (sym.isStaticMember) genStaticMember(sym, focus)
        else if (sym.isMethod)
          genMethodCall(sym, statically = false, qualp, Seq(), focus)
        else {
          val ty   = genType(tree.symbol.tpe)
          val qual = genExpr(qualp, focus)
          val name = genFieldName(tree.symbol)
          val elem =
            if (isExternModule(sym.owner))
              qual withValue Val.Global(name, Type.Ptr)
            else
              qual withOp Op.Field(ty, qual.value, name)
          elem withOp Op.Load(ty, elem.value)
        }

      case id: Ident =>
        val sym = id.symbol
        if (curLocalInfo.mutableVars.contains(sym))
          focus withOp Op.Load(genType(sym.tpe), curEnv.resolve(sym))
        else if (isModule(sym)) focus withOp Op.Module(genTypeName(sym))
        else focus withValue (curEnv.resolve(sym))

      case lit: Literal =>
        genLiteral(lit, focus)

      case block: Block =>
        genBlock(block, focus)

      case Typed(Super(_, _), _) =>
        focus.withValue(curThis)

      case Typed(expr, _) =>
        genExpr(expr, focus)

      case Assign(lhsp, rhsp) =>
        lhsp match {
          case sel @ Select(qualp, _) =>
            val ty   = genType(sel.tpe)
            val qual = genExpr(qualp, focus)
            val rhs  = genExpr(rhsp, qual)
            val name = genFieldName(sel.symbol)
            val elem =
              if (isExternModule(sel.symbol.owner))
                rhs withValue Val.Global(name, Type.Ptr)
              else
                rhs withOp Op.Field(ty, qual.value, name)
            elem withOp Op.Store(ty, elem.value, rhs.value)

          case id: Ident =>
            val ty  = genType(id.tpe)
            val rhs = genExpr(rhsp, focus)
            rhs withOp Op.Store(ty, curEnv.resolve(id.symbol), rhs.value)
        }

      case av: ArrayValue =>
        genArrayValue(av, focus)

      case m: Match =>
        genSwitch(m, focus)

      case fun: Function =>
        unsupported(fun)

      case EmptyTree =>
        focus

      case _ =>
        abort("Unexpected tree in genExpr: " + tree + "/" + tree.getClass +
            " at: " + tree.pos)
    }

    def genLiteral(lit: Literal, focus: Focus): Focus = {
      val value = lit.value
      value.tag match {
        case UnitTag | NullTag | BooleanTag | ByteTag | ShortTag | CharTag |
            IntTag | LongTag | FloatTag | DoubleTag | StringTag =>
          focus withValue genLiteralValue(lit)

        case ClazzTag =>
          val typeof = focus withOp Op.Typeof(genType(value.typeValue))
          genBoxClass(typeof.value, typeof)

        case EnumTag =>
          genStaticMember(value.symbolValue, focus)
      }
    }

    def genLiteralValue(lit: Literal): Val = {
      val value = lit.value
      value.tag match {
        case UnitTag =>
          Val.Unit
        case NullTag =>
          Val.Zero(Rt.Object)
        case BooleanTag =>
          if (value.booleanValue) Val.True else Val.False
        case ByteTag =>
          Val.I8(value.intValue.toByte)
        case ShortTag | CharTag =>
          Val.I16(value.intValue.toShort)
        case IntTag =>
          Val.I32(value.intValue)
        case LongTag =>
          Val.I64(value.longValue)
        case FloatTag =>
          Val.F32(value.floatValue)
        case DoubleTag =>
          Val.F64(value.doubleValue)
        case StringTag =>
          Val.String(value.stringValue)
      }
    }

    def genModule(sym: Symbol, focus: Focus): Focus =
      focus withOp Op.Module(genTypeName(sym))

    def genStaticMember(sym: Symbol, focus: Focus): Focus = {
      val ty     = genType(sym.tpe)
      val module = focus withOp Op.Module(genTypeName(sym.owner))
      val elem   = module withOp Op.Field(ty, module.value, genFieldName(sym))

      elem withOp Op.Load(ty, elem.value)
    }

    def genTry(retty: nir.Type,
               expr: Tree,
               catches: List[Tree],
               finalizer: Tree,
               focus: Focus): Focus =
      focus.branchTry(retty,
                      normal = genExpr(expr, _),
                      exc = genCatch(retty, catches, _, _))

    def genCatch(retty: nir.Type,
                 catches: List[Tree],
                 excrec: Val,
                 focus: Focus): Focus = {
      val excwrap = focus withOp Op.Extract(excrec, Seq(0))
      val exccast =
        excwrap withOp Op.Conv(Conv.Bitcast, Type.Ptr, excwrap.value)
      val exc = exccast withOp Op.Load(Rt.Object, exccast.value)

      val cases = catches.map {
        case CaseDef(pat, _, body) =>
          val (symopt, excty) = pat match {
            case Typed(Ident(nme.WILDCARD), tpt) =>
              (None, genType(tpt.tpe))
            case Ident(nme.WILDCARD) =>
              (None, genType(ThrowableClass.tpe))
            case Bind(_, _) =>
              (Some(pat.symbol), genType(pat.symbol.tpe))
          }
          val f = { focus: Focus =>
            val enter = symopt.map { sym =>
              val cast = focus withOp Op.As(excty, exc.value)
              curEnv.enter(sym, cast.value)
              cast
            }.getOrElse(focus)
            val begin =
              enter withOp Op.Call(Rt.beginCatchSig,
                                   Rt.beginCatch,
                                   Seq(excwrap.value))
            val res = genExpr(body, begin)
            val end = res withOp Op.Call(Rt.endCatchSig, Rt.endCatch, Seq())
            end withValue res.value
          }

          (excty, f)
      }

      def wrap(cases: Seq[(nir.Type, Focus => Focus)], focus: Focus): Focus =
        cases match {
          case Seq() =>
            focus finish Cf.Resume(excrec)
          case (excty, f) +: rest =>
            val cond = focus withOp Op.Is(excty, exc.value)
            cond.branchIf(cond.value, retty, f, wrap(rest, _))
        }

      wrap(cases, exc)
    }

    def genFinally(finalizer: Tree, focus: Focus): Focus = ???

    def genBlock(block: Block, focus: Focus) = {
      val Block(stats, last) = block

      def isCaseLabelDef(tree: Tree) =
        tree.isInstanceOf[LabelDef] && hasSynthCaseSymbol(tree)

      def translateMatch(last: LabelDef) = {
        val (prologue, cases) = stats.span(s => !isCaseLabelDef(s))
        val labels            = cases.map { case label: LabelDef => label }
        genMatch(prologue, labels :+ last, focus)
      }

      last match {
        case label: LabelDef if isCaseLabelDef(label) =>
          translateMatch(label)

        case Apply(
            TypeApply(Select(label: LabelDef, nme.asInstanceOf_Ob), _), _)
            if isCaseLabelDef(label) =>
          translateMatch(label)

        case _ =>
          val focs      = sequenced(stats, focus)(genExpr(_, _))
          val lastfocus = focs.lastOption.getOrElse(focus)
          genExpr(last, lastfocus)
      }
    }

    def genMatch(prologue: List[Tree], lds: List[LabelDef], focus: Focus) = {
      val prfocus   = sequenced(prologue, focus)(genExpr(_, _))
      val lastfocus = prfocus.lastOption.getOrElse(focus)
      lds.foreach(curEnv.enterLabel)

      val first = Next.Label(curEnv.resolveLabel(lds.head), Seq())
      var resfocus = lastfocus finish Cf.Jump(first)
      for (ld <- lds.init) {
        val lfocus = genLabel(ld)
        assert(lfocus.isComplete)
        resfocus = resfocus appendBlocks lfocus.blocks
      }

      genLabel(lds.last) prependBlocks resfocus.blocks
    }

    def genLabel(label: LabelDef, hijackThis: Boolean = false) = {
      val local = curEnv.resolveLabel(label)
      val params = label.params.map { id =>
        val local = Val.Local(curEnv.fresh(), genType(id.tpe))
        curEnv.enter(id.symbol, local)
        local
      }
      val entry        = Focus.entry(local, params)(curEnv.fresh)
      val newThis: Val = if (hijackThis) params.head else curThis

      scoped(
          curThis := newThis
      ) {
        genExpr(label.rhs, entry)
      }
    }

    def genArrayValue(av: ArrayValue, focus: Focus): Focus = {
      val ArrayValue(tpt, elems) = av

      val len       = Literal(Constant(elems.length))
      val elemcode  = genPrimCode(tpt.tpe)
      val modulesym = NArrayModule(elemcode)
      val methsym   = NArrayAllocMethod(elemcode)
      val alloc     = genModuleMethodCall(modulesym, methsym, Seq(len), focus)
      val init =
        if (elems.isEmpty) alloc
        else {
          val (values, last) = genArgs(elems, alloc)
          val updates = sequenced(values.zipWithIndex, last) { (vi, focus) =>
            val (v, i) = vi
            val idx    = Literal(Constant(i))

            genMethodCall(NArrayUpdateMethod(elemcode),
                          statically = true,
                          alloc.value,
                          Seq(idx),
                          focus)
          }

          updates.last
        }

      init withValue alloc.value
    }

    def genIf(condp: Tree,
              thenp: Tree,
              elsep: Tree,
              retty: nir.Type,
              focus: Focus) = {
      val cond = genExpr(condp, focus)
      cond.branchIf(cond.value, retty, genExpr(thenp, _), genExpr(elsep, _))
    }

    def genSwitch(m: Match, focus: Focus): Focus = {
      val Match(scrutp, casesp) = m
      val scrut                 = genExpr(scrutp, focus)
      val retty                 = genType(m.tpe)
      val defaultcase: Tree = casesp.collectFirst {
        case c @ CaseDef(Ident(nme.WILDCARD), _, body) => body
      }.get
      val normalcases: Seq[(Val, Tree)] = casesp.flatMap {
        case CaseDef(Ident(nme.WILDCARD), _, _) =>
          Seq()
        case CaseDef(pat, guard, body) =>
          assert(guard.isEmpty)
          val vals: Seq[Val] = pat match {
            case lit: Literal =>
              List(genLiteralValue(lit))
            case Alternative(alts) =>
              alts.map {
                case lit: Literal => genLiteralValue(lit)
              }
            case _ =>
              Nil
          }
          vals.map((_, body))
      }

      scrut.branchSwitch(scrut.value,
                         retty,
                         defaultf = genExpr(defaultcase, _),
                         casevals = normalcases.map { case (v, _) => v },
                         casefs = normalcases.map {
                           case (_, body) => genExpr(body, _: Focus)
                         })
    }

    def genApplyDynamic(app: ApplyDynamic, focus: Focus) =
      unsupported(app)

    def genApply(app: Apply, focus: Focus): Focus = {
      val Apply(fun, args) = app

      fun match {
        case _: TypeApply =>
          genApplyTypeApply(app, focus)
        case Select(Super(_, _), _) =>
          genMethodCall(
              fun.symbol, statically = true, curThis.get, args, focus)
        case Select(New(_), nme.CONSTRUCTOR) =>
          genApplyNew(app, focus)
        case _ =>
          val sym = fun.symbol

          if (sym.isLabel) {
            genApplyLabel(app, focus)
          } else if (scalaPrimitives.isPrimitive(sym)) {
            genPrimitiveOp(app, focus)
          } else if (currentRun.runDefinitions.isBox(sym)) {
            val arg = args.head
            genPrimitiveBox(arg, arg.tpe, focus)
          } else if (currentRun.runDefinitions.isUnbox(sym)) {
            genPrimitiveUnbox(args.head, app.tpe, focus)
          } else {
            val Select(receiverp, _) = fun
            genMethodCall(
                fun.symbol, statically = false, receiverp, args, focus)
          }
      }
    }

    def genApplyLabel(tree: Tree, focus: Focus) = notMergeableGuard {
      val Apply(fun, args)    = tree
      val Val.Local(label, _) = curEnv.resolve(fun.symbol)
      val argsfocus           = sequenced(args, focus)(genExpr(_, _))
      val lastfocus           = argsfocus.lastOption.getOrElse(focus)

      lastfocus finish Cf.Jump(Next.Label(label, argsfocus.map(_.value)))
    }

    def genPrimitiveBox(argp: Tree, tpe: Type, focus: Focus) =
      genModuleMethodCall(
          BoxesRunTimeModule, BoxMethod(genPrimCode(tpe)), Seq(argp), focus)

    def genPrimitiveUnbox(argp: Tree, tpe: Type, focus: Focus) =
      genModuleMethodCall(
          BoxesRunTimeModule, UnboxMethod(genPrimCode(tpe)), Seq(argp), focus)

    def genPrimitiveOp(app: Apply, focus: Focus): Focus = {
      import scalaPrimitives._

      val Apply(fun @ Select(receiver, _), args) = app

      val sym  = app.symbol
      val code = scalaPrimitives.getPrimitive(sym, receiver.tpe)

      if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code))
        genSimpleOp(app, receiver :: args, code, focus)
      else if (code == CONCAT) genStringConcat(receiver, args, focus)
      else if (code == HASH) genHashCode(app, receiver, focus)
      else if (isArrayOp(code) || code == ARRAY_CLONE)
        genArrayOp(app, code, focus)
      else if (nirPrimitives.isPtrOp(code))
        genPtrOp(app, code, focus)
      else if (isCoercion(code)) genCoercion(app, receiver, code, focus)
      else if (code == SYNCHRONIZED) genSynchronized(app, focus)
      else if (code == CAST)
        genCastOp(app, focus)
      else
        abort("Unknown primitive operation: " + sym.fullName + "(" +
            fun.symbol.simpleName + ") " + " at: " + (app.pos))
    }

    lazy val jlClassName     = nir.Global.Type("java.lang.Class")
    lazy val jlClass         = nir.Type.Class(jlClassName)
    lazy val jlClassCtorName = jlClassName member "init_ptr"
    lazy val jlClassCtorSig =
      nir.Type.Function(Seq(jlClass, Type.Ptr), nir.Type.Void)
    lazy val jlClassCtor = nir.Val.Global(jlClassCtorName, nir.Type.Ptr)

    def genBoxClass(type_ : Val, focus: Focus) = {
      val alloc = focus withOp Op.Alloc(jlClass)
      val init =
        alloc withOp Op.Call(
            jlClassCtorSig, jlClassCtor, Seq(alloc.value, type_))

      init withValue alloc.value
    }

    def numOfType(num: Int, ty: nir.Type) = ty match {
      case Type.I8  => Val.I8(num.toByte)
      case Type.I16 => Val.I16(num.toShort)
      case Type.I32 => Val.I32(num)
      case Type.I64 => Val.I64(num.toLong)
      case Type.F32 => Val.F32(num.toFloat)
      case Type.F64 => Val.F64(num.toDouble)
      case _        => unreachable
    }

    def genSimpleOp(app: Apply, args: List[Tree], code: Int, focus: Focus): Focus = {
      val retty = genType(app.tpe)

      args match {
        case List(right)       => genUnaryOp(code, right, retty, focus)
        case List(left, right) => genBinaryOp(code, left, right, retty, focus)
        case _                 => abort("Too many arguments for primitive function: " + app)
      }
    }

    def negateInt(value: nir.Val, focus: Focus): Focus =
      focus withOp Op.Bin(Bin.Isub, value.ty, numOfType(0, value.ty), value)
    def negateFloat(value: nir.Val, focus: Focus): Focus =
      focus withOp Op.Bin(Bin.Fsub, value.ty, numOfType(0, value.ty), value)
    def negateBits(value: nir.Val, focus: Focus): Focus =
      focus withOp Op.Bin(Bin.Xor, value.ty, numOfType(-1, value.ty), value)
    def negateBool(value: nir.Val, focus: Focus): Focus =
      focus withOp Op.Bin(Bin.Xor, Type.Bool, Val.True, value)

    def genUnaryOp(code: Int, rightp: Tree, opty: nir.Type, focus: Focus) = {
      import scalaPrimitives._

      val right = genExpr(rightp, focus)

      (opty, code) match {
        case (Type.I(_) | Type.F(_), POS) => right
        case (Type.F(_), NEG)  => negateFloat(right.value, right)
        case (Type.I(_), NEG)  => negateInt(right.value, right)
        case (Type.I(_), NOT)  => negateBits(right.value, right)
        case (Type.I(_), ZNOT) => negateBool(right.value, right)
        case _ => abort("Unknown unary operation code: " + code)
      }
    }

    def genBinaryOp(code: Int,
                    left: Tree,
                    right: Tree,
                    retty: nir.Type,
                    focus: Focus): Focus = {
      import scalaPrimitives._

      val lty  = genType(left.tpe)
      val rty  = genType(right.tpe)
      val opty = binaryOperationType(lty, rty)

      opty match {
        case Type.F(_) =>
          code match {
            case ADD =>
              genBinaryOp(Op.Bin(Bin.Fadd, _, _, _), left, right, opty, focus)
            case SUB =>
              genBinaryOp(Op.Bin(Bin.Fsub, _, _, _), left, right, opty, focus)
            case MUL =>
              genBinaryOp(Op.Bin(Bin.Fmul, _, _, _), left, right, opty, focus)
            case DIV =>
              genBinaryOp(Op.Bin(Bin.Fdiv, _, _, _), left, right, opty, focus)
            case MOD =>
              genBinaryOp(Op.Bin(Bin.Frem, _, _, _), left, right, opty, focus)

            case EQ =>
              genBinaryOp(Op.Comp(Comp.Feq, _, _, _), left, right, opty, focus)
            case NE =>
              genBinaryOp(Op.Comp(Comp.Fne, _, _, _), left, right, opty, focus)
            case LT =>
              genBinaryOp(Op.Comp(Comp.Flt, _, _, _), left, right, opty, focus)
            case LE =>
              genBinaryOp(Op.Comp(Comp.Fle, _, _, _), left, right, opty, focus)
            case GT =>
              genBinaryOp(Op.Comp(Comp.Fgt, _, _, _), left, right, opty, focus)
            case GE =>
              genBinaryOp(Op.Comp(Comp.Fge, _, _, _), left, right, opty, focus)

            case _ =>
              abort(
                  "Unknown floating point type binary operation code: " + code)
          }

        case Type.I(_) =>
          code match {
            case ADD =>
              genBinaryOp(Op.Bin(Bin.Iadd, _, _, _), left, right, opty, focus)
            case SUB =>
              genBinaryOp(Op.Bin(Bin.Isub, _, _, _), left, right, opty, focus)
            case MUL =>
              genBinaryOp(Op.Bin(Bin.Imul, _, _, _), left, right, opty, focus)
            case DIV =>
              genBinaryOp(Op.Bin(Bin.Sdiv, _, _, _), left, right, opty, focus)
            case MOD =>
              genBinaryOp(Op.Bin(Bin.Srem, _, _, _), left, right, opty, focus)

            case OR =>
              genBinaryOp(Op.Bin(Bin.Or, _, _, _), left, right, opty, focus)
            case XOR =>
              genBinaryOp(Op.Bin(Bin.Xor, _, _, _), left, right, opty, focus)
            case AND =>
              genBinaryOp(Op.Bin(Bin.And, _, _, _), left, right, opty, focus)
            case LSL =>
              genBinaryOp(Op.Bin(Bin.Shl, _, _, _), left, right, opty, focus)
            case LSR =>
              genBinaryOp(Op.Bin(Bin.Lshr, _, _, _), left, right, opty, focus)
            case ASR =>
              genBinaryOp(Op.Bin(Bin.Ashr, _, _, _), left, right, opty, focus)

            case EQ =>
              genBinaryOp(Op.Comp(Comp.Ieq, _, _, _), left, right, opty, focus)
            case NE =>
              genBinaryOp(Op.Comp(Comp.Ine, _, _, _), left, right, opty, focus)
            case LT =>
              genBinaryOp(Op.Comp(Comp.Slt, _, _, _), left, right, opty, focus)
            case LE =>
              genBinaryOp(Op.Comp(Comp.Sle, _, _, _), left, right, opty, focus)
            case GT =>
              genBinaryOp(Op.Comp(Comp.Sgt, _, _, _), left, right, opty, focus)
            case GE =>
              genBinaryOp(Op.Comp(Comp.Sge, _, _, _), left, right, opty, focus)

            case ZOR =>
              genIf(left, Literal(Constant(true)), right, retty, focus)
            case ZAND =>
              genIf(left, right, Literal(Constant(false)), retty, focus)

            case _ =>
              abort("Unknown integer type binary operation code: " + code)
          }

        case _: Type.RefKind =>
          code match {
            case EQ =>
              genClassEquality(
                  left, right, ref = false, negated = false, focus)
            case NE =>
              genClassEquality(left, right, ref = false, negated = true, focus)
            case ID =>
              genClassEquality(left, right, ref = true, negated = false, focus)
            case NI =>
              genClassEquality(left, right, ref = true, negated = true, focus)

            case _ => abort("Unknown reference type operation code: " + code)
          }

        case ty =>
          abort("Uknown binary operation type: " + ty)
      }
    }

    def genBinaryOp(op: (nir.Type, Val, Val) => Op,
                    leftp: Tree,
                    rightp: Tree,
                    opty: nir.Type,
                    focus: Focus): Focus = {
      val left        = genExpr(leftp, focus)
      val leftcoerced = genCoercion(left.value, genType(leftp.tpe), opty, left)
      val right       = genExpr(rightp, leftcoerced)
      val rightcoerced = genCoercion(
          right.value, genType(rightp.tpe), opty, right)

      rightcoerced withOp op(opty, leftcoerced.value, rightcoerced.value)
    }

    def genClassEquality(leftp: Tree,
                         rightp: Tree,
                         ref: Boolean,
                         negated: Boolean,
                         focus: Focus) = {
      val left = genExpr(leftp, focus)

      if (ref) {
        val right = genExpr(rightp, left)
        val comp  = if (negated) Comp.Ieq else Comp.Ine
        right withOp Op.Comp(comp, Rt.Object, left.value, right.value)
      } else {
        val equals = genMethodCall(NObjectEqualsMethod,
                                   statically = true,
                                   left.value,
                                   Seq(rightp),
                                   left)
        if (negated) negateBool(equals.value, equals)
        else equals
      }
    }

    def binaryOperationType(lty: nir.Type, rty: nir.Type) = (lty, rty) match {
      case (nir.Type.I(lwidth), nir.Type.I(rwidth)) =>
        if (lwidth >= rwidth) lty else rty
      case (nir.Type.I(_), nir.Type.F(_)) =>
        rty
      case (nir.Type.F(_), nir.Type.I(_)) =>
        lty
      case (nir.Type.F(lwidth), nir.Type.F(rwidth)) =>
        if (lwidth >= rwidth) lty else rty
      case (_: nir.Type.RefKind, _: nir.Type.RefKind) =>
        Rt.Object
      case (ty1, ty2) if ty1 == ty2 =>
        ty1
      case _ =>
        abort(s"can't perform binary opeation between $lty and $rty")
    }

    def genStringConcat(selfp: Tree, argsp: List[Tree], focus: Focus): Focus =
      genMethodCall(String_+, statically = true, selfp, argsp, focus)

    // TODO: this doesn't seem to get called on foo.## expressions
    def genHashCode(tree: Tree, receiverp: Tree, focus: Focus) = {
      val recv = genExpr(receiverp, focus)

      genMethodCall(
          NObjectHashCodeMethod, statically = true, recv.value, Seq(), recv)
    }

    def genArrayOp(app: Apply, code: Int, focus: Focus): Focus = {
      import scalaPrimitives._

      val Apply(Select(arrayp, _), argsp) = app

      val array = genExpr(arrayp, focus)
      def elemcode = genArrayCode(arrayp.tpe)
      val method =
        if (code == ARRAY_CLONE) NArrayCloneMethod(elemcode)
        else if (scalaPrimitives.isArrayGet(code)) NArrayApplyMethod(elemcode)
        else if (scalaPrimitives.isArraySet(code)) NArrayUpdateMethod(elemcode)
        else NArrayLengthMethod(elemcode)

      genMethodCall(method, statically = true, array.value, argsp, array)
    }

    def extractClassFromImplicitClassTag(tree: Tree): Symbol = {
      tree match {
        case Typed(Apply(ref: RefTree, args), _) =>
          ref.symbol match {
            case ByteClassTag    => ByteClass
            case ShortClassTag   => ShortClass
            case CharClassTag    => CharClass
            case IntClassTag     => IntClass
            case LongClassTag    => LongClass
            case FloatClassTag   => FloatClass
            case DoubleClassTag  => DoubleClass
            case BooleanClassTag => BooleanClass
            case UnitClassTag    => UnitClass
            case AnyClassTag     => AnyClass
            case ObjectClassTag  => ObjectClass
            case AnyValClassTag  => ObjectClass
            case AnyRefClassTag  => ObjectClass
            case NothingClassTag => NothingClass
            case NullClassTag    => NullClass
            case ClassTagApply   =>
              val Seq(Literal(const: Constant)) = args
              const.typeValue.typeSymbol
            case _ =>
              unsupported(tree)
          }

        case tree =>
          unsupported(tree)
      }
    }

    def boxValue(sym: Symbol, focus: Focus): Focus =
      if (genPrimCode(sym) == 'O') focus
      else genPrimitiveBox(ValTree(focus.value), sym.info, focus)

    def unboxValue(sym: Symbol, focus: Focus): Focus =
      if (genPrimCode(sym) == 'O') focus
      else genPrimitiveUnbox(ValTree(focus.value), sym.info, focus)

    def genPtrOp(app: Apply, code: Int, focus: Focus): Focus = {
      val Apply(Select(ptrp, _), argsp) = app

      (code, argsp) match {
        case (PTR_LOAD, Seq(ctp)) =>
          val sym = extractClassFromImplicitClassTag(ctp)
          val ty  = genTypeSym(sym)
          val ptr = genExpr(ptrp, focus)
          boxValue(sym, ptr withOp Op.Load(ty, ptr.value))

        case (PTR_STORE, Seq(valuep, ctp)) =>
          val sym   = extractClassFromImplicitClassTag(ctp)
          val ty    = genTypeSym(sym)
          val ptr   = genExpr(ptrp, focus)
          val value = unboxValue(sym, genExpr(valuep, ptr))
          value withOp Op.Store(ty, ptr.value, value.value)

        case (PTR_ADD, Seq(valuep, ctp)) =>
          val sym   = extractClassFromImplicitClassTag(ctp)
          val ty    = genTypeSym(sym)
          val ptr   = genExpr(ptrp, focus)
          val value = genExpr(valuep, ptr)
          value withOp Op.Elem(ty, ptr.value, Seq(value.value))

        case (PTR_SUB, Seq(valuep, ctp)) =>
          val sym   = extractClassFromImplicitClassTag(ctp)
          val ty    = genTypeSym(sym)
          val ptr   = genExpr(ptrp, focus)
          val value = genExpr(valuep, ptr)
          val neg   = negateInt(value.value, value)
          neg withOp Op.Elem(ty, ptr.value, Seq(neg.value))
      }
    }

    def castConv(fromty: nir.Type, toty: nir.Type): Option[nir.Conv] =
      (fromty, toty) match {
        case (Type.I(_), Type.Ptr)        => Some(nir.Conv.Inttoptr)
        case (Type.Ptr, Type.I(_))        => Some(nir.Conv.Ptrtoint)
        case (_: Type.RefKind, Type.Ptr)  => Some(nir.Conv.Bitcast)
        case (_: Type.RefKind, Type.I(_)) => Some(nir.Conv.Ptrtoint)
        case (Type.I(_), _: Type.RefKind) => Some(nir.Conv.Inttoptr)
        case _ if fromty == toty          => None
        case _                            => unsupported(s"cast from $fromty to $toty")
      }

    def genCastOp(app: Apply, focus: Focus): Focus = {
      val Apply(_, Seq(valuep, ctp)) = app

      val sym    = extractClassFromImplicitClassTag(ctp)
      val fromty = genType(valuep.tpe)
      val toty   = genTypeSym(sym)
      val value  = genExpr(valuep, focus)

      boxValue(sym, castConv(fromty, toty).fold(value) { conv =>
        value withOp Op.Conv(conv, toty, value.value)
      })
    }

    def genSynchronized(app: Apply, focus: Focus): Focus = {
      val Apply(Select(receiverp, _), List(argp)) = app

      val monitor = genModuleMethodCall(NMonitorModule,
                                        NMonitorGetMethod,
                                        Seq(receiverp),
                                        focus)
      val enter = genMethodCall(NMonitorEnterMethod,
                                statically = true,
                                monitor.value,
                                Seq(),
                                monitor)
      val arg = genExpr(argp, enter)
      val exit = genMethodCall(
          NMonitorExitMethod, statically = true, monitor.value, Seq(), arg)

      exit withValue arg.value
    }

    def genCoercion(
        app: Apply, receiver: Tree, code: Int, focus: Focus): Focus = {
      val rec            = genExpr(receiver, focus)
      val (fromty, toty) = coercionTypes(code)

      genCoercion(rec.value, fromty, toty, rec)
    }

    def genCoercion(
        value: Val, fromty: nir.Type, toty: nir.Type, focus: Focus): Focus =
      if (fromty == toty) focus withValue value
      else {
        val conv = (fromty, toty) match {
          case (nir.Type.I(lwidth), nir.Type.I(rwidth)) if lwidth < rwidth =>
            Conv.Sext
          case (nir.Type.I(lwidth), nir.Type.I(rwidth)) if lwidth > rwidth =>
            Conv.Trunc
          case (nir.Type.I(_), nir.Type.F(_)) =>
            Conv.Sitofp
          case (nir.Type.F(_), nir.Type.I(_)) =>
            Conv.Fptosi
          case (nir.Type.F64, nir.Type.F32) =>
            Conv.Fptrunc
          case (nir.Type.F32, nir.Type.F64) =>
            Conv.Fpext
        }
        focus withOp Op.Conv(conv, toty, value)
      }

    def coercionTypes(code: Int) = {
      import scalaPrimitives._

      code match {
        case B2B       => (nir.Type.I8, nir.Type.I8)
        case B2S | B2C => (nir.Type.I8, nir.Type.I16)
        case B2I       => (nir.Type.I8, nir.Type.I32)
        case B2L       => (nir.Type.I8, nir.Type.I64)
        case B2F       => (nir.Type.I8, nir.Type.F32)
        case B2D       => (nir.Type.I8, nir.Type.F64)

        case S2B | C2B             => (nir.Type.I16, nir.Type.I8)
        case S2S | S2C | C2S | C2C => (nir.Type.I16, nir.Type.I16)
        case S2I | C2I             => (nir.Type.I16, nir.Type.I32)
        case S2L | C2L             => (nir.Type.I16, nir.Type.I64)
        case S2F | C2F             => (nir.Type.I16, nir.Type.F32)
        case S2D | C2D             => (nir.Type.I16, nir.Type.F64)

        case I2B       => (nir.Type.I32, nir.Type.I8)
        case I2S | I2C => (nir.Type.I32, nir.Type.I16)
        case I2I       => (nir.Type.I32, nir.Type.I32)
        case I2L       => (nir.Type.I32, nir.Type.I64)
        case I2F       => (nir.Type.I32, nir.Type.F32)
        case I2D       => (nir.Type.I32, nir.Type.F64)

        case L2B       => (nir.Type.I64, nir.Type.I8)
        case L2S | L2C => (nir.Type.I64, nir.Type.I16)
        case L2I       => (nir.Type.I64, nir.Type.I32)
        case L2L       => (nir.Type.I64, nir.Type.I64)
        case L2F       => (nir.Type.I64, nir.Type.F32)
        case L2D       => (nir.Type.I64, nir.Type.F64)

        case F2B       => (nir.Type.F32, nir.Type.I8)
        case F2S | F2C => (nir.Type.F32, nir.Type.I16)
        case F2I       => (nir.Type.F32, nir.Type.I32)
        case F2L       => (nir.Type.F32, nir.Type.I64)
        case F2F       => (nir.Type.F32, nir.Type.F32)
        case F2D       => (nir.Type.F32, nir.Type.F64)

        case D2B       => (nir.Type.F64, nir.Type.I8)
        case D2S | D2C => (nir.Type.F64, nir.Type.I16)
        case D2I       => (nir.Type.F64, nir.Type.I32)
        case D2L       => (nir.Type.F64, nir.Type.I64)
        case D2F       => (nir.Type.F64, nir.Type.F32)
        case D2D       => (nir.Type.F64, nir.Type.F64)
      }
    }

    def genApplyTypeApply(app: Apply, focus: Focus) = {
      val Apply(TypeApply(fun @ Select(receiverp, _), targs), _) = app
      val ty                                                     = genType(targs.head.tpe)
      val rec                                                    = genExpr(receiverp, focus)
      rec.withOp(
          fun.symbol match {
        case Object_isInstanceOf => Op.Is(ty, rec.value)
        case Object_asInstanceOf => Op.As(ty, rec.value)
      })
    }

    def genApplyNew(app: Apply, focus: Focus) = {
      val Apply(fun @ Select(New(tpt), nme.CONSTRUCTOR), args) = app

      decomposeType(tpt.tpe) match {
        case (ArrayClass, Seq(targ)) =>
          genNewArray(genPrimCode(targ), args, focus)

        case (cls, Seq()) =>
          genNew(cls, fun.symbol, args, focus)

        case (sym, targs) =>
          unsupported(s"unexpected new: $sym with targs $targs")
      }
    }

    def genNewArray(elemcode: Char, argsp: Seq[Tree], focus: Focus) = {
      val module = NArrayModule(elemcode)
      val meth   = NArrayAllocMethod(elemcode)

      genModuleMethodCall(module, meth, argsp, focus)
    }

    def genNew(
        clssym: Symbol, ctorsym: Symbol, args: List[Tree], focus: Focus) = {
      val cls   = genTypeSym(clssym)
      val alloc = focus.withOp(Op.Alloc(cls))
      val call = genMethodCall(
          ctorsym, statically = true, alloc.value, args, alloc)

      call withValue alloc.value
    }

    def genModuleMethodCall(module: Symbol,
                            method: Symbol,
                            args: Seq[Tree],
                            focus: Focus): Focus = {
      val self = genModule(module, focus)

      genMethodCall(method, statically = true, self.value, args, self)
    }

    def genMethodCall(sym: Symbol,
                      statically: Boolean,
                      selfp: Tree,
                      argsp: Seq[Tree],
                      focus: Focus): Focus = {
      val self = genExpr(selfp, focus)

      genMethodCall(sym, statically, self.value, argsp, self)
    }

    def genMethodCall(sym: Symbol,
                      statically: Boolean,
                      self: Val,
                      argsp: Seq[Tree],
                      focus: Focus): Focus = {
      val name         = genMethodName(sym)
      val sig          = genMethodSig(sym)
      val (args, last) = genArgs(argsp, focus)
      val method =
        if (statically)
          last withValue Val.Global(name, nir.Type.Ptr)
        else
          last withOp Op.Method(sig, self, name)
      val values = self +: args

      method withOp Op.Call(sig, method.value, values)
    }

    def genArgs(argsp: Seq[Tree], focus: Focus): (Seq[Val], Focus) = {
      val args      = sequenced(argsp, focus)(genExpr(_, _))
      val argvalues = args.map(_.value)
      val last      = args.lastOption.getOrElse(focus)

      (argvalues, last)
    }
  }
}

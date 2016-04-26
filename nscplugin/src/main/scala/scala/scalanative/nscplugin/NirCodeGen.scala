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

abstract class NirCodeGen extends PluginComponent
                             with NirFiles
                             with NirTypeEncoding
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
    var mutableVars     = Set.empty[Symbol]
    var labels          = Set.empty[LabelDef]
    val labelApplyCount = mutable.Map.empty[Symbol, Int].withDefault(_ => 0)

    override def traverse(tree: Tree) = {
      tree match {
        case label: LabelDef =>
          labels += label
        case Assign(id @ Ident(_), _) =>
          mutableVars += id.symbol
        case Apply(fun, _) if fun.symbol.isLabel =>
          labelApplyCount(fun.symbol) = labelApplyCount(fun.symbol) + 1
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
          case EmptyTree => Nil
          case PackageDef(_, stats) => stats flatMap collectClassDefs
          case cd: ClassDef => cd :: Nil
        }
      }
      val classDefs = collectClassDefs(cunit.body)

      classDefs.foreach { cd =>
        val sym = cd.symbol
        if (isPrimitiveValueClass(sym) || (sym == ArrayClass))
          ()
        else {
          val defn = genClass(cd)
          genIRFile(cunit, sym, defn)
        }
      }
    }

    def genClass(cd: ClassDef): Seq[Defn] = scoped (
      curClassSym := cd.symbol
    ) {
      val sym     = cd.symbol
      val body    = cd.impl.body
      def attrs   = genClassAttrs(sym)
      def name    = genClassName(sym)
      def parent  = if (sym.superClass == NoSymbol) Nrt.Object.name
                    else if (sym.superClass == ObjectClass) Nrt.Object.name
                    else genClassName(sym.superClass)
      def traits  = genClassInterfaces(sym)
      def members = genClassMembers(sym, body)

      if (isModule(sym))
        Defn.Module(attrs, name, parent, traits) +: members
      else if (sym.isInterface)
        Defn.Trait(attrs, name, traits) +: members
      else
        Defn.Class(attrs, name, parent, traits) +: members
    }

    def genClassAttrs(sym: Symbol): Seq[Attr] = {
      def pinned = {
        def modulePinnedInit =
          if (isModule(sym) && !isExternalModule(sym))
            Seq(genMethodName(sym.asClass.primaryConstructor))
          else
            Seq()
        def pinnedOverrides =
          sym.info.declarations.collect {
            case decl if decl.overrides.nonEmpty =>
              genMethodName(decl)
          }
        val all = modulePinnedInit ++ pinnedOverrides

        if (all.nonEmpty) Some(Attr.Pin(all)) else None
      }

      pinned ++: sym.annotations.collect {
        case ann if ann.symbol == ExternClass => Attr.External
      }
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol
        if psym.isInterface
      } yield {
        genClassName(psym)
      }

    def genClassMembers(sym: Symbol, body: Seq[Tree]): Seq[Defn] =
      if (isExternalModule(sym)) {
        val ctorBody =
          body.collectFirst {
            case dd: DefDef if dd.name == nme.CONSTRUCTOR => dd.rhs
          }.get
        val fieldSyms =
          ctorBody match {
            case Block(_ +: init, _) =>
              init.map {
                case Assign(ref: RefTree, Apply(extern, Seq()))
                    if extern.symbol == ExternMethod =>
                  ref.symbol
                case stat =>
                  unsupported(stat)
              }
          }

        body.flatMap {
          case dd @ DefDef(_, name, _, _, _, Apply(rhs: RefTree, Seq()))
              if name != nme.CONSTRUCTOR
              && rhs.symbol == ExternMethod =>
            val sym   = dd.symbol
            val attrs = genMethodAttrs(sym) :+ Attr.External
            val name  = genMethodName(sym)
            val sig   = genMethodSig(sym)

            Seq(Defn.Declare(attrs, name, sig))

          case dd: DefDef
              if dd.name == nme.CONSTRUCTOR
              || dd.symbol.hasFlag(ACCESSOR) =>
            Seq()

          case vd: ValDef if fieldSyms.contains(vd.symbol) =>
            val sym   = vd.symbol
            val attrs = Seq(Attr.External)
            val name  = genFieldName(sym)
            val ty    = genType(sym.info)

            if (sym.hasFlag(MUTABLE))
              Seq(Defn.Var(attrs, name, ty, Val.None))
            else
              Seq(Defn.Const(attrs, name, ty, Val.None))

          case stat =>
            unsupported(stat)
        }
      } else {
        def fields  = genClassFields(sym).toSeq
        def methods = body.collect { case dd: DefDef => genMethod(dd) }

        fields ++ methods
      }

    def genClassFields(sym: Symbol) =
      for {
        f <- sym.info.decls
        if !f.isMethod && f.isTerm && !isModule(f)
      } yield {
        val name = genFieldName(f)
        val ty = genType(f.tpe)
        Defn.Var(Seq(), name, ty, Val.Zero(ty))
      }

    def genMethod(dd: DefDef): Defn = scoped (
      curMethodSym := dd.symbol
    ) {
      val sym       = dd.symbol
      val attrs     = genMethodAttrs(sym)
      val name      = genMethodName(sym)
      val paramSyms = defParamSymbols(dd)
      val sig       = genMethodSig(sym)

      if (sym.isDeferred)
        Defn.Declare(attrs, name, sig)
      else {
        val fresh  = new Fresh("src")
        val env    = new Env(fresh)

        scoped (
          curEnv       := env,
          curLocalInfo := (new CollectLocalInfo).collect(dd.rhs)
        ) {
          val params = genParams(paramSyms)
          Defn.Define(attrs, name, sig, genMethodBody(params, dd.rhs))
        }
      }
    }

    def genMethodAttrs(sym: Symbol): Seq[Attr] =
      sym.overrides.map {
        case sym => Attr.Override(genMethodName(sym))
      } ++ sym.annotations.collect {
        case ann if ann.symbol == InlineClass   => Attr.InlineHint
        case ann if ann.symbol == NoInlineClass => Attr.NoInline
      }

    def genMethodSig(sym: Symbol): nir.Type = sym match {
      case sym: ModuleSymbol =>
        val MethodType(params, res) = sym.tpe
        val paramtys = params.map(p => genType(p.tpe))
        val selfty   = genType(sym.owner.tpe)
        val retty    = genType(res)

        Type.Function(Seq(selfty), retty)

      case sym: MethodSymbol =>
        val params    = sym.paramLists.flatten
        val paramtys  = params.map(p => genType(p.tpe))
        val owner     = sym.owner
        val selfty    =
          if (isExternalModule(owner)) None
          else Some(genType(sym.owner.tpe))
        val retty     =
          if (sym.isClassConstructor) Type.Unit
          else genType(sym.tpe.resultType)

        Type.Function(selfty ++: paramtys, retty)
    }

    def defParamSymbols(dd: DefDef): List[Symbol] = {
      val vp = dd.vparamss
      if (vp.isEmpty) Nil else vp.head.map(_.symbol)
    }

    def genParams(paramSyms: Seq[Symbol]): Seq[Val.Local] = {
      val self = Val.Local(curEnv.fresh(), genType(curClassSym.tpe))
      val params = paramSyms.map { sym =>
        val name = curEnv.fresh()
        val ty = genType(sym.tpe)
        val param = Val.Local(name, ty)
        curEnv.enter(sym, param)
        param
      }

      self +: params
    }

    def notMergeableGuard(f: => Focus): Focus =
      try f
      catch {
        case Focus.NotMergeable(focus) => focus
      }

    def genMethodBody(params: Seq[Val.Local], bodyp: Tree): Seq[nir.Block] =
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

        case _ =>
          val body =
            scoped (
              curThis := Val.Local(params.head.name, params.head.ty)
            ) {
              notMergeableGuard(genExpr(bodyp, Focus.entry(params)(curEnv.fresh)))
            }

          (body finish Cf.Ret(body.value)).blocks
      }

    def genExpr(tree: Tree, focus: Focus): Focus = tree match {
      case label: LabelDef =>
        assert(label.params.length == 0)
        curEnv.enterLabel(label)
        genLabel(label)

      case vd: ValDef =>
        // TODO: attribute valdef name to the rhs node
        val rhs = genExpr(vd.rhs, focus)
        val isMutable = curLocalInfo.mutableVars.contains(vd.symbol)
        if (!isMutable) {
          curEnv.enter(vd.symbol, rhs.value)
          rhs withValue Val.Unit
        } else {
          val ty = genType(vd.symbol.tpe)
          val alloca = rhs.withOp(Op.Alloca(ty))
          curEnv.enter(vd.symbol, alloca.value)
          alloca withOp Op.Store(ty, alloca.value, rhs.value)
        }

      case If(cond, thenp, elsep) =>
        genIf(cond, thenp, elsep, genType(tree.tpe), focus)

      case Return(exprp) =>
        val expr = genExpr(exprp, focus)
        expr.finish(Cf.Ret(expr.value))

      case Try(expr, catches, finalizer) if catches.isEmpty && finalizer.isEmpty =>
        genExpr(expr, focus)

      case Try(expr, catches, finalizer) =>
        genTry(genType(tree.tpe), expr, catches, finalizer, focus)

      case Throw(exprp) =>
        val expr = genExpr(exprp, focus)
        val call = expr withOp Nrt.call(Nrt.throw_, expr.value)
        call finish Cf.Unreachable

      case app: Apply =>
        genApply(app, focus)

      case app: ApplyDynamic =>
        genApplyDynamic(app, focus)

      case This(qual) =>
        focus.withValue {
          if (tree.symbol == curClassSym.get) curThis.get
          else Val.Global(genClassName(tree.symbol), genType(tree.tpe))
        }

      case Select(qualp, selp) =>
        val sym = tree.symbol
        if (isModule(sym))
          focus withOp Op.Module(genClassName(sym))
        else if (sym.isStaticMember)
          genStaticMember(sym, focus)
        else if (sym.isMethod)
          genMethodCall(sym, statically = false, qualp, Seq(), focus)
        else {
          val ty = genType(tree.symbol.tpe)
          val qual = genExpr(qualp, focus)
          val elem = qual.withOp(Op.Field(ty, qual.value, genFieldName(tree.symbol)))
          elem withOp Op.Load(ty, elem.value)
        }

      case id: Ident =>
        val sym = id.symbol
        if (curLocalInfo.mutableVars.contains(sym))
          focus withOp Op.Load(genType(sym.tpe), curEnv.resolve(sym))
        else if (isModule(sym))
          focus withOp Op.Module(genClassName(sym))
        else
          focus withValue(curEnv.resolve(sym))

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
            val elem = rhs.withOp(Op.Field(ty, qual.value, genFieldName(sel.symbol)))
            elem.withOp(Op.Store(ty, elem.value, rhs.value))

          case id: Ident =>
            val ty = genType(id.tpe)
            val rhs = genExpr(rhsp, focus)
            rhs.withOp(Op.Store(ty, curEnv.resolve(id.symbol), rhs.value))
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
        abort("Unexpected tree in genExpr: " +
              tree + "/" + tree.getClass + " at: " + tree.pos)
    }

    def genLiteral(lit: Literal, focus: Focus): Focus = {
      val value = lit.value
      value.tag match {
        case NullTag
           | UnitTag
           | BooleanTag
           | ByteTag
           | ShortTag
           | CharTag
           | IntTag
           | LongTag
           | FloatTag
           | DoubleTag
           | StringTag =>
          focus withValue genLiteralValue(lit)

        case ClazzTag =>
          val typeof = focus withOp Op.TypeOf(genType(value.typeValue))
          genBoxClass(typeof.value, typeof)

        case EnumTag =>
          genStaticMember(value.symbolValue, focus)
      }
    }

    def genLiteralValue(lit: Literal): Val = {
      val value = lit.value
      value.tag match {
        case NullTag =>
          Val.Zero(Nrt.Object)
        case UnitTag =>
          Val.Unit
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
      focus withOp Op.Module(genClassName(sym))

    def genStaticMember(sym: Symbol, focus: Focus): Focus = {
      val ty     = genType(sym.tpe)
      val module = focus withOp Op.Module(genClassName(sym.owner))
      val elem   = module withOp Op.Field(ty, module.value, genFieldName(sym))

      elem withOp Op.Load(ty, elem.value)
    }

    def genTry(retty: nir.Type, expr: Tree, catches: List[Tree], finalizer: Tree, focus: Focus) =
      focus.branchTry(retty, normal = genExpr(expr, _), exc = genCatch(retty, catches, _, _))

    def genCatch(retty: nir.Type, catches: List[Tree], excrec: Val, focus: Focus) = {
      val excwrap = focus   withOp Op.Extract(excrec, Seq(0))
      val exccast = excwrap withOp Op.Conv(Conv.Bitcast, Type.Ptr(Nrt.Object), excwrap.value)
      val exc     = exccast withOp Op.Load(Nrt.Object, exccast.value)

      val cases =
        catches.map {
          case CaseDef(pat, _, body) =>
            val (symopt, excty) = pat match {
              case Typed(Ident(nme.WILDCARD), tpt) =>
                (None, genType(tpt.tpe))
              case Ident(nme.WILDCARD) =>
                (None, genType(ThrowableClass.tpe))
              case Bind(_, _) =>
                (Some(pat.symbol), genType(pat.symbol.tpe))
            }

            (excty, { focus: Focus =>
              val enter = symopt.map { sym =>
                val cast = focus withOp Op.As(excty, exc.value)
                curEnv.enter(sym, cast.value)
                cast
              }.getOrElse(focus)
              val begin = enter withOp Nrt.call(Nrt.begin_catch, excwrap.value)
              val res = genExpr(body, begin)
              val end = res withOp Nrt.call(Nrt.end_catch)
              end withValue res.value
            })
        }

      def wrap(cases: Seq[(nir.Type, Focus => Focus)], focus: Focus): Focus = cases match {
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
        val labels = cases.map { case label: LabelDef => label }
        genMatch(prologue, labels :+ last, focus)
      }

      last match {
        case label: LabelDef if isCaseLabelDef(label) =>
          translateMatch(label)

        case Apply(TypeApply(Select(label: LabelDef, nme.asInstanceOf_Ob), _), _)
            if isCaseLabelDef(label) =>
          translateMatch(label)

        case _ =>
          val focs = sequenced(stats, focus)(genExpr(_, _))
          val lastfocus = focs.lastOption.getOrElse(focus)
          genExpr(last, lastfocus)
      }
    }

    def genMatch(prologue: List[Tree], lds: List[LabelDef], focus: Focus) = {
      val prfocus = sequenced(prologue, focus)(genExpr(_, _))
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
      val local   = curEnv.resolveLabel(label)
      val params  = label.params.map { id =>
        val local = Val.Local(curEnv.fresh(), genType(id.tpe))
        curEnv.enter(id.symbol, local)
        local
      }
      val entry   = Focus.entry(local, params)(curEnv.fresh)
      val newThis: Val = if (hijackThis) params.head else curThis

      scoped (
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
      val init      =
        if (elems.isEmpty)
          alloc
        else {
          val (values, last) = genArgs(elems, alloc)
          val updates =
            sequenced(values.zipWithIndex, last) { (vi, focus) =>
              val (v, i) = vi
              val idx    = Literal(Constant(i))

              genMethodCall(NArrayUpdateMethod(elemcode), statically = true,
                            alloc.value, Seq(idx), focus)
            }

          updates.last
        }

      init withValue alloc.value
    }

    def genIf(condp: Tree, thenp: Tree, elsep: Tree, retty: nir.Type, focus: Focus) = {
      val cond = genExpr(condp, focus)
      cond.branchIf(cond.value, retty, genExpr(thenp, _), genExpr(elsep, _))
    }

    def genSwitch(m: Match, focus: Focus): Focus = {
      val Match(scrutp, casesp) = m
      val scrut = genExpr(scrutp, focus)
      val retty = genType(m.tpe)
      val defaultcase: Tree =
        casesp.collectFirst {
          case c @ CaseDef(Ident(nme.WILDCARD), _, body) => body
        }.get
      val normalcases: Seq[(Val, Tree)] =
        casesp.flatMap {
          case CaseDef(Ident(nme.WILDCARD), _, _) =>
            Seq()
          case CaseDef(pat, guard, body) =>
            assert(guard.isEmpty)
            val vals: Seq[Val] =
              pat match {
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

      scrut.branchSwitch(scrut.value, retty,
        defaultf = genExpr(defaultcase, _),
        casevals = normalcases.map { case (v, _) => v },
        casefs   = normalcases.map { case (_, body) => genExpr(body, _: Focus) })
    }

    def genApplyDynamic(app: ApplyDynamic, focus: Focus) =
      unsupported(app)

    def genApply(app: Apply, focus: Focus): Focus = {
      val Apply(fun, args) = app

      fun match {
        case _: TypeApply =>
          genApplyTypeApply(app, focus)
        case Select(Super(_, _), _) =>
          genMethodCall(fun.symbol, statically = true, curThis.get, args, focus)
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
            genMethodCall(fun.symbol, statically = false, receiverp, args, focus)
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
      genModuleMethodCall(BoxesRunTimeModule, BoxMethod(genPrimCode(tpe)), Seq(argp), focus)

    def genPrimitiveUnbox(argp: Tree, tpe: Type, focus: Focus) =
      genModuleMethodCall(BoxesRunTimeModule, UnboxMethod(genPrimCode(tpe)), Seq(argp), focus)

    def genPrimitiveOp(app: Apply, focus: Focus): Focus = {
      import scalaPrimitives._

      val sym = app.symbol
      val Apply(fun @ Select(receiver, _), args) = app
      val code = scalaPrimitives.getPrimitive(sym, receiver.tpe)

      if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code))
        genSimpleOp(app, receiver :: args, code, focus)
      else if (code == CONCAT)
        genStringConcat(receiver, args, focus)
      else if (code == HASH)
        genHashCode(app, receiver, focus)
      else if (isArrayOp(code))
        genArrayOp(app, code, focus)
      else if (isCoercion(code))
        genCoercion(app, receiver, code, focus)
      else if (code == SYNCHRONIZED)
        genSynchronized(app, focus)
      else if (code == ANY_GETCLASS)
        genGetClass(receiver, focus)
      else if (code >= MONITOR_NOTIFY && code <= MONITOR_WAIT)
        genMonitorOp(receiver, args, code, focus)
      else
        abort("Unknown primitive operation: " + sym.fullName + "(" +
              fun.symbol.simpleName + ") " + " at: " + (app.pos))
    }

    lazy val jlClassName     = nir.Global.Type("java.lang.Class")
    lazy val jlClass         = nir.Type.Class(jlClassName)
    lazy val jlClassCtorName = jlClassName + "init_class.nrt.Type"
    lazy val jlClassCtorSig  = nir.Type.Function(Seq(jlClass, Nrt.Type), nir.Type.Unit)
    lazy val jlClassCtor     = nir.Val.Global(jlClassCtorName, nir.Type.Ptr(jlClassCtorSig))

    def genGetClass(receiverp: Tree, focus: Focus): Focus = {
      val receiver = genExpr(receiverp, focus)
      val gettype  = receiver withOp Nrt.call(Nrt.Object_getType, receiver.value)

      genBoxClass(gettype.value, gettype)
    }

    def genBoxClass(type_ : Val, focus: Focus) = {
      val alloc = focus withOp Op.Alloc(jlClass)
      val init  = alloc withOp Op.Call(jlClassCtorSig, jlClassCtor, Seq(alloc.value, type_))

      init withValue alloc.value
    }

    def genMonitorOp(receiverp: Tree, argsp: Seq[Tree], code: Int, focus: Focus): Focus = {
      import NirPrimitives._

      val receiver          = genExpr(receiverp, focus)
      val (args, argfocus)  = genArgs(argsp, receiver)
      val monitor           = argfocus withOp Nrt.call(Nrt.Object_getMonitor, receiver.value)
      val (fun, actualArgs) = code match {
        case MONITOR_NOTIFY    => (Nrt.Monitor_notify   , Seq(monitor.value))
        case MONITOR_NOTIFYALL => (Nrt.Monitor_notifyAll, Seq(monitor.value))
        case MONITOR_WAIT      =>
          argsp.length match {
            case 0 => (Nrt.Monitor_wait, Seq(monitor.value, Val.I64(0), Val.I32(0)))
            case 1 => (Nrt.Monitor_wait, Seq(monitor.value, args.head, Val.I32(0)))
            case 2 => (Nrt.Monitor_wait, monitor.value +: args)
          }
      }

      monitor withOp Nrt.call(fun, actualArgs: _*)
    }

    def genIntrinsicCall(fun: Val.Global, argsp: Seq[Tree], focus: Focus): Focus = {
      val (args, last) = genArgs(argsp, focus)

      last withOp Nrt.call(fun, args: _*)
    }

    def numOfType(num: Int, ty: nir.Type) = ty match {
      case Type.I8  => Val.I8 (num.toByte)
      case Type.I16 => Val.I16(num.toShort)
      case Type.I32 => Val.I32(num)
      case Type.I64 => Val.I64(num.toLong)
      case Type.F32 => Val.F32(num.toFloat)
      case Type.F64 => Val.F64(num.toDouble)
      case _        => unreachable
    }

    def genSimpleOp(app: Apply, args: List[Tree], code: Int, focus: Focus) = {
      val retty = genType(app.tpe)

      args match {
        case List(right)       => genUnaryOp(code, right, retty, focus)
        case List(left, right) => genBinaryOp(code, left, right, retty, focus)
        case _                 => abort("Too many arguments for primitive function: " + app)
      }
    }

    def genUnaryOp(code: Int, rightp: Tree, opty: nir.Type, focus: Focus) = {
      import scalaPrimitives._
      val right = genExpr(rightp, focus)

      (opty, code) match {
        case (Type.I(_) |
              Type.F(_), POS)  => right
        case (Type.F(_), NEG ) => right withOp Op.Bin(Bin.Isub, opty, numOfType( 0, opty), right.value)
        case (Type.I(_), NEG ) => right withOp Op.Bin(Bin.Fsub, opty, numOfType( 0, opty), right.value)
        case (Type.I(_), NOT ) => right withOp Op.Bin(Bin.Xor,  opty, numOfType(-1, opty), right.value)
        case (Type.I(_), ZNOT) => right withOp Op.Bin(Bin.Xor,  opty, Val.True,            right.value)
        case _                 => abort("Unknown unary operation code: " + code)
      }
    }

    def genBinaryOp(code: Int, left: Tree, right: Tree, retty: nir.Type,
                    focus: Focus): Focus = {
      import scalaPrimitives._

      val lty  = genType(left.tpe)
      val rty  = genType(right.tpe)
      val opty = binaryOperationType(lty, rty)

      opty match {
        case Type.F(_) =>
          code match {
            case ADD => genBinaryOp(Op.Bin(Bin.Fadd,  _, _, _), left, right, opty, focus)
            case SUB => genBinaryOp(Op.Bin(Bin.Fsub,  _, _, _), left, right, opty, focus)
            case MUL => genBinaryOp(Op.Bin(Bin.Fmul,  _, _, _), left, right, opty, focus)
            case DIV => genBinaryOp(Op.Bin(Bin.Fdiv,  _, _, _), left, right, opty, focus)
            case MOD => genBinaryOp(Op.Bin(Bin.Frem,  _, _, _), left, right, opty, focus)

            case EQ => genBinaryOp(Op.Comp(Comp.Feq, _, _, _), left, right, opty, focus)
            case NE => genBinaryOp(Op.Comp(Comp.Fne, _, _, _), left, right, opty, focus)
            case LT => genBinaryOp(Op.Comp(Comp.Flt, _, _, _), left, right, opty, focus)
            case LE => genBinaryOp(Op.Comp(Comp.Fle, _, _, _), left, right, opty, focus)
            case GT => genBinaryOp(Op.Comp(Comp.Fgt, _, _, _), left, right, opty, focus)
            case GE => genBinaryOp(Op.Comp(Comp.Fge, _, _, _), left, right, opty, focus)

            case _ => abort("Unknown floating point type binary operation code: " + code)
          }

        case Type.I(_) =>
          code match {
            case ADD => genBinaryOp(Op.Bin(Bin.Iadd, _, _, _), left, right, opty, focus)
            case SUB => genBinaryOp(Op.Bin(Bin.Isub, _, _, _), left, right, opty, focus)
            case MUL => genBinaryOp(Op.Bin(Bin.Imul, _, _, _), left, right, opty, focus)
            case DIV => genBinaryOp(Op.Bin(Bin.Sdiv, _, _, _), left, right, opty, focus)
            case MOD => genBinaryOp(Op.Bin(Bin.Srem, _, _, _), left, right, opty, focus)

            case OR  => genBinaryOp(Op.Bin(Bin.Or,   _, _, _), left, right, opty, focus)
            case XOR => genBinaryOp(Op.Bin(Bin.Xor,  _, _, _), left, right, opty, focus)
            case AND => genBinaryOp(Op.Bin(Bin.And,  _, _, _), left, right, opty, focus)
            case LSL => genBinaryOp(Op.Bin(Bin.Shl,  _, _, _), left, right, opty, focus)
            case LSR => genBinaryOp(Op.Bin(Bin.Lshr, _, _, _), left, right, opty, focus)
            case ASR => genBinaryOp(Op.Bin(Bin.Ashr, _, _, _), left, right, opty, focus)

            case EQ => genBinaryOp(Op.Comp(Comp.Ieq, _, _, _), left, right, opty, focus)
            case NE => genBinaryOp(Op.Comp(Comp.Ine, _, _, _), left, right, opty, focus)
            case LT => genBinaryOp(Op.Comp(Comp.Slt, _, _, _), left, right, opty, focus)
            case LE => genBinaryOp(Op.Comp(Comp.Sle, _, _, _), left, right, opty, focus)
            case GT => genBinaryOp(Op.Comp(Comp.Sgt, _, _, _), left, right, opty, focus)
            case GE => genBinaryOp(Op.Comp(Comp.Sge, _, _, _), left, right, opty, focus)

            case ZOR  => genIf(left, Literal(Constant(true)), right, retty, focus)
            case ZAND => genIf(left, right, Literal(Constant(false)), retty, focus)

            case _ => abort("Unknown integer type binary operation code: " + code)
          }

        case _: Type.RefKind =>
          code match {
            case EQ => genClassEquality(left, right, ref = false, negated = false, focus)
            case NE => genClassEquality(left, right, ref = false, negated = true,  focus)
            case ID => genClassEquality(left, right, ref = true,  negated = false, focus)
            case NI => genClassEquality(left, right, ref = true,  negated = true,  focus)

            case _ => abort("Unknown reference type operation code: " + code)
          }

        case ty =>
          abort("Uknown binary operation type: " + ty)
      }
    }

    def genBinaryOp(op: (nir.Type, Val, Val) => Op, leftp: Tree, rightp: Tree,
                    opty: nir.Type, focus: Focus): Focus = {
      val left         = genExpr(leftp, focus)
      val leftcoerced  = genCoercion(left.value, genType(leftp.tpe), opty, left)
      val right        = genExpr(rightp, leftcoerced)
      val rightcoerced = genCoercion(right.value, genType(rightp.tpe), opty, right)

      rightcoerced withOp op(opty, leftcoerced.value, rightcoerced.value)
    }

    def genClassEquality(leftp: Tree, rightp: Tree, ref: Boolean,
                         negated: Boolean, focus: Focus) = {
      val left  = genExpr(leftp, focus)
      val right = genExpr(rightp, left)

      if (ref) {
        val comp = if (negated) Comp.Ieq else Comp.Ine
        right withOp Op.Comp(comp, Nrt.Object, left.value, right.value)
      } else {
        val call = Nrt.call(Nrt.Object_equals, left.value, right.value)
        val equals = right withOp call
        if (negated)
          equals withOp Op.Bin(Bin.Xor, Type.Bool, Val.True, equals.value)
        else
          equals
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
        Nrt.Object
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

      recv withOp Nrt.call(Nrt.Object_hashCode, recv.value)
    }

    def genArrayOp(app: Apply, code: Int, focus: Focus): Focus = {
      import scalaPrimitives._
      val Apply(Select(arrayp, _), argsp) = app

      val array        = genExpr(arrayp, focus)
      def elemcode     = genArrayCode(arrayp.tpe)
      val method       =
        if (scalaPrimitives.isArrayGet(code))
          NArrayApplyMethod(elemcode)
        else if (scalaPrimitives.isArraySet(code))
          NArrayUpdateMethod(elemcode)
        else
          NArrayLengthMethod(elemcode)

      genMethodCall(method, statically = true, array.value, argsp, array)
    }

    def genSynchronized(app: Apply, focus: Focus): Focus = {
      val Apply(Select(receiverp, _), List(argp)) = app

      val obj   = genExpr(receiverp, focus)
      val mon   = obj withOp Nrt.call(Nrt.Object_getMonitor, obj.value)
      val enter = mon withOp Nrt.call(Nrt.Monitor_enter, mon.value)
      val arg   = genExpr(argp, enter)
      val exit  = arg withOp Nrt.call(Nrt.Monitor_exit, mon.value)

      exit withValue arg.value
    }

    def genCoercion(app: Apply, receiver: Tree, code: Int, focus: Focus): Focus = {
      val rec = genExpr(receiver, focus)
      val (fromty, toty) = coercionTypes(code)

      genCoercion(rec.value, fromty, toty, rec)
    }

    def genCoercion(value: Val, fromty: nir.Type, toty: nir.Type, focus: Focus): Focus =
      if (fromty == toty)
        focus withValue value
      else {
        val conv = (fromty, toty) match {
          case (nir.Type.I(lwidth), nir.Type.I(rwidth))
            if lwidth < rwidth                => Conv.Sext
          case (nir.Type.I(lwidth), nir.Type.I(rwidth))
            if lwidth > rwidth                => Conv.Trunc
          case (nir.Type.I(_), nir.Type.F(_)) => Conv.Sitofp
          case (nir.Type.F(_), nir.Type.I(_)) => Conv.Fptosi
          case (nir.Type.F64, nir.Type.F32)   => Conv.Fptrunc
          case (nir.Type.F32, nir.Type.F64)   => Conv.Fpext
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

        case S2B       | C2B       => (nir.Type.I16, nir.Type.I8)
        case S2S | S2C | C2S | C2C => (nir.Type.I16, nir.Type.I16)
        case S2I       | C2I       => (nir.Type.I16, nir.Type.I32)
        case S2L       | C2L       => (nir.Type.I16, nir.Type.I64)
        case S2F       | C2F       => (nir.Type.I16, nir.Type.F32)
        case S2D       | C2D       => (nir.Type.I16, nir.Type.F64)

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
      val ty = genType(targs.head.tpe)
      val rec = genExpr(receiverp, focus)
      rec.withOp(fun.symbol match {
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

    def genNew(clssym: Symbol, ctorsym: Symbol, args: List[Tree], focus: Focus) = {
      val cls   = genType(clssym)
      val alloc = focus.withOp(Op.Alloc(cls))
      val call  = genMethodCall(ctorsym, statically = true, alloc.value, args, alloc)

      call withValue alloc.value
    }

    def genModuleMethodCall(module: Symbol, method: Symbol, args: Seq[Tree],
        focus: Focus): Focus = {
      val self = genModule(module, focus)

      genMethodCall(method, statically = true, self.value, args, self)
    }

    def genMethodCall(sym: Symbol, statically: Boolean, selfp: Tree, argsp: Seq[Tree],
        focus: Focus): Focus = {
      val self = genExpr(selfp, focus)

      genMethodCall(sym, statically, self.value, argsp, self)
    }

    def genMethodCall(sym: Symbol, statically: Boolean, self: Val, argsp: Seq[Tree],
        focus: Focus): Focus = {
      val name         = genMethodName(sym)
      val sig          = genMethodSig(sym)
      val (args, last) = genArgs(argsp, focus)
      val external     = isExternalModule(sym.owner)
      val method       =
        if (statically || external)
          last withValue Val.Global(name, nir.Type.Ptr(sig))
        else
          last withOp Op.Method(sig, self, name)
      val values =
        if (external) args
        else self +: args

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


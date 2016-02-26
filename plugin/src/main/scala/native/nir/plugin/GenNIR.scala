package native
package nir
package plugin

import scala.collection.mutable
import scala.tools.nsc._
import scala.tools.nsc.plugins._
import scala.util.{Either, Left, Right}
import native.nir.Focus, Focus.sequenced
import native.util, util._, util.ScopedVar.scoped
import native.nir.Shows._

abstract class GenNIR extends PluginComponent
                         with NativeIntrinsics
                         with GenIRFiles
                         with GenTypeKinds
                         with GenNameEncoding {
  import global.{ merge => _, _ }
  import global.definitions._
  import global.treeInfo.hasSynthCaseSymbol

  val phaseName = "native"

  override def newPhase(prev: Phase): StdPhase =
    new SaltyCodePhase(prev)

  class Env(val fresh: Fresh) {
    private val env = mutable.Map.empty[Symbol, Val]

    def enter(sym: Symbol, value: Val): Unit = env += ((sym, value))
    def enterLabel(ld: LabelDef): Unit = enter(ld.symbol, Val.Local(fresh(), Type.Label))
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
          val defns = genClass(cd)
          //println(sh"${defns.head}")
          genIRFile(cunit, sym, defns)
        }
      }
    }

    def genClass(cd: ClassDef): Seq[Defn] = scoped (
      curClassSym := cd.symbol
    ) {
      //println(cd)
      val sym     = cd.symbol
      val attrs   = genClassAttrs(sym)
      val name    = genClassName(sym)
      def parent  = if (sym.superClass == NoSymbol) Intr.object_.name
                    else if (sym.superClass == ObjectClass) Intr.object_.name
                    else genClassName(sym.superClass)
      val ifaces  = genClassInterfaces(sym)
      val fields  = genClassFields(sym).toSeq
      val defdefs = cd.impl.body.collect { case dd: DefDef => dd }
      val methods = defdefs.map(genDef)
      val members = fields ++ methods

      if (isModule(sym))
        Seq(Defn.Module(attrs, name, parent, ifaces, members))
      else if (sym.isInterface)
        Seq(Defn.Interface(attrs, name, ifaces, members))
      else
        Seq(Defn.Class(attrs, name, parent, ifaces, members))
    }

    def genClassAttrs(sym: Symbol): Seq[Attr] = Seq()

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol
        if psym.isInterface
      } yield {
        genClassName(psym)
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

    def genDef(dd: DefDef): Defn = scoped (
      curMethodSym := dd.symbol
    ) {
      //println(dd)
      val sym       = dd.symbol
      val attrs     = genDefAttrs(sym)
      val name      = genDefName(sym)
      val paramSyms = defParamSymbols(dd)
      val sig       = genDefSig(sym)

      if (sym.isDeferred)
        Defn.Declare(attrs, name, sig)
      else {
        val fresh = new Fresh("src")
        val env = new Env(fresh)
        scoped (
          curEnv := env,
          curLocalInfo := (new CollectLocalInfo).collect(dd.rhs)
        ) {
          val focus = genDefBody(paramSyms, dd.rhs)
          val blocks = focus.finish(Cf.Ret(focus.value)).blocks
          Defn.Define(attrs, name, sig, blocks)
        }
      }
    }

    val overridesToString = Attr.Override(Intr.object_toString.name)
    val overridesEquals   = Attr.Override(Intr.object_equals.name)
    val overridesHashCode = Attr.Override(Intr.object_hashCode.name)

    def genDefAttrs(sym: Symbol): Seq[Attr] = {
      sym.overrides.collect {
        case JObject_toString => overridesToString
        case JObject_equals   => overridesEquals
        case JObject_hashCode => overridesHashCode
        case sym              => Attr.Override(genDefName(sym))
      }
    }

    def genDefSig(sym: Symbol): nir.Type = sym match {
      case sym: ModuleSymbol =>
        val MethodType(params, res) = sym.tpe
        val paramtys = params.map(p => genType(p.tpe))
        val selfty   = genType(sym.owner.tpe)
        val retty    = genType(res)
        Type.Function(Seq(selfty), retty)

      case sym: MethodSymbol =>
        val params    = sym.paramLists.flatten
        val paramtys  = params.map(p => genType(p.tpe))
        val selfty    = genType(sym.owner.tpe)
        val retty     =
          if (sym.isClassConstructor) Intr.unit
          else genType(sym.tpe.resultType)
        Type.Function(selfty +: paramtys, retty)
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

    def genDefBody(paramSyms: Seq[Symbol], body: Tree) =
      notMergeableGuard {
        body match {
          /*
          case Block(List(ValDef(_, nme.THIS, _, _)),
                     label @ LabelDef(name, Ident(nme.THIS) :: _, rhs)) =>

            curLabelEnv.enterLabel(label, curLocalInfo.labelApplyCount(label.symbol) + 1)
            val start = Focus.start()
            val values = params.take(label.params.length)
            curLabelEnv.enterLabelCall(label.symbol, values, start)
            scoped (
              curThis := curLabelEnv.resolveLabelParams(label.symbol).head
            ) {
              genLabel(label)
            }
          */
          case _ =>
            val params = genParams(paramSyms)
            scoped (
              curThis := Val.Local(params.head.name, params.head.ty)
            ) {
              genExpr(body, Focus.entry(params)(curEnv.fresh))
            }
        }
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
          rhs withValue Intr.unit_value
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
        expr.finish(Cf.Throw(expr.value))

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
          genMethodCall(sym, qualp, Seq(), focus)
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
          ???
        case EnumTag =>
          genStaticMember(value.symbolValue, focus)
      }
    }

    def genLiteralValue(lit: Literal): Val = {
      val value = lit.value
      value.tag match {
        case NullTag =>
          Val.Zero(Intr.object_)
        case UnitTag =>
          Intr.unit_value
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
      val exccast = excwrap withOp Op.Conv(Conv.Bitcast, Type.Ptr(Intr.object_), excwrap.value)
      val exc     = exccast withOp Op.Load(Intr.object_, exccast.value)

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
              val begin = enter withOp Intr.call(Intr.begin_catch, excwrap.value)
              val res = genExpr(body, begin)
              val end = res withOp Intr.call(Intr.end_catch)
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

    def genFinally(finalizer: Tree, focus: Focus): Focus = ???/*{
      val Tails(open, closed) = tails

      def genClosed(focus: Focus, wrap: (ir.Node, ir.Node) => ir.Node): Seq[ir.Node] = {
        val ir.End(ends) = genExpr(finalizer, focus).end((cf, ef, v) => wrap(cf, ef))
        ends
      }

      val closedtails = Tails(Seq(), closed.flatMap {
        case ir.Return(cf, ef, v) => genClosed(Focus(cf, ef, Val.Unit()), ir.Return(_, _, v))
        case ir.Throw(cf, ef, v)  => genClosed(Focus(cf, ef, Val.Unit()), ir.Throw(_, _, v))
        case ir.Undefined(cf, ef) => genClosed(Focus(cf, ef, Val.Unit()), ir.Undefined(_, _))
      })

      val opentails =
        if (open.isEmpty) Tails.empty
        else {
          val (focus, _) = Tails(open, Seq()).merge
          genExpr(finalizer, focus)
        }

      opentails ++ closedtails
    }*/

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

    def genLabel(label: LabelDef) = {
      val Val.Local(name, _) = curEnv.resolve(label.symbol)
      val params = label.params.map { id =>
        val local = Val.Local(curEnv.fresh(), genType(id.tpe))
        curEnv.enter(id.symbol, local)
        local
      }
      val entry = Focus.entry(name, params)(curEnv.fresh)
      genExpr(label.rhs, entry)
    }

    def genArrayValue(av: ArrayValue, focus: Focus): Focus = {
      val ArrayValue(tpt, elems) = av
      val elemty     = genArrayElementType(tpt.tpe)
      val len        = elems.length
      val allocfocus = focus withOp Intr.call(Intr.alloc_array(elemty), Val.I32(len))
      val rfocus     =
        if (elems.isEmpty)
          allocfocus
        else {
          val vfocus    = sequenced(elems, allocfocus)(genExpr(_, _))
          val values    = vfocus.map(_.value)
          val lastfocus = vfocus.lastOption.getOrElse(focus)
          val sfocus    = sequenced(values.zipWithIndex, lastfocus) { (vi, foc) =>
            val (value, i) = vi
            foc withOp Intr.call(Intr.array_update(elemty), allocfocus.value, Val.I32(i), value)
          }
          sfocus.last
        }

      rfocus withValue allocfocus.value
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
          genNormalMethodCall(fun.symbol, curThis.get, args, focus)
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
            genMethodCall(fun.symbol, receiverp, args, focus)
          }
      }
    }

    def genApplyLabel(tree: Tree, focus: Focus) = notMergeableGuard {
      val Apply(fun, args) = tree
      val Val.Local(label, _) = curEnv.resolve(fun.symbol)
      val argsfocus = sequenced(args, focus)(genExpr(_, _))
      val lastfocus = argsfocus.lastOption.getOrElse(focus)
      lastfocus finish Cf.Jump(Next.Label(label, argsfocus.map(_.value)))
    }

    lazy val prim2ty = Map(
      BooleanTpe -> Intr.bool,
      ByteTpe    -> Intr.byte,
      CharTpe    -> Intr.char,
      ShortTpe   -> Intr.short,
      IntTpe     -> Intr.int,
      LongTpe    -> Intr.long,
      FloatTpe   -> Intr.float,
      DoubleTpe  -> Intr.double
    )

    lazy val boxed2ty = Map[Symbol, nir.Type](
      BoxedBooleanClass   -> Intr.bool,
      BoxedByteClass      -> Intr.byte,
      BoxedCharacterClass -> Intr.char,
      BoxedShortClass     -> Intr.short,
      BoxedIntClass       -> Intr.int,
      BoxedLongClass      -> Intr.long,
      BoxedFloatClass     -> Intr.float,
      BoxedDoubleClass    -> Intr.double
    )

    def genPrimitiveBox(exprp: Tree, tpe: Type, focus: Focus) = {
      val expr = genExpr(exprp, focus)

      expr withOp Intr.call(Intr.box(prim2ty(tpe.widen)), expr.value)
    }

    def genPrimitiveUnbox(exprp: Tree, tpe: Type, focus: Focus) = {
      val expr = genExpr(exprp, focus)

      expr withOp Intr.call(Intr.unbox(prim2ty(tpe.widen)), expr.value)
    }

    def genPrimitiveOp(app: Apply, focus: Focus): Focus = {
      import scalaPrimitives._

      val sym = app.symbol
      val Apply(fun @ Select(receiver, _), args) = app
      val code = scalaPrimitives.getPrimitive(sym, receiver.tpe)

      if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code))
        genSimpleOp(app, receiver :: args, code, focus)
      else if (code == CONCAT)
        genStringConcat(app, receiver, args, focus)
      else if (code == HASH)
        genHashCode(app, receiver, focus)
      else if (isArrayOp(code))
        genArrayOp(app, code, focus)
      else if (isCoercion(code))
        genCoercion(app, receiver, code, focus)
      else if (code == SYNCHRONIZED)
        genSynchronized(app, focus)
      else
        abort("Unknown primitive operation: " + sym.fullName + "(" +
              fun.symbol.simpleName + ") " + " at: " + (app.pos))
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

        case _: Type.ClassKind =>
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
        right withOp Op.Comp(comp, Intr.object_, left.value, right.value)
      } else {
        val call = Intr.call(Intr.object_equals, left.value, right.value)
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
      case (_: nir.Type.ClassKind, _: nir.Type.ClassKind) =>
        Intr.object_
      case (ty1, ty2) if ty1 == ty2 =>
        ty1
      case _ =>
        abort(s"can't perform binary opeation between $lty and $rty")
    }

    def genStringConcat(tree: Tree, leftp: Tree, args: List[Tree], focus: Focus) = {
      val List(rightp) = args
      val left = genExpr(leftp, focus)
      val right = genExpr(rightp, left)

      right withOp Intr.call(Intr.string_concat, left.value, right.value)
    }

    // TODO: this doesn't seem to get called on foo.## expressions
    def genHashCode(tree: Tree, receiverp: Tree, focus: Focus) = {
      val recv = genExpr(receiverp, focus)

      recv withOp Intr.call(Intr.object_hashCode, recv.value)
    }

    def genArrayOp(app: Apply, code: Int, focus: Focus): Focus = {
      import scalaPrimitives._
      val Apply(Select(array, _), args) = app
      val allfocus = sequenced(array :: args, focus)(genExpr(_, _))
      val lastfocus  = allfocus.last
      def arrayvalue = allfocus(0).value
      def argvalues  = allfocus.tail.map(_.value)
      def elemty     = genArrayElementType(array.tpe)

      if (scalaPrimitives.isArrayGet(code))
        lastfocus withOp Intr.call(Intr.array_apply(elemty), arrayvalue, argvalues(0))
      else if (scalaPrimitives.isArraySet(code))
        lastfocus withOp Intr.call(Intr.array_update(elemty), arrayvalue, argvalues(1), argvalues(2))
      else
        lastfocus withOp Intr.call(Intr.array_length(elemty), arrayvalue)
    }

    def genSynchronized(app: Apply, focus: Focus): Focus = {
      val Apply(Select(receiverp, _), List(argp)) = app
      val rec   = genExpr(receiverp, focus)
      val enter = rec withOp Intr.call(Intr.monitor_enter, rec.value)
      val arg   = genExpr(argp, enter)
      val exit  = arg withOp Intr.call(Intr.monitor_exit, rec.value)

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
      val ctor = fun.symbol
      val kind = genKind(tpt.tpe)
      val ty   = toIRType(kind)

      kind match {
        case builtin: BuiltinClassKind =>
          genNewBuiltin(builtin, ctor, args, focus)
        case arr: ArrayKind =>
          genNewArray(genArrayElementType(arr), args, focus)
        case ckind: ClassKind =>
          genNew(ckind.sym, ctor, args, focus)
        case ty =>
          abort("unexpected new: " + app + "\ngen type: " + ty)
      }
    }

    def genNewBuiltin(builtin: BuiltinClassKind, ctorsym: Symbol,
                      args: List[Tree], focus: Focus) =
      builtin match {
        case JObjectKind =>
          focus withOp Op.Alloc(Intr.object_)
        case JStringKind =>
          ???
        case JCharKind
           | JBooleanKind
           | JByteKind
           | JShortKind
           | JIntKind
           | JLongKind
           | JFloatKind
           | JDoubleKind =>
          genValueOf(toIRType(builtin), args, focus)
      }

    def genNewArray(elemty: nir.Type, lengthsp: Seq[Tree], focus: Focus) = {
      val lengths      = sequenced(lengthsp, focus)(genExpr(_, _))
      val lengthvalues = lengths.map(_.value)
      val last         = lengths.lastOption.getOrElse(focus)

      lengthvalues match {
        case Seq(length) =>
          last withOp Intr.call(Intr.alloc_array(elemty), length)
        case _ =>
          ???
      }
    }

    def genNew(sym: Symbol, ctorsym: Symbol, args: List[Tree], focus: Focus) = {
      val alloc = focus.withOp(Op.Alloc(genType(sym.tpe)))
      val ctor = genNormalMethodCall(ctorsym, alloc.value, args, alloc)

      ctor.withValue(alloc.value)
    }

    def genMethodCall(sym: Symbol, selfp: Tree, argsp: Seq[Tree], focus: Focus): Focus =
      if (isIntrinsic(sym))
        genIntrinsicCall(sym, selfp, argsp, focus)
      else {
        val self = genExpr(selfp, focus)
        genNormalMethodCall(sym, self.value, argsp, self)
      }

    def genNormalMethodCall(sym: Symbol, self: Val, argsp: Seq[Tree], focus: Focus): Focus = {
      val name      = genDefName(sym)
      val sig       = genDefSig(sym)
      val args      = sequenced(argsp, focus)(genExpr(_, _))
      val argvalues = args.map(_.value)
      val last      = args.lastOption.getOrElse(focus)
      val elem      = last withOp Op.Method(sig, self, name)
      val call      = elem withOp Op.Call(sig, elem.value, self +: argvalues)

      call
    }

    def genIntrinsicCall(sym: Symbol, selfp: Tree, argsp: Seq[Tree], focus: Focus): Focus =
      sym match {
        case UnboxValue(fromty, toty) =>
          val self    = genExpr(selfp, focus)
          val unboxed = self withOp Intr.call(Intr.unbox(fromty), self.value)
          genCoercion(unboxed.value, fromty.unboxed, toty, unboxed)
        case BoxValue(boxty) =>
          genValueOf(boxty, argsp, focus)
        case ParseValue(ty) =>
          genValueFromString(ty, argsp, unsigned = false, focus)
        case ParseUnsignedValue(ty) =>
          genValueFromString(ty, argsp, unsigned = true, focus)
        case BoxModuleToString(boxty) =>
          genValueToString(boxty, argsp, unsigned = false, focus)
        case BoxModuleToUnsignedString(boxty) =>
          genValueToString(boxty, argsp, unsigned = true, focus)
        case DivideUnsigned(ty) =>
          val List(l, r) = argsp
          val left = genExpr(l, focus)
          val right = genExpr(r, left)
          right withOp Op.Bin(Bin.Udiv, ty, left.value, right.value)
        case RemainderUnsigned(ty) =>
          val List(l, r) = argsp
          val left = genExpr(l, focus)
          val right = genExpr(r, left)
          right withOp Op.Bin(Bin.Urem, ty, left.value, right.value)
        case ToUnsigned(fromty, toty) =>
          val List(argp) = argsp
          val unboxed = genExpr(argp, focus)
          unboxed withOp Op.Conv(Conv.Zext, toty, unboxed.value)
        case ToString(Intr.object_) =>
          val self = genExpr(selfp, focus)
          self withOp Intr.call(Intr.object_toString, self.value)
        case ToString(boxty) =>
          val self    = genExpr(selfp, focus)
          val unboxed = self withOp Intr.call(Intr.unbox(boxty), self.value)
          unboxed withOp Intr.call(Intr.toString_(boxty), unboxed.value)
        case HashCode(Intr.object_) =>
          val self = genExpr(selfp, focus)
          self withOp Intr.call(Intr.object_hashCode, self.value)
        case HashCode(boxty) =>
          val self    = genExpr(selfp, focus)
          val unboxed = self withOp Intr.call(Intr.unbox(boxty), self.value)
          unboxed withOp Intr.call(Intr.hashCode_(boxty), unboxed.value)
        case ScalaRunTimeHashCode() =>
          val List(argp) = argsp
          val unboxed = genExpr(argp, focus)
          unboxed withOp Intr.call(Intr.object_hashCode, unboxed.value)
        case Equals() =>
          val List(argp) = argsp
          val self = genExpr(selfp, focus)
          val arg  = genExpr(argp, self)
          arg withOp Intr.call(Intr.object_equals, self.value, arg.value)
        case GetClass() =>
          ???
        case ObjectCtor() =>
          focus
      }

    def genValueToString(boxty: nir.Type, argsp: Seq[Tree],
                         unsigned: Boolean, focus: Focus) =
      argsp match {
        case List(valuep) =>
          val ty     = genType(valuep.tpe)
          val value  = genExpr(valuep, focus)
          val lookup = if (unsigned) Intr.toUnsignedString else Intr.toString_
          value withOp Intr.call(lookup(boxty), value.value)
        case List(valuep, radixp) =>
          val ty     = genType(valuep.tpe)
          val value  = genExpr(valuep, focus)
          val radix  = genExpr(radixp, value)
          val lookup = if (unsigned) Intr.toUnsignedString_rdx else Intr.toString_rdx
          value withOp Intr.call(lookup(boxty), value.value, radix.value)
      }

    def genValueFromString(boxty: nir.Type, argsp: Seq[Tree],
                           unsigned: Boolean, focus: Focus) =
      argsp match {
        case List(strp) =>
          val str = genExpr(strp, focus)
          val lookup = if (unsigned) Intr.parseUnsigned else Intr.parse
          str withOp Intr.call(lookup(boxty), str.value)
        case List(strp, radixp) =>
          val str = genExpr(strp, focus)
          val radix = genExpr(radixp, str)
          val lookup = if (unsigned) Intr.parseUnsigned_rdx else Intr.parse_rdx
          radix withOp Intr.call(lookup(boxty), str.value, radix.value)
      }

    def genValueOf(boxty: nir.Type, argsp: Seq[Tree], focus: Focus) = {
      val converted =
        argsp match {
          case List(s, radix) =>
            genValueFromString(boxty.unboxed, argsp, unsigned = false, focus)
          case List(s) if s.tpe.widen == StringTpe =>
            genValueFromString(boxty.unboxed, argsp, unsigned = false, focus)
          case List(valuep) =>
            genExpr(valuep, focus)
        }
      converted withOp Intr.call(Intr.box(boxty), converted.value)
    }
  }
}


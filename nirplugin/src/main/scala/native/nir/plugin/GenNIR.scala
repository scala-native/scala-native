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
                         with NativeBuiltins
                         with GenIRFiles
                         with GenTypeKinds
                         with GenNameEncoding {
  import global.{ merge => _, _ }
  import global.definitions._
  import global.treeInfo.hasSynthCaseSymbol

  val phaseName = "native"

  override def newPhase(prev: Phase): StdPhase =
    new SaltyCodePhase(prev)

  def undefined(focus: Focus) =
    focus.finish(Op.Undefined)

  class Env {
    private val env = mutable.Map.empty[Symbol, Val]

    def enter(sym: Symbol, value: Val): Val = {
      env += ((sym, value))
      value
    }
    def resolve(sym: Symbol): Val = env(sym)
  }

  class LabelEnv(env: Env) {
    def enterLabel(ld: LabelDef, calls: Int) = ???
    def enterLabelCall(sym: Symbol, values: Seq[Val], focus: Focus) = ???
    def resolveLabel(sym: Symbol) = ???
    def resolveLabelParams(sym: Symbol) = ???
    def resolveLabelEf(sym: Symbol) = ???
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
    implicit val fresh = new Fresh

    val curLocalInfo = new util.ScopedVar[CollectLocalInfo]
    val curClassSym  = new util.ScopedVar[Symbol]
    val curMethodSym = new util.ScopedVar[Symbol]
    val curEnv       = new util.ScopedVar[Env]
    val curLabelEnv  = new util.ScopedVar[LabelEnv]
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
          println(cd)
          val defns = genClass(cd)
          println(sh"${defns.head}")
          genIRFile(cunit, sym, defns)
        }
      }
    }

    def isForeignExternModule(sym: Symbol): Boolean =
      sym.annotations.find(_.tpe =:= ExternClass.tpe).isDefined

    def genClass(cd: ClassDef): Seq[Defn] = scoped (
      curClassSym := cd.symbol
    ) {
      val sym     = cd.symbol
      val name    = genClassName(sym)
      val parent  = if (sym.superClass == NoSymbol) Type.None
                    else genType(sym.superClass.tpe)
      val ifaces  = genClassInterfaces(sym)
      val fields  = genClassFields(sym).toSeq
      val defdefs = cd.impl.body.collect { case dd: DefDef => dd }
      val methods = defdefs.map(genDef)
      val members = fields ++ methods

      if (sym.isModuleClass || sym.isImplClass)
        Seq(Defn.Module(name, parent, ifaces, members))
      else if (sym.isInterface)
        Seq(Defn.Interface(name, ifaces, members))
      else
        Seq(Defn.Class(name, parent, ifaces, members))
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol
        if psym.isInterface
      } yield {
        genType(psym.tpe)
      }

    def genClassFields(sym: Symbol) =
      for {
        f <- sym.info.decls
        if !f.isMethod && f.isTerm && !f.isModule
      } yield {
        val name = genFieldName(f)
        val ty = genType(f.tpe)
        Defn.Var(name, ty, Val.Zero(ty))
      }

    def genDef(dd: DefDef): Defn = scoped (
      curMethodSym := dd.symbol
    ) {
      val sym = dd.symbol
      val name = genDefName(sym)
      val paramSyms = defParamSymbols(dd)
      val sig = genDefSig(sym)

      if (isForeignExternModule(sym.owner))
        Defn.Declare(genForeignName(sym), sig)
      else if (sym.isDeferred)
        Defn.Declare(name, sig)
      else {
        val env = new Env
        scoped (
          curEnv := env,
          curLabelEnv := new LabelEnv(env),
          curLocalInfo := (new CollectLocalInfo).collect(dd.rhs)
        ) {
          val focus = genDefBody(paramSyms, dd.rhs)
          val blocks = focus.finish(Op.Ret(focus.value)).blocks
          Defn.Define(name, sig, blocks)
        }
      }
    }

    def genDefSig(sym: Symbol): nir.Type = {
      val params   = sym.asMethod.paramLists.flatten
      val selfty   = genType(sym.owner.tpe)
      val paramtys = params.map(p => genType(p.tpe))
      val retty    =
        if (sym.isClassConstructor) Type.Unit
        else genType(sym.tpe.resultType)

      Type.Function(selfty +: paramtys, retty)
    }

    def defParamSymbols(dd: DefDef): List[Symbol] = {
      val vp = dd.vparamss
      if (vp.isEmpty) Nil else vp.head.map(_.symbol)
    }

    def genParams(paramSyms: Seq[Symbol]): Seq[Param] = {
      val self = Param(Name.Local("this"), genType(curClassSym.tpe))
      val params = paramSyms.map { sym =>
        val name = genLocalName(sym)
        val ty = genType(sym.tpe)
        val param = Param(name, ty)
        curEnv.enter(sym, Val.Name(name, ty))
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
              curThis := Val.Name(params.head.name, params.head.ty)
            ) {
              genExpr(body, Focus.entry(params))
            }
        }
      }

    def genExpr(tree: Tree, focus: Focus): Focus = tree match {
      case ld: LabelDef =>
        assert(ld.params.length == 0)
        val label = curLabelEnv.enterLabel(ld, curLocalInfo.labelApplyCount(ld.symbol) + 1)
        curLabelEnv.enterLabelCall(ld.symbol, Seq(), focus)
        genLabel(ld)

      case vd: ValDef =>
        // TODO: attribute valdef name to the rhs node
        val rhs = genExpr(vd.rhs, focus)
        val isMutable = curLocalInfo.mutableVars.contains(vd.symbol)
        if (!isMutable) {
          curEnv.enter(vd.symbol, rhs.value)
          rhs.withValue(Val.Unit)
        } else {
          val ty = genType(vd.symbol.tpe)
          val alloca = rhs.withOp(Op.Alloca(ty))
          curEnv.enter(vd.symbol, alloca.value)
          alloca.withOp(Op.Store(ty, alloca.value, rhs.value))
        }

      case If(cond, thenp, elsep) =>
        genIf(cond, thenp, elsep, genType(tree.tpe), focus)

      case Return(exprp) =>
        val expr = genExpr(exprp, focus)
        expr.finish(Op.Ret(expr.value))

      case Try(expr, catches, finalizer) if catches.isEmpty && finalizer.isEmpty =>
        genExpr(expr, focus)

      case Try(expr, catches, finalizer) =>
        genTry(expr, catches, finalizer, focus)

      case Throw(exprp) =>
        val expr = genExpr(exprp, focus)
        expr.finish(Op.Ret(expr.value))

      case app: Apply =>
        genApply(app, focus)

      case app: ApplyDynamic =>
        genApplyDynamic(app, focus)

      case This(qual) =>
        focus.withValue {
          if (tree.symbol == curClassSym.get) curThis.get
          else Val.Name(genClassName(tree.symbol), genType(tree.tpe))
        }

      case Select(qualp, selp) =>
        val sym = tree.symbol
        if (sym.isModule)
          focus.withValue(Val.Name(genClassName(sym), genType(sym.tpe)))
        else if (sym.isStaticMember)
          genStaticMember(sym, focus)
        else if (sym.isMethod) {
          val qual = genExpr(qualp, focus)
          genMethodCall(sym, qual.value, Seq(), qual)
        } else {
          val ty = genType(tree.symbol.tpe)
          val qual = genExpr(qualp, focus)
          val elem = qual.withOp(Op.FieldElem(ty, genFieldName(tree.symbol), qual.value))
          elem withOp Op.Load(ty, elem.value)
        }

      case id: Ident =>
        val sym = id.symbol
        if (!curLocalInfo.mutableVars.contains(sym))
          focus.withValue {
            if (sym.isModule) Val.Name(genClassName(sym), genType(sym.tpe))
            else curEnv.resolve(sym)
          }
        else
          focus.withOp(Op.Load(genType(sym.tpe), curEnv.resolve(sym)))

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
            val elem = rhs.withOp(Op.FieldElem(ty, genFieldName(sel.symbol), qual.value))
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
        undefined(focus)

      case EmptyTree =>
        focus.withValue(Val.Unit)

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
           | StringTag
           | ClazzTag =>
          focus withValue genLiteralValue(lit)
        case EnumTag =>
          genStaticMember(value.symbolValue, focus)
      }
    }

    def genLiteralValue(lit: Literal): Val = {
      val value = lit.value
      value.tag match {
        case NullTag =>
          Val.Null
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
        case ClazzTag =>
          Val.Class(genType(value.typeValue))
      }
    }

    def genStaticMember(sym: Symbol, focus: Focus): Focus = {
      val ty = genType(sym.tpe)
      val module = Val.Name(genClassName(sym.owner), genType(sym.owner.tpe))
      val elem = focus.withOp(Op.FieldElem(ty, genFieldName(sym), module))
      elem.withOp(Op.Load(ty, elem.value))
    }

    def genTry(expr: Tree, catches: List[Tree], finalizer: Tree, focus: Focus) = ???/*{
      val cf          = ir.Try(focus.cf)
      val normal      = genExpr(expr, focus withCf cf)
      val exceptional = genCatch(catches, focus withCf ir.CaseException(cf))

      genFinally(normal ++ exceptional, finalizer)
    }*/

    def genCatch(catches: List[Tree], focus: Focus) = ???/*{
      val exc    = focus.cf
      val switch = ir.Switch(exc, ir.GetClass(ir.Empty, exc))

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
            symopt foreach (curEnv.enter(_, ir.As(exc, excty)))
            genExpr(body, focus withCf ir.CaseConst(switch, excty))
        }
      val default =
        Tails.termn(ir.Throw(ir.CaseDefault(switch), focus.ef, exc))

      Tails.flatten(default +: cases)
    }*/

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

    def genMatch(prologue: List[Tree], lds: List[LabelDef], focus: Focus) = ???/*{
      val prfocus = sequenced(prologue, focus)(genExpr(_, _))
      val lastfocus = prfocus.lastOption.getOrElse(focus)

      curLabelEnv.enterLabel(lds.head, curLocalInfo.labelApplyCount(lds.head.symbol) + 1)
      for (ld <- lds.tail) {
        curLabelEnv.enterLabel(ld, curLocalInfo.labelApplyCount(ld.symbol))
      }
      curLabelEnv.enterLabelCall(lds.head.symbol, Seq(), lastfocus)

      var lasttails = prt
      for (ld <- lds) {
        lasttails = lasttails ++ genLabel(ld)
      }

      lasttails
    }*/

    def genLabel(label: LabelDef) = ??? /*{
      val cf = curLabelEnv.resolveLabel(label.symbol)
      val ef = curLabelEnv.resolveLabelEf(label.symbol)
      genExpr(label.rhs, Focus(cf, ef, Val.Unit()))
    }*/

    def genArrayValue(av: ArrayValue, focus: Focus): Focus = {
      val ArrayValue(tpt, elems) = av
      val ty         = genType(tpt.tpe)
      val len        = elems.length
      val allocfocus = focus withOp Op.AllocArray(ty, Val.I32(len))
      val rfocus     =
        if (elems.isEmpty)
          allocfocus
        else {
          val vfocus    = sequenced(elems, allocfocus)(genExpr(_, _))
          val values    = vfocus.map(_.value)
          val lastfocus = vfocus.lastOption.getOrElse(focus)
          val sfocus    = sequenced(values.zipWithIndex, lastfocus) { (vi, foc) =>
            val (value, i) = vi
            val elem = foc withOp Op.ArrayElem(ty, allocfocus.value, Val.I32(i))
            elem withOp Op.Store(ty, elem.value, value)
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
        genExpr(defaultcase, _),
        normalcases.map { case (v, _) => v },
        normalcases.map { case (_, body) => genExpr(body, _: Focus) })
    }

    def genApplyDynamic(app: ApplyDynamic, focus: Focus) =
      undefined(focus)

    def genApply(app: Apply, focus: Focus): Focus = {
      val Apply(fun, args) = app

      fun match {
        case _: TypeApply =>
          genApplyTypeApply(app, focus)
        case Select(Super(_, _), _) =>
          genApplySuper(app, focus)
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
          } else if (isBuiltin(sym)) {
            genBuiltinApply(app, focus)
          } else {
            genNormalApply(app, focus)
          }
      }
    }

    def genApplyLabel(tree: Tree, focus: Focus) = ???/*notMergeableGuard {
      val Apply(fun, args) = tree
      val label = curLabelEnv.resolveLabel(fun.symbol)
      val argsfocus = sequenced(args, focus)(genExpr(_, _))
      val lastfocus = argsfocus.lastOption.getOrElse(focus)
      curLabelEnv.enterLabelCall(fun.symbol, argsfocus.map(_.value), lastfocus)
      val res = tails
      res
    }*/

    lazy val prim2ty = Map(
      BooleanTpe -> Type.BooleanClass,
      ByteTpe    -> Type.ByteClass,
      CharTpe    -> Type.CharacterClass,
      ShortTpe   -> Type.ShortClass,
      IntTpe     -> Type.IntegerClass,
      LongTpe    -> Type.LongClass,
      FloatTpe   -> Type.FloatClass,
      DoubleTpe  -> Type.DoubleClass
    )

    lazy val boxed2primty = Map[Symbol, nir.Type](
      BoxedBooleanClass   -> Type.BooleanClass,
      BoxedByteClass      -> Type.ByteClass,
      BoxedCharacterClass -> Type.CharacterClass,
      BoxedShortClass     -> Type.ShortClass,
      BoxedIntClass       -> Type.IntegerClass,
      BoxedLongClass      -> Type.LongClass,
      BoxedFloatClass     -> Type.FloatClass,
      BoxedDoubleClass    -> Type.DoubleClass
    )

    lazy val boxed2boxedty = Map[Symbol, nir.Type](
      BoxedBooleanClass   -> Type.BooleanClass,
      BoxedByteClass      -> Type.ByteClass,
      BoxedCharacterClass -> Type.CharacterClass,
      BoxedShortClass     -> Type.ShortClass,
      BoxedIntClass       -> Type.IntegerClass,
      BoxedLongClass      -> Type.LongClass,
      BoxedFloatClass     -> Type.FloatClass,
      BoxedDoubleClass    -> Type.DoubleClass
    )

    def genPrimitiveBox(exprp: Tree, tpe: Type, focus: Focus) = {
      val expr = genExpr(exprp, focus)

      expr.withOp(Op.Box(prim2ty(tpe.widen), expr.value))
    }

    def genPrimitiveUnbox(exprp: Tree, tpe: Type, focus: Focus) = {
      val expr = genExpr(exprp, focus)

      expr.withOp(Op.Unbox(prim2ty(tpe.widen), expr.value))
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

    def genUnaryOp(code: Int, rightp: Tree, retty: nir.Type, focus: Focus) = {
      import scalaPrimitives._
      val right = genExpr(rightp, focus)

      code match {
        case POS  => right
        case NEG  => right.withOp(Op.Bin(Bin.Sub, retty, numOfType(0, retty), right.value))
        case NOT  => right.withOp(Op.Bin(Bin.Xor, retty, numOfType(-1, retty), right.value))
        case ZNOT => right.withOp(Op.Bin(Bin.Xor, retty, Val.True, right.value))
        case _    => abort("Unknown unary operation code: " + code)
      }
    }

    def genBinaryOp(code: Int, left: Tree, right: Tree, retty: nir.Type,
                    focus: Focus): Focus = {
      import scalaPrimitives._

      val lty   = genType(left.tpe)
      val rty   = genType(right.tpe)

      code match {
        case ADD  => genBinaryOp(Op.Bin(Bin.Add,  _, _, _), left, right, retty, focus)
        case SUB  => genBinaryOp(Op.Bin(Bin.Sub,  _, _, _), left, right, retty, focus)
        case MUL  => genBinaryOp(Op.Bin(Bin.Mul,  _, _, _), left, right, retty, focus)
        case DIV  => genBinaryOp(Op.Bin(Bin.Div,  _, _, _), left, right, retty, focus)
        case MOD  => genBinaryOp(Op.Bin(Bin.Mod,  _, _, _), left, right, retty, focus)
        case OR   => genBinaryOp(Op.Bin(Bin.Or,   _, _, _), left, right, retty, focus)
        case XOR  => genBinaryOp(Op.Bin(Bin.Xor,  _, _, _), left, right, retty, focus)
        case AND  => genBinaryOp(Op.Bin(Bin.And,  _, _, _), left, right, retty, focus)
        case LSL  => genBinaryOp(Op.Bin(Bin.Shl,  _, _, _), left, right, retty, focus)
        case LSR  => genBinaryOp(Op.Bin(Bin.Lshr, _, _, _), left, right, retty, focus)
        case ASR  => genBinaryOp(Op.Bin(Bin.Ashr, _, _, _), left, right, retty, focus)

        case LT   => genBinaryOp(Op.Comp(Comp.Lt,  _, _, _), left, right, binaryOperationType(lty, rty), focus)
        case LE   => genBinaryOp(Op.Comp(Comp.Lte, _, _, _), left, right, binaryOperationType(lty, rty), focus)
        case GT   => genBinaryOp(Op.Comp(Comp.Gt,  _, _, _), left, right, binaryOperationType(lty, rty), focus)
        case GE   => genBinaryOp(Op.Comp(Comp.Gte, _, _, _), left, right, binaryOperationType(lty, rty), focus)

        case EQ   => genEqualityOp(left, right, ref = false, negated = false, focus)
        case NE   => genEqualityOp(left, right, ref = false, negated = true,  focus)
        case ID   => genEqualityOp(left, right, ref = true,  negated = false, focus)
        case NI   => genEqualityOp(left, right, ref = true,  negated = true,  focus)

        case ZOR  => genIf(left, Literal(Constant(true)), right, retty, focus)
        case ZAND => genIf(left, right, Literal(Constant(false)), retty, focus)

        case _    => abort("Unknown binary operation code: " + code)
      }
    }

    def genBinaryOp(op: (nir.Type, Val, Val) => Op, leftp: Tree, rightp: Tree, retty: nir.Type,
                    focus: Focus): Focus = {
      val left         = genExpr(leftp, focus)
      val leftcoerced  = genCoercion(left.value, genType(leftp.tpe), retty, left)
      val right        = genExpr(rightp, leftcoerced)
      val rightcoerced = genCoercion(right.value, genType(rightp.tpe), retty, right)

      rightcoerced withOp op(retty, leftcoerced.value, rightcoerced.value)
    }

    def genEqualityOp(leftp: Tree, rightp: Tree,
                      ref: Boolean, negated: Boolean,
                      focus: Focus) = {
      val comp = if (negated) Comp.Neq else Comp.Eq

      genType(leftp.tpe) match {
        case _: nir.Type.ClassKind =>
          val left  = genExpr(leftp, focus)
          val right = genExpr(rightp, left)

          if (ref)
            right withOp Op.Comp(comp, nir.Type.ObjectClass, left.value, right.value)
          else {
            val equals = right withOp Op.Equals(left.value, right.value)
            if (negated)
              equals withOp Op.Bin(Bin.Xor, Type.Bool, Val.True, equals.value)
            else
              equals
          }

        case kind =>
          val lty   = genType(leftp.tpe)
          val rty   = genType(rightp.tpe)
          val retty = binaryOperationType(lty, rty)

          genBinaryOp(Op.Comp(comp, _, _, _), leftp, rightp, retty, focus)
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
        nir.Type.ObjectClass
      case (ty1 , ty2) if ty1 == ty2 =>
        ty1
      case _ =>
        abort(s"can't perform binary opeation between $lty and $rty")
    }

    def genStringConcat(tree: Tree, leftp: Tree, args: List[Tree], focus: Focus) = {
      val List(rightp) = args
      val left = genExpr(leftp, focus)
      val right = genExpr(rightp, left)

      right withOp Op.StringConcat(left.value, right.value)
    }

    // TODO: this doesn't seem to get called on foo.## expressions
    def genHashCode(tree: Tree, receiverp: Tree, focus: Focus) = {
      val recv = genExpr(receiverp, focus)

      recv.withOp(Op.HashCode(recv.value))
    }

    def genArrayOp(app: Apply, code: Int, focus: Focus): Focus = {
      import scalaPrimitives._
      val Apply(Select(array, _), args) = app
      val allfocus = sequenced(array :: args, focus)(genExpr(_, _))
      val lastfocus  = allfocus.last
      def arrayvalue = allfocus(0).value
      def argvalues  = allfocus.tail.map(_.value)
      def elemty     = genType(app.tpe)

      if (scalaPrimitives.isArrayGet(code)) {
        val elem = lastfocus withOp Op.ArrayElem(elemty, arrayvalue, argvalues(0))
        elem withOp Op.Load(elemty, elem.value)
      } else if (scalaPrimitives.isArraySet(code)) {
        val elem = lastfocus withOp Op.ArrayElem(elemty, arrayvalue, argvalues(0))
        elem withOp Op.Store(elemty, elem.value, argvalues(1))
      } else
        lastfocus withOp Op.ArrayLength(arrayvalue)
    }

    def genSynchronized(app: Apply, focus: Focus): Focus = {
      val Apply(Select(receiverp, _), List(argp)) = app
      val rec   = genExpr(receiverp, focus)
      val enter = rec withOp Op.MonitorEnter(rec.value)
      val arg   = genExpr(argp, enter)
      val exit  = arg withOp Op.MonitorExit(rec.value)

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
        case Object_isInstanceOf => Op.IsInstanceOf(rec.value, ty)
        case Object_asInstanceOf => Op.AsInstanceOf(rec.value, ty)
      })
    }

    def genBuiltinApply(app: Tree, focus: Focus): Focus = {
      val Apply(fun @ Select(receiverp, _), argsp) = app

      fun.symbol match {
        case UnboxValue(fromty, toty) =>
          val boxed = genExpr(receiverp, focus)
          val unboxed = boxed withOp Op.Unbox(fromty, boxed.value)
          genCoercion(unboxed.value, fromty.unboxed, toty, unboxed)
        case BoxValue(boxty) =>
          genValueOfApply(boxty, argsp, focus)
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
          right.withOp(Seq(Attr.Usgn), Op.Bin(Bin.Div, ty, left.value, right.value))
        case RemainderUnsigned(ty) =>
          val List(l, r) = argsp
          val left = genExpr(l, focus)
          val right = genExpr(r, left)
          right.withOp(Seq(Attr.Usgn), Op.Bin(Bin.Mod, ty, left.value, right.value))
        case ToUnsigned(fromty, toty) =>
          val List(argp) = argsp
          val unboxed = genExpr(argp, focus)
          unboxed withOp Op.Conv(Conv.Zext, toty, unboxed.value)
        case ToString(nir.Type.ObjectClass) =>
          val obj = genExpr(receiverp, focus)
          obj withOp Op.ToString(obj.value, Val.None)
        case ToString(boxty) =>
          val boxed = genExpr(receiverp, focus)
          val unboxed = boxed withOp Op.Unbox(boxty, boxed.value)
          unboxed withOp Op.ToString(unboxed.value, Val.None)
        case HashCode(nir.Type.ObjectClass) =>
          val obj = genExpr(receiverp, focus)
          obj withOp Op.HashCode(obj.value)
        case HashCode(boxty) =>
          val boxed = genExpr(receiverp, focus)
          val unboxed = boxed withOp Op.Unbox(boxty, boxed.value)
          unboxed withOp Op.HashCode(unboxed.value)
        case ScalaRunTimeHashCode() =>
          val List(argp) = argsp
          val unboxed = genExpr(argp, focus)
          unboxed withOp Op.HashCode(unboxed.value)
        case Equals() =>
          val List(argp) = argsp
          val obj = genExpr(receiverp, focus)
          val arg = genExpr(argp, obj)
          arg withOp Op.Equals(obj.value, arg.value)
        case GetClass() =>
          val obj = genExpr(receiverp, focus)
          obj withOp Op.GetClass(obj.value)
      }
    }

    def genValueToString(ty: nir.Type, argsp: Seq[Tree],
                         unsigned: Boolean, focus: Focus) = {
      val attrs = if (unsigned) Seq(Attr.Usgn) else Seq()

      argsp match {
        case List(valuep) =>
          val value = genExpr(valuep, focus)
          value.withOp(attrs, Op.ToString(value.value, Val.None))
        case List(valuep, radixp) =>
          val value = genExpr(valuep, focus)
          val radix = genExpr(radixp, value)
          radix.withOp(attrs, Op.ToString(value.value, radix.value))
      }
    }

    def genValueFromString(ty: nir.Type, argsp: Seq[Tree],
                           unsigned: Boolean, focus: Focus) = {
      val attrs = if (unsigned) Seq(Attr.Usgn) else Seq()

      argsp match {
        case List(strp) =>
          val str = genExpr(strp, focus)
          str.withOp(attrs, Op.FromString(ty, str.value, Val.None))
        case List(strp, radixp) =>
          val str = genExpr(strp, focus)
          val radix = genExpr(radixp, str)
          radix.withOp(attrs, Op.FromString(ty, str.value, radix.value))
      }
    }

    def genValueOfApply(boxty: nir.Type, argsp: Seq[Tree], focus: Focus) = {
      val converted =
        argsp match {
          case List(s, radix) =>
            genValueFromString(boxty.unboxed, argsp, unsigned = false, focus)
          case List(s) if s.tpe.widen == StringTpe =>
            genValueFromString(boxty.unboxed, argsp, unsigned = false, focus)
          case List(valuep) =>
            genExpr(valuep, focus)
        }
      converted withOp Op.Box(boxty, converted.value)
    }

    def genNormalApply(app: Apply, focus: Focus) = {
      val Apply(fun @ Select(receiverp, _), args) = app
      val rec = genExpr(receiverp, focus)

      genMethodCall(fun.symbol, rec.value, args, rec)
    }

    def genApplySuper(app: Apply, focus: Focus) = {
      val Apply(fun @ Select(sup, _), args) = app

      genMethodCall(fun.symbol, curThis.get, args, focus)
    }

    def genApplyNew(app: Apply, focus: Focus) = {
      val Apply(fun @ Select(New(tpt), nme.CONSTRUCTOR), args) = app
      val ctor = fun.symbol
      val kind = genKind(tpt.tpe)
      val ty   = toIRType(kind)

      kind match {
        case builtin: BuiltinClassKind =>
          genNewBuiltin(builtin, ctor, args, focus)
        case ArrayKind(of) =>
          genNewArray(toIRType(of), args.head, focus)
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
          focus withOp Op.Alloc(Type.ObjectClass)
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
          genValueOfApply(toIRType(builtin), args, focus)
      }

    def genNewArray(elemty: nir.Type, lengthp: Tree, focus: Focus) = {
      val length = genExpr(lengthp, focus)

      length.withOp(Op.AllocArray(elemty, length.value))
    }

    def genNew(sym: Symbol, ctorsym: Symbol, args: List[Tree], focus: Focus) = {
      val alloc = focus.withOp(Op.AllocClass(genType(sym.tpe)))
      val ctor = genMethodCall(ctorsym, alloc.value, args, alloc)

      ctor.withValue(alloc.value)
    }

    def genMethodCall(sym: Symbol, self: Val, argsp: Seq[Tree], focus: Focus): Focus = {
      val args      = sequenced(argsp, focus)(genExpr(_, _))
      val argvalues = args.map(_.value)
      val last      = args.lastOption.getOrElse(focus)

      if (isForeignExternModule(sym.owner))
        genForeignCall(sym, argvalues, last)
      else
        genNormalMethodCall(sym, self, argvalues, last)
    }

    def genForeignCall(sym: Symbol, args: Seq[Val], focus: Focus): Focus = {
      val name = genForeignName(sym)
      val sig  = genDefSig(sym)
      val call = focus withOp Op.Call(sig, Val.Name(name, Type.Ptr(sig)), args)

      call
    }

    def genNormalMethodCall(sym: Symbol, self: Val, args: Seq[Val], focus: Focus): Focus = {
      val name = genDefName(sym)
      val sig  = genDefSig(sym)
      val elem = focus withOp Op.MethodElem(sig, name, self)
      val call = elem withOp Op.Call(sig, elem.value, self +: args)

      call
    }
  }
}


package salty.tools
package compiler

import scala.collection.{mutable => mut}
import scala.tools.nsc._
import scala.tools.nsc.plugins._
import scala.util.{Either, Left, Right}
import salty.ir
import salty.ir.{Type => Ty, Instr => I, Defn => D, Rel => R, Name => N}
import salty.ir.{Focus, Tails}, Focus.sequenced
import salty.util, util.sh, util.ScopedVar.{withScopedVars => scoped}

abstract class GenSaltyCode extends PluginComponent
                               with GenIRFiles
                               with GenTypeKinds
                               with GenNameEncoding {
  import global.{ merge => _, _ }
  import global.definitions._
  import global.treeInfo.hasSynthCaseSymbol

  val phaseName = "saltycode"

  override def newPhase(prev: Phase): StdPhase =
    new SaltyCodePhase(prev)

  def debug[T](msg: String)(v: T): T = { println(s"$msg = $v"); v }

  def unreachable = abort("unreachable")

  def undefined(focus: Focus) =
    Tails(focus withCf I.Undefined(focus.cf, focus.ef))

  class Env {
    val env = mut.Map.empty[Symbol, I.Val]
    def enter(sym: Symbol, node: I.Val): I.Val = {
      env += ((sym, node))
      node
    }
    def resolve(sym: Symbol): I.Val = env(sym)
  }

  class LabelEnv(env: Env) {
    def enterLabel(label: LabelDef): ir.Instr = ???
    def enterLabelCall(sym: Symbol, values: Seq[ir.Instr], from: ir.Instr): Unit = ???
    def resolveLabel(sym: Symbol): ir.Instr = ???
    def resolveLabelParams(sym: Symbol): List[ir.Instr] = ???
  }

  class CollectLocalInfo extends Traverser {
    var mutableVars: Set[Symbol] = Set.empty
    var labels: Set[LabelDef] = Set.empty

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
    val curLabelEnv  = new util.ScopedVar[LabelEnv]
    val curThis      = new util.ScopedVar[I.Val]

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
          val scope = genClass(cd)
          genDOTFile(cunit, sym, scope)
        }
      }
    }

    def genClass(cd: ClassDef): ir.Scope = scoped (
      curClassSym := cd.symbol
    ) {
      val sym     = cd.symbol
      val name    = genClassName(sym)
      val parent  = R.Child(genClassDefn(sym.superClass))
      val ifaces  = genClassInterfaces(sym).map(R.Implements)
      val fields  = genClassFields(sym).toSeq
      val methods = genClassMethods(cd.impl.body)
      val owner   =
        if (sym.isModuleClass)
          name -> D.Module(name, parent +: ifaces)
        else if (sym.isInterface)
          name -> D.Interface(name, ifaces)
        else
          name -> D.Class(name, parent +: ifaces)

      ir.Scope(Map(((owner +: fields) ++ methods): _*))
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol
        if psym.isInterface
      } yield {
        genClassDefn(psym)
      }

    def genClassMethods(stats: List[Tree]): List[(ir.Name, ir.Defn)] =
      stats.flatMap {
        case dd: DefDef => List(genDef(dd))
        case _          => Nil
      }

    def genClassFields(sym: Symbol) = {
      val owner = genClassDefn(sym)
      for {
        f <- sym.info.decls
        if !f.isMethod && f.isTerm && !f.isModule
      } yield {
        val name = genFieldName(f)
        name -> D.Field(name, genType(f.tpe), Seq(R.Belongs(owner)))
      }
    }

    def genDef(dd: DefDef): (ir.Name, ir.Defn) = scoped (
      curMethodSym := dd.symbol
    ) {
      println(s"generating $dd")
      val sym = dd.symbol
      val name = genDefName(sym)
      val paramSyms = defParamSymbols(dd)
      val ty =
        if (dd.symbol.isClassConstructor) Ty.Unit
        else genType(sym.tpe.resultType)
      val rel = Seq(R.Belongs(genClassDefn(sym)))

      if (dd.symbol.isDeferred) {
        val params = genParams(paramSyms, define = false)
        name -> D.Declare(name, ty, params, rel)
      } else {
        val env = new Env
        scoped (
          curEnv := env,
          curLabelEnv := new LabelEnv(env),
          curLocalInfo := (new CollectLocalInfo).collect(dd.rhs)
        ) {
          val params = genParams(paramSyms, define = true)
          val body = genDefBody(dd.rhs, params)
          name -> D.Define(name, ty, params, body, rel)
        }
      }
    }

    def defParamSymbols(dd: DefDef): List[Symbol] = {
      val vp = dd.vparamss
      if (vp.isEmpty) Nil else vp.head.map(_.symbol)
    }

    def genParams(paramSyms: List[Symbol], define: Boolean): Seq[I.In] = {
      val self = I.In(Ty.Of(genClassDefn(curClassSym)))
      val params = paramSyms.map { sym =>
        val node = I.In(genType(sym.tpe))
        if (define)
          curEnv.enter(sym, node)
        node
      }

      self +: params
    }

    def genDefBody(body: Tree, params: Seq[I.In]) =
      scoped (
        curThis := params.head
      ) {
        val tails =
          try genExpr(body, Focus.start())
          catch {
            case Tails.NotMergeable(tails) => tails
          }
        tails.end
      }

    /* (body match {
      case Block(List(ValDef(_, nme.THIS, _, _)),
                 label @ LabelDef(name, Ident(nme.THIS) :: _, rhs)) =>
        val entry = B(Tn.Undefined)
        val values = paramValues.take(label.params.length)
        curLabelEnv.enterLabel(label)
        curLabelEnv.enterLabelCall(label.symbol, values, entry)
        val block =
          scoped (
            curThis := curLabelEnv.resolveLabelParams(label.symbol).head
          ) {
            genLabel(label)
          }
        entry.termn = Tn.Jump(block)
        entry
      case _ =>
        scoped (
          curThis := paramValues.head
        ) {
          genExpr(body)
        }
    }).simplify.verify*/

    def genExpr(tree: Tree, focus: Focus): Tails = tree match {
      case label: LabelDef =>
        ???
        /*
        curLabelEnv.enterLabel(label)
        genLabel(label)
        */

      case vd: ValDef =>
        val (rfocus, rt) = genExpr(vd.rhs, focus).merge
        val isMutable = curLocalInfo.mutableVars.contains(vd.symbol)
        val vdfocus =
          if (!isMutable) {
            curEnv.enter(vd.symbol, rfocus.value)
            rfocus withValue I.Unit
          } else {
            val alloc = I.Alloc(genType(vd.symbol.tpe))
            curEnv.enter(vd.symbol, alloc)
            rfocus mapEf (I.Store(_, alloc, rfocus.value))
          }
        vdfocus +: rt

      case If(cond, thenp, elsep) =>
        genIf(cond, thenp, elsep, focus)

      case Return(expr) =>
        val (efocus, etails) = genExpr(expr, focus).merge
        (efocus withCf I.Return(efocus.cf, efocus.ef, efocus.value)) +: etails

      case Try(expr, catches, finalizer) if catches.isEmpty && finalizer.isEmpty =>
        genExpr(expr, focus)

      case Try(expr, catches, finalizer) =>
        genTry(expr, catches, finalizer, focus)

      case Throw(expr) =>
        val (efocus, etails) = genExpr(expr, focus).merge
        (efocus withCf I.Throw(efocus.cf, efocus.ef, efocus.value)) +: etails

      case app: Apply =>
        genApply(app, focus)

      case app: ApplyDynamic =>
        genApplyDynamic(app, focus)

      case This(qual) =>
        Tails(focus withValue {
          if (tree.symbol == curClassSym.get) curThis
          else I.ValueOf(genClassDefn(tree.symbol))
        })

      case Select(qual, sel) =>
        val sym = tree.symbol
        if (sym.isModule)
          Tails(focus withValue I.ValueOf(genClassDefn(sym)))
        else if (sym.isStaticMember)
          Tails(focus withValue genStaticMember(sym))
        else {
          val (qfocus, qt) = genExpr(qual, focus).merge
          val elem = I.Elem(qfocus.value, I.ValueOf(genFieldDefn(tree.symbol)))
          (qfocus mapEf (I.Load(_, elem))) +: qt
        }

      case id: Ident =>
        val sym = id.symbol
        Tails {
          if (!curLocalInfo.mutableVars.contains(sym))
            focus withValue {
              if (sym.isModule) I.ValueOf(genClassDefn(sym))
              else curEnv.resolve(sym)
            }
          else
            focus mapEf (I.Load(_, curEnv.resolve(sym)))
        }

      case lit: Literal =>
        Tails(focus withValue genValue(lit))

      case block: Block =>
        genBlock(block, focus)

      case Typed(Super(_, _), _) =>
        Tails(focus withValue curThis)

      case Typed(expr, _) =>
        genExpr(expr, focus)

      case Assign(lhs, rhs) =>
        lhs match {
          case sel @ Select(qual, _) =>
            val (qfocus, qt) = genExpr(qual, focus).merge
            val (rfocus, rt) = genExpr(rhs, qfocus).merge
            val elem = I.Elem(qfocus.value, I.ValueOf(genFieldDefn(sel.symbol)))
            (rfocus mapEf (I.Store(_, elem, rfocus.value))) +: (qt ++ rt)

          case id: Ident =>
            val (rfocus, rt) = genExpr(rhs, focus).merge
            (rfocus mapEf (I.Store(_, curEnv.resolve(id.symbol), rfocus.value))) +: rt
        }

      case av: ArrayValue =>
        genArrayValue(av, focus)

      case m: Match =>
        genSwitch(m, focus)

      case fun: Function =>
        undefined(focus)

      case EmptyTree =>
        Tails(focus withValue I.Unit)

      case _ =>
        abort("Unexpected tree in genExpr: " +
              tree + "/" + tree.getClass + " at: " + tree.pos)
    }

    def genValue(lit: Literal): I.Val = {
      val value = lit.value
      value.tag match {
        case NullTag =>
          I.Null
        case UnitTag =>
          I.Unit
        case BooleanTag =>
          if (value.booleanValue) I.True else I.False
        case ByteTag =>
          I.I8(value.intValue.toByte)
        case ShortTag | CharTag =>
          I.I16(value.intValue.toShort)
        case IntTag =>
          I.I32(value.intValue)
        case LongTag =>
          I.I64(value.longValue)
        case FloatTag =>
          I.F32(value.floatValue)
        case DoubleTag =>
          I.F64(value.doubleValue)
        case StringTag =>
          I.Str(value.stringValue)
        case ClazzTag =>
          I.Class(genType(value.typeValue))
        case EnumTag =>
          genStaticMember(value.symbolValue)
      }
    }

    def genStaticMember(sym: Symbol) =
      I.ValueOf(genFieldDefn(sym))

    def genTry(expr: Tree, catches: List[Tree], finalizer: Tree, focus: Focus) = {
      val cf          = I.Try(focus.cf)
      val normal      = genExpr(expr, focus withCf cf)
      val exceptional = genCatch(catches, finalizer, focus withCf I.CaseException(cf))

      normal ++ exceptional
    }

    // TODO: finally
    def genCatch(catches: List[Tree], finalizer: Tree, focus: Focus) = {
      assert(finalizer.isEmpty, ???)
      val exc    = I.ExceptionOf(focus.cf)
      val switch = I.Switch(focus.cf, I.TagOf(exc))

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
            symopt foreach (curEnv.enter(_, I.Cast(exc, excty)))
            genExpr(body, focus withCf I.CaseConst(switch, I.Tag(excty)))
        }

      Tails.flatten(cases)
    }

    def genBlock(block: Block, focus: Focus) = {
      val Block(stats, last) = block

      /*
      def isCaseLabelDef(tree: Tree) =
        tree.isInstanceOf[LabelDef] && hasSynthCaseSymbol(tree)

      def translateMatch(last: LabelDef) = {
        val (prologue, cases) = stats.span(s => !isCaseLabelDef(s))
        val labels = cases.map { case label: LabelDef => label }
        genMatch(prologue, labels, last)
      }
      */

      last match {
        /*
        case label: LabelDef if isCaseLabelDef(label) =>
          translateMatch(label)

        case Apply(TypeApply(Select(label: LabelDef, nme.asInstanceOf_Ob), _), _)
            if isCaseLabelDef(label) =>
          translateMatch(label)
        */

        case _ =>
          val (focs, tails) = sequenced(stats, focus)(genExpr(_, _))
          val lastfocus = focs.lastOption.getOrElse(focus)
          genExpr(last, lastfocus) ++ tails
      }
    }

    def genMatch(prologue: List[Tree], cases: List[LabelDef], last: LabelDef) = ??? /*{
      prologue.map(genExpr).chain { _ =>
        curLabelEnv.enterLabel(last)
        for (label <- cases) {
          curLabelEnv.enterLabel(label)
        }
        genLabel(last)
        cases.map(genLabel).head
      }
    }*/

    def genLabel(label: LabelDef) = ??? /*{
      val entry = curLabelEnv.resolveLabel(label.symbol)
      val target = genExpr(label.rhs)
      entry.termn = Tn.Jump(target)
      entry
    }*/

    def genArrayValue(av: ArrayValue, focus: Focus): Tails = {
      val ArrayValue(tpt, elems) = av
      val ty           = genType(tpt.tpe)
      val len          = elems.length
      val salloc       = I.Salloc(ty, I.I32(len))
      val (rfocus, rt) =
        if (elems.isEmpty)
          (focus, Tails.Empty)
        else {
          val (vfocus, vt) = sequenced(elems, focus)(genExpr(_, _))
          val values       = vfocus.map(_.value)
          val lastfocus    = vfocus.lastOption.getOrElse(focus)
          val (sfocus, st) = sequenced(values.zipWithIndex, lastfocus) { (vi, foc) =>
            val (value, i) = vi
            Tails(foc withEf I.Store(foc.ef, I.Elem(salloc, I.I32(i)), value))
          }
          (sfocus.last, vt ++ st)
        }

      (rfocus withValue salloc) +: rt
    }

    def genIf(cond: Tree, thenp: Tree, elsep: Tree, focus: Focus) = {
      val (condfocus, condt) = genExpr(cond, focus).merge
      val cf = I.If(condfocus.cf, condfocus.value)
      condt ++
      genExpr(thenp, condfocus withCf I.CaseTrue(cf)) ++
      genExpr(elsep, condfocus withCf I.CaseFalse(cf))
    }

    def genSwitch(m: Match, focus: Focus): Tails = {
      val Match(sel, cases) = m

      val (selfocus, selt) = genExpr(sel, focus).merge
      val switch = I.Switch(selfocus.cf, selfocus.value)

      val defaultBody =
        cases.collectFirst {
          case c @ CaseDef(Ident(nme.WILDCARD), _, body) => body
        }.get
      val defaultTails =
        genExpr(defaultBody, selfocus withCf I.CaseDefault(switch))
      val branchTails: Seq[Tails] =
        cases.map {
          case CaseDef(Ident(nme.WILDCARD), _, _) =>
            Tails.Empty
          case CaseDef(pat, guard, body) =>
            assert(guard.isEmpty)
            val consts =
              pat match {
                case lit: Literal =>
                  val __ @ (const: I.Const) = genValue(lit)
                  List(const)
                case Alternative(alts) =>
                  alts.map {
                    case lit: Literal =>
                      val __ @ (const: I.Const) = genValue(lit)
                      const
                  }
                case _ =>
                  Nil
              }
            val cf = consts match {
              case const :: Nil => I.CaseConst(switch, consts.head)
              case _            => I.Merge(consts.map(I.CaseConst(switch, _)))
            }
            genExpr(body, selfocus withCf cf)
        }

      Tails.flatten(defaultTails +: branchTails)
    }

    def genApplyDynamic(app: ApplyDynamic, focus: Focus) =
      undefined(focus)

    def genApply(app: Apply, focus: Focus): Tails = {
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
            genLabelApply(app, focus)
          } else if (scalaPrimitives.isPrimitive(sym)) {
            genPrimitiveOp(app, focus)
          } else if (currentRun.runDefinitions.isBox(sym)) {
            val arg = args.head
            genPrimitiveBox(arg, arg.tpe, focus)
          } else if (currentRun.runDefinitions.isUnbox(sym)) {
            genPrimitiveUnbox(args.head, app.tpe, focus)
          } else {
            genNormalApply(app, focus)
          }
      }
    }

    def genLabelApply(tree: Tree, focus: Focus) = ??? /*tree match {
      case Apply(fun, Nil) =>
        curLabelEnv.resolveLabel(fun.symbol)
      case Apply(fun, args) =>
        args.map(genExpr).chain { vals =>
          val block = B(Tn.Jump(curLabelEnv.resolveLabel(fun.symbol)))
          curLabelEnv.enterLabelCall(fun.symbol, vals, block)
          block
        }
    }*/

    lazy val primitive2box = Map(
      BooleanTpe -> Ty.Of(D.Extern(N.Global("java.lang.Boolean"))),
      ByteTpe    -> Ty.Of(D.Extern(N.Global("java.lang.Byte"))),
      CharTpe    -> Ty.Of(D.Extern(N.Global("java.lang.Character"))),
      ShortTpe   -> Ty.Of(D.Extern(N.Global("java.lang.Short"))),
      IntTpe     -> Ty.Of(D.Extern(N.Global("java.lang.Integer"))),
      LongTpe    -> Ty.Of(D.Extern(N.Global("java.lang.Long"))),
      FloatTpe   -> Ty.Of(D.Extern(N.Global("java.lang.Float"))),
      DoubleTpe  -> Ty.Of(D.Extern(N.Global("java.lang.Double")))
    )

    lazy val ctorName = N.Global(nme.CONSTRUCTOR.toString)

    def genPrimitiveBox(expr: Tree, tpe: Type, focus: Focus) = {
      val (efocus, et) = genExpr(expr, focus).merge
      val box = I.Box(efocus.value, Ty.Ref(primitive2box(tpe.widen)))

      (efocus withValue box) +: et
    }

    def genPrimitiveUnbox(expr: Tree, tpe: Type, focus: Focus) = {
      val (efocus, et) = genExpr(expr, focus).merge
      val unbox  = I.Unbox(efocus.value, primitive2box(tpe.widen))

      (efocus withValue unbox) +: et
    }

    def genPrimitiveOp(app: Apply, focus: Focus): Tails = {
      import scalaPrimitives._

      val sym = app.symbol
      val Apply(fun @ Select(receiver, _), args) = app
      val code = scalaPrimitives.getPrimitive(sym, receiver.tpe)

      if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code))
        genSimpleOp(app, receiver :: args, code, focus)
      else if (code == CONCAT)
        genStringConcat(app, receiver, args, focus)
      else if (code == HASH)
        genHash(app, receiver, focus)
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

    def numOfType(num: Int, ty: ir.Type) = ty match {
      case Ty.I8  => I.I8 (num.toByte)
      case Ty.I16 => I.I16(num.toShort)
      case Ty.I32 => I.I32(num)
      case Ty.I64 => I.I64(num.toLong)
      case Ty.F32 => I.F32(num.toFloat)
      case Ty.F64 => I.F64(num.toDouble)
      case _      => unreachable
    }

    def genSimpleOp(app: Apply, args: List[Tree], code: Int, focus: Focus) = {
      val retty = genType(app.tpe)

      args match {
        case List(right)       => genUnaryOp(code, right, retty, focus)
        case List(left, right) => genBinaryOp(code, left, right, retty, focus)
        case _                 => abort("Too many arguments for primitive function: " + app)
      }
    }

    def genUnaryOp(code: Int, right: Tree, retty: ir.Type, focus: Focus) = {
      import scalaPrimitives._

      val (rfocus, rt) = genExpr(right, focus).merge
      val resfocus =
        code match {
          case POS  => rfocus
          case NEG  => rfocus mapValue (v => I.Sub(numOfType(0, retty), v))
          case NOT  => rfocus mapValue (v => I.Xor(numOfType(-1, retty), v))
          case ZNOT => rfocus mapValue (v => I.Xor(I.True, v))
          case _    => abort("Unknown unary operation code: " + code)
        }

      rfocus +: rt
    }

    def genBinaryOp(code: Int, left: Tree, right: Tree, retty: ir.Type,
                    focus: Focus): Tails = {
      import scalaPrimitives._

      val lty   = genType(left.tpe)
      val rty   = genType(right.tpe)

      code match {
        case ADD  => genBinaryOp(I.Add,  left, right, retty, focus)
        case SUB  => genBinaryOp(I.Sub,  left, right, retty, focus)
        case MUL  => genBinaryOp(I.Mul,  left, right, retty, focus)
        case DIV  => genBinaryOp(I.Div,  left, right, retty, focus)
        case MOD  => genBinaryOp(I.Mod,  left, right, retty, focus)
        case OR   => genBinaryOp(I.Or,   left, right, retty, focus)
        case XOR  => genBinaryOp(I.Xor,  left, right, retty, focus)
        case AND  => genBinaryOp(I.And,  left, right, retty, focus)
        case LSL  => genBinaryOp(I.Shl,  left, right, retty, focus)
        case LSR  => genBinaryOp(I.Lshr, left, right, retty, focus)
        case ASR  => genBinaryOp(I.Ashr, left, right, retty, focus)

        case LT   => genBinaryOp(I.Lt,  left, right, binaryOperationType(lty, rty), focus)
        case LE   => genBinaryOp(I.Lte, left, right, binaryOperationType(lty, rty), focus)
        case GT   => genBinaryOp(I.Gt,  left, right, binaryOperationType(lty, rty), focus)
        case GE   => genBinaryOp(I.Gte, left, right, binaryOperationType(lty, rty), focus)

        case EQ   => genEqualityOp(left, right, ref = false, negated = false, focus)
        case NE   => genEqualityOp(left, right, ref = false, negated = true,  focus)
        case ID   => genEqualityOp(left, right, ref = true,  negated = false, focus)
        case NI   => genEqualityOp(left, right, ref = true,  negated = true,  focus)

        case ZOR  => genIf(left, Literal(Constant(true)), right, focus)
        case ZAND => genIf(left, right, Literal(Constant(false)), focus)

        case _    => abort("Unknown binary operation code: " + code)
      }
    }

    def genBinaryOp(op: (I.Val, I.Val) => I.Val, left: Tree, right: Tree, retty: ir.Type,
                    focus: Focus): Tails = {
      val (lfocus, lt) = genExpr(left, focus).merge
      val lcoerced     = genCoercion(lfocus.value, genType(left.tpe), retty)
      val (rfocus, rt) = genExpr(right, lfocus).merge
      val rcoerced     = genCoercion(rfocus.value, genType(right.tpe), retty)

      (rfocus withValue op(lcoerced, rcoerced)) +: (lt ++ rt)
    }

    def genEqualityOp(left: Tree, right: Tree, ref: Boolean, negated: Boolean,
                      focus: Focus) = {
      val eq = if (negated) I.Neq else I.Eq

      genKind(left.tpe) match {
        case ClassKind(_) | BottomKind(NullClass) =>
          val (lfocus, lt) = genExpr(left, focus).merge
          val (rfocus, rt) = genExpr(right, lfocus).merge
          val resfocus =
            if (ref)
              rfocus withValue eq(lfocus.value, rfocus.value)
            else if (lfocus.value eq I.Null)
              rfocus withValue eq(rfocus.value, I.Null)
            else if (rfocus.value eq I.Null)
              rfocus withValue eq(lfocus.value, I.Null)
            else {
              val equals = I.Equals(rfocus.ef, lfocus.value, rfocus.value)
              val value = if (!negated) equals else I.Xor(I.True, equals)
              rfocus withEf equals withValue value
            }
          resfocus +: (lt ++ rt)

        case kind =>
          val lty = genType(left.tpe)
          val rty = genType(right.tpe)
          val retty = binaryOperationType(lty, rty)
          genBinaryOp(eq, left, right, retty, focus)
      }
    }

    def binaryOperationType(lty: ir.Type, rty: ir.Type) = (lty, rty) match {
      case (Ty.I(lwidth), Ty.I(rwidth)) =>
        if (lwidth >= rwidth) lty else rty
      case (Ty.I(_), Ty.F(_)) =>
        rty
      case (Ty.F(_), Ty.I(_)) =>
        lty
      case (Ty.F(lwidth), Ty.F(rwidth)) =>
        if (lwidth >= rwidth) lty else rty
      case (ty1 , ty2) if ty1 == ty2 =>
        ty1
      case (Ty.Null, _) =>
        rty
      case (_, Ty.Null) =>
        lty
      case _ =>
        abort(s"can't perform binary opeation between $lty and $rty")
    }

    def genStringConcat(tree: Tree, left: Tree, args: List[Tree], focus: Focus) = {
      val List(right) = args
      val (lfocus, lt) = genExpr(left, focus).merge
      val (rfocus, rt) = genExpr(right, lfocus).merge

      (rfocus withValue I.Add(lfocus.value, rfocus.value)) +: (lt ++ rt)
    }

    def genHash(tree: Tree, receiver: Tree, focus: Focus) = {
      val method = getMember(ScalaRunTimeModule, nme.hash_)
      val (recfocus, rt) = genExpr(receiver, focus).merge

      genMethodCall(method, recfocus.value, Nil, recfocus) ++ rt
    }

    def genArrayOp(app: Apply, code: Int, focus: Focus): Tails = {
      import scalaPrimitives._

      val Apply(Select(array, _), args) = app
      val (allfocus, allt) = sequenced(array :: args, focus)(genExpr(_, _))
      val lastfocus  = allfocus.last
      def arrayvalue = allfocus(0).value
      def argvalues  = allfocus.tail.map(_.value)
      val rfocus =
        if (scalaPrimitives.isArrayGet(code))
          lastfocus mapEf (I.Load(_, I.Elem(arrayvalue, argvalues(0))))
        else if (scalaPrimitives.isArraySet(code))
          lastfocus mapEf (I.Store(_, I.Elem(arrayvalue, argvalues(0)), argvalues(1)))
        else
          lastfocus withValue I.Length(arrayvalue)

      rfocus +: allt
    }

    // TODO: re-evaluate dropping sychcronized
    // TODO: NPE
    def genSynchronized(app: Apply, focus: Focus): Tails = {
      val Apply(Select(receiver, _), List(arg)) = app
      val (recfocus, rt) = genExpr(receiver, focus).merge
      val (argfocus, at) = genExpr(arg, recfocus).merge

      argfocus +: (rt ++ at)
    }

    def genCoercion(app: Apply, receiver: Tree, code: Int, focus: Focus): Tails = {
      val (rfocus, rt) = genExpr(receiver, focus).merge
      val (fromty, toty) = coercionTypes(code)

      (rfocus mapValue (genCoercion(_, fromty, toty))) +: rt
    }

    def genCoercion(value: I.Val, fromty: ir.Type, toty: ir.Type): I.Val =
      if (fromty == toty)
        value
      else {
        val op = (fromty, toty) match {
          case (Ty.I(lwidth), Ty.I(rwidth))
            if lwidth < rwidth      => I.Zext
          case (Ty.I(lwidth), Ty.I(rwidth))
            if lwidth > rwidth      => I.Trunc
          case (Ty.I(_), Ty.F(_))   => I.Sitofp
          case (Ty.F(_), Ty.I(_))   => I.Fptosi
          case (Ty.F64, Ty.F32)     => I.Fptrunc
          case (Ty.F32, Ty.F64)     => I.Fpext
          case (Ty.Null, _)         => I.Cast
        }
        op(value, toty)
      }

    def coercionTypes(code: Int): (ir.Type, ir.Type) = {
      import scalaPrimitives._

      code match {
        case B2B       => (Ty.I8, Ty.I8)
        case B2S | B2C => (Ty.I8, Ty.I16)
        case B2I       => (Ty.I8, Ty.I32)
        case B2L       => (Ty.I8, Ty.I64)
        case B2F       => (Ty.I8, Ty.F32)
        case B2D       => (Ty.I8, Ty.F64)

        case S2B       | C2B       => (Ty.I16, Ty.I8)
        case S2S | S2C | C2S | C2C => (Ty.I16, Ty.I16)
        case S2I       | C2I       => (Ty.I16, Ty.I32)
        case S2L       | C2L       => (Ty.I16, Ty.I64)
        case S2F       | C2F       => (Ty.I16, Ty.F32)
        case S2D       | C2D       => (Ty.I16, Ty.F64)

        case I2B       => (Ty.I32, Ty.I8)
        case I2S | I2C => (Ty.I32, Ty.I16)
        case I2I       => (Ty.I32, Ty.I32)
        case I2L       => (Ty.I32, Ty.I64)
        case I2F       => (Ty.I32, Ty.F32)
        case I2D       => (Ty.I32, Ty.F64)

        case L2B       => (Ty.I64, Ty.I8)
        case L2S | L2C => (Ty.I64, Ty.I16)
        case L2I       => (Ty.I64, Ty.I32)
        case L2L       => (Ty.I64, Ty.I64)
        case L2F       => (Ty.I64, Ty.F32)
        case L2D       => (Ty.I64, Ty.F64)

        case F2B       => (Ty.F32, Ty.I8)
        case F2S | F2C => (Ty.F32, Ty.I16)
        case F2I       => (Ty.F32, Ty.I32)
        case F2L       => (Ty.F32, Ty.I64)
        case F2F       => (Ty.F32, Ty.F32)
        case F2D       => (Ty.F32, Ty.F64)

        case D2B       => (Ty.F64, Ty.I8)
        case D2S | D2C => (Ty.F64, Ty.I16)
        case D2I       => (Ty.F64, Ty.I32)
        case D2L       => (Ty.F64, Ty.I64)
        case D2F       => (Ty.F64, Ty.F32)
        case D2D       => (Ty.F64, Ty.F64)
      }
    }

    def genApplyTypeApply(app: Apply, focus: Focus) = {
      val Apply(TypeApply(fun @ Select(receiver, _), targs), _) = app
      val ty = genType(targs.head.tpe)
      val (rfocus, rt) = genExpr(receiver, focus).merge
      val value = fun.symbol match {
        case Object_isInstanceOf => I.Is(rfocus.value, ty)
        case Object_asInstanceOf => I.Cast(rfocus.value, ty)
      }

      (rfocus withValue value) +: rt
    }

    def genNormalApply(app: Apply, focus: Focus) = {
      val Apply(fun @ Select(receiver, _), args) = app
      val (rfocus, rt) = genExpr(receiver, focus).merge

      genMethodCall(fun.symbol, rfocus.value, args, rfocus) ++ rt
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
        case _: ArrayKind =>
          genNewArray(ty, args.head, focus)
        case ckind: ClassKind =>
          genNew(ckind.sym, ctor, args, focus)
        case ty =>
          abort("unexpected new: " + app + "\ngen type: " + ty)
      }
    }

    def genNewArray(ty: ir.Type, length: Tree, focus: Focus) = {
      val Ty.Slice(elemty) = ty
      val (lfocus, lt) = genExpr(length, focus).merge

      (lfocus withValue I.Salloc(elemty, lfocus.value)) +: lt
    }

    def genNew(sym: Symbol, ctorsym: Symbol, args: List[Tree], focus: Focus) = {
      val stat  = genClassDefn(sym)
      val alloc = I.Alloc(Ty.Of(stat))

      genMethodCall(ctorsym, alloc, args, focus)
    }

    def genMethodCall(sym: Symbol, self: I.Val, args: Seq[Tree], focus: Focus): Tails = {
      val (argfocus, argt) = sequenced(args, focus)(genExpr(_, _))
      val argvalues        = argfocus.map(_.value)
      val lastfocus        = argfocus.lastOption.getOrElse(focus)
      val stat             = genDefDefn(sym)
      val call             = I.Call(lastfocus.ef, I.ValueOf(stat), self +: argvalues)

      (lastfocus withEf call withValue call) +: argt
    }

  }
}


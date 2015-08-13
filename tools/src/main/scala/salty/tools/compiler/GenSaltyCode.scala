package salty.tools
package compiler

import scala.tools.nsc._
import scala.tools.nsc.plugins._
import salty.ir
import salty.ir.{Expr => E, Type => T, Instr => I,
                 Val => V, Name => N, Stat => S}
import salty.util.ScopedVar, ScopedVar.withScopedVars

abstract class GenSaltyCode extends PluginComponent {
  import global._
  import global.definitions._

  def noimpl = N.Global("not_implemented")

  val phaseName = "saltycode"

  override def newPhase(prev: Phase): StdPhase =
    new SaltyCodePhase(prev)

  class Env {
    private var freshCounter: Int = 0
    private var used: Set[N] = Set.empty[N]
    private var subst: Map[Symbol, N] = Map.empty[Symbol, N]

    private def freshId(): Int = {
      val res = freshCounter
      freshCounter += 1
      res
    }

    def enter(sym: Symbol): N = {
      val name = N.Local(sym.name + "_" + freshId())
      subst += sym -> name
      name
    }

    def resolve(sym: Symbol): N = subst(sym)

    def fresh(): N = N.Local(freshId().toString)
  }

  class SaltyCodePhase(prev: Phase) extends StdPhase(prev) {
    val currentClassSym = new ScopedVar[Symbol]
    val currentMethodSym = new ScopedVar[Symbol]
    val currentEnv = new ScopedVar[Env]

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

      val irClasses = classDefs.flatMap { cd =>
        val sym = cd.symbol
        if (isPrimitiveValueClass(sym) || (sym == ArrayClass)) Nil
        else List(genClass(cd))
      }

      println("Input:")
      classDefs.foreach(c => println(c.toString))
      println("\nOutput:")
      irClasses.foreach(c => println(c.show.build))
    }

    def genClass(cd: ClassDef): ir.Stat = withScopedVars (
      currentClassSym := cd.symbol
    ) {
      val ClassDef(mods, name, _, impl) = cd
      val sym = cd.symbol
      val irName = encodeClassName(sym)
      val irParent = encodeClassName(sym.superClass)
      val irInterfaces = genClassInterfaces(sym)
      val irBody = impl.body.flatMap(genClassStat(_))

      if (sym.isModuleClass)
        S.Module(irName, irParent, irInterfaces, irBody)
      else if (sym.isInterface)
        S.Interface(irName, irInterfaces, irBody)
      else
        S.Class(irName, irParent, irInterfaces, irBody)
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol
        if psym.isInterface
      } yield {
        encodeClassName(psym)
      }

    def genClassStat(stat: Tree): Seq[ir.Stat] = stat match {
      case vd: ValDef => Seq(genField(vd))
      case dd: DefDef => Seq(genMethod(dd))
      case _          => Seq()
    }

    def genField(vd: ValDef): ir.Stat = {
      val name = encodeFieldName(vd.symbol)
      val ty = genType(vd.tpt.tpe)
      val init = genDefault(ty)

      S.Var(name, ty, init)
    }

    def genMethod(dd: DefDef): ir.Stat = withScopedVars (
      currentMethodSym := dd.symbol
    ) {
      val sym = dd.symbol
      val name = encodeMethodName(sym)
      val paramSyms = methodParamSymbols(dd)
      val ty =
        if (dd.symbol.isClassConstructor) T.Unit
        else genType(sym.tpe.resultType)

      if (dd.symbol.isDeferred) {
        val params = genDeclParams(paramSyms)
        S.Decl(name, params, ty)
      } else {
        withScopedVars (
          currentEnv := new Env
        ) {
          val params = genDefParams(paramSyms)
          val body = genExpr(dd.rhs)
          S.Def(name, params, ty, body)
        }
      }
    }

    def methodParamSymbols(dd: DefDef): List[Symbol] = {
      val vp = dd.vparamss
      if (vp.isEmpty) Nil else vp.head.map(_.symbol)
    }

    def genDeclParams(paramSyms: List[Symbol]): Seq[ir.Type] =
      paramSyms.map(sym => genType(sym.tpe))

    def genDefParams(paramSyms: List[Symbol]): Seq[ir.LabeledType] =
      paramSyms.map { sym =>
        val name = currentEnv.enter(sym)
        val ty = genType(sym.tpe)
        ir.LabeledType(name, ty)
      }

    def genExpr(tree: Tree): ir.Expr = tree match {
      case If(cond, thenp, elsep) =>
        val E.Block(instrs, value) = genExpr(cond)
        val res = currentEnv.fresh()
        val instr =
          I.Assign(res, E.If(value, genExpr(thenp), genExpr(elsep)))
        mkBlock(instrs :+ instr, res)

      case r: Return =>
        noimpl

      case t: Try =>
        noimpl

      case t: Throw =>
        noimpl

      case app: Apply =>
        genApply(app)

      case app: ApplyDynamic =>
        genApplyDynamic(app)

      case This(qual) =>
        if (tree.symbol == currentClassSym.get) V.This
        else encodeClassName(tree.symbol)

      case Select(qual, sel) =>
        noimpl

      case id: Ident =>
        val sym = id.symbol
        if (sym.isModule) encodeClassName(sym)
        else currentEnv.resolve(sym)

      case Literal(value) =>
        genLiteral(value)

      case block: Block =>
        genBlock(block)

      case Typed(Super(_, _), _) =>
        V.This

      case Typed(expr, _) =>
        genExpr(expr)

      case Assign(l, r) =>
        noimpl

      case av: ArrayValue =>
        noimpl

      case m: Match =>
        genSwitch(m)

      case fun: Function =>
        noimpl

      case EmptyTree =>
        noimpl

      case _ =>
        noimpl
    }

    def genBlock(block: Block) = noimpl

    def genSwitch(m: Match) = noimpl

    def genLiteral(value: Constant) = value.tag match {
      case NullTag =>
        V.Null
      case UnitTag =>
        V.Unit
      case BooleanTag =>
        V.Bool(value.booleanValue)
      case ByteTag =>
        V.Number(value.intValue.toString, T.I8)
      case ShortTag | CharTag =>
        V.Number(value.intValue.toString, T.I16)
      case IntTag =>
        V.Number(value.intValue.toString, T.I32)
      case LongTag =>
        V.Number(value.longValue.toString, T.I64)
      case FloatTag =>
        V.Number(value.floatValue.toString, T.F32)
      case DoubleTag =>
        V.Number(value.doubleValue.toString, T.F64)
      case StringTag =>
        ???
      case ClazzTag =>
        ???
      case EnumTag =>
        ???
    }

    def genApplyDynamic(app: ApplyDynamic) = noimpl

    def genApply(app: Apply) = {
      val Apply(fun, args) = app

      fun match {
        case _: TypeApply =>
          genApplyTypeApply(app)
        case Select(Super(_, _), _) =>
          genApplySuper(app)
        case Select(New(_), nme.CONSTRUCTOR) =>
          genApplyNew(app)
        case _ =>
          val sym = fun.symbol

          if (sym.isLabel) {
            genLabelApply(app)
          } else if (scalaPrimitives.isPrimitive(sym)) {
            genPrimitiveOp(app)
          } else if (currentRun.runDefinitions.isBox(sym)) {
            val arg = args.head
            makePrimitiveBox(genExpr(arg), arg.tpe)
          } else if (currentRun.runDefinitions.isUnbox(sym)) {
            val arg = args.head
            makePrimitiveUnbox(genExpr(arg), app.tpe)
          } else {
            genNormalApply(app)
          }
      }
    }

    def genLabelApply(tree: Tree) = noimpl

    def makePrimitiveBox(expr: ir.Expr, ty: Type) = noimpl

    def makePrimitiveUnbox(expr: ir.Expr, ty: Type) = noimpl

    def genPrimitiveOp(app: Apply) = {
      import scalaPrimitives._

      val sym = app.symbol
      val Apply(fun @ Select(receiver, _), args) = app
      val code = scalaPrimitives.getPrimitive(sym, receiver.tpe)

      if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code))
        genSimpleOp(app, receiver :: args, code)
      else if (code == scalaPrimitives.CONCAT)
        genStringConcat(app, receiver, args)
      else if (code == HASH)
        genScalaHash(app, receiver)
      else if (isArrayOp(code))
        genArrayOp(app, code)
      else if (code == SYNCHRONIZED)
        genSynchronized(app)
      else if (isCoercion(code))
        genCoercion(app, receiver, code)
      else
        abort("Unknown primitive operation: " + sym.fullName + "(" +
              fun.symbol.simpleName + ") " + " at: " + (app.pos))
    }

    def genSimpleOp(app: Apply, args: List[Tree], code: Int) = {
      import scalaPrimitives._

      val resType = genType(app.tpe)

      args match {
        case List(unary) =>
          val block @ E.Block(instrs, value) = genExpr(unary)
          if (code == POS) block
          else {
            val expr = code match {
              case NEG  => E.Bin(E.Bin.Sub, V.Number("0", resType), value)
              case NOT  => E.Bin(E.Bin.Xor, V.Number("-1", resType), value)
              case ZNOT => E.Bin(E.Bin.Xor, V.Bool(true), value)
              case _ =>
                abort("Unknown unary operation code: " + code)
            }
            val res = currentEnv.fresh()
            mkBlock(instrs :+ I.Assign(res, expr), res)
          }

        // TODO: convert to the common type
        // TODO: equality on reference types
        case List(left, right) =>
          val lblock @ E.Block(linstrs, lvalue) = genExpr(left)
          val rblock @ E.Block(rinstrs, rvalue) = genExpr(right)
          val expr = code match {
            case ADD  => E.Bin(E.Bin.Add,  lvalue, rvalue)
            case SUB  => E.Bin(E.Bin.Sub,  lvalue, rvalue)
            case MUL  => E.Bin(E.Bin.Mul,  lvalue, rvalue)
            case DIV  => E.Bin(E.Bin.Div,  lvalue, rvalue)
            case MOD  => E.Bin(E.Bin.Mod,  lvalue, rvalue)
            case OR   => E.Bin(E.Bin.Or,   lvalue, rvalue)
            case XOR  => E.Bin(E.Bin.Xor,  lvalue, rvalue)
            case AND  => E.Bin(E.Bin.And,  lvalue, rvalue)
            case LSL  => E.Bin(E.Bin.Shl,  lvalue, rvalue)
            case LSR  => E.Bin(E.Bin.Lshr, lvalue, rvalue)
            case ASR  => E.Bin(E.Bin.Ashr, lvalue, rvalue)
            case EQ   => E.Bin(E.Bin.Eq,   lvalue, rvalue)
            case NE   => E.Bin(E.Bin.Neq,  lvalue, rvalue)
            case LT   => E.Bin(E.Bin.Lt,   lvalue, rvalue)
            case LE   => E.Bin(E.Bin.Lte,  lvalue, rvalue)
            case GT   => E.Bin(E.Bin.Gt,   lvalue, rvalue)
            case GE   => E.Bin(E.Bin.Gte,  lvalue, rvalue)
            case ID   => ???
            case NI   => ???
            case ZOR  => E.If(lvalue, V.Bool(true), rblock)
            case ZAND => E.If(lvalue, rblock, V.Bool(false))
            case _ =>
              abort("Unknown binary operation code: " + code)
          }
          val res = currentEnv.fresh()
          mkBlock(linstrs ++ rinstrs :+ I.Assign(res, expr), res)

        case _ =>
          abort("Too many arguments for primitive function: " + app)
      }
    }

    def genStringConcat(app: Apply, receiver: Tree, args: List[Tree]) = noimpl

    def genScalaHash(app: Apply, receiver: Tree) = noimpl

    def genArrayOp(app: Apply, code: Int) = noimpl

    def genSynchronized(app: Apply) = noimpl

    def genCoercion(app: Apply, receiver: Tree, code: Int) = {
      val block @ E.Block(instrs, value) = genExpr(receiver)
      val (fromty, toty) = coercionTypes(code)

      if (fromty == toty) block
      else {
        val expr = (fromty, toty) match {
          case (T.I(lwidth), T.I(rwidth)) if lwidth < rwidth =>
            E.Conv(E.Conv.Zext, value, toty)
          case (T.I(lwidth), T.I(rwidth)) if lwidth > rwidth =>
            E.Conv(E.Conv.Trunc, value, toty)
          case (T.I(_), T.F(_)) =>
            E.Conv(E.Conv.Sitofp, value, toty)
          case (T.F(_), T.I(_)) =>
            E.Conv(E.Conv.Fptosi, value, toty)
          case (T.F64, T.F32) =>
            E.Conv(E.Conv.Fptrunc, value, toty)
          case (T.F32, T.F64) =>
            E.Conv(E.Conv.Fpext, value, toty)
        }
        val res = currentEnv.fresh()
        mkBlock(instrs :+ I.Assign(res, expr), res)
      }
    }

    def coercionTypes(code: Int): (ir.Type, ir.Type) = {
      import scalaPrimitives._

      code match {
        case B2B       => (T.I8, T.I8)
        case B2S | B2C => (T.I8, T.I16)
        case B2I       => (T.I8, T.I32)
        case B2L       => (T.I8, T.I64)
        case B2F       => (T.I8, T.F32)
        case B2D       => (T.I8, T.F64)

        case S2B       | C2B       => (T.I16, T.I8)
        case S2S | S2C | C2S | C2C => (T.I16, T.I16)
        case S2I       | C2I       => (T.I16, T.I32)
        case S2L       | C2L       => (T.I16, T.I64)
        case S2F       | C2F       => (T.I16, T.F32)
        case S2D       | C2D       => (T.I16, T.F64)

        case I2B       => (T.I32, T.I8)
        case I2S | I2C => (T.I32, T.I16)
        case I2I       => (T.I32, T.I32)
        case I2L       => (T.I32, T.I64)
        case I2F       => (T.I32, T.F32)
        case I2D       => (T.I32, T.F64)

        case L2B       => (T.I64, T.I8)
        case L2S | L2C => (T.I64, T.I16)
        case L2I       => (T.I64, T.I32)
        case L2L       => (T.I64, T.I64)
        case L2F       => (T.I64, T.F32)
        case L2D       => (T.I64, T.F64)

        case F2B       => (T.F32, T.I8)
        case F2S | F2C => (T.F32, T.I16)
        case F2I       => (T.F32, T.I32)
        case F2L       => (T.F32, T.I64)
        case F2F       => (T.F32, T.F32)
        case F2D       => (T.F32, T.F64)

        case D2B       => (T.F64, T.I8)
        case D2S | D2C => (T.F64, T.I16)
        case D2I       => (T.F64, T.I32)
        case D2L       => (T.F64, T.I64)
        case D2F       => (T.F64, T.F32)
        case D2D       => (T.F64, T.F64)
      }
    }

    def genApplyTypeApply(app: Apply) = {
      val Apply(TypeApply(fun @ Select(obj, _), targs), _) = app
      val ty = genType(targs.head.tpe)
      val E.Block(instrs, l) = genExpr(obj)
      val expr = fun.symbol match {
        case Object_isInstanceOf =>
          E.Is(l, ty)
        case Object_asInstanceOf =>
          E.Conv(E.Conv.Dyncast, l, ty)
      }
      val res = currentEnv.fresh()
      val instr = I.Assign(res, expr)

      E.Block(instrs :+ instr, res)
    }

    def genApplySuper(app: Apply) = ???

    // TODO: new array
    def genApplyNew(app: Apply) = {
      val Apply(fun @ Select(New(tpt), nme.CONSTRUCTOR), args) = app
      val ctor = fun.symbol
      val tpe = tpt.tpe

      genNew(tpe.typeSymbol, ctor, args)
    }

    def genNew(clazz: Symbol, ctor: Symbol, args: List[Tree]) = {
      val argblocks = args.map(genExpr)
      val arginstrs = argblocks.flatMap { case E.Block(i, _) => i }
      val argvals = argblocks.map { case E.Block(_, v) => v }
      val cname = encodeClassName(clazz)
      val ctorname = encodeMethodName(ctor)
      val res = currentEnv.fresh()

      mkBlock(
        arginstrs :+
        I.Assign(res, E.New(cname)) :+
        E.Call(N.Nested(cname, ctorname), res +: argvals),
        res)
    }

    def genDefault(t: ir.Type) = t match {
      case T.Unit          => V.Unit
      case T.I(_) | T.F(_) => V.Number("0", t)
      case T.Ptr(_)        => V.Null
      case _               => abort(s"can't generate default for $t")
    }

    def genNormalApply(app: Apply) = {
      val Apply(fun @ Select(receiver, _), args) = app
      val res = currentEnv.fresh()
      val E.Block(rinstrs, rvalue) = genExpr(receiver)
      val argblocks = args.map(genExpr)
      val arginstrs = argblocks.flatMap { case E.Block(instrs, _) => instrs }
      val argvalues = argblocks.map { case E.Block(_, value) => value }
      val mname     = N.Nested(encodeClassName(receiver.symbol),
                                     encodeMethodName(fun.symbol))
      val callargs  = rvalue +: argvalues
      val callinstr = I.Assign(res, E.Call(mname, callargs))

      mkBlock(rinstrs ++ arginstrs :+ callinstr, res)
    }

    lazy val genObjectType = T.Ptr(N.Global("java.lang.Object"))

    def genRefType(sym: Symbol, targs: List[Type] = Nil) = sym match {
      case ArrayClass   => T.Array(genType(targs.head))
      case NullClass    => T.Null
      case NothingClass => T.Nothing
      case _            => encodeClassName(sym)
    }

    lazy val genPrimitiveType: PartialFunction[Symbol, ir.Type] = {
      case UnitClass    => T.Unit
      case BooleanClass => T.Bool
      case ByteClass    => T.I8
      case CharClass    => T.I16
      case ShortClass   => T.I16
      case IntClass     => T.I32
      case LongClass    => T.I64
      case FloatClass   => T.F32
      case DoubleClass  => T.F64
    }

    def genPrimitiveOrRefType(sym: Symbol, targs: List[Type] = Nil) =
      genPrimitiveType.applyOrElse(sym, genRefType((_: Symbol), targs))

    def genType(t: Type): ir.Type = t.normalize match {
      case ThisType(ArrayClass)            => genObjectType
      case ThisType(sym)                   => genRefType(sym)
      case SingleType(_, sym)              => genPrimitiveOrRefType(sym)
      case ConstantType(_)                 => genType(t.underlying)
      case TypeRef(_, sym, args)           => genPrimitiveOrRefType(sym, args)
      case ClassInfoType(_, _, ArrayClass) => abort("ClassInfoType to ArrayClass!")
      case ClassInfoType(_, _, sym)        => genPrimitiveOrRefType(sym)
      case t: AnnotatedType                => genType(t.underlying)
      case tpe: ErasedValueType            => genRefType(tpe.valueClazz)
    }

    def debug[T](msg: String)(v: T): T = { println(s"$msg = $v"); v }

    def encodeFieldName(sym: Symbol) = N.Global(sym.name.toString)

    def encodeMethodName(sym: Symbol) = N.Global(sym.name.toString)

    def encodeClassName(sym: Symbol) = N.Global(sym.fullName.toString)

    def mkBlock(instrs: Seq[I], value: V) =
      if (instrs.isEmpty) value else E.Block(instrs, value)
  }
}

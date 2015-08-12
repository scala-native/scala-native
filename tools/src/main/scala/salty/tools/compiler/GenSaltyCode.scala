package salty.tools
package compiler

import scala.tools.nsc._
import scala.tools.nsc.plugins._
import salty.ir
import salty.util.ScopedVar, ScopedVar.withScopedVars

abstract class GenSaltyCode extends PluginComponent {
  import global._
  import global.definitions._

  def noimpl = ir.Name.Global("not_implemented")

  val phaseName = "saltycode"

  override def newPhase(prev: Phase): StdPhase =
    new SaltyCodePhase(prev)

  class Env {
    private var freshCounter: Int = 0
    private var used: Set[ir.Name] = Set.empty[ir.Name]
    private var subst: Map[Symbol, ir.Name] = Map.empty[Symbol, ir.Name]

    private def freshId(): Int = {
      val res = freshCounter
      freshCounter += 1
      res
    }

    def enter(sym: Symbol): ir.Name = {
      val name = ir.Name.Local(sym.name + "_" + freshId())
      subst += sym -> name
      name
    }

    def resolve(sym: Symbol): ir.Name = subst(sym)

    def fresh(): ir.Name = ir.Name.Local(freshId().toString)
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
        ir.Stat.Module(irName, irParent, irInterfaces, irBody)
      else if (sym.isInterface)
        ir.Stat.Interface(irName, irInterfaces, irBody)
      else
        ir.Stat.Class(irName, irParent, irInterfaces, irBody)
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
      case EmptyTree  => Seq()
      case dd: DefDef => Seq(genMethod(dd))
      case _  => Seq()
    }

    def genMethod(dd: DefDef): ir.Stat = withScopedVars (
      currentMethodSym := dd.symbol
    ) {
      val sym = dd.symbol
      val name = encodeMethodName(sym)
      val paramSyms = methodParamSymbols(dd)
      val ty =
        if (dd.symbol.isClassConstructor) ir.Type.Unit
        else genType(sym.tpe.resultType)

      if (dd.symbol.isDeferred) {
        val params = genDeclParams(paramSyms)
        ir.Stat.Decl(name, params, ty)
      } else {
        withScopedVars (
          currentEnv := new Env
        ) {
          val params = genDefParams(paramSyms)
          val body = genExpr(dd.rhs)
          ir.Stat.Def(name, params, ty, body)
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
      case Literal(value) =>
        genLiteral(value)

      case app: Apply =>
        genApply(app)

      case This(qual) =>
        if (tree.symbol == currentClassSym.get) ir.Val.This
        else encodeClassName(tree.symbol)

      case id: Ident =>
        val sym = id.symbol
        if (sym.isModule) encodeClassName(sym)
        else currentEnv.resolve(sym)

      case If(cond, thenp, elsep) =>
        val ir.Expr.Block(instrs, value) = genExpr(cond)
        val res = currentEnv.fresh()
        val instr =
          ir.Instr.Assign(res, ir.Expr.If(value, genExpr(thenp), genExpr(elsep)))
        mkBlock(instrs :+ instr, res)

      case _ =>
        noimpl
    }

    def genLiteral(value: Constant) = value.tag match {
      case NullTag =>
        ir.Val.Null
      case UnitTag =>
        ir.Val.Unit
      case BooleanTag =>
        ir.Val.Bool(value.booleanValue)
      case ByteTag =>
        ir.Val.Number(value.intValue.toString, ir.Type.I8)
      case ShortTag | CharTag =>
        ir.Val.Number(value.intValue.toString, ir.Type.I16)
      case IntTag =>
        ir.Val.Number(value.intValue.toString, ir.Type.I32)
      case LongTag =>
        ir.Val.Number(value.longValue.toString, ir.Type.I64)
      case FloatTag =>
        ir.Val.Number(value.floatValue.toString, ir.Type.F32)
      case DoubleTag =>
        ir.Val.Number(value.doubleValue.toString, ir.Type.F64)
      case StringTag =>
        ???
      case ClazzTag =>
        ???
      case EnumTag =>
        ???
    }

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
      import ir.Expr.Bin
      val resType = genType(app.tpe)

      args match {
        case List(unary) =>
          val block @ ir.Expr.Block(instrs, value) = genExpr(unary)
          if (code == POS) block
          else {
            val expr = code match {
              case NEG  => Bin(Bin.Sub, ir.Val.Number("0", resType), value)
              case NOT  => Bin(Bin.Xor, ir.Val.Number("-1", resType), value)
              case ZNOT => Bin(Bin.Xor, ir.Val.Bool(true), value)
              case _ =>
                abort("Unknown unary operation code: " + code)
            }
            val res = currentEnv.fresh()
            mkBlock(instrs :+ ir.Instr.Assign(res, expr), res)
          }

        // TODO: convert to the common type
        // TODO: equality on reference types
        case List(left, right) =>
          val lblock @ ir.Expr.Block(linstrs, lvalue) = genExpr(left)
          val rblock @ ir.Expr.Block(rinstrs, rvalue) = genExpr(right)
          val expr = code match {
            case ADD  => Bin(Bin.Add,  lvalue, rvalue)
            case SUB  => Bin(Bin.Sub,  lvalue, rvalue)
            case MUL  => Bin(Bin.Mul,  lvalue, rvalue)
            case DIV  => Bin(Bin.Div,  lvalue, rvalue)
            case MOD  => Bin(Bin.Mod,  lvalue, rvalue)
            case OR   => Bin(Bin.Or,   lvalue, rvalue)
            case XOR  => Bin(Bin.Xor,  lvalue, rvalue)
            case AND  => Bin(Bin.And,  lvalue, rvalue)
            case LSL  => Bin(Bin.Shl,  lvalue, rvalue)
            case LSR  => Bin(Bin.Lshr, lvalue, rvalue)
            case ASR  => Bin(Bin.Ashr, lvalue, rvalue)
            case EQ   => Bin(Bin.Eq,   lvalue, rvalue)
            case NE   => Bin(Bin.Neq,  lvalue, rvalue)
            case LT   => Bin(Bin.Lt,   lvalue, rvalue)
            case LE   => Bin(Bin.Lte,  lvalue, rvalue)
            case GT   => Bin(Bin.Gt,   lvalue, rvalue)
            case GE   => Bin(Bin.Gte,  lvalue, rvalue)
            case ID   => ???
            case NI   => ???
            case ZOR  => ir.Expr.If(lvalue, ir.Val.Bool(true), rblock)
            case ZAND => ir.Expr.If(lvalue, rblock, ir.Val.Bool(false))
            case _ =>
              abort("Unknown binary operation code: " + code)
          }
          val res = currentEnv.fresh()
          mkBlock(linstrs ++ rinstrs :+ ir.Instr.Assign(res, expr), res)

        case _ =>
          abort("Too many arguments for primitive function: " + app)
      }
    }

    def genStringConcat(app: Apply, receiver: Tree, args: List[Tree]) = noimpl

    def genScalaHash(app: Apply, receiver: Tree) = noimpl

    def genArrayOp(app: Apply, code: Int) = noimpl

    def genSynchronized(app: Apply) = noimpl

    def genCoercion(app: Apply, receiver: Tree, code: Int) = {
      import salty.ir.Expr.Conv
      import salty.ir.Type._

      val block @ ir.Expr.Block(instrs, value) = genExpr(receiver)
      val (fromty, toty) = coercionTypes(code)

      if (fromty == toty) block
      else {
        val expr = (fromty, toty) match {
          case (I(lwidth), I(rwidth)) if lwidth < rwidth =>
            Conv(Conv.Zext, value, toty)
          case (I(lwidth), I(rwidth)) if lwidth > rwidth =>
            Conv(Conv.Trunc, value, toty)
          case (I(_), F(_)) =>
            Conv(Conv.Sitofp, value, toty)
          case (F(_), I(_)) =>
            Conv(Conv.Fptosi, value, toty)
          case (F64, F32) =>
            Conv(Conv.Fptrunc, value, toty)
          case (F32, F64) =>
            Conv(Conv.Fpext, value, toty)
        }
        val res = currentEnv.fresh()
        mkBlock(instrs :+ ir.Instr.Assign(res, expr), res)
      }
    }

    def coercionTypes(code: Int) = {
      import scalaPrimitives._
      import salty.ir.Type._

      code match {
        case B2B       => (I8, I8)
        case B2S | B2C => (I8, I16)
        case B2I       => (I8, I32)
        case B2L       => (I8, I64)
        case B2F       => (I8, F32)
        case B2D       => (I8, F64)

        case S2B       | C2B       => (I16, I8)
        case S2S | S2C | C2S | C2C => (I16, I16)
        case S2I       | C2I       => (I16, I32)
        case S2L       | C2L       => (I16, I64)
        case S2F       | C2F       => (I16, F32)
        case S2D       | C2D       => (I16, F64)

        case I2B       => (I32, I8)
        case I2S | I2C => (I32, I16)
        case I2I       => (I32, I32)
        case I2L       => (I32, I64)
        case I2F       => (I32, F32)
        case I2D       => (I32, F64)

        case L2B       => (I64, I8)
        case L2S | L2C => (I64, I16)
        case L2I       => (I64, I32)
        case L2L       => (I64, I64)
        case L2F       => (I64, F32)
        case L2D       => (I64, F64)

        case F2B       => (F32, I8)
        case F2S | F2C => (F32, I16)
        case F2I       => (F32, I32)
        case F2L       => (F32, I64)
        case F2F       => (F32, F32)
        case F2D       => (F32, F64)

        case D2B       => (F64, I8)
        case D2S | D2C => (F64, I16)
        case D2I       => (F64, I32)
        case D2L       => (F64, I64)
        case D2F       => (F64, F32)
        case D2D       => (F64, F64)
      }
    }

    def genApplyTypeApply(app: Apply) = {
      val Apply(TypeApply(fun @ Select(obj, _), targs), _) = app
      val cast = fun.symbol match {
        case Object_isInstanceOf => false
        case Object_asInstanceOf => true
      }
      val ty = genType(targs.head.tpe)
      val ir.Expr.Block(instrs, l) = genExpr(obj)
      val res = currentEnv.fresh()
      val instr =
        ir.Instr.Assign(res,
          if (cast)
            ir.Expr.Conv(ir.Expr.Conv.Dyncast, l, ty)
          else
            ir.Expr.Is(l, ty))

      ir.Expr.Block(instrs :+ instr, res)
    }

    def genApplySuper(app: Apply) = ???

    def genApplyNew(app: Apply) = ???

    def genNormalApply(app: Apply) = {
      val Apply(fun @ Select(receiver, _), args) = app
      val res = currentEnv.fresh()
      val ir.Expr.Block(rinstrs, rvalue) = genExpr(receiver)
      val argblocks = args.map(genExpr)
      val arginstrs = argblocks.map { case ir.Expr.Block(instrs, _) => instrs } .flatten
      val argvalues = argblocks.map { case ir.Expr.Block(_, value) => value }
      val mname     = ir.Name.Nested(encodeClassName(receiver.symbol),
                                     encodeMethodName(fun.symbol))
      val callargs  = rvalue +: argvalues
      val callinstr = ir.Instr.Assign(res, ir.Expr.Call(mname, callargs))

      mkBlock(rinstrs ++ arginstrs :+ callinstr, res)
    }

    lazy val genObjectType = ir.Type.Ptr(ir.Name.Global("java.lang.Object"))

    def genRefType(sym: Symbol, targs: List[Type] = Nil) = sym match {
      case ArrayClass   => ir.Type.Array(genType(targs.head))
      case NullClass    => ir.Type.Null
      case NothingClass => ir.Type.Nothing
      case _            => encodeClassName(sym)
    }

    lazy val genPrimitiveType: PartialFunction[Symbol, ir.Type] = {
      case UnitClass    => ir.Type.Unit
      case BooleanClass => ir.Type.Bool
      case ByteClass    => ir.Type.I8
      case CharClass    => ir.Type.I16
      case ShortClass   => ir.Type.I16
      case IntClass     => ir.Type.I32
      case LongClass    => ir.Type.I64
      case FloatClass   => ir.Type.F32
      case DoubleClass  => ir.Type.F64
    }

    def genPrimitiveOrRefType(sym: Symbol, targs: List[Type] = Nil) =
      genPrimitiveType.applyOrElse(sym, (_: Symbol) => genRefType(sym, targs))

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

    def encodeMethodName(sym: Symbol) = ir.Name.Global(sym.name.toString)

    def encodeClassName(sym: Symbol) = ir.Name.Global(sym.fullName.toString)

    def mkBlock(instrs: Seq[ir.Instr], value: ir.Val) =
      if (instrs.isEmpty) value else ir.Expr.Block(instrs, value)
  }
}

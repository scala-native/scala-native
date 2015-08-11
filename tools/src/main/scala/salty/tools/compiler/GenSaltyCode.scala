package salty.tools
package compiler

import scala.tools.nsc._
import scala.tools.nsc.plugins._
import salty.ir
import salty.util.ScopedVar, ScopedVar.withScopedVars

abstract class GenSaltyCode extends PluginComponent {
  import global._
  import global.definitions._

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
      val name = ir.Name("%" + sym.name + "_" + freshId())
      subst += sym -> name
      name
    }

    def resolve(sym: Symbol): ir.Name = subst(sym)

    def fresh(): ir.Name = ir.Name("%" + freshId())
  }

  class SaltyCodePhase(prev: Phase) extends StdPhase(prev) {
    val currentClassSym = new ScopedVar[Symbol]
    val currentMethodSym = new ScopedVar[Symbol]
    val currentEnv = new ScopedVar[Env]

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
    ){
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
    }

    def genMethod(dd: DefDef): ir.Stat = withScopedVars (
      currentMethodSym := dd.symbol
    ) {
      val sym = dd.symbol
      val name = encodeMethodName(dd)
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

    def genDeclParams(paramSyms: List[Symbol]): Seq[ir.Type] = {
      val params = paramSyms.map(sym => genType(sym.tpe))
      encodeClassType(currentClassSym) +: params
    }

    def genDefParams(paramSyms: List[Symbol]): Seq[ir.LabeledType] = {
      val params =
        paramSyms.map { sym =>
          val name = currentEnv.enter(sym)
          val ty = genType(sym.tpe)
          ir.LabeledType(name, ty)
        }
      val self = ir.LabeledType(ir.Name("%this"), encodeClassType(currentClassSym))
      self +: params
    }

    def genExpr(t: Tree): ir.Expr.Block = t match {
      case Literal(value) =>
        val v = value.tag match {
          case NullTag =>
            ir.Val.Null
          case UnitTag =>
            ir.Val.Unit
          case BooleanTag =>
            ir.Val.Bool(value.booleanValue)
          case ByteTag =>
            ir.Val.Integer(value.intValue.toString, ir.Type.I8)
          case ShortTag | CharTag =>
            ir.Val.Integer(value.intValue.toString, ir.Type.I16)
          case IntTag =>
            ir.Val.Integer(value.intValue.toString, ir.Type.I32)
          case LongTag =>
            ir.Val.Integer(value.longValue.toString, ir.Type.I64)
          case FloatTag =>
            ir.Val.Float(value.floatValue.toString, ir.Type.F32)
          case DoubleTag =>
            ir.Val.Float(value.doubleValue.toString, ir.Type.F64)
          case StringTag =>
            ???
          case ClazzTag =>
            ???
          case EnumTag =>
            ???
        }
        ir.Expr.Block(v)

      case app: Apply =>
        genApply(app)

      case id: Ident =>
        val sym = id.symbol
        val name =
          if (sym.isModule) encodeClassName(sym)
          else currentEnv.resolve(sym)
        ir.Expr.Block(name)

      case _ =>
        ir.Expr.Block(ir.Name("@unrecognized"))
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
          println(s"no apply impl for ${showRaw(app)}\n${app.toString}")
          ???
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
            ir.Expr.Cast(ir.CastOp.Dyncast, l, ty)
          else
            ir.Expr.Is(l, ty))

      ir.Expr.Block(instrs :+ instr, res)
    }

    def genApplySuper(app: Apply) = ???

    def genApplyNew(app: Apply) = ???

    lazy val genObjectType = ir.Type.Ptr(ir.Name("@java.lang.Object"))

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

    def encodeMethodName(dd: DefDef) = ir.Name("@" + dd.name)

    def encodeClassName(sym: Symbol) = ir.Name("@" + sym.fullName)

    def encodeClassType(sym: Symbol) = ir.Type.Ptr(encodeClassName(sym))
  }
}

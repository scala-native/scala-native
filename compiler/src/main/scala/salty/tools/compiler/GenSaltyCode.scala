package salty.tools
package compiler

import scala.collection.mutable.ListBuffer
import scala.tools.nsc._
import scala.tools.nsc.plugins._
import scala.util.{Either, Left, Right}
import salty.ir
import salty.ir.{Expr => E, Type => Ty, Termn => Tn, Instr => I,
                 Val => V, Name => N, Stat => S, Block => B, Branch => Br,
                 BinOp, ConvOp}
import salty.ir.Shows._
import salty.util, util.Sh

abstract class GenSaltyCode extends PluginComponent
                               with GenIRFiles
                               with GenTypeKinds
                               with GenNameEncoding {
  import global._
  import global.definitions._
  import global.treeInfo.hasSynthCaseSymbol

  val phaseName = "saltycode"

  override def newPhase(prev: Phase): StdPhase =
    new SaltyCodePhase(prev)

  def debug[T](msg: String)(v: T): T = { println(s"$msg = $v"); v }

  def unreachable = abort("unreachable")

  class Env {
    var used = Set.empty[ir.Name]
    var names = Map.empty[Symbol, ir.Name]

    implicit val fresh = new ir.Fresh {
      override def apply(prefix: String) = {
        var name = super.apply(prefix)
        while (used.contains(name))
          name = super.apply(prefix)
        used += name
        name
      }
    }

    def enter(sym: Symbol): ir.Name = {
      var name = N.Local(sym.name.toString)
      var i = 0
      while (used.contains(name)) {
        name = N.Local(sym.name.toString + i)
        i += 1
      }
      used += name
      names += sym -> name
      name
    }

    def resolve(sym: Symbol): ir.Name =
      names(sym)
  }

  class LabelEnv(env: Env) {
    var blocks = Map.empty[Symbol, ir.Block]
    var params = Map.empty[Symbol, List[ir.Val]]

    def enterLabel(label: LabelDef): ir.Block = {
      val sym = label.symbol
      val name = env.enter(sym)
      val params = label.params.zip(sym.asMethod.paramLists.head).map {
        case (treeid, reflsym) =>
          val name = env.enter(reflsym)
          env.names += treeid.symbol -> name
          name
      }
      val instrs = params.map { param =>
        I.Assign(param, E.Phi(Nil))
      }
      val block = B(name, instrs, Tn.Undefined)
      this.blocks += sym -> block
      this.params += sym -> params
      block
    }

    def enterLabelCall(sym: Symbol, values: Seq[ir.Val], from: ir.Block): Unit = {
      val block = resolveLabel(sym)
      block.instrs = block.instrs.zip(values).map {
        case (I.Assign(n, E.Phi(branches)), value) =>
          I.Assign(n, E.Phi(branches :+ Br(value, from)))
        case _ =>
          unreachable
      }
    }

    def resolveLabel(sym: Symbol): ir.Block = blocks(sym)

    def resolveLabelParams(sym: Symbol): List[ir.Val] = params(sym)
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
    val currentLocalInfo = new util.ScopedVar[CollectLocalInfo]
    val currentClassSym  = new util.ScopedVar[Symbol]
    val currentMethodSym = new util.ScopedVar[Symbol]
    val currentEnv       = new util.ScopedVar[Env]
    val currentLabelEnv  = new util.ScopedVar[LabelEnv]
    val currentThis      = new util.ScopedVar[ir.Val]

    implicit def fresh: ir.Fresh = currentEnv.fresh

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
      val generatedIR = ListBuffer.empty[(Symbol, ir.Stat)]

      classDefs.foreach { cd =>
        val sym = cd.symbol
        if (isPrimitiveValueClass(sym) || (sym == ArrayClass))
          ()
        else
          generatedIR += ((sym, genClass(cd)))
      }

      for ((sym, stat) <- generatedIR) {
        genIRFile(cunit, sym, stat)
      }
    }

    def genClass(cd: ClassDef): ir.Stat = util.ScopedVar.withScopedVars (
      currentClassSym := cd.symbol
    ) {
      val sym = cd.symbol
      val name = encodeClassName(sym)
      val parent = encodeClassName(sym.superClass)
      val interfaces = genClassInterfaces(sym)
      val fields = genClassFields(sym)
      val methods = genClassMethods(cd.impl.body)
      val body = fields ++ methods

      if (sym.isModuleClass)
        S.Module(name, parent, interfaces, body)
      else if (sym.isInterface)
        S.Interface(name, interfaces, body)
      else
        S.Class(name, parent, interfaces, body)
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol
        if psym.isInterface
      } yield {
        encodeClassName(psym)
      }

    def genClassMethods(stats: List[Tree]): List[ir.Stat] =
      stats.flatMap {
        case dd: DefDef => List(genDef(dd))
        case _          => Nil
      }

    def genClassFields(sym: Symbol) =
      (for {
        f <- sym.info.decls
        if !f.isMethod && f.isTerm && !f.isModule
      } yield {
        S.Var(encodeFieldName(f), genType(f.tpe))
      }).toList

    def genDef(dd: DefDef): ir.Stat = util.ScopedVar.withScopedVars (
      currentMethodSym := dd.symbol
    ) {
      val sym = dd.symbol
      val name = encodeDefName(sym)
      val paramSyms = defParamSymbols(dd)
      val ty =
        if (dd.symbol.isClassConstructor) Ty.Unit
        else genType(sym.tpe.resultType)

      if (dd.symbol.isDeferred) {
        val params = genDeclParams(paramSyms)
        S.Declare(name, params, ty)
      } else {
        val env = new Env
        util.ScopedVar.withScopedVars (
          currentEnv := env,
          currentLabelEnv := new LabelEnv(env),
          currentLocalInfo := (new CollectLocalInfo).collect(dd.rhs)
        ) {
          val params = genDefParams(paramSyms)
          val body = genDefBody(dd.rhs, params.map(_.name))
          S.Define(name, params, ty, body)
        }
      }
    }

    def defParamSymbols(dd: DefDef): List[Symbol] = {
      val vp = dd.vparamss
      if (vp.isEmpty) Nil else vp.head.map(_.symbol)
    }

    def genDeclParams(paramSyms: List[Symbol]): List[ir.Type] =
      paramSyms.map(sym => genType(sym.tpe))

    def genDefParams(paramSyms: List[Symbol]): List[ir.LabeledType] =
      paramSyms.map { sym =>
        val name = currentEnv.enter(sym)
        val ty = genType(sym.tpe)
        ir.LabeledType(name, ty)
      }

    def genDefBody(body: Tree, paramValues: List[ir.Val]): ir.Block = (body match {
      case Block(List(ValDef(_, nme.THIS, _, _)),
                 label @ LabelDef(name, Ident(nme.THIS) :: _, rhs)) =>
        val entry = B(Tn.Undefined)
        val values = (V.This :: paramValues).take(label.params.length)
        currentLabelEnv.enterLabel(label)
        currentLabelEnv.enterLabelCall(label.symbol, values, entry)
        val block =
          util.ScopedVar.withScopedVars (
            currentThis := currentLabelEnv.resolveLabelParams(label.symbol).head
          ) {
            genLabel(label)
          }
        entry.termn = Tn.Jump(block)
        entry.simplify
      case _ =>
        util.ScopedVar.withScopedVars (
          currentThis := V.This
        ) {
          genExpr(body).simplify
        }
    }).verify

    def genExpr(tree: Tree): ir.Block = tree match {
      case label: LabelDef =>
        currentLabelEnv.enterLabel(label)
        genLabel(label)

      case vd: ValDef =>
        val name = currentEnv.enter(vd.symbol)
        val rhs = genExpr(vd.rhs)
        rhs.merge { v =>
          val isMutable = currentLocalInfo.mutableVars.contains(vd.symbol)
          if (!isMutable)
            B(List(I.Assign(name, v)),
              Tn.Out(V.Unit))
          else
            B(List(I.Assign(name, E.Alloc(genType(vd.symbol.tpe))),
                  E.Store(name, v)),
              Tn.Out(V.Unit))
        }

      case If(cond, thenp, elsep) =>
        genExpr(cond).merge { v =>
          B(Tn.If(v, genExpr(thenp), genExpr(elsep)))
        }

      case Return(expr) =>
        genExpr(expr).merge { v =>
          B(Tn.Return(v))
        }

      case Try(expr, catches, finalizer) if catches.isEmpty && finalizer.isEmpty =>
        genExpr(expr)

      case Try(expr, catches, finalizer) =>
        val finallyopt =
          if (finalizer.isEmpty) None
          else
            Some(genExpr(finalizer).merge { v =>
              B(Tn.Out(V.Unit))
            })
        B(Tn.Try(genExpr(expr), genCatch(catches), finallyopt))

      case Throw(expr) =>
        genExpr(expr).merge { v =>
          B(Tn.Throw(v))
        }

      case app: Apply =>
        genApply(app)

      case app: ApplyDynamic =>
        genApplyDynamic(app)

      case This(qual) =>
        B(Tn.Out(
          if (tree.symbol == currentClassSym.get) currentThis
          else encodeClassName(tree.symbol)))

      case Select(qual, sel) =>
        val sym = tree.symbol
        if (sym.isModule)
          B(Tn.Out(encodeClassName(sym)))
        else if (sym.isStaticMember)
          genStaticMember(sym)
        else
          genExpr(qual).merge { v =>
            val n = fresh()
            B(List(I.Assign(n, E.Load(V.Elem(v, encodeFullFieldName(tree.symbol))))),
              Tn.Out(n))
          }

      case id: Ident =>
        val sym = id.symbol
        if (!currentLocalInfo.mutableVars.contains(sym))
          B(Tn.Out(
            if (sym.isModule) encodeClassName(sym)
            else currentEnv.resolve(sym)))
        else {
          val name = fresh()
          B(List(I.Assign(name, E.Load(currentEnv.resolve(sym)))),
            Tn.Out(name))
        }

      case lit: Literal =>
        genValue(lit) match {
          case Left(value)  => B(Tn.Out(value))
          case Right(block) => block
        }

      case block: Block =>
        genBlock(block)

      case Typed(Super(_, _), _) =>
        B(Tn.Out(currentThis))

      case Typed(expr, _) =>
        genExpr(expr)

      case Assign(lhs, rhs) =>
        lhs match {
          case sel @ Select(qual, _) =>
            genExpr(qual).chain(genExpr(rhs)) { (vqual, vrhs) =>
              B(List(E.Store(V.Elem(vqual, encodeFullFieldName(sel.symbol)), vrhs)),
                Tn.Out(V.Unit))
            }
          case id: Ident =>
            genExpr(rhs).merge { v =>
              B(List(E.Store(currentEnv.resolve(id.symbol), v)),
                Tn.Out(V.Unit))
            }
        }

      case av: ArrayValue =>
        genArrayValue(av)

      case m: Match =>
        genSwitch(m)

      case fun: Function =>
        ???

      case EmptyTree =>
        B(Tn.Out(V.Unit))

      case _ =>
        abort("Unexpected tree in genExpr: " +
              tree + "/" + tree.getClass + " at: " + tree.pos)
    }


    // TODO: finally
    def genCatch(catches: List[Tree]) =
      if (catches.isEmpty) None
      else Some {
        val exc = fresh("e")
        val elseb = B(Tn.Throw(exc))
        val catchb =
          catches.foldRight(elseb) { (catchp, elseb) =>
            val CaseDef(pat, _, body) = catchp
            val (nameopt, excty) = pat match {
              case Typed(Ident(nme.WILDCARD), tpt) =>
                (None, genType(tpt.tpe))
              case Ident(nme.WILDCARD) =>
                (None, genType(ThrowableClass.tpe))
              case Bind(_, _) =>
                (Some(currentEnv.enter(pat.symbol)), genType(pat.symbol.tpe))
            }
            val bodyb = genExpr(body)
            val n = fresh()

            B(List(I.Assign(n, E.Is(exc, excty))),
              Tn.If(n,
                nameopt.map { name =>
                  B(List(I.Assign(name, E.Conv(ConvOp.Cast, exc, excty))),
                    Tn.Jump(bodyb))
                }.getOrElse(bodyb),
                elseb))
          }

        B(List(I.Assign(exc, E.Catchpad)), Tn.Jump(catchb))
      }

    def genBlock(block: Block) = {
      val Block(stats, last) = block

      def isCaseLabelDef(tree: Tree) =
        tree.isInstanceOf[LabelDef] && hasSynthCaseSymbol(tree)

      def translateMatch(last: LabelDef) = {
        val (prologue, cases) = stats.span(s => !isCaseLabelDef(s))
        val labels = cases.map { case label: LabelDef => label }
        genMatch(prologue, labels, last)
      }

      last match {
        case label: LabelDef if isCaseLabelDef(label) =>
          translateMatch(label)

        case Apply(TypeApply(Select(label: LabelDef, nme.asInstanceOf_Ob), _), _)
            if isCaseLabelDef(label) =>
          translateMatch(label)

        case _ =>
          B.chain(stats.map(genExpr)) { vals =>
            genExpr(last)
          }
      }
    }

    def genMatch(prologue: List[Tree], cases: List[LabelDef], last: LabelDef) = {
      B.chain(prologue.map(genExpr)) { _ =>
        currentLabelEnv.enterLabel(last)
        for (label <- cases) {
          currentLabelEnv.enterLabel(label)
        }
        genLabel(last)
        cases.map(genLabel).head
      }
    }

    def genLabel(label: LabelDef) = {
      val entry = currentLabelEnv.resolveLabel(label.symbol)
      val target = genExpr(label.rhs)
      entry.termn = Tn.Jump(target)
      entry
    }

    def genValue(lit: Literal): Either[ir.Val, ir.Block]= {
      val value = lit.value

      value.tag match {
        case NullTag =>
          Left(V.Null)
        case UnitTag =>
          Left(V.Unit)
        case BooleanTag =>
          Left(V.Bool(value.booleanValue))
        case ByteTag =>
          Left(V.Number(value.intValue.toString, Ty.I8))
        case ShortTag | CharTag =>
          Left(V.Number(value.intValue.toString, Ty.I16))
        case IntTag =>
          Left(V.Number(value.intValue.toString, Ty.I32))
        case LongTag =>
          Left(V.Number(value.longValue.toString, Ty.I64))
        case FloatTag =>
          Left(V.Number(value.floatValue.toString, Ty.F32))
        case DoubleTag =>
          Left(V.Number(value.doubleValue.toString, Ty.F64))
        case ClazzTag =>
          Left(V.Class(genType(value.typeValue)))
        case StringTag =>
          Left(V.Str(value.stringValue))
        case EnumTag =>
          Right(genStaticMember(value.symbolValue))
      }
    }

    // TODO: insert coercions
    def genArrayValue(av: ArrayValue) = {
      val ArrayValue(tpt, elems) = av

      B.chain(elems.map(genExpr)) { values =>
        val ty  = genType(tpt.tpe)
        val len = values.length
        val n   = fresh()

        B(List(I.Assign(n, E.Alloc(Ty.Array(ty, len))),
              E.Store(n, V.Array(values))),
          Tn.Out(V.Slice(n, V(len))))
      }
    }

    def genSwitch(m: Match) = {
      val Match(sel, cases) = m

      genExpr(sel).merge { selvalue =>
        val defaultBody =
          cases.collectFirst {
            case c @ CaseDef(Ident(nme.WILDCARD), _, body) => body
          }.get
        val defaultBlock = genExpr(defaultBody)
        val branches = cases.flatMap { case CaseDef(pat, guard, body) =>
          val bodyBlock = genExpr(body)
          val guardedBlock =
            if (guard.isEmpty) bodyBlock
            else
              genExpr(guard).merge { gv =>
                B(Tn.If(gv, bodyBlock, defaultBlock))
              }
          val values: List[ir.Val] =
            pat match {
              case lit: Literal =>
                val Left(value) = genValue(lit)
                List(value)
              case Alternative(alts) =>
                alts.map {
                  case lit: Literal =>
                    val Left(value) = genValue(lit)
                    value
                }
              case _ =>
                Nil
            }
          values.map(Br(_, guardedBlock))
        }

        B(Tn.Switch(selvalue, defaultBlock, branches))
      }
    }

    def genApplyDynamic(app: ApplyDynamic) = ???

    def genApply(app: Apply): ir.Block = {
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
            makePrimitiveBox(arg, arg.tpe)
          } else if (currentRun.runDefinitions.isUnbox(sym)) {
            makePrimitiveUnbox(args.head, app.tpe)
          } else {
            genNormalApply(app)
          }
      }
    }

    def genLabelApply(tree: Tree) = tree match {
      case Apply(fun, Nil) =>
        currentLabelEnv.resolveLabel(fun.symbol)
      case Apply(fun, args) =>
        val argblocks = args.map(genExpr)
        B.chain(argblocks) { vals =>
          val block = B(Tn.Jump(currentLabelEnv.resolveLabel(fun.symbol)))
          currentLabelEnv.enterLabelCall(fun.symbol, vals, block)
          block
        }
    }

    lazy val primitive2box = Map(
      BooleanTpe -> Ty.Named(N.Global("java.lang.Boolean")),
      ByteTpe    -> Ty.Named(N.Global("java.lang.Byte")),
      CharTpe    -> Ty.Named(N.Global("java.lang.Character")),
      ShortTpe   -> Ty.Named(N.Global("java.lang.Short")),
      IntTpe     -> Ty.Named(N.Global("java.lang.Integer")),
      LongTpe    -> Ty.Named(N.Global("java.lang.Long")),
      FloatTpe   -> Ty.Named(N.Global("java.lang.Float")),
      DoubleTpe  -> Ty.Named(N.Global("java.lang.Double"))
    )

    lazy val ctorName = N.Global(nme.CONSTRUCTOR.toString)

    def makePrimitiveBox(expr: Tree, tpe: Type) =
      genExpr(expr).merge { v =>
        val name = fresh()
        B(List(I.Assign(name, E.Box(v, primitive2box(tpe.widen)))),
          Tn.Out(name))
      }

    def makePrimitiveUnbox(expr: Tree, tpe: Type) =
      genExpr(expr).merge { v =>
        val name = fresh()
        B(List(I.Assign(name, E.Unbox(v, primitive2box(tpe.widen)))),
          Tn.Out(name))
      }

    def genPrimitiveOp(app: Apply): ir.Block = {
      import scalaPrimitives._

      val sym = app.symbol
      val Apply(fun @ Select(receiver, _), args) = app
      val code = scalaPrimitives.getPrimitive(sym, receiver.tpe)

      if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code))
        genSimpleOp(app, receiver :: args, code)
      else if (code == CONCAT)
        genStringConcat(app, receiver, args)
      else if (code == HASH)
        genHash(app, receiver)
      else if (isArrayOp(code))
        genArrayOp(app, code)
      else if (isCoercion(code))
        genCoercion(app, receiver, code)
      else if (code == SYNCHRONIZED)
        genSynchronized(app)
      else
        abort("Unknown primitive operation: " + sym.fullName + "(" +
              fun.symbol.simpleName + ") " + " at: " + (app.pos))
    }

    def genSimpleOp(app: Apply, args: List[Tree], code: Int): ir.Block = {
      import scalaPrimitives._

      val retty = genType(app.tpe)

      args match {
        case List(right) =>
          val rblock = genExpr(right)
          def unary(op: BinOp, lvalue: ir.Val) =
            rblock.merge { rvalue =>
              val n = fresh()
              B(List(I.Assign(n, E.Bin(op, lvalue, rvalue))),
                Tn.Out(n))
            }
          code match {
            case POS  => rblock
            case NEG  => unary(BinOp.Sub, V.Number("0", retty))
            case NOT  => unary(BinOp.Xor, V.Number("-1", retty))
            case ZNOT => unary(BinOp.Xor, V(true))
            case _ =>
              abort("Unknown unary operation code: " + code)
          }

        // TODO: convert to the common type
        // TODO: eq, ne
        case List(left, right) =>
          val lblock = genExpr(left)
          val rblock = genExpr(right)
          val lty    = genType(left.tpe)
          val rty    = genType(right.tpe)
          def bin(op: BinOp, ty: ir.Type) =
            lblock.chain(rblock) { (lvalue, rvalue) =>
              genCoercion(lvalue, lty, ty).merge { lcoerced =>
                genCoercion(rvalue, rty, ty).merge { rcoerced =>
                  val n = fresh()
                  B(List(I.Assign(n, E.Bin(op, lcoerced, rcoerced))),
                    Tn.Out(n))
                }
              }
            }
          def equality(negated: Boolean) =
            genKind(left.tpe) match {
              case ClassKind(_) =>
                lblock.chain(rblock) { (lvalue, rvalue) =>
                  val classEq = genClassEquality(lvalue, rvalue)
                  if (!negated)
                    classEq
                  else
                    classEq.merge { v =>
                      val n = fresh()
                      B(List(I.Assign(n, E.Bin(BinOp.Xor, V(true), v))),
                        Tn.Out(n))
                    }
                }
              case kind =>
                val op = if (negated) BinOp.Neq else BinOp.Eq
                bin(BinOp.Eq, binaryOperationType(lty, rty))
            }
          def referenceEquality(negated: Boolean) = {
            val op = if (negated) BinOp.Neq else BinOp.Eq
            val n = fresh()
            lblock.chain(rblock) { (lvalue, rvalue) =>
              B(List(I.Assign(n, E.Bin(op, lvalue, rvalue))),
                Tn.Out(n))
            }
          }
          code match {
            // arithmetic & bitwise
            case ADD  => bin(BinOp.Add,  retty)
            case SUB  => bin(BinOp.Sub,  retty)
            case MUL  => bin(BinOp.Mul,  retty)
            case DIV  => bin(BinOp.Div,  retty)
            case MOD  => bin(BinOp.Mod,  retty)
            case OR   => bin(BinOp.Or,   retty)
            case XOR  => bin(BinOp.Xor,  retty)
            case AND  => bin(BinOp.And,  retty)
            case LSL  => bin(BinOp.Shl,  retty)
            case LSR  => bin(BinOp.Lshr, retty)
            case ASR  => bin(BinOp.Ashr, retty)
            // comparison
            case LT   => bin(BinOp.Lt,  binaryOperationType(lty, rty))
            case LE   => bin(BinOp.Lte, binaryOperationType(lty, rty))
            case GT   => bin(BinOp.Gt,  binaryOperationType(lty, rty))
            case GE   => bin(BinOp.Gte, binaryOperationType(lty, rty))
            // equality
            case EQ   => equality(negated = false)
            case NE   => equality(negated = true)
            case ID   => referenceEquality(negated = false)
            case NI   => referenceEquality(negated = true)
            // logical
            case ZOR  =>
              lblock.merge { lvalue =>
                B(Tn.If(lvalue, B(Tn.Out(V(true))), rblock))
              }
            case ZAND =>
              lblock.merge { lvalue =>
                B(Tn.If(lvalue, rblock, B(Tn.Out(V(false)))))
              }
            case _ =>
              abort("Unknown binary operation code: " + code)
          }

        case _ =>
          abort("Too many arguments for primitive function: " + app)
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

    def genClassEquality(lvalue: ir.Val, rvalue: ir.Val) = {
      import scalaPrimitives._

      val n = fresh()

      rvalue match {
        case V.Null =>
          B(List(I.Assign(n, E.Bin(BinOp.Eq, lvalue, V.Null))),
            Tn.Out(n))
        case _ =>
          B(List(I.Assign(n, E.Bin(BinOp.Equals, lvalue, rvalue))),
            Tn.Out(n))
      }
    }

    def genStringConcat(tree: Tree, receiver: Tree, args: List[Tree]) =
      genExpr(receiver).chain(genExpr(args.head)) { (l, r) =>
        val n = fresh()

        B(List(I.Assign(n, E.Bin(BinOp.Add, l, r))),
          Tn.Out(n))
      }

    def genHash(tree: Tree, receiver: Tree) = {
      val cls    = ScalaRunTimeModule
      val method = getMember(cls, nme.hash_)

      genExpr(receiver).merge { v =>
        genMethodCall(method, encodeClassName(cls), List(v))
      }
    }

    def genArrayOp(app: Apply, code: Int) = {
      import scalaPrimitives._

      val Apply(Select(array, _), args) = app
      val blocks = (array :: args).map(genExpr)

      B.chain(blocks) { values =>
        val arrayvalue :: argvalues = values
        val n = fresh()

        if (scalaPrimitives.isArrayGet(code))
          B(List(I.Assign(n, E.Load(V.Elem(arrayvalue, argvalues(0))))),
            Tn.Out(n))
        else if (scalaPrimitives.isArraySet(code))
          B(List(E.Store(V.Elem(arrayvalue, argvalues(0)), argvalues(1))),
            Tn.Out(V.Unit))
        else
          B(List(I.Assign(n, E.Length(arrayvalue))),
            Tn.Out(n))
      }
    }

    // TODO: re-evaluate dropping sychcronized
    // TODO: NPE
    def genSynchronized(app: Apply) = {
      val Apply(Select(receiver, _), List(arg)) = app
      genExpr(receiver).chain(genExpr(arg)) { (v1, v2) =>
        B(Tn.Out(v2))
      }
    }

    def genCoercion(app: Apply, receiver: Tree, code: Int): ir.Block = {
      val block = genExpr(receiver)
      val (fromty, toty) = coercionTypes(code)

      block.merge { value =>
        genCoercion(value, fromty, toty)
      }
    }

    def genCoercion(value: ir.Val, fromty: ir.Type, toty: ir.Type): ir.Block =
      if (fromty == toty) B(Tn.Out(value))
      else {
        val op = (fromty, toty) match {
          case (Ty.I(lwidth), Ty.I(rwidth))
            if lwidth < rwidth      => ConvOp.Zext
          case (Ty.I(lwidth), Ty.I(rwidth))
            if lwidth > rwidth      => ConvOp.Trunc
          case (Ty.I(_), Ty.F(_))   => ConvOp.Sitofp
          case (Ty.F(_), Ty.I(_))   => ConvOp.Fptosi
          case (Ty.F64, Ty.F32)     => ConvOp.Fptrunc
          case (Ty.F32, Ty.F64)     => ConvOp.Fpext
          case (Ty.Null, _)         => ConvOp.Cast
        }
        val expr = E.Conv(op, value, toty)
        val n = fresh()

        B(List(I.Assign(n, expr)), Tn.Out(n))
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

    def genApplyTypeApply(app: Apply) = {
      val Apply(TypeApply(fun @ Select(obj, _), targs), _) = app
      val ty = genType(targs.head.tpe)

      genExpr(obj).merge { v =>
        val expr = fun.symbol match {
          case Object_isInstanceOf => E.Is(v, ty)
          case Object_asInstanceOf => E.Conv(ConvOp.Cast, v, ty)
        }
        val n = fresh()

        B(List(I.Assign(n, expr)),
          Tn.Out(n))
      }
    }

    def genApplySuper(app: Apply) = {
      val Apply(fun @ Select(sup, _), args) = app
      val method = encodeFullDefName(fun.symbol)
      val n = fresh()

      B.chain(args.map(genExpr)) { values =>
        B(List(I.Assign(n, E.Call(method, currentThis.get +: values))),
          Tn.Out(n))
      }
    }

    def genApplyNew(app: Apply) = {
      val Apply(fun @ Select(New(tpt), nme.CONSTRUCTOR), args) = app
      val ctor = fun.symbol
      val kind = genKind(tpt.tpe)
      val ty   = toIRType(kind)

      kind match {
        case _: ArrayKind =>
          genNewArray(ty, args.head)
        case ckind: ClassKind =>
          genNew(ckind.name, ctor, args)
        case ty =>
          abort("unexpected new: " + app + "\ngen type: " + ty)
      }
    }

    def genNewArray(ty: ir.Type, length: Tree) = {
      val Ty.Slice(elemty) = ty
      val n = fresh()

      genExpr(length).merge { v =>
        B(List(I.Assign(n, E.Alloc(elemty, Some(v)))),
          Tn.Out(V.Slice(n, v)))
      }
    }

    def genNew(cname: ir.Name, ctorsym: Symbol, args: List[Tree]) =
      B.chain(args.map(genExpr)) { values =>
        val ctor = N.Nested(cname, encodeDefName(ctorsym))
        val n    = fresh()

        B(List(I.Assign(n, E.Alloc(Ty.Named(cname))),
               E.Call(ctor, n +: values)),
          Tn.Out(n))
      }

    def genNormalApply(app: Apply) = {
      val Apply(fun @ Select(receiver, _), args) = app

      genExpr(receiver).merge { rvalue =>
        B.chain(args.map(genExpr)) { argvalues =>
          genMethodCall(fun.symbol, rvalue, argvalues)
        }
      }
    }

    def genMethodCall(sym: Symbol, self: ir.Val, args: Seq[ir.Val]) = {
      val mname = N.Nested(encodeClassName(sym.owner),
                           encodeDefName(sym))
      val n     = fresh()

      B(List(I.Assign(n, E.Call(mname, self +: args))),
        Tn.Out(n))
    }

    def genStaticMember(sym: Symbol) = {
      val cls    = encodeClassName(sym.owner)
      val method = encodeDefName(sym)
      val n      = fresh()

      B(List(I.Assign(n, E.Call(N.Nested(cls, method), Nil))),
        Tn.Out(n))
    }
  }
}


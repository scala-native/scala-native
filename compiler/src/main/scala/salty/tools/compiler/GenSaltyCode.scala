package salty.tools
package compiler

import scala.collection.{mutable => mut}
import scala.tools.nsc._
import scala.tools.nsc.plugins._
import scala.util.{Either, Left, Right}
import salty.ir
import salty.ir.{Type => Ty, Node => N, Defn => D, Meta => M, Name => Nm, Op}
import salty.ir.Combinators._
import salty.util, util.sh

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
    val env = mut.Map.empty[Symbol, ir.Node]
    def enter(sym: Symbol, node: ir.Node): ir.Node = {
      env += ((sym, node))
      node
    }
    def resolve(sym: Symbol): ir.Node = env(sym)
  }

  class LabelEnv(env: Env) {
    def enterLabel(label: LabelDef): ir.Node = ???
    def enterLabelCall(sym: Symbol, values: Seq[ir.Node], from: ir.Node): Unit = ???
    def resolveLabel(sym: Symbol): ir.Node = ???
    def resolveLabelParams(sym: Symbol): List[ir.Node] = ???
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
    val currentThis      = new util.ScopedVar[ir.Node]

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

    def genClass(cd: ClassDef): ir.Scope = util.ScopedVar.withScopedVars (
      currentClassSym := cd.symbol
    ) {
      val sym     = cd.symbol
      val name    = getClassName(sym)
      val parent  = getClassDefn(sym.superClass)
      val ifaces  = genClassInterfaces(sym)
      val fields  = genClassFields(sym).toSeq
      val methods = genClassMethods(cd.impl.body)
      val owner   =
        if (sym.isModuleClass)
          name -> D.Module(parent, ifaces)
        else if (sym.isInterface)
          name -> D.Interface(ifaces)
        else
          name -> D.Class(parent, ifaces)

      ir.Scope(Map(((owner +: fields) ++ methods): _*))
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol
        if psym.isInterface
      } yield {
        getClassDefn(psym)
      }

    def genClassMethods(stats: List[Tree]): List[(ir.Name, ir.Defn)] =
      stats.flatMap {
        case dd: DefDef =>
          try { List(genDef(dd)) }
          catch { case e => println(s"failed ${dd.symbol} due to $e"); Nil }
        case _          => Nil
      }

    def genClassFields(sym: Symbol) = {
      val owner = getClassDefn(sym)
      for {
        f <- sym.info.decls
        if !f.isMethod && f.isTerm && !f.isModule
      } yield {
        getFieldName(f) -> D.Field(genType(f.tpe), Seq(M.Belongs(owner)))
      }
    }

    def genDef(dd: DefDef): (ir.Name, ir.Defn) = util.ScopedVar.withScopedVars (
      currentMethodSym := dd.symbol
    ) {
      val sym = dd.symbol
      val name = getDefName(sym)
      val paramSyms = defParamSymbols(dd)
      val ty =
        if (dd.symbol.isClassConstructor) Ty.Unit
        else genType(sym.tpe.resultType)
      val meta = Seq(M.Belongs(getClassDefn(sym)))

      if (dd.symbol.isDeferred) {
        val params = genParams(paramSyms, define = false)
        name -> D.Declare(params, ty, meta)
      } else {
        val env = new Env
        util.ScopedVar.withScopedVars (
          currentEnv := env,
          currentLabelEnv := new LabelEnv(env),
          currentLocalInfo := (new CollectLocalInfo).collect(dd.rhs)
        ) {
          val params = genParams(paramSyms, define = true)
          val body = genDefBody(dd.rhs, params)
          name -> D.Define(params, body, meta)
        }
      }
    }

    def defParamSymbols(dd: DefDef): List[Symbol] = {
      val vp = dd.vparamss
      if (vp.isEmpty) Nil else vp.head.map(_.symbol)
    }

    def genParams(paramSyms: List[Symbol], define: Boolean): List[ir.Node] = {
      val self = N.In(Ty.Of(getClassDefn(currentClassSym)))
      val params = paramSyms.map { sym =>
        val node = N.In(genType(sym.tpe))
        if (define)
          currentEnv.enter(sym, node)
        node
      }

      self +: params
    }

    def genDefBody(body: Tree, params: List[ir.Node]): ir.Node = genExpr(body)

    /* (body match {
      case Block(List(ValDef(_, nme.THIS, _, _)),
                 label @ LabelDef(name, Ident(nme.THIS) :: _, rhs)) =>
        val entry = B(Tn.Undefined)
        val values = paramValues.take(label.params.length)
        currentLabelEnv.enterLabel(label)
        currentLabelEnv.enterLabelCall(label.symbol, values, entry)
        val block =
          util.ScopedVar.withScopedVars (
            currentThis := currentLabelEnv.resolveLabelParams(label.symbol).head
          ) {
            genLabel(label)
          }
        entry.termn = Tn.Jump(block)
        entry
      case _ =>
        util.ScopedVar.withScopedVars (
          currentThis := paramValues.head
        ) {
          genExpr(body)
        }
    }).simplify.verify*/

    def genExpr(tree: Tree): ir.Node = tree match {
      case label: LabelDef =>
        currentLabelEnv.enterLabel(label)
        genLabel(label)

      case vd: ValDef =>
        val rhs = genExpr(vd.rhs).merge
        currentEnv.enter(vd.symbol, rhs)
        val isMutable = currentLocalInfo.mutableVars.contains(vd.symbol)
        if (!isMutable)
          N.Out(N.Unit, effects = Seq(rhs))
        else {
          val alloc = N.Alloc(genType(vd.symbol.tpe))
          val store = N.Store(alloc, rhs)
          N.Out(N.Unit, effects = Seq(store))
        }

      case If(cond, thenp, elsep) =>
        ???

      case Return(expr) =>
        N.Return(genExpr(expr))

      case Try(expr, catches, finalizer) if catches.isEmpty && finalizer.isEmpty =>
        genExpr(expr)

      case Try(expr, catches, finalizer) =>
        ???

      case Throw(expr) =>
        N.Throw(genExpr(expr))

      case app: Apply =>
        genApply(app)

      case app: ApplyDynamic =>
        genApplyDynamic(app)

      case This(qual) =>
        N.Out(
          if (tree.symbol == currentClassSym.get) currentThis
          else N.Of(getClassDefn(tree.symbol)))

      case Select(qual, sel) =>
        val sym = tree.symbol
        if (sym.isModule)
          N.Out(N.Of(getClassDefn(sym)))
        else if (sym.isStaticMember)
          genStaticMember(sym)
        else
          N.Out(N.Load(N.Elem(genExpr(qual), N.Of(getFieldDefn(tree.symbol)))))

      case id: Ident =>
        val sym = id.symbol
        if (!currentLocalInfo.mutableVars.contains(sym))
          N.Out(
            if (sym.isModule) N.Of(getClassDefn(sym))
            else currentEnv.resolve(sym))
        else
          N.Out(N.Load(currentEnv.resolve(sym)))

      case lit: Literal =>
        genValue(lit)

      case block: Block =>
        genBlock(block)

      case Typed(Super(_, _), _) =>
        N.Out(currentThis)

      case Typed(expr, _) =>
        genExpr(expr)

      case Assign(lhs, rhs) =>
        lhs match {
          case sel @ Select(qual, _) =>
            val qualNode = genExpr(qual)
            val rhsNode = genExpr(rhs)
            val elemNode = N.Elem(qualNode, N.Of(getFieldDefn(sel.symbol)))
            val storeNode = N.Store(elemNode, rhsNode)
            N.Out(N.Unit, effects = Seq(storeNode))

          case id: Ident =>
            val store = N.Store(currentEnv.resolve(id.symbol), genExpr(rhs))
            N.Out(N.Unit, effects = Seq(store))
        }

      case av: ArrayValue =>
        genArrayValue(av)

      case m: Match =>
        genSwitch(m)

      case fun: Function =>
        N.Undefined

      case EmptyTree =>
        N.Out(N.Unit)

      case _ =>
        abort("Unexpected tree in genExpr: " +
              tree + "/" + tree.getClass + " at: " + tree.pos)
    }

    def genValue(lit: Literal): ir.Node = {
      val value = lit.value
      value.tag match {
        case NullTag =>
          (N.Null)
        case UnitTag =>
          (N.Unit)
        case BooleanTag =>
          (if (value.booleanValue) N.True else N.False)
        case ByteTag =>
          (N.I8(value.intValue.toByte))
        case ShortTag | CharTag =>
          (N.I16(value.intValue.toShort))
        case IntTag =>
          (N.I32(value.intValue))
        case LongTag =>
          (N.I64(value.longValue))
        case FloatTag =>
          (N.F32(value.floatValue))
        case DoubleTag =>
          (N.F64(value.doubleValue))
        case ClazzTag =>
          (N.Class(genType(value.typeValue)))
        case StringTag =>
          (N.Str(value.stringValue))
        case EnumTag =>
          (genStaticMember(value.symbolValue))
      }
    }

    def genStaticMember(sym: Symbol) =
      N.Out(N.Of(getFieldDefn(sym)))

    def genCatch(catches: List[Tree]) = ??? /*
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
    */

    def genBlock(block: Block) = ??? /*{
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
          stats.map(genExpr).chain { vals =>
            genExpr(last)
          }
      }
    }*/

    def genMatch(prologue: List[Tree], cases: List[LabelDef], last: LabelDef) = ??? /*{
      prologue.map(genExpr).chain { _ =>
        currentLabelEnv.enterLabel(last)
        for (label <- cases) {
          currentLabelEnv.enterLabel(label)
        }
        genLabel(last)
        cases.map(genLabel).head
      }
    }*/

    def genLabel(label: LabelDef) = ??? /*{
      val entry = currentLabelEnv.resolveLabel(label.symbol)
      val target = genExpr(label.rhs)
      entry.termn = Tn.Jump(target)
      entry
    }*/


    // TODO: insert coercions
    def genArrayValue(av: ArrayValue) = ??? /*{
      val ArrayValue(tpt, elems) = av

      elems.map(genExpr).chain { values =>
        val n      = fresh()
        val ty     = genType(tpt.tpe)
        val len    = values.length
        val stores = values.zipWithIndex.map {
          case (v, i) =>
            E.Store(V.Elem(n, V(i)), v)
        }

        B(I.Assign(n, E.Alloc(ty, Some(V(len)))) +:
          stores,
          Tn.Out(n))
      }
    }*/

    def genSwitch(m: Match) = ??? /*{
      val Match(sel, cases) = m

      genExpr(sel).merge { selvalue =>
        val defaultBody =
          cases.collectFirst {
            case c @ CaseDef(Ident(nme.WILDCARD), _, body) => body
          }.get
        val defaultBlock = genExpr(defaultBody)
        val branches = cases.flatMap {
          case CaseDef(Ident(nme.WILDCARD), _, _) =>
            Nil
          case CaseDef(pat, guard, body) =>
            val bodyBlock = genExpr(body)
            val guardedBlock =
              if (guard.isEmpty) bodyBlock
              else
                genExpr(guard).merge { gv =>
                  B(Tn.If(gv, bodyBlock, defaultBlock))
                }
            val values: List[ir.Node] =
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
    }*/

    def genApplyDynamic(app: ApplyDynamic) = N.Undefined

    def genApply(app: Apply): ir.Node = {
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

    def genLabelApply(tree: Tree) = ??? /*tree match {
      case Apply(fun, Nil) =>
        currentLabelEnv.resolveLabel(fun.symbol)
      case Apply(fun, args) =>
        args.map(genExpr).chain { vals =>
          val block = B(Tn.Jump(currentLabelEnv.resolveLabel(fun.symbol)))
          currentLabelEnv.enterLabelCall(fun.symbol, vals, block)
          block
        }
    }*/

    lazy val primitive2box = Map(
      BooleanTpe -> Ty.Of(D.Extern(Nm.Global("java.lang.Boolean"))),
      ByteTpe    -> Ty.Of(D.Extern(Nm.Global("java.lang.Byte"))),
      CharTpe    -> Ty.Of(D.Extern(Nm.Global("java.lang.Character"))),
      ShortTpe   -> Ty.Of(D.Extern(Nm.Global("java.lang.Short"))),
      IntTpe     -> Ty.Of(D.Extern(Nm.Global("java.lang.Integer"))),
      LongTpe    -> Ty.Of(D.Extern(Nm.Global("java.lang.Long"))),
      FloatTpe   -> Ty.Of(D.Extern(Nm.Global("java.lang.Float"))),
      DoubleTpe  -> Ty.Of(D.Extern(Nm.Global("java.lang.Double")))
    )

    lazy val ctorName = Nm.Global(nme.CONSTRUCTOR.toString)

    def makePrimitiveBox(expr: Tree, tpe: Type) = ??? /*
      genExpr(expr).merge { v =>
        val name = fresh()
        B(List(I.Assign(name, E.Box(v, primitive2box(tpe.widen)))),
          Tn.Out(name))
      }
      */

    def makePrimitiveUnbox(expr: Tree, tpe: Type) = ??? /*
      genExpr(expr).merge { v =>
        val name = fresh()
        B(List(I.Assign(name, E.Unbox(v, primitive2box(tpe.widen)))),
          Tn.Out(name))
      }
      */

    def genPrimitiveOp(app: Apply): ir.Node = {
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

    def genSimpleOp(app: Apply, args: List[Tree], code: Int): ir.Node = {
      import scalaPrimitives._

      val retty = genType(app.tpe)

      args match {
        case List(right) =>
          val rblock = genExpr(right)
          def unary(op: Op, lvalue: ir.Node) =
            N.Out(N.Bin(op, lvalue, rblock.merge))
          code match {
            case POS  => rblock
            case NEG  => ??? // unary(Op.Sub, V.Number("0", retty))
            case NOT  => ??? // unary(Op.Xor, V.Number("-1", retty))
            case ZNOT => unary(Op.Xor, N.True)
            case _ =>
              abort("Unknown unary operation code: " + code)
          }

        // TODO: convert to the common type
        // TODO: eq, ne
        case List(left, right) =>
          val lnode = genExpr(left)
          val rnode = genExpr(right)
          val lty   = genType(left.tpe)
          val rty   = genType(right.tpe)
          def bin(op: Op, ty: ir.Type) = {
            val lcoerced = genCoercion(lnode.merge, lty, ty).merge
            val rcoerced = genCoercion(rnode.merge, rty, ty).merge
            N.Out(N.Bin(op, lcoerced, rcoerced))
          }
          def equality(negated: Boolean) = ??? /*
            genKind(left.tpe) match {
              case ClassKind(_) =>
                lblock.chain(rblock) { (lvalue, rvalue) =>
                  val classEq = genClassEquality(lvalue, rvalue)
                  if (!negated)
                    classEq
                  else
                    classEq.merge { v =>
                      val n = fresh()
                      B(List(I.Assign(n, E.Bin(Op.Xor, V(true), v))),
                        Tn.Out(n))
                    }
                }
              case kind =>
                val op = if (negated) Op.Neq else Op.Eq
                bin(Op.Eq, binaryOperationType(lty, rty))
            }
          */
          def referenceEquality(negated: Boolean) = ??? /*{
            val op = if (negated) Op.Neq else Op.Eq
            val n = fresh()
            lblock.chain(rblock) { (lvalue, rvalue) =>
              B(List(I.Assign(n, E.Bin(op, lvalue, rvalue))),
                Tn.Out(n))
            }
          } */
          code match {
            // arithmetic & bitwise
            case ADD  => bin(Op.Add,  retty)
            case SUB  => bin(Op.Sub,  retty)
            case MUL  => bin(Op.Mul,  retty)
            case DIV  => bin(Op.Div,  retty)
            case MOD  => bin(Op.Mod,  retty)
            case OR   => bin(Op.Or,   retty)
            case XOR  => bin(Op.Xor,  retty)
            case AND  => bin(Op.And,  retty)
            case LSL  => bin(Op.Shl,  retty)
            case LSR  => bin(Op.Lshr, retty)
            case ASR  => bin(Op.Ashr, retty)
            // comparison
            case LT   => bin(Op.Lt,  binaryOperationType(lty, rty))
            case LE   => bin(Op.Lte, binaryOperationType(lty, rty))
            case GT   => bin(Op.Gt,  binaryOperationType(lty, rty))
            case GE   => bin(Op.Gte, binaryOperationType(lty, rty))
            // equality
            case EQ   => equality(negated = false)
            case NE   => equality(negated = true)
            case ID   => referenceEquality(negated = false)
            case NI   => referenceEquality(negated = true)
            // logical
            case ZOR  =>
              ???
              /*lblock.merge { lvalue =>
                B(Tn.If(lvalue, B(Tn.Out(V(true))), rblock))
              }*/
            case ZAND =>
              ???
              /*lblock.merge { lvalue =>
                B(Tn.If(lvalue, rblock, B(Tn.Out(V(false)))))
              }*/
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

    def genClassEquality(lvalue: ir.Node, rvalue: ir.Node): ir.Node = ??? /* {
      import scalaPrimitives._

      val n = fresh()

      rvalue match {
        case V.Null =>
          B(List(I.Assign(n, E.Bin(Op.Eq, lvalue, V.Null))),
            Tn.Out(n))
        case _ =>
          B(List(I.Assign(n, E.Bin(Op.Equals, lvalue, rvalue))),
            Tn.Out(n))
      }
    }*/

    def genStringConcat(tree: Tree, receiver: Tree, args: List[Tree]) = ??? /*
      genExpr(receiver).chain(genExpr(args.head)) { (l, r) =>
        val n = fresh()

        B(List(I.Assign(n, E.Bin(Op.Add, l, r))),
          Tn.Out(n))
      }
    */

    def genHash(tree: Tree, receiver: Tree): ir.Node = ??? /*{
      val cls    = ScalaRunTimeModule
      val method = getMember(cls, nme.hash_)

      genExpr(receiver).merge { v =>
        genMethodCall(method, v, List())
      }
    }*/

    def genArrayOp(app: Apply, code: Int): ir.Node = ??? /*{
      import scalaPrimitives._

      val Apply(Select(array, _), args) = app
      val blocks = (array :: args).map(genExpr)

      blocks.chain { values =>
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
    }*/

    // TODO: re-evaluate dropping sychcronized
    // TODO: NPE
    def genSynchronized(app: Apply): ir.Node = ??? /*{
      val Apply(Select(receiver, _), List(arg)) = app
      genExpr(receiver).chain(genExpr(arg)) { (v1, v2) =>
        B(Tn.Out(v2))
      }
    }*/

    def genCoercion(app: Apply, receiver: Tree, code: Int): ir.Node = {
      val nreceiver = genExpr(receiver)
      val (fromty, toty) = coercionTypes(code)

      genCoercion(nreceiver.merge, fromty, toty)
    }

    def genCoercion(value: ir.Node, fromty: ir.Type, toty: ir.Type): ir.Node =
      if (fromty == toty)
        value
      else {
        val op = (fromty, toty) match {
          case (Ty.I(lwidth), Ty.I(rwidth))
            if lwidth < rwidth      => Op.Zext
          case (Ty.I(lwidth), Ty.I(rwidth))
            if lwidth > rwidth      => Op.Trunc
          case (Ty.I(_), Ty.F(_))   => Op.Sitofp
          case (Ty.F(_), Ty.I(_))   => Op.Fptosi
          case (Ty.F64, Ty.F32)     => Op.Fptrunc
          case (Ty.F32, Ty.F64)     => Op.Fpext
          case (Ty.Null, _)         => Op.Cast
        }
        N.Conv(op, value, toty)
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

    def genApplyTypeApply(app: Apply): ir.Node = ??? /* {
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
    }*/

    def genApplySuper(app: Apply): ir.Node = ??? /* {
      val Apply(fun @ Select(sup, _), args) = app
      val stat = getDefDefn(fun.symbol)
      val n    = fresh()

      args.map(genExpr).chain { values =>
        B(List(I.Assign(n, E.Call(stat, currentThis.get +: values))),
          Tn.Out(n))
      }
    }*/

    def genApplyNew(app: Apply): ir.Node = ??? /*{
      val Apply(fun @ Select(New(tpt), nme.CONSTRUCTOR), args) = app
      val ctor = fun.symbol
      val kind = genKind(tpt.tpe)
      val ty   = toIRType(kind)

      kind match {
        case _: ArrayKind =>
          genNewArray(ty, args.head)
        case ckind: ClassKind =>
          genNew(ckind.sym, ctor, args)
        case ty =>
          abort("unexpected new: " + app + "\ngen type: " + ty)
      }
    }*/

    def genNewArray(ty: ir.Type, length: Tree): ir.Node = ??? /*{
      val Ty.Slice(elemty) = ty
      val n = fresh()

      genExpr(length).merge { v =>
        B(List(I.Assign(n, E.Alloc(elemty, Some(v)))),
          Tn.Out(n))
      }
    }*/

    def genNew(sym: Symbol, ctorsym: Symbol, args: List[Tree]): ir.Node = ??? /*
      args.map(genExpr).chain { values =>
        val stat = getClassDefn(sym)
        val ctor = getDefDefn(ctorsym)
        val n    = fresh()

        B(List(I.Assign(n, E.Alloc(Ty.Of(stat))),
               E.Call(ctor, n +: values)),
          Tn.Out(n))
      }
    */

    def genNormalApply(app: Apply): ir.Node = ??? /*{
      val Apply(fun @ Select(receiver, _), args) = app

      genExpr(receiver).merge { rvalue =>
        args.map(genExpr).chain { argvalues =>
          genMethodCall(fun.symbol, rvalue, argvalues)
        }
      }
    }*/

    def genMethodCall(sym: Symbol, self: ir.Node, args: Seq[ir.Node]): ir.Node = ??? /*{
      val stat = getDefDefn(sym)
      val n    = fresh()

      B(List(I.Assign(n, E.Call(stat, self +: args))),
        Tn.Out(n))
    }*/
  }
}


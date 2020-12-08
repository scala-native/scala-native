package scala.scalanative.nscplugin.check

import scala.annotation.tailrec
import scala.reflect.internal.Flags.PARAMACCESSOR
import scala.scalanative.nir
import scala.scalanative.nir._
import scala.scalanative.nscplugin.NirPrimitives._
import scala.scalanative.util.ScopedVar.scoped
import scala.tools.nsc

trait NirCheckExpr[G <: nsc.Global with Singleton] {
  self: PreNirSanityCheck[G] =>

  import SimpleType.{fromSymbol, fromType}
  import global._
  import nirAddons._

  import definitions._
  import scalaPrimitives._

  import nirDefinitions._
  import nirPrimitives._
  import treeInfo.hasSynthCaseSymbol

  def checkExpr(tree: Tree): Unit = tree match {
    case EmptyTree          => Ok
    case tree: Block        => checkBlock(tree)
    case tree: LabelDef     => checkLabel(tree)
    case tree: ValDef       => checkValDef(tree)
    case tree: DefDef       => checkMethod(tree)
    case tree: If           => checkIf(tree)
    case tree: Match        => checkMatch(tree)
    case tree: Try          => checkTry(tree)
    case tree: Throw        => checkThrow(tree)
    case tree: Return       => checkReturn(tree)
    case tree: ArrayValue   => checkArrayValue(tree)
    case _: Literal         => Ok
    case _: This            => Ok
    case _: Super           => Ok
    case _: Ident           => Ok
    case tree: Select       => checkSelect(tree)
    case tree: Assign       => checkAssign(tree)
    case tree: Typed        => checkTyped(tree)
    case tree: Function     => checkFunction(tree)
    case tree: ApplyDynamic => checkApplyDynamic(tree)
    case tree: Apply        => checkApply(tree)
    case tree: TypeApply    => checkTypeApply(tree)
    case tree: TypeDef      => checkTypeDef(tree)
    case tree: UnApply      => checkUnapply(tree)
    case _: TypeTree        => Ok
    case _: Import          => Ok
    case Bind(_, body)      => checkExpr(body)
    case Alternative(alts)  => alts.foreach(checkExpr)
    case tree: ImplDef      => checkClassOrModule(tree)
    case _ =>
      reporter.error(tree.pos, s"Unexpected tree: $tree/${tree.getClass}")
  }

  def checkBlock(block: Block): Unit = {
    val Block(stats, last) = block

    def isCaseLabelDef(tree: Tree) =
      tree.isInstanceOf[LabelDef] && hasSynthCaseSymbol(tree)

    def checkMatch(last: LabelDef) = {
      stats.foreach {
        case t if !isCaseLabelDef(t) => checkExpr(t)
        case label: LabelDef =>
          checkLabel(label)
          checkLabel(last)
        case _ => Ok
      }
    }

    last match {
      case label: LabelDef if isCaseLabelDef(label) =>
        checkMatch(label)

      case Apply(TypeApply(Select(label: LabelDef, nme.asInstanceOf_Ob), _), _)
          if isCaseLabelDef(label) =>
        checkMatch(label)

      case _ =>
        stats.foreach(checkExpr)
        checkExpr(last)
    }
  }

  def checkLabelDef(label: LabelDef): Unit = checkLabel(label)

  def checkLabel(label: LabelDef): Unit = checkExpr(label.rhs)

  def checkTailRecLabel(label: LabelDef): Unit = checkExpr(label.rhs)

  def checkValDef(vd: ValDef): Unit =
    scoped(
      curValSym := vd.symbol
    ) {
      if (curClassSym.get.isExtern) {
        checkExternVal(vd)
      } else checkExpr(vd.rhs)
    }

  def checkExternVal(vd: ValDef): Unit = {
    if (curValSym.isLazy) {
      reporter.error(
        vd.pos,
        s"fields in extern ${symToKindPlural(curClassSym)} must not be lazy")
    }

    if (curValSym.hasFlag(PARAMACCESSOR)) {
      reporter.error(
        vd.pos,
        s"parameters in extern ${symToKindPlural(curClassSym)} are not allowed - only extern fields and methods are allowed")
    }

    vd.rhs match {
      case sel: Select if sel.symbol == ExternMethod =>
        checkExternMemberHasTpeAnnotation(vd)

      case _ =>
        reporter.error(
          vd.pos,
          s"fields in extern ${symToKindPlural(curClassSym)} must have extern body")
    }
  }

  def checkIf(tree: If): Unit = {
    val If(cond, thenp, elsep) = tree
    checkIf(cond, thenp, elsep)
  }

  def checkIf(condp: Tree, thenp: Tree, elsep: Tree): Unit = {
    checkExpr(condp)
    checkExpr(thenp)
    checkExpr(elsep)
  }

  def checkMatch(m: Match): Unit = {
    val Match(scrutp, allcaseps) = m

    checkExpr(scrutp)
    allcaseps.foreach {
      case CaseDef(pat, guard, body) =>
        checkExpr(pat)
        checkExpr(guard)
        checkExpr(body)
    }
  }

  def checkTry(tree: Try): Unit = tree match {
    case Try(expr, catches, finalizer)
        if catches.isEmpty && finalizer.isEmpty =>
      checkExpr(expr)
    case Try(expr, catches, finalizer) =>
      checkTry(expr, catches, finalizer)
  }

  def checkTry(expr: Tree, catches: List[Tree], finallyp: Tree): Unit = {
    checkExpr(expr)
    catches.foreach {
      case CaseDef(_, guard, body) =>
        checkExpr(guard)
        checkExpr(body)
    }
    checkExpr(finallyp)
  }

  def checkThrow(tree: Throw): Unit = checkExpr(tree.expr)

  def checkReturn(tree: Return): Unit = checkExpr(tree.expr)

  def checkArrayValue(av: ArrayValue): Unit = {
    val ArrayValue(_, elems) = av
    elems.foreach(checkExpr)
  }

  def checkSelect(tree: Select): Unit = {
    val Select(qualp, _) = tree
    checkExpr(qualp)
  }

  def checkAssign(tree: Assign): Unit = {
    val Assign(lhsp, rhsp) = tree

    checkExpr(rhsp)
    lhsp match {
      case Select(qualp, _) => checkExpr(qualp)
      case _: Ident         => Ok
    }
  }

  def checkTyped(tree: Typed): Unit = tree match {
    case Typed(Super(_, _), _) => Ok
    case Typed(expr, _) =>
      checkExpr(expr)
  }

  def checkTypeDef(tree: TypeDef): Unit = {
    checkExpr(tree.rhs)
  }

  def checkTypeApply(tree: TypeApply): Unit = {
    val TypeApply(fun, args) = tree
    checkExpr(fun)
    args.foreach(checkExpr)
  }

  def checkFunction(tree: Function): Unit = {
    val Function(vparams, body) = tree
    checkExpr(body)
  }

  def checkApplyDynamic(app: ApplyDynamic): Unit = {
    val ApplyDynamic(obj, args) = app
    checkExpr(obj)
    args.foreach(checkExpr)
  }

  def checkApply(app: Apply): Unit = {
    val Apply(fun, args) = app

    fun match {
      case _: TypeApply => checkApplyTypeApply(app)
      case Select(New(_), nme.CONSTRUCTOR) =>
        checkApplyNew(app)
      case Select(fun, _) =>
        args.foreach(checkExpr)
      case _ =>
        val sym = fun.symbol

        if (isNirPrimitive(sym)) checkApplyNirPrimitive(app)
        if (isPrimitive(sym)) checkApplyPrimitive(app)
        else args.foreach(checkExpr)
    }
  }

  def checkApplyPrimitive(app: Apply): Unit = {
    val sym = app.symbol

    val Apply(fun @ Select(receiver, _), args) = app
    val code                                   = getPrimitive(sym, receiver.tpe)

    if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code)) {
      checkSimpleOp(app, receiver :: args, code)
    } else if (code == CONCAT) {
      checkStringConcat(receiver, args.head)
    } else if (code == HASH) {
      checkHashCode(args.head)
    } else if (isArrayOp(code)) {
      checkArrayOp(app, code)
    } else if (isCoercion(code)) {
      checkCoercion(app, receiver, code)
    } else if (code == SYNCHRONIZED) {
      val Apply(Select(receiverp, _), List(argp)) = app
      checkSynchronized(receiverp, argp)
    } else {
      reporter.error(
        app.pos,
        s"Unknown Scala primitive operation: ${sym.fullName} (${fun.symbol.simpleName})")
    }
  }

  def checkApplyNirPrimitive(app: Apply): Unit = {
    val Apply(fun, args) = app
    val sym              = app.symbol
    val code             = getNirPrimitive(sym)

    if (code == ARRAY_CLONE) checkArrayOp(app, code)
    else if (nirPrimitives.isRawPtrOp(code)) checkRawPtrOp(app, code)
    else if (nirPrimitives.isRawCastOp(code)) checkRawCastOp(app, code)
    else if (code == CFUNCPTR_APPLY) checkCFuncPtrApply(app, code)
    else if (code == CFUNCPTR_FROM_FUNCTION) checkCFuncFromScalaFunction(app)
    else if (code == STACKALLOC) checkStackalloc(app)
    else if (code == CQUOTE) Ok
    else if (code == BOXED_UNIT) Ok
    else if (code >= DIV_UINT && code <= ULONG_TO_DOUBLE)
      checkUnsignedOp(app, code)
    else {
      reporter.error(
        app.pos,
        s"Unknown primitive operation: ${sym.fullName} (${fun.symbol.simpleName})")
    }
  }

  def checkCFuncFromScalaFunction(app: Apply): Unit = {
    def checkResolvableClousure(tree: Tree): Unit = {
      freeLocalVars(tree).foreach { freeSym =>
        println(freeSym)
        reporter.error(
          freeSym.pos,
          s"can't infer a function pointer to a closure with captures: $freeSym")
      }
    }

    @tailrec
    def resolveFunction(tree: Tree): Unit = {
      tree match {
        case Block(stats, expr) =>
          stats.foreach { s =>
            println(s.getClass.getName)
            println(s)
            checkResolvableClousure(s)
          }
          resolveFunction(expr)

        case Function(_, _) => // Scala 2.12+
          checkResolvableClousure(tree)

        case Ident(_) => //
          reporter.error(tree.pos, "Cannot lift value into CFuncPtr")

        case fn: Apply => // Scala 2.11 only
          println(fn)
          val alternatives = fn.tpe
            .member(nme.apply)
            .alternatives

          alternatives
            .find { sym =>
              sym.tpe != ObjectTpe ||
              sym.tpe.params.exists(_.tpe != ObjectTpe)
            }
            .orElse(alternatives.headOption)
            .getOrElse(reporter
              .error(tree.pos, s"not found any apply method in ${fn.tpe}"))
          checkExpr(tree)

        case last =>
          reporter.error(tree.pos,
                         "Failed to resolve function ref for extern forwarder ")
      }
    }

    //    Apply(
    //      Apply(
    //        TypeApply(
    //          Select(Select(Select(Select(Ident(scala), scala.scalanative), scala.scalanative.unsafe), scala.scalanative.unsafe.CFuncPtr1), TermName("fromScalaFunction")),
    //          List(TypeTree().setOriginal(Select(Select(Select(Select(Ident(scala), scala.scalanative), scala.scalanative.unsafe), scala.scalanative.unsafe.package), TypeName("CInt"))), TypeTree().setOriginal(Select(Ident(scala), scala.Unit)))
    //        ),
    //        List(Function(List(ValDef(Modifiers(PARAM), TermName("x$1"), TypeTree(), EmptyTree)), Literal(Constant(()))))
    //      ), List(Select(Select(This(TypeName("unsafe")), scala.scalanative.unsafe.Tag), TermName("materializeIntTag")), Select(Select(This(TypeName("unsafe")), scala.scalanative.unsafe.Tag), TermName("materializeUnitTag")))
    //    )

    app match {
      case Apply(Apply(_: TypeApply, Seq(fn)), evidences) =>
        fn match {
          // lifted method to function. We cannot assert method is valid
          case Function(_, Apply(_, _))               => Ok //2.13
          case Block(Seq(), Function(_, Apply(_, _))) => Ok //2.12
          case _                                      => resolveFunction(fn)
        }
      case _ =>
        reporter.error(
          app.pos,
          s"Unknown CFuncPtr.fromScalaFunction tree: ${showRaw(app)}")
    }
  }

  def checkSimpleOp(app: Apply, args: List[Tree], code: Int): Unit = {
    assert(args.nonEmpty, "empty arguments list")

    args.foreach(checkExpr)
    if (args.size > 2) {
      reporter.error(app.pos, s"Too many arguments for primitive function")
    }
  }

  def checkBinaryOp(op: (nir.Type, Val, Val) => Op,
                    leftp: Tree,
                    rightp: Tree,
                    opty: nir.Type): Unit = {
    checkExpr(leftp)
    checkExpr(rightp)
  }

  def checkClassEquality(leftp: Tree,
                         rightp: Tree,
                         ref: Boolean,
                         negated: Boolean): Unit = {
    checkExpr(leftp)
    checkExpr(rightp)
  }

  val referenceOpCodes  = Set(EQ, ID, NE, NI)
  val baseNumberOpCodes = Set(ADD, SUB, MUL, DIV, MOD, EQ, NE, LT, LE, GT, GE)
  val integerOpCodes =
    baseNumberOpCodes ++ Set(OR, XOR, AND, LSL, LSR, ASR, ZOR, ZAND)
  def checkBinaryOp(code: Int,
                    left: Tree,
                    right: Tree,
                    retty: nir.Type): Unit = {
    import scalaPrimitives._
    val pos = left.pos.focusEnd

    val lty = genType(left.tpe)
    val rty = genType(right.tpe)
    if (isShiftOp(code)) Ok
    else checkBinaryOperationType(lty, rty)(pos)
  }

  def checkBinaryOperationType(lty: nir.Type, rty: nir.Type)(pos: Position) =
    (lty, rty) match {
      // Bug compatibility with scala/bug/issues/11253
      case (Type.Long, Type.Float)             => Ok
      case (nir.Type.Ptr, _: nir.Type.RefKind) => Ok
      case (_: nir.Type.RefKind, nir.Type.Ptr) => Ok
      case (nir.Type.Bool, nir.Type.Bool)      => Ok
      case (nir.Type.I(lwidth, _), nir.Type.I(rwidth, _))
          if lwidth < 32 && rwidth < 32 =>
        Ok
      case (nir.Type.I(lwidth, _), nir.Type.I(rwidth, _)) => Ok
      case (nir.Type.I(_, _), nir.Type.F(_))              => Ok
      case (nir.Type.F(_), nir.Type.I(_, _))              => Ok
      case (nir.Type.F(lwidth), nir.Type.F(rwidth))       => Ok
      case (_: nir.Type.RefKind, _: nir.Type.RefKind)     => Ok
      case (ty1, ty2) if ty1 == ty2                       => Ok
      case _ =>
        reporter.error(pos,
                       s"Cannot perform binary operation between $lty and $rty")
    }

  def checkStringConcat(leftp: Tree, rightp: Tree): Unit = {
    checkExpr(leftp)
    checkExpr(rightp)
  }

  def checkHashCode(argp: Tree): Unit =
    checkExpr(argp)

  def checkArrayOp(app: Apply, code: Int): Unit = {
    val Apply(Select(arrayp, _), argsp) = app

    checkExpr(arrayp)
    argsp.foreach(checkExpr)
  }

  def checkRawPtrOp(app: Apply, code: Int): Unit = {
    if (nirPrimitives.isRawPtrOp(code) || code == ELEM_RAW_PTR) {
      app.args.foreach(checkExpr)
    } else reporter.error(app.pos, s"Unknown pointer operation #$code: $app ")
  }

  def checkRawCastOp(app: Apply, code: Int): Unit = {
    val Apply(_, Seq(argp)) = app

    checkExpr(argp)

    val fromty = genType(argp.tpe)
    val toty   = genType(app.tpe)
    checkCastOp(fromty, toty)(app.pos)
  }

  def checkCFuncPtrApply(app: Apply, code: Int): Unit = {
    val Apply(Apply(receiverp, argsp), evidences) = app

    checkExpr(receiverp)
//    println(evidences)
//    evidences.foreach { ev =>
//      println(showRaw(ev))
//      unwrapTagOption(ev).getOrElse {
//        reporter.error(ev.pos, s"Cannot recover runtime tag")
//      }
//    }

    argsp.foreach(checkExpr)
  }

  def checkCastOp(fromty: nir.Type, toty: nir.Type)(pos: Position): Unit = {
    (fromty, toty) match {
      case (_: Type.I, Type.Ptr) | (Type.Ptr, _: Type.I)               => Ok
      case (_: Type.RefKind, Type.Ptr) | (Type.Ptr, _: Type.RefKind)   => Ok
      case (_: Type.RefKind, _: Type.RefKind)                          => Ok
      case (_: Type.RefKind, _: Type.I)                                => Ok
      case (_: Type.RefKind, _: Type.I) | (_: Type.I, _: Type.RefKind) => Ok
      case (Type.I(w1, _), Type.F(w2)) if w1 == w2                     => Ok
      case (Type.F(w1), Type.I(w2, _)) if w1 == w2                     => Ok
      case _ if fromty == toty                                         => Ok
      case _ =>
        reporter.error(pos, s"Cannot cast from $fromty to $toty")
    }
  }

  def checkStackalloc(app: Apply): Unit = {
    val Apply(_, Seq(sizep)) = app
    checkExpr(sizep)
  }

  def checkUnsignedOp(app: Tree, code: Int): Unit = {
    val Apply(_, argsp) = app
    argsp.foreach(checkExpr)
  }

  def checkSynchronized(receiverp: Tree, bodyp: Tree): Unit = checkExpr(bodyp)

  def checkCoercion(app: Apply, receiver: Tree, code: Int): Unit =
    checkExpr(receiver)

  def checkApplyTypeApply(app: Apply): Unit = {
    val Apply(TypeApply(fun, targs), args) = app
    checkExpr(fun)
    targs.foreach(checkExpr)
    args.foreach(checkExpr)
  }

  def checkApplyNew(app: Apply): Unit = {
    val Apply(Select(New(_), nme.CONSTRUCTOR), args) = app
    args.foreach(checkExpr)
  }

  def checkUnapply(tree: UnApply): Unit = {
    val UnApply(fun, args) = tree
    checkExpr(fun)
    args.foreach(checkExpr)
  }

  protected def checkExternMemberHasTpeAnnotation(df: ValOrDefDef): Unit = {
    df.tpt match {
      case t @ TypeTree() if t.original == null =>
        val kind =
          if (df.symbol.isField) "fields"
          else "methods"

        reporter.error(
          df.pos,
          s"extern $kind in ${symToKindPlural(curClassSym)} must have an explicit result type")
      case _: TypeTree => Ok
    }
  }

}

package scala.scalanative
package nscplugin

import scala.annotation.{tailrec, switch}
import scala.collection.mutable
import scala.tools.nsc
import scalanative.util.{StringUtils, unsupported}
import scalanative.util.ScopedVar.scoped
import scalanative.nscplugin.NirPrimitives._

trait NirGenExpr[G <: nsc.Global with Singleton] { self: NirGenPhase[G] =>
  import global.{definitions => defn, _}
  import defn._
  import treeInfo.{hasSynthCaseSymbol, StripCast}
  import nirAddons._
  import nirDefinitions._
  import SimpleType.{fromType, fromSymbol}

  sealed case class ValTree(value: nir.Val)(
      pos: global.Position = global.NoPosition
  ) extends Tree {
    super.setPos(pos)
  }
  object ValTree {
    def apply(from: Tree)(value: nir.Val) =
      new ValTree(value = value)(pos = from.pos)
  }

  sealed case class ContTree(f: ExprBuffer => nir.Val)(
      pos: global.Position
  ) extends Tree { super.setPos(pos) }
  object ContTree {
    def apply(from: Tree)(build: ExprBuffer => nir.Val) =
      new ContTree(f = build)(
        pos = from.pos
      )
  }

  class FixupBuffer(implicit fresh: nir.Fresh) extends nir.InstructionBuilder {
    private var labeled = false

    override def +=(inst: nir.Inst): Unit = {
      implicit val pos: nir.SourcePosition = inst.pos
      inst match {
        case inst: nir.Inst.Label =>
          if (labeled) {
            unreachable(unwind)
          }
          labeled = true
        case _ =>
          if (!labeled) {
            label(fresh())
          }
          labeled = !inst.isInstanceOf[nir.Inst.Cf]
      }
      super.+=(inst)
      inst match {
        case nir.Inst.Let(_, op, _) if op.resty == nir.Type.Nothing =>
          unreachable(unwind)
          label(fresh())
        case _ =>
          ()
      }
    }

    override def ++=(insts: Seq[nir.Inst]): Unit =
      insts.foreach { inst => this += inst }

    override def ++=(other: nir.InstructionBuilder): Unit =
      this ++= other.toSeq
  }

  class ExprBuffer(implicit fresh: nir.Fresh) extends FixupBuffer { buf =>
    def genExpr(tree: Tree): nir.Val = tree match {
      case EmptyTree =>
        nir.Val.Unit
      case ValTree(value) =>
        value
      case ContTree(f) =>
        f(this)
      case tree: Block =>
        genBlock(tree)
      case tree: LabelDef =>
        genLabelDef(tree)
      case tree: ValDef =>
        genValDef(tree)
      case tree: If =>
        genIf(tree)
      case tree: Match =>
        genMatch(tree)
      case tree: Try =>
        genTry(tree)
      case tree: Throw =>
        genThrow(tree)
      case tree: Return =>
        genReturn(tree)
      case tree: Literal =>
        genLiteral(tree)
      case tree: ArrayValue =>
        genArrayValue(tree)
      case tree: This =>
        genThis(tree)
      case tree: Ident =>
        genIdent(tree)
      case tree: Select =>
        genSelect(tree)
      case tree: Assign =>
        genAssign(tree)
      case tree: Typed =>
        genTyped(tree)
      case tree: Function =>
        genFunction(tree)
      case tree: ApplyDynamic =>
        genApplyDynamic(tree)
      case tree: Apply =>
        genApply(tree)
      case _ =>
        abort(
          "Unexpected tree in genExpr: " + tree + "/" + tree.getClass +
            " at: " + tree.pos
        )
    }

    def genBlock(block: Block): nir.Val = {
      val Block(stats, last) = block

      def isCaseLabelDef(tree: Tree) =
        tree.isInstanceOf[LabelDef] && hasSynthCaseSymbol(tree)

      def translateMatch(last: LabelDef) = {
        val (prologue, cases) = stats.span(s => !isCaseLabelDef(s))
        val labels = cases.map { case label: LabelDef => label }
        genMatch(prologue, labels :+ last)
      }

      withFreshBlockScope(block.pos) { parentScope =>
        last match {
          case label: LabelDef if isCaseLabelDef(label) =>
            translateMatch(label)

          case Apply(
                TypeApply(Select(label: LabelDef, nme.asInstanceOf_Ob), _),
                _
              ) if isCaseLabelDef(label) =>
            translateMatch(label)

          case _ =>
            stats.foreach(genExpr(_))
            genExpr(last)
        }
      }
    }

    def genLabelDef(label: LabelDef): nir.Val = {
      assert(label.params.isEmpty, "empty LabelDef params")
      buf.jump(nir.Next(curMethodEnv.enterLabel(label)))(label.pos)
      genLabel(label)
    }

    def genLabel(label: LabelDef): nir.Val = {
      val local = curMethodEnv.resolveLabel(label)
      val params = label.params.map { id =>
        val local = nir.Val.Local(fresh(), genType(id.tpe))
        curMethodEnv.enter(id.symbol, local)
        local
      }

      buf.label(local, params)(label.pos)
      genExpr(label.rhs)
    }

    def genTailRecLabel(
        dd: DefDef,
        isStatic: Boolean,
        label: LabelDef
    ): nir.Val = {
      val local = curMethodEnv.resolveLabel(label)
      val params = label.params.zip(genParamSyms(dd, isStatic)).map {
        case (lparam, mparamopt) =>
          val local = nir.Val.Local(fresh(), genType(lparam.tpe))
          curMethodEnv.enter(lparam.symbol, local)
          mparamopt.foreach(curMethodEnv.enter(_, local))
          local
      }

      buf.label(local, params)(label.pos)
      if (isStatic) {
        genExpr(label.rhs)
      } else
        withFreshBlockScope(label.rhs.pos) { _ =>
          scoped(
            curMethodThis := Some(params.head)
          )(genExpr(label.rhs))
        }
    }

    def genValDef(vd: ValDef): nir.Val = {
      implicit val pos: nir.SourcePosition = vd.pos
      val localNames = curMethodLocalNames.get
      val isMutable = curMethodInfo.mutableVars.contains(vd.symbol)
      def name = genLocalName(vd.symbol)

      val rhs = genExpr(vd.rhs) match {
        case v @ nir.Val.Local(id, _) =>
          if (localNames.contains(id) || isMutable) ()
          else localNames.update(id, name)
          vd.rhs match {
            // When rhs is a block patch the scopeId of it's result to match the current scopeId
            // This allows us to reflect that ValDef is accessible in this scope
            case _: Block | Typed(_: Block, _) | Try(_: Block, _, _) |
                Try(Typed(_: Block, _), _, _) =>
              buf.updateLetInst(id)(i => i.copy()(i.pos, curScopeId.get))
            case _ => ()
          }
          v
        case nir.Val.Unit => nir.Val.Unit
        case v =>
          if (isMutable) v
          else buf.let(namedId(fresh)(name), nir.Op.Copy(v), unwind)
      }
      if (isMutable) {
        val slot = curMethodEnv.resolve(vd.symbol)
        buf.varstore(slot, rhs, unwind)
      } else {
        curMethodEnv.enter(vd.symbol, rhs)
        nir.Val.Unit
      }
    }

    def genIf(tree: If): nir.Val = {
      val If(cond, thenp, elsep) = tree
      def isUnitType(tpe: Type) =
        defn.isUnitType(tpe) || tpe =:= defn.BoxedUnitTpe
      val retty =
        if (isUnitType(thenp.tpe) || isUnitType(elsep.tpe)) nir.Type.Unit
        else genType(tree.tpe)
      genIf(retty, cond, thenp, elsep)(tree.pos.orElse(fallbackSourcePosition))
    }

    def genIf(
        retty: nir.Type,
        condp: Tree,
        thenp: Tree,
        elsep: Tree,
        ensureLinktime: Boolean = false
    )(implicit ifPos: nir.SourcePosition): nir.Val = {
      val thenn, elsen, mergen = fresh()
      val mergev = nir.Val.Local(fresh(), retty)

      getLinktimeCondition(condp).fold {
        if (ensureLinktime) {
          globalError(
            condp.pos,
            "Cannot resolve given condition in linktime, it might be depending on runtime value"
          )
        }
        val cond = genExpr(condp)
        buf.branch(cond, nir.Next(thenn), nir.Next(elsen))(
          condp.pos.orElse(ifPos)
        )
      } { cond =>
        curMethodEnv.get.isUsingLinktimeResolvedValue = true
        buf.branchLinktime(cond, nir.Next(thenn), nir.Next(elsen))(
          condp.pos.orElse(ifPos)
        )
      }

      locally {
        buf.label(thenn)(thenp.pos.orElse(ifPos))
        val thenv = genExpr(thenp)
        buf.jumpExcludeUnitValue(retty)(mergen, thenv)
      }
      locally {
        buf.label(elsen)(elsep.pos.orElse(ifPos))
        val elsev = genExpr(elsep)
        buf.jumpExcludeUnitValue(retty)(mergen, elsev)
      }
      buf.labelExcludeUnitValue(mergen, mergev)
    }

    def genMatch(m: Match): nir.Val = {
      val Match(scrutp, allcaseps) = m
      type Case = (nir.Local, nir.Val, Tree, global.Position)

      // Extract switch cases and assign unique names to them.
      val caseps: Seq[Case] = allcaseps.flatMap {
        case CaseDef(Ident(nme.WILDCARD), _, _) =>
          Seq.empty
        case cd @ CaseDef(pat, guard, body) =>
          assert(guard.isEmpty, "CaseDef guard was not empty")
          val vals: Seq[nir.Val] = pat match {
            case lit: Literal =>
              List(genLiteralValue(lit))
            case Alternative(alts) =>
              alts.map {
                case lit: Literal => genLiteralValue(lit)
              }
            case _ =>
              Nil
          }
          vals.map((fresh(), _, body, cd.pos))
      }

      // Extract default case.
      val defaultp: Tree = allcaseps.collectFirst {
        case c @ CaseDef(Ident(nme.WILDCARD), _, body) => body
      }.get

      val retty = genType(m.tpe)
      val scrut = genExpr(scrutp)

      // Generate code for the switch and its cases.
      def genSwitch(): nir.Val = {
        // Generate some more fresh names and types.
        val casenexts = caseps.map { case (n, v, _, _) => nir.Next.Case(v, n) }
        val defaultnext = nir.Next(fresh())
        val merge = fresh()
        val mergev = nir.Val.Local(fresh(), retty)

        implicit val pos: nir.SourcePosition = m.pos

        // Generate code for the switch and its cases.
        val scrut = genExpr(scrutp)
        buf.switch(scrut, defaultnext, casenexts)
        buf.label(defaultnext.id)(defaultp.pos)
        buf.jumpExcludeUnitValue(retty)(merge, genExpr(defaultp))(
          defaultp.pos
        )
        caseps.foreach {
          case (n, _, expr, pos) =>
            buf.label(n)(pos)
            val caseres = genExpr(expr)
            buf.jumpExcludeUnitValue(retty)(merge, caseres)(pos)
        }
        buf.labelExcludeUnitValue(merge, mergev)
      }

      // Unreachable in Scala 2.12
      def genIfsChain(): nir.Val = {
        /* Default label needs to be generated before any others and then added to
         * current MethodEnv. It's label might be referenced in any of them in
         * case of match with guards, eg.:
         *
         * "Hello, World!" match {
         *  case "Hello" if cond1 => "foo"
         *  case "World" if cond2 => "bar"
         *  case _ if cond3 => "bar-baz"
         *  case _ => "baz-bar"
         * }
         *
         * might be translated to something like:
         *
         * val x1 = "Hello, World!"
         * if(x1 == "Hello"){ if(cond1) "foo" else default4() }
         * else if (x1 == "World"){ if(cond2) "bar" else default4() }
         * else default4()
         *
         * def default4() = if(cond3) "bar-baz" else "baz-bar
         *
         * We need to make sure to only reserve id for LabelDef at this stage allowing to 'call it' in genApplyLabel.
         * Actual default label would be generated at the end of the if-else chain.
         * Generating other ASTs and mutating state might lead to unexpected
         * runtime errors.
         */
        val optDefaultLabel = defaultp match {
          case label: LabelDef => Some(curMethodEnv.enterLabel(label))
          case _               => None
        }

        def loop(cases: List[Case]): nir.Val = {
          cases match {
            case (_, caze, body, p) :: elsep =>
              implicit val pos: nir.SourcePosition = p

              val cond =
                buf.genClassEquality(
                  leftp = ValTree(scrut)(p),
                  rightp = ValTree(caze)(p),
                  ref = false,
                  negated = false
                )
              buf.genIf(
                retty = retty,
                condp = ValTree(cond)(p),
                thenp = ContTree(body)(_.genExpr(body)),
                elsep = ContTree(_ => loop(elsep))(p)
              )

            case Nil =>
              (defaultp, optDefaultLabel) match {
                case (label: LabelDef, Some(labelId)) =>
                  assert(labelId == curMethodEnv.resolveLabel(label))
                  buf.jump(nir.Next(labelId))(label.pos)
                  buf.genLabel(label)
                case _ =>
                  buf.genExpr(defaultp)
              }
          }
        }
        loop(caseps.toList)
      }

      /* Since 2.13 we need to enforce that only Int switch cases reach backend
       * For all other cases we're generating If-else chain */
      val isIntMatch = scrut.ty == nir.Type.Int &&
        caseps.forall(_._2.ty == nir.Type.Int)

      if (isIntMatch) genSwitch()
      else genIfsChain()
    }

    def genMatch(prologue: List[Tree], lds: List[LabelDef]): nir.Val = {
      // Generate prologue expressions.
      prologue.foreach(genExpr(_))

      // Enter symbols for all labels and jump to the first one.
      lds.foreach(curMethodEnv.enterLabel)
      val firstLd = lds.head
      buf.jump(nir.Next(curMethodEnv.resolveLabel(firstLd)))(firstLd.pos)

      // Generate code for all labels and return value of the last one.
      lds.map(genLabel(_)).last
    }

    def genTry(tree: Try): nir.Val = tree match {
      case Try(expr, catches, finalizer)
          if catches.isEmpty && finalizer.isEmpty =>
        genExpr(expr)
      case Try(expr, catches, finalizer) =>
        val retty = genType(tree.tpe)
        genTry(retty, expr, catches, finalizer)(tree.pos)
    }

    def genTry(
        retty: nir.Type,
        expr: Tree,
        catches: List[Tree],
        finallyp: Tree
    )(enclosingPos: nir.SourcePosition): nir.Val = {
      val handler = fresh()
      val excn = fresh()
      val normaln = fresh()
      val mergen = fresh()
      val excv = nir.Val.Local(fresh(), nir.Rt.Object)
      val mergev = nir.Val.Local(fresh(), retty)

      implicit val pos: nir.SourcePosition = expr.pos.orElse(enclosingPos)
      // Nested code gen to separate out try/catch-related instructions.
      val nested = new ExprBuffer
      withFreshBlockScope(pos) { _ =>
        scoped(
          curUnwindHandler := Some(handler)
        ) {
          nested.label(normaln)
          val res = nested.genExpr(expr)
          nested.jumpExcludeUnitValue(retty)(mergen, res)
        }
      }
      withFreshBlockScope(pos) { _ =>
        nested.label(handler, Seq(excv))
        val res = nested.genTryCatch(retty, excv, mergen, catches)
        nested.jumpExcludeUnitValue(retty)(mergen, res)
      }

      // Append finally to the try/catch instructions and merge them back.
      val insts =
        if (finallyp.isEmpty) nested.toSeq
        else genTryFinally(finallyp, nested.toSeq)

      // Append try/catch instructions to the outher instruction buffer.
      buf.jump(nir.Next(normaln))
      buf ++= insts
      buf.labelExcludeUnitValue(mergen, mergev)
    }

    def genTryCatch(
        retty: nir.Type,
        exc: nir.Val,
        mergen: nir.Local,
        catches: List[Tree]
    )(implicit exprPos: nir.SourcePosition): nir.Val = {
      val cases = catches.map {
        case CaseDef(pat, _, body) =>
          val (excty, symopt) = pat match {
            case Typed(Ident(nme.WILDCARD), tpt) =>
              (genType(tpt.tpe), None)
            case Ident(nme.WILDCARD) =>
              (genType(ThrowableClass.tpe), None)
            case Bind(_, _) =>
              (genType(pat.symbol.tpe), Some(pat.symbol))
          }
          val f = ContTree(body) { (buf: ExprBuffer) =>
            withFreshBlockScope(body.pos) { _ =>
              symopt.foreach { sym =>
                val cast = buf.as(excty, exc, unwind)
                curMethodLocalNames.get.update(cast.id, genLocalName(sym))
                curMethodEnv.enter(sym, cast)
              }
              val res = genExpr(body)
              buf.jumpExcludeUnitValue(retty)(mergen, res)
            }
            nir.Val.Unit
          }
          (excty, f, exprPos)
      }

      def wrap(
          cases: Seq[(nir.Type, ContTree, nir.SourcePosition)]
      ): nir.Val =
        cases match {
          case Seq() =>
            buf.raise(exc, unwind)
            nir.Val.Unit
          case (excty, f, pos) +: rest =>
            val cond = buf.is(excty, exc, unwind)(pos, getScopeId)
            genIf(
              retty,
              ValTree(f)(cond),
              f,
              ContTree(f)(_ => wrap(rest))
            )(pos)
        }

      wrap(cases)
    }

    def genTryFinally(finallyp: Tree, insts: Seq[nir.Inst]): Seq[nir.Inst] = {
      val labels =
        insts.collect {
          case nir.Inst.Label(n, _) => n
        }.toSet
      def internal(cf: nir.Inst.Cf) = cf match {
        case inst @ nir.Inst.Jump(n) =>
          labels.contains(n.id)
        case inst @ nir.Inst.If(_, n1, n2) =>
          labels.contains(n1.id) && labels.contains(n2.id)
        case inst @ nir.Inst.LinktimeIf(_, n1, n2) =>
          labels.contains(n1.id) && labels.contains(n2.id)
        case inst @ nir.Inst.Switch(_, n, ns) =>
          labels.contains(n.id) && ns.forall(n => labels.contains(n.id))
        case inst @ nir.Inst.Throw(_, n) =>
          (n ne nir.Next.None) && labels.contains(n.id)
        case _ =>
          false
      }

      val finalies = new ExprBuffer
      val transformed = insts.map {
        case cf: nir.Inst.Cf if internal(cf) =>
          // We don't touch control-flow within try/catch block.
          cf
        case cf: nir.Inst.Cf =>
          // All control-flow edges that jump outside the try/catch block
          // must first go through finally block if it's present. We generate
          // a new copy of the finally handler for every edge.
          val finallyn = fresh()
          withFreshBlockScope(cf.pos) { _ =>
            finalies.label(finallyn)(cf.pos)
            val res = finalies.genExpr(finallyp)
          }
          finalies += cf
          // The original jump outside goes through finally block first.
          nir.Inst.Jump(nir.Next(finallyn))(cf.pos)
        case inst =>
          inst
      }
      transformed ++ finalies.toSeq
    }

    def genThrow(tree: Throw): nir.Val = {
      val Throw(exprp) = tree
      val res = genExpr(exprp)
      buf.raise(res, unwind)(tree.pos)
      nir.Val.Unit
    }

    def genReturn(tree: Return): nir.Val = {
      val Return(exprp) = tree
      genReturn(genExpr(exprp))(exprp.pos)
    }

    def genReturn(value: nir.Val)(implicit pos: nir.SourcePosition): nir.Val = {
      val retv =
        if (curMethodIsExtern.get) {
          val nir.Type.Function(_, retty) = genExternMethodSig(curMethodSym)
          toExtern(retty, value)
        } else {
          value
        }
      buf.ret(retv)
      nir.Val.Unit
    }

    def genLiteral(lit: Literal): nir.Val = {
      val value = lit.value
      implicit val pos: nir.SourcePosition =
        lit.pos.orElse(fallbackSourcePosition)
      value.tag match {
        case UnitTag | NullTag | BooleanTag | ByteTag | ShortTag | CharTag |
            IntTag | LongTag | FloatTag | DoubleTag | StringTag =>
          genLiteralValue(lit)

        case ClazzTag =>
          genTypeValue(value.typeValue)

        case EnumTag =>
          genStaticMember(EmptyTree, value.symbolValue)
      }
    }

    def genLiteralValue(lit: Literal): nir.Val = {
      val value = lit.value
      value.tag match {
        case UnitTag =>
          nir.Val.Unit
        case NullTag =>
          nir.Val.Null
        case BooleanTag =>
          if (value.booleanValue) nir.Val.True else nir.Val.False
        case ByteTag =>
          nir.Val.Byte(value.intValue.toByte)
        case ShortTag =>
          nir.Val.Short(value.intValue.toShort)
        case CharTag =>
          nir.Val.Char(value.intValue.toChar)
        case IntTag =>
          nir.Val.Int(value.intValue)
        case LongTag =>
          nir.Val.Long(value.longValue)
        case FloatTag =>
          nir.Val.Float(value.floatValue)
        case DoubleTag =>
          nir.Val.Double(value.doubleValue)
        case StringTag =>
          nir.Val.String(value.stringValue)
      }
    }

    def genArrayValue(av: ArrayValue): nir.Val = {
      val ArrayValue(tpt, elems) = av
      implicit val pos: nir.SourcePosition =
        av.pos.orElse(fallbackSourcePosition)
      genArrayValue(tpt, elems)
    }

    def genArrayValue(tpt: Tree, elems: Seq[Tree])(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      val elemty = genType(tpt.tpe)
      val values = genSimpleArgs(elems)

      if (values.forall(_.isCanonical) && values.exists(v => !v.isZero)) {
        buf.arrayalloc(elemty, nir.Val.ArrayValue(elemty, values), unwind)
      } else {
        val alloc = buf.arrayalloc(elemty, nir.Val.Int(elems.length), unwind)
        values.zip(elems).zipWithIndex.foreach {
          case ((v, elem), i) =>
            if (!v.isZero) {
              buf.arraystore(elemty, alloc, nir.Val.Int(i), v, unwind)(
                elem.pos.orElse(pos),
                getScopeId
              )
            }
        }
        alloc
      }
    }

    def genThis(tree: This): nir.Val =
      if (curMethodThis.nonEmpty && tree.symbol == curClassSym.get) {
        curMethodThis.get.get
      } else {
        genModule(tree.symbol)(tree.pos)
      }

    def genModule(sym: Symbol)(implicit pos: nir.SourcePosition): nir.Val = {
      if (sym.isModule && sym.isScala3Defined &&
          sym.hasAttachment[DottyEnumSingletonCompat.type]) {
        /* #2983 This is a reference to a singleton `case` from a Scala 3 `enum`.
         * It is not a module. Instead, it is a static field (accessed through
         * a static getter) in the `enum` class.
         * We use `originalOwner` and `rawname` because that's what the JVM back-end uses.
         */
        val className = genTypeName(sym.originalOwner.companionClass)
        val getterMethodName = nir.Sig.Method(
          sym.rawname.toString(),
          Seq(genType(sym.tpe)),
          nir.Sig.Scope.PublicStatic
        )
        val name = className.member(getterMethodName)
        buf.call(
          ty = genMethodSig(sym),
          ptr = nir.Val.Global(name, nir.Type.Ptr),
          args = Nil,
          unwind = unwind
        )
      } else {
        buf.module(genModuleName(sym), unwind)
      }
    }

    def genIdent(tree: Ident): nir.Val = {
      val sym = tree.symbol
      implicit val pos: nir.SourcePosition =
        tree.pos.orElse(fallbackSourcePosition)
      val value = if (curMethodInfo.mutableVars.contains(sym)) {
        buf.varload(curMethodEnv.resolve(sym), unwind)
      } else if (sym.isModule) {
        genModule(sym)
      } else {
        curMethodEnv.resolve(sym)
      }
      if (nir.Type.isNothing(value.ty)) {
        // Short circuit the generated code for phantom value
        // scala.runtime.Nothing$ extends Throwable so it's safe to throw
        buf.raise(value, unwind)
        buf.unreachable(unwind)
      }
      value
    }

    def genSelect(tree: Select): nir.Val = {
      val Select(qualp, selp) = tree

      val sym = tree.symbol
      val owner = sym.owner
      implicit val pos: nir.SourcePosition = tree.pos.orElse(curMethodSym.pos)

      if (sym.isModule) {
        genModule(sym)
      } else if (sym.isStaticMember) {
        genStaticMember(qualp, sym)
      } else if (sym.isMethod) {
        genApplyMethod(sym, statically = false, qualp, Seq.empty)
      } else if (owner.isStruct) {
        val index = owner.info.decls.filter(_.isField).toList.indexOf(sym)
        val qual = genExpr(qualp)
        buf.extract(qual, Seq(index), unwind)
      } else {
        val ty = genType(tree.symbol.tpe)
        val name = genFieldName(tree.symbol)
        if (sym.isExtern) {
          val externTy = genExternType(tree.symbol.tpe)
          genLoadExtern(ty, externTy, tree.symbol)
        } else {
          val qual = genExpr(qualp)
          buf.fieldload(ty, qual, name, unwind)
        }
      }
    }

    def genStaticMember(receiver: Tree, sym: Symbol)(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      if (sym == BoxedUnit_UNIT) nir.Val.Unit
      else genApplyStaticMethod(sym, receiver.symbol, Seq.empty)
    }

    def genAssign(tree: Assign): nir.Val = {
      val Assign(lhsp, rhsp) = tree
      implicit val pos: nir.SourcePosition =
        tree.pos.orElse(lhsp.pos).orElse(rhsp.pos).orElse(curMethodSym.pos)

      lhsp match {
        case sel @ Select(qualp, _) =>
          val sym = sel.symbol
          val qual = genExpr(qualp)
          val rhs = genExpr(rhsp)
          val name = genFieldName(sym)
          if (sym.isExtern) {
            val externTy = genExternType(sym.tpe)
            genStoreExtern(externTy, sym, rhs)
          } else {
            val ty = genType(sym.tpe)
            val qual = genExpr(qualp)
            buf.fieldstore(ty, qual, name, rhs, unwind)
          }

        case id: Ident =>
          val rhs = genExpr(rhsp)
          val slot = curMethodEnv.resolve(id.symbol)
          buf.varstore(slot, rhs, unwind)
      }
    }

    def genTyped(tree: Typed): nir.Val = tree match {
      case Typed(Super(_, _), _) =>
        curMethodThis.get.get
      case Typed(expr, _) =>
        genExpr(expr)
    }

    // On Scala 2.12 (and on 2.11 with -Ydelambdafy:method), closures are preserved
    // until the backend to be compiled using LambdaMetaFactory.
    //
    // Scala Native does not have any special treatment for closures.
    // We reimplement the anonymous class generation which was originally used on
    // Scala 2.11 and earlier.
    //
    // Each anonymous function gets a generated class for it,
    // similarly to closure encoding on Scala 2.11 and earlier:
    //
    //   class AnonFunX extends java.lang.Object with FunctionalInterface {
    //     var capture1: T1
    //     ...
    //     var captureN: TN
    //     def <init>(this, v1: T1, ..., vN: TN): Unit = {
    //       java.lang.Object.<init>(this)
    //       this.capture1 = v1
    //       ...
    //       this.captureN = vN
    //     }
    //     def samMethod(param1: Tp1, ..., paramK: TpK): Tret = {
    //       EnclsoingClass.this.staticMethod(param1, ..., paramK, this.capture1, ..., this.captureN)
    //     }
    //   }
    //
    // Bridges might require multiple samMethod variants to be created.
    def genFunction(tree: Function): nir.Val = {
      val Function(
        paramTrees,
        callTree @ Apply(targetTree @ Select(_, _), functionArgs)
      ) =
        tree
      implicit val pos: nir.SourcePosition = tree.pos

      val funSym = tree.tpe.typeSymbolDirect
      val paramSyms = paramTrees.map(_.symbol)
      val captureSyms =
        global.delambdafy.FreeVarTraverser.freeVarsOf(tree).toSeq

      val statBuf = curStatBuffer.get

      // Generate an anonymous class definition.

      val suffix = "$$Lambda$" + curClassFresh.get.apply().id
      val anonName = nir.Global.Top(genName(curClassSym).top.id + suffix)
      val traitName = genTypeName(funSym)

      statBuf += nir.Defn.Class(
        nir.Attrs.None,
        anonName,
        Some(nir.Rt.Object.name),
        Seq(traitName)
      )

      // Generate fields to store the captures.

      // enclosing class `this` reference + capture symbols
      val captureSymsWithEnclThis = curClassSym.get +: captureSyms

      val (captureTypes, captureNames) =
        captureSymsWithEnclThis.zipWithIndex.map {
          case (sym, idx) =>
            val name = anonName.member(nir.Sig.Field("capture" + idx))
            val ty = genType(sym.tpe)
            statBuf += nir.Defn.Var(nir.Attrs.None, name, ty, nir.Val.Zero(ty))
            (ty, name)
        }.unzip

      // Generate an anonymous class constructor that initializes all the fields.

      val ctorName = anonName.member(nir.Sig.Ctor(captureTypes))
      val ctorTy =
        nir.Type.Function(nir.Type.Ref(anonName) +: captureTypes, nir.Type.Unit)
      val ctorBody = scoped(curScopeId := nir.ScopeId.TopLevel) {
        val fresh = nir.Fresh()
        val buf = new nir.InstructionBuilder()(fresh)
        val self = nir.Val.Local(fresh(), nir.Type.Ref(anonName))
        val captureFormals = captureTypes.map { ty =>
          nir.Val.Local(fresh(), ty)
        }
        buf.label(fresh(), self +: captureFormals)
        val superTy = nir.Type.Function(Seq(nir.Rt.Object), nir.Type.Unit)
        val superName = nir.Rt.Object.name.member(nir.Sig.Ctor(Seq.empty))
        val superCtor = nir.Val.Global(superName, nir.Type.Ptr)
        buf.call(superTy, superCtor, Seq(self), nir.Next.None)
        captureNames.zip(captureFormals).foreach {
          case (name, capture) =>
            buf.fieldstore(capture.ty, self, name, capture, nir.Next.None)
        }
        buf.ret(nir.Val.Unit)
        buf.toSeq
      }

      statBuf += new nir.Defn.Define(nir.Attrs.None, ctorName, ctorTy, ctorBody)

      // Generate methods that implement SAM interface each of the required signatures.

      functionMethodSymbols(tree).foreach { funSym =>
        val funSig = genName(funSym).asInstanceOf[nir.Global.Member].sig
        val funName = anonName.member(funSig)

        val selfType = nir.Type.Ref(anonName)
        val nir.Sig.Method(_, sigTypes :+ retType, _) = funSig.unmangled
        val paramTypes = selfType +: sigTypes

        val bodyFresh = nir.Fresh()
        val bodyEnv = new MethodEnv(fresh)

        val body = scoped(
          curMethodEnv := bodyEnv,
          curMethodInfo := (new CollectMethodInfo).collect(EmptyTree),
          curFresh := bodyFresh,
          curScopeId := nir.ScopeId.TopLevel,
          curUnwindHandler := None
        ) {
          val fresh = nir.Fresh()
          val buf = new ExprBuffer()(fresh)
          val self = nir.Val.Local(fresh(), selfType)
          val params = sigTypes.map { ty => nir.Val.Local(fresh(), ty) }
          buf.label(fresh(), self +: params)

          // At this point, the type parameter symbols are all Objects.
          // We need to transform them, so that their type conforms to
          // what the apply method expects:
          // - values that can be unboxed, are unboxed
          // - otherwise, the value is cast to the appropriate type
          paramSyms
            .zip(functionArgs.takeRight(sigTypes.length))
            .zip(params)
            .foreach {
              case ((sym, arg), value) =>
                implicit val pos: nir.SourcePosition = arg.pos

                val result =
                  enteringPhase(currentRun.posterasurePhase)(sym.tpe) match {
                    case tpe if tpe.sym.isPrimitiveValueClass =>
                      val targetTpe = genType(tpe)
                      if (targetTpe == value.ty) value
                      else buf.unbox(genBoxType(tpe), value, nir.Next.None)

                    case ErasedValueType(valueClazz, underlying) =>
                      val unboxMethod = valueClazz.derivedValueClassUnbox
                      val casted =
                        buf.genCastOp(value.ty, genRefType(valueClazz), value)
                      val unboxed = buf.genApplyMethod(
                        sym = unboxMethod,
                        statically = false,
                        self = casted,
                        argsp = Nil
                      )
                      if (unboxMethod.tpe.resultType == underlying)
                        unboxed
                      else
                        buf.genCastOp(
                          unboxed.ty,
                          genRefType(underlying),
                          unboxed
                        )

                    case _ =>
                      val unboxed =
                        buf.unboxValue(sym.tpe, partial = true, value)
                      if (unboxed == value) // no need to or cannot unbox, we should cast
                        buf.genCastOp(
                          genRefType(sym.tpe),
                          genRefType(arg.tpe),
                          value
                        )
                      else unboxed
                  }
                curMethodEnv.enter(sym, result)
            }

          captureSymsWithEnclThis.zip(captureNames).foreach {
            case (sym, name) =>
              val value =
                buf.fieldload(genType(sym.tpe), self, name, nir.Next.None)
              curMethodEnv.enter(sym, value)
          }

          val sym = targetTree.symbol
          val method = nir.Val.Global(genMethodName(sym), nir.Type.Ptr)
          val values =
            buf.genMethodArgs(sym, Ident(curClassSym.get) +: functionArgs)
          val sig = genMethodSig(sym)
          val res = buf.call(sig, method, values, nir.Next.None)

          // Get the result type of the lambda after erasure, when entering posterasure.
          // This allows to recover the correct type in case value classes are involved.
          // In that case, the type will be an ErasedValueType.
          val resTyEnteringPosterasure =
            enteringPhase(currentRun.posterasurePhase) {
              targetTree.symbol.tpe.resultType
            }
          buf.ret(
            if (retType == res.ty && resTyEnteringPosterasure == sym.tpe.resultType)
              res
            else
              ensureBoxed(res, resTyEnteringPosterasure, callTree.tpe)(
                buf,
                callTree.pos
              )
          )
          buf.toSeq
        }

        statBuf += new nir.Defn.Define(
          nir.Attrs.None,
          funName,
          nir.Type.Function(paramTypes, retType),
          body
        )
      }

      // Generate call site of the closure allocation to
      // instantiante the anonymous class and call its constructor
      // passing all of the captures as arguments.

      val alloc = buf.classalloc(anonName, unwind)
      val captureVals = curMethodThis.get.get +: captureSyms.map { sym =>
        genExpr(Ident(sym))
      }
      buf.call(
        ctorTy,
        nir.Val.Global(ctorName, nir.Type.Ptr),
        alloc +: captureVals,
        unwind
      )
      alloc
    }

    def ensureBoxed(
        value: nir.Val,
        tpeEnteringPosterasure: Type,
        targetTpe: Type
    )(implicit buf: ExprBuffer, pos: nir.SourcePosition): nir.Val = {
      tpeEnteringPosterasure match {
        case tpe if isPrimitiveValueType(tpe) =>
          buf.boxValue(targetTpe, value)

        case tpe: ErasedValueType =>
          val boxedClass = tpe.valueClazz
          val ctorName = genMethodName(boxedClass.primaryConstructor)
          val ctorSig = genMethodSig(boxedClass.primaryConstructor)

          val alloc =
            buf.classalloc(nir.Global.Top(boxedClass.fullName), unwind)
          val ctor = buf.method(
            alloc,
            ctorName.asInstanceOf[nir.Global.Member].sig,
            unwind
          )
          buf.call(ctorSig, ctor, Seq(alloc, value), unwind)

          alloc

        case _ =>
          value
      }
    }

    private def ensureUnboxed(
        value: nir.Val,
        tpeEnteringPosterasure: Type
    )(implicit buf: ExprBuffer, pos: nir.SourcePosition): nir.Val = {
      tpeEnteringPosterasure match {
        case tpe if isPrimitiveValueType(tpe) =>
          val targetTpe = genType(tpeEnteringPosterasure)
          if (targetTpe == value.ty) value
          else buf.unbox(genBoxType(tpe), value, nir.Next.None)

        case tpe: ErasedValueType =>
          val valueClass = tpe.valueClazz
          val unboxMethod = treeInfo.ValueClass.valueUnbox(tpe)
          val castedValue =
            buf.genCastOp(value.ty, genRefType(valueClass), value)
          buf.genApplyMethod(
            sym = unboxMethod,
            statically = false,
            self = castedValue,
            argsp = Nil
          )

        case tpe =>
          val unboxed = buf.unboxValue(tpe, partial = true, value)
          if (unboxed == value) // no need to or cannot unbox, we should cast
            buf.genCastOp(
              genRefType(tpeEnteringPosterasure),
              genRefType(tpe),
              value
            )
          else unboxed
      }
    }

    // Compute a set of method symbols that SAM-generated class needs to implement.
    def functionMethodSymbols(tree: Function): Seq[Symbol] = {
      val funSym = tree.tpe.typeSymbolDirect
      if (isFunctionSymbol(funSym)) {
        unspecializedSymbol(funSym).info.members
          .filter(_.name.toString == "apply")
          .toSeq
      } else {
        val samInfo = tree.attachments.get[SAMFunction].getOrElse {
          abort(
            s"Cannot find the SAMFunction attachment on $tree at ${tree.pos}"
          )
        }

        val samsBuilder = List.newBuilder[Symbol]
        val seenSignatures = mutable.Set.empty[nir.Sig]

        val synthCls = samInfo.synthCls
        // On Scala < 2.12.5, `synthCls` is polyfilled to `NoSymbol`
        // and hence `samBridges` will always be empty (scala/bug#10512).
        // Since we only support Scala 2.12.12 and up,
        // we assert that this is not the case.
        assert(synthCls != NoSymbol, "Unexpected NoSymbol")
        val samBridges = {
          import scala.reflect.internal.Flags.BRIDGE
          synthCls.info
            .findMembers(excludedFlags = 0L, requiredFlags = BRIDGE)
            .toList
        }

        for (sam <- samInfo.sam :: samBridges) {
          // Remove duplicates, e.g., if we override the same method declared
          // in two super traits.
          val sig = genName(sam).asInstanceOf[nir.Global.Member].sig
          if (seenSignatures.add(sig))
            samsBuilder += sam
        }

        samsBuilder.result()
      }
    }

    def genApplyDynamic(app: ApplyDynamic): nir.Val = {
      val ApplyDynamic(obj, args) = app
      val sym = app.symbol
      implicit val pos: nir.SourcePosition = app.pos

      val params = sym.tpe.params

      val isEqEqOrBangEq =
        (sym.name == EqEqMethodName || sym.name == NotEqMethodName) &&
          params.size == 1

      // If the method is '=='or '!=' generate class equality instead of dyn-call
      if (isEqEqOrBangEq) {
        val neg = sym.name == nme.ne || sym.name == NotEqMethodName
        val last = genClassEquality(obj, args.head, ref = false, negated = neg)
        buf.box(nir.Type.Ref(nir.Global.Top("java.lang.Boolean")), last, unwind)
      } else {
        genApplyDynamic(sym, obj, args)
      }
    }

    def genApplyDynamic(sym: Symbol, obj: Tree, argsp: Seq[Tree])(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      val self = genExpr(obj)
      val methodSig = genMethodSig(sym).asInstanceOf[nir.Type.Function]
      val params = sym.tpe.params

      def isArrayLikeOp = {
        sym.name == nme.update &&
        params.size == 2 && params.head.tpe.typeSymbol == IntClass
      }

      def genDynCall(arrayUpdate: Boolean)(buf: ExprBuffer) = {

        // In the case of an array update we need to manually erase the return type.
        val methodName: nir.Sig =
          if (arrayUpdate) {
            nir.Sig.Proxy("update", Seq(nir.Type.Int, nir.Rt.Object))
          } else {
            val nir.Global.Member(_, sig) = genMethodName(sym)
            sig.toProxy
          }

        val sig =
          nir.Type.Function(
            methodSig.args.head ::
              methodSig.args.tail
                .map(ty => nir.Type.box.getOrElse(ty, ty))
                .toList,
            nir.Type.Ref(nir.Global.Top("java.lang.Object"))
          )

        val callerType = methodSig.args.head
        val boxedArgTypes =
          methodSig.args.tail.map(ty => nir.Type.box.getOrElse(ty, ty)).toList

        val retType = nir.Type.Ref(nir.Global.Top("java.lang.Object"))
        val signature = nir.Type.Function(callerType :: boxedArgTypes, retType)
        val args = genMethodArgs(sym, argsp)

        val method = buf.dynmethod(self, methodName, unwind)
        val values = self +: args

        val call = buf.call(signature, method, values, unwind)
        buf.as(
          nir.Type.box.getOrElse(methodSig.ret, methodSig.ret),
          call,
          unwind
        )
      }

      // If the signature matches an array update, tests at runtime if it really is an array update.
      if (isArrayLikeOp) {
        val cond = ContTree(obj) { (buf: ExprBuffer) =>
          buf.is(
            nir.Type.Ref(
              nir.Global.Top("scala.scalanative.runtime.ObjectArray")
            ),
            self,
            unwind
          )
        }
        val thenp = ContTree(obj)(genDynCall(arrayUpdate = true))
        val elsep = ContTree(obj)(genDynCall(arrayUpdate = false))
        genIf(
          nir.Type.Ref(nir.Global.Top("java.lang.Object")),
          cond,
          thenp,
          elsep
        )

      } else {
        genDynCall(arrayUpdate = false)(this)
      }
    }

    def genApply(app: Apply): nir.Val = {
      def tree = app
      val Apply(fun, args) = app

      implicit val pos: nir.SourcePosition =
        app.pos.orElse(fallbackSourcePosition)
      def fail(msg: String) = {
        reporter.error(app.pos, msg)
        nir.Val.Null
      }
      tree match {
        case _ if fun.symbol == ExternMethod =>
          fail(s"extern can be used only from non-inlined extern methods")
        case Apply(_: TypeApply, _) =>
          genApplyTypeApply(app)
        case Apply(Select(Super(_, _), _), _) =>
          genApplyMethod(
            fun.symbol,
            statically = true,
            curMethodThis.get.get,
            args
          )
        case Apply(Select(New(_), nme.CONSTRUCTOR), _) =>
          genApplyNew(app)

        // Based on Scala2 Cleanup phase, and WrapArray extractor defined in Scala.js variant
        // Replaces `Array(<ScalaRunTime>.wrapArray(ArrayValue(...).$asInstanceOf[...]), <tag>)`
        // with just `ArrayValue(...).$asInstanceOf[...]`
        //
        // See scala/bug#6611; we must *only* do this for literal vararg arrays.
        // format: off
        case Apply(appMeth @ Select(appMethQual, _), Apply(wrapRefArrayMeth, StripCast(arrValue @ ArrayValue(elemtpt, elems)) :: Nil) :: classTagEvidence :: Nil)
        if WrapArray.isClassTagBasedWrapArrayMethod(wrapRefArrayMeth.symbol) &&
                appMeth.symbol == ArrayModule_genericApply &&
                !elemtpt.tpe.typeSymbol.isBottomClass &&
                !elemtpt.tpe.typeSymbol.isPrimitiveValueClass /* can happen via specialization.*/ =>
        
          classTagEvidence.attachments.get[analyzer.MacroExpansionAttachment] match {
            case Some(att) 
            if att.expandee.symbol.name == nme.materializeClassTag && 
               tree.isInstanceOf[ApplyToImplicitArgs] =>
                 genArrayValue(arrValue)
            case _ =>
              val arrValue = genApplyMethod(
                  ClassTagClass.info.decl(nme.newArray),
                  statically = false,
                  classTagEvidence,
                  ValTree(tree)(nir.Val.Int(elems.size)) :: Nil
                )
              val scalaRuntimeTimeModule = genModule(ScalaRunTimeModule)
              elems.zipWithIndex.foreach { case (elem, i) =>
                genApplyModuleMethod(
                  ScalaRunTimeModule,
                  currentRun.runDefinitions.arrayUpdateMethod,
                  ValTree(tree)(arrValue) :: ValTree(tree)(nir.Val.Int(i)) :: elem :: Nil
                )
              }
              arrValue
          }

        case Apply(appMeth @ Select(appMethQual, _), elem0 :: WrapArray(rest @ ArrayValue(elemtpt, elems)) :: Nil)
            if appMeth.symbol == ArrayModule_apply(elemtpt.tpe) && 
               treeInfo.isQualifierSafeToElide(appMethQual) =>
          genArrayValue(elemtpt, elem0 +: elems)

        // See scala/bug#12201, should be rewrite as Primitive Array.
        // Match Array
        case Apply(appMeth @ Select(appMethQual, _), WrapArray(arrValue: ArrayValue) :: _ :: Nil) 
        if appMeth.symbol == ArrayModule_genericApply && 
           treeInfo.isQualifierSafeToElide(appMethQual) =>
          genArrayValue(arrValue)

        case Apply(appMeth @ Select(appMethQual, _), elem :: (nil: RefTree) :: Nil)
        if nil.symbol == NilModule && appMeth.symbol == ArrayModule_apply(elem.tpe.widen) && 
           treeInfo.isExprSafeToInline(nil) && 
           treeInfo.isQualifierSafeToElide(appMethQual) =>
          genArrayValue(TypeTree(elem.tpe.widen), elem :: Nil)
        
        // format: on
        case _ =>
          val sym = fun.symbol
          if (sym.isLabel) {
            genApplyLabel(app)
          } else if (scalaPrimitives.isPrimitive(sym)) {
            genApplyPrimitive(app)
          } else if (currentRun.runDefinitions.isBox(sym)) {
            val arg = args.head
            genApplyBox(arg.tpe, arg)
          } else if (currentRun.runDefinitions.isUnbox(sym)) {
            genApplyUnbox(app.tpe, args.head)
          } else {
            val Select(receiverp, _) = fun
            genApplyMethod(fun.symbol, statically = false, receiverp, args)
          }
      }
    }

    def genApplyLabel(tree: Tree): nir.Val = {
      val Apply(fun, argsp) = tree
      val nir.Val.Local(label, _) = curMethodEnv.resolve(fun.symbol)
      val args = genSimpleArgs(argsp)
      buf.jump(label, args)(tree.pos)
      nir.Val.Unit
    }

    def genApplyBox(st: SimpleType, argp: Tree)(implicit
        enclosingPos: nir.SourcePosition
    ): nir.Val = {
      val value = genExpr(argp)

      buf.box(genBoxType(st), value, unwind)(
        argp.pos.orElse(enclosingPos),
        getScopeId
      )
    }

    def genApplyUnbox(st: SimpleType, argp: Tree)(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      val value = genExpr(argp)
      value.ty match {
        case _: nir.Type.I | _: nir.Type.F =>
          // No need for unboxing, fixing some slack generated by the general
          // purpose Scala compiler.
          value
        case _ =>
          buf.unbox(genBoxType(st), value, unwind)
      }
    }

    def genApplyPrimitive(app: Apply): nir.Val = {
      import scalaPrimitives._

      val Apply(fun @ Select(receiver, _), args) = app
      implicit val pos: nir.SourcePosition = app.pos

      val sym = app.symbol
      val code = scalaPrimitives.getPrimitive(sym, receiver.tpe)
      (code: @switch) match {
        case CONCAT                 => genStringConcat(app)
        case HASH                   => genHashCode(args.head)
        case CFUNCPTR_APPLY         => genCFuncPtrApply(app, code)
        case CFUNCPTR_FROM_FUNCTION => genCFuncFromScalaFunction(app)
        case SYNCHRONIZED =>
          val Apply(Select(receiverp, _), List(argp)) = app
          genSynchronized(receiverp, argp)(app.pos)
        case STACKALLOC              => genStackalloc(app)
        case CLASS_FIELD_RAWPTR      => genClassFieldRawPtr(app)
        case SIZE_OF                 => genSizeOf(app)
        case ALIGNMENT_OF            => genAlignmentOf(app)
        case CQUOTE                  => genCQuoteOp(app)
        case BOXED_UNIT              => nir.Val.Unit
        case USES_LINKTIME_INTRINSIC => genLinktimeIntrinsicApply(app)
        case code =>
          if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code))
            genSimpleOp(app, receiver :: args, code)
          else if (isArrayOp(code) || code == ARRAY_CLONE) genArrayOp(app, code)
          else if (nirPrimitives.isRawPtrOp(code)) genRawPtrOp(app, code)
          else if (nirPrimitives.isRawPtrCastOp(code))
            genRawPtrCastOp(app, code)
          else if (nirPrimitives.isRawSizeCastOp(code))
            genRawSizeCastOp(app, args.head, code)
          else if (isCoercion(code)) genCoercion(app, receiver, code)
          else if (code >= DIV_UINT && code <= ULONG_TO_DOUBLE)
            genUnsignedOp(app, code)
          else {
            abort(
              "Unknown primitive operation: " + sym.fullName + "(" +
                fun.symbol.simpleName + ") " + " at: " + (app.pos)
            )
          }
      }
    }

    private def genLinktimeIntrinsicApply(app: Apply): nir.Val = {
      import nirDefinitions._
      implicit def pos: nir.SourcePosition = app.pos
      val Apply(fun, args) = app

      val sym = fun.symbol
      val Select(receiverp, _) = fun
      val isStatic = sym.owner.isStaticOwner

      sym match {
        case _
            if JavaUtilServiceLoaderLoad.contains(sym) ||
              JavaUtilServiceLoaderLoadInstalled == sym =>
          args.head match {
            case Literal(c: Constant) => () // ok
            case _ =>
              reporter.error(
                app.pos,
                s"Limitation of ScalaNative runtime: first argument of ${sym} needs to be literal constant of class type, use `classOf[T]` instead."
              )
          }
        case _ =>
          reporter.error(
            app.pos,
            s"Unhandled intrinsic function call for $sym"
          )
      }

      curMethodEnv.get.isUsingIntrinsics = true
      genApplyMethod(sym, statically = isStatic, receiverp, args)
    }

    private final val ExternForwarderSig =
      nir.Sig.Generated("$extern$forwarder")

    def getLinktimeCondition(condp: Tree): Option[nir.LinktimeCondition] = {
      import nir.LinktimeCondition._
      def genComparsion(name: Name, value: nir.Val): nir.Comp = {
        def intOrFloatComparison(onInt: nir.Comp, onFloat: nir.Comp)(implicit
            tpe: nir.Type
        ) =
          if (tpe.isInstanceOf[nir.Type.F]) onFloat else onInt

        import nir.Comp._
        implicit val tpe: nir.Type = value.ty
        name match {
          case nme.EQ => intOrFloatComparison(Ieq, Feq)
          case nme.NE => intOrFloatComparison(Ine, Fne)
          case nme.GT => intOrFloatComparison(Sgt, Fgt)
          case nme.GE => intOrFloatComparison(Sge, Fge)
          case nme.LT => intOrFloatComparison(Slt, Flt)
          case nme.LE => intOrFloatComparison(Sle, Fle)
          case nme =>
            globalError(condp.pos, s"Unsupported condition '$nme'");
            nir.Comp.Ine
        }
      }

      condp match {
        // if(bool) (...)
        case Apply(LinktimeProperty(name, _, position), Nil) =>
          Some {
            SimpleCondition(
              propertyName = name,
              comparison = nir.Comp.Ieq,
              value = nir.Val.True
            )(position)
          }

        // if(!bool) (...)
        case Apply(
              Select(
                Apply(LinktimeProperty(name, _, position), Nil),
                nme.UNARY_!
              ),
              Nil
            ) =>
          Some {
            SimpleCondition(
              propertyName = name,
              comparison = nir.Comp.Ieq,
              value = nir.Val.False
            )(position)
          }

        // if(property <comp> x) (...)
        case Apply(
              Select(LinktimeProperty(name, _, position), comp),
              List(arg @ Literal(Constant(_)))
            ) =>
          Some {
            val argValue = genLiteralValue(arg)
            SimpleCondition(
              propertyName = name,
              comparison = genComparsion(comp, argValue),
              value = argValue
            )(position)
          }

        // if(cond1 {&&,||} cond2) (...)
        case Apply(Select(cond1, op), List(cond2)) =>
          (getLinktimeCondition(cond1), getLinktimeCondition(cond2)) match {
            case (Some(c1), Some(c2)) =>
              val bin = op match {
                case nme.ZAND => nir.Bin.And
                case nme.ZOR  => nir.Bin.Or
              }
              Some(ComplexCondition(bin, c1, c2)(condp.pos))
            case (None, None) => None
            case _ =>
              globalError(
                condp.pos,
                "Mixing link-time and runtime conditions is not allowed"
              )
              None
          }

        case _ => None
      }
    }

    def genFuncExternForwarder(funcName: nir.Global, treeSym: Symbol)(implicit
        pos: nir.SourcePosition
    ): nir.Defn = {
      val attrs = nir.Attrs(isExtern = true)

      val sig = genMethodSig(treeSym)
      val externSig = genExternMethodSig(treeSym)

      val nir.Type.Function(origtys, _) = sig
      val nir.Type.Function(paramtys, retty) = externSig

      val methodName = genMethodName(treeSym)
      val method = nir.Val.Global(methodName, nir.Type.Ptr)
      val methodRef = nir.Val.Global(methodName, origtys.head)

      val forwarderName = funcName.member(ExternForwarderSig)
      val forwarderBody = scoped(
        curUnwindHandler := None,
        curScopeId := nir.ScopeId.TopLevel
      ) {
        val fresh = nir.Fresh()
        val buf = new ExprBuffer()(fresh)

        val params = paramtys.map(ty => nir.Val.Local(fresh(), ty))
        buf.label(fresh(), params)
        val boxedParams = params.zip(origtys.tail).map {
          case (param, ty) => buf.fromExtern(ty, param)
        }

        val res = buf.call(sig, method, methodRef +: boxedParams, nir.Next.None)
        val unboxedRes = buf.toExtern(retty, res)
        buf.ret(unboxedRes)

        buf.toSeq
      }
      new nir.Defn.Define(attrs, forwarderName, externSig, forwarderBody)
    }

    def genCFuncFromScalaFunction(app: Apply): nir.Val = {
      implicit val pos: nir.SourcePosition = app.pos
      val fn = app.args.head

      def withGeneratedForwarder(fnRef: nir.Val)(sym: Symbol): nir.Val = {
        val nir.Type.Ref(className, _, _) = fnRef.ty
        curStatBuffer += genFuncExternForwarder(className, sym)
        fnRef
      }

      def reportClosingOverLocalState(args: Seq[Tree]): Unit =
        reporter.error(
          fn.pos,
          s"Closing over local state of ${args.map(v => show(v.symbol)).mkString(", ")} in function transformed to CFuncPtr results in undefined behaviour."
        )

      @tailrec
      def resolveFunction(tree: Tree): nir.Val = tree match {
        case Typed(expr, _) => resolveFunction(expr)
        case Block(_, expr) => resolveFunction(expr)
        case fn @ Function(
              params,
              Apply(targetTree, targetArgs)
            ) => // Scala 2.12+
          val paramTermNames = params.map(_.name)
          val localStateParams = targetArgs
            .filter(arg => !paramTermNames.contains(arg.symbol.name))
          if (localStateParams.nonEmpty)
            reportClosingOverLocalState(localStateParams)

          withGeneratedForwarder {
            genFunction(fn)
          }(targetTree.symbol)

        case _ =>
          unsupported(
            "Failed to resolve function ref for extern forwarder "
              + s"in ${showRaw(fn)} [$pos]"
          )
      }

      val fnRef = resolveFunction(fn)
      val className = genTypeName(app.tpe.sym)

      val ctorTy = nir.Type.Function(
        Seq(nir.Type.Ref(className), nir.Type.Ptr),
        nir.Type.Unit
      )
      val ctorName = className.member(nir.Sig.Ctor(Seq(nir.Type.Ptr)))
      val rawptr = buf.method(fnRef, ExternForwarderSig, unwind)

      val alloc = buf.classalloc(className, unwind)
      buf.call(
        ctorTy,
        nir.Val.Global(ctorName, nir.Type.Ptr),
        Seq(alloc, rawptr),
        unwind
      )
      alloc
    }

    def numOfType(num: Int, ty: nir.Type): nir.Val = ty match {
      case nir.Type.Byte                  => nir.Val.Byte(num.toByte)
      case nir.Type.Short | nir.Type.Char => nir.Val.Short(num.toShort)
      case nir.Type.Int                   => nir.Val.Int(num)
      case nir.Type.Long                  => nir.Val.Long(num.toLong)
      case nir.Type.Size                  => nir.Val.Size(num.toLong)
      case nir.Type.Float                 => nir.Val.Float(num.toFloat)
      case nir.Type.Double                => nir.Val.Double(num.toDouble)
      case _ => unsupported(s"num = $num, ty = ${ty.show}")
    }

    def genSimpleOp(app: Apply, args: List[Tree], code: Int): nir.Val = {
      val retty = genType(app.tpe)

      implicit val pos: nir.SourcePosition =
        app.pos.orElse(fallbackSourcePosition)

      args match {
        case List(right)       => genUnaryOp(code, right, retty)
        case List(left, right) => genBinaryOp(code, left, right, retty)
        case _ => abort("Too many arguments for primitive function: " + app)
      }
    }

    def negateInt(value: nir.Val)(implicit pos: nir.SourcePosition): nir.Val =
      buf.bin(nir.Bin.Isub, value.ty, numOfType(0, value.ty), value, unwind)
    def negateFloat(value: nir.Val)(implicit pos: nir.SourcePosition): nir.Val =
      buf.bin(nir.Bin.Fmul, value.ty, numOfType(-1, value.ty), value, unwind)
    def negateBits(value: nir.Val)(implicit pos: nir.SourcePosition): nir.Val =
      buf.bin(nir.Bin.Xor, value.ty, numOfType(-1, value.ty), value, unwind)
    def negateBool(value: nir.Val)(implicit pos: nir.SourcePosition): nir.Val =
      buf.bin(nir.Bin.Xor, nir.Type.Bool, nir.Val.True, value, unwind)

    def genUnaryOp(code: Int, rightp: Tree, opty: nir.Type)(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      import scalaPrimitives._

      val right = genExpr(rightp)
      val coerced = genCoercion(right, right.ty, opty)

      (opty, code) match {
        case (_: nir.Type.I | _: nir.Type.F, POS) => coerced
        case (_: nir.Type.I, NOT)                 => negateBits(coerced)
        case (_: nir.Type.F, NEG)                 => negateFloat(coerced)
        case (_: nir.Type.I, NEG)                 => negateInt(coerced)
        case (nir.Type.Bool, ZNOT)                => negateBool(coerced)
        case _ => abort("Unknown unary operation code: " + code)
      }
    }

    def genBinaryOp(code: Int, left: Tree, right: Tree, retty: nir.Type)(
        implicit exprPos: nir.SourcePosition
    ): nir.Val = {
      import scalaPrimitives._

      val lty = genType(left.tpe)
      val rty = genType(right.tpe)
      val opty =
        if (isShiftOp(code)) {
          if (lty == nir.Type.Long) {
            nir.Type.Long
          } else {
            nir.Type.Int
          }
        } else {
          binaryOperationType(lty, rty)
        }

      val binres = opty match {
        case _: nir.Type.F =>
          code match {
            case ADD =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Fadd, _, _, _), left, right, opty)
            case SUB =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Fsub, _, _, _), left, right, opty)
            case MUL =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Fmul, _, _, _), left, right, opty)
            case DIV =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Fdiv, _, _, _), left, right, opty)
            case MOD =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Frem, _, _, _), left, right, opty)

            case EQ =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Feq, _, _, _), left, right, opty)
            case NE =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Fne, _, _, _), left, right, opty)
            case LT =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Flt, _, _, _), left, right, opty)
            case LE =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Fle, _, _, _), left, right, opty)
            case GT =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Fgt, _, _, _), left, right, opty)
            case GE =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Fge, _, _, _), left, right, opty)

            case _ =>
              abort(
                "Unknown floating point type binary operation code: " + code
              )
          }

        case nir.Type.Bool | _: nir.Type.I =>
          code match {
            case ADD =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Iadd, _, _, _), left, right, opty)
            case SUB =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Isub, _, _, _), left, right, opty)
            case MUL =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Imul, _, _, _), left, right, opty)
            case DIV =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Sdiv, _, _, _), left, right, opty)
            case MOD =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Srem, _, _, _), left, right, opty)

            case OR =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Or, _, _, _), left, right, opty)
            case XOR =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Xor, _, _, _), left, right, opty)
            case AND =>
              genBinaryOp(nir.Op.Bin(nir.Bin.And, _, _, _), left, right, opty)
            case LSL =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Shl, _, _, _), left, right, opty)
            case LSR =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Lshr, _, _, _), left, right, opty)
            case ASR =>
              genBinaryOp(nir.Op.Bin(nir.Bin.Ashr, _, _, _), left, right, opty)

            case EQ =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Ieq, _, _, _), left, right, opty)
            case NE =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Ine, _, _, _), left, right, opty)
            case LT =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Slt, _, _, _), left, right, opty)
            case LE =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Sle, _, _, _), left, right, opty)
            case GT =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Sgt, _, _, _), left, right, opty)
            case GE =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Sge, _, _, _), left, right, opty)

            case ZOR =>
              genIf(retty, left, Literal(Constant(true)), right)
            case ZAND =>
              genIf(retty, left, right, Literal(Constant(false)))

            case _ =>
              abort("Unknown integer type binary operation code: " + code)
          }

        case _: nir.Type.RefKind =>
          def genEquals(ref: Boolean, negated: Boolean) = (left, right) match {
            // If null is present on either side, we must always
            // generate reference equality, regardless of where it
            // was called with == or eq. This shortcut is not optional.
            case (Literal(Constant(null)), _) | (_, Literal(Constant(null))) =>
              genClassEquality(left, right, ref = true, negated = negated)
            case _ =>
              genClassEquality(left, right, ref = ref, negated = negated)
          }

          code match {
            case EQ =>
              genEquals(ref = false, negated = false)
            case NE =>
              genEquals(ref = false, negated = true)
            case ID =>
              genEquals(ref = true, negated = false)
            case NI =>
              genEquals(ref = true, negated = true)

            case _ =>
              abort("Unknown reference type operation code: " + code)
          }

        case nir.Type.Ptr =>
          code match {
            case EQ | ID =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Ieq, _, _, _), left, right, opty)
            case NE | NI =>
              genBinaryOp(nir.Op.Comp(nir.Comp.Ine, _, _, _), left, right, opty)
          }

        case ty =>
          abort("Unknown binary operation type: " + ty)
      }

      genCoercion(binres, binres.ty, retty)(right.pos)
    }

    def genBinaryOp(
        op: (nir.Type, nir.Val, nir.Val) => nir.Op,
        leftp: Tree,
        rightp: Tree,
        opty: nir.Type
    )(implicit enclosingPos: nir.SourcePosition): nir.Val = {
      val leftPos: nir.SourcePosition = leftp.pos.orElse(enclosingPos)
      val leftty = genType(leftp.tpe)
      val left = genExpr(leftp)
      val leftcoerced = genCoercion(left, leftty, opty)(leftPos)
      val rightty = genType(rightp.tpe)
      val rightPos: nir.SourcePosition = rightp.pos.orElse(enclosingPos)
      val right = genExpr(rightp)
      val rightcoerced = genCoercion(right, rightty, opty)

      buf.let(op(opty, leftcoerced, rightcoerced), unwind)
    }

    private def genClassEquality(
        leftp: Tree,
        rightp: Tree,
        ref: Boolean,
        negated: Boolean
    )(implicit pos: nir.SourcePosition): nir.Val = {

      if (ref) {
        // referencial equality
        val left = genExpr(leftp)
        val right = genExpr(rightp)
        val comp = if (negated) nir.Comp.Ine else nir.Comp.Ieq
        buf.comp(comp, nir.Rt.Object, left, right, unwind)
      } else genClassUniversalEquality(leftp, rightp, negated)
    }

    private def genClassUniversalEquality(l: Tree, r: Tree, negated: Boolean)(
        implicit pos: nir.SourcePosition
    ): nir.Val = {

      /* True if the equality comparison is between values that require the use of the rich equality
       * comparator (scala.runtime.BoxesRunTime.equals). This is the case when either side of the
       * comparison might have a run-time type subtype of java.lang.Number or java.lang.Character.
       * When it is statically known that both sides are equal and subtypes of Number of Character,
       * not using the rich equality is possible (their own equals method will do ok.)
       */
      val mustUseAnyComparator: Boolean = {
        // Exclude custom trees introduced by Scala Natvie from checks
        def isScalaTree(tree: Tree) = tree match {
          case _: ValTree  => false
          case _: ContTree => false
          case _           => true
        }
        val usesOnlyScalaTrees = isScalaTree(l) && isScalaTree(r)
        def areSameFinals =
          l.tpe.isFinalType && r.tpe.isFinalType && (l.tpe =:= r.tpe) && {
            val sym = l.tpe.typeSymbol
            sym != BoxedFloatClass && sym != BoxedDoubleClass
          }
        usesOnlyScalaTrees && !areSameFinals &&
          platform.isMaybeBoxed(l.tpe.typeSymbol) &&
          platform.isMaybeBoxed(r.tpe.typeSymbol)
      }
      def isNull(t: Tree) = t match {
        case Literal(Constant(null))   => true
        case ValTree(nir.Val.Null)     => true
        case ValTree(nir.Val.Zero(ty)) => nir.Type.isPtrType(ty)
        case _                         => false
      }
      def isLiteral(t: Tree) = t match {
        case Literal(_)      => true
        case ValTree(nirVal) => nirVal.isLiteral
        case _               => false
      }
      def isNonNullExpr(t: Tree) =
        isLiteral(t) || ((t.symbol ne null) && t.symbol.isModule)
      def maybeNegate(v: nir.Val): nir.Val = if (negated) negateBool(v) else v
      def comparator = if (negated) nir.Comp.Ine else nir.Comp.Ieq
      if (mustUseAnyComparator) maybeNegate {
        val equalsMethod: Symbol = {
          if (l.tpe <:< BoxedNumberClass.tpe) {
            if (r.tpe <:< BoxedNumberClass.tpe) platform.externalEqualsNumNum
            else if (r.tpe <:< BoxedCharacterClass.tpe)
              platform.externalEqualsNumChar
            else platform.externalEqualsNumObject
          } else platform.externalEquals
        }
        genApplyStaticMethod(equalsMethod, defn.BoxesRunTimeModule, Seq(l, r))
      }
      else if (isNull(l)) {
        // null == expr -> expr eq null
        buf.comp(comparator, nir.Rt.Object, genExpr(r), nir.Val.Null, unwind)
      } else if (isNull(r)) {
        // expr == null -> expr eq null
        buf.comp(comparator, nir.Rt.Object, genExpr(l), nir.Val.Null, unwind)
      } else if (isNonNullExpr(l)) maybeNegate {
        // SI-7852 Avoid null check if L is statically non-null.
        genApplyMethod(
          sym = defn.Any_equals,
          statically = false,
          selfp = l,
          argsp = Seq(r)
        )
      }
      else
        maybeNegate {
          // l == r -> if (l eq null) r eq null else l.equals(r)
          val thenn, elsen, mergen = fresh()
          val mergev = nir.Val.Local(fresh(), nir.Type.Bool)
          val left = genExpr(l)
          val isnull =
            buf.comp(nir.Comp.Ieq, nir.Rt.Object, left, nir.Val.Null, unwind)
          buf.branch(isnull, nir.Next(thenn), nir.Next(elsen))
          locally {
            buf.label(thenn)
            val right = genExpr(r)
            val thenv =
              buf.comp(nir.Comp.Ieq, nir.Rt.Object, right, nir.Val.Null, unwind)
            buf.jump(mergen, Seq(thenv))
          }
          locally {
            buf.label(elsen)
            val elsev = genApplyMethod(
              defn.Any_equals,
              statically = false,
              left,
              Seq(r)
            )
            buf.jump(mergen, Seq(elsev))
          }
          buf.label(mergen, Seq(mergev))
          mergev
        }
    }

    def binaryOperationType(lty: nir.Type, rty: nir.Type) = (lty, rty) match {
      // Bug compatibility with scala/bug/issues/11253
      case (nir.Type.Long, nir.Type.Float) =>
        nir.Type.Double

      case (nir.Type.Ptr, _: nir.Type.RefKind) =>
        lty

      case (_: nir.Type.RefKind, nir.Type.Ptr) =>
        rty

      case (nir.Type.Bool, nir.Type.Bool) =>
        nir.Type.Bool

      case (lhs: nir.Type.FixedSizeI, rhs: nir.Type.FixedSizeI) =>
        if (lhs.width < 32 && rhs.width < 32) {
          nir.Type.Int
        } else if (lhs.width >= rhs.width) {
          lhs
        } else {
          rhs
        }

      case (_: nir.Type.FixedSizeI, _: nir.Type.F) =>
        rty

      case (_: nir.Type.F, _: nir.Type.FixedSizeI) =>
        lty

      case (lhs: nir.Type.F, rhs: nir.Type.F) =>
        if (lhs.width >= rhs.width) lhs else rhs

      case (_: nir.Type.RefKind, _: nir.Type.RefKind) =>
        nir.Rt.Object

      case (ty1, ty2) if ty1 == ty2 =>
        ty1

      case (nir.Type.Nothing, ty) =>
        ty

      case (ty, nir.Type.Nothing) =>
        ty

      case _ =>
        abort(s"can't perform binary operation between $lty and $rty")
    }

    /*
     * Returns a list of trees that each should be concatenated, from left to right.
     * It turns a chained call like "a".+("b").+("c") into a list of arguments.
     */
    def liftStringConcat(tree: Tree): List[Tree] = {
      val result = collection.mutable.ListBuffer[Tree]()
      def loop(tree: Tree): Unit = {
        tree match {
          case Apply(fun @ Select(larg, method), rarg :: Nil)
              if (scalaPrimitives.isPrimitive(fun.symbol) &&
                scalaPrimitives.getPrimitive(fun.symbol) ==
                scalaPrimitives.CONCAT) =>
            loop(larg)
            loop(rarg)
          case _ =>
            result += tree
        }
      }
      loop(tree)
      result.toList
    }

    /* Issue a call to `StringBuilder#append` for the right element type     */
    private final def genStringBuilderAppend(
        stringBuilder: nir.Val.Local,
        tree: Tree
    ): Unit = {
      implicit val nirPos: nir.SourcePosition =
        tree.pos.orElse(fallbackSourcePosition)

      val tpe = tree.tpe
      val argType =
        if (tpe <:< defn.StringTpe) nir.Rt.String
        else if (tpe <:< nirDefinitions.jlStringBufferType)
          genType(nirDefinitions.jlStringBufferRef)
        else if (tpe <:< nirDefinitions.jlCharSequenceType)
          genType(nirDefinitions.jlCharSequenceRef)
        // Don't match for `Array(Char)`, even though StringBuilder has such an overload:
        // `"a" + Array('b')` should NOT be "ab", but "a[C@...".
        else if (tpe <:< defn.ObjectTpe) nir.Rt.Object
        else genType(tpe)

      val value = genExpr(tree)
      val (adaptedValue, targetType) = argType match {
        // jlStringBuilder does not have overloads for byte and short, but we can just use the int version
        case nir.Type.Byte | nir.Type.Short =>
          genCoercion(value, value.ty, nir.Type.Int) -> nir.Type.Int
        case nirType => value -> nirType
      }

      val (appendFunction, appendSig) =
        jlStringBuilderAppendForSymbol(targetType)
      buf.call(
        appendSig,
        appendFunction,
        Seq(stringBuilder, adaptedValue),
        unwind
      )
    }

    private lazy val jlStringBuilderRef =
      nir.Type.Ref(genTypeName(nirDefinitions.jlStringBuilderRef))
    private lazy val jlStringBuilderCtor =
      jlStringBuilderRef.name.member(nir.Sig.Ctor(Seq(nir.Type.Int)))
    private lazy val jlStringBuilderCtorSig = nir.Type.Function(
      Seq(jlStringBuilderRef, nir.Type.Int),
      nir.Type.Unit
    )
    private lazy val jlStringBuilderToString =
      jlStringBuilderRef.name.member(
        nir.Sig.Method("toString", Seq(nir.Rt.String))
      )
    private lazy val jlStringBuilderToStringSig = nir.Type.Function(
      Seq(jlStringBuilderRef),
      nir.Rt.String
    )

    private def genStringConcat(tree: Apply): nir.Val = {
      implicit val nirPos: nir.SourcePosition = tree.pos
      liftStringConcat(tree) match {
        // Optimization for expressions of the form "" + x
        case List(Literal(Constant("")), arg) =>
          genApplyStaticMethod(
            nirDefinitions.String_valueOf_Object,
            defn.StringClass,
            Seq(arg)
          )

        case concatenations =>
          val concatArguments = concatenations.view
            .filter {
              // empty strings are no-ops in concatenation
              case Literal(Constant("")) => false
              case _                     => true
            }
            .map {
              // Eliminate boxing of primitive values. Boxing is introduced by erasure because
              // there's only a single synthetic `+` method "added" to the string class.
              case Apply(boxOp, value :: Nil)
                  // TODO: SN specific boxing
                  if currentRun.runDefinitions.isBox(boxOp.symbol) =>
                value
              case other => other
            }
            .toList
          // Estimate capacity needed for the string builder
          val approxBuilderSize = concatArguments.view.map {
            case Literal(Constant(s: String)) => s.length
            case Literal(c @ Constant(_)) if c.isNonUnitAnyVal =>
              String.valueOf(c).length
            case _ => 0
          }.sum

          // new StringBuidler(approxBuilderSize)
          val stringBuilder =
            buf.classalloc(jlStringBuilderRef.name, unwind, None)
          buf.call(
            jlStringBuilderCtorSig,
            nir.Val.Global(jlStringBuilderCtor, nir.Type.Ptr),
            Seq(stringBuilder, nir.Val.Int(approxBuilderSize)),
            unwind
          )
          // concat substrings
          concatArguments.foreach(genStringBuilderAppend(stringBuilder, _))
          // stringBuilder.toString
          buf.call(
            jlStringBuilderToStringSig,
            nir.Val.Global(jlStringBuilderToString, nir.Type.Ptr),
            Seq(stringBuilder),
            unwind
          )
      }
    }

    def genHashCode(argp: Tree)(implicit pos: nir.SourcePosition): nir.Val = {
      genApplyStaticMethod(
        getMemberMethod(RuntimeStaticsModule, nme.anyHash),
        defn.RuntimeStaticsModule,
        Seq(argp)
      )
    }

    def genArrayOp(app: Apply, code: Int): nir.Val = {
      import scalaPrimitives._

      val Apply(Select(arrayp, _), argsp) = app

      val nir.Type.Array(elemty, _) = genType(arrayp.tpe)

      def elemcode = genArrayCode(arrayp.tpe)
      val array = genExpr(arrayp)

      implicit val pos: nir.SourcePosition = app.pos

      if (code == ARRAY_CLONE) {
        val method = RuntimeArrayCloneMethod(elemcode)
        genApplyMethod(method, statically = true, array, argsp)
      } else if (scalaPrimitives.isArrayGet(code)) {
        val idx = genExpr(argsp(0))
        buf.arrayload(elemty, array, idx, unwind)
      } else if (scalaPrimitives.isArraySet(code)) {
        val idx = genExpr(argsp(0))
        val value = genExpr(argsp(1))
        buf.arraystore(elemty, array, idx, value, unwind)
      } else {
        buf.arraylength(array, unwind)
      }
    }

    def boxValue(st: SimpleType, value: nir.Val)(implicit
        pos: nir.SourcePosition
    ): nir.Val =
      st.sym match {
        case UByteClass | UShortClass | UIntClass | ULongClass | USizeClass =>
          genApplyModuleMethod(
            RuntimeBoxesModule,
            BoxUnsignedMethod(st.sym),
            Seq(ValTree(value)())
          )
        case _ =>
          if (genPrimCode(st) == 'O') {
            value
          } else {
            genApplyBox(st, ValTree(value)())
          }
      }

    def unboxValue(st: SimpleType, partial: Boolean, value: nir.Val)(implicit
        pos: nir.SourcePosition
    ): nir.Val = st.sym match {
      case UByteClass | UShortClass | UIntClass | ULongClass | USizeClass =>
        // Results of asInstanceOfs are partially unboxed, meaning
        // that non-standard value types remain to be boxed.
        if (partial) {
          value
        } else {
          genApplyModuleMethod(
            RuntimeBoxesModule,
            UnboxUnsignedMethod(st.sym),
            Seq(ValTree(value)())
          )
        }
      case _ =>
        if (genPrimCode(st) == 'O') {
          value
        } else {
          genApplyUnbox(st, ValTree(value)())
        }
    }

    def genRawPtrOp(app: Apply, code: Int): nir.Val = code match {
      case _ if nirPrimitives.isRawPtrLoadOp(code) =>
        genRawPtrLoadOp(app, code)
      case _ if nirPrimitives.isRawPtrStoreOp(code) =>
        genRawPtrStoreOp(app, code)
      case ELEM_RAW_PTR =>
        genRawPtrElemOp(app, code)
      case _ =>
        abort(
          s"Unknown pointer operation #$code : " + app +
            " at: " + app.pos
        )
    }

    def genRawPtrLoadOp(app: Apply, code: Int): nir.Val = {
      val Apply(_, Seq(ptrp)) = app
      implicit def pos: nir.SourcePosition = app.pos

      val ptr = genExpr(ptrp)

      val ty = code match {
        case LOAD_BOOL     => nir.Type.Bool
        case LOAD_CHAR     => nir.Type.Char
        case LOAD_BYTE     => nir.Type.Byte
        case LOAD_SHORT    => nir.Type.Short
        case LOAD_INT      => nir.Type.Int
        case LOAD_LONG     => nir.Type.Long
        case LOAD_FLOAT    => nir.Type.Float
        case LOAD_DOUBLE   => nir.Type.Double
        case LOAD_RAW_PTR  => nir.Type.Ptr
        case LOAD_RAW_SIZE => nir.Type.Size
        case LOAD_OBJECT   => nir.Rt.Object
      }
      val memoryOrder =
        if (!ptrp.symbol.isVolatile) None
        else Some(nir.MemoryOrder.Acquire)
      buf.load(ty, ptr, unwind, memoryOrder)
    }

    def genRawPtrStoreOp(app: Apply, code: Int): nir.Val = {
      val Apply(_, Seq(ptrp, valuep)) = app
      implicit def pos: nir.SourcePosition = app.pos

      val ptr = genExpr(ptrp)
      val value = genExpr(valuep)

      val ty = code match {
        case STORE_BOOL     => nir.Type.Bool
        case STORE_CHAR     => nir.Type.Char
        case STORE_BYTE     => nir.Type.Byte
        case STORE_SHORT    => nir.Type.Short
        case STORE_INT      => nir.Type.Int
        case STORE_LONG     => nir.Type.Long
        case STORE_FLOAT    => nir.Type.Float
        case STORE_DOUBLE   => nir.Type.Double
        case STORE_RAW_PTR  => nir.Type.Ptr
        case STORE_RAW_SIZE => nir.Type.Size
        case STORE_OBJECT   => nir.Rt.Object
      }
      val memoryOrder =
        if (!ptrp.symbol.isVolatile) None
        else Some(nir.MemoryOrder.Release)
      buf.store(ty, ptr, value, unwind, memoryOrder)
    }

    def genRawPtrElemOp(app: Apply, code: Int): nir.Val = {
      val Apply(_, Seq(ptrp, offsetp)) = app
      implicit def pos: nir.SourcePosition = app.pos

      val ptr = genExpr(ptrp)
      val offset = genExpr(offsetp)

      buf.elem(nir.Type.Byte, ptr, Seq(offset), unwind)
    }

    def genRawPtrCastOp(app: Apply, code: Int): nir.Val = {
      val Apply(_, Seq(argp)) = app
      implicit def pos: nir.SourcePosition = app.pos

      val fromty = genType(argp.tpe)
      val toty = genType(app.tpe)
      val value = genExpr(argp)

      genCastOp(fromty, toty, value)
    }

    def genRawSizeCastOp(app: Apply, receiver: Tree, code: Int): nir.Val = {
      implicit def pos: nir.SourcePosition = app.pos
      val rec = genExpr(receiver)
      val (fromty, toty, conv) = code match {
        case CAST_RAWSIZE_TO_INT =>
          (nir.Type.Size, nir.Type.Int, nir.Conv.SSizeCast)
        case CAST_RAWSIZE_TO_LONG =>
          (nir.Type.Size, nir.Type.Long, nir.Conv.SSizeCast)
        case CAST_RAWSIZE_TO_LONG_UNSIGNED =>
          (nir.Type.Size, nir.Type.Long, nir.Conv.ZSizeCast)
        case CAST_INT_TO_RAWSIZE =>
          (nir.Type.Int, nir.Type.Size, nir.Conv.SSizeCast)
        case CAST_INT_TO_RAWSIZE_UNSIGNED =>
          (nir.Type.Int, nir.Type.Size, nir.Conv.ZSizeCast)
        case CAST_LONG_TO_RAWSIZE =>
          (nir.Type.Long, nir.Type.Size, nir.Conv.SSizeCast)
      }

      buf.conv(conv, toty, rec, unwind)
    }

    def castConv(fromty: nir.Type, toty: nir.Type): Option[nir.Conv] =
      (fromty, toty) match {
        case (_: nir.Type.I, nir.Type.Ptr)       => Some(nir.Conv.Inttoptr)
        case (nir.Type.Ptr, _: nir.Type.I)       => Some(nir.Conv.Ptrtoint)
        case (_: nir.Type.RefKind, nir.Type.Ptr) => Some(nir.Conv.Bitcast)
        case (nir.Type.Ptr, _: nir.Type.RefKind) => Some(nir.Conv.Bitcast)
        case (_: nir.Type.RefKind, _: nir.Type.RefKind) =>
          Some(nir.Conv.Bitcast)
        case (_: nir.Type.RefKind, _: nir.Type.I) => Some(nir.Conv.Ptrtoint)
        case (_: nir.Type.I, _: nir.Type.RefKind) => Some(nir.Conv.Inttoptr)
        case (l: nir.Type.FixedSizeI, r: nir.Type.F) if l.width == r.width =>
          Some(nir.Conv.Bitcast)
        case (l: nir.Type.F, r: nir.Type.FixedSizeI) if l.width == r.width =>
          Some(nir.Conv.Bitcast)
        case _ if fromty == toty               => None
        case (nir.Type.Float, nir.Type.Double) => Some(nir.Conv.Fpext)
        case (nir.Type.Double, nir.Type.Float) => Some(nir.Conv.Fptrunc)
        case _ =>
          unsupported(s"cast from $fromty to $toty")
      }

    /** Generates direct call to function ptr with optional unboxing arguments
     *  and boxing result Apply.args can contain different number of arguments
     *  depending on usage, however they are passed in constant order:
     *    - 0..N args
     *    - return type evidence
     */
    def genCFuncPtrApply(app: Apply, code: Int): nir.Val = {
      val Apply(appRec @ Select(receiverp, _), aargs) = app

      val paramTypes = app.attachments.get[NonErasedTypes] match {
        case None =>
          reporter.error(
            app.pos,
            s"Failed to generate exact NIR types for $app, something is wrong with scala-native internal."
          )
          Nil
        case Some(NonErasedTypes(paramTys)) => paramTys
      }

      implicit val pos: nir.SourcePosition = app.pos

      val self = genExpr(receiverp)
      val retType = genType(paramTypes.last)
      val unboxedRetType = nir.Type.unbox.getOrElse(retType, retType)

      val args = aargs
        .zip(paramTypes)
        .map {
          case (arg, ty) =>
            val tpe = genType(ty)
            val obj = genExpr(arg)

            /* buf.unboxValue does not handle Ref( Ptr | CArray | ... ) unboxing
             * That's why we're doing it directly */
            if (nir.Type.unbox.isDefinedAt(tpe)) {
              buf.unbox(tpe, obj, unwind)(arg.pos, getScopeId)
            } else {
              buf.unboxValue(fromType(ty), partial = false, obj)(arg.pos)
            }
        }
      val argTypes = args.map(_.ty)
      val funcSig = nir.Type.Function(argTypes, unboxedRetType)

      val selfName = genTypeName(CFuncPtrClass)
      val getRawPtrName = selfName
        .member(nir.Sig.Field("rawptr", nir.Sig.Scope.Private(selfName)))

      val target = buf.fieldload(nir.Type.Ptr, self, getRawPtrName, unwind)
      val result = buf.call(funcSig, target, args, unwind)
      if (retType != unboxedRetType)
        buf.box(retType, result, unwind)
      else {
        boxValue(paramTypes.last, result)
      }
    }

    def genCastOp(fromty: nir.Type, toty: nir.Type, value: nir.Val)(implicit
        pos: nir.SourcePosition
    ): nir.Val =
      castConv(fromty, toty)
        .orElse(castConv(value.ty, toty))
        .fold(value)(buf.conv(_, toty, value, unwind))

    private lazy val optimizedFunctions = {
      // Included functions should be pure, and should not not narrow the result type
      Set[Symbol](
        CastIntToRawSize,
        CastIntToRawSizeUnsigned,
        CastLongToRawSize,
        CastRawSizeToInt,
        CastRawSizeToLong,
        CastRawSizeToLongUnsigned,
        Size_fromByte,
        Size_fromShort,
        Size_fromInt,
        USize_fromUByte,
        USize_fromUShort,
        USize_fromUInt,
        RuntimePackage_fromRawSize,
        RuntimePackage_fromRawUSize
      ) ++ UnsignedOfMethods ++ RuntimePackage_toRawSizeAlts
    }

    private def getUnboxedSize(
        sizep: Tree
    )(implicit pos: nir.SourcePosition): nir.Val =
      sizep match {
        // Optimize call, strip numeric conversions
        case Literal(Constant(size: Int)) => nir.Val.Size(size)
        case Block(Nil, expr)             => getUnboxedSize(expr)
        case Apply(fun, List(arg))
            if optimizedFunctions.contains(fun.symbol) ||
              arg.symbol.exists && optimizedFunctions.contains(arg.symbol) =>
          getUnboxedSize(arg)
        case Typed(expr, _) => getUnboxedSize(expr)
        case _              =>
          // actual unboxing
          val size = genExpr(sizep)
          val sizeTy = nir.Type.normalize(size.ty)
          val unboxed =
            if (nir.Type.unbox.contains(sizeTy)) buf.unbox(sizeTy, size, unwind)
            else if (nir.Type.box.contains(sizeTy)) size
            else {
              reporter.error(
                sizep.pos,
                s"Invalid usage of Intrinsic.stackalloc, argument is not an integer type: ${sizeTy}"
              )
              nir.Val.Size(0)
            }

          if (unboxed.ty == nir.Type.Size) unboxed
          else buf.conv(nir.Conv.SSizeCast, nir.Type.Size, unboxed, unwind)
      }

    private def genStackalloc(app: Apply): nir.Val = app match {
      case Apply(_, args) => {
        implicit val pos: nir.SourcePosition = app.pos
        val tpe = app.attachments
          .get[NonErasedType]
          .map(v => genType(v.tpe, deconstructValueTypes = true))
          .getOrElse {
            reporter.error(
              app.pos,
              "Not found type attachment for stackalloc operation, report it as a bug."
            )
            nir.Type.Nothing
          }

        val size = args match {
          case Seq()         => nir.Val.Size(1)
          case Seq(sizep)    => getUnboxedSize(sizep)
          case Seq(_, sizep) => getUnboxedSize(sizep)
        }
        buf.stackalloc(tpe, size, unwind)
      }
    }

    def genCQuoteOp(app: Apply): nir.Val = {
      app match {
        // Sometimes I really miss quasiquotes.
        //
        // case q"""
        //   scala.scalanative.unsafe.`package`.CQuote(
        //     new StringContext(scala.this.Predef.wrapRefArray(
        //       Array[String]{${str: String}}.$asInstanceOf[Array[Object]]()
        //     ))
        //   ).c()
        // """ =>
        case Apply(
              Select(
                Apply(
                  _,
                  List(
                    Apply(
                      _,
                      List(
                        Apply(
                          _,
                          List(
                            Apply(
                              TypeApply(
                                Select(
                                  ArrayValue(
                                    _,
                                    List(Literal(Constant(str: String)))
                                  ),
                                  _
                                ),
                                _
                              ),
                              _
                            )
                          )
                        )
                      )
                    )
                  )
                ),
                _
              ),
              _
            ) =>
          val bytes = nir.Val.ByteString(StringUtils.processEscapes(str))
          val const = nir.Val.Const(bytes)
          buf.box(nir.Rt.BoxedPtr, const, unwind)(app.pos, getScopeId)

        case _ =>
          unsupported(app)
      }
    }

    def genUnsignedOp(app: Tree, code: Int): nir.Val = {
      implicit val pos: nir.SourcePosition = app.pos
      app match {
        case Apply(_, Seq(argp)) if code == UNSIGNED_OF =>
          val ty = genType(app.tpe.resultType)
          val arg = genExpr(argp)

          buf.box(ty, arg, unwind)

        case Apply(_, Seq(argp))
            if code >= BYTE_TO_UINT && code <= INT_TO_ULONG =>
          val ty = genType(app.tpe)
          val arg = genExpr(argp)

          buf.conv(nir.Conv.Zext, ty, arg, unwind)

        case Apply(_, Seq(argp))
            if code >= UINT_TO_FLOAT && code <= ULONG_TO_DOUBLE =>
          val ty = genType(app.tpe)
          val arg = genExpr(argp)

          buf.conv(nir.Conv.Uitofp, ty, arg, unwind)

        case Apply(_, Seq(leftp, rightp)) =>
          val bin = code match {
            case DIV_UINT | DIV_ULONG => nir.Bin.Udiv
            case REM_UINT | REM_ULONG => nir.Bin.Urem
          }
          val ty = genType(leftp.tpe)
          val left = genExpr(leftp)
          val right = genExpr(rightp)

          buf.bin(bin, ty, left, right, unwind)
      }
    }

    def genClassFieldRawPtr(
        app: Apply
    )(implicit pos: nir.SourcePosition): nir.Val = {
      val Apply(_, List(target, fieldName: Literal)) = app
      val fieldNameId = fieldName.value.stringValue
      val classInfo = target.tpe.finalResultType
      val classInfoSym = classInfo.typeSymbol.asClass
      def matchesName(f: Symbol) = {
        f.nameString == fieldNameId
      }

      val candidates =
        (classInfo.decls ++ classInfoSym.parentSymbols.flatMap(_.info.decls))
          .filter(f => f.isField && matchesName(f))

      candidates.find(!_.isVar).foreach { f =>
        reporter.error(
          app.pos,
          s"Resolving pointer of immutable field ${fieldNameId} in ${f.owner} is not allowed"
        )
      }

      candidates
        .collectFirst {
          case f if matchesName(f) && f.isVariable =>
            // Don't allow to get pointer to immutable field, as it might allow for mutation
            buf.field(genExpr(target), genFieldName(f), unwind)
        }
        .getOrElse {
          reporter.error(
            app.pos,
            s"${classInfoSym} does not contain field ${fieldNameId}"
          )
          nir.Val.Int(-1)
        }

    }

    def genSizeOf(app: Apply)(implicit pos: nir.SourcePosition): nir.Val =
      genLayoutValueOf("sizeOf", buf.sizeOf(_, unwind))(app)

    def genAlignmentOf(app: Apply)(implicit pos: nir.SourcePosition): nir.Val =
      genLayoutValueOf("alignmentOf", buf.alignmentOf(_, unwind))(app)

    // used as internal implementation of sizeOf / alignmentOf
    private def genLayoutValueOf(opType: String, toVal: nir.Type => nir.Val)(
        app: Apply
    )(implicit pos: nir.SourcePosition): nir.Val = {
      def fail(msg: => String) = {
        reporter.error(app.pos, msg)
        nir.Val.Zero(nir.Type.Size)
      }
      app.attachments.get[NonErasedType] match {
        case None =>
          app.args match {
            case Seq(Literal(cls: Constant)) =>
              val nirTpe = genType(cls.typeValue, deconstructValueTypes = false)
              toVal(nirTpe)
            case _ =>
              fail(
                s"Method $opType(Class[_]) requires single class literal argument, if you used $opType[T] report it as a bug"
              )
          }
        case Some(NonErasedType(tpe)) if tpe.sym.isTraitOrInterface =>
          fail(
            s"Type ${tpe} is a trait or interface, $opType cannot be calculated"
          )
        case Some(NonErasedType(tpe)) =>
          try {
            val nirTpe = genType(tpe, deconstructValueTypes = true)
            toVal(nirTpe)
          } catch {
            case ex: Throwable =>
              fail(
                s"Failed to generate exact NIR type of $tpe - ${ex.getMessage}"
              )
          }
      }
    }

    def genSynchronized(receiverp: Tree, bodyp: Tree)(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      genSynchronized(receiverp)(_.genExpr(bodyp))
    }

    def genSynchronized(
        receiverp: Tree
    )(
        bodyGen: ExprBuffer => nir.Val
    )(implicit pos: nir.SourcePosition): nir.Val = {
      // Here we wrap the synchronized call into the try-finally block
      // to ensure that monitor would be released even in case of the exception
      // or in case of non-local returns
      val nested = new ExprBuffer()
      val normaln = fresh()
      val handler = fresh()
      val mergen = fresh()

      // scalanative.runtime.`package`.enterMonitor(receiver)
      genApplyStaticMethod(
        sym = RuntimeEnterMonitorMethod,
        receiver = RuntimePackageClass,
        argsp = List(receiverp)
      )
      // synchronized block
      val retValue = scoped(curUnwindHandler := Some(handler)) {
        nested.label(normaln)
        bodyGen(nested)
      }
      val retty = retValue.ty
      nested.jumpExcludeUnitValue(retty)(mergen, retValue)

      // dummy exception handler,
      // monitor$.exit() call would be added to it in genTryFinally transformer
      locally {
        val excv = nir.Val.Local(fresh(), nir.Rt.Object)
        nested.label(handler, Seq(excv))
        nested.raise(excv, unwind)
        nested.jumpExcludeUnitValue(retty)(mergen, nir.Val.Zero(retty))
      }

      // Append try/catch instructions to the outher instruction buffer.
      buf.jump(nir.Next(normaln))
      buf ++= genTryFinally(
        // scalanative.runtime.`package`.exitMonitor(receiver)
        finallyp = ContTree(receiverp)(
          _.genApplyStaticMethod(
            sym = RuntimeExitMonitorMethod,
            receiver = RuntimePackageClass,
            argsp = List(receiverp)
          )
        ),
        insts = nested.toSeq
      )
      val mergev = nir.Val.Local(fresh(), retty)
      buf.labelExcludeUnitValue(mergen, mergev)
    }

    def genCoercion(app: Apply, receiver: Tree, code: Int): nir.Val = {
      val rec = genExpr(receiver)
      val (fromty, toty) = coercionTypes(code)

      genCoercion(rec, fromty, toty)(app.pos)
    }

    def genCoercion(value: nir.Val, fromty: nir.Type, toty: nir.Type)(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      if (fromty == toty) value
      else if (nir.Type.isNothing(fromty) || nir.Type.isNothing(toty)) value
      else {
        val conv = (fromty, toty) match {
          case (nir.Type.Ptr, _: nir.Type.RefKind) =>
            nir.Conv.Bitcast
          case (_: nir.Type.RefKind, nir.Type.Ptr) =>
            nir.Conv.Bitcast
          case (l: nir.Type.FixedSizeI, r: nir.Type.FixedSizeI) =>
            if (l.width < r.width) {
              if (l.signed) {
                nir.Conv.Sext
              } else {
                nir.Conv.Zext
              }
            } else if (l.width > r.width) {
              nir.Conv.Trunc
            } else {
              nir.Conv.Bitcast
            }
          case (i: nir.Type.I, _: nir.Type.F) if i.signed =>
            nir.Conv.Sitofp
          case (i: nir.Type.I, _: nir.Type.F) if !i.signed =>
            nir.Conv.Uitofp
          case (_: nir.Type.F, i: nir.Type.FixedSizeI) if i.signed =>
            if (i.width < 32) {
              val ivalue = genCoercion(value, fromty, nir.Type.Int)
              return genCoercion(ivalue, nir.Type.Int, toty)
            }
            nir.Conv.Fptosi
          case (_: nir.Type.F, i: nir.Type.FixedSizeI) if !i.signed =>
            if (i.width < 32) {
              val ivalue = genCoercion(value, fromty, nir.Type.Int)
              return genCoercion(ivalue, nir.Type.Int, toty)
            }
            nir.Conv.Fptoui
          case (nir.Type.Double, nir.Type.Float) =>
            nir.Conv.Fptrunc
          case (nir.Type.Float, nir.Type.Double) =>
            nir.Conv.Fpext
        }
        buf.conv(conv, toty, value, unwind)
      }
    }

    def coercionTypes(code: Int): (nir.Type, nir.Type) = {
      import scalaPrimitives._

      code match {
        case B2B => (nir.Type.Byte, nir.Type.Byte)
        case B2S => (nir.Type.Byte, nir.Type.Short)
        case B2C => (nir.Type.Byte, nir.Type.Char)
        case B2I => (nir.Type.Byte, nir.Type.Int)
        case B2L => (nir.Type.Byte, nir.Type.Long)
        case B2F => (nir.Type.Byte, nir.Type.Float)
        case B2D => (nir.Type.Byte, nir.Type.Double)

        case S2B => (nir.Type.Short, nir.Type.Byte)
        case S2S => (nir.Type.Short, nir.Type.Short)
        case S2C => (nir.Type.Short, nir.Type.Char)
        case S2I => (nir.Type.Short, nir.Type.Int)
        case S2L => (nir.Type.Short, nir.Type.Long)
        case S2F => (nir.Type.Short, nir.Type.Float)
        case S2D => (nir.Type.Short, nir.Type.Double)

        case C2B => (nir.Type.Char, nir.Type.Byte)
        case C2S => (nir.Type.Char, nir.Type.Short)
        case C2C => (nir.Type.Char, nir.Type.Char)
        case C2I => (nir.Type.Char, nir.Type.Int)
        case C2L => (nir.Type.Char, nir.Type.Long)
        case C2F => (nir.Type.Char, nir.Type.Float)
        case C2D => (nir.Type.Char, nir.Type.Double)

        case I2B => (nir.Type.Int, nir.Type.Byte)
        case I2S => (nir.Type.Int, nir.Type.Short)
        case I2C => (nir.Type.Int, nir.Type.Char)
        case I2I => (nir.Type.Int, nir.Type.Int)
        case I2L => (nir.Type.Int, nir.Type.Long)
        case I2F => (nir.Type.Int, nir.Type.Float)
        case I2D => (nir.Type.Int, nir.Type.Double)

        case L2B => (nir.Type.Long, nir.Type.Byte)
        case L2S => (nir.Type.Long, nir.Type.Short)
        case L2C => (nir.Type.Long, nir.Type.Char)
        case L2I => (nir.Type.Long, nir.Type.Int)
        case L2L => (nir.Type.Long, nir.Type.Long)
        case L2F => (nir.Type.Long, nir.Type.Float)
        case L2D => (nir.Type.Long, nir.Type.Double)

        case F2B => (nir.Type.Float, nir.Type.Byte)
        case F2S => (nir.Type.Float, nir.Type.Short)
        case F2C => (nir.Type.Float, nir.Type.Char)
        case F2I => (nir.Type.Float, nir.Type.Int)
        case F2L => (nir.Type.Float, nir.Type.Long)
        case F2F => (nir.Type.Float, nir.Type.Float)
        case F2D => (nir.Type.Float, nir.Type.Double)

        case D2B => (nir.Type.Double, nir.Type.Byte)
        case D2S => (nir.Type.Double, nir.Type.Short)
        case D2C => (nir.Type.Double, nir.Type.Char)
        case D2I => (nir.Type.Double, nir.Type.Int)
        case D2L => (nir.Type.Double, nir.Type.Long)
        case D2F => (nir.Type.Double, nir.Type.Float)
        case D2D => (nir.Type.Double, nir.Type.Double)
      }
    }

    def genApplyTypeApply(app: Apply): nir.Val = {
      val Apply(tapp @ TypeApply(fun @ Select(receiverp, _), targs), argsp) =
        app

      val fromty = genType(receiverp.tpe)
      val toty = genType(targs.head.tpe)
      def boxty = genBoxType(targs.head.tpe)
      val value = genExpr(receiverp)
      implicit val pos: nir.SourcePosition =
        tapp.pos.orElse(app.pos).orElse(fallbackSourcePosition)
      lazy val boxed = boxValue(receiverp.tpe, value)(receiverp.pos.orElse(pos))

      fun.symbol match {
        case Object_isInstanceOf =>
          buf.is(boxty, boxed, unwind)

        case Object_asInstanceOf =>
          (fromty, toty) match {
            case (_: nir.Type.PrimitiveKind, _: nir.Type.PrimitiveKind) =>
              genCoercion(value, fromty, toty)
            case _ if boxed.ty =?= boxty => boxed
            case (_, nir.Type.Nothing) =>
              val runtimeNothing = genType(RuntimeNothingClass)
              val isNullL, notNullL = fresh()
              val isNull =
                buf.comp(nir.Comp.Ieq, boxed.ty, boxed, nir.Val.Null, unwind)
              buf.branch(isNull, nir.Next(isNullL), nir.Next(notNullL))
              buf.label(isNullL)
              buf.raise(nir.Val.Null, unwind)
              buf.label(notNullL)
              buf.as(runtimeNothing, boxed, unwind)
              buf.unreachable(unwind)
              buf.label(fresh())
              nir.Val.Zero(nir.Type.Nothing)
            case _ =>
              val cast = buf.as(boxty, boxed, unwind)
              unboxValue(app.tpe, partial = true, cast)(app.pos)
          }

        case Object_synchronized =>
          assert(argsp.size == 1, "synchronized with wrong number of args")
          genSynchronized(ValTree(receiverp)(boxed), argsp.head)
      }
    }

    def genApplyNew(app: Apply): nir.Val = {
      val Apply(fun @ Select(New(tpt), nme.CONSTRUCTOR), args) = app
      implicit val pos: nir.SourcePosition =
        app.pos.orElse(fallbackSourcePosition)

      SimpleType.fromType(tpt.tpe) match {
        case SimpleType(ArrayClass, Seq(targ)) =>
          genApplyNewArray(targ, args)

        case st if st.isStruct =>
          genApplyNewStruct(st, args)

        case SimpleType(cls, Seq()) =>
          genApplyNew(cls, fun.symbol, args)

        case SimpleType(sym, targs) =>
          unsupported(s"unexpected new: $sym with targs $targs")
      }
    }

    def genApplyNewStruct(st: SimpleType, argsp: Seq[Tree]): nir.Val = {
      val ty = genType(st)
      val args = genSimpleArgs(argsp)
      var res: nir.Val = nir.Val.Zero(ty)

      args.zip(argsp).zipWithIndex.foreach {
        case ((arg, argp), idx) =>
          res = buf.insert(res, arg, Seq(idx), unwind)(argp.pos, getScopeId)
      }

      res
    }

    def genApplyNewArray(targ: SimpleType, argsp: Seq[Tree])(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      val Seq(lengthp) = argsp
      val length = genExpr(lengthp)

      buf.arrayalloc(genType(targ), length, unwind)
    }

    def genApplyNew(clssym: Symbol, ctorsym: Symbol, args: List[Tree])(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      val alloc = buf.classalloc(genTypeName(clssym), unwind)
      val call = genApplyMethod(ctorsym, statically = true, alloc, args)
      alloc
    }

    def genApplyModuleMethod(module: Symbol, method: Symbol, args: Seq[Tree])(
        implicit pos: nir.SourcePosition
    ): nir.Val = {
      val self = genModule(module)
      genApplyMethod(method, statically = true, self, args)
    }

    def genApplyMethod(
        sym: Symbol,
        statically: Boolean,
        selfp: Tree,
        argsp: Seq[Tree]
    )(implicit pos: nir.SourcePosition): nir.Val = {
      if (sym.isExtern && sym.isAccessor) {
        genApplyExternAccessor(sym, argsp)
      } else if (sym.isStaticMember) {
        genApplyStaticMethod(sym, selfp.symbol, argsp)
      } else {
        val self = genExpr(selfp)
        genApplyMethod(sym, statically, self, argsp)
      }
    }

    private def genApplyStaticMethod(
        sym: Symbol,
        receiver: Symbol,
        argsp: Seq[Tree]
    )(implicit pos: nir.SourcePosition): nir.Val = {
      require(!sym.isExtern, sym.owner)
      val name = genStaticMemberName(sym, receiver)
      val method = nir.Val.Global(name, nir.Type.Ptr)
      val sig = genMethodSig(sym, statically = true)
      val args = genMethodArgs(sym, argsp)
      buf.call(sig, method, args, unwind)
    }

    def genApplyExternAccessor(sym: Symbol, argsp: Seq[Tree])(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      argsp match {
        case Seq() =>
          val ty = genMethodSig(sym).ret
          val externTy = genExternMethodSig(sym).ret
          genLoadExtern(ty, externTy, sym)
        case Seq(valuep) =>
          val externTy = genExternType(sym.tpe.paramss.flatten.last.tpe)
          genStoreExtern(externTy, sym, genExpr(valuep))
      }
    }

    def genLoadExtern(ty: nir.Type, externTy: nir.Type, sym: Symbol)(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      assert(sym.isExtern, "loadExtern was not extern")

      val name = nir.Val.Global(genName(sym), nir.Type.Ptr)
      val memoryOrder =
        if (!sym.isVolatile) None
        else Some(nir.MemoryOrder.Acquire)

      fromExtern(
        ty,
        buf.load(externTy, name, unwind, memoryOrder)
      )
    }

    def genStoreExtern(externTy: nir.Type, sym: Symbol, value: nir.Val)(implicit
        pos: nir.SourcePosition
    ): nir.Val = {
      assert(sym.isExtern, "storeExtern was not extern")
      val name = nir.Val.Global(genName(sym), nir.Type.Ptr)
      val externValue = toExtern(externTy, value)
      val memoryOrder =
        if (!sym.isVolatile) None
        else Some(nir.MemoryOrder.Release)

      buf.store(externTy, name, externValue, unwind, memoryOrder)
    }

    def toExtern(expectedTy: nir.Type, value: nir.Val)(implicit
        pos: nir.SourcePosition
    ): nir.Val =
      (expectedTy, value.ty) match {
        case (_, refty: nir.Type.Ref)
            if nir.Type.boxClasses.contains(refty.name)
              && nir.Type.unbox(nir.Type.Ref(refty.name)) == expectedTy =>
          buf.unbox(nir.Type.Ref(refty.name), value, unwind)
        case _ =>
          value
      }

    def fromExtern(expectedTy: nir.Type, value: nir.Val)(implicit
        pos: nir.SourcePosition
    ): nir.Val =
      (expectedTy, value.ty) match {
        case (refty: nir.Type.Ref, ty)
            if nir.Type.boxClasses.contains(refty.name)
              && nir.Type.unbox(nir.Type.Ref(refty.name)) == ty =>
          buf.box(nir.Type.Ref(refty.name), value, unwind)
        case _ =>
          value
      }

    def genApplyMethod(
        sym: Symbol,
        statically: Boolean,
        self: nir.Val,
        argsp: Seq[Tree]
    )(implicit pos: nir.SourcePosition): nir.Val = {
      val owner = sym.owner
      val name = genMethodName(sym)
      val origSig = genMethodSig(sym)
      val isExtern = sym.isExtern
      val sig =
        if (isExtern) genExternMethodSig(sym)
        else origSig
      val args = genMethodArgs(sym, argsp)
      val method =
        if (statically || owner.isStruct || isExtern) {
          nir.Val.Global(name, nir.Type.Ptr)
        } else {
          val nir.Global.Member(_, sig) = name
          buf.method(self, sig, unwind)
        }
      val values =
        if (sym.isStaticInNIR) args
        else self +: args

      val res = buf.call(sig, method, values, unwind)

      if (!isExtern) {
        res
      } else {
        val nir.Type.Function(_, retty) = origSig
        fromExtern(retty, res)
      }
    }

    def genMethodArgs(sym: Symbol, argsp: Seq[Tree]): Seq[nir.Val] =
      if (sym.isExtern) genExternMethodArgs(sym, argsp)
      else genSimpleArgs(argsp)

    private def genSimpleArgs(argsp: Seq[Tree]): Seq[nir.Val] =
      argsp.map(genExpr)

    private def genExternMethodArgs(
        sym: Symbol,
        argsp: Seq[Tree]
    ): Seq[nir.Val] = {
      val res = Seq.newBuilder[nir.Val]
      val nir.Type.Function(argTypes, _) = genExternMethodSig(sym)
      val paramSyms = sym.tpe.params
      assert(
        argTypes.size == argsp.size && argTypes.size == paramSyms.size,
        "Different number of arguments passed to method signature and apply method"
      )

      def genArg(
          argp: Tree,
          paramTpe: global.Type,
          isVarArg: Boolean = false
      ): nir.Val = {
        implicit def pos: nir.SourcePosition =
          argp.pos.orElse(fallbackSourcePosition)
        implicit def exprBuf: ExprBuffer = buf
        val rawValue = genExpr(argp)
        val maybeUnboxed =
          if (isVarArg) ensureUnboxed(rawValue, paramTpe.finalResultType)
          else rawValue
        val externType = genExternType(paramTpe.finalResultType)
        val value = (maybeUnboxed, nir.Type.box.get(externType)) match {
          case (value @ nir.Val.Null, Some(unboxedType)) =>
            externType match {
              case nir.Type.Ptr | _: nir.Type.RefKind => value
              case _ =>
                reporter.warning(
                  argp.pos,
                  s"Passing null as argument of type ${paramTpe} to the extern method is unsafe. " +
                    s"The argument would be unboxed to primitive value of type $externType."
                )
                nir.Val.Zero(unboxedType)
            }
          case (value, _) => value
        }
        toExtern(externType, value)
      }

      for (((argp, sigType), paramSym) <- argsp zip argTypes zip paramSyms) {
        sigType match {
          case nir.Type.Vararg =>
            argp match {
              case Apply(_, List(ArrayValue(_, args))) =>
                for (tree <- args) {
                  implicit def pos: nir.SourcePosition =
                    tree.pos.orElse(fallbackSourcePosition)
                  val sym = tree.symbol
                  val tpe =
                    tree.attachments
                      .get[NonErasedType]
                      .map(_.tpe)
                      .getOrElse {
                        if (tree.symbol != null && tree.symbol.exists)
                          tree.symbol.tpe.finalResultType
                        else tree.tpe
                      }
                  val arg = genArg(tree, tpe, isVarArg = true)
                  def isUnsigned = nir.Type.isUnsignedType(genType(tpe))
                  // Decimal varargs needs to be promoted to at least Int, and float needs to be promoted to Double
                  val promotedArg = arg.ty match {
                    case nir.Type.Float =>
                      this.genCastOp(nir.Type.Float, nir.Type.Double, arg)
                    case i: nir.Type.FixedSizeI
                        if i.width < nir.Type.Int.width =>
                      val conv =
                        if (isUnsigned) nir.Conv.Zext
                        else nir.Conv.Sext
                      buf.conv(conv, nir.Type.Int, arg, unwind)
                    case _ => arg
                  }
                  res += promotedArg
                }
              // Scala 2.13 only
              case Select(_, name) if name == defn.NilModule.name => ()
              case _ =>
                reporter.error(
                  argp.pos,
                  "Unable to extract vararg arguments, varargs to extern methods must be passed directly to the applied function"
                )
            }
          case _ => res += genArg(argp, paramSym.tpe)
        }
      }
      res.result()
    }

    private def labelExcludeUnitValue(label: nir.Local, value: nir.Val.Local)(
        implicit pos: nir.SourcePosition
    ): nir.Val =
      value.ty match {
        case nir.Type.Unit =>
          buf.label(label); nir.Val.Unit
        case _ =>
          buf.label(label, Seq(value)); value
      }

    private def jumpExcludeUnitValue(
        mergeType: nir.Type
    )(label: nir.Local, value: nir.Val)(implicit
        pos: nir.SourcePosition
    ): Unit =
      mergeType match {
        case nir.Type.Unit =>
          buf.jump(label, Nil)
        case _ =>
          buf.jump(label, Seq(value))
      }
  }

  object WrapArray {
    private lazy val hasNewCollections =
      !scala.util.Properties.versionNumberString.startsWith("2.12.")

    private val wrapArrayModule =
      if (hasNewCollections) ScalaRunTimeModule
      else PredefModule

    val wrapRefArrayMethod: Symbol =
      getMemberMethod(wrapArrayModule, nme.wrapRefArray)

    val genericWrapArrayMethod: Symbol =
      getMemberMethod(wrapArrayModule, nme.genericWrapArray)

    def isClassTagBasedWrapArrayMethod(sym: Symbol): Boolean =
      sym == wrapRefArrayMethod || sym == genericWrapArrayMethod

    private val isWrapArray: Set[Symbol] = {
      Seq(
        nme.wrapRefArray,
        nme.wrapByteArray,
        nme.wrapShortArray,
        nme.wrapCharArray,
        nme.wrapIntArray,
        nme.wrapLongArray,
        nme.wrapFloatArray,
        nme.wrapDoubleArray,
        nme.wrapBooleanArray,
        nme.wrapUnitArray,
        nme.genericWrapArray
      ).map(getMemberMethod(wrapArrayModule, _)).toSet
    }

    def unapply(tree: Apply): Option[Tree] = tree match {
      case Apply(wrapArray_?, List(wrapped))
          if isWrapArray(wrapArray_?.symbol) =>
        Some(wrapped)
      case _ =>
        None
    }
  }

}

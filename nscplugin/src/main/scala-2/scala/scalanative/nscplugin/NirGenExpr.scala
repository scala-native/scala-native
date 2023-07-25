package scala.scalanative
package nscplugin

import scala.annotation.tailrec
import scala.collection.mutable
import scala.tools.nsc
import scalanative.nir._
import scalanative.util.{StringUtils, unsupported}
import scalanative.util.ScopedVar.scoped
import scalanative.nscplugin.NirPrimitives._

trait NirGenExpr[G <: nsc.Global with Singleton] { self: NirGenPhase[G] =>
  import global._
  import definitions._
  import treeInfo.hasSynthCaseSymbol
  import nirAddons._
  import nirDefinitions._
  import SimpleType.{fromType, fromSymbol}

  sealed case class ValTree(value: nir.Val) extends Tree
  sealed case class ContTree(f: () => nir.Val) extends Tree

  class FixupBuffer(implicit fresh: Fresh) extends nir.Buffer {
    private var labeled = false

    override def +=(inst: Inst): Unit = {
      implicit val pos: nir.Position = inst.pos
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
        case Inst.Let(_, op, _) if op.resty == Type.Nothing =>
          unreachable(unwind)
          label(fresh())
        case _ =>
          ()
      }
    }

    override def ++=(insts: Seq[Inst]): Unit =
      insts.foreach { inst => this += inst }

    override def ++=(other: nir.Buffer): Unit =
      this ++= other.toSeq
  }

  class ExprBuffer(implicit fresh: Fresh) extends FixupBuffer { buf =>
    def genExpr(tree: Tree): Val = tree match {
      case EmptyTree =>
        Val.Unit
      case ValTree(value) =>
        value
      case ContTree(f) =>
        f()
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

    def genBlock(block: Block): Val = {
      val Block(stats, last) = block

      def isCaseLabelDef(tree: Tree) =
        tree.isInstanceOf[LabelDef] && hasSynthCaseSymbol(tree)

      def translateMatch(last: LabelDef) = {
        val (prologue, cases) = stats.span(s => !isCaseLabelDef(s))
        val labels = cases.map { case label: LabelDef => label }
        genMatch(prologue, labels :+ last)
      }

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

    def genLabelDef(label: LabelDef): Val = {
      assert(label.params.isEmpty, "empty LabelDef params")
      buf.jump(Next(curMethodEnv.enterLabel(label)))(label.pos)
      genLabel(label)
    }

    def genLabel(label: LabelDef): Val = {
      val local = curMethodEnv.resolveLabel(label)
      val params = label.params.map { id =>
        val local = Val.Local(fresh(), genType(id.tpe))
        curMethodEnv.enter(id.symbol, local)
        local
      }

      buf.label(local, params)(label.pos)
      genExpr(label.rhs)
    }

    def genTailRecLabel(dd: DefDef, isStatic: Boolean, label: LabelDef): Val = {
      val local = curMethodEnv.resolveLabel(label)
      val params = label.params.zip(genParamSyms(dd, isStatic)).map {
        case (lparam, mparamopt) =>
          val local = Val.Local(fresh(), genType(lparam.tpe))
          curMethodEnv.enter(lparam.symbol, local)
          mparamopt.foreach(curMethodEnv.enter(_, local))
          local
      }

      buf.label(local, params)(label.pos)
      if (isStatic) {
        genExpr(label.rhs)
      } else {
        scoped(curMethodThis := Some(params.head)) {
          genExpr(label.rhs)
        }
      }
    }

    def genValDef(vd: ValDef): Val = {
      val rhs = genExpr(vd.rhs)
      val isMutable = curMethodInfo.mutableVars.contains(vd.symbol)
      if (!isMutable) {
        curMethodEnv.enter(vd.symbol, rhs)
        Val.Unit
      } else {
        val slot = curMethodEnv.resolve(vd.symbol)
        buf.varstore(slot, rhs, unwind)(vd.pos)
      }
    }

    def genIf(tree: If): Val = {
      val If(cond, thenp, elsep) = tree
      val retty = genType(tree.tpe)
      genIf(retty, cond, thenp, elsep)(tree.pos)
    }

    def genIf(
        retty: nir.Type,
        condp: Tree,
        thenp: Tree,
        elsep: Tree,
        ensureLinktime: Boolean = false
    )(implicit ifPos: nir.Position): Val = {
      val thenn, elsen, mergen = fresh()
      val mergev = Val.Local(fresh(), retty)

      getLinktimeCondition(condp).fold {
        if (ensureLinktime) {
          globalError(
            condp.pos,
            "Cannot resolve given condition in linktime, it might be depending on runtime value"
          )
        }
        val cond = genExpr(condp)
        buf.branch(cond, Next(thenn), Next(elsen))(condp.pos)
      } { cond =>
        curMethodUsesLinktimeResolvedValues = true
        buf.branchLinktime(cond, Next(thenn), Next(elsen))(condp.pos)
      }

      locally {
        buf.label(thenn)(thenp.pos)
        val thenv = genExpr(thenp)
        buf.jumpExcludeUnitValue(retty)(mergen, thenv)
      }
      locally {
        buf.label(elsen)(elsep.pos)
        val elsev = genExpr(elsep)
        buf.jumpExcludeUnitValue(retty)(mergen, elsev)
      }
      buf.labelExcludeUnitValue(mergen, mergev)
    }

    def genMatch(m: Match): Val = {
      val Match(scrutp, allcaseps) = m
      type Case = (Local, Val, Tree, nir.Position)

      // Extract switch cases and assign unique names to them.
      val caseps: Seq[Case] = allcaseps.flatMap {
        case CaseDef(Ident(nme.WILDCARD), _, _) =>
          Seq.empty
        case cd @ CaseDef(pat, guard, body) =>
          assert(guard.isEmpty, "CaseDef guard was not empty")
          val vals: Seq[Val] = pat match {
            case lit: Literal =>
              List(genLiteralValue(lit))
            case Alternative(alts) =>
              alts.map {
                case lit: Literal => genLiteralValue(lit)
              }
            case _ =>
              Nil
          }
          vals.map((fresh(), _, body, cd.pos: nir.Position))
      }

      // Extract default case.
      val defaultp: Tree = allcaseps.collectFirst {
        case c @ CaseDef(Ident(nme.WILDCARD), _, body) => body
      }.get

      val retty = genType(m.tpe)
      val scrut = genExpr(scrutp)

      // Generate code for the switch and its cases.
      def genSwitch(): Val = {
        // Generate some more fresh names and types.
        val casenexts = caseps.map { case (n, v, _, _) => Next.Case(v, n) }
        val defaultnext = Next(fresh())
        val merge = fresh()
        val mergev = Val.Local(fresh(), retty)

        implicit val pos: nir.Position = m.pos

        // Generate code for the switch and its cases.
        val scrut = genExpr(scrutp)
        buf.switch(scrut, defaultnext, casenexts)
        buf.label(defaultnext.name)(defaultp.pos)
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

      def genIfsChain(): Val = {
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
         * We need to make sure to only generate LabelDef at this stage.
         * Generating other ASTs and mutating state might lead to unexpected
         * runtime errors.
         */
        val optDefaultLabel = defaultp match {
          case label: LabelDef => Some(genLabelDef(label))
          case _               => None
        }

        def loop(cases: List[Case]): Val = {
          cases match {
            case (_, caze, body, p) :: elsep =>
              implicit val pos: nir.Position = p

              val cond =
                buf.genClassEquality(
                  leftp = ValTree(scrut),
                  rightp = ValTree(caze),
                  ref = false,
                  negated = false
                )
              buf.genIf(
                retty = retty,
                condp = ValTree(cond),
                thenp = ContTree(() => genExpr(body)),
                elsep = ContTree(() => loop(elsep))
              )

            case Nil => optDefaultLabel.getOrElse(genExpr(defaultp))
          }
        }
        loop(caseps.toList)
      }

      /* Since 2.13 we need to enforce that only Int switch cases reach backend
       * For all other cases we're generating If-else chain */
      val isIntMatch = scrut.ty == Type.Int &&
        caseps.forall(_._2.ty == Type.Int)

      if (isIntMatch) genSwitch()
      else genIfsChain()
    }

    def genMatch(prologue: List[Tree], lds: List[LabelDef]): Val = {
      // Generate prologue expressions.
      prologue.foreach(genExpr(_))

      // Enter symbols for all labels and jump to the first one.
      lds.foreach(curMethodEnv.enterLabel)
      val firstLd = lds.head
      buf.jump(Next(curMethodEnv.resolveLabel(firstLd)))(firstLd.pos)

      // Generate code for all labels and return value of the last one.
      lds.map(genLabel(_)).last
    }

    def genTry(tree: Try): Val = tree match {
      case Try(expr, catches, finalizer)
          if catches.isEmpty && finalizer.isEmpty =>
        genExpr(expr)
      case Try(expr, catches, finalizer) =>
        val retty = genType(tree.tpe)
        genTry(retty, expr, catches, finalizer)
    }

    def genTry(
        retty: nir.Type,
        expr: Tree,
        catches: List[Tree],
        finallyp: Tree
    ): Val = {
      val handler = fresh()
      val excn = fresh()
      val normaln = fresh()
      val mergen = fresh()
      val excv = Val.Local(fresh(), Rt.Object)
      val mergev = Val.Local(fresh(), retty)

      implicit val pos: nir.Position = expr.pos
      // Nested code gen to separate out try/catch-related instructions.
      val nested = new ExprBuffer
      locally {
        scoped(curUnwindHandler := Some(handler)) {
          nested.label(normaln)
          val res = nested.genExpr(expr)
          nested.jumpExcludeUnitValue(retty)(mergen, res)
        }
      }
      locally {
        nested.label(handler, Seq(excv))
        val res = nested.genTryCatch(retty, excv, mergen, catches)(expr.pos)
        nested.jumpExcludeUnitValue(retty)(mergen, res)
      }

      // Append finally to the try/catch instructions and merge them back.
      val insts =
        if (finallyp.isEmpty) {
          nested.toSeq
        } else {
          genTryFinally(finallyp, nested.toSeq)
        }

      // Append try/catch instructions to the outher instruction buffer.
      buf.jump(Next(normaln))
      buf ++= insts
      buf.labelExcludeUnitValue(mergen, mergev)
    }

    def genTryCatch(
        retty: nir.Type,
        exc: Val,
        mergen: Local,
        catches: List[Tree]
    )(implicit exprPos: nir.Position): Val = {
      val cases = catches.map {
        case cd @ CaseDef(pat, _, body) =>
          val (excty, symopt) = pat match {
            case Typed(Ident(nme.WILDCARD), tpt) =>
              (genType(tpt.tpe), None)
            case Ident(nme.WILDCARD) =>
              (genType(ThrowableClass.tpe), None)
            case Bind(_, _) =>
              (genType(pat.symbol.tpe), Some(pat.symbol))
          }
          val f = { () =>
            symopt.foreach { sym =>
              val cast = buf.as(excty, exc, unwind)(cd.pos)
              curMethodEnv.enter(sym, cast)
            }
            val res = genExpr(body)
            buf.jumpExcludeUnitValue(retty)(mergen, res)
            Val.Unit
          }
          (excty, f, exprPos)
      }

      def wrap(cases: Seq[(nir.Type, () => Val, nir.Position)]): Val =
        cases match {
          case Seq() =>
            buf.raise(exc, unwind)
            Val.Unit
          case (excty, f, pos) +: rest =>
            val cond = buf.is(excty, exc, unwind)(pos)
            genIf(
              retty,
              ValTree(cond),
              ContTree(f),
              ContTree(() => wrap(rest))
            )(pos)
        }

      wrap(cases)
    }

    def genTryFinally(finallyp: Tree, insts: Seq[nir.Inst]): Seq[Inst] = {
      val labels =
        insts.collect {
          case Inst.Label(n, _) => n
        }.toSet
      def internal(cf: Inst.Cf) = cf match {
        case inst @ Inst.Jump(n) =>
          labels.contains(n.name)
        case inst @ Inst.If(_, n1, n2) =>
          labels.contains(n1.name) && labels.contains(n2.name)
        case inst @ Inst.LinktimeIf(_, n1, n2) =>
          labels.contains(n1.name) && labels.contains(n2.name)
        case inst @ Inst.Switch(_, n, ns) =>
          labels.contains(n.name) && ns.forall(n => labels.contains(n.name))
        case inst @ Inst.Throw(_, n) =>
          (n ne Next.None) && labels.contains(n.name)
        case _ =>
          false
      }

      val finalies = new ExprBuffer
      val transformed = insts.map {
        case cf: Inst.Cf if internal(cf) =>
          // We don't touch control-flow within try/catch block.
          cf
        case cf: Inst.Cf =>
          // All control-flow edges that jump outside the try/catch block
          // must first go through finally block if it's present. We generate
          // a new copy of the finally handler for every edge.
          val finallyn = fresh()
          finalies.label(finallyn)(cf.pos)
          val res = finalies.genExpr(finallyp)
          finalies += cf
          // The original jump outside goes through finally block first.
          Inst.Jump(Next(finallyn))(cf.pos)
        case inst =>
          inst
      }
      transformed ++ finalies.toSeq
    }

    def genThrow(tree: Throw): Val = {
      val Throw(exprp) = tree
      val res = genExpr(exprp)
      buf.raise(res, unwind)(tree.pos)
      Val.Unit
    }

    def genReturn(tree: Return): Val = {
      val Return(exprp) = tree
      genReturn(genExpr(exprp))(exprp.pos)
    }

    def genReturn(value: Val)(implicit pos: nir.Position): Val = {
      val retv =
        if (curMethodIsExtern.get) {
          val Type.Function(_, retty) = genExternMethodSig(curMethodSym)
          toExtern(retty, value)
        } else {
          value
        }
      buf.ret(retv)
      Val.Unit
    }

    def genLiteral(lit: Literal): Val = {
      val value = lit.value
      implicit val pos: nir.Position = lit.pos
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

    def genLiteralValue(lit: Literal): Val = {
      val value = lit.value
      value.tag match {
        case UnitTag =>
          Val.Unit
        case NullTag =>
          Val.Null
        case BooleanTag =>
          if (value.booleanValue) Val.True else Val.False
        case ByteTag =>
          Val.Byte(value.intValue.toByte)
        case ShortTag =>
          Val.Short(value.intValue.toShort)
        case CharTag =>
          Val.Char(value.intValue.toChar)
        case IntTag =>
          Val.Int(value.intValue)
        case LongTag =>
          Val.Long(value.longValue)
        case FloatTag =>
          Val.Float(value.floatValue)
        case DoubleTag =>
          Val.Double(value.doubleValue)
        case StringTag =>
          Val.String(value.stringValue)
      }
    }

    def genArrayValue(av: ArrayValue): Val = {
      val ArrayValue(tpt, elems) = av
      implicit val pos: nir.Position = av.pos

      val elemty = genType(tpt.tpe)
      val values = genSimpleArgs(elems)

      if (values.forall(_.isCanonical) && values.exists(v => !v.isZero)) {
        buf.arrayalloc(elemty, Val.ArrayValue(elemty, values), unwind)
      } else {
        val alloc = buf.arrayalloc(elemty, Val.Int(elems.length), unwind)
        values.zip(elems).zipWithIndex.foreach {
          case ((v, elem), i) =>
            if (!v.isZero) {
              buf.arraystore(elemty, alloc, Val.Int(i), v, unwind)(elem.pos)
            }
        }
        alloc
      }
    }

    def genThis(tree: This): Val =
      if (curMethodThis.nonEmpty && tree.symbol == curClassSym.get) {
        curMethodThis.get.get
      } else {
        genModule(tree.symbol)(tree.pos)
      }

    def genModule(sym: Symbol)(implicit pos: nir.Position): Val = {
      if (sym.isModule && sym.isScala3Defined &&
          sym.hasAttachment[DottyEnumSingletonCompat.type]) {
        /* #2983 This is a reference to a singleton `case` from a Scala 3 `enum`.
         * It is not a module. Instead, it is a static field (accessed through
         * a static getter) in the `enum` class.
         * We use `originalOwner` and `rawname` because that's what the JVM back-end uses.
         */
        val className = genTypeName(sym.originalOwner.companionClass)
        val getterMethodName = Sig.Method(
          sym.rawname.toString(),
          Seq(genType(sym.tpe)),
          Sig.Scope.PublicStatic
        )
        val name = className.member(getterMethodName)
        buf.call(
          ty = genMethodSig(sym),
          ptr = Val.Global(name, nir.Type.Ptr),
          args = Nil,
          unwind = unwind
        )
      } else {
        buf.module(genModuleName(sym), unwind)
      }
    }

    def genIdent(tree: Ident): Val = {
      val sym = tree.symbol
      implicit val pos: nir.Position = tree.pos
      if (curMethodInfo.mutableVars.contains(sym)) {
        buf.varload(curMethodEnv.resolve(sym), unwind)
      } else if (sym.isModule) {
        genModule(sym)
      } else {
        curMethodEnv.resolve(sym)
      }
    }

    def genSelect(tree: Select): Val = {
      val Select(qualp, selp) = tree

      val sym = tree.symbol
      val owner = sym.owner
      implicit val pos: nir.Position = tree.pos

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
        if (sym.owner.isExternType) {
          val externTy = genExternType(tree.symbol.tpe)
          genLoadExtern(ty, externTy, tree.symbol)
        } else {
          val qual = genExpr(qualp)
          buf.fieldload(ty, qual, name, unwind)
        }
      }
    }

    def genStaticMember(receiver: Tree, sym: Symbol)(implicit
        pos: nir.Position
    ): Val = {
      if (sym == BoxedUnit_UNIT) Val.Unit
      else genApplyStaticMethod(sym, receiver, Seq.empty)
    }

    def genAssign(tree: Assign): Val = {
      val Assign(lhsp, rhsp) = tree
      implicit val pos: nir.Position = tree.pos

      lhsp match {
        case sel @ Select(qualp, _) =>
          val sym = sel.symbol
          val qual = genExpr(qualp)
          val rhs = genExpr(rhsp)
          val name = genFieldName(sym)
          if (sym.owner.isExternType) {
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

    def genTyped(tree: Typed): Val = tree match {
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
    def genFunction(tree: Function): Val = {
      val Function(
        paramTrees,
        callTree @ Apply(targetTree @ Select(_, _), functionArgs)
      ) =
        tree
      implicit val pos: nir.Position = tree.pos

      val funSym = tree.tpe.typeSymbolDirect
      val paramSyms = paramTrees.map(_.symbol)
      val captureSyms =
        global.delambdafy.FreeVarTraverser.freeVarsOf(tree).toSeq

      val statBuf = curStatBuffer.get

      // Generate an anonymous class definition.

      val suffix = "$$Lambda$" + curClassFresh.get.apply().id
      val anonName = nir.Global.Top(genName(curClassSym).top.id + suffix)
      val traitName = genName(funSym)

      statBuf += nir.Defn.Class(
        Attrs.None,
        anonName,
        Some(nir.Rt.Object.name),
        Seq(traitName)
      )

      // Generate fields to store the captures.

      // enclosing class `this` reference + capture symbols
      val captureSymsWithEnclThis = curClassSym.get +: captureSyms

      val captureTypes = captureSymsWithEnclThis.map(sym => genType(sym.tpe))
      val captureNames =
        captureSymsWithEnclThis.zipWithIndex.map {
          case (sym, idx) =>
            val name = anonName.member(nir.Sig.Field("capture" + idx))
            val ty = genType(sym.tpe)
            statBuf += nir.Defn.Var(Attrs.None, name, ty, Val.Zero(ty))
            name
        }

      // Generate an anonymous class constructor that initializes all the fields.

      val ctorName = anonName.member(Sig.Ctor(captureTypes))
      val ctorTy =
        nir.Type.Function(Type.Ref(anonName) +: captureTypes, Type.Unit)
      val ctorBody = {
        val fresh = Fresh()
        val buf = new nir.Buffer()(fresh)
        val self = Val.Local(fresh(), Type.Ref(anonName))
        val captureFormals = captureTypes.map { ty => Val.Local(fresh(), ty) }
        buf.label(fresh(), self +: captureFormals)
        val superTy = nir.Type.Function(Seq(Rt.Object), Type.Unit)
        val superName = Rt.Object.name.member(Sig.Ctor(Seq.empty))
        val superCtor = Val.Global(superName, Type.Ptr)
        buf.call(superTy, superCtor, Seq(self), Next.None)
        captureNames.zip(captureFormals).foreach {
          case (name, capture) =>
            buf.fieldstore(capture.ty, self, name, capture, Next.None)
        }
        buf.ret(Val.Unit)
        buf.toSeq
      }

      statBuf += Defn.Define(Attrs.None, ctorName, ctorTy, ctorBody)

      // Generate methods that implement SAM interface each of the required signatures.

      functionMethodSymbols(tree).foreach { funSym =>
        val funSig = genName(funSym).asInstanceOf[nir.Global.Member].sig
        val funName = anonName.member(funSig)

        val selfType = Type.Ref(anonName)
        val Sig.Method(_, sigTypes :+ retType, _) = funSig.unmangled
        val paramTypes = selfType +: sigTypes

        val bodyFresh = Fresh()
        val bodyEnv = new MethodEnv(fresh)

        val body = scoped(
          curMethodEnv := bodyEnv,
          curMethodInfo := (new CollectMethodInfo).collect(EmptyTree),
          curFresh := bodyFresh,
          curUnwindHandler := None
        ) {
          val fresh = Fresh()
          val buf = new ExprBuffer()(fresh)
          val self = Val.Local(fresh(), selfType)
          val params = sigTypes.map { ty => Val.Local(fresh(), ty) }
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
                implicit val pos: nir.Position = arg.pos

                val result =
                  enteringPhase(currentRun.posterasurePhase)(sym.tpe) match {
                    case tpe if tpe.sym.isPrimitiveValueClass =>
                      val targetTpe = genType(tpe)
                      if (targetTpe == value.ty) value
                      else buf.unbox(genBoxType(tpe), value, Next.None)

                    case ErasedValueType(valueClazz, underlying) =>
                      val unboxMethod = valueClazz.derivedValueClassUnbox
                      val casted =
                        buf.genCastOp(value.ty, genType(valueClazz), value)
                      val unboxed = buf.genApplyMethod(
                        sym = unboxMethod,
                        statically = false,
                        self = casted,
                        argsp = Nil
                      )
                      if (unboxMethod.tpe.resultType == underlying)
                        unboxed
                      else
                        buf.genCastOp(unboxed.ty, genType(underlying), unboxed)

                    case _ =>
                      val unboxed =
                        buf.unboxValue(sym.tpe, partial = true, value)
                      if (unboxed == value) // no need to or cannot unbox, we should cast
                        buf.genCastOp(genType(sym.tpe), genType(arg.tpe), value)
                      else unboxed
                  }
                curMethodEnv.enter(sym, result)
            }

          captureSymsWithEnclThis.zip(captureNames).foreach {
            case (sym, name) =>
              val value = buf.fieldload(genType(sym.tpe), self, name, Next.None)
              curMethodEnv.enter(sym, value)
          }

          val sym = targetTree.symbol
          val method = Val.Global(genMethodName(sym), Type.Ptr)
          val values =
            buf.genMethodArgs(sym, Ident(curClassSym.get) +: functionArgs)
          val sig = genMethodSig(sym)
          val res = buf.call(sig, method, values, Next.None)

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

        statBuf += Defn.Define(
          Attrs.None,
          funName,
          Type.Function(paramTypes, retType),
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
        Val.Global(ctorName, Type.Ptr),
        alloc +: captureVals,
        unwind
      )
      alloc
    }

    def ensureBoxed(
        value: Val,
        tpeEnteringPosterasure: Type,
        targetTpe: Type
    )(implicit buf: ExprBuffer, pos: nir.Position): Val = {
      tpeEnteringPosterasure match {
        case tpe if isPrimitiveValueType(tpe) =>
          buf.boxValue(targetTpe, value)

        case tpe: ErasedValueType =>
          val boxedClass = tpe.valueClazz
          val ctorName = genMethodName(boxedClass.primaryConstructor)
          val ctorSig = genMethodSig(boxedClass.primaryConstructor)

          val alloc = buf.classalloc(Global.Top(boxedClass.fullName), unwind)
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
        val seenSignatures = mutable.Set.empty[Sig]

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
          val sig = genName(sam).asInstanceOf[Global.Member].sig
          if (seenSignatures.add(sig))
            samsBuilder += sam
        }

        samsBuilder.result()
      }
    }

    def genApplyDynamic(app: ApplyDynamic): Val = {
      val ApplyDynamic(obj, args) = app
      val sym = app.symbol
      implicit val pos: nir.Position = app.pos

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
        val self = genExpr(obj)
        genApplyDynamic(sym, self, args)
      }
    }

    def genApplyDynamic(sym: Symbol, self: Val, argsp: Seq[Tree])(implicit
        pos: nir.Position
    ): Val = {
      val methodSig = genMethodSig(sym).asInstanceOf[Type.Function]
      val params = sym.tpe.params

      def isArrayLikeOp = {
        sym.name == nme.update &&
        params.size == 2 && params.head.tpe.typeSymbol == IntClass
      }

      def genDynCall(arrayUpdate: Boolean) = {

        // In the case of an array update we need to manually erase the return type.
        val methodName: Sig =
          if (arrayUpdate) {
            Sig.Proxy("update", Seq(Type.Int, Rt.Object))
          } else {
            val Global.Member(_, sig) = genMethodName(sym)
            sig.toProxy
          }

        val sig =
          Type.Function(
            methodSig.args.head ::
              methodSig.args.tail.map(ty => Type.box.getOrElse(ty, ty)).toList,
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
        val cond = ContTree { () =>
          buf.is(
            nir.Type.Ref(
              nir.Global.Top("scala.scalanative.runtime.ObjectArray")
            ),
            self,
            unwind
          )
        }
        val thenp = ContTree { () => genDynCall(arrayUpdate = true) }
        val elsep = ContTree { () => genDynCall(arrayUpdate = false) }
        genIf(
          nir.Type.Ref(nir.Global.Top("java.lang.Object")),
          cond,
          thenp,
          elsep
        )

      } else {
        genDynCall(arrayUpdate = false)
      }
    }

    def genApply(app: Apply): Val = {
      val Apply(fun, args) = app

      implicit val pos: nir.Position = app.pos

      fun match {
        case _: TypeApply =>
          genApplyTypeApply(app)
        case Select(Super(_, _), _) =>
          genApplyMethod(
            fun.symbol,
            statically = true,
            curMethodThis.get.get,
            args
          )
        case Select(New(_), nme.CONSTRUCTOR) =>
          genApplyNew(app)
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

    def genApplyLabel(tree: Tree): Val = {
      val Apply(fun, argsp) = tree
      val Val.Local(label, _) = curMethodEnv.resolve(fun.symbol)
      val args = genSimpleArgs(argsp)
      buf.jump(label, args)(tree.pos)
      Val.Unit
    }

    def genApplyBox(st: SimpleType, argp: Tree): Val = {
      val value = genExpr(argp)

      buf.box(genBoxType(st), value, unwind)(argp.pos)
    }

    def genApplyUnbox(st: SimpleType, argp: Tree)(implicit
        pos: nir.Position
    ): Val = {
      val value = genExpr(argp)
      value.ty match {
        case _: scalanative.nir.Type.I | _: scalanative.nir.Type.F =>
          // No need for unboxing, fixing some slack generated by the general
          // purpose Scala compiler.
          value
        case _ =>
          buf.unbox(genBoxType(st), value, unwind)
      }
    }

    def genApplyPrimitive(app: Apply): Val = {
      import scalaPrimitives._

      val Apply(fun @ Select(receiver, _), args) = app
      implicit val pos: nir.Position = app.pos

      val sym = app.symbol
      val code = scalaPrimitives.getPrimitive(sym, receiver.tpe)
      if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code)) {
        genSimpleOp(app, receiver :: args, code)
      } else if (code == CONCAT)
        genStringConcat(receiver, args.head)
      else if (code == HASH) genHashCode(args.head)
      else if (isArrayOp(code) || code == ARRAY_CLONE) genArrayOp(app, code)
      else if (nirPrimitives.isRawPtrOp(code)) genRawPtrOp(app, code)
      else if (nirPrimitives.isRawPtrCastOp(code)) genRawPtrCastOp(app, code)
      else if (code == CFUNCPTR_APPLY) genCFuncPtrApply(app, code)
      else if (code == CFUNCPTR_FROM_FUNCTION) genCFuncFromScalaFunction(app)
      else if (nirPrimitives.isRawSizeCastOp(code))
        genRawSizeCastOp(app, args.head, code)
      else if (isCoercion(code)) genCoercion(app, receiver, code)
      else if (code == SYNCHRONIZED) {
        val Apply(Select(receiverp, _), List(argp)) = app
        genSynchronized(receiverp, argp)(app.pos)
      } else if (code == STACKALLOC) genStackalloc(app)
      else if (code == CQUOTE) genCQuoteOp(app)
      else if (code == BOXED_UNIT) Val.Unit
      else if (code >= DIV_UINT && code <= ULONG_TO_DOUBLE)
        genUnsignedOp(app, code)
      else if (code == CLASS_FIELD_RAWPTR) genClassFieldRawPtr(app)
      else if (code == SIZE_OF) genSizeOf(app)
      else if (code == ALIGNMENT_OF) genAlignmentOf(app)
      else {
        abort(
          "Unknown primitive operation: " + sym.fullName + "(" +
            fun.symbol.simpleName + ") " + " at: " + (app.pos)
        )
      }
    }

    private final val ExternForwarderSig = Sig.Generated("$extern$forwarder")

    def getLinktimeCondition(condp: Tree): Option[LinktimeCondition] = {
      import LinktimeCondition._
      def genComparsion(name: Name, value: Val): Comp = {
        def intOrFloatComparison(onInt: Comp, onFloat: Comp)(implicit
            tpe: nir.Type
        ) =
          if (tpe.isInstanceOf[Type.F]) onFloat else onInt

        import Comp._
        implicit val tpe: nir.Type = value.ty
        name match {
          case nme.EQ => intOrFloatComparison(Ieq, Feq)
          case nme.NE => intOrFloatComparison(Ine, Fne)
          case nme.GT => intOrFloatComparison(Sgt, Fgt)
          case nme.GE => intOrFloatComparison(Sge, Fge)
          case nme.LT => intOrFloatComparison(Slt, Flt)
          case nme.LE => intOrFloatComparison(Sle, Fle)
          case nme =>
            globalError(condp.pos, s"Unsupported condition '$nme'"); Comp.Ine
        }
      }

      condp match {
        // if(bool) (...)
        case Apply(LinktimeProperty(name, _, position), Nil) =>
          Some {
            SimpleCondition(
              propertyName = name,
              comparison = Comp.Ieq,
              value = Val.True
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
              comparison = Comp.Ieq,
              value = Val.False
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
                case nme.ZAND => Bin.And
                case nme.ZOR  => Bin.Or
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

    def genFuncExternForwarder(funcName: Global, treeSym: Symbol)(implicit
        pos: nir.Position
    ): Defn = {
      val attrs = Attrs(isExtern = true)

      val sig = genMethodSig(treeSym)
      val externSig = genExternMethodSig(treeSym)

      val Type.Function(origtys, _) = sig
      val Type.Function(paramtys, retty) = externSig

      val methodName = genMethodName(treeSym)
      val method = Val.Global(methodName, Type.Ptr)
      val methodRef = Val.Global(methodName, origtys.head)

      val forwarderName = funcName.member(ExternForwarderSig)
      val forwarderBody = scoped(
        curUnwindHandler := None
      ) {
        val fresh = Fresh()
        val buf = new ExprBuffer()(fresh)

        val params = paramtys.map(ty => Val.Local(fresh(), ty))
        buf.label(fresh(), params)
        val boxedParams = params.zip(origtys.tail).map {
          case (param, ty) => buf.fromExtern(ty, param)
        }

        val res = buf.call(sig, method, methodRef +: boxedParams, Next.None)
        val unboxedRes = buf.toExtern(retty, res)
        buf.ret(unboxedRes)

        buf.toSeq
      }
      Defn.Define(attrs, forwarderName, externSig, forwarderBody)
    }

    def genCFuncFromScalaFunction(app: Apply): Val = {
      implicit val pos: nir.Position = app.pos
      val fn = app.args.head

      def withGeneratedForwarder(fnRef: Val)(sym: Symbol): Val = {
        val Type.Ref(className, _, _) = fnRef.ty
        curStatBuffer += genFuncExternForwarder(className, sym)
        fnRef
      }

      def reportClosingOverLocalState(args: Seq[Tree]): Unit =
        reporter.error(
          fn.pos,
          s"Closing over local state of ${args.map(v => show(v.symbol)).mkString(", ")} in function transformed to CFuncPtr results in undefined behaviour."
        )

      @tailrec
      def resolveFunction(tree: Tree): Val = tree match {
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
        Seq(Type.Ref(className), Type.Ptr),
        Type.Unit
      )
      val ctorName = className.member(Sig.Ctor(Seq(Type.Ptr)))
      val rawptr = buf.method(fnRef, ExternForwarderSig, unwind)

      val alloc = buf.classalloc(className, unwind)
      buf.call(
        ctorTy,
        Val.Global(ctorName, Type.Ptr),
        Seq(alloc, rawptr),
        unwind
      )
      alloc
    }

    def numOfType(num: Int, ty: nir.Type): Val = ty match {
      case Type.Byte              => Val.Byte(num.toByte)
      case Type.Short | Type.Char => Val.Short(num.toShort)
      case Type.Int               => Val.Int(num)
      case Type.Long              => Val.Long(num.toLong)
      case Type.Size              => Val.Size(num.toLong)
      case Type.Float             => Val.Float(num.toFloat)
      case Type.Double            => Val.Double(num.toDouble)
      case _                      => unsupported(s"num = $num, ty = ${ty.show}")
    }

    def genSimpleOp(app: Apply, args: List[Tree], code: Int): Val = {
      val retty = genType(app.tpe)

      implicit val pos: nir.Position = app.pos

      args match {
        case List(right)       => genUnaryOp(code, right, retty)
        case List(left, right) => genBinaryOp(code, left, right, retty)
        case _ => abort("Too many arguments for primitive function: " + app)
      }
    }

    def negateInt(value: nir.Val)(implicit pos: nir.Position): Val =
      buf.bin(Bin.Isub, value.ty, numOfType(0, value.ty), value, unwind)
    def negateFloat(value: nir.Val)(implicit pos: nir.Position): Val =
      buf.bin(Bin.Fmul, value.ty, numOfType(-1, value.ty), value, unwind)
    def negateBits(value: nir.Val)(implicit pos: nir.Position): Val =
      buf.bin(Bin.Xor, value.ty, numOfType(-1, value.ty), value, unwind)
    def negateBool(value: nir.Val)(implicit pos: nir.Position): Val =
      buf.bin(Bin.Xor, Type.Bool, Val.True, value, unwind)

    def genUnaryOp(code: Int, rightp: Tree, opty: nir.Type): Val = {
      import scalaPrimitives._

      implicit val pos: nir.Position = rightp.pos
      val right = genExpr(rightp)
      val coerced = genCoercion(right, right.ty, opty)

      (opty, code) match {
        case (_: Type.I | _: Type.F, POS) => coerced
        case (_: Type.I, NOT)             => negateBits(coerced)
        case (_: Type.F, NEG)             => negateFloat(coerced)
        case (_: Type.I, NEG)             => negateInt(coerced)
        case (Type.Bool, ZNOT)            => negateBool(coerced)
        case _ => abort("Unknown unary operation code: " + code)
      }
    }

    def genBinaryOp(code: Int, left: Tree, right: Tree, retty: nir.Type)(
        implicit exprPos: nir.Position
    ): Val = {
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
        case _: Type.F =>
          code match {
            case ADD =>
              genBinaryOp(Op.Bin(Bin.Fadd, _, _, _), left, right, opty)
            case SUB =>
              genBinaryOp(Op.Bin(Bin.Fsub, _, _, _), left, right, opty)
            case MUL =>
              genBinaryOp(Op.Bin(Bin.Fmul, _, _, _), left, right, opty)
            case DIV =>
              genBinaryOp(Op.Bin(Bin.Fdiv, _, _, _), left, right, opty)
            case MOD =>
              genBinaryOp(Op.Bin(Bin.Frem, _, _, _), left, right, opty)

            case EQ =>
              genBinaryOp(Op.Comp(Comp.Feq, _, _, _), left, right, opty)
            case NE =>
              genBinaryOp(Op.Comp(Comp.Fne, _, _, _), left, right, opty)
            case LT =>
              genBinaryOp(Op.Comp(Comp.Flt, _, _, _), left, right, opty)
            case LE =>
              genBinaryOp(Op.Comp(Comp.Fle, _, _, _), left, right, opty)
            case GT =>
              genBinaryOp(Op.Comp(Comp.Fgt, _, _, _), left, right, opty)
            case GE =>
              genBinaryOp(Op.Comp(Comp.Fge, _, _, _), left, right, opty)

            case _ =>
              abort(
                "Unknown floating point type binary operation code: " + code
              )
          }

        case Type.Bool | _: Type.I =>
          code match {
            case ADD =>
              genBinaryOp(Op.Bin(Bin.Iadd, _, _, _), left, right, opty)
            case SUB =>
              genBinaryOp(Op.Bin(Bin.Isub, _, _, _), left, right, opty)
            case MUL =>
              genBinaryOp(Op.Bin(Bin.Imul, _, _, _), left, right, opty)
            case DIV =>
              genBinaryOp(Op.Bin(Bin.Sdiv, _, _, _), left, right, opty)
            case MOD =>
              genBinaryOp(Op.Bin(Bin.Srem, _, _, _), left, right, opty)

            case OR =>
              genBinaryOp(Op.Bin(Bin.Or, _, _, _), left, right, opty)
            case XOR =>
              genBinaryOp(Op.Bin(Bin.Xor, _, _, _), left, right, opty)
            case AND =>
              genBinaryOp(Op.Bin(Bin.And, _, _, _), left, right, opty)
            case LSL =>
              genBinaryOp(Op.Bin(Bin.Shl, _, _, _), left, right, opty)
            case LSR =>
              genBinaryOp(Op.Bin(Bin.Lshr, _, _, _), left, right, opty)
            case ASR =>
              genBinaryOp(Op.Bin(Bin.Ashr, _, _, _), left, right, opty)

            case EQ =>
              genBinaryOp(Op.Comp(Comp.Ieq, _, _, _), left, right, opty)
            case NE =>
              genBinaryOp(Op.Comp(Comp.Ine, _, _, _), left, right, opty)
            case LT =>
              genBinaryOp(Op.Comp(Comp.Slt, _, _, _), left, right, opty)
            case LE =>
              genBinaryOp(Op.Comp(Comp.Sle, _, _, _), left, right, opty)
            case GT =>
              genBinaryOp(Op.Comp(Comp.Sgt, _, _, _), left, right, opty)
            case GE =>
              genBinaryOp(Op.Comp(Comp.Sge, _, _, _), left, right, opty)

            case ZOR =>
              genIf(retty, left, Literal(Constant(true)), right)
            case ZAND =>
              genIf(retty, left, right, Literal(Constant(false)))

            case _ =>
              abort("Unknown integer type binary operation code: " + code)
          }

        case _: Type.RefKind =>
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

        case Type.Ptr =>
          code match {
            case EQ | ID =>
              genBinaryOp(Op.Comp(Comp.Ieq, _, _, _), left, right, opty)
            case NE | NI =>
              genBinaryOp(Op.Comp(Comp.Ine, _, _, _), left, right, opty)
          }

        case ty =>
          abort("Unknown binary operation type: " + ty)
      }

      genCoercion(binres, binres.ty, retty)(right.pos)
    }

    def genBinaryOp(
        op: (nir.Type, Val, Val) => Op,
        leftp: Tree,
        rightp: Tree,
        opty: nir.Type
    ): Val = {
      val leftty = genType(leftp.tpe)
      val left = genExpr(leftp)
      val leftcoerced = genCoercion(left, leftty, opty)(leftp.pos)
      val rightty = genType(rightp.tpe)
      val right = genExpr(rightp)
      val rightcoerced = genCoercion(right, rightty, opty)(rightp.pos)

      buf.let(op(opty, leftcoerced, rightcoerced), unwind)(leftp.pos)
    }

    def genClassEquality(
        leftp: Tree,
        rightp: Tree,
        ref: Boolean,
        negated: Boolean
    ): Val = {
      val left = genExpr(leftp)
      implicit val pos: nir.Position = rightp.pos

      if (ref) {
        val right = genExpr(rightp)
        val comp = if (negated) Comp.Ine else Comp.Ieq
        buf.comp(comp, Rt.Object, left, right, unwind)
      } else {
        val thenn, elsen, mergen = fresh()
        val mergev = Val.Local(fresh(), nir.Type.Bool)

        val isnull = buf.comp(Comp.Ieq, Rt.Object, left, Val.Null, unwind)
        buf.branch(isnull, Next(thenn), Next(elsen))
        locally {
          buf.label(thenn)
          val right = genExpr(rightp)
          val thenv = buf.comp(Comp.Ieq, Rt.Object, right, Val.Null, unwind)
          buf.jump(mergen, Seq(thenv))
        }
        locally {
          buf.label(elsen)
          val elsev = genApplyMethod(
            NObjectEqualsMethod,
            statically = false,
            left,
            Seq(rightp)
          )
          buf.jump(mergen, Seq(elsev))
        }
        buf.label(mergen, Seq(mergev))
        if (negated) negateBool(mergev) else mergev
      }
    }

    def binaryOperationType(lty: nir.Type, rty: nir.Type) = (lty, rty) match {
      // Bug compatibility with scala/bug/issues/11253
      case (Type.Long, Type.Float) =>
        Type.Double
      case (nir.Type.Ptr, _: nir.Type.RefKind) =>
        lty
      case (_: nir.Type.RefKind, nir.Type.Ptr) =>
        rty
      case (nir.Type.Bool, nir.Type.Bool) =>
        nir.Type.Bool
      case (nir.Type.FixedSizeI(lwidth, _), nir.Type.FixedSizeI(rwidth, _))
          if lwidth < 32 && rwidth < 32 =>
        nir.Type.Int
      case (nir.Type.FixedSizeI(lwidth, _), nir.Type.FixedSizeI(rwidth, _)) =>
        if (lwidth >= rwidth) lty else rty
      case (nir.Type.FixedSizeI(_, _), nir.Type.F(_)) =>
        rty
      case (nir.Type.F(_), nir.Type.FixedSizeI(_, _)) =>
        lty
      case (nir.Type.F(lwidth), nir.Type.F(rwidth)) =>
        if (lwidth >= rwidth) lty else rty
      case (_: nir.Type.RefKind, _: nir.Type.RefKind) =>
        Rt.Object
      case (ty1, ty2) if ty1 == ty2 =>
        ty1
      case (Type.Nothing, ty) => ty
      case (ty, Type.Nothing) => ty

      case _ =>
        abort(s"can't perform binary operation between $lty and $rty")
    }

    def genStringConcat(leftp: Tree, rightp: Tree): Val = {
      def stringify(sym: Symbol, value: Val)(implicit
          pos: nir.Position
      ): Val = {
        val cond = ContTree { () =>
          buf.comp(Comp.Ieq, Rt.Object, value, Val.Null, unwind)
        }
        val thenp = ContTree { () => Val.String("null") }
        val elsep = ContTree { () =>
          if (sym == StringClass) {
            value
          } else {
            val meth = Object_toString
            genApplyMethod(meth, statically = false, value, Seq.empty)
          }
        }
        genIf(Rt.String, cond, thenp, elsep)
      }

      val left = {
        implicit val pos: nir.Position = leftp.pos

        val typesym = leftp.tpe.typeSymbol
        val unboxed = genExpr(leftp)
        val boxed = boxValue(typesym, unboxed)
        stringify(typesym, boxed)
      }

      val right = {
        val typesym = rightp.tpe.typeSymbol
        val boxed = genExpr(rightp)
        stringify(typesym, boxed)(rightp.pos)
      }

      genApplyMethod(String_+, statically = true, left, Seq(ValTree(right)))(
        leftp.pos
      )
    }

    def genHashCode(argp: Tree)(implicit pos: nir.Position): Val = {
      val arg = boxValue(argp.tpe, genExpr(argp))
      val isnull =
        buf.comp(Comp.Ieq, Rt.Object, arg, Val.Null, unwind)(argp.pos)
      val cond = ValTree(isnull)
      val thenp = ValTree(Val.Int(0))
      val elsep = ContTree { () =>
        val meth = NObjectHashCodeMethod
        genApplyMethod(meth, statically = false, arg, Seq.empty)
      }
      genIf(Type.Int, cond, thenp, elsep)
    }

    def genArrayOp(app: Apply, code: Int): Val = {
      import scalaPrimitives._

      val Apply(Select(arrayp, _), argsp) = app

      val Type.Array(elemty, _) = genType(arrayp.tpe)

      def elemcode = genArrayCode(arrayp.tpe)
      val array = genExpr(arrayp)

      implicit val pos: nir.Position = app.pos

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

    def boxValue(st: SimpleType, value: Val)(implicit pos: nir.Position): Val =
      st.sym match {
        case UByteClass | UShortClass | UIntClass | ULongClass | USizeClass =>
          genApplyModuleMethod(
            RuntimeBoxesModule,
            BoxUnsignedMethod(st.sym),
            Seq(ValTree(value))
          )
        case _ =>
          if (genPrimCode(st) == 'O') {
            value
          } else {
            genApplyBox(st, ValTree(value))
          }
      }

    def unboxValue(st: SimpleType, partial: Boolean, value: Val)(implicit
        pos: nir.Position
    ): Val = st.sym match {
      case UByteClass | UShortClass | UIntClass | ULongClass | USizeClass =>
        // Results of asInstanceOfs are partially unboxed, meaning
        // that non-standard value types remain to be boxed.
        if (partial) {
          value
        } else {
          genApplyModuleMethod(
            RuntimeBoxesModule,
            UnboxUnsignedMethod(st.sym),
            Seq(ValTree(value))
          )
        }
      case _ =>
        if (genPrimCode(st) == 'O') {
          value
        } else {
          genApplyUnbox(st, ValTree(value))
        }
    }

    def genRawPtrOp(app: Apply, code: Int): Val = code match {
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

    def genRawPtrLoadOp(app: Apply, code: Int): Val = {
      val Apply(_, Seq(ptrp)) = app

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
        case LOAD_OBJECT   => Rt.Object
      }
      val syncAttrs =
        if (!ptrp.symbol.isVolatile) None
        else Some(SyncAttrs(MemoryOrder.Acquire))
      buf.load(ty, ptr, unwind, syncAttrs)(app.pos)
    }

    def genRawPtrStoreOp(app: Apply, code: Int): Val = {
      val Apply(_, Seq(ptrp, valuep)) = app

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
        case STORE_OBJECT   => Rt.Object
      }
      val syncAttrs =
        if (!ptrp.symbol.isVolatile) None
        else Some(SyncAttrs(MemoryOrder.Release))
      buf.store(ty, ptr, value, unwind, syncAttrs)(app.pos)
    }

    def genRawPtrElemOp(app: Apply, code: Int): Val = {
      val Apply(_, Seq(ptrp, offsetp)) = app

      val ptr = genExpr(ptrp)
      val offset = genExpr(offsetp)

      buf.elem(Type.Byte, ptr, Seq(offset), unwind)(app.pos)
    }

    def genRawPtrCastOp(app: Apply, code: Int): Val = {
      val Apply(_, Seq(argp)) = app

      val fromty = genType(argp.tpe)
      val toty = genType(app.tpe)
      val value = genExpr(argp)

      genCastOp(fromty, toty, value)(app.pos)
    }

    def genRawSizeCastOp(app: Apply, receiver: Tree, code: Int): Val = {
      val rec = genExpr(receiver)
      val (fromty, toty, conv) = code match {
        case CAST_RAWSIZE_TO_INT =>
          (nir.Type.Size, nir.Type.Int, Conv.SSizeCast)
        case CAST_RAWSIZE_TO_LONG =>
          (nir.Type.Size, nir.Type.Long, Conv.SSizeCast)
        case CAST_RAWSIZE_TO_LONG_UNSIGNED =>
          (nir.Type.Size, nir.Type.Long, Conv.ZSizeCast)
        case CAST_INT_TO_RAWSIZE =>
          (nir.Type.Int, nir.Type.Size, Conv.SSizeCast)
        case CAST_INT_TO_RAWSIZE_UNSIGNED =>
          (nir.Type.Int, nir.Type.Size, Conv.ZSizeCast)
        case CAST_LONG_TO_RAWSIZE =>
          (nir.Type.Long, nir.Type.Size, Conv.SSizeCast)
      }

      buf.conv(conv, toty, rec, unwind)(app.pos)
    }

    def castConv(fromty: nir.Type, toty: nir.Type): Option[nir.Conv] =
      (fromty, toty) match {
        case (_: Type.I, Type.Ptr)              => Some(nir.Conv.Inttoptr)
        case (Type.Ptr, _: Type.I)              => Some(nir.Conv.Ptrtoint)
        case (_: Type.RefKind, Type.Ptr)        => Some(nir.Conv.Bitcast)
        case (Type.Ptr, _: Type.RefKind)        => Some(nir.Conv.Bitcast)
        case (_: Type.RefKind, _: Type.RefKind) => Some(nir.Conv.Bitcast)
        case (_: Type.RefKind, _: Type.I)       => Some(nir.Conv.Ptrtoint)
        case (_: Type.I, _: Type.RefKind)       => Some(nir.Conv.Inttoptr)
        case (Type.FixedSizeI(w1, _), Type.F(w2)) if w1 == w2 =>
          Some(nir.Conv.Bitcast)
        case (Type.F(w1), Type.FixedSizeI(w2, _)) if w1 == w2 =>
          Some(nir.Conv.Bitcast)
        case _ if fromty == toty       => None
        case (Type.Float, Type.Double) => Some(nir.Conv.Fpext)
        case (Type.Double, Type.Float) => Some(nir.Conv.Fptrunc)
        case _ =>
          unsupported(s"cast from $fromty to $toty")
      }

    /** Generates direct call to function ptr with optional unboxing arguments
     *  and boxing result Apply.args can contain different number of arguments
     *  depending on usage, however they are passed in constant order:
     *    - 0..N args
     *    - return type evidence
     */
    def genCFuncPtrApply(app: Apply, code: Int): Val = {
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

      implicit val pos: nir.Position = app.pos

      val self = genExpr(receiverp)
      val retType = genType(paramTypes.last)
      val unboxedRetType = Type.unbox.getOrElse(retType, retType)

      val args = aargs
        .zip(paramTypes)
        .map {
          case (arg, ty) =>
            val tpe = genType(ty)
            val obj = genExpr(arg)

            /* buf.unboxValue does not handle Ref( Ptr | CArray | ... ) unboxing
             * That's why we're doing it directly */
            if (Type.unbox.isDefinedAt(tpe)) {
              buf.unbox(tpe, obj, unwind)(arg.pos)
            } else {
              buf.unboxValue(fromType(ty), partial = false, obj)(arg.pos)
            }
        }
      val argTypes = args.map(_.ty)
      val funcSig = Type.Function(argTypes, unboxedRetType)

      val selfName = genTypeName(CFuncPtrClass)
      val getRawPtrName = selfName
        .member(Sig.Field("rawptr", Sig.Scope.Private(selfName)))

      val target = buf.fieldload(Type.Ptr, self, getRawPtrName, unwind)
      val result = buf.call(funcSig, target, args, unwind)
      if (retType != unboxedRetType)
        buf.box(retType, result, unwind)
      else {
        boxValue(paramTypes.last, result)
      }
    }

    def genCastOp(fromty: nir.Type, toty: nir.Type, value: Val)(implicit
        pos: nir.Position
    ): Val =
      castConv(fromty, toty).fold(value)(buf.conv(_, toty, value, unwind))

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

    private def getUnboxedSize(sizep: Tree)(implicit pos: nir.Position): Val =
      sizep match {
        // Optimize call, strip numeric conversions
        case Literal(Constant(size: Int)) => Val.Size(size)
        case Block(Nil, expr)             => getUnboxedSize(expr)
        case Apply(fun, List(arg))
            if optimizedFunctions.contains(fun.symbol) ||
              arg.symbol.exists && optimizedFunctions.contains(arg.symbol) =>
          getUnboxedSize(arg)
        case Typed(expr, _) => getUnboxedSize(expr)
        case _              =>
          // actual unboxing
          val size = genExpr(sizep)
          val sizeTy = Type.normalize(size.ty)
          val unboxed =
            if (Type.unbox.contains(sizeTy)) buf.unbox(sizeTy, size, unwind)
            else if (Type.box.contains(sizeTy)) size
            else {
              reporter.error(
                sizep.pos,
                s"Invalid usage of Intrinsic.stackalloc, argument is not an integer type: ${sizeTy}"
              )
              Val.Size(0)
            }

          if (unboxed.ty == nir.Type.Size) unboxed
          else buf.conv(Conv.SSizeCast, nir.Type.Size, unboxed, unwind)
      }

    private def genStackalloc(app: Apply): Val = app match {
      case Apply(_, args) => {
        implicit val pos: nir.Position = app.pos
        val tpe = app.attachments
          .get[NonErasedType]
          .map(v => genType(v.tpe, deconstructValueTypes = true))
          .getOrElse {
            reporter.error(
              app.pos,
              "Not found type attachment for stackalloc operation, report it as a bug."
            )
            Type.Nothing
          }

        val size = args match {
          case Seq()         => Val.Size(1)
          case Seq(sizep)    => getUnboxedSize(sizep)
          case Seq(_, sizep) => getUnboxedSize(sizep)
        }
        buf.stackalloc(tpe, size, unwind)
      }
    }

    def genCQuoteOp(app: Apply): Val = {
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
          val bytes = Val.ByteString(StringUtils.processEscapes(str))
          val const = Val.Const(bytes)
          buf.box(nir.Rt.BoxedPtr, const, unwind)(app.pos)

        case _ =>
          unsupported(app)
      }
    }

    def genUnsignedOp(app: Tree, code: Int): Val = {
      implicit val pos: nir.Position = app.pos
      app match {
        case Apply(_, Seq(argp)) if code == UNSIGNED_OF =>
          val ty = genType(app.tpe.resultType)
          val arg = genExpr(argp)

          buf.box(ty, arg, unwind)

        case Apply(_, Seq(argp))
            if code >= BYTE_TO_UINT && code <= INT_TO_ULONG =>
          val ty = genType(app.tpe)
          val arg = genExpr(argp)

          buf.conv(Conv.Zext, ty, arg, unwind)

        case Apply(_, Seq(argp))
            if code >= UINT_TO_FLOAT && code <= ULONG_TO_DOUBLE =>
          val ty = genType(app.tpe)
          val arg = genExpr(argp)

          buf.conv(Conv.Uitofp, ty, arg, unwind)

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

    def genClassFieldRawPtr(app: Apply)(implicit pos: nir.Position): Val = {
      val Apply(_, List(target, fieldName: Literal)) = app
      val fieldNameId = fieldName.value.stringValue
      val classInfo = target.tpe.finalResultType
      val classInfoSym = classInfo.typeSymbol.asClass
      def matchesName(f: Symbol) = {
        f.nameString == TermName(fieldNameId).toString()
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
          Val.Int(-1)
        }

    }

    def genSizeOf(app: Apply)(implicit pos: nir.Position): Val =
      genLayoutValueOf("sizeOf", buf.sizeOf(_, unwind))(app)

    def genAlignmentOf(app: Apply)(implicit pos: nir.Position): Val =
      genLayoutValueOf("alignmentOf", buf.alignmentOf(_, unwind))(app)

    // used as internal implementation of sizeOf / alignmentOf
    private def genLayoutValueOf(opType: String, toVal: nir.Type => Val)(
        app: Apply
    )(implicit pos: nir.Position): Val = {
      def fail(msg: => String) = {
        reporter.error(app.pos, msg)
        Val.Zero(Type.Size)
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
        pos: nir.Position
    ): Val = {
      genSynchronized(receiverp)(_.genExpr(bodyp))
    }

    def genSynchronized(
        receiverp: Tree
    )(bodyGen: ExprBuffer => Val)(implicit pos: nir.Position): Val = {
      // Here we wrap the synchronized call into the try-finally block
      // to ensure that monitor would be released even in case of the exception
      // or in case of non-local returns
      val nested = new ExprBuffer()
      val normaln = fresh()
      val handler = fresh()
      val mergen = fresh()

      // scalanative.runtime.`package`.enterMonitor(receiver)
      genExpr(
        treeBuild.mkMethodCall(RuntimeEnterMonitorMethod, List(receiverp))
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
        val excv = Val.Local(fresh(), Rt.Object)
        nested.label(handler, Seq(excv))
        nested.raise(excv, unwind)
        nested.jumpExcludeUnitValue(retty)(mergen, Val.Zero(retty))
      }

      // Append try/catch instructions to the outher instruction buffer.
      buf.jump(Next(normaln))
      buf ++= genTryFinally(
        // scalanative.runtime.`package`.exitMonitor(receiver)
        finallyp =
          treeBuild.mkMethodCall(RuntimeExitMonitorMethod, List(receiverp)),
        insts = nested.toSeq
      )
      val mergev = Val.Local(fresh(), retty)
      buf.labelExcludeUnitValue(mergen, mergev)
    }

    def genCoercion(app: Apply, receiver: Tree, code: Int): Val = {
      val rec = genExpr(receiver)
      val (fromty, toty) = coercionTypes(code)

      genCoercion(rec, fromty, toty)(app.pos)
    }

    def genCoercion(value: Val, fromty: nir.Type, toty: nir.Type)(implicit
        pos: nir.Position
    ): Val = {
      if (fromty == toty) {
        value
      } else {
        val conv = (fromty, toty) match {
          case (nir.Type.Ptr, _: nir.Type.RefKind) =>
            Conv.Bitcast
          case (_: nir.Type.RefKind, nir.Type.Ptr) =>
            Conv.Bitcast
          case (
                nir.Type.FixedSizeI(fromw, froms),
                nir.Type.FixedSizeI(tow, tos)
              ) =>
            if (fromw < tow) {
              if (froms) {
                Conv.Sext
              } else {
                Conv.Zext
              }
            } else if (fromw > tow) {
              Conv.Trunc
            } else {
              Conv.Bitcast
            }
          case (i: nir.Type.I, _: nir.Type.F) if i.signed =>
            Conv.Sitofp
          case (i: nir.Type.I, _: nir.Type.F) if !i.signed =>
            Conv.Uitofp
          case (_: nir.Type.F, nir.Type.FixedSizeI(iwidth, true)) =>
            if (iwidth < 32) {
              val ivalue = genCoercion(value, fromty, Type.Int)
              return genCoercion(ivalue, Type.Int, toty)
            }
            Conv.Fptosi
          case (_: nir.Type.F, nir.Type.FixedSizeI(iwidth, false)) =>
            if (iwidth < 32) {
              val ivalue = genCoercion(value, fromty, Type.Int)
              return genCoercion(ivalue, Type.Int, toty)
            }
            Conv.Fptoui
          case (nir.Type.Double, nir.Type.Float) =>
            Conv.Fptrunc
          case (nir.Type.Float, nir.Type.Double) =>
            Conv.Fpext
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

    def genApplyTypeApply(app: Apply): Val = {
      val Apply(TypeApply(fun @ Select(receiverp, _), targs), argsp) = app

      val fromty = genType(receiverp.tpe)
      val toty = genType(targs.head.tpe)
      def boxty = genBoxType(targs.head.tpe)
      val value = genExpr(receiverp)
      def boxed = boxValue(receiverp.tpe, value)(receiverp.pos)

      implicit val pos: nir.Position = fun.pos

      fun.symbol match {
        case Object_isInstanceOf =>
          buf.is(boxty, boxed, unwind)

        case Object_asInstanceOf =>
          (fromty, toty) match {
            case _ if boxed.ty == boxty =>
              boxed
            case (_: Type.PrimitiveKind, _: Type.PrimitiveKind) =>
              genCoercion(value, fromty, toty)
            case (_, Type.Nothing) =>
              val runtimeNothing = genType(RuntimeNothingClass)
              val isNullL, notNullL = fresh()
              val isNull = buf.comp(Comp.Ieq, boxed.ty, boxed, Val.Null, unwind)
              buf.branch(isNull, Next(isNullL), Next(notNullL))
              buf.label(isNullL)
              buf.raise(Val.Null, unwind)
              buf.label(notNullL)
              buf.as(runtimeNothing, boxed, unwind)
              buf.unreachable(unwind)
              buf.label(fresh())
              Val.Zero(Type.Nothing)
            case _ =>
              val cast = buf.as(boxty, boxed, unwind)
              unboxValue(app.tpe, partial = true, cast)(app.pos)
          }

        case Object_synchronized =>
          assert(argsp.size == 1, "synchronized with wrong number of args")
          genSynchronized(ValTree(boxed), argsp.head)
      }
    }

    def genApplyNew(app: Apply): Val = {
      val Apply(fun @ Select(New(tpt), nme.CONSTRUCTOR), args) = app
      implicit val pos: nir.Position = app.pos

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

    def genApplyNewStruct(st: SimpleType, argsp: Seq[Tree]): Val = {
      val ty = genType(st)
      val args = genSimpleArgs(argsp)
      var res: Val = Val.Zero(ty)

      args.zip(argsp).zipWithIndex.foreach {
        case ((arg, argp), idx) =>
          res = buf.insert(res, arg, Seq(idx), unwind)(argp.pos)
      }

      res
    }

    def genApplyNewArray(targ: SimpleType, argsp: Seq[Tree])(implicit
        pos: nir.Position
    ): Val = {
      val Seq(lengthp) = argsp
      val length = genExpr(lengthp)

      buf.arrayalloc(genType(targ), length, unwind)
    }

    def genApplyNew(clssym: Symbol, ctorsym: Symbol, args: List[Tree])(implicit
        pos: nir.Position
    ): Val = {
      val alloc = buf.classalloc(genTypeName(clssym), unwind)
      val call = genApplyMethod(ctorsym, statically = true, alloc, args)
      alloc
    }

    def genApplyModuleMethod(module: Symbol, method: Symbol, args: Seq[Tree])(
        implicit pos: nir.Position
    ): Val = {
      val self = genModule(module)
      genApplyMethod(method, statically = true, self, args)
    }

    def genApplyMethod(
        sym: Symbol,
        statically: Boolean,
        selfp: Tree,
        argsp: Seq[Tree]
    )(implicit pos: nir.Position): Val = {
      if (sym.owner.isExternType && sym.isAccessor) {
        genApplyExternAccessor(sym, argsp)
      } else if (sym.isStaticMember) {
        genApplyStaticMethod(sym, selfp, argsp)
      } else {
        val self = genExpr(selfp)
        genApplyMethod(sym, statically, self, argsp)
      }
    }

    private def genApplyStaticMethod(
        sym: Symbol,
        receiver: Tree,
        argsp: Seq[Tree]
    )(implicit pos: nir.Position): Val = {
      require(!sym.owner.isExternType, sym.owner)
      val name = genStaticMemberName(sym, receiver.symbol)
      val method = Val.Global(name, nir.Type.Ptr)
      val sig = genMethodSig(sym)
      val args = genMethodArgs(sym, argsp)
      buf.call(sig, method, args, unwind)
    }

    def genApplyExternAccessor(sym: Symbol, argsp: Seq[Tree])(implicit
        pos: nir.Position
    ): Val = {
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
        pos: nir.Position
    ): Val = {
      assert(sym.owner.isExternType, "loadExtern was not extern")

      val name = Val.Global(genName(sym), Type.Ptr)
      val syncAttrs =
        if (!sym.isVolatile) None
        else Some(SyncAttrs(MemoryOrder.Acquire))

      fromExtern(
        ty,
        buf.load(externTy, name, unwind, syncAttrs)
      )
    }

    def genStoreExtern(externTy: nir.Type, sym: Symbol, value: Val)(implicit
        pos: nir.Position
    ): Val = {
      assert(sym.owner.isExternType, "storeExtern was not extern")
      val name = Val.Global(genName(sym), Type.Ptr)
      val externValue = toExtern(externTy, value)
      val syncAttrs =
        if (!sym.isVolatile) None
        else Some(SyncAttrs(MemoryOrder.Release))

      buf.store(externTy, name, externValue, unwind, syncAttrs)
    }

    def toExtern(expectedTy: nir.Type, value: Val)(implicit
        pos: nir.Position
    ): Val =
      (expectedTy, value.ty) match {
        case (_, refty: Type.Ref)
            if Type.boxClasses.contains(refty.name)
              && Type.unbox(Type.Ref(refty.name)) == expectedTy =>
          buf.unbox(Type.Ref(refty.name), value, unwind)
        case _ =>
          value
      }

    def fromExtern(expectedTy: nir.Type, value: Val)(implicit
        pos: nir.Position
    ): Val =
      (expectedTy, value.ty) match {
        case (refty: nir.Type.Ref, ty)
            if Type.boxClasses.contains(refty.name)
              && Type.unbox(Type.Ref(refty.name)) == ty =>
          buf.box(Type.Ref(refty.name), value, unwind)
        case _ =>
          value
      }

    def genApplyMethod(
        sym: Symbol,
        statically: Boolean,
        self: Val,
        argsp: Seq[Tree]
    )(implicit pos: nir.Position): Val = {
      val owner = sym.owner
      val name = genMethodName(sym)
      val origSig = genMethodSig(sym)
      val isExtern = owner.isExternType
      val sig =
        if (isExtern) {
          genExternMethodSig(sym)
        } else {
          origSig
        }
      val args = genMethodArgs(sym, argsp)
      val method =
        if (statically || owner.isStruct || isExtern) {
          Val.Global(name, nir.Type.Ptr)
        } else {
          val Global.Member(_, sig) = name
          buf.method(self, sig, unwind)
        }
      val values =
        if (sym.isStaticInNIR) args
        else self +: args

      val res = buf.call(sig, method, values, unwind)

      if (!isExtern) {
        res
      } else {
        val Type.Function(_, retty) = origSig
        fromExtern(retty, res)
      }
    }

    def genMethodArgs(sym: Symbol, argsp: Seq[Tree]): Seq[Val] =
      if (sym.owner.isExternType) genExternMethodArgs(sym, argsp)
      else genSimpleArgs(argsp)

    private def genSimpleArgs(argsp: Seq[Tree]): Seq[Val] = argsp.map(genExpr)

    private def genExternMethodArgs(sym: Symbol, argsp: Seq[Tree]): Seq[Val] = {
      val res = Seq.newBuilder[Val]
      val nir.Type.Function(argTypes, _) = genExternMethodSig(sym)
      val paramSyms = sym.tpe.params
      assert(
        argTypes.size == argsp.size && argTypes.size == paramSyms.size,
        "Different number of arguments passed to method signature and apply method"
      )

      def genArg(
          argp: Tree,
          paramTpe: global.Type
      ): nir.Val = {
        implicit def pos: nir.Position = argp.pos
        val externType = genExternType(paramTpe)
        val value = (genExpr(argp), Type.box.get(externType)) match {
          case (value @ Val.Null, Some(unboxedType)) =>
            externType match {
              case Type.Ptr | _: Type.RefKind => value
              case _ =>
                reporter.warning(
                  argp.pos,
                  s"Passing null as argument of type ${paramTpe} to the extern method is unsafe. " +
                    s"The argument would be unboxed to primitive value of type $externType."
                )
                Val.Zero(unboxedType)
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
                  implicit def pos: nir.Position = tree.pos
                  val sym = tree.symbol
                  val tpe =
                    if (tree.symbol != null && tree.symbol.exists)
                      tree.symbol.tpe.finalResultType
                    else tree.tpe
                  val arg = genArg(tree, tpe)
                  def isUnsigned = Type.isUnsignedType(genType(tpe))
                  // Decimal varargs needs to be promoted to at least Int, and float needs to be promoted to Double
                  val promotedArg = arg.ty match {
                    case Type.Float =>
                      this.genCastOp(Type.Float, Type.Double, arg)
                    case Type.FixedSizeI(width, _) if width < Type.Int.width =>
                      val conv =
                        if (isUnsigned) nir.Conv.Zext
                        else nir.Conv.Sext
                      buf.conv(conv, Type.Int, arg, unwind)
                    case Type.Long =>
                      // On 32-bit systems Long needs to be truncated to Int
                      // Cast it to size to make undependent from architecture
                      val conv =
                        if (isUnsigned) nir.Conv.ZSizeCast
                        else nir.Conv.SSizeCast
                      buf.conv(conv, Type.Size, arg, unwind)
                    case _ => arg
                  }
                  res += promotedArg
                }
              // Scala 2.13 only
              case Select(_, name) if name == definitions.NilModule.name => ()
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

    private def labelExcludeUnitValue(label: Local, value: nir.Val.Local)(
        implicit pos: nir.Position
    ): nir.Val =
      value.ty match {
        case Type.Unit => buf.label(label); Val.Unit
        case _         => buf.label(label, Seq(value)); value
      }

    private def jumpExcludeUnitValue(
        mergeType: nir.Type
    )(label: Local, value: nir.Val)(implicit pos: nir.Position): Unit =
      mergeType match {
        case Type.Unit => buf.jump(label, Nil)
        case _         => buf.jump(label, Seq(value))
      }
  }
}

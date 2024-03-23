package scala.scalanative
package nscplugin

import scala.language.implicitConversions
import scala.annotation.{tailrec, switch}

import dotty.tools.dotc.ast
import ast.tpd._
import dotty.tools.backend.ScalaPrimitivesOps._
import dotty.tools.dotc.core
import core.Contexts._
import core.Symbols._
import core.Names._
import core.Types._
import core.Constants._
import core.StdNames._
import core.Flags._
import core.Denotations._
import core.SymDenotations._
import core.TypeErasure.ErasedValueType
import core._
import dotty.tools.FatalError
import dotty.tools.dotc.report
import dotty.tools.dotc.transform
import dotty.tools.dotc.util.Spans.*
import transform.{ValueClasses, Erasure}
import dotty.tools.backend.jvm.DottyBackendInterface.symExtensions
import scala.scalanative.nscplugin.CompilerCompat.SymUtilsCompat.*

import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.util.ScopedVar.scoped
import scala.scalanative.util.unsupported
import scala.scalanative.util.StringUtils
import dotty.tools.dotc.ast.desugar
import dotty.tools.dotc.util.Property
import scala.scalanative.nscplugin.NirDefinitions.NonErasedType
import scala.scalanative.util.unreachable

trait NirGenExpr(using Context) {
  self: NirCodeGen =>
  import positionsConversions.fromSpan

  sealed case class ValTree(value: nir.Val)(
      span: Span = NoSpan
  ) extends Tree { this.span = span }

  def ValTree(from: Tree)(value: nir.Val) =
    new ValTree(value = value)(span = from.span)

  sealed case class ContTree(f: ExprBuffer => nir.Val)(
      span: Span = NoSpan
  ) extends Tree { this.span = span }

  def ContTree(from: Tree)(build: ExprBuffer => nir.Val) =
    new ContTree(f = build)(span = from.span)

  def fallbackSourcePosition: nir.SourcePosition = curMethodSym.get.span

  class ExprBuffer(using fresh: nir.Fresh) extends FixupBuffer {
    buf =>

    def genExpr(tree: Tree): nir.Val = {
      tree match {
        case EmptyTree      => nir.Val.Unit
        case ValTree(value) => value
        case ContTree(f)    => f(this)
        case tree: Apply =>
          val updatedTree = lazyValsAdapter.transformApply(tree)
          genApply(updatedTree)
        case tree: Assign         => genAssign(tree)
        case tree: Block          => genBlock(tree)
        case tree: Closure        => genClosure(tree)
        case tree: Labeled        => genLabelDef(tree)
        case tree: Ident          => genIdent(tree)
        case tree: If             => genIf(tree)
        case tree: JavaSeqLiteral => genJavaSeqLiteral(tree)
        case tree: Literal        => genLiteral(tree)
        case tree: Match          => genMatch(tree)
        case tree: Return         => genReturn(tree)
        case tree: Select         => genSelect(tree)
        case tree: This           => genThis(tree)
        case tree: Try            => genTry(tree)
        case tree: Typed          => genTyped(tree)
        case tree: TypeApply      => genTypeApply(tree)
        case tree: ValDef         => genValDef(tree)
        case tree: WhileDo        => genWhileDo(tree)
        case _ =>
          throw FatalError(
            s"""Unexpected tree in genExpr: `${tree}`
             raw=$tree
             pos=${tree.span}
             """
          )
      }
    }

    object SafeZoneInstance extends Property.Key[nir.Val]

    def genApply(app: Apply): nir.Val = {
      given nir.SourcePosition = app.span.orElse(fallbackSourcePosition)
      val Apply(fun, args) = app

      val sym = fun.symbol
      def isStatic = sym.owner.isStaticOwner
      def qualifier0 = qualifierOf(fun)
      def qualifier = qualifier0.withSpan(qualifier0.span.orElse(fun.span))
      def arg = args.head

      inline def fail(msg: String)(using Context) = {
        report.error(msg, app.srcPos)
        nir.Val.Null
      }
      fun match {
        case _ if sym == defnNir.UnsafePackage_extern =>
          fail(s"extern can be used only from non-inlined extern methods")

        case _: TypeApply => genApplyTypeApply(app)
        case Select(Super(_, _), _) =>
          genApplyMethod(
            sym,
            statically = true,
            curMethodThis.get.get,
            args
          )
        case Select(New(_), nme.CONSTRUCTOR) =>
          genApplyNew(app)
        case _ if sym == defn.newArrayMethod =>
          val Seq(
            Literal(componentType: Constant),
            arrayType,
            SeqLiteral(dimensions, _)
          ) = args: @unchecked
          if (dimensions.size == 1)
            val length = genExpr(dimensions.head)
            buf.arrayalloc(
              genType(componentType.typeValue),
              length,
              unwind,
              zone = app.getAttachment(SafeZoneInstance)
            )
          else genApplyMethod(sym, statically = isStatic, qualifier, args)
        case _ =>
          if (nirPrimitives.isPrimitive(fun)) genApplyPrimitive(app)
          else if (Erasure.Boxing.isBox(sym)) genApplyBox(arg.tpe, arg)
          else if (Erasure.Boxing.isUnbox(sym)) genApplyUnbox(app.tpe, arg)
          else genApplyMethod(sym, statically = false, qualifier, args)
      }
    }

    def genAssign(tree: Assign): nir.Val = {
      val Assign(lhsp, rhsp) = tree
      given nir.SourcePosition = tree.span

      desugarTree(lhsp) match {
        case sel @ Select(qualp, _) =>
          def rhs = genExpr(rhsp)
          val sym = sel.symbol
          val name = genFieldName(sym)
          if (sym.isExtern) {
            // Ignore intrinsic call to extern in class constructor
            // It would always present and would lead to undefined behaviour otherwise
            val shouldIgnoreAssign =
              curMethodSym.get.isClassConstructor &&
                rhsp.symbol == defnNir.UnsafePackage_extern
            if (shouldIgnoreAssign) nir.Val.Unit
            else {
              val externTy = genExternType(sel.tpe)
              genStoreExtern(externTy, sym, rhs)
            }
          } else {
            val qual =
              if (sym.isStaticMember) genModule(qualp.symbol)
              else genExpr(qualp)
            val ty = genType(sel.tpe)
            buf.fieldstore(ty, qual, name, rhs, unwind)
          }

        case id: Ident =>
          val rhs = genExpr(rhsp)
          val slot = curMethodEnv.resolve(id.symbol)
          buf.varstore(slot, rhs, unwind)
      }
    }

    def genBlock(block: Block): nir.Val = {
      val Block(stats, last) = block

      def isCaseLabelDef(tree: Tree) =
        tree.isInstanceOf[Labeled] && tree.symbol.isAllOf(SyntheticCase)

      def translateMatch(last: Labeled) = {
        val (prologue, cases) = stats.span(!isCaseLabelDef(_))
        val labels = cases.asInstanceOf[List[Labeled]]
        genMatch(prologue, labels :+ last)
      }

      withFreshBlockScope(block.span) { parentScope =>
        last match {
          case label: Labeled if isCaseLabelDef(label) =>
            translateMatch(label)

          case Apply(
                TypeApply(Select(label: Labeled, nme.asInstanceOf_), _),
                _
              ) if isCaseLabelDef(label) =>
            translateMatch(label)

          case _ =>
            stats.foreach(genExpr)
            genExpr(last)
        }
      }

    }

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
    def genClosure(tree: Closure): nir.Val = {
      given nir.SourcePosition = tree.span
      val Closure(env, fun, functionalInterface) = tree
      val treeTpe = tree.tpe.typeSymbol
      val funSym = fun.symbol
      val funInterfaceSym = functionalInterface.tpe.typeSymbol

      val anonClassName = {
        val nir.Global.Top(className) = genTypeName(curClassSym)
        val suffix = "$$Lambda$" + curClassFresh.get.apply().id
        nir.Global.Top(className + suffix)
      }

      val isStaticCall = funSym.isStaticInNIR
      val allCaptureValues =
        if (isStaticCall) env
        else qualifierOf(fun) :: env
      val captureSyms = allCaptureValues.map(_.symbol)
      val captureTypesAndNames = {
        for
          (tree, idx) <- allCaptureValues.zipWithIndex
          tpe = tree match {
            case This(iden) => genType(fun.symbol.owner)
            case _          => genType(tree.tpe)
          }
          name = anonClassName.member(nir.Sig.Field(s"capture$idx"))
        yield (tpe, name)
      }
      val (captureTypes, captureNames) = captureTypesAndNames.unzip

      def genAnonymousClass: nir.Defn = {
        val traits =
          if (functionalInterface.isEmpty) genTypeName(treeTpe) :: Nil
          else {
            val parents = funInterfaceSym.info.parents
            genTypeName(funInterfaceSym) +: parents.collect {
              case tpe if tpe.typeSymbol.isTraitOrInterface =>
                genTypeName(tpe.typeSymbol)
            }
          }

        nir.Defn.Class(
          attrs = nir.Attrs.None,
          name = anonClassName,
          parent = Some(nir.Rt.Object.name),
          traits = traits
        )
      }

      def genCaptureFields: List[nir.Defn] = {
        for (tpe, name) <- captureTypesAndNames
        yield nir.Defn.Var(
          attrs = nir.Attrs.None,
          name = name,
          ty = tpe,
          rhs = nir.Val.Zero(tpe)
        )
      }

      val ctorName = anonClassName.member(nir.Sig.Ctor(captureTypes))
      val ctorTy = nir.Type.Function(
        nir.Type.Ref(anonClassName) +: captureTypes,
        nir.Type.Unit
      )

      def genAnonymousClassCtor: nir.Defn = {
        val body = {
          scoped(
            curScopeId := nir.ScopeId.TopLevel
          ) {
            val fresh = nir.Fresh()
            val buf = new nir.InstructionBuilder()(fresh)

            val superTy = nir.Type.Function(Seq(nir.Rt.Object), nir.Type.Unit)
            val superName = nir.Rt.Object.name.member(nir.Sig.Ctor(Seq.empty))
            val superCtor = nir.Val.Global(superName, nir.Type.Ptr)

            val self = nir.Val.Local(fresh(), nir.Type.Ref(anonClassName))
            val captureFormals = captureTypes.map(nir.Val.Local(fresh(), _))
            buf.label(fresh(), self +: captureFormals)
            buf.call(superTy, superCtor, Seq(self), nir.Next.None)
            captureNames.zip(captureFormals).foreach { (name, capture) =>
              buf.fieldstore(capture.ty, self, name, capture, nir.Next.None)
            }
            buf.ret(nir.Val.Unit)

            buf.toSeq
          }
        }

        new nir.Defn.Define(nir.Attrs.None, ctorName, ctorTy, body)
      }

      def resolveAnonClassMethods: List[Symbol] = {
        val samMethods =
          if (funInterfaceSym.exists) funInterfaceSym.info.possibleSamMethods
          else treeTpe.info.possibleSamMethods

        samMethods
          .map(_.symbol)
          .toList
      }

      def genAnonClassMethod(sym: Symbol): nir.Defn = {
        val nir.Global.Member(_, funSig) = genName(sym): @unchecked
        val nir.Sig.Method(_, sigTypes :+ retType, _) =
          funSig.unmangled: @unchecked

        val selfType = nir.Type.Ref(anonClassName)
        val methodName = anonClassName.member(funSig)
        val paramTypes = selfType +: sigTypes
        val paramSyms = funSym.paramSymss.flatten

        def genBody = {
          given fresh: nir.Fresh = nir.Fresh()
          val freshScopes = initFreshScope(EmptyTree)
          given buf: ExprBuffer = new ExprBuffer()
          scoped(
            curFresh := fresh,
            curFreshScope := freshScopes,
            curScopeId := nir.ScopeId.of(freshScopes.last),
            curExprBuffer := buf,
            curMethodEnv := MethodEnv(fresh),
            curMethodLabels := MethodLabelsEnv(fresh),
            curMethodInfo := CollectMethodInfo(),
            curUnwindHandler := None
          ) {
            val self = nir.Val.Local(fresh(), selfType)
            val params = sigTypes.map(nir.Val.Local(fresh(), _))

            buf.label(fresh(), self +: params)
            // At this point, the type parameter symbols are all Objects.
            // We need to transform them, so that their type conforms to
            // what the apply method expects:
            // - values that can be unboxed, are unboxed
            // - otherwise, the value is cast to the appropriate type
            val paramVals =
              for (param, sym) <- params.zip(paramSyms)
              yield ensureUnboxed(param, sym.info.finalResultType)

            val captureVals =
              for (sym, (tpe, name)) <- captureSyms.zip(captureTypesAndNames)
              yield buf.fieldload(tpe, self, name, unwind)

            val allVals =
              (captureVals ++ paramVals).toList.map(ValTree(_)(sym.span))
            val res = if (isStaticCall) {
              scoped(curMethodThis := None) {
                buf.genApplyStaticMethod(
                  funSym,
                  qualifierOf(fun).symbol,
                  allVals
                )
              }
            } else {
              val thisVal :: argVals = allVals: @unchecked
              scoped(curMethodThis := Some(thisVal.value)) {
                buf.genApplyMethod(
                  funSym,
                  statically = false,
                  thisVal,
                  argVals
                )
              }
            }

            val retValue =
              if (retType == res.ty) res
              else
                // Get the result type of the lambda after erasure, when entering posterasure.
                // This allows to recover the correct type in case value classes are involved.
                // In that case, the type will be an ErasedValueType.
                buf.ensureBoxed(res, funSym.info.finalResultType)
            buf.ret(retValue)
            buf.toSeq
          }
        }

        new nir.Defn.Define(
          nir.Attrs.None,
          methodName,
          nir.Type.Function(paramTypes, retType),
          genBody
        )
      }

      def genAnonymousClassMethods: List[nir.Defn] =
        resolveAnonClassMethods
          .map(genAnonClassMethod)

      generatedDefns ++= {
        genAnonymousClass ::
          genAnonymousClassCtor ::
          genCaptureFields :::
          genAnonymousClassMethods
      }

      def allocateClosure() = {
        val alloc = buf.classalloc(anonClassName, unwind)
        val captures = allCaptureValues.map(genExpr)
        buf.call(
          ctorTy,
          nir.Val.Global(ctorName, nir.Type.Ptr),
          alloc +: captures,
          unwind
        )
        alloc
      }
      allocateClosure()
    }

    def genIdent(tree: Ident): nir.Val =
      desugarIdent(tree) match {
        case Ident(_) =>
          val sym = tree.symbol
          given nir.SourcePosition = tree.span
          if (curMethodInfo.mutableVars.contains(sym))
            buf.varload(curMethodEnv.resolve(sym), unwind)
          else if (sym.is(Module))
            genModule(sym)
          else curMethodEnv.resolve(sym)
        case desuagred: Select =>
          genSelect(desuagred.withSpan(tree.span))
        case tree =>
          throw FatalError(s"Unsupported desugared ident tree: $tree")
      }

    def genIf(tree: If): nir.Val = {
      given nir.SourcePosition = tree.span
      val If(cond, thenp, elsep) = tree
      def isUnitType(tpe: Type) =
        tpe =:= defn.UnitType || defn.isBoxedUnitClass(tpe.sym)
      val retty =
        if (isUnitType(thenp.tpe) || isUnitType(elsep.tpe)) nir.Type.Unit
        else genType(tree.tpe)
      genIf(retty, cond, thenp, elsep)
    }

    def genIf(
        retty: nir.Type,
        condp: Tree,
        thenp: Tree,
        elsep: Tree,
        ensureLinktime: Boolean = false
    )(using enclosingPos: nir.SourcePosition): nir.Val = {
      val thenn, elsen, mergen = fresh()
      val mergev = nir.Val.Local(fresh(), retty)

      locally {
        given nir.SourcePosition = condp.span.orElse(enclosingPos)
        getLinktimeCondition(condp) match {
          case Some(cond) =>
            curMethodEnv.get.isUsingLinktimeResolvedValue = true
            buf.branchLinktime(cond, nir.Next(thenn), nir.Next(elsen))
          case None =>
            if ensureLinktime then
              report.error(
                "Cannot resolve given condition in linktime, it might be depending on runtime value",
                condp.srcPos
              )
            val cond = genExpr(condp)
            buf.branch(cond, nir.Next(thenn), nir.Next(elsen))
        }
      }

      locally {
        given nir.SourcePosition = thenp.span.orElse(enclosingPos)
        buf.label(thenn)
        val thenv = genExpr(thenp)
        buf.jumpExcludeUnitValue(retty)(mergen, thenv)
      }
      locally {
        given nir.SourcePosition = elsep.span.orElse(enclosingPos)
        buf.label(elsen)
        val elsev = genExpr(elsep)
        buf.jumpExcludeUnitValue(retty)(mergen, elsev)
      }
      buf.labelExcludeUnitValue(mergen, mergev)
    }

    def genJavaSeqLiteral(tree: JavaSeqLiteral): nir.Val = {
      val JavaArrayType(elemTpe) = tree.tpe: @unchecked

      val elems = tree.elems
      val elemty = genType(elemTpe)
      val values = genSimpleArgs(elems)
      given nir.SourcePosition = tree.span

      if (values.forall(_.isCanonical) && values.exists(v => !v.isZero))
        buf.arrayalloc(elemty, nir.Val.ArrayValue(elemty, values), unwind)
      else
        val alloc = buf.arrayalloc(elemty, nir.Val.Int(values.length), unwind)
        for (v, i) <- values.zipWithIndex if !v.isZero do
          given nir.SourcePosition = elems(i).span
          buf.arraystore(elemty, alloc, nir.Val.Int(i), v, unwind)
        alloc
    }

    def genLabelDef(label: Labeled): nir.Val = {
      given nir.SourcePosition = label.span
      val Labeled(bind, body) = label
      assert(bind.body == EmptyTree, "non-empty Labeled bind body")

      val (labelEntry, labelExit) = curMethodLabels.enterLabel(label)
      val labelExitParam = nir.Val.Local(fresh(), genType(bind.tpe))
      curMethodLabels.enterExitType(labelExit, labelExitParam.ty)

      buf.jump(nir.Next(labelEntry))

      buf.label(labelEntry, Nil)
      buf.jumpExcludeUnitValue(labelExitParam.ty)(
        labelExit,
        genExpr(label.expr)
      )

      buf.labelExcludeUnitValue(labelExit, labelExitParam)
    }

    def genLiteral(lit: Literal): nir.Val = {
      val value = lit.const

      value.tag match {
        case ClazzTag => genTypeValue(value.typeValue)
        case _        => genLiteralValue(lit)
      }
    }

    private def genLiteralValue(lit: Literal): nir.Val = {
      val value = lit.const
      value.tag match {
        case UnitTag => nir.Val.Unit
        case NullTag => nir.Val.Null
        case BooleanTag =>
          if (value.booleanValue) nir.Val.True else nir.Val.False
        case ByteTag   => nir.Val.Byte(value.intValue.toByte)
        case ShortTag  => nir.Val.Short(value.intValue.toShort)
        case CharTag   => nir.Val.Char(value.intValue.toChar)
        case IntTag    => nir.Val.Int(value.intValue)
        case LongTag   => nir.Val.Long(value.longValue)
        case FloatTag  => nir.Val.Float(value.floatValue)
        case DoubleTag => nir.Val.Double(value.doubleValue)
        case StringTag => nir.Val.String(value.stringValue)
      }
    }

    def genMatch(m: Match): nir.Val = {
      given nir.SourcePosition = m.span
      val Match(scrutp, allcaseps) = m
      case class Case(
          id: nir.Local,
          value: nir.Val,
          tree: Tree,
          position: nir.SourcePosition
      )

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
          vals.map(Case(fresh(), _, body, cd.span: nir.SourcePosition))
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
        val casenexts = caseps.map {
          case Case(n, v, _, _) => nir.Next.Case(v, n)
        }
        val defaultnext = nir.Next(fresh())
        val merge = fresh()
        val mergev = nir.Val.Local(fresh(), retty)

        val defaultCasePos: nir.SourcePosition = defaultp.span

        // Generate code for the switch and its cases.
        val scrut = genExpr(scrutp)
        buf.switch(scrut, defaultnext, casenexts)
        buf.label(defaultnext.id)(using defaultCasePos)
        buf.jumpExcludeUnitValue(retty)(merge, genExpr(defaultp))(using
          defaultCasePos
        )
        caseps.foreach {
          case Case(n, _, expr, pos) =>
            given nir.SourcePosition = pos
            buf.label(n)
            val caseres = genExpr(expr)
            buf.jumpExcludeUnitValue(retty)(merge, caseres)
        }
        buf.labelExcludeUnitValue(merge, mergev)
      }

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
         * We need to make sure to only generate LabelDef at this stage.
         * Generating other ASTs and mutating state might lead to unexpected
         * runtime errors.
         */
        val optDefaultLabel = defaultp match {
          case label: Labeled => Some(genLabelDef(label))
          case _              => None
        }

        def loop(cases: List[Case]): nir.Val = {
          cases match {
            case Case(_, caze, body, p) :: elsep =>
              given nir.SourcePosition = p

              val cond =
                buf.genClassEquality(
                  leftp = ValTree(scrutp)(scrut),
                  rightp = ValTree(body)(caze),
                  ref = false,
                  negated = false
                )
              buf.genIf(
                retty = retty,
                condp = ValTree(body)(cond),
                thenp = ContTree(body)(_.genExpr(body)),
                elsep = ContTree(body)(_ => loop(elsep))
              )

            case Nil => optDefaultLabel.getOrElse(genExpr(defaultp))
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

    private def genMatch(prologue: List[Tree], lds: List[Labeled]): nir.Val = {
      // Generate prologue expressions.
      prologue.foreach(genExpr(_))

      // Enter symbols for all labels and jump to the first one.
      lds.foreach(curMethodLabels.enterLabel)
      val firstLd = lds.head
      given nir.SourcePosition = firstLd.span
      buf.jump(nir.Next(curMethodLabels.resolveEntry(firstLd)))

      // Generate code for all labels and return value of the last one.
      lds.map(genLabelDef(_)).last
    }

    def genModule(sym: Symbol)(using nir.SourcePosition): nir.Val = {
      val moduleSym = if (sym.isTerm) sym.moduleClass match {
        case NoSymbol  => sym.info.typeSymbol
        case moduleCls => moduleCls
      }
      else sym
      val name = genModuleName(moduleSym)
      buf.module(name, unwind)
    }

    def genReturn(tree: Return): nir.Val = {
      val Return(exprp, from) = tree
      given nir.SourcePosition = tree.span
      val rhs = genExpr(exprp)
      val fromSym = from.symbol
      val label = Option.when(fromSym.is(Label)) {
        curMethodLabels.resolveExit(fromSym)
      }
      genReturn(rhs, label)
    }

    def genReturn(value: nir.Val, from: Option[nir.Local] = None)(using
        pos: nir.SourcePosition
    ): nir.Val = {
      val retv =
        if (curMethodIsExtern.get)
          val nir.Type.Function(_, retty) = genExternMethodSig(curMethodSym)
          toExtern(retty, value)
        else value

      from match {
        case Some(label) =>
          val retty = curMethodLabels.resolveExitType(label)
          buf.jumpExcludeUnitValue(retty)(label, retv)
        case _ if retv.ty == nir.Type.Unit => buf.ret(nir.Val.Unit)
        case _                             => buf.ret(retv)
      }
      nir.Val.Unit
    }

    def genSelect(tree: Select): nir.Val = {
      given nir.SourcePosition = tree.span
      val Select(qualp, selp) = tree

      val sym = tree.symbol
      val owner = if sym != NoSymbol then sym.owner else NoSymbol

      if (sym.is(Module)) genModule(sym)
      else if (sym.isStaticInNIR && !sym.isExtern)
        genStaticMember(sym, qualp.symbol)
      else if (sym.is(Method))
        genApplyMethod(sym, statically = false, qualp, Seq.empty)
      else if (owner.isStruct) {
        val index = owner.info.decls.filter(_.isField).toList.indexOf(sym)
        val qual = genExpr(qualp)
        buf.extract(qual, Seq(index), unwind)
      } else {
        val ty = genType(tree.tpe)
        val qual =
          if (sym.isStaticMember) genModule(owner)
          else genExpr(qualp)
        val name = genFieldName(tree.symbol)
        if (sym.isExtern) {
          val externTy = genExternType(tree.tpe)
          genLoadExtern(ty, externTy, tree.symbol)
        } else {
          buf.fieldload(ty, qual, name, unwind)
        }
      }
    }

    def genThis(tree: This): nir.Val = {
      given nir.SourcePosition = tree.span
      val sym = tree.symbol
      def currentThis = curMethodThis.get
      def currentClass = curClassSym.get
      def currentMethod = curMethodSym.get

      def canUseCurrentThis = currentThis.nonEmpty &&
        (sym == currentClass || currentMethod.owner == currentClass)
      val canLoadAsModule =
        sym != currentClass &&
          sym.is(ModuleClass, butNot = Package) ||
          sym.isPackageObject

      if (canLoadAsModule) genModule(sym)
      else if (canUseCurrentThis) currentThis.get
      else
        report.error(
          s"Cannot resolve `this` instance for ${tree}",
          tree.sourcePos
        )
        nir.Val.Zero(genType(currentClass))
    }

    def genTry(tree: Try): nir.Val = tree match {
      case Try(expr, catches, finalizer)
          if catches.isEmpty && finalizer.isEmpty =>
        genExpr(expr)
      case Try(expr, catches, finalizer) =>
        val retty = genType(tree.tpe)
        genTry(retty, expr, catches, finalizer)
    }

    private def genTry(
        retty: nir.Type,
        expr: Tree,
        catches: List[Tree],
        finallyp: Tree
    ): nir.Val = {
      given nir.SourcePosition = expr.span
      val handler, normaln, mergen = fresh()
      val excv = nir.Val.Local(fresh(), nir.Rt.Object)
      val mergev = nir.Val.Local(fresh(), retty)

      // Nested code gen to separate out try/catch-related instructions.
      val nested = ExprBuffer()
      scoped(
        curUnwindHandler := Some(handler)
      ) {
        withFreshBlockScope(summon[nir.SourcePosition]) { _ =>
          nested.label(normaln)
          val res = nested.genExpr(expr)
          nested.jumpExcludeUnitValue(retty)(mergen, res)
        }
      }
      withFreshBlockScope(summon[nir.SourcePosition]) { _ =>
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

    private def genTryCatch(
        retty: nir.Type,
        exc: nir.Val,
        mergen: nir.Local,
        catches: List[Tree]
    )(using exprPos: nir.SourcePosition): nir.Val = {
      val cases = catches.map {
        case cd @ CaseDef(pat, _, body) =>
          val (excty, symopt) = pat match {
            case Typed(Ident(nme.WILDCARD), tpt) =>
              genType(tpt.tpe) -> None
            case Ident(nme.WILDCARD) =>
              genType(defn.ThrowableClass.info.resultType) -> None
            case Bind(_, _) =>
              genType(pat.tpe) -> Some(pat.symbol)
          }
          val f = ContTree(body) { (buf: ExprBuffer) =>
            withFreshBlockScope(body.span) { _ =>
              symopt.foreach { sym =>
                val cast = buf.as(excty, exc, unwind)(cd.span, getScopeId)
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
            )(using pos)
        }

      wrap(cases)
    }

    private def genTryFinally(
        finallyp: Tree,
        insts: Seq[nir.Inst]
    ): Seq[nir.Inst] = {
      val labels =
        insts.collect {
          case nir.Inst.Label(n, _) => n
        }.toSet
      def internal(cf: nir.Inst.Cf) = cf match {
        case inst @ nir.Inst.Jump(n) =>
          labels.contains(n.id)
        case inst @ nir.Inst.If(_, n1, n2) =>
          labels.contains(n1.id) && labels.contains(n2.id)
        case inst @ nir.Inst.LinktimeIf(_, n, n2) =>
          labels.contains(n.id) && labels.contains(n2.id)
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
            finalies.genExpr(finallyp)
          }
          finalies += cf
          // The original jump outside goes through finally block first.
          nir.Inst.Jump(nir.Next(finallyn))(cf.pos)
        case inst =>
          inst
      }
      transformed ++ finalies.toSeq
    }

    def genTyped(tree: Typed): nir.Val = tree match {
      case Typed(Super(_, _), _) => curMethodThis.get.get
      case Typed(expr, _)        => genExpr(expr)
    }

    def genTypeApply(tree: TypeApply): nir.Val = {
      given nir.SourcePosition = tree.span
      val TypeApply(fun @ Select(receiverp, _), targs) = tree: @unchecked

      val funSym = fun.symbol
      val fromty = genType(receiverp.tpe)
      val toty = genType(targs.head.tpe)
      def boxty = genBoxType(targs.head.tpe)
      val value = genExpr(receiverp)
      lazy val boxed = boxValue(receiverp.tpe, value)(using receiverp.span)

      if (funSym == defn.Any_isInstanceOf) buf.is(boxty, boxed, unwind)
      else if (funSym == defn.Any_asInstanceOf)
        (fromty, toty) match {
          case (_: nir.Type.PrimitiveKind, _: nir.Type.PrimitiveKind) =>
            genCoercion(value, fromty, toty)
          case _ if boxed.ty =?= boxty => boxed
          case (_, nir.Type.Nothing) =>
            val isNullL, notNullL = fresh()
            val isNull =
              buf.comp(nir.Comp.Ieq, boxed.ty, boxed, nir.Val.Null, unwind)
            buf.branch(isNull, nir.Next(isNullL), nir.Next(notNullL))
            buf.label(isNullL)
            buf.raise(nir.Val.Null, unwind)
            buf.label(notNullL)
            buf.as(nir.Rt.RuntimeNothing, boxed, unwind)
            buf.unreachable(unwind)
            buf.label(fresh())
            nir.Val.Zero(nir.Type.Nothing)
          case _ =>
            val cast = buf.as(boxty, boxed, unwind)
            unboxValue(tree.tpe, partial = true, cast)
        }
      else {
        report.error("Unkown case genTypeApply: " + funSym, tree.sourcePos)
        nir.Val.Null
      }
    }

    def genValDef(vd: ValDef): nir.Val = {
      given nir.SourcePosition = vd.span
      val localNames = curMethodLocalNames.get
      val isMutable = curMethodInfo.mutableVars.contains(vd.symbol)
      def name = genLocalName(vd.symbol)
      val rhs = genExpr(vd.rhs) match {
        case v @ nir.Val.Local(id, _) =>
          if !(localNames.contains(id) || isMutable)
          then localNames.update(id, name)
          vd.rhs match {
            // When rhs is a block patch the scopeId of it's result to match the current scopeId
            // This allows us to reflect that ValDef is accessible in this scope
            case _: Block | Typed(_: Block, _) | Try(_: Block, _, _) |
                Try(Typed(_: Block, _), _, _) =>
              buf.updateLetInst(id)(i => i.copy()(i.pos, curScopeId.get))
            case _ => ()
          }
          v
        case nir.Val.Unit =>
          nir.Val.Unit
        case v =>
          buf.let(fresh.namedId(name), nir.Op.Copy(v), unwind)
      }

      if (vd.symbol.isExtern)
        checkExplicitReturnTypeAnnotation(vd, "extern field")
      if (isMutable)
        val slot = curMethodEnv.resolve(vd.symbol)
        buf.varstore(slot, rhs, unwind)
      else
        curMethodEnv.enter(vd.symbol, rhs)
        nir.Val.Unit
    }

    def genWhileDo(wd: WhileDo): nir.Val = {
      val WhileDo(cond, body) = wd
      val condLabel, bodyLabel, exitLabel = fresh()

      locally {
        given nir.SourcePosition = wd.span
        buf.jump(nir.Next(condLabel))
      }

      locally {
        given nir.SourcePosition = cond.span.orElse(wd.span)
        buf.label(condLabel)
        val genCond =
          if (cond == EmptyTree) nir.Val.Bool(true)
          else genExpr(cond)
        buf.branch(genCond, nir.Next(bodyLabel), nir.Next(exitLabel))
      }

      locally {
        given nir.SourcePosition = body.span
        buf.label(bodyLabel)
        val _ = genExpr(body)
        buf.jump(condLabel, Nil)
      }

      locally {
        given nir.SourcePosition = wd.span.endPos
        buf.label(exitLabel, Seq.empty)
        if (cond == EmptyTree) nir.Val.Zero(genType(defn.NothingClass))
        else nir.Val.Unit
      }
    }

    private def genApplyBox(st: SimpleType, argp: Tree)(using
        nir.SourcePosition
    ): nir.Val = {
      val value = genExpr(argp)
      buf.box(genBoxType(st), value, unwind)
    }

    private def genApplyUnbox(st: SimpleType, argp: Tree)(using
        nir.SourcePosition
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

    private def genApplyPrimitive(app: Apply): nir.Val = {
      import NirPrimitives._
      import dotty.tools.backend.ScalaPrimitivesOps._
      given nir.SourcePosition = app.span
      val Apply(fun, args) = app
      val Select(receiver, _) = desugarTree(fun): @unchecked

      val sym = app.symbol
      val code = nirPrimitives.getPrimitive(app, receiver.tpe)
      def arg = args.head

      (code: @switch) match {
        case THROW                  => genThrow(app, args)
        case CONCAT                 => genStringConcat(app)
        case HASH                   => genHashCode(arg)
        case BOXED_UNIT             => nir.Val.Unit
        case SYNCHRONIZED           => genSynchronized(receiver, arg)
        case CFUNCPTR_APPLY         => genCFuncPtrApply(app)
        case CFUNCPTR_FROM_FUNCTION => genCFuncFromScalaFunction(app)
        case STACKALLOC             => genStackalloc(app)
        case SAFEZONE_ALLOC         => genSafeZoneAlloc(app)
        case CQUOTE                 => genCQuoteOp(app)
        case CLASS_FIELD_RAWPTR     => genClassFieldRawPtr(app)
        case SIZE_OF                => genSizeOf(app)
        case ALIGNMENT_OF           => genAlignmentOf(app)
        case REFLECT_SELECTABLE_SELECTDYN =>
          genReflectiveCall(app, isSelectDynamic = true)
        case REFLECT_SELECTABLE_APPLYDYN =>
          genReflectiveCall(app, isSelectDynamic = false)
        case USES_LINKTIME_INTRINSIC => genLinktimeIntrinsicApply(app)
        case _ =>
          if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code))
            genSimpleOp(app, receiver :: args, code)
          else if (isArrayOp(code) || code == ARRAY_CLONE) genArrayOp(app, code)
          else if (isCoercion(code)) genCoercion(app, receiver, code)
          else if (NirPrimitives.isRawPtrOp(code)) genRawPtrOp(app, code)
          else if (NirPrimitives.isRawPtrCastOp(code)) genRawPtrCastOp(app)
          else if (NirPrimitives.isRawSizeCastOp(code))
            genRawSizeCastOp(app, code)
          else if (NirPrimitives.isUnsignedOp(code)) genUnsignedOp(app, code)
          else {
            report.error(
              s"Unknown primitive operation: ${sym.fullName}(${fun.symbol.showName})",
              app.sourcePos
            )
            nir.Val.Null
          }
      }
    }

    private def genLinktimeIntrinsicApply(app: Apply): nir.Val = {
      import defnNir.*
      given nir.SourcePosition = app.span
      val Apply(fun, args) = app

      val sym = fun.symbol
      def isStatic = sym.owner.isStaticOwner
      def qualifier0 = qualifierOf(fun)
      def qualifier = qualifier0.withSpan(qualifier0.span.orElse(fun.span))

      sym match {
        case _
            if JavaUtilServiceLoaderLoad.contains(sym) ||
              JavaUtilServiceLoaderLoadInstalled == sym =>
          args.head match {
            case Literal(c: Constant) => () // ok
            case _ =>
              report.error(
                s"Limitation of ScalaNative runtime: first argument of ${sym.show} needs to be literal constant of class type, use `classOf[T]` instead.",
                app.srcPos
              )
          }
        case _ =>
          report.error(
            s"Unhandled intrinsic function call for ${sym.show}",
            app.srcPos
          )
      }

      curMethodEnv.get.isUsingIntrinsics = true
      genApplyMethod(sym, statically = isStatic, qualifier, args)
    }

    private def genApplyTypeApply(app: Apply): nir.Val = {
      val Apply(tApply @ TypeApply(fun, targs), argsp) = app: @unchecked
      val Select(receiverp, _) = desugarTree(fun): @unchecked
      given nir.SourcePosition = app.span

      val funSym = fun.symbol
      genExpr(receiverp)
      if (funSym == defn.Object_synchronized)
        assert(argsp.size == 1, "synchronized with wrong number of args")
        genSynchronized(receiverp, argsp.head)
      else genTypeApply(tApply)
    }

    private def genApplyNew(app: Apply): nir.Val = {
      val Apply(fun @ Select(New(tpt), nme.CONSTRUCTOR), args) = app: @unchecked
      given nir.SourcePosition = app.span

      fromType(tpt.tpe) match {
        case st if st.sym.isStruct =>
          genApplyNewStruct(st, args)

        case SimpleType(cls, Seq()) =>
          val ctor = fun.symbol
          assert(
            ctor.isClassConstructor,
            "'new' call to non-constructor: " + ctor.name
          )

          genApplyNew(
            cls,
            ctor,
            args,
            zone = app.getAttachment(SafeZoneInstance)
          )

        case SimpleType(sym, targs) =>
          unsupported(s"unexpected new: $sym with targs $targs")
      }
    }

    private def genApplyNewStruct(st: SimpleType, argsp: Seq[Tree]): nir.Val = {
      val ty = genType(st)
      val args = genSimpleArgs(argsp)
      var res: nir.Val = nir.Val.Zero(ty)

      for ((arg, argp), idx) <- args.zip(argsp).zipWithIndex
      do
        given nir.SourcePosition = argp.span
        res = buf.insert(res, arg, Seq(idx), unwind)
      res
    }

    private def genApplyNew(
        clssym: Symbol,
        ctorsym: Symbol,
        args: List[Tree],
        zone: Option[nir.Val]
    )(using
        nir.SourcePosition
    ): nir.Val = {
      val alloc = buf.classalloc(genTypeName(clssym), unwind, zone)
      genApplyMethod(ctorsym, statically = true, alloc, args)
      alloc
    }

    def genApplyModuleMethod(
        module: Symbol,
        method: Symbol,
        args: Seq[Tree]
    )(using
        nir.SourcePosition
    ): nir.Val = {
      val self = genModule(module)
      genApplyMethod(method, statically = true, self, args)
    }

    def genApplyMethod(
        sym: Symbol,
        statically: Boolean,
        selfp: Tree,
        argsp: Seq[Tree]
    )(using nir.SourcePosition): nir.Val = {
      if (sym.isExtern && sym.is(Accessor)) genApplyExternAccessor(sym, argsp)
      else if (sym.isStaticInNIR && !sym.isExtern)
        genApplyStaticMethod(sym, selfp.symbol, argsp)
      else
        val self = genExpr(selfp)
        genApplyMethod(sym, statically, self, argsp)
    }

    private def genApplyMethod(
        sym: Symbol,
        statically: Boolean,
        self: nir.Val,
        argsp: Seq[Tree]
    )(using nir.SourcePosition): nir.Val = {
      assert(!sym.isStaticMethod, sym)
      val owner = sym.owner.asClass
      val name = genMethodName(sym)
      val isExtern = sym.isExtern

      val origSig = genMethodSig(sym)
      val sig =
        if isExtern then genExternMethodSig(sym)
        else origSig
      val args = genMethodArgs(sym, argsp)

      val isStaticCall = statically || owner.isStruct || isExtern
      val method =
        if (isStaticCall) nir.Val.Global(name, nir.Type.Ptr)
        else
          val nir.Global.Member(_, sig) = name: @unchecked
          buf.method(self, sig, unwind)
      val values =
        if isExtern then args
        else self +: args

      val res = buf.call(sig, method, values, unwind)

      if !isExtern then res
      else {
        val nir.Type.Function(_, retty) = origSig
        fromExtern(retty, res)
      }
    }

    def genApplyStaticMethod(
        sym: Symbol,
        receiver: Symbol,
        argsp: Seq[Tree]
    )(using nir.SourcePosition): nir.Val = {
      require(!sym.isExtern, sym)

      val sig = genMethodSig(sym, statically = true)
      val args = genMethodArgs(sym, argsp)
      val methodName = genStaticMemberName(sym, receiver)
      val method = nir.Val.Global(methodName, nir.Type.Ptr)
      buf.call(sig, method, args, unwind)
    }

    private def genApplyExternAccessor(sym: Symbol, argsp: Seq[Tree])(using
        nir.SourcePosition
    ): nir.Val = {
      argsp match {
        case Seq() =>
          val ty = genMethodSig(sym).ret
          val externTy = genExternMethodSig(sym).ret
          genLoadExtern(ty, externTy, sym)
        case Seq(valuep) =>
          val externTy = genExternType(valuep.tpe)
          genStoreExtern(externTy, sym, genExpr(valuep))
      }
    }

    // Utils
    private def boxValue(st: SimpleType, value: nir.Val)(using
        nir.SourcePosition
    ): nir.Val = {
      if (st.sym.isUnsignedType)
        genApplyModuleMethod(
          defnNir.RuntimeBoxesModule,
          defnNir.BoxUnsignedMethod(st.sym),
          Seq(ValTree(value)())
        )
      else if (genPrimCode(st) == 'O') value
      else genApplyBox(st, ValTree(value)())
    }

    private def unboxValue(st: SimpleType, partial: Boolean, value: nir.Val)(
        using nir.SourcePosition
    ): nir.Val = {
      if (st.sym.isUnsignedType) {
        // Results of asInstanceOfs are partially unboxed, meaning
        // that non-standard value types remain to be boxed.
        if (partial) value
        else
          genApplyModuleMethod(
            defnNir.RuntimeBoxesModule,
            defnNir.UnboxUnsignedMethod(st.sym),
            Seq(ValTree(value)())
          )
      } else if (genPrimCode(st) == 'O') value
      else genApplyUnbox(st, ValTree(value)())
    }

    private def genSimpleOp(
        app: Apply,
        args: List[Tree],
        code: Int
    ): nir.Val = {
      given nir.SourcePosition = app.span
      val retty = genType(app.tpe)

      args match {
        case List(right)       => genUnaryOp(code, right, retty)
        case List(left, right) => genBinaryOp(code, left, right, retty)
        case _ =>
          report.error(
            s"Too many arguments for primitive function: $app",
            app.sourcePos
          )
          nir.Val.Null
      }
    }

    private def negateBool(value: nir.Val)(using nir.SourcePosition): nir.Val =
      buf.bin(nir.Bin.Xor, nir.Type.Bool, nir.Val.True, value, unwind)

    private def genUnaryOp(code: Int, rightp: Tree, opty: nir.Type)(using
        nir.SourcePosition
    ): nir.Val = {
      val right = genExpr(rightp)
      val coerced = genCoercion(right, right.ty, opty)
      val tpe = coerced.ty

      def numOfType(num: Int, ty: nir.Type): nir.Val = ty match {
        case nir.Type.Byte                  => nir.Val.Byte(num.toByte)
        case nir.Type.Short | nir.Type.Char => nir.Val.Short(num.toShort)
        case nir.Type.Int                   => nir.Val.Int(num)
        case nir.Type.Long                  => nir.Val.Long(num.toLong)
        case nir.Type.Float                 => nir.Val.Float(num.toFloat)
        case nir.Type.Double                => nir.Val.Double(num.toDouble)
        case nir.Type.Size                  => nir.Val.Size(num.toLong)
        case _ => unsupported(s"num = $num, ty = ${ty.show}")
      }

      (opty, code) match {
        case (_: nir.Type.I | _: nir.Type.F, POS) => coerced
        case (_: nir.Type.I, NOT) =>
          buf.bin(nir.Bin.Xor, tpe, numOfType(-1, tpe), coerced, unwind)
        case (_: nir.Type.F, NEG) =>
          buf.bin(nir.Bin.Fmul, tpe, numOfType(-1, tpe), coerced, unwind)
        case (_: nir.Type.I, NEG) =>
          buf.bin(nir.Bin.Isub, tpe, numOfType(0, tpe), coerced, unwind)
        case (nir.Type.Bool, ZNOT) => negateBool(coerced)
        case _ =>
          report.error(s"Unknown unary operation code: $code", rightp.sourcePos)
          nir.Val.Null
      }
    }

    private def genBinaryOp(
        code: Int,
        left: Tree,
        right: Tree,
        retty: nir.Type
    )(using nir.SourcePosition): nir.Val = {
      val lty = genType(left.tpe)
      val rty = genType(right.tpe)
      val opty = {
        if (isShiftOp(code))
          if (lty == nir.Type.Long) nir.Type.Long
          else nir.Type.Int
        else
          binaryOperationType(lty, rty)
      }
      def genOp(op: (nir.Type, nir.Val, nir.Val) => nir.Op): nir.Val = {
        val leftcoerced = genCoercion(genExpr(left), lty, opty)(using left.span)
        val rightcoerced =
          genCoercion(genExpr(right), rty, opty)(using right.span)
        buf.let(op(opty, leftcoerced, rightcoerced), unwind)(using
          left.span,
          getScopeId
        )
      }

      val binres = opty match {
        case _: nir.Type.F =>
          code match {
            case ADD => genOp(nir.Op.Bin(nir.Bin.Fadd, _, _, _))
            case SUB => genOp(nir.Op.Bin(nir.Bin.Fsub, _, _, _))
            case MUL => genOp(nir.Op.Bin(nir.Bin.Fmul, _, _, _))
            case DIV => genOp(nir.Op.Bin(nir.Bin.Fdiv, _, _, _))
            case MOD => genOp(nir.Op.Bin(nir.Bin.Frem, _, _, _))

            case EQ => genOp(nir.Op.Comp(nir.Comp.Feq, _, _, _))
            case NE => genOp(nir.Op.Comp(nir.Comp.Fne, _, _, _))
            case LT => genOp(nir.Op.Comp(nir.Comp.Flt, _, _, _))
            case LE => genOp(nir.Op.Comp(nir.Comp.Fle, _, _, _))
            case GT => genOp(nir.Op.Comp(nir.Comp.Fgt, _, _, _))
            case GE => genOp(nir.Op.Comp(nir.Comp.Fge, _, _, _))

            case _ =>
              report.error(
                s"Unknown floating point type binary operation code: $code",
                right.sourcePos
              )
              nir.Val.Null
          }
        case nir.Type.Bool | _: nir.Type.I =>
          code match {
            case ADD => genOp(nir.Op.Bin(nir.Bin.Iadd, _, _, _))
            case SUB => genOp(nir.Op.Bin(nir.Bin.Isub, _, _, _))
            case MUL => genOp(nir.Op.Bin(nir.Bin.Imul, _, _, _))
            case DIV => genOp(nir.Op.Bin(nir.Bin.Sdiv, _, _, _))
            case MOD => genOp(nir.Op.Bin(nir.Bin.Srem, _, _, _))

            case OR  => genOp(nir.Op.Bin(nir.Bin.Or, _, _, _))
            case XOR => genOp(nir.Op.Bin(nir.Bin.Xor, _, _, _))
            case AND => genOp(nir.Op.Bin(nir.Bin.And, _, _, _))
            case LSL => genOp(nir.Op.Bin(nir.Bin.Shl, _, _, _))
            case LSR => genOp(nir.Op.Bin(nir.Bin.Lshr, _, _, _))
            case ASR => genOp(nir.Op.Bin(nir.Bin.Ashr, _, _, _))

            case EQ => genOp(nir.Op.Comp(nir.Comp.Ieq, _, _, _))
            case NE => genOp(nir.Op.Comp(nir.Comp.Ine, _, _, _))
            case LT => genOp(nir.Op.Comp(nir.Comp.Slt, _, _, _))
            case LE => genOp(nir.Op.Comp(nir.Comp.Sle, _, _, _))
            case GT => genOp(nir.Op.Comp(nir.Comp.Sgt, _, _, _))
            case GE => genOp(nir.Op.Comp(nir.Comp.Sge, _, _, _))

            case ZOR  => genIf(retty, left, Literal(Constant(true)), right)
            case ZAND => genIf(retty, left, right, Literal(Constant(false)))
            case _ =>
              report.error(
                s"Unknown integer type binary operation code: $code",
                right.sourcePos
              )
              nir.Val.Null
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
            case EQ => genEquals(ref = false, negated = false)
            case NE => genEquals(ref = false, negated = true)
            case ID => genEquals(ref = true, negated = false)
            case NI => genEquals(ref = true, negated = true)
            case _ =>
              report.error(
                s"Unknown reference type binary operation code: $code",
                right.sourcePos
              )
              nir.Val.Null
          }
        case nir.Type.Ptr =>
          code match {
            case EQ | ID => genOp(nir.Op.Comp(nir.Comp.Ieq, _, _, _))
            case NE | NI => genOp(nir.Op.Comp(nir.Comp.Ine, _, _, _))
          }
        case ty =>
          report.error(
            s"Unknown binary operation type: $ty",
            right.sourcePos
          )
          nir.Val.Null
      }

      genCoercion(binres, binres.ty, retty)(using right.span)
    }

    private def binaryOperationType(lty: nir.Type, rty: nir.Type) =
      (lty, rty) match {
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
          report.error(s"can't perform binary operation between $lty and $rty")
          nir.Type.Nothing
      }

    private def genClassEquality(
        leftp: Tree,
        rightp: Tree,
        ref: Boolean,
        negated: Boolean
    )(using nir.SourcePosition): nir.Val = {

      if (ref) {
        // referencial equality
        val left = genExpr(leftp)
        val right = genExpr(rightp)
        val comp = if (negated) nir.Comp.Ine else nir.Comp.Ieq
        buf.comp(comp, nir.Rt.Object, left, right, unwind)
      } else genClassUniversalEquality(leftp, rightp, negated)
    }

    private def genClassUniversalEquality(l: Tree, r: Tree, negated: Boolean)(
        using nir.SourcePosition
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
        def areSameFinals = l.tpe.typeSymbol.is(Final) &&
          r.tpe.typeSymbol.is(Final) &&
          (l.tpe =:= r.tpe)
        def isMaybeBoxed(sym: Symbol): Boolean = {
          (sym == defn.ObjectClass) ||
          (sym == defn.JavaSerializableClass) ||
          (sym == defn.ComparableClass) ||
          (sym.derivesFrom(defn.BoxedNumberClass)) ||
          (sym.derivesFrom(defn.BoxedCharClass)) ||
          (sym.derivesFrom(defn.BoxedBooleanClass))
        }
        usesOnlyScalaTrees && !areSameFinals &&
          isMaybeBoxed(l.tpe.typeSymbol) &&
          isMaybeBoxed(r.tpe.typeSymbol)
      }
      def isNull(t: Tree): Boolean = t match {
        case Literal(Constant(null)) => true
        case _                       => false
      }
      def isNonNullExpr(t: Tree): Boolean =
        t.isInstanceOf[Literal] || ((t.symbol ne null) && t.symbol.is(Module))

      def comparator = if (negated) nir.Comp.Ine else nir.Comp.Ieq
      def maybeNegate(v: nir.Val): nir.Val =
        if negated then negateBool(v) else v

      if (mustUseAnyComparator) maybeNegate {
        val equalsMethod: Symbol = {
          if (l.tpe <:< defn.BoxedNumberClass.info) {
            if (r.tpe <:< defn.BoxedNumberClass.info)
              defn.BoxesRunTimeModule.requiredMethod(nme.equalsNumNum)
            else if (r.tpe <:< defn.BoxedCharClass.info)
              defn.BoxesRunTimeModule.requiredMethod(nme.equalsNumChar)
            else defn.BoxesRunTimeModule.requiredMethod(nme.equalsNumObject)
          } else defn.BoxesRunTimeModule_externalEquals
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

    def genMethodArgs(sym: Symbol, argsp: Seq[Tree]): Seq[nir.Val] =
      if sym.isExtern
      then genExternMethodArgs(sym, argsp)
      else genSimpleArgs(argsp)

    private def genSimpleArgs(argsp: Seq[Tree]): Seq[nir.Val] =
      argsp.map(genExpr)

    private def genExternMethodArgs(
        sym: Symbol,
        argsp: Seq[Tree]
    ): Seq[nir.Val] = {
      val res = Seq.newBuilder[nir.Val]
      val nir.Type.Function(argTypes, _) = genExternMethodSig(sym)
      val paramTypes = sym.paramInfo.paramInfoss.flatten
      assert(
        argTypes.size == argsp.size && argTypes.size == paramTypes.size,
        "Different number of arguments passed to method signature and apply method"
      )

      def genArg(
          argp: Tree,
          paramTpe: Types.Type,
          isVarArg: Boolean = false
      ): nir.Val = {
        given nir.SourcePosition = argp.span
        given ExprBuffer = buf
        val externType = genExternType(paramTpe.finalResultType)
        val rawValue = genExpr(argp)
        val maybeUnboxed =
          if (isVarArg) ensureUnboxed(rawValue, paramTpe.finalResultType)
          else rawValue
        val value = (maybeUnboxed, nir.Type.box.get(externType)) match {
          case (value @ nir.Val.Null, Some(unboxedType)) =>
            externType match {
              case nir.Type.Ptr | _: nir.Type.RefKind =>
                value
              case _ =>
                report.warning(
                  s"Passing null as argument of type ${paramTpe.show} to the extern method is unsafe. " +
                    s"The argument would be unboxed to primitive value of type $externType.",
                  argp.srcPos
                )
                nir.Val.Zero(unboxedType)
            }
          case (value, _) => value
        }
        toExtern(externType, value)
      }

      for ((argp, sigType), paramTpe) <- argsp zip argTypes zip paramTypes
      do
        sigType match {
          case nir.Type.Vararg =>
            argp match {
              case Apply(_, List(seqLiteral: JavaSeqLiteral)) =>
                for tree <- seqLiteral.elems
                do
                  given nir.SourcePosition = tree.span
                  val tpe = tree
                    .getAttachment(NirDefinitions.NonErasedType)
                    .getOrElse(tree.tpe)
                  val arg = genArg(tree, tpe, isVarArg = true)
                  def isUnsigned = nir.Type.isUnsignedType(genType(tpe))
                  // Decimal varargs needs to be promoted to at least Int, and Float needs to be promoted to Double
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
              case _ =>
                report.error(
                  "Unable to extract vararg arguments, varargs to extern methods must be passed directly to the applied function",
                  argp.srcPos
                )
            }
          case _ => res += genArg(argp, paramTpe)
        }
      res.result()
    }

    private def genArrayOp(app: Apply, code: Int): nir.Val = {
      import NirPrimitives._
      import dotty.tools.backend.ScalaPrimitivesOps._

      val Apply(Select(arrayp, _), argsp) = app: @unchecked
      val nir.Type.Array(elemty, _) = genType(arrayp.tpe): @unchecked
      given nir.SourcePosition = app.span

      def elemcode = genArrayCode(arrayp.tpe)
      val array = genExpr(arrayp)

      if (code == ARRAY_CLONE)
        val method = defnNir.RuntimeArray_clone(elemcode)
        genApplyMethod(method, statically = true, array, argsp)
      else if (isArrayGet(code))
        val idx = genExpr(argsp(0))
        buf.arrayload(elemty, array, idx, unwind)
      else if (isArraySet(code))
        val idx = genExpr(argsp(0))
        val value = genExpr(argsp(1))
        buf.arraystore(elemty, array, idx, value, unwind)
      else buf.arraylength(array, unwind)
    }

    private def genHashCode(argp: Tree)(using nir.SourcePosition): nir.Val =
      genApplyStaticMethod(
        defn.staticsMethod(nme.anyHash),
        defn.ScalaStaticsModule,
        Seq(argp)
      )

    /*
     * Returns a list of trees that each should be concatenated, from left to right.
     * It turns a chained call like "a".+("b").+("c") into a list of arguments.
     */
    def liftStringConcat(tree: Tree): List[Tree] = tree match {
      case tree @ Apply(fun @ DesugaredSelect(larg, method), rarg) =>
        if (nirPrimitives.isPrimitive(fun) &&
            nirPrimitives.getPrimitive(tree, larg.tpe) == CONCAT)
          liftStringConcat(larg) ::: rarg
        else
          tree :: Nil
      case _ =>
        tree :: Nil
    }

    /* Issue a call to `StringBuilder#append` for the right element type     */
    private final def genStringBuilderAppend(
        stringBuilder: nir.Val.Local,
        tree: Tree
    ): Unit = {
      given nir.SourcePosition = tree.span
      val tpe = tree.tpe
      val argType =
        if (tpe <:< defn.StringType) nir.Rt.String
        else if (tpe <:< defnNir.jlStringBufferType)
          genType(defnNir.jlStringBufferRef)
        else if (tpe <:< defnNir.jlCharSequenceType)
          genType(defnNir.jlCharSequenceRef)
        // Don't match for `Array(Char)`, even though StringBuilder has such an overload:
        // `"a" + Array('b')` should NOT be "ab", but "a[C@...".
        else if (tpe <:< defn.ObjectType) nir.Rt.Object
        else genType(tpe)

      val value = genExpr(tree)
      val (adaptedValue, targetType) = argType match {
        // jlStringBuilder does not have overloads for byte and short, but we can just use the int version
        case nir.Type.Byte | nir.Type.Short =>
          genCoercion(value, value.ty, nir.Type.Int) -> nir.Type.Int
        case nirType => value -> nirType
      }
      val (appendFunction, appendSig) = jlStringBuilderAppendForSymbol(
        targetType
      )
      buf.call(
        appendSig,
        appendFunction,
        Seq(stringBuilder, adaptedValue),
        unwind
      )
    }

    private lazy val jlStringBuilderRef =
      nir.Type.Ref(genTypeName(defnNir.jlStringBuilderRef))
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
      given nir.SourcePosition = tree.span
      liftStringConcat(tree) match {
        // Optimization for expressions of the form "" + x
        case List(Literal(Constant("")), arg) =>
          genApplyStaticMethod(
            defn.String_valueOf_Object,
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
                  if Erasure.Boxing.isBox(boxOp.symbol) &&
                    boxOp.symbol.denot.owner != defn.UnitModuleClass =>
                value
              case other => other
            }
            .toList
          // Estimate capacity needed for the string builder
          val approxBuilderSize = concatArguments.view.map {
            case Literal(Constant(s: String)) => s.length
            case Literal(c: Constant) if c.isNonUnitAnyVal =>
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

    private def genStaticMember(sym: Symbol, receiver: Symbol)(using
        nir.SourcePosition
    ): nir.Val = {
      /* Actually, there is no static member in Scala Native. If we come here, that
       * is because we found the symbol in a Java-emitted .class in the
       * classpath. But the corresponding implementation in Scala Native will
       * actually be a val in the companion module.
       */

      if (sym == defn.BoxedUnit_UNIT) nir.Val.Unit
      else if (sym == defn.BoxedUnit_TYPE) nir.Val.Unit
      else genApplyStaticMethod(sym, receiver, Seq.empty)
    }

    private def genSynchronized(receiverp: Tree, bodyp: Tree)(using
        nir.SourcePosition
    ): nir.Val = {
      genSynchronized(receiverp)(_.genExpr(bodyp))
    }

    def genSynchronized(
        receiverp: Tree
    )(bodyGen: ExprBuffer => nir.Val)(using nir.SourcePosition): nir.Val = {
      // Here we wrap the synchronized call into the try-finally block
      // to ensure that monitor would be released even in case of the exception
      // or in case of non-local returns
      val nested = new ExprBuffer()
      val normaln = fresh()
      val handler = fresh()
      val mergen = fresh()

      // scalanative.runtime.`package`.enterMonitor(receiver)
      genApplyStaticMethod(
        defnNir.RuntimePackage_enterMonitor,
        defnNir.RuntimePackageClass,
        List(receiverp)
      )

      // synchronized block
      val retValue = scoped(curUnwindHandler := Some(handler)) {
        nested.label(normaln)
        bodyGen(nested)
      }
      val retty = retValue.ty
      val mergev = nir.Val.Local(fresh(), retty)
      nested.jumpExcludeUnitValue(retty)(mergen, retValue)

      // dummy exception handler,
      // monitorExit call would be added to it in genTryFinally transformer
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
        ContTree(receiverp)(
          _.genApplyStaticMethod(
            defnNir.RuntimePackage_exitMonitor,
            defnNir.RuntimePackageClass,
            List(receiverp)
          )
        ),
        nested.toSeq
      )
      buf.labelExcludeUnitValue(mergen, mergev)
    }

    private def genThrow(tree: Tree, args: List[Tree]): nir.Val = {
      given nir.SourcePosition = tree.span
      val exception = args.head
      val res = genExpr(exception)
      buf.raise(res, unwind)
      nir.Val.Unit
    }

    def genCastOp(from: nir.Type, to: nir.Type, value: nir.Val)(using
        nir.SourcePosition
    ): nir.Val =
      castConv(from, to).fold(value)(buf.conv(_, to, value, unwind))

    private def genCoercion(app: Apply, receiver: Tree, code: Int): nir.Val = {
      given nir.SourcePosition = app.span
      val rec = genExpr(receiver)
      val (fromty, toty) = coercionTypes(code)

      genCoercion(rec, fromty, toty)
    }

    private def genCoercion(value: nir.Val, fromty: nir.Type, toty: nir.Type)(
        using nir.SourcePosition
    ): nir.Val = {
      if (fromty == toty) value
      else if (fromty == nir.Type.Nothing || toty == nir.Type.Nothing) value
      else {
        val conv = (fromty, toty) match {
          case (nir.Type.Ptr, _: nir.Type.RefKind) => nir.Conv.Bitcast
          case (_: nir.Type.RefKind, nir.Type.Ptr) => nir.Conv.Bitcast
          case (l: nir.Type.FixedSizeI, r: nir.Type.FixedSizeI) =>
            if (l.width < r.width)
              if (l.signed) nir.Conv.Sext
              else nir.Conv.Zext
            else if (l.width > r.width) nir.Conv.Trunc
            else nir.Conv.Bitcast
          case (i: nir.Type.I, _: nir.Type.F) if i.signed => nir.Conv.Sitofp
          case (_: nir.Type.I, _: nir.Type.F)             => nir.Conv.Uitofp
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
          case (nir.Type.Double, nir.Type.Float) => nir.Conv.Fptrunc
          case (nir.Type.Float, nir.Type.Double) => nir.Conv.Fpext
          case _ =>
            report.error(
              s"Unsupported coercion types: from $fromty to $toty"
            )
            nir.Conv.Bitcast
        }
        buf.conv(conv, toty, value, unwind)
      }
    }

    private def coercionTypes(code: Int): (nir.Type, nir.Type) = {
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

    private def castConv(fromty: nir.Type, toty: nir.Type): Option[nir.Conv] =
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
        case _ => unsupported(s"cast from $fromty to $toty")
      }

    /** Boxes a value of the given type before `elimErasedValueType`.
     *
     *  This should be used when sending values to a LLVM context, which is
     *  erased/boxed at the NIR level, although it is not erased at the
     *  dotty/JVM level.
     *
     *  @param value
     *    Value to be boxed if needed.
     *  @param tpeEnteringElimErasedValueType
     *    The type of `value` as it was entering the `elimErasedValueType`
     *    phase.
     */
    private def ensureBoxed(
        value: nir.Val,
        tpeEnteringPosterasure: core.Types.Type
    )(using buf: ExprBuffer, pos: nir.SourcePosition): nir.Val = {
      tpeEnteringPosterasure match {
        case tpe if tpe.isPrimitiveValueType =>
          buf.boxValue(tpe, value)

        case ErasedValueType(valueClass, _) =>
          val boxedClass = valueClass.typeSymbol.asClass
          val ctorName = genMethodName(boxedClass.primaryConstructor)
          val ctorSig = genMethodSig(boxedClass.primaryConstructor)

          val alloc = buf.classalloc(genTypeName(boxedClass), unwind)
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

    /** Unboxes a value typed as Any to the given type before
     *  `elimErasedValueType`.
     *
     *  This should be used when receiving values from a LLVM context, which is
     *  erased/boxed at the NIR level, although it is not erased at the
     *  dotty/JVM level.
     *
     *  @param value
     *    Tree to be extracted.
     *  @param tpeEnteringElimErasedValueType
     *    The type of `value` as it was entering the `elimErasedValueType`
     *    phase.
     */
    private def ensureUnboxed(
        value: nir.Val,
        tpeEnteringPosterasure: core.Types.Type
    )(using
        buf: ExprBuffer,
        pos: nir.SourcePosition
    ): nir.Val = {
      tpeEnteringPosterasure match {
        case tpe if tpe.isPrimitiveValueType =>
          val targetTpe = genType(tpeEnteringPosterasure)
          if (targetTpe == value.ty) value
          else buf.unbox(genBoxType(tpe), value, nir.Next.None)

        case ErasedValueType(valueClass, _) =>
          val boxedClass = valueClass.typeSymbol.asClass
          val unboxMethod = ValueClasses.valueClassUnbox(boxedClass)
          val castedValue = buf.genCastOp(value.ty, genType(valueClass), value)
          buf.genApplyMethod(
            sym = unboxMethod,
            statically = false,
            self = castedValue,
            argsp = Nil
          )

        case tpe =>
          val unboxed = buf.unboxValue(tpe, partial = true, value)
          if (unboxed == value) // no need to or cannot unbox, we should cast
            buf.genCastOp(genType(tpeEnteringPosterasure), genType(tpe), value)
          else unboxed
      }
    }

    // Native specifc features
    private def genRawPtrOp(app: Apply, code: Int): nir.Val = {
      if (NirPrimitives.isRawPtrLoadOp(code)) genRawPtrLoadOp(app, code)
      else if (NirPrimitives.isRawPtrStoreOp(code)) genRawPtrStoreOp(app, code)
      else if (code == NirPrimitives.ELEM_RAW_PTR) genRawPtrElemOp(app)
      else {
        report.error(s"Unknown pointer operation #$code : $app", app.sourcePos)
        nir.Val.Null
      }
    }

    private def genRawPtrLoadOp(app: Apply, code: Int): nir.Val = {
      import NirPrimitives._
      given nir.SourcePosition = app.span

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
        case LOAD_OBJECT   => nir.Rt.Object
      }
      val memoryOrder =
        Option.when(ptrp.symbol.isVolatile)(
          nir.MemoryOrder.Acquire
        )
      buf.load(ty, ptr, unwind, memoryOrder)
    }

    private def genRawPtrStoreOp(app: Apply, code: Int): nir.Val = {
      import NirPrimitives._
      given nir.SourcePosition = app.span
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
        case STORE_OBJECT   => nir.Rt.Object
      }
      val memoryOrder = Option.when(ptrp.symbol.isVolatile)(
        nir.MemoryOrder.Release
      )
      buf.store(ty, ptr, value, unwind, memoryOrder)
    }

    private def genRawPtrElemOp(app: Apply): nir.Val = {
      given nir.SourcePosition = app.span
      val Apply(_, Seq(ptrp, offsetp)) = app

      val ptr = genExpr(ptrp)
      val offset = genExpr(offsetp)
      buf.elem(nir.Type.Byte, ptr, Seq(offset), unwind)
    }

    private def genRawPtrCastOp(app: Apply): nir.Val = {
      given nir.SourcePosition = app.span
      val Apply(_, Seq(argp)) = app

      val fromty = genType(argp.tpe)
      val toty = genType(app.tpe)
      val value = genExpr(argp)

      genCastOp(fromty, toty, value)
    }

    def genRawSizeCastOp(app: Apply, code: Int): nir.Val = {
      import NirPrimitives._
      given pos: nir.SourcePosition = app.span
      val Apply(_, Seq(argp)) = app
      val rec = genExpr(argp)
      val (toty, conv) = code match {
        case CAST_RAWSIZE_TO_INT  => nir.Type.Int -> nir.Conv.SSizeCast
        case CAST_RAWSIZE_TO_LONG => nir.Type.Long -> nir.Conv.SSizeCast
        case CAST_RAWSIZE_TO_LONG_UNSIGNED =>
          nir.Type.Long -> nir.Conv.ZSizeCast
        case CAST_INT_TO_RAWSIZE          => nir.Type.Size -> nir.Conv.SSizeCast
        case CAST_INT_TO_RAWSIZE_UNSIGNED => nir.Type.Size -> nir.Conv.ZSizeCast
        case CAST_LONG_TO_RAWSIZE         => nir.Type.Size -> nir.Conv.SSizeCast
      }

      buf.conv(conv, toty, rec, unwind)
    }

    private def genUnsignedOp(app: Tree, code: Int): nir.Val = {
      given nir.SourcePosition = app.span
      import NirPrimitives._
      def castToUnsigned = code == UNSIGNED_OF
      def castUnsignedInteger = code >= BYTE_TO_UINT && code <= INT_TO_ULONG
      def castUnsignedToFloat = code >= UINT_TO_FLOAT && code <= ULONG_TO_DOUBLE
      app match {
        case Apply(_, Seq(argp)) if castToUnsigned =>
          val ty = genType(app.tpe.resultType)
          val arg = genExpr(argp)

          buf.box(ty, arg, unwind)

        case Apply(_, Seq(argp)) if castUnsignedInteger =>
          val ty = genType(app.tpe)
          val arg = genExpr(argp)

          buf.conv(nir.Conv.Zext, ty, arg, unwind)

        case Apply(_, Seq(argp)) if castUnsignedToFloat =>
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

    private def getLinktimeCondition(
        condp: Tree
    ): Option[nir.LinktimeCondition] = {
      import nir.LinktimeCondition._
      def genComparsion(name: Name, value: nir.Val): nir.Comp = {
        def intOrFloatComparison(onInt: nir.Comp, onFloat: nir.Comp) =
          value.ty match {
            case _: nir.Type.F =>
              onFloat
            case _ =>
              onInt
          }

        import nir.Comp._
        name match {
          case nme.EQ => intOrFloatComparison(Ieq, Feq)
          case nme.NE => intOrFloatComparison(Ine, Fne)
          case nme.GT => intOrFloatComparison(Sgt, Fgt)
          case nme.GE => intOrFloatComparison(Sge, Fge)
          case nme.LT => intOrFloatComparison(Slt, Flt)
          case nme.LE => intOrFloatComparison(Sle, Fle)
          case nme =>
            report.error(s"Unsupported condition '$nme'", condp.sourcePos)
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
            )(using position)
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
            )(using position)
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
            )(using position)
          }

        // special case for Scala 3 - it wraps != into !(_ == _)
        // if(property == x) (...)
        case Apply(
              Select(
                Apply(
                  Select(LinktimeProperty(name, _, position), nme.EQ),
                  List(arg @ Literal(Constant(_)))
                ),
                nme.UNARY_!
              ),
              Nil
            ) =>
          Some {
            val argValue = genLiteralValue(arg)
            SimpleCondition(
              propertyName = name,
              comparison = genComparsion(nme.NE, argValue),
              value = argValue
            )(using position)
          }

        // if(cond1 {&&,||} cond2) (...)
        case Apply(Select(cond1, op), List(cond2)) =>
          (getLinktimeCondition(cond1), getLinktimeCondition(cond2)) match {
            case (Some(c1), Some(c2)) =>
              val bin = op match {
                case nme.ZAND => nir.Bin.And
                case nme.ZOR  => nir.Bin.Or
              }
              given nir.SourcePosition = condp.span
              Some(ComplexCondition(bin, c1, c2))
            case (None, None) => None
            case _ =>
              report.error(
                "Mixing link-time and runtime conditions is not allowed",
                condp.sourcePos
              )
              None
          }

        case _ => None
      }
    }

    private lazy val optimizedFunctions = {
      // Included functions should be pure, and should not not narrow the result type
      Set[Symbol](
        defnNir.Intrinsics_castIntToRawSize,
        defnNir.Intrinsics_castIntToRawSizeUnsigned,
        defnNir.Intrinsics_castLongToRawSize,
        defnNir.Intrinsics_castRawSizeToInt,
        defnNir.Intrinsics_castRawSizeToLong,
        defnNir.Intrinsics_castRawSizeToLongUnsigned,
        defnNir.Size_fromByte,
        defnNir.Size_fromShort,
        defnNir.Size_fromInt,
        defnNir.USize_fromUByte,
        defnNir.USize_fromUShort,
        defnNir.USize_fromUInt,
        defnNir.RuntimePackage_fromRawSize,
        defnNir.RuntimePackage_fromRawUSize
      ) ++ defnNir.Intrinsics_unsignedOfAlts ++ defnNir.RuntimePackage_toRawSizeAlts
    }

    private def getUnboxedSize(sizep: Tree)(using nir.SourcePosition): nir.Val =
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
            if nir.Type.unbox.contains(sizeTy) then
              buf.unbox(sizeTy, size, unwind)
            else if nir.Type.box.contains(sizeTy) then size
            else {
              report.error(
                s"Invalid usage of Intrinsic.stackalloc, argument is not an integer type: ${sizeTy}",
                sizep.srcPos
              )
              nir.Val.Size(0)
            }

          if (unboxed.ty == nir.Type.Size) unboxed
          else buf.conv(nir.Conv.SSizeCast, nir.Type.Size, unboxed, unwind)
      }

    def genStackalloc(app: Apply): nir.Val = {
      given nir.SourcePosition = app.span
      val Apply(_, args) = app
      val tpe = app
        .getAttachment(NonErasedType)
        .map(genType(_, deconstructValueTypes = true))
        .getOrElse {
          report.error(
            "Not found type attachment for stackalloc operation, report it as a bug.",
            app.srcPos
          )
          nir.Type.Nothing
        }

      val size = args match {
        case Seq()         => nir.Val.Size(1)
        case Seq(sizep)    => getUnboxedSize(sizep)
        case Seq(_, sizep) => getUnboxedSize(sizep)
        case _             => scalanative.util.unreachable
      }
      buf.stackalloc(tpe, size, unwind)
    }

    def genSafeZoneAlloc(app: Apply): nir.Val = {
      val Apply(_, List(sz, tree)) = app
      // For new expression with a specified safe zone, e.g. `new {sz} T(...)`,
      // it's translated to `allocate(sz, new T(...))` in TyperPhase.
      tree match {
        case Apply(Select(New(_), nme.CONSTRUCTOR), _)          =>
        case Apply(fun, _) if fun.symbol == defn.newArrayMethod =>
        case _ =>
          report.error(
            s"Unexpected tree in scala.scalanative.runtime.SafeZoneAllocator.allocate: `${tree}`",
            tree.srcPos
          )
      }
      // Put the zone into the attachment of `new T(...)`.
      if tree.hasAttachment(SafeZoneInstance) then
        report.warning(
          s"Safe zone handle is already attached to ${tree}, which is unexpected.",
          tree.srcPos
        )
      tree.putAttachment(SafeZoneInstance, genExpr(sz))
      genExpr(tree)
    }

    def genCQuoteOp(app: Apply): nir.Val = {
      app match {
        // case q"""
        //   scala.scalanative.unsafe.`package`.CQuote(
        //     new StringContext(scala.this.Predef.wrapRefArray(
        //       Array[String]{${str: String}}.$asInstanceOf[Array[Object]]()
        //     ))
        //   ).c()
        case Apply(
              Select(
                Apply(
                  _, // Ident(CQuote),
                  List(
                    Apply(
                      _,
                      List(Apply(_, List(javaSeqLiteral: JavaSeqLiteral)))
                    )
                  )
                ),
                _
              ),
              _
            ) =>
          given nir.SourcePosition = app.span
          val List(Literal(Constant(str: String))) =
            javaSeqLiteral.elems: @unchecked
          val bytes = nir.Val.ByteString(StringUtils.processEscapes(str))
          val const = nir.Val.Const(bytes)
          buf.box(nir.Rt.BoxedPtr, const, unwind)

        case _ =>
          report.error("Failed to interpret CQuote", app.sourcePos)
          nir.Val.Null
      }
    }

    def genClassFieldRawPtr(app: Apply): nir.Val = {
      given nir.SourcePosition = app.span
      val Apply(_, List(target, fieldName: Literal)) = app: @unchecked
      val fieldNameId = fieldName.const.stringValue
      val classInfo = target.tpe.finalResultType
      val classInfoSym = classInfo.typeSymbol.asClass
      def matchesName(f: SingleDenotation) =
        f.name.mangled == termName(fieldNameId).mangled
      def isImmutableField(f: SymDenotation) = {
        // If `val` was defined in trait it would be internally mutable, but with stable accessors
        !f.is(Mutable) || classInfoSym.parentSyms.exists(s =>
          s.asClass.info.decls.exists { f =>
            matchesName(f) && f.asSymDenotation
              .isAllOf(Method | Accessor, butNot = Mutable)
          }
        )
      }

      val allFields =
        classInfoSym.info.fields ++ classInfoSym.info.parents.flatMap(_.fields)
      allFields
        .collectFirst {
          case f if matchesName(f) =>
            // Don't allow to get pointer to immutable field, as it might allow for mutation
            if (isImmutableField(f.asSymDenotation)) {
              val owner = f.asSymDenotation.owner
              report.error(
                s"Resolving pointer of immutable field ${fieldNameId} in ${owner.show} is not allowed"
              )
            }
            buf.field(genExpr(target), genFieldName(f.symbol), unwind)
        }
        .getOrElse {
          report.error(
            s"${classInfoSym.show} does not contain field ${fieldNameId}",
            app.sourcePos
          )
          nir.Val.Int(-1)
        }
    }

    def genSizeOf(app: Apply): nir.Val =
      genLayoutValueOf("sizeOf", buf.sizeOf(_, unwind))(app)
    def genAlignmentOf(app: Apply): nir.Val =
      genLayoutValueOf("alignmentOf", buf.alignmentOf(_, unwind))(app)

    private def genLayoutValueOf(
        opType: => String,
        toVal: nir.SourcePosition ?=> nir.Type => nir.Val
    )(app: Apply): nir.Val = {
      given nir.SourcePosition = app.span
      def fail(msg: => String) =
        report.error(msg, app.srcPos)
        nir.Val.Zero(nir.Type.Size)

      app.getAttachment(NirDefinitions.NonErasedType) match
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
        case Some(tpe) if tpe.typeSymbol.isTraitOrInterface =>
          fail(
            s"Type ${tpe.show} is a trait or interface, its $opType cannot be calculated"
          )
        case Some(tpe) =>
          try {
            val nirTpe = genType(tpe, deconstructValueTypes = true)
            toVal(nirTpe)
          } catch {
            case ex: Throwable =>
              fail(
                s"Failed to generate exact NIR type of ${tpe.show} - ${ex.getMessage}"
              )
          }
    }

    def genLoadExtern(ty: nir.Type, externTy: nir.Type, sym: Symbol)(using
        nir.SourcePosition
    ): nir.Val = {
      assert(sym.isExtern, "loadExtern was not extern")

      val name = nir.Val.Global(genName(sym), nir.Type.Ptr)
      val memoryOrder = Option.when(sym.isVolatile)(
        nir.MemoryOrder.Acquire
      )
      fromExtern(
        ty,
        buf.load(externTy, name, unwind, memoryOrder)
      )
    }

    def genStoreExtern(externTy: nir.Type, sym: Symbol, value: nir.Val)(using
        nir.SourcePosition
    ): nir.Val = {
      assert(sym.isExtern, "storeExtern was not extern")
      val name = nir.Val.Global(genName(sym), nir.Type.Ptr)
      val externValue = toExtern(externTy, value)
      val memoryOrder = Option.when(sym.isVolatile)(
        nir.MemoryOrder.Release
      )

      buf.store(externTy, name, externValue, unwind, memoryOrder)
    }

    def toExtern(expectedTy: nir.Type, value: nir.Val)(using
        nir.SourcePosition
    ): nir.Val =
      (expectedTy, value.ty) match {
        case (nir.Type.Unit, _) => nir.Val.Unit
        case (_, refty: nir.Type.Ref)
            if nir.Type.boxClasses.contains(refty.name)
              && nir.Type.unbox(nir.Type.Ref(refty.name)) == expectedTy =>
          buf.unbox(nir.Type.Ref(refty.name), value, unwind)
        case _ =>
          value
      }

    def fromExtern(expectedTy: nir.Type, value: nir.Val)(using
        nir.SourcePosition
    ): nir.Val =
      (expectedTy, value.ty) match {
        case (refty: nir.Type.Ref, ty)
            if nir.Type.boxClasses.contains(refty.name)
              && nir.Type.unbox(nir.Type.Ref(refty.name)) == ty =>
          buf.box(nir.Type.Ref(refty.name), value, unwind)
        case _ =>
          value
      }

    /** Generates direct call to function ptr with optional unboxing arguments
     *  and boxing result Apply.args can contain different number of arguments
     *  depending on usage, however they are passed in constant order:
     *    - 0..N args
     *    - return type evidence
     */
    private def genCFuncPtrApply(app: Apply): nir.Val = {
      given nir.SourcePosition = app.span
      val Apply(appRec @ Select(receiverp, _), aargs) = app: @unchecked

      val attachment = app
        .getAttachment(NirDefinitions.NonErasedTypes)
        .orElse(appRec.getAttachment(NirDefinitions.NonErasedTypes))

      val paramTypes = attachment match {
        case None =>
          report.error(
            s"Failed to generated exact NIR types for $app, something is wrong with scala-native internls.",
            app.srcPos
          )
          return nir.Val.Null
        case Some(paramTys) => paramTys
      }

      val self = genExpr(receiverp)
      val retType = genType(paramTypes.last)
      val unboxedRetType = nir.Type.unbox.getOrElse(retType, retType)

      val args = aargs
        .zip(paramTypes)
        .map {
          case (Apply(Select(_, nme.box), List(value)), _) =>
            genExpr(value)
          case (arg, ty) =>
            given nir.SourcePosition = arg.span
            val tpe = genType(ty)
            val obj = genExpr(arg)

            /* buf.unboxValue does not handle Ref( Ptr | CArray | ... ) unboxing
             * That's why we're doing it directly */
            if (nir.Type.unbox.isDefinedAt(tpe)) buf.unbox(tpe, obj, unwind)
            else buf.unboxValue(fromType(ty), partial = false, obj)
        }
      val argTypes = args.map(_.ty)
      val funcSig = nir.Type.Function(argTypes, unboxedRetType)

      val selfName = genTypeName(defnNir.CFuncPtrClass)
      val getRawPtrName = selfName
        .member(nir.Sig.Field("rawptr", nir.Sig.Scope.Private(selfName)))

      val target = buf.fieldload(nir.Type.Ptr, self, getRawPtrName, unwind)
      val result = buf.call(funcSig, target, args, unwind)
      if (retType != unboxedRetType) buf.box(retType, result, unwind)
      else boxValue(paramTypes.last, result)
    }

    private final val ExternForwarderSig =
      nir.Sig.Generated("$extern$forwarder")

    private def genCFuncFromScalaFunction(app: Apply): nir.Val = {
      given pos: nir.SourcePosition = app.span
      val paramTypes = app.getAttachment(NirDefinitions.NonErasedTypes) match
        case None =>
          report.error(
            s"Failed to generate exact NIR types for $app, something is wrong with scala-native internals.",
            app.srcPos
          )
          Nil
        case Some(paramTys) =>
          paramTys.map(fromType)

      val fn :: _ = app.args: @unchecked

      @tailrec
      def resolveFunction(tree: Tree): nir.Val = tree match {
        case Typed(expr, _) => resolveFunction(expr)
        case Block(_, expr) => resolveFunction(expr)
        case fn @ Closure(env, target, _) =>
          if env.nonEmpty then
            report.error(
              s"Closing over local state of ${env.map(_.symbol.show).mkString(", ")} in function transformed to CFuncPtr results in undefined behaviour.",
              fn.srcPos
            )

          val fnRef = genClosure(fn)
          val nir.Type.Ref(className, _, _) = fnRef.ty: @unchecked

          generatedDefns += genFuncExternForwarder(
            className,
            target.symbol,
            fn,
            paramTypes
          )
          fnRef

        case ref: RefTree =>
          report.error(
            s"Function passed to ${app.symbol.show} needs to be inlined",
            tree.sourcePos
          )
          nir.Val.Null

        case _ =>
          report.error(
            "Failed to resolve function ref for extern forwarder",
            tree.sourcePos
          )
          nir.Val.Null
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

    private def genFuncExternForwarder(
        funcName: nir.Global,
        funSym: Symbol,
        funTree: Closure,
        evidences: List[SimpleType]
    )(using nir.SourcePosition): nir.Defn = {
      val attrs = nir.Attrs(isExtern = true)

      // In case if passed function is adapted closure it's param types
      // would be erased, in such case we would recover original types
      // using evidence types (materialized unsafe.Tags)
      val isAdapted = funSym.name.mangledString.contains("$adapted$")
      val sig = genMethodSig(funSym)
      val externSig = genExternMethodSig(funSym)

      val nir.Type.Function(origtys, _) =
        if (!isAdapted) sig
        else {
          val params :+ retty = evidences
            .map(genType(_))
            .map(t => nir.Type.box.getOrElse(t, t)): @unchecked
          nir.Type.Function(params, retty)
        }

      val forwarderSig @ nir.Type.Function(paramtys, retty) =
        if (!isAdapted) externSig
        else {
          val params :+ retty = evidences
            .map(genExternType)
            .map(t => nir.Type.unbox.getOrElse(t, t)): @unchecked
          nir.Type.Function(params, retty)
        }

      val forwarderName = funcName.member(ExternForwarderSig)
      val forwarderBody = scoped(
        curUnwindHandler := None,
        curScopeId := nir.ScopeId.TopLevel
      ) {
        val fresh = nir.Fresh()
        val buf = ExprBuffer(using fresh)

        val params = paramtys.map(ty => nir.Val.Local(fresh(), ty))
        buf.label(fresh(), params)
        val origTypes =
          if (funSym.isStaticInNIR || isAdapted) origtys else origtys.tail
        val boxedParams = origTypes.zip(params).map(buf.fromExtern(_, _))
        val argsp = boxedParams.map(ValTree(funTree)(_))

        // Check number of arguments that would be be used in a call to the function,
        // it should be equal to the quantity of implicit evidences (without return type evidence)
        // and arguments passed via closure env.
        if (argsp.size != evidences.length - 1 + funTree.env.size) {
          report.error(
            "Failed to create scalanative.unsafe.CFuncPtr from scala.Function, report this issue to Scala Native team.",
            funTree.srcPos
          )
        }

        val res =
          if (funSym.isStaticInNIR)
            buf.genApplyStaticMethod(funSym, NoSymbol, argsp)
          else
            val owner = buf.genModule(funSym.owner)
            val selfp = ValTree(funTree)(owner)
            buf.genApplyMethod(funSym, statically = true, selfp, argsp)

        val unboxedRes = buf.toExtern(retty, res)
        buf.ret(unboxedRes)

        buf.toSeq
      }
      new nir.Defn.Define(attrs, forwarderName, forwarderSig, forwarderBody)
    }

    private object WrapArray {
      lazy val isWrapArray: Set[Symbol] = {
        val names = defn
          .ScalaValueClasses()
          .map(sym => nme.wrapXArray(sym.name))
          .concat(Set(nme.wrapRefArray, nme.genericWrapArray))
        val symsInPredef = names.map(defn.ScalaPredefModule.requiredMethod(_))
        val symsInScalaRunTime =
          names.map(defn.ScalaRuntimeModule.requiredMethod(_))
        (symsInPredef ++ symsInScalaRunTime).toSet
      }

      def unapply(tree: Apply): Option[Tree] = tree match {
        case Apply(wrapArray_?, List(wrapped))
            if isWrapArray(wrapArray_?.symbol) =>
          Some(wrapped)
        case _ =>
          None
      }
    }

    private def genReflectiveCall(
        tree: Apply,
        isSelectDynamic: Boolean
    ): nir.Val = {
      given nir.SourcePosition = tree.span
      val Apply(fun @ Select(receiver, _), args) = tree: @unchecked

      val selectedValue = genApplyMethod(
        defnNir.ReflectSelectable_selectedValue,
        statically = false,
        genExpr(receiver),
        Seq.empty
      )

      // Extract the method name as a String
      val methodNameStr = args.head match {
        case Literal(Constants.Constant(name: String)) => name
        case _ =>
          report.error(
            "The method name given to Selectable.selectDynamic or Selectable.applyDynamic " +
              "must be a literal string. " +
              "Other uses are not supported in Scala Native.",
            args.head.sourcePos
          )
          "erroneous"
      }

      val (formalParamTypeRefs, actualArgs) =
        if (isSelectDynamic) (Nil, Nil)
        else
          args.tail match {
            // Extract the param type refs and actual args from the 2nd and 3rd argument to applyDynamic
            case WrapArray(classOfsArray: JavaSeqLiteral) ::
                WrapArray(actualArgsAnyArray: JavaSeqLiteral) :: Nil =>
              // Extract nir.Type's from the classOf[_] trees
              val formalParamTypes = classOfsArray.elems.map {
                // classOf[tp] -> tp
                case Literal(const) if const.tag == Constants.ClazzTag =>
                  genType(const.typeValue)

                // Anything else is invalid
                case otherTree =>
                  report.error(
                    "The java.lang.Class[_] arguments passed to Selectable.applyDynamic must be " +
                      "literal classOf[T] expressions (typically compiler-generated). " +
                      "Other uses are not supported in Scala Native.",
                    otherTree.sourcePos
                  )
                  nir.Rt.Object
              }

              // Gen the actual args, downcasting them to the formal param types
              val actualArgs =
                actualArgsAnyArray.elems
                  .zip(formalParamTypes)
                  .map { (actualArgAny, formalParamType) =>
                    val genActualArgAny = genExpr(actualArgAny)
                    (genActualArgAny.ty, formalParamType) match {
                      case (ty: nir.Type.Ref, formal: nir.Type.Ref) =>
                        if ty.name == formal.name then genActualArgAny
                        else
                          buf.as(
                            formalParamType,
                            genActualArgAny,
                            unwind
                          )

                      case (ty: nir.Type.Ref, formal: nir.Type.PrimitiveKind) =>
                        assert(nir.Type.Ref(ty.name) == nir.Type.box(formal))
                        genActualArgAny

                      case _ => scalanative.util.unreachable
                    }
                  }

              (formalParamTypes, actualArgs)

            case _ =>
              report.error(
                "Passing the varargs of Selectable.applyDynamic with `: _*` " +
                  "is not supported in Scala Native.",
                tree.sourcePos
              )
              (Nil, Nil)
          }

      val dynMethod = buf.dynmethod(
        selectedValue,
        nir.Sig.Proxy(methodNameStr, formalParamTypeRefs),
        unwind
      )
      // Proxies operate only on boxed types, however formal param types and name of the method
      // might contain primitive types. With current imlementation of proxies we workaround it
      // by always using boxed types in function calls
      val boxedFormalParamTypeRefs = formalParamTypeRefs.map {
        case ty: nir.Type.PrimitiveKind =>
          nir.Type.box(ty)
        case ty =>
          ty
      }
      buf.call(
        nir.Type.Function(
          selectedValue.ty :: boxedFormalParamTypeRefs,
          nir.Rt.Object
        ),
        dynMethod,
        selectedValue :: actualArgs,
        unwind
      )
    }

    private def labelExcludeUnitValue(label: nir.Local, value: nir.Val.Local)(
        using nir.SourcePosition
    ): nir.Val =
      value.ty match
        case nir.Type.Unit =>
          buf.label(label); nir.Val.Unit
        case _ =>
          buf.label(label, Seq(value)); value

    private def jumpExcludeUnitValue(
        mergeType: nir.Type
    )(label: nir.Local, value: nir.Val)(using
        nir.SourcePosition
    ): Unit =
      mergeType match
        case nir.Type.Unit =>
          buf.jump(label, Nil)
        case _ =>
          buf.jump(label, Seq(value))

  }

  sealed class FixupBuffer(using fresh: nir.Fresh)
      extends nir.InstructionBuilder {
    private var labeled = false

    override def +=(inst: nir.Inst): Unit = {
      given nir.SourcePosition = inst.pos
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
        case _ => ()
      }
    }

    override def ++=(insts: Seq[nir.Inst]): Unit =
      insts.foreach { inst => this += inst }

    override def ++=(other: nir.InstructionBuilder): Unit =
      this ++= other.toSeq
  }

}

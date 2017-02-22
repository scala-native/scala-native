package scala.scalanative
package nscplugin

import java.nio.file.Path
import scala.collection.mutable, mutable.UnrolledBuffer
import scala.tools.nsc.{util => _, _}
import scala.tools.nsc.plugins._
import scala.util.{Either, Left, Right}
import scala.reflect.internal.Flags._
import util._, util.ScopedVar.scoped
import nir.Focus, Focus.{sequenced, merged}
import nir._
import NirPrimitives._

abstract class NirCodeGen
    extends PluginComponent
    with NirFiles
    with NirTypeEncoding
    with NirNameEncoding {
  val nirAddons: NirGlobalAddons {
    val global: NirCodeGen.this.global.type
  }

  import global._
  import definitions._
  import treeInfo.hasSynthCaseSymbol
  import nirAddons._
  import nirDefinitions._
  import SimpleType.{fromType, fromSymbol}

  val phaseName = "nir"

  override def newPhase(prev: Phase): StdPhase =
    new NirCodePhase(prev)

  case class ValTree(value: nir.Val) extends Tree
  case class ContTree(f: nir.Focus => nir.Focus) extends Tree

  class MethodEnv(val fresh: Fresh) {
    private val env = mutable.Map.empty[Symbol, Val]

    def enter(sym: Symbol, value: Val): Unit =
      env += ((sym, value))

    def enterLabel(ld: LabelDef): Local = {
      val local = fresh()
      enter(ld.symbol, Val.Local(local, Type.Ptr))
      local
    }

    def resolve(sym: Symbol): Val = env(sym)

    def resolveLabel(ld: LabelDef): Local = {
      val Val.Local(n, Type.Ptr) = resolve(ld.symbol)
      n
    }
  }

  class CollectMethodInfo extends Traverser {
    var mutableVars = Set.empty[Symbol]
    var labels      = Set.empty[LabelDef]

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

  class NirCodePhase(prev: Phase) extends StdPhase(prev) {
    private val curClassSym   = new util.ScopedVar[Symbol]
    private val curClassDefns = new util.ScopedVar[UnrolledBuffer[nir.Defn]]
    private val curMethodSym  = new util.ScopedVar[Symbol]
    private val curMethodInfo = new util.ScopedVar[CollectMethodInfo]
    private val curMethodEnv  = new util.ScopedVar[MethodEnv]
    private val curMethodThis = new util.ScopedVar[Option[Val]]
    private val curUnwind     = new util.ScopedVar[nir.Next]

    private val lazyAnonDefs =
      mutable.Map.empty[Symbol, ClassDef]
    private val resultDefns =
      mutable.UnrolledBuffer.empty[(Path, Seq[Defn])]

    private def consumeLazyAnonDef(sym: Symbol): ClassDef = {
      lazyAnonDefs
        .get(sym)
        .fold {
          sys.error(s"Couldn't find anon def for $sym")
        } { cd =>
          lazyAnonDefs.remove(cd.symbol)
          cd
        }
    }

    private implicit def fresh: Fresh =
      curMethodEnv.fresh

    override def run(): Unit = {
      scalaPrimitives.init()
      nirPrimitives.init()
      super.run()
      genIRFiles(resultDefns)
      resultDefns.clear()
    }

    override def apply(cunit: CompilationUnit): Unit = {
      try {
        def collectClassDefs(tree: Tree): List[ClassDef] = {
          tree match {
            case EmptyTree            => Nil
            case PackageDef(_, stats) => stats flatMap collectClassDefs
            case cd: ClassDef         => cd :: Nil
          }
        }

        val (anonDefs, classDefs) = collectClassDefs(cunit.body).partition {
          cd =>
            cd.symbol.isAnonymousFunction
        }

        lazyAnonDefs ++= anonDefs.map(cd => cd.symbol -> cd)

        val allDefns = UnrolledBuffer.empty[(Symbol, Seq[nir.Defn])]

        classDefs.foreach { cd =>
          val sym = cd.symbol

          if (isPrimitiveValueClass(sym) || (sym == ArrayClass)) ()
          else allDefns += sym -> genClass(cd)
        }

        lazyAnonDefs.foreach {
          case (sym, cd) =>
            allDefns += sym -> genClass(cd)
        }

        allDefns.foreach {
          case (sym, defns) =>
            resultDefns += ((getPathFor(cunit, sym), defns))
        }
      } finally {
        lazyAnonDefs.clear()
      }
    }

    def genClass(cd: ClassDef): Seq[Defn] = {
      scoped(
          curClassSym := cd.symbol,
          curClassDefns := UnrolledBuffer.empty[nir.Defn]
      ) {
        if (cd.symbol.isStruct) genStruct(cd)
        else genNormalClass(cd)

        curClassDefns.get
      }
    }

    def genStruct(cd: ClassDef): Unit = {
      val sym    = cd.symbol
      val attrs  = genStructAttrs(sym)
      val name   = genTypeName(sym)
      val fields = genStructFields(sym)
      val body   = cd.impl.body

      curClassDefns += Defn.Struct(attrs, name, fields)
      genMethods(cd)
    }

    def genStructAttrs(sym: Symbol): Attrs = Attrs.None

    def genNormalClass(cd: ClassDef): Unit = {
      val sym = cd.symbol
      def attrs  = genClassAttrs(cd)
      def name   = genTypeName(sym)
      def parent = genClassParent(sym)
      def traits = genClassInterfaces(sym)

      genClassFields(sym)
      genMethods(cd)

      curClassDefns += {
        if (sym.isScalaModule) Defn.Module(attrs, name, parent, traits)
        else if (sym.isInterface) Defn.Trait(attrs, name, traits)
        else Defn.Class(attrs, name, parent, traits)
      }
    }

    def genClassParent(sym: Symbol): Option[nir.Global] =
      if (sym == NObjectClass) None
      else if (sym.superClass == NoSymbol || sym.superClass == ObjectClass)
        Some(genTypeName(NObjectClass))
      else Some(genTypeName(sym.superClass))

    def genClassAttrs(cd: ClassDef): Attrs = {
      val sym = cd.symbol
      def pinned = {
        val ctor =
          if (!sym.isScalaModule || sym.isExternModule) {
            Seq()
          } else {
            Seq(Attr.PinAlways(genMethodName(sym.asClass.primaryConstructor)))
          }
        val annotated = for {
          decl <- sym.info.decls
          if decl.hasAnnotation(PinClass)
        } yield {
          Attr.PinAlways(genName(decl))
        }

        annotated.toSeq ++ ctor
      }
      val attrs = sym.annotations.collect {
        case ann if ann.symbol == ExternClass => Attr.Extern
        case ann if ann.symbol == LinkClass =>
          val Apply(_, Seq(Literal(Constant(name: String)))) = ann.tree
          Attr.Link(name)
      }
      val pure = if (PureModules.contains(sym)) Seq(Attr.Pure) else Seq()

      val weak = cd.impl.body.collect {
        case dd: DefDef =>
          Attr.PinWeak(genMethodName(dd.symbol))
      }

      Attrs.fromSeq(pinned ++ pure ++ attrs ++ weak)
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol if psym.isInterface
      } yield {
        genTypeName(psym)
      }

    def genClassFields(sym: Symbol): Unit = {
      val attrs = nir.Attrs(isExtern = sym.isExternModule)

      for (f <- sym.info.decls if f.isField) {
        val ty   = genType(f.tpe, box = false)
        val name = genFieldName(f)
        val rhs  = if (sym.isExternModule) Val.None else Val.Zero(ty)

        curClassDefns += Defn.Var(attrs, name, ty, rhs)
      }
    }

    def genMethods(cd: ClassDef): Unit =
      cd.impl.body.foreach {
        case dd: DefDef =>
          genMethod(dd)
        case _ =>
          ()
      }

    def genMethod(dd: DefDef): Unit = {
      val fresh = new Fresh("src")
      val env   = new MethodEnv(fresh)

      scoped(
          curMethodSym := dd.symbol,
          curMethodEnv := env,
          curMethodInfo := (new CollectMethodInfo).collect(dd.rhs),
          curUnwind := Next.None
      ) {
        val sym      = dd.symbol
        val owner    = curClassSym.get
        val attrs    = genMethodAttrs(sym)
        val name     = genMethodName(sym)
        val isStatic = owner.isExternModule || owner.isImplClass
        val sig      = genMethodSig(sym, isStatic)
        val params   = genParams(owner, dd, isStatic)

        dd.rhs match {
          case EmptyTree =>
            curClassDefns += Defn.Declare(attrs, name, sig)

          case _
              if dd.name == nme.CONSTRUCTOR && owner.isExternModule =>
            validateExternCtor(dd.rhs)
            ()

          case _ if dd.name == nme.CONSTRUCTOR && owner.isStruct =>
            ()

          case rhs if owner.isExternModule =>
            genExternMethod(attrs, name, sig, params, rhs)

          case rhs =>
            val body = genNormalMethodBody(params, rhs, isStatic)
            curClassDefns += Defn.Define(attrs, name, sig, body)
        }
      }
    }

    def genExternMethod(attrs: nir.Attrs,
                        name: nir.Global,
                        sig: nir.Type,
                        params: Seq[nir.Val.Local],
                        rhs: Tree): Unit = {
      rhs match {
        case Apply(ref: RefTree, Seq()) if ref.symbol == ExternMethod =>
          val moduleName  = genTypeName(curClassSym)
          val externAttrs = Attrs(isExtern = true)
          val externDefn  = Defn.Declare(externAttrs, name, sig)

          curClassDefns += externDefn

        case _ if curMethodSym.hasFlag(ACCESSOR) =>
          ()

        case rhs =>
          unsupported("methods in extern objects must have extern body")
      }
    }

    def validateExternCtor(rhs: Tree): Unit = {
      val Block(_ +: init, _) = rhs
      val externs = init.map {
        case Assign(ref: RefTree, Apply(extern, Seq()))
            if extern.symbol == ExternMethod =>
          ref.symbol
        case _ =>
          unsupported(
              "extern objects may only contain " + "extern fields and methods")
      }.toSet
      for {
        f <- curClassSym.info.decls if f.isField
        if !externs.contains(f)
      } {
        unsupported("extern objects may only contain extern fields")
      }
    }

    def genMethodAttrs(sym: Symbol): Attrs =
      Attrs.fromSeq({
        if (sym.hasFlag(ACCESSOR)) Seq(Attr.AlwaysInline)
        else Seq()
      } ++ sym.overrides.map {
        case sym => Attr.Override(genMethodName(sym))
      } ++ sym.annotations.collect {
        case ann if ann.symbol == NoInlineClass   => Attr.NoInline
        case ann if ann.symbol == InlineHintClass => Attr.InlineHint
        case ann if ann.symbol == InlineClass     => Attr.AlwaysInline
      } ++ {
        if (PureMethods.contains(sym)) Seq(Attr.Pure) else Seq()
      } ++ {
        val owner = sym.owner
        if (owner.primaryConstructor eq sym)
          owner.info.declarations.collect {
            case decl if decl.overrides.nonEmpty =>
              decl.overrides.map {
                case ov =>
                  Attr.PinIf(genMethodName(decl), genMethodName(ov))
              }
          }.toSeq.flatten
        else Seq()
      })

    def genMethodSigParams(sym: Symbol): Seq[nir.Type] = {
      val wereRepeated = exitingPhase(currentRun.typerPhase) {
        for {
          params <- sym.tpe.paramss
          param  <- params
        } yield {
          param.name -> isScalaRepeatedParamType(param.tpe)
        }
      }.toMap

      sym.tpe.params.map {
        case p
            if wereRepeated.getOrElse(p.name, false) &&
            sym.owner.isExternModule =>
          Type.Vararg

        case p =>
          genType(p.tpe, box = false)
      }
    }

    def genMethodSig(
        sym: Symbol, forceStatic: Boolean = false): nir.Type.Function = {
      require(sym.isMethod || sym.isStaticMember)

      val tpe      = sym.tpe
      val owner    = sym.owner
      val paramtys = genMethodSigParams(sym)
      val selfty   =
        if (forceStatic || owner.isExternModule || owner.isImplClass) None
        else Some(genType(owner.tpe, box = true))
      val retty =
        if (sym.isClassConstructor) Type.Unit
        else genType(sym.tpe.resultType, box = false)

      Type.Function(selfty ++: paramtys, retty)
    }

    def genParams(
        owner: Symbol, dd: DefDef, isStatic: Boolean): Seq[Val.Local] = {
      val paramSyms = {
        val vp = dd.vparamss
        if (vp.isEmpty) Nil else vp.head.map(_.symbol)
      }
      val self =
        if (isStatic) None
        else Some(Val.Local(fresh(), genType(curClassSym.tpe, box = true)))
      val params = paramSyms.map { sym =>
        val name  = fresh()
        val ty    = genType(sym.tpe, box = false)
        val param = Val.Local(name, ty)
        curMethodEnv.enter(sym, param)
        param
      }

      self ++: params
    }

    def genNormalMethodBody(params: Seq[Val.Local],
                            bodyp: Tree,
                            isStatic: Boolean): Seq[nir.Inst] = {
      val entry = {
        val start = Focus.start().withLabel(fresh(), params: _*)
        val vars  = curMethodInfo.mutableVars.toSeq

        vars.foldLeft(start) {
          case (focus, sym) =>
            val ty    = genType(sym.info, box = false)
            val alloc = focus withOp Op.Stackalloc(ty, Val.None)
            curMethodEnv.enter(sym, alloc.value)
            alloc
        }
      }
      val body = bodyp match {
        // Tailrec emits magical labeldefs that can hijack this reference is
        // current method. This requires special treatment on our side.
        case Block(List(ValDef(_, nme.THIS, _, _)),
                   label @ LabelDef(name, Ident(nme.THIS) :: _, rhs)) =>
          val local  = curMethodEnv.enterLabel(label)
          val values = params.take(label.params.length)
          val jump   = entry.withJump(local, values: _*)

          genLabel(label, jump, hijackThis = true)

        case _ if curMethodSym.get == NObjectInitMethod =>
          return Seq(
              Inst.Label(fresh(), params),
              Inst.Ret(nir.Val.Unit)
          )

        case _ =>
          scoped(
              curMethodThis := {
                if (isStatic) None
                else Some(Val.Local(params.head.name, params.head.ty))
              }
          ) {
            genExpr(bodyp, entry)
          }
      }

      body.finish(Inst.Ret(body.value))
    }

    def genExpr(tree: Tree, focus: Focus): Focus = {
      tree match {
        case ValTree(value) =>
          focus withValue value

        case ContTree(f) =>
          f(focus)

        case label: LabelDef =>
          assert(label.params.length == 0)
          val local = curMethodEnv.enterLabel(label)
          genLabel(label, focus.withJump(local))

        case vd: ValDef =>
          val rhs       = genExpr(vd.rhs, focus)
          val isMutable = curMethodInfo.mutableVars.contains(vd.symbol)
          if (!isMutable) {
            curMethodEnv.enter(vd.symbol, rhs.value)
            rhs withValue Val.None
          } else {
            val ty    = genType(vd.symbol.tpe, box = false)
            val alloc = curMethodEnv.resolve(vd.symbol)
            rhs withOp Op.Store(ty, alloc, rhs.value)
          }

        case If(cond, thenp, elsep) =>
          val retty = genType(tree.tpe, box = false)
          genIf(retty, cond, thenp, elsep, focus)

        case Return(exprp) =>
          val expr = genExpr(exprp, focus)
          expr.withRet(expr.value)

        case Try(expr, catches, finalizer)
            if catches.isEmpty && finalizer.isEmpty =>
          genExpr(expr, focus)

        case Try(expr, catches, finalizer) =>
          val retty = genType(tree.tpe, box = false)
          genTry(retty, expr, catches, finalizer, focus)

        case Throw(exprp) =>
          val expr = genExpr(exprp, focus)
          expr.withThrow(expr.value, curUnwind)

        case app: Apply =>
          genApply(app, focus)

        case app: ApplyDynamic =>
          genApplyDynamic(app, focus)

        case This(qual) =>
          if (curMethodThis.nonEmpty && tree.symbol == curClassSym.get) {
            focus withValue curMethodThis.get.get
          } else {
            genModule(tree.symbol, focus)
          }

        case sel @ Select(qualp, selp) =>
          val sym   = tree.symbol
          val owner = sym.owner
          if (sym.isModule) {
            focus withOp Op.Module(genTypeName(sym), curUnwind)
          } else if (sym.isStaticMember) {
            genStaticMember(sym, focus)
          } else if (sym.isMethod) {
            genMethodCall(sym, statically = false, qualp, Seq(), focus)
          } else if (owner.isStruct) {
            val index = owner.info.decls.filter(_.isField).toList.indexOf(sym)
            val qual  = genExpr(qualp, focus)
            qual withOp Op.Extract(qual.value, Seq(index))
          } else {
            val ty   = genType(tree.symbol.tpe, box = false)
            val qual = genExpr(qualp, focus)
            val name = genFieldName(tree.symbol)
            val elem =
              if (sym.owner.isExternModule) {
                qual withValue Val.Global(name, Type.Ptr)
              } else {
                qual withOp Op.Field(qual.value, name)
              }
            elem withOp Op.Load(ty, elem.value)
          }

        case id: Ident =>
          val sym = id.symbol
          if (curMethodInfo.mutableVars.contains(sym)) {
            val ty = genType(sym.tpe, box = false)
            focus withOp Op.Load(ty, curMethodEnv.resolve(sym))
          } else if (sym.isModule) {
            focus withOp Op.Module(genTypeName(sym), curUnwind)
          } else {
            focus withValue (curMethodEnv.resolve(sym))
          }

        case lit: Literal =>
          genLiteral(lit, focus)

        case block: Block =>
          genBlock(block, focus)

        case Typed(Super(_, _), _) =>
          focus.withValue(curMethodThis.get.get)

        case Typed(expr, _) =>
          genExpr(expr, focus)

        case Assign(lhsp, rhsp) =>
          lhsp match {
            case sel @ Select(qualp, _) =>
              val ty   = genType(sel.tpe, box = false)
              val qual = genExpr(qualp, focus)
              val rhs  = genExpr(rhsp, qual)
              val name = genFieldName(sel.symbol)
              val elem =
                if (sel.symbol.owner.isExternModule) {
                  rhs withValue Val.Global(name, Type.Ptr)
                } else {
                  rhs withOp Op.Field(qual.value, name)
                }

              elem withOp Op.Store(ty, elem.value, rhs.value)

            case id: Ident =>
              val ty  = genType(id.tpe, box = false)
              val rhs = genExpr(rhsp, focus)
              val ptr = curMethodEnv.resolve(id.symbol)

              rhs withOp Op.Store(ty, ptr, rhs.value)
          }

        case av: ArrayValue =>
          genArrayValue(av, focus)

        case m: Match =>
          genSwitch(m, focus)

        case fun: Function =>
          unsupported(fun)

        case EmptyTree =>
          focus

        case _ =>
          abort("Unexpected tree in genExpr: " + tree + "/" + tree.getClass +
              " at: " + tree.pos)
      }
    }

    def genLiteral(lit: Literal, focus: Focus): Focus = {
      val value = lit.value
      value.tag match {
        case UnitTag | NullTag | BooleanTag | ByteTag | ShortTag | CharTag |
            IntTag | LongTag | FloatTag | DoubleTag | StringTag =>
          focus withValue genLiteralValue(lit)

        case ClazzTag =>
          val ty = focus withValue genTypeValue(value.typeValue)
          genBoxClass(ty.value, ty)

        case EnumTag =>
          genStaticMember(value.symbolValue, focus)
      }
    }

    def genLiteralValue(lit: Literal): Val = {
      val value = lit.value
      value.tag match {
        case UnitTag =>
          Val.Unit
        case NullTag =>
          Val.Zero(Rt.Object)
        case BooleanTag =>
          if (value.booleanValue) Val.True else Val.False
        case ByteTag =>
          Val.Byte(value.intValue.toByte)
        case ShortTag | CharTag =>
          Val.Short(value.intValue.toShort)
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

    def genIf(retty: nir.Type,
              condp: Tree,
              thenp: Tree,
              elsep: Tree,
              focus: Focus) = {
      val thenn, elsen = fresh()

      val cond  = genExpr(condp, focus)
      val br    = cond.withIf(cond.value, Next(thenn), Next(elsen))

      val thenbr = genExpr(thenp, Focus.start.withLabel(thenn))
      val elsebr = genExpr(elsep, Focus.start.withLabel(elsen))

      merged(retty, Seq(br, thenbr, elsebr))
    }

    def genSwitch(m: Match, focus: Focus): Focus = {
      val Match(scrutp, allcaseps) = m

      val defaultp: Tree = allcaseps.collectFirst {
        case c @ CaseDef(Ident(nme.WILDCARD), _, body) => body
      }.get

      val caseps: Seq[(Val, Tree)] = allcaseps.flatMap {
        case CaseDef(Ident(nme.WILDCARD), _, _) =>
          Seq()
        case CaseDef(pat, guard, body) =>
          assert(guard.isEmpty)
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
          vals.map((_, body))
      }

      val defaultn  = fresh()
      val defaultbr = genExpr(defaultp, Focus.start.withLabel(defaultn))

      val casens    = caseps.map { _ => fresh() }
      val casevals  = caseps.map { case (v, _) => v }
      val caseexprs = caseps.map { case (_, e) => e }
      val casebrs = caseexprs.zip(casens).map {
        case (expr, n) =>
          genExpr(expr, Focus.start.withLabel(n))
      }

      val retty    = genType(m.tpe, box = false)
      val scrut    = genExpr(scrutp, focus)
      val switchbr = scrut.withSwitch(scrut.value, defaultn, casevals, casens)

      merged(retty, switchbr +: defaultbr +: casebrs)
    }

    def genTry(retty: nir.Type,
               expr: Tree,
               catches: List[Tree],
               finallyp: Tree,
               focus: Focus): Focus = {
      val unwindn = fresh()
      val excn    = fresh()
      val normaln = fresh()
      val mergen  = fresh()
      val excv    = Val.Local(fresh(), Rt.Object)
      val mergev  = Val.Local(fresh(), retty)
      val initial = focus.finish(nir.Inst.Jump(nir.Next(normaln)))

      val normal = {
        scoped(curUnwind := Next.Unwind(unwindn)) {
          val start = Focus.start.withLabel(normaln)
          val focus = genExpr(expr, start)
          val jump  = nir.Inst.Jump(nir.Next.Label(mergen, Seq(focus.value)))
          focus.finish(jump)
        }
      }

      val exceptional = {
        val start = Focus.start.withLabel(unwindn, excv).withValue(excv)
        val focus = genCatch(retty, mergen, catches, start)
        val jump  = nir.Inst.Jump(nir.Next.Label(mergen, Seq(focus.value)))
        focus.finish(jump)
      }

      val insts = initial ++ genFinally(finallyp, normal ++ exceptional)

      Focus.start(insts).withLabel(mergen, mergev).withValue(mergev)
    }

    def genCatch(retty: nir.Type,
                 merge: Local,
                 catches: List[Tree],
                 focus: Focus): Focus = {
      val exc   = focus.value
      val cases = catches.map {
        case CaseDef(pat, _, body) =>
          val (excty, symopt) = pat match {
            case Typed(Ident(nme.WILDCARD), tpt) =>
              (genType(tpt.tpe, box = false), None)
            case Ident(nme.WILDCARD) =>
              (genType(ThrowableClass.tpe, box = false), None)
            case Bind(_, _) =>
              (genType(pat.symbol.tpe, box = false), Some(pat.symbol))
          }
          val f = { focus: Focus =>
            val enter = symopt.map { sym =>
              val cast = focus withOp Op.As(excty, exc)
              curMethodEnv.enter(sym, cast.value)
              cast
            }.getOrElse(focus)
            val res = genExpr(body, enter)
            res.withJump(merge, res.value)
          }

          (excty, f)
      }

      def wrap(cases: Seq[(nir.Type, Focus => Focus)], focus: Focus): Focus =
        cases match {
          case Seq() =>
            focus.withThrow(exc, curUnwind)
          case (excty, f) +: rest =>
            val cond = focus withOp Op.Is(excty, exc)
            genIf(retty, ValTree(cond.value),
                  ContTree(f), ContTree(wrap(rest, _)), cond)
        }

      wrap(cases, focus)
    }

    def genFinally(finallyp: Tree, insts: Seq[nir.Inst]): Seq[Inst] = {
      val labels = insts.collect {
        case Inst.Label(n, _) => n
      }.toSet
      def internal(cf: Inst.Cf) = cf match {
        case inst @ Inst.Jump(n) =>
          labels.contains(n.name)
        case inst @ Inst.If(_, n1, n2) =>
          labels.contains(n1.name) && labels.contains(n2.name)
        case inst @ Inst.Switch(_, n, ns) =>
          labels.contains(n.name) && ns.forall(n => labels.contains(n.name))
        case inst @ Inst.Throw(_, n) =>
          (n ne Next.None) && labels.contains(n.name)
        case _ =>
          false
      }
      val extra = new nir.Buffer
      val transformed = insts.map {
        case cf: Inst.Cf if internal(cf) =>
          cf
        case cf: Inst.Cf =>
          val finallyn = fresh()
          val start    = Focus.start.withLabel(finallyn)
          val focus    = genExpr(finallyp, start)
          extra ++= focus.finish(cf)
          Inst.Jump(Next(finallyn))
        case inst =>
          inst
      }

      transformed ++ extra.toSeq
    }

    def genModule(sym: Symbol, focus: Focus): Focus =
      focus withOp Op.Module(genTypeName(sym), curUnwind)

    def genStaticMember(sym: Symbol, focus: Focus): Focus =
      if (sym == BoxedUnit_UNIT) {
        focus withValue Val.Unit
      } else {
        val ty     = genType(sym.tpe, box = false)
        val module = focus withOp Op.Module(genTypeName(sym.owner), curUnwind)
        genMethodCall(sym, statically = true, module.value,
                      Seq(ValTree(module.value)), module)
      }

    def genBlock(block: Block, focus: Focus) = {
      val Block(stats, last) = block

      def isCaseLabelDef(tree: Tree) =
        tree.isInstanceOf[LabelDef] && hasSynthCaseSymbol(tree)

      def translateMatch(last: LabelDef) = {
        val (prologue, cases) = stats.span(s => !isCaseLabelDef(s))
        val labels            = cases.map { case label: LabelDef => label }
        genMatch(prologue, labels :+ last, focus)
      }

      last match {
        case label: LabelDef if isCaseLabelDef(label) =>
          translateMatch(label)

        case Apply(
            TypeApply(Select(label: LabelDef, nme.asInstanceOf_Ob), _), _)
            if isCaseLabelDef(label) =>
          translateMatch(label)

        case _ =>
          val focs      = sequenced(stats, focus)(genExpr(_, _))
          val lastfocus = focs.lastOption.getOrElse(focus)
          genExpr(last, lastfocus)
      }
    }

    def genMatch(prologue: List[Tree], lds: List[LabelDef], focus: Focus) = {
      val prfocus   = sequenced(prologue, focus)(genExpr(_, _))
      val lastfocus = prfocus.lastOption.getOrElse(focus)
      lds.foreach(curMethodEnv.enterLabel)

      val first    = curMethodEnv.resolveLabel(lds.head)
      var resfocus = lastfocus.withJump(first)

      lds.foreach { ld =>
        resfocus = genLabel(ld, resfocus)
      }

      resfocus
    }

    def genLabel(label: LabelDef,
                 focus: Focus,
                 hijackThis: Boolean = false) = {
      val local = curMethodEnv.resolveLabel(label)
      val params = label.params.map { id =>
        val local = Val.Local(fresh(), genType(id.tpe, box = false))
        curMethodEnv.enter(id.symbol, local)
        local
      }
      val entry = focus.withLabel(local, params: _*)
      val newThis = if (hijackThis) Some(params.head) else curMethodThis.get

      scoped(curMethodThis := newThis) {
        genExpr(label.rhs, entry)
      }
    }

    def genArrayValue(av: ArrayValue, focus: Focus): Focus = {
      val ArrayValue(tpt, elems) = av

      val len       = Literal(Constant(elems.length))
      val elemcode  = genPrimCode(tpt.tpe)
      val modulesym = RuntimeArrayModule(elemcode)
      val methsym   = RuntimeArrayAllocMethod(elemcode)
      val alloc     = genModuleMethodCall(modulesym, methsym, Seq(len), focus)
      val init =
        if (elems.isEmpty) alloc
        else {
          val (values, last) = genSimpleArgs(elems, alloc)
          val updates = sequenced(values.zipWithIndex, last) { (vi, focus) =>
            val (v, i) = vi
            val idx    = Literal(Constant(i))

            genMethodCall(RuntimeArrayUpdateMethod(elemcode),
                          statically = true,
                          alloc.value,
                          Seq(idx, ValTree(v)),
                          focus)
          }

          updates.last
        }

      init withValue alloc.value
    }

    def genApplyDynamic(app: ApplyDynamic, focus: Focus) = {
      val ApplyDynamic(obj, args) = app
      val sym = app.symbol

      val params = sym.tpe.params

      val isEqEqOrBangEq = (sym.name == EqEqMethodName || sym.name == NotEqMethodName) &&
        params.size == 1

      // If the method is '=='or '!=' generate class equality instead of dyn-call
      if(isEqEqOrBangEq) {
        val neg = sym.name == nme.ne || sym.name == NotEqMethodName
        val last = genClassEquality(obj, args.head, ref = false, negated = neg, focus)
        last withOp Op.Box(nir.Type.Class(nir.Global.Top("java.lang.Boolean")), last.value)
      } else {
        val self = genExpr(obj, focus)
        genDynMethodCall(sym, self.value, args, self)
      }

    }

    def genDynMethodCall(sym: Symbol, self: Val, argsp: Seq[Tree], focus: Focus): Focus = {

      val methodSig = genMethodSig(sym).asInstanceOf[Type.Function]
      val params    = sym.tpe.params


      def isArrayLikeOp = {
        sym.name == nme.update &&
          params.size == 2 && params.head.tpe.typeSymbol == IntClass
      }

      def genDynCall(arrayUpdate: Boolean, focus: Focus) = {

        // In the case of an array update we need to manually erase the return type.
        val methodName =
          if (arrayUpdate) "update_i32_class.java.lang.Object" else nir.Global.genSignature(genMethodName(sym))

        val sig =
          Type.Function(
            methodSig.args.head ::
              methodSig.args.tail.map(ty => Type.box.getOrElse(ty, ty)).toList,
            nir.Type.Class(nir.Global.Top("java.lang.Object")))


        val callerType    = methodSig.args.head
        val boxedArgTypes = methodSig.args.tail.map(ty => nir.Type.box.getOrElse(ty, ty)).toList

        val retType       = nir.Type.Class(nir.Global.Top("java.lang.Object"))
        val signature     = nir.Type.Function(callerType :: boxedArgTypes, retType)
        val (args, last)  = genMethodArgs(sym, argsp, focus, boxedArgTypes)

        val method = last withOp Op.Dynmethod(self, methodName.toString)
        val values = self +: args

        val call = method withOp Op.Call(signature, method.value, values, curUnwind)
        call withOp Op.As(nir.Type.box.getOrElse(methodSig.ret, methodSig.ret), call.value)
      }

      // If the signature matches an array update, tests at runtime if it really is an array update.
      if(isArrayLikeOp) {
        val last = focus withOp Op.Is(nir.Type.Class(nir.Global.Top("scala.scalanative.runtime.ObjectArray")), self)
        val cond = ValTree(last.value)

        val thenp = ContTree { focus =>
          genDynCall(arrayUpdate = true, focus)
        }
        val elsep = ContTree { focus =>
          genDynCall(arrayUpdate = false, focus)
        }
        genIf(nir.Type.Class(nir.Global.Top("java.lang.Object")), cond, thenp, elsep, last)

      } else {
        genDynCall(arrayUpdate = false, focus)
      }

    }

    def genApply(app: Apply, focus: Focus): Focus = {
      val Apply(fun, args) = app

      fun match {
        case _: TypeApply =>
          genApplyTypeApply(app, focus)
        case Select(Super(_, _), _) =>
          genMethodCall(fun.symbol,
                        statically = true,
                        curMethodThis.get.get,
                        args,
                        focus)
        case Select(New(_), nme.CONSTRUCTOR) =>
          genApplyNew(app, focus)
        case _ =>
          val sym = fun.symbol

          if (sym.isLabel) {
            genApplyLabel(app, focus)
          } else if (scalaPrimitives.isPrimitive(sym)) {
            genPrimitiveOp(app, focus)
          } else if (currentRun.runDefinitions.isBox(sym)) {
            val arg = args.head
            genPrimitiveBox(arg.tpe, arg, focus)
          } else if (currentRun.runDefinitions.isUnbox(sym)) {
            genPrimitiveUnbox(app.tpe, args.head, focus)
          } else {
            val Select(receiverp, _) = fun
            genMethodCall(
                fun.symbol, statically = false, receiverp, args, focus)
          }
      }
    }

    def genApplyLabel(tree: Tree, focus: Focus) = {
      val Apply(fun, argsp)   = tree
      val Val.Local(label, _) = curMethodEnv.resolve(fun.symbol)
      val (args, last)        = genSimpleArgs(argsp, focus)

      last.withJump(label, args: _*)
    }

    def genPrimitiveBox(st: SimpleType, argp: Tree, focus: Focus) = {
      val last = genExpr(argp, focus)
      last withOp Op.Box(genBoxType(st), last.value)
    }

    def genPrimitiveUnbox(st: SimpleType, argp: Tree, focus: Focus) = {
      val last = genExpr(argp, focus)
      last.value.ty match {
        case _: scalanative.nir.Type.I | _: scalanative.nir.Type.F =>
          // No need for unboxing, fixing some slack generated by the general
          // purpose Scala compiler.
          last
        case _ =>
          last withOp Op.Unbox(genBoxType(st), last.value)
      }
    }

    def genPrimitiveOp(app: Apply, focus: Focus): Focus = {
      import scalaPrimitives._

      val Apply(fun @ Select(receiver, _), args) = app

      val sym  = app.symbol
      val code = scalaPrimitives.getPrimitive(sym, receiver.tpe)

      if (isArithmeticOp(code) || isLogicalOp(code) || isComparisonOp(code)) {
        genSimpleOp(app, receiver :: args, code, focus)
      } else if (code == CONCAT) {
        genStringConcat(receiver, args.head, focus)
      } else if (code == HASH) {
        genHashCode(args.head, focus)
      } else if (isArrayOp(code) || code == ARRAY_CLONE) {
        genArrayOp(app, code, focus)
      } else if (nirPrimitives.isPtrOp(code)) {
        genPtrOp(app, code, focus)
      } else if (nirPrimitives.isFunPtrOp(code)) {
        genFunPtrOp(app, code, focus)
      } else if (isCoercion(code)) {
        genCoercion(app, receiver, code, focus)
      } else if (code == SYNCHRONIZED) {
        genSynchronized(app, focus)
      } else if (code == CCAST) {
        genCastOp(app, focus)
      } else if (code == SIZEOF || code == TYPEOF) {
        genOfOp(app, code, focus)
      } else if (code == STACKALLOC) {
        genStackalloc(app, focus)
      } else if (code == CQUOTE) {
        genCQuoteOp(app, focus)
      } else if (code == BOXED_UNIT) {
        focus withValue Val.Unit
      } else if (code >= DIV_UINT && code <= INT_TO_ULONG) {
        genUnsignedOp(app, code, focus)
      } else if (code == SELECT) {
        genSelectOp(app, focus)
      } else {
        abort(
            "Unknown primitive operation: " + sym.fullName + "(" +
            fun.symbol.simpleName + ") " + " at: " + (app.pos))
      }
    }

    lazy val jlClassName     = nir.Global.Top("java.lang.Class")
    lazy val jlClass         = nir.Type.Class(jlClassName)
    lazy val jlClassCtorName = jlClassName member "init_ptr"
    lazy val jlClassCtorSig =
      nir.Type.Function(Seq(jlClass, Type.Ptr), nir.Type.Unit)
    lazy val jlClassCtor = nir.Val.Global(jlClassCtorName, nir.Type.Ptr)

    def genBoxClass(type_ : Val, focus: Focus) = {
      val alloc = focus withOp Op.Classalloc(jlClassName)
      val init =
        alloc withOp Op.Call(
            jlClassCtorSig, jlClassCtor, Seq(alloc.value, type_), curUnwind)

      init withValue alloc.value
    }

    def numOfType(num: Int, ty: nir.Type) = ty match {
      case Type.Byte   | Type.UByte              => Val.Byte(num.toByte)
      case Type.Short  | Type.UShort | Type.Char => Val.Short(num.toShort)
      case Type.Int    | Type.UInt               => Val.Int(num)
      case Type.Long   | Type.ULong              => Val.Long(num.toLong)
      case Type.Float                            => Val.Float(num.toFloat)
      case Type.Double                           => Val.Double(num.toDouble)
      case _                                     => util.unsupported(s"num = $num, ty = ${ty.show}")
    }

    def genSimpleOp(
        app: Apply, args: List[Tree], code: Int, focus: Focus): Focus = {
      val retty = genType(app.tpe, box = false)

      args match {
        case List(right)       => genUnaryOp(code, right, retty, focus)
        case List(left, right) => genBinaryOp(code, left, right, retty, focus)
        case _                 => abort("Too many arguments for primitive function: " + app)
      }
    }

    def negateInt(value: nir.Val, focus: Focus): Focus =
      focus withOp Op.Bin(Bin.Isub, value.ty, numOfType(0, value.ty), value)
    def negateFloat(value: nir.Val, focus: Focus): Focus =
      focus withOp Op.Bin(Bin.Fsub, value.ty, numOfType(0, value.ty), value)
    def negateBits(value: nir.Val, focus: Focus): Focus =
      focus withOp Op.Bin(Bin.Xor, value.ty, numOfType(-1, value.ty), value)
    def negateBool(value: nir.Val, focus: Focus): Focus =
      focus withOp Op.Bin(Bin.Xor, Type.Bool, Val.True, value)

    def genUnaryOp(code: Int, rightp: Tree, opty: nir.Type, focus: Focus) = {
      import scalaPrimitives._

      val right = genExpr(rightp, focus)

      (opty, code) match {
        case (_: Type.I | _: Type.F, POS) => right
        case (_: Type.F, NEG)             => negateFloat(right.value, right)
        case (_: Type.I, NEG)             => negateInt(right.value, right)
        case (_: Type.I, NOT)             => negateBits(right.value, right)
        case (_: Type.I, ZNOT)            => negateBool(right.value, right)
        case _                            => abort("Unknown unary operation code: " + code)
      }
    }

    def genBinaryOp(code: Int,
                    left: Tree,
                    right: Tree,
                    retty: nir.Type,
                    focus: Focus): Focus = {
      import scalaPrimitives._

      val lty  = genType(left.tpe, box = false)
      val rty  = genType(right.tpe, box = false)
      val opty = binaryOperationType(lty, rty)

      val binres = opty match {
        case _: Type.F =>
          code match {
            case ADD =>
              genBinaryOp(Op.Bin(Bin.Fadd, _, _, _), left, right, opty, focus)
            case SUB =>
              genBinaryOp(Op.Bin(Bin.Fsub, _, _, _), left, right, opty, focus)
            case MUL =>
              genBinaryOp(Op.Bin(Bin.Fmul, _, _, _), left, right, opty, focus)
            case DIV =>
              genBinaryOp(Op.Bin(Bin.Fdiv, _, _, _), left, right, opty, focus)
            case MOD =>
              genBinaryOp(Op.Bin(Bin.Frem, _, _, _), left, right, opty, focus)

            case EQ =>
              genBinaryOp(Op.Comp(Comp.Feq, _, _, _), left, right, opty, focus)
            case NE =>
              genBinaryOp(Op.Comp(Comp.Fne, _, _, _), left, right, opty, focus)
            case LT =>
              genBinaryOp(Op.Comp(Comp.Flt, _, _, _), left, right, opty, focus)
            case LE =>
              genBinaryOp(Op.Comp(Comp.Fle, _, _, _), left, right, opty, focus)
            case GT =>
              genBinaryOp(Op.Comp(Comp.Fgt, _, _, _), left, right, opty, focus)
            case GE =>
              genBinaryOp(Op.Comp(Comp.Fge, _, _, _), left, right, opty, focus)

            case _ =>
              abort(
                  "Unknown floating point type binary operation code: " + code)
          }

        case _: Type.I =>
          code match {
            case ADD =>
              genBinaryOp(Op.Bin(Bin.Iadd, _, _, _), left, right, opty, focus)
            case SUB =>
              genBinaryOp(Op.Bin(Bin.Isub, _, _, _), left, right, opty, focus)
            case MUL =>
              genBinaryOp(Op.Bin(Bin.Imul, _, _, _), left, right, opty, focus)
            case DIV =>
              genBinaryOp(Op.Bin(Bin.Sdiv, _, _, _), left, right, opty, focus)
            case MOD =>
              genBinaryOp(Op.Bin(Bin.Srem, _, _, _), left, right, opty, focus)

            case OR =>
              genBinaryOp(Op.Bin(Bin.Or, _, _, _), left, right, opty, focus)
            case XOR =>
              genBinaryOp(Op.Bin(Bin.Xor, _, _, _), left, right, opty, focus)
            case AND =>
              genBinaryOp(Op.Bin(Bin.And, _, _, _), left, right, opty, focus)
            case LSL =>
              genBinaryOp(Op.Bin(Bin.Shl, _, _, _), left, right, opty, focus)
            case LSR =>
              genBinaryOp(Op.Bin(Bin.Lshr, _, _, _), left, right, opty, focus)
            case ASR =>
              genBinaryOp(Op.Bin(Bin.Ashr, _, _, _), left, right, opty, focus)

            case EQ =>
              genBinaryOp(Op.Comp(Comp.Ieq, _, _, _), left, right, opty, focus)
            case NE =>
              genBinaryOp(Op.Comp(Comp.Ine, _, _, _), left, right, opty, focus)
            case LT =>
              genBinaryOp(Op.Comp(Comp.Slt, _, _, _), left, right, opty, focus)
            case LE =>
              genBinaryOp(Op.Comp(Comp.Sle, _, _, _), left, right, opty, focus)
            case GT =>
              genBinaryOp(Op.Comp(Comp.Sgt, _, _, _), left, right, opty, focus)
            case GE =>
              genBinaryOp(Op.Comp(Comp.Sge, _, _, _), left, right, opty, focus)

            case ZOR =>
              genIf(retty, left, Literal(Constant(true)), right, focus)
            case ZAND =>
              genIf(retty, left, right, Literal(Constant(false)), focus)

            case _ =>
              abort("Unknown integer type binary operation code: " + code)
          }

        case _: Type.RefKind =>
          code match {
            case EQ =>
              genClassEquality(
                  left, right, ref = false, negated = false, focus)
            case NE =>
              genClassEquality(left, right, ref = false, negated = true, focus)
            case ID =>
              genClassEquality(left, right, ref = true, negated = false, focus)
            case NI =>
              genClassEquality(left, right, ref = true, negated = true, focus)

            case _ => abort("Unknown reference type operation code: " + code)
          }

        case Type.Ptr =>
          code match {
            case EQ | ID =>
              genBinaryOp(Op.Comp(Comp.Ieq, _, _, _), left, right, opty, focus)
            case NE | NI =>
              genBinaryOp(Op.Comp(Comp.Ine, _, _, _), left, right, opty, focus)
          }

        case ty =>
          abort("Uknown binary operation type: " + ty)
      }

      genCoercion(binres.value, binres.value.ty, retty, binres)
    }

    def genBinaryOp(op: (nir.Type, Val, Val) => Op,
                    leftp: Tree,
                    rightp: Tree,
                    opty: nir.Type,
                    focus: Focus): Focus = {
      val leftty       = genType(leftp.tpe, box = false)
      val left         = genExpr(leftp, focus)
      val leftcoerced  = genCoercion(left.value, leftty, opty, left)
      val rightty      = genType(rightp.tpe, box = false)
      val right        = genExpr(rightp, leftcoerced)
      val rightcoerced = genCoercion(right.value, rightty, opty, right)

      rightcoerced withOp op(opty, leftcoerced.value, rightcoerced.value)
    }

    def genClassEquality(leftp: Tree,
                         rightp: Tree,
                         ref: Boolean,
                         negated: Boolean,
                         focus: Focus) = {
      val left = genExpr(leftp, focus)

      if (ref) {
        val right = genExpr(rightp, left)
        val comp  = if (negated) Comp.Ine else Comp.Ieq
        right withOp Op.Comp(comp, Rt.Object, left.value, right.value)
      } else {
        val isnull = left withOp Op.Comp(Comp.Ieq, Rt.Object, left.value, Val.Zero(Rt.Object))
        val cond = ValTree(isnull.value)
        val thenp = ContTree { focus =>
          val right = genExpr(rightp, focus)
          right withOp Op.Comp(Comp.Ieq, Rt.Object, right.value, Val.Zero(Rt.Object))
        }
        val elsep = ContTree { focus =>
          genMethodCall(NObjectEqualsMethod, statically = false,
                        left.value, Seq(rightp), focus)
        }
        val equals = genIf(Type.Bool, cond, thenp, elsep, isnull)

        if (negated) negateBool(equals.value, equals)
        else equals
      }
    }

    def binaryOperationType(lty: nir.Type, rty: nir.Type) = (lty, rty) match {
      case (nir.Type.Ptr, _: nir.Type.RefKind) =>
        lty
      case (_: nir.Type.RefKind, nir.Type.Ptr) =>
        rty
      case (nir.Type.Bool, nir.Type.Bool) =>
        nir.Type.Bool
      case (nir.Type.I(lwidth, _), nir.Type.I(rwidth, _)) if lwidth < 32 && rwidth < 32 =>
        nir.Type.Int
      case (nir.Type.I(lwidth, _), nir.Type.I(rwidth, _)) =>
        if (lwidth >= rwidth) lty else rty
      case (nir.Type.I(_, _), nir.Type.F(_)) =>
        rty
      case (nir.Type.F(_), nir.Type.I(_, _)) =>
        lty
      case (nir.Type.F(lwidth), nir.Type.F(rwidth)) =>
        if (lwidth >= rwidth) lty else rty
      case (_: nir.Type.RefKind, _: nir.Type.RefKind) =>
        Rt.Object
      case (ty1, ty2) if ty1 == ty2 =>
        ty1
      case _ =>
        abort(s"can't perform binary operation between $lty and $rty")
    }

    def genStringConcat(leftp: Tree, rightp: Tree, focus: Focus): Focus = {
      def stringify(sym: Symbol, focus: Focus) =
        if (sym == StringClass) {
          focus
        } else {
          genMethodCall(Object_toString, statically = false, focus.value, Seq(), focus)
        }

      val left = {
        val typesym = leftp.tpe.typeSymbol
        val unboxed = genExpr(leftp, focus)
        val boxed   = boxValue(typesym, unboxed)
        stringify(typesym, boxed)
      }

      val right = {
        val typesym = rightp.tpe.typeSymbol
        val boxed   = genExpr(rightp, left)
        stringify(typesym, boxed)
      }

      genMethodCall(String_+,
                    statically = true,
                    left.value,
                    Seq(ValTree(right.value)),
                    right)
    }

    def genHashCode(argp: Tree, focus: Focus) = {
      val meth = NObjectHashCodeMethod
      val arg  = boxValue(argp.tpe, genExpr(argp, focus))
      genMethodCall(meth, statically = false, arg.value, Seq(), arg)
    }

    def genArrayOp(app: Apply, code: Int, focus: Focus): Focus = {
      import scalaPrimitives._

      val Apply(Select(arrayp, _), argsp) = app

      val array = genExpr(arrayp, focus)
      def elemcode = genArrayCode(arrayp.tpe)
      val method =
        if (code == ARRAY_CLONE) RuntimeArrayCloneMethod(elemcode)
        else if (scalaPrimitives.isArrayGet(code))
          RuntimeArrayApplyMethod(elemcode)
        else if (scalaPrimitives.isArraySet(code))
          RuntimeArrayUpdateMethod(elemcode)
        else RuntimeArrayLengthMethod(elemcode)

      genMethodCall(method, statically = true, array.value, argsp, array)
    }

    def unwrapClassTagOption(tree: Tree): Option[Symbol] =
      tree match {
        case Typed(Apply(ref: RefTree, args), _) =>
          ref.symbol match {
            case ByteClassTag    => Some(ByteClass)
            case ShortClassTag   => Some(ShortClass)
            case CharClassTag    => Some(CharClass)
            case IntClassTag     => Some(IntClass)
            case LongClassTag    => Some(LongClass)
            case FloatClassTag   => Some(FloatClass)
            case DoubleClassTag  => Some(DoubleClass)
            case BooleanClassTag => Some(BooleanClass)
            case UnitClassTag    => Some(UnitClass)
            case AnyClassTag     => Some(AnyClass)
            case ObjectClassTag  => Some(ObjectClass)
            case AnyValClassTag  => Some(ObjectClass)
            case AnyRefClassTag  => Some(ObjectClass)
            case NothingClassTag => Some(NothingClass)
            case NullClassTag    => Some(NullClass)
            case ClassTagApply =>
              val Seq(Literal(const: Constant)) = args
              Some(const.typeValue.typeSymbol)
            case _ =>
              None
          }

        case tree =>
          None
      }

    def unwrapTagOption(tree: Tree): Option[SimpleType] = {
      tree match {
        case Apply(ref: RefTree, args) =>
          def allsts = {
            val sts = args.flatMap(unwrapTagOption(_).toSeq)
            if (sts.length == args.length) Some(sts) else None
          }
          def just(sym: Symbol) = Some(SimpleType(sym))
          def wrap(sym: Symbol) = allsts.map(SimpleType(sym, _))

          ref.symbol match {
            case UnitTagMethod    => just(UnitClass)
            case BooleanTagMethod => just(BooleanClass)
            case CharTagMethod    => just(CharClass)
            case ByteTagMethod    => just(ByteClass)
            case UByteTagMethod   => just(UByteClass)
            case ShortTagMethod   => just(ShortClass)
            case UShortTagMethod  => just(UShortClass)
            case IntTagMethod     => just(IntClass)
            case UIntTagMethod    => just(UIntClass)
            case LongTagMethod    => just(LongClass)
            case ULongTagMethod   => just(ULongClass)
            case FloatTagMethod   => just(FloatClass)
            case DoubleTagMethod  => just(DoubleClass)
            case PtrTagMethod     => just(PtrClass)
            case RefTagMethod     => just(unwrapClassTagOption(args.head).get)
            case sym if NatBaseTagMethod.contains(sym) =>
              just(NatBaseClass(NatBaseTagMethod.indexOf(sym)))
            case NatDigitTagMethod =>
              wrap(NatDigitClass)
            case CArrayTagMethod =>
              wrap(CArrayClass)
            case sym if CStructTagMethod.contains(sym) =>
              wrap(CStructClass(args.length))
            case _ =>
              None
          }

        case tree =>
          None
      }
    }

    def unwrapTag(tree: Tree): SimpleType =
      unwrapTagOption(tree).getOrElse {
        unsupported(s"can't recover runtime tag from $tree")
      }

    def boxValue(st: SimpleType, focus: Focus): Focus =
      if (genPrimCode(st.sym) == 'O') {
        focus
      } else {
        genPrimitiveBox(st, ValTree(focus.value), focus)
      }

    def unboxValue(st: SimpleType, partial: Boolean, focus: Focus): Focus = {
      val code = genPrimCode(st)

      code match {
        // Results of asInstanceOfs are partially unboxed, meaning
        // that non-standard value types remain to be boxed.
        case _ if partial && 'a' <= code && code <= 'z' =>
          focus
        case 'O' =>
          focus
        case _ =>
          genPrimitiveUnbox(st, ValTree(focus.value), focus)
      }
    }

    def genPtrOp(app: Apply, code: Int, focus: Focus): Focus = {
      val Apply(sel @ Select(ptrp, _), argsp) = app

      val ptr = genExpr(ptrp, focus)

      (code, argsp) match {
        case (PTR_LOAD, Seq(tagp)) =>
          val st = unwrapTag(tagp)
          val ty = genType(st, box = false)
          boxValue(st, ptr withOp Op.Load(ty, ptr.value))

        case (PTR_STORE, Seq(valuep, tagp)) =>
          val st    = unwrapTag(tagp)
          val ty    = genType(st, box = false)
          val value = unboxValue(st, partial = false, genExpr(valuep, ptr))
          value withOp Op.Store(ty, ptr.value, value.value)

        case (PTR_ADD, Seq(offsetp, tagp)) =>
          val st     = unwrapTag(tagp)
          val ty     = genType(st, box = false)
          val offset = genExpr(offsetp, ptr)
          offset withOp Op.Elem(ty, ptr.value, Seq(offset.value))

        case (PTR_SUB, Seq(offsetp, tagp)) =>
          val st     = unwrapTag(tagp)
          val ty     = genType(st, box = false)
          val offset = genExpr(offsetp, ptr)
          val neg    = negateInt(offset.value, offset)
          neg withOp Op.Elem(ty, ptr.value, Seq(neg.value))

        case (PTR_APPLY, Seq(offsetp, tagp)) =>
          val st     = unwrapTag(tagp)
          val ty     = genType(st, box = false)
          val offset = genExpr(offsetp, ptr)
          val elem   = offset withOp Op.Elem(ty, ptr.value, Seq(offset.value))
          boxValue(st, elem withOp Op.Load(ty, elem.value))

        case (PTR_UPDATE, Seq(offsetp, valuep, tagp)) =>
          val st     = unwrapTag(tagp)
          val ty     = genType(st, box = false)
          val offset = genExpr(offsetp, ptr)
          val value  = unboxValue(st, partial = false, genExpr(valuep, offset))
          val elem   = value withOp Op.Elem(ty, ptr.value, Seq(offset.value))
          elem withOp Op.Store(ty, elem.value, value.value)

        case (PTR_FIELD, Seq(tagp, _)) =>
          val st    = unwrapTag(tagp)
          val ty    = genType(st, box = false)
          val index = PtrFieldMethod.indexOf(sel.symbol)
          val path  = Seq(nir.Val.Int(0), nir.Val.Int(index))
          ptr withOp Op.Elem(ty, ptr.value, path)
      }
    }

    def genFunPtrOp(app: Apply, code: Int, focus: Focus): Focus = {
      code match {
        case FUN_PTR_CALL =>
          val Apply(sel @ Select(funp, _), allargsp) = app

          val arity = CFunctionPtrApply.indexOf(sel.symbol)

          val (argsp, tagsp) = allargsp.splitAt(arity)

          val fun    = genExpr(funp, focus)
          val tagsts = tagsp.map(unwrapTag)
          val tagtys = tagsts.map(genType(_, box = false))
          val sig    = Type.Function(tagtys.init, tagtys.last)

          val args = mutable.UnrolledBuffer.empty[nir.Val]
          var last = fun
          tagsts.init.zip(argsp).foreach {
            case (st, argp) =>
              last = unboxValue(st, partial = false, genExpr(argp, last))
              args += last.value
          }

          last withOp Op.Call(sig, fun.value, args, curUnwind)

        case FUN_PTR_FROM =>
          val Apply(_, Seq(MaybeBlock(Typed(ctor, funtpt))))           = app
          val Apply(Select(New(tpt), termNames.CONSTRUCTOR), ctorargs) = ctor

          assert(ctorargs.isEmpty,
                 s"Can't get function pointer to a closure with captures: ${ctorargs} in application ${app}")

          val anondef = consumeLazyAnonDef(tpt.tpe.typeSymbol)
          val body    = anondef.impl.body
          val apply = body.collectFirst {
            case dd: DefDef if dd.symbol.hasFlag(SPECIALIZED) =>
              dd
          }.getOrElse {
            body.collectFirst {
              case dd: DefDef
                  if dd.name == nme.apply && !dd.symbol.hasFlag(BRIDGE) =>
                dd
            }.get
          }

          apply match {
            case ExternForwarder(tosym) =>
              focus withValue Val.Global(genMethodName(tosym), Type.Ptr)

            case _ =>
              val attrs  = Attrs(isExtern = true)
              val name   = genAnonName(curClassSym, anondef.symbol)
              val sig    = genMethodSig(apply.symbol, forceStatic = true)
              val params = genParams(curClassSym, apply, isStatic = true)
              val body = genNormalMethodBody(
                  params, apply.rhs, isStatic = true)

              curClassDefns += Defn.Define(attrs, name, sig, body)

              focus withValue Val.Global(name, Type.Ptr)
          }

        case _ =>
          unreachable
      }
    }

    def castConv(fromty: nir.Type, toty: nir.Type): Option[nir.Conv] =
      (fromty, toty) match {
        case (_: Type.I, Type.Ptr)                => Some(nir.Conv.Inttoptr)
        case (Type.Ptr, _: Type.I)                => Some(nir.Conv.Ptrtoint)
        case (_: Type.RefKind, Type.Ptr)          => Some(nir.Conv.Bitcast)
        case (Type.Ptr, _: Type.RefKind)          => Some(nir.Conv.Bitcast)
        case (_: Type.RefKind, _: Type.I)         => Some(nir.Conv.Ptrtoint)
        case (_: Type.I, _: Type.RefKind)         => Some(nir.Conv.Inttoptr)
        case (Type.I(w1, _), Type.F(w2)) if w1 == w2 => Some(nir.Conv.Bitcast)
        case (Type.F(w1), Type.I(w2, _)) if w1 == w2 => Some(nir.Conv.Bitcast)
        case _ if fromty == toty                  => None
        case _ =>
          unsupported(s"cast from $fromty to $toty")
      }

    def genCastOp(app: Apply, focus: Focus): Focus = {
      val Apply(Select(Apply(_, List(valuep)), _), List(fromtagp, totagp)) = app

      val fromst = unwrapTag(fromtagp)
      val tost   = unwrapTag(totagp)
      val fromty = genType(fromst, box = false)
      val toty   = genType(tost, box = false)
      val from   = unboxValue(fromst, partial = false, genExpr(valuep, focus))

      boxValue(tost, castConv(fromty, toty).fold(from) { conv =>
        from withOp Op.Conv(conv, toty, from.value)
      })
    }

    def genOfOp(app: Apply, code: Int, focus: Focus): Focus = {
      val Apply(_, Seq(tagp)) = app

      val st = unwrapTag(tagp)

      code match {
        case SIZEOF => focus withOp Op.Sizeof(genType(st, box = false))
        case TYPEOF => focus withValue genTypeValue(st)
        case _      => unreachable
      }
    }

    def genStackalloc(app: Apply, focus: Focus): Focus = {
      val (sizeopt, tagp) = app match {
        case Apply(_, Seq(tagp)) =>
          (None, tagp)
        case Apply(_, Seq(sizep, tagp)) =>
          (Some(sizep), tagp)
      }
      val ty   = genType(unwrapTag(tagp), box = false)
      val size = sizeopt.fold(focus withValue Val.None)(genExpr(_, focus))

      size withOp Op.Stackalloc(ty, size.value)
    }

    def genCQuoteOp(app: Apply, focus: Focus): Focus =
      app match {
        // Sometimes I really miss quasiquotes.
        //
        // case q"""
        //   scala.scalanative.native.`package`.CQuote(
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
            Apply(_,
                  List(
                  Apply(_,
                        List(
                        Apply(TypeApply(
                              Select(ArrayValue(
                                     _, List(Literal(Constant(str: String)))),
                                     _),
                              _),
                              _))))))),
            _),
            _) =>
          focus withValue Val.Const(
              Val.Chars(str.replace("\\n", "\n").replace("\\r", "\r")))

        case _ =>
          unsupported(app)
      }

    def genUnsignedOp(app: Tree, code: Int, focus: Focus): Focus =
      app match {
        case Apply(_, Seq(argp)) =>
          assert(code >= BYTE_TO_UINT && code <= INT_TO_ULONG)

          val ty  = genType(app.tpe, box = false)
          val arg = genExpr(argp, focus)

          arg withOp Op.Conv(Conv.Zext, ty, arg.value)

        case Apply(_, Seq(leftp, rightp)) =>
          val bin = code match {
            case DIV_UINT | DIV_ULONG => nir.Bin.Udiv
            case REM_UINT | REM_ULONG => nir.Bin.Urem
          }
          val ty    = genType(leftp.tpe, box = false)
          val left  = genExpr(leftp, focus)
          val right = genExpr(rightp, left)

          right withOp Op.Bin(bin, ty, left.value, right.value)
      }


    def genSelectOp(app: Tree, focus: Focus): Focus = {
      val Apply(_, Seq(condp, thenp, elsep, tagp)) = app

      val st    = unwrapTag(tagp)
      val cond  = genExpr(condp, focus)
      val then_ = unboxValue(st, partial = false, genExpr(thenp, cond))
      val else_ = unboxValue(st, partial = false, genExpr(elsep, then_))
      val sel   = else_ withOp Op.Select(cond.value, then_.value, else_.value)

      boxValue(st, sel)
    }


    def genSynchronized(app: Apply, focus: Focus): Focus = {
      val Apply(Select(receiverp, _), List(argp)) = app

      val monitor = genModuleMethodCall(RuntimeModule,
                                        GetMonitorMethod,
                                        Seq(receiverp),
                                        focus)
      val enter = genMethodCall(RuntimeMonitorEnterMethod,
                                statically = true,
                                monitor.value,
                                Seq(),
                                monitor)
      val arg = genExpr(argp, enter)
      val exit = genMethodCall(RuntimeMonitorExitMethod,
                               statically = true,
                               monitor.value,
                               Seq(),
                               arg)

      exit withValue arg.value
    }

    def genCoercion(
        app: Apply, receiver: Tree, code: Int, focus: Focus): Focus = {
      val rec            = genExpr(receiver, focus)
      val (fromty, toty) = coercionTypes(code)

      genCoercion(rec.value, fromty, toty, rec)
    }

    def genCoercion(
        value: Val, fromty: nir.Type, toty: nir.Type, focus: Focus): Focus = {
      if (fromty == toty) {
        focus withValue value
      } else {
        val conv = (fromty, toty) match {
          case (nir.Type.Ptr, _: nir.Type.RefKind) =>
            Conv.Bitcast
          case (_: nir.Type.RefKind, nir.Type.Ptr) =>
            Conv.Bitcast
          case (nir.Type.I(fromw, froms), nir.Type.I(tow, tos)) =>
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
          case (nir.Type.I(_, true), _: nir.Type.F) =>
            Conv.Sitofp
          case (nir.Type.I(_, false), _: nir.Type.F) =>
            Conv.Uitofp
          case (_: nir.Type.F, nir.Type.I(_, true)) =>
            Conv.Fptosi
          case (_: nir.Type.F, nir.Type.I(_, false)) =>
            Conv.Fptoui
          case (nir.Type.Double, nir.Type.Float) =>
            Conv.Fptrunc
          case (nir.Type.Float, nir.Type.Double) =>
            Conv.Fpext
        }
        focus withOp Op.Conv(conv, toty, value)
      }
    }

    def coercionTypes(code: Int) = {
      import scalaPrimitives._

      code match {
        case B2B => (nir.Type.Byte, nir.Type.Byte  )
        case B2S => (nir.Type.Byte, nir.Type.Short )
        case B2C => (nir.Type.Byte, nir.Type.Char  )
        case B2I => (nir.Type.Byte, nir.Type.Int   )
        case B2L => (nir.Type.Byte, nir.Type.Long  )
        case B2F => (nir.Type.Byte, nir.Type.Float )
        case B2D => (nir.Type.Byte, nir.Type.Double)

        case S2B => (nir.Type.Short, nir.Type.Byte  )
        case S2S => (nir.Type.Short, nir.Type.Short )
        case S2C => (nir.Type.Short, nir.Type.Char  )
        case S2I => (nir.Type.Short, nir.Type.Int   )
        case S2L => (nir.Type.Short, nir.Type.Long  )
        case S2F => (nir.Type.Short, nir.Type.Float )
        case S2D => (nir.Type.Short, nir.Type.Double)

        case C2B => (nir.Type.Char, nir.Type.Byte  )
        case C2S => (nir.Type.Char, nir.Type.Short )
        case C2C => (nir.Type.Char, nir.Type.Char  )
        case C2I => (nir.Type.Char, nir.Type.Int   )
        case C2L => (nir.Type.Char, nir.Type.Long  )
        case C2F => (nir.Type.Char, nir.Type.Float )
        case C2D => (nir.Type.Char, nir.Type.Double)

        case I2B => (nir.Type.Int, nir.Type.Byte  )
        case I2S => (nir.Type.Int, nir.Type.Short )
        case I2C => (nir.Type.Int, nir.Type.Char  )
        case I2I => (nir.Type.Int, nir.Type.Int   )
        case I2L => (nir.Type.Int, nir.Type.Long  )
        case I2F => (nir.Type.Int, nir.Type.Float )
        case I2D => (nir.Type.Int, nir.Type.Double)

        case L2B => (nir.Type.Long, nir.Type.Byte  )
        case L2S => (nir.Type.Long, nir.Type.Short )
        case L2C => (nir.Type.Long, nir.Type.Char  )
        case L2I => (nir.Type.Long, nir.Type.Int   )
        case L2L => (nir.Type.Long, nir.Type.Long  )
        case L2F => (nir.Type.Long, nir.Type.Float )
        case L2D => (nir.Type.Long, nir.Type.Double)

        case F2B => (nir.Type.Float, nir.Type.Byte  )
        case F2S => (nir.Type.Float, nir.Type.Short )
        case F2C => (nir.Type.Float, nir.Type.Char  )
        case F2I => (nir.Type.Float, nir.Type.Int   )
        case F2L => (nir.Type.Float, nir.Type.Long  )
        case F2F => (nir.Type.Float, nir.Type.Float )
        case F2D => (nir.Type.Float, nir.Type.Double)

        case D2B => (nir.Type.Double, nir.Type.Byte  )
        case D2S => (nir.Type.Double, nir.Type.Short )
        case D2C => (nir.Type.Double, nir.Type.Char  )
        case D2I => (nir.Type.Double, nir.Type.Int   )
        case D2L => (nir.Type.Double, nir.Type.Long  )
        case D2F => (nir.Type.Double, nir.Type.Float )
        case D2D => (nir.Type.Double, nir.Type.Double)
      }
    }

    def genApplyTypeApply(app: Apply, focus: Focus) = {
      val Apply(TypeApply(fun @ Select(receiverp, _), targs), _) = app

      val ty    = genType(targs.head.tpe, box = true)
      val box   = boxValue(receiverp.tpe, genExpr(receiverp, focus))

      fun.symbol match {
        case Object_isInstanceOf =>
          box withOp Op.Is(ty, box.value)

        case Object_asInstanceOf =>
          if (box.value.ty == ty) {
            box
          } else {
            unboxValue(app.tpe, partial = true, box withOp Op.As(ty, box.value))
          }
      }
    }

    def genApplyNew(app: Apply, focus: Focus) = {
      val Apply(fun @ Select(New(tpt), nme.CONSTRUCTOR), args) = app

      SimpleType.fromType(tpt.tpe) match {
        case SimpleType(ArrayClass, Seq(targ)) =>
          genNewArray(genPrimCode(targ), args, focus)

        case st if st.isStruct =>
          genNewStruct(st, args, focus)

        case st @ SimpleType(UByteClass | UIntClass | UShortClass | ULongClass, Seq())
          // We can't just compare the curClassSym with RuntimeBoxesModule
          // as it's not the same when you're actually compiling Boxes module.
          if curClassSym.fullName.toString != "scala.scalanative.runtime.Boxes" =>
          genPrimitiveBox(st, args.head, focus)

        case SimpleType(cls, Seq()) =>
          genNew(cls, fun.symbol, args, focus)

        case SimpleType(sym, targs) =>
          unsupported(s"unexpected new: $sym with targs $targs")
      }
    }

    def genNewStruct(st: SimpleType, argsp: Seq[Tree], focus: Focus): Focus = {
      val ty           = genType(st, box = false)
      val (args, last) = genSimpleArgs(argsp, focus)
      val undef        = last withValue Val.Undef(ty)

      args.zipWithIndex.foldLeft(undef) {
        case (focus, (value, index)) =>
          focus withOp Op.Insert(focus.value, value, Seq(index))
      }
    }

    def genNewArray(elemcode: Char, argsp: Seq[Tree], focus: Focus) = {
      val module = RuntimeArrayModule(elemcode)
      val meth   = RuntimeArrayAllocMethod(elemcode)

      genModuleMethodCall(module, meth, argsp, focus)
    }

    def genNew(
        clssym: Symbol, ctorsym: Symbol, args: List[Tree], focus: Focus) = {
      val alloc = focus.withOp(Op.Classalloc(genTypeName(clssym)))
      val call = genMethodCall(
          ctorsym, statically = true, alloc.value, args, alloc)

      call withValue alloc.value
    }

    def genExternAccessor(sym: Symbol, argsp: Seq[Tree], focus: Focus) =
      argsp match {
        case Seq() =>
          val ty   = genMethodSig(sym).ret
          val name = genMethodName(sym)
          val elem = focus withValue Val.Global(name, Type.Ptr)
          elem withOp Op.Load(ty, elem.value)

        case Seq(value) =>
          unsupported(argsp)
      }

    def genModuleMethodCall(module: Symbol,
                            method: Symbol,
                            args: Seq[Tree],
                            focus: Focus): Focus = {
      val self = genModule(module, focus)

      genMethodCall(method, statically = true, self.value, args, self)
    }

    def genMethodCall(sym: Symbol,
                      statically: Boolean,
                      selfp: Tree,
                      argsp: Seq[Tree],
                      focus: Focus): Focus = {
      if (sym.owner.isExternModule && sym.hasFlag(ACCESSOR)) {
        genExternAccessor(sym, argsp, focus)
      } else {
        val self = genExpr(selfp, focus)

        genMethodCall(sym, statically, self.value, argsp, self)
      }
    }

    def genMethodCall(sym: Symbol,
                      statically: Boolean,
                      self: Val,
                      argsp: Seq[Tree],
                      focus: Focus): Focus = {
      val owner        = sym.owner
      val name         = genMethodName(sym)
      val sig          = genMethodSig(sym)
      val argsPt =
        if (owner.isExternModule || owner.isImplClass)
          sig.args
        else
          sig.args.tail

      val (args, last) = genMethodArgs(sym, argsp, focus, argsPt)

      val method =
        if (statically || owner.isStruct || owner.isExternModule) {
          last withValue Val.Global(name, nir.Type.Ptr)
        } else {
          last withOp Op.Method(self, name)
        }
      val values =
        if (owner.isExternModule || owner.isImplClass)
          args
        else
          self +: args

      method withOp Op.Call(sig, method.value, values, curUnwind)
    }

    def genSimpleArgs(argsp: Seq[Tree], focus: Focus): (Seq[Val], Focus) =
      genSimpleArgsWithPt(argsp, focus, argsp.map(_ => None))

    def genSimpleArgsWithPt(argsp: Seq[Tree], focus: Focus, argspPt: Seq[Option[scalanative.nir.Type]]): (Seq[Val], Focus) = {
      val args1     = sequenced(argsp, focus)(genExpr(_, _))
      val argvalues = args1.map(_.value)

      // Under certain circumstances e.g., the result of the c-function pointer dereference
      // being used in a position where an object reference is objected, nir code generator
      // leaves a primitive value, since the Scala compiler didn't box it. Such applications
      // have to be augmented depending on the expected type of the function.
      def inferImplicitArgConv(x: (Focus, Option[scalanative.nir.Type]), f: Focus) = x match {
        case (argFocus, None)        => argFocus
        case (argFocus, Some(argPt)) =>
          (argFocus.value.ty, argPt) match {
            case (argFocusTpe: scalanative.nir.Type.I, _: scalanative.nir.Type.RefKind) =>
              scalanative.nir.Type.box.get(argFocusTpe).map { boxedTpe =>
                argFocus withOp Op.Box(boxedTpe, argFocus.value)
              } getOrElse(argFocus)
            case (argFocusTpe: scalanative.nir.Type.F, _: scalanative.nir.Type.RefKind) =>
              scalanative.nir.Type.box.get(argFocusTpe).map { boxedTpe =>
                argFocus withOp Op.Box(boxedTpe, argFocus.value)
              } getOrElse(argFocus)
            case _ =>
              argFocus
          }

      }

      val args2       = sequenced(args1.zip(argspPt), focus)(inferImplicitArgConv(_, _))
      val argsValues2 = args2.map(_.value)
      val last        = args2.lastOption.getOrElse(focus)

      (argsValues2, last)
    }

    def genMethodArgs(
        sym: Symbol, argsp: Seq[Tree], focus: Focus, argsPt: Seq[nir.Type]): (Seq[Val], Focus) =
      if (!sym.owner.isExternModule) {
        genSimpleArgsWithPt(argsp, focus, argsPt.map(Some(_)))
      } else {
        val wereRepeated = exitingPhase(currentRun.typerPhase) {
          for {
            params <- sym.tpe.paramss
            param  <- params
          } yield {
            param.name -> isScalaRepeatedParamType(param.tpe)
          }
        }.toMap

        val args = mutable.UnrolledBuffer.empty[Val]
        var curfocus = focus

        for ((argp, paramSym) <- argsp zip sym.tpe.params) {
          val wasRepeated = wereRepeated.getOrElse(paramSym.name, false)
          if (wasRepeated) {
            val (vals, newfocus) = genExpandRepeatedArg(argp, curfocus).get
            curfocus = newfocus
            args ++= vals
          } else {
            curfocus = genExpr(argp, curfocus)
            args += curfocus.value
          }
        }

        (args, curfocus)
      }

    def genExpandRepeatedArg(
        argp: Tree, focus: Focus): Option[(Seq[Val], Focus)] = {
      // Given an extern method `def foo(args: Vararg*)`
      argp match {
        // foo(vararg1, ..., varargN) where N > 0
        case MaybeAsInstanceOf(
            WrapArray(MaybeAsInstanceOf(ArrayValue(tpt, elems)))) =>
          val values = mutable.UnrolledBuffer.empty[Val]
          val resfocus = elems.foldLeft(focus) {
            case (focus, CVararg(st, argp)) =>
              val arg = unboxValue(st, partial = false, genExpr(argp, focus))
              values += arg.value
              arg
          }
          Some((values, resfocus))

        // foo(argSeq:_*) - cannot be optimized
        case _ =>
          None
      }
    }

    object CVararg {
      def unapply(tree: Tree): Option[(SimpleType, Tree)] = tree match {
        case Apply(fun, Seq(argp, tagp)) if fun.symbol == CVarargMethod =>
          val st = unwrapTag(tagp)
          Some((st, argp))
        case _ =>
          None
      }
    }

    object MaybeAsInstanceOf {
      def unapply(tree: Tree): Some[Tree] = tree match {
        case Apply(TypeApply(asInstanceOf_? @ Select(base, _), _), _)
            if asInstanceOf_?.symbol == Object_asInstanceOf =>
          Some(base)
        case _ =>
          Some(tree)
      }
    }

    object MaybeBlock {
      def unapply(tree: Tree): Some[Tree] = tree match {
        case Block(Seq(), MaybeBlock(expr)) => Some(expr)
        case _                              => Some(tree)
      }
    }

    object WrapArray {
      lazy val isWrapArray: Set[Symbol] =
        Seq(nme.wrapRefArray,
            nme.wrapByteArray,
            nme.wrapShortArray,
            nme.wrapCharArray,
            nme.wrapIntArray,
            nme.wrapLongArray,
            nme.wrapFloatArray,
            nme.wrapDoubleArray,
            nme.wrapBooleanArray,
            nme.wrapUnitArray,
            nme.genericWrapArray).map(getMemberMethod(PredefModule, _)).toSet

      def unapply(tree: Apply): Option[Tree] = tree match {
        case Apply(wrapArray_?, List(wrapped))
            if isWrapArray(wrapArray_?.symbol) =>
          Some(wrapped)
        case _ =>
          None
      }
    }

    object ExternForwarder {
      // format: OFF
      def unapply(tree: Tree): Option[Symbol] = tree match {
        case DefDef(_, _, _, Seq(params), _, Apply(sel @ Select(from, _), args))
            if from.symbol != null
            && from.symbol.isExternModule
            && params.length == args.length
            && params.zip(args).forall {
                 case (param, arg: RefTree) => param.symbol == arg.symbol
                 case _                     => false
               } =>
          Some(sel.symbol)
        case _ =>
          None
      }
      // format: ON
    }
  }
}

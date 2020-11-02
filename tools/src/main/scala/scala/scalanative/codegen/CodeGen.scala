package scala.scalanative
package codegen

import java.{lang => jl}
import java.nio.file.Paths
import scala.collection.mutable
import scala.scalanative.util.ShowBuilder.FileShowBuilder
import scalanative.util.{Scope, ShowBuilder, partitionBy, procs, unsupported}
import scalanative.io.VirtualDirectory
import scalanative.nir._
import scalanative.nir.ControlFlow.{Block, Edge, Graph => CFG}
import scalanative.util.unreachable
import scalanative.build.ScalaNative.dumpDefns

object CodeGen {

  /** Lower and generate code for given assembly. */
  def apply(config: build.Config, linked: linker.Result): Unit = {
    val defns   = linked.defns
    val proxies = GenerateReflectiveProxies(linked.dynimpls, defns)

    implicit val meta = new Metadata(linked, proxies)

    val generated = Generate(Global.Top(config.mainClass), defns ++ proxies)
    val lowered   = lower(generated)
    dumpDefns(config, "lowered", lowered)
    emit(config, lowered)
  }

  private def lower(defns: Seq[Defn])(implicit meta: Metadata): Seq[Defn] = {
    val buf = mutable.UnrolledBuffer.empty[Defn]

    partitionBy(defns)(_.name).par
      .map {
        case (_, defns) =>
          Lower(defns)
      }
      .seq
      .foreach { defns => buf ++= defns }

    buf
  }

  /** Generate code for given assembly. */
  private def emit(config: build.Config, assembly: Seq[Defn])(
      implicit meta: Metadata): Unit =
    Scope { implicit in =>
      val env     = assembly.map(defn => defn.name -> defn).toMap
      val workdir = VirtualDirectory.real(config.workdir)

      // Partition into multiple LLVM IR files proportional to number
      // of available processesors. This prevents LLVM from optimizing
      // across IR module boundary unless LTO is turned on.
      def separate(): Unit =
        partitionBy(assembly, procs)(_.name.top.mangle).par.foreach {
          case (id, defns) =>
            val sorted = defns.sortBy(_.name.show)
            new Impl(config.targetTriple, env, sorted)
              .gen(id.toString, workdir)
        }

      // Generate a single LLVM IR file for the whole application.
      // This is an adhoc form of LTO. We use it in release mode if
      // Clang's LTO is not available.
      def single(): Unit = {
        val sorted = assembly.sortBy(_.name.show)
        new Impl(config.targetTriple, env, sorted)
          .gen(id = "out", workdir)
      }

      (config.mode, config.LTO) match {
        case (build.Mode.Debug, _)                   => separate()
        case (_: build.Mode.Release, build.LTO.None) => single()
        case (_: build.Mode.Release, _)              => separate()
      }
    }

  private final class Impl(targetTriple: String,
                           env: Map[Global, Defn],
                           defns: Seq[Defn])(implicit meta: Metadata) {
    import Impl._

    private var currentBlockName: Local = _
    private var currentBlockSplit: Int  = _

    private val copies           = mutable.Map.empty[Local, Val]
    private val deps             = mutable.Set.empty[Global]
    private val generated        = mutable.Set.empty[String]
    private val externSigMembers = mutable.Map.empty[Sig, Global.Member]

    def gen(id: String, dir: VirtualDirectory): Unit = {
      val body = dir.write(Paths.get(s"$id-body.ll")) { writer =>
        genDefns(defns)(new FileShowBuilder(writer))
      }

      val headers = dir.write(Paths.get(s"$id.ll")) { writer =>
        implicit val sb: ShowBuilder = new FileShowBuilder(writer)
        genPrelude()
        genConsts()
        genDeps()
      }

      dir.merge(Seq(body), headers)
    }

    def genDeps()(implicit sb: ShowBuilder): Unit = deps.foreach { n =>
      val mn = mangled(n)
      if (!generated.contains(mn)) {
        sb.newline()
        genDefn {
          val defn             = env(n)
          implicit val rootPos = defn.pos
          defn match {
            case defn @ Defn.Var(attrs, _, _, _) =>
              defn.copy(attrs.copy(isExtern = true))
            case defn @ Defn.Const(attrs, _, ty, _) =>
              defn.copy(attrs.copy(isExtern = true))
            case defn @ Defn.Declare(attrs, _, _) =>
              defn.copy(attrs.copy(isExtern = true))
            case defn @ Defn.Define(attrs, name, ty, _) =>
              Defn.Declare(attrs, name, ty)
            case _ =>
              unreachable
          }
        }
        generated += mn
      }
    }

    def touch(n: Global): Unit =
      deps += n

    def lookup(n: Global): Type = n match {
      case Global.Member(Global.Top("__const"), _) =>
        constTy(n)
      case _ =>
        touch(n)
        env(n) match {
          case Defn.Var(_, _, ty, _)     => ty
          case Defn.Const(_, _, ty, _)   => ty
          case Defn.Declare(_, _, sig)   => sig
          case Defn.Define(_, _, sig, _) => sig
          case _                         => unreachable
        }
    }

    def genDefns(defns: Seq[Defn])(implicit sb: ShowBuilder): Unit = {
      import sb._
      def onDefn(defn: Defn): Unit = {
        val mn = mangled(defn.name)
        if (!generated.contains(mn)) {
          newline()
          genDefn(defn)
          generated += mn
        }
      }

      defns.foreach { defn => if (defn.isInstanceOf[Defn.Const]) onDefn(defn) }
      defns.foreach { defn => if (defn.isInstanceOf[Defn.Var]) onDefn(defn) }
      defns.foreach { defn =>
        if (defn.isInstanceOf[Defn.Declare]) onDefn(defn)
      }
      defns.foreach { defn => if (defn.isInstanceOf[Defn.Define]) onDefn(defn) }
    }

    def genPrelude()(implicit sb: ShowBuilder): Unit = {
      import sb._
      if (targetTriple.nonEmpty) {
        str("target triple = \"")
        str(targetTriple)
        str("\"")
        newline()
      }
      line("declare i32 @llvm.eh.typeid.for(i8*)")
      line("declare i32 @__gxx_personality_v0(...)")
      line("declare i8* @__cxa_begin_catch(i8*)")
      line("declare void @__cxa_end_catch()")
      line(
        "@_ZTIN11scalanative16ExceptionWrapperE = external constant { i8*, i8*, i8* }")
    }

    def genConsts()(implicit sb: ShowBuilder): Unit = {
      import sb._
      constMap.toSeq.sortBy(_._2.show).foreach {
        case (v, name) =>
          newline()
          str("@")
          genGlobal(name)
          str(" = private unnamed_addr constant ")
          genVal(v)
      }
    }

    def genDefn(defn: Defn)(implicit sb: ShowBuilder): Unit = defn match {
      case Defn.Var(attrs, name, ty, rhs) =>
        genGlobalDefn(attrs, name, isConst = false, ty, rhs)
      case Defn.Const(attrs, name, ty, rhs) =>
        genGlobalDefn(attrs, name, isConst = true, ty, rhs)
      case Defn.Declare(attrs, name, sig) =>
        genFunctionDefn(attrs, name, sig, Seq(), Fresh())
      case Defn.Define(attrs, name, sig, insts) =>
        genFunctionDefn(attrs, name, sig, insts, Fresh(insts))
      case defn =>
        unsupported(defn)
    }

    def genGlobalDefn(attrs: Attrs,
                      name: nir.Global,
                      isConst: Boolean,
                      ty: nir.Type,
                      rhs: nir.Val)(implicit sb: ShowBuilder): Unit = {
      import sb._
      str("@")
      genGlobal(name)
      str(" = ")
      str(if (attrs.isExtern) "external " else "hidden ")
      str(if (isConst) "constant" else "global")
      str(" ")
      if (attrs.isExtern) {
        genType(ty)
      } else {
        genVal(rhs)
      }
    }

    def genFunctionDefn(attrs: Attrs,
                        name: Global,
                        sig: Type,
                        insts: Seq[Inst],
                        fresh: Fresh)(implicit sb: ShowBuilder): Unit = {
      import sb._

      val Type.Function(argtys, retty) = sig

      val isDecl = insts.isEmpty

      str(if (isDecl) "declare " else "define ")
      genFunctionReturnType(retty)
      str(" @")
      genGlobal(name)
      str("(")
      if (isDecl) {
        rep(argtys, sep = ", ")(genType)
      } else {
        insts.head match {
          case Inst.Label(_, params) =>
            rep(params, sep = ", ")(genVal)
          case _ =>
            unreachable
        }
      }
      str(")")
      if (attrs.opt eq Attr.NoOpt) {
        str(" optnone noinline")
      } else {
        if (attrs.inlineHint ne Attr.MayInline) {
          str(" ")
          genAttr(attrs.inlineHint)
        }
      }
      if (!attrs.isExtern && !isDecl) {
        str(" ")
        str(gxxpersonality)
      }
      if (!isDecl) {
        str(" {")

        insts.foreach {
          case Inst.Let(n, Op.Copy(v), _) =>
            copies(n) = v
          case _ =>
            ()
        }

        val cfg = CFG(insts)
        cfg.all.foreach { block => genBlock(block)(cfg, fresh, sb) }
        cfg.all.foreach { block => genBlockLandingPads(block)(cfg, fresh, sb) }
        newline()

        str("}")

        copies.clear()
      }
    }

    def genFunctionReturnType(retty: Type)(implicit sb: ShowBuilder): Unit = {
      retty match {
        case refty: Type.RefKind =>
          genReferenceTypeAttribute(refty)
        case _ =>
          ()
      }
      genType(retty)
    }

    def genReferenceTypeAttribute(refty: Type.RefKind)(
        implicit sb: ShowBuilder): Unit = {
      import sb._
      val (nonnull, deref, size) = toDereferenceable(refty)

      if (nonnull) {
        str("nonnull ")
      }
      str(deref)
      str("(")
      str(size)
      str(") ")
    }

    def toDereferenceable(refty: Type.RefKind): (Boolean, String, Long) = {
      val size = meta.linked.infos(refty.className) match {
        case info: linker.Trait =>
          meta.layout(meta.linked.ObjectClass).size
        case info: linker.Class =>
          meta.layout(info).size
        case _ =>
          unreachable
      }

      if (!refty.isNullable) {
        (true, "dereferenceable", size)
      } else {
        (false, "dereferenceable_or_null", size)
      }
    }

    def genBlock(block: Block)(implicit cfg: CFG,
                               fresh: Fresh,
                               sb: ShowBuilder): Unit = {
      import sb._
      val Block(name, params, insts, isEntry) = block
      currentBlockName = name
      currentBlockSplit = 0

      genBlockHeader()
      indent()
      genBlockPrologue(block)
      rep(insts) { inst => genInst(inst) }
      unindent()
    }

    def genBlockHeader()(implicit sb: ShowBuilder): Unit = {
      import sb._
      newline()
      genBlockSplitName()
      str(":")
    }

    def genBlockSplitName()(implicit sb: ShowBuilder): Unit = {
      import sb._
      genLocal(currentBlockName)
      str(".")
      str(currentBlockSplit)
    }

    def genBlockPrologue(block: Block)(implicit cfg: CFG,
                                       fresh: Fresh,
                                       sb: ShowBuilder): Unit = {
      import sb._
      if (!block.isEntry) {
        val params = block.params
        params.zipWithIndex.foreach {
          case (Val.Local(name, ty), n) =>
            newline()
            str("%")
            genLocal(name)
            str(" = phi ")
            genType(ty)
            str(" ")
            rep(block.inEdges, sep = ", ") { edge =>
              def genRegularEdge(next: Next.Label): Unit = {
                val Next.Label(_, vals) = next
                genJustVal(vals(n))
                str(", %")
                genLocal(edge.from.name)
                str(".")
                str(edge.from.splitCount)
              }
              def genUnwindEdge(unwind: Next.Unwind): Unit = {
                val Next.Unwind(Val.Local(exc, _), Next.Label(_, vals)) = unwind
                genJustVal(vals(n))
                str(", %")
                genLocal(exc)
                str(".landingpad.succ")
              }

              str("[")
              edge.next match {
                case n: Next.Label =>
                  genRegularEdge(n)
                case Next.Case(_, n: Next.Label) =>
                  genRegularEdge(n)
                case n: Next.Unwind =>
                  genUnwindEdge(n)
                case _ =>
                  unreachable
              }
              str("]")
            }
        }
      }
    }

    def genBlockLandingPads(block: Block)(implicit cfg: CFG,
                                          fresh: Fresh,
                                          sb: ShowBuilder): Unit = {
      block.insts.foreach {
        case inst @ Inst.Let(_, _, unwind: Next.Unwind) =>
          import inst.pos
          genLandingPad(unwind)
        case _ =>
          ()
      }
    }

    def genLandingPad(unwind: Next.Unwind)(implicit fresh: Fresh,
                                           pos: nir.Position,
                                           sb: ShowBuilder): Unit = {
      import sb._
      val Next.Unwind(Val.Local(excname, _), next) = unwind

      val excpad  = "_" + excname.id + ".landingpad"
      val excsucc = excpad + ".succ"
      val excfail = excpad + ".fail"

      val exc                  = "%_" + excname.id
      val rec, r0, r1, id, cmp = "%_" + fresh().id
      val w0, w1, w2           = "%_" + fresh().id

      def line(s: String) = { newline(); str(s) }

      line(s"$excpad:")
      indent()
      line(s"$rec = $landingpad")
      line(s"$r0 = extractvalue $excrecty $rec, 0")
      line(s"$r1 = extractvalue $excrecty $rec, 1")
      line(s"$id = $typeid")
      line(s"$cmp = icmp eq i32 $r1, $id")
      line(s"br i1 $cmp, label %$excsucc, label %$excfail")
      unindent()

      line(s"$excsucc:")
      indent()
      line(s"$w0 = call i8* @__cxa_begin_catch(i8* $r0)")
      line(s"$w1 = bitcast i8* $w0 to i8**")
      line(s"$w2 = getelementptr i8*, i8** $w1, i32 1")
      line(s"$exc = load i8*, i8** $w2")
      line(s"call void @__cxa_end_catch()")
      genInst(Inst.Jump(next))
      unindent()

      line(s"$excfail:")
      indent()
      line(s"resume $excrecty $rec")
      unindent()
    }

    def genType(ty: Type)(implicit sb: ShowBuilder): Unit = {
      import sb._
      ty match {
        case Type.Vararg                                           => str("...")
        case _: Type.RefKind | Type.Ptr | Type.Null | Type.Nothing => str("i8*")
        case Type.Bool                                             => str("i1")
        case i: Type.I                                             => str("i"); str(i.width)
        case Type.Float                                            => str("float")
        case Type.Double                                           => str("double")
        case Type.ArrayValue(ty, n) =>
          str("[")
          str(n)
          str(" x ")
          genType(ty)
          str("]")
        case Type.StructValue(tys) =>
          str("{ ")
          rep(tys, sep = ", ")(genType)
          str(" }")
        case Type.Function(args, ret) =>
          genType(ret)
          str(" (")
          rep(args, sep = ", ")(genType)
          str(")")
        case ty =>
          unsupported(ty)
      }
    }

    private val constMap = mutable.Map.empty[Val, Global]
    private val constTy  = mutable.Map.empty[Global, Type]
    def constFor(v: Val): Global =
      if (constMap.contains(v)) {
        constMap(v)
      } else {
        val idx = constMap.size
        val name =
          Global.Member(Global.Top("__const"), Sig.Generated(idx.toString))
        constMap(v) = name
        constTy(name) = v.ty
        name
      }
    def deconstify(v: Val): Val = v match {
      case Val.Local(local, _) if copies.contains(local) =>
        deconstify(copies(local))
      case Val.StructValue(vals) =>
        Val.StructValue(vals.map(deconstify))
      case Val.ArrayValue(elemty, vals) =>
        Val.ArrayValue(elemty, vals.map(deconstify))
      case Val.Const(value) =>
        Val.Global(constFor(deconstify(value)), Type.Ptr)
      case _ =>
        v
    }

    def genJustVal(v: Val)(implicit sb: ShowBuilder): Unit = {
      import sb._

      deconstify(v) match {
        case Val.True      => str("true")
        case Val.False     => str("false")
        case Val.Null      => str("null")
        case Val.Zero(ty)  => str("zeroinitializer")
        case Val.Byte(v)   => str(v)
        case Val.Char(v)   => str(v.toInt)
        case Val.Short(v)  => str(v)
        case Val.Int(v)    => str(v)
        case Val.Long(v)   => str(v)
        case Val.Float(v)  => genFloatHex(v)
        case Val.Double(v) => genDoubleHex(v)
        case Val.StructValue(vs) =>
          str("{ ")
          rep(vs, sep = ", ")(genVal)
          str(" }")
        case Val.ArrayValue(_, vs) =>
          str("[ ")
          rep(vs, sep = ", ")(genVal)
          str(" ]")
        case v: Val.Chars =>
          genChars(v.bytes)
        case Val.Local(n, ty) =>
          str("%")
          genLocal(n)
        case Val.Global(n, ty) =>
          str("bitcast (")
          genType(lookup(n))
          str("* @")
          genGlobal(n)
          str(" to i8*)")
        case _ =>
          unsupported(v)
      }
    }

    def genChars(bytes: Array[Byte])(implicit sb: ShowBuilder): Unit = {
      import sb._

      str("c\"")
      bytes.foreach {
        case '\\' => str("\\\\")
        case c if c < 0x20 || c == '"' || c >= 0x7f =>
          val hex = Integer.toHexString(c)
          str {
            if (hex.length < 2) "\\0" + hex
            else "\\" + hex
          }
        case c => str(c.toChar)
      }
      str("\\00\"")
    }

    def genFloatHex(value: Float)(implicit sb: ShowBuilder): Unit = {
      import sb._
      str("0x")
      str(jl.Long.toHexString(jl.Double.doubleToRawLongBits(value.toDouble)))
    }

    def genDoubleHex(value: Double)(implicit sb: ShowBuilder): Unit = {
      import sb._
      str("0x")
      str(jl.Long.toHexString(jl.Double.doubleToRawLongBits(value)))
    }

    def genVal(value: Val)(implicit sb: ShowBuilder): Unit = {
      import sb._
      genType(value.ty)
      str(" ")
      genJustVal(value)
    }

    def mangled(g: Global): String = g match {
      case Global.None =>
        unsupported(g)
      case Global.Member(_, sig) if sig.isExtern =>
        val Sig.Extern(id) = sig.unmangled
        id
      case _ =>
        "_S" + g.mangle
    }

    def genGlobal(g: Global)(implicit sb: ShowBuilder): Unit = {
      import sb._
      str("\"")
      str(mangled(g))
      str("\"")
    }

    def genLocal(local: Local)(implicit sb: ShowBuilder): Unit = {
      import sb._
      local match {
        case Local(id) =>
          str("_")
          str(id)
      }
    }

    def genInst(inst: Inst)(implicit fresh: Fresh, sb: ShowBuilder): Unit = {
      import sb._
      inst match {
        case inst: Inst.Let =>
          genLet(inst)

        case Inst.Unreachable(unwind) =>
          assert(unwind eq Next.None)
          newline()
          str("unreachable")

        case Inst.Ret(value) =>
          newline()
          str("ret ")
          genVal(value)

        case Inst.Jump(next) =>
          newline()
          str("br ")
          genNext(next)

        // LLVM Phis can not express two different if branches pointing at the
        // same target basic block. In those cases we replace branching with
        // select instruction.
        case Inst.If(cond,
                     thenNext @ Next.Label(thenName, thenArgs),
                     elseNext @ Next.Label(elseName, elseArgs))
            if thenName == elseName =>
          if (thenArgs == elseArgs) {
            genInst(Inst.Jump(thenNext)(inst.pos))
          } else {
            val args = thenArgs.zip(elseArgs).map {
              case (thenV, elseV) =>
                val name = fresh()
                newline()
                str("%")
                genLocal(name)
                str(" = select ")
                genVal(cond)
                str(", ")
                genVal(thenV)
                str(", ")
                genVal(elseV)
                Val.Local(name, thenV.ty)
            }
            genInst(Inst.Jump(Next.Label(thenName, args))(inst.pos))
          }

        case Inst.If(cond, thenp, elsep) =>
          newline()
          str("br ")
          genVal(cond)
          str(", ")
          genNext(thenp)
          str(", ")
          genNext(elsep)

        case Inst.Switch(scrut, default, cases) =>
          newline()
          str("switch ")
          genVal(scrut)
          str(", ")
          genNext(default)
          str(" [")
          indent()
          rep(cases) { next =>
            newline()
            genNext(next)
          }
          unindent()
          newline()
          str("]")

        case cf =>
          unsupported(cf)
      }
    }

    def genLet(inst: Inst.Let)(implicit fresh: Fresh, sb: ShowBuilder): Unit = {
      import sb._
      def isVoid(ty: Type): Boolean =
        ty == Type.Unit || ty == Type.Nothing

      val op     = inst.op
      val name   = inst.name
      val unwind = inst.unwind

      def genBind() =
        if (!isVoid(op.resty)) {
          str("%")
          genLocal(name)
          str(" = ")
        }

      op match {
        case _: Op.Copy =>
          ()

        case call: Op.Call =>
          /* When a call points to an extern method with same mangled Sig as some already defined call
           * in another extern object we need to manually enforce getting into second case of `genCall`
           * (when lookup(pointee) != call.ty). By replacing `call.ptr` with the ptr of that already
           * defined call so we can enforce creating call bitcasts to the correct types.
           * Because of the deduplication in `genDeps` and since mangling Sig.Extern is not based
           * on function types, each extern method in deps is generated only once in IR file.
           * In this case LLVM linking would otherwise result in call arguments type mismatch.
           */
          val callDef = call.ptr match {
            case Val.Global(m @ Global.Member(_, sig), valty) if sig.isExtern =>
              val glob = externSigMembers.getOrElseUpdate(sig, m)
              if (glob == m) call
              else call.copy(ptr = Val.Global(glob, valty))
            case _ => call
          }
          genCall(genBind, callDef, unwind)

        case Op.Load(ty, ptr) =>
          val pointee = fresh()

          newline()
          str("%")
          genLocal(pointee)
          str(" = bitcast ")
          genVal(ptr)
          str(" to ")
          genType(ty)
          str("*")

          newline()
          genBind()
          str("load ")
          genType(ty)
          str(", ")
          genType(ty)
          str("* %")
          genLocal(pointee)
          ty match {
            case refty: Type.RefKind =>
              val (nonnull, deref, size) = toDereferenceable(refty)
              if (nonnull) {
                str(", !nonnull !{}")
              }
              str(", !")
              str(deref)
              str(" !{i64 ")
              str(size)
              str("}")
            case _ =>
              ()
          }

        case Op.Store(ty, ptr, value) =>
          val pointee = fresh()

          newline()
          str("%")
          genLocal(pointee)
          str(" = bitcast ")
          genVal(ptr)
          str(" to ")
          genType(ty)
          str("*")

          newline()
          genBind()
          str("store ")
          genVal(value)
          str(", ")
          genType(ty)
          str("* %")
          genLocal(pointee)

        case Op.Elem(ty, ptr, indexes) =>
          val pointee = fresh()
          val derived = fresh()

          newline()
          str("%")
          genLocal(pointee)
          str(" = bitcast ")
          genVal(ptr)
          str(" to ")
          genType(ty)
          str("*")

          newline()
          str("%")
          genLocal(derived)
          str(" = getelementptr ")
          genType(ty)
          str(", ")
          genType(ty)
          str("* %")
          genLocal(pointee)
          str(", ")
          rep(indexes, sep = ", ")(genVal)

          newline()
          genBind()
          str("bitcast ")
          genType(ty.elemty(indexes.tail))
          str("* %")
          genLocal(derived)
          str(" to i8*")

        case Op.Stackalloc(ty, n) =>
          val pointee = fresh()

          newline()
          str("%")
          genLocal(pointee)
          str(" = alloca ")
          genType(ty)
          str(", ")
          genVal(n)
          str(", align 8")

          newline()
          genBind()
          str("bitcast ")
          genType(ty)
          str("* %")
          genLocal(pointee)
          str(" to i8*")

        case _ =>
          newline()
          genBind()
          genOp(op)
      }
    }

    def genCall(genBind: () => Unit, call: Op.Call, unwind: Next)(
        implicit fresh: Fresh,
        sb: ShowBuilder): Unit = {
      import sb._
      call match {
        case Op.Call(ty, Val.Global(pointee, _), args)
            if lookup(pointee) == ty =>
          val Type.Function(argtys, _) = ty

          touch(pointee)

          newline()
          genBind()
          str(if (unwind ne Next.None) "invoke " else "call ")
          genCallFunctionType(ty)
          str(" @")
          genGlobal(pointee)
          str("(")
          rep(args, sep = ", ")(genCallArgument)
          str(")")

          if (unwind ne Next.None) {
            str(" to label %")
            currentBlockSplit += 1
            genBlockSplitName()
            str(" unwind ")
            genNext(unwind)

            unindent()
            genBlockHeader()
            indent()
          }

        case Op.Call(ty, ptr, args) =>
          val Type.Function(_, resty) = ty

          val pointee = fresh()

          newline()
          str("%")
          genLocal(pointee)
          str(" = bitcast ")
          genVal(ptr)
          str(" to ")
          genType(ty)
          str("*")

          newline()
          genBind()
          str(if (unwind ne Next.None) "invoke " else "call ")
          genCallFunctionType(ty)
          str(" %")
          genLocal(pointee)
          str("(")
          rep(args, sep = ", ")(genCallArgument)
          str(")")

          if (unwind ne Next.None) {
            str(" to label %")
            currentBlockSplit += 1
            genBlockSplitName()
            str(" unwind ")
            genNext(unwind)

            unindent()
            genBlockHeader()
            indent()
          }
      }
    }

    def genCallFunctionType(ty: Type)(implicit sb: ShowBuilder): Unit = {
      import sb._
      ty match {
        case Type.Function(argtys, retty) =>
          val hasVarArgs = argtys.contains(Type.Vararg)
          if (hasVarArgs) {
            genType(ty)
          } else {
            genFunctionReturnType(retty)
          }
        case _ =>
          unreachable
      }
    }

    def genCallArgument(v: Val)(implicit sb: ShowBuilder): Unit = {
      import sb._
      v match {
        case Val.Local(_, refty: Type.RefKind) =>
          val (nonnull, deref, size) = toDereferenceable(refty)
          genType(refty)
          if (nonnull) {
            str(" nonnull")
          }
          str(" ")
          str(deref)
          str("(")
          str(size)
          str(")")
          str(" ")
          genJustVal(v)
        case _ =>
          genVal(v)
      }
    }

    def genOp(op: Op)(implicit sb: ShowBuilder): Unit = {
      import sb._
      op match {
        case Op.Extract(aggr, indexes) =>
          str("extractvalue ")
          genVal(aggr)
          str(", ")
          rep(indexes, sep = ", ")(str)
        case Op.Insert(aggr, value, indexes) =>
          str("insertvalue ")
          genVal(aggr)
          str(", ")
          genVal(value)
          str(", ")
          rep(indexes, sep = ", ")(str)
        case Op.Bin(opcode, ty, l, r) =>
          val bin = opcode match {
            case Bin.Iadd => "add"
            case Bin.Isub => "sub"
            case Bin.Imul => "mul"
            case _        => opcode.toString.toLowerCase
          }
          str(bin)
          str(" ")
          genVal(l)
          str(", ")
          genJustVal(r)
        case Op.Comp(opcode, ty, l, r) =>
          val cmp = opcode match {
            case Comp.Ieq => "icmp eq"
            case Comp.Ine => "icmp ne"
            case Comp.Ult => "icmp ult"
            case Comp.Ule => "icmp ule"
            case Comp.Ugt => "icmp ugt"
            case Comp.Uge => "icmp uge"
            case Comp.Slt => "icmp slt"
            case Comp.Sle => "icmp sle"
            case Comp.Sgt => "icmp sgt"
            case Comp.Sge => "icmp sge"
            case Comp.Feq => "fcmp oeq"
            case Comp.Fne => "fcmp une"
            case Comp.Flt => "fcmp olt"
            case Comp.Fle => "fcmp ole"
            case Comp.Fgt => "fcmp ogt"
            case Comp.Fge => "fcmp oge"
          }
          str(cmp)
          str(" ")
          genVal(l)
          str(", ")
          genJustVal(r)
        case Op.Conv(conv, ty, v) =>
          genConv(conv)
          str(" ")
          genVal(v)
          str(" to ")
          genType(ty)
        case op =>
          unsupported(op)
      }
    }

    def genNext(next: Next)(implicit sb: ShowBuilder): Unit = {
      import sb._
      next match {
        case Next.Case(v, next) =>
          genVal(v)
          str(", label %")
          genLocal(next.name)
          str(".0")
        case Next.Unwind(Val.Local(exc, _), _) =>
          str("label %_")
          str(exc.id)
          str(".landingpad")
        case next =>
          str("label %")
          genLocal(next.name)
          str(".0")
      }
    }

    def genConv(conv: Conv)(implicit sb: ShowBuilder): Unit =
      sb.str(conv.show)

    def genAttr(attr: Attr)(implicit sb: ShowBuilder): Unit =
      sb.str(attr.show)
  }

  private object Impl {
    val gxxpersonality =
      "personality i8* bitcast (i32 (...)* @__gxx_personality_v0 to i8*)"
    val excrecty = "{ i8*, i32 }"
    val landingpad =
      "landingpad { i8*, i32 } catch i8* bitcast ({ i8*, i8*, i8* }* @_ZTIN11scalanative16ExceptionWrapperE to i8*)"
    val typeid =
      "call i32 @llvm.eh.typeid.for(i8* bitcast ({ i8*, i8*, i8* }* @_ZTIN11scalanative16ExceptionWrapperE to i8*))"
  }

  val depends: Seq[Global] = {
    val buf = mutable.UnrolledBuffer.empty[Global]
    buf ++= Lower.depends
    buf ++= Generate.depends
    buf += Rt.Object.name member Rt.ScalaEqualsSig
    buf += Rt.Object.name member Rt.ScalaHashCodeSig
    buf += Rt.Object.name member Rt.JavaEqualsSig
    buf += Rt.Object.name member Rt.JavaHashCodeSig
    buf
  }
}

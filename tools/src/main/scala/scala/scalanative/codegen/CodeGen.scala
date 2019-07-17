package scala.scalanative
package codegen

import java.{lang => jl}
import java.nio.ByteBuffer
import java.nio.file.Paths
import scala.annotation.tailrec
import scala.collection.mutable
import scalanative.util.{Scope, ShowBuilder, unsupported, partitionBy, procs}
import scalanative.io.{VirtualDirectory, withScratchBuffer}
import scalanative.nir._
import scalanative.nir.ControlFlow.{Graph => CFG, Block, Edge}
import scalanative.util.unreachable

object CodeGen {

  /** Lower and generate code for given assembly. */
  def apply(config: build.Config, linked: linker.Result): Unit = {
    val defns   = linked.defns
    val proxies = GenerateReflectiveProxies(linked.dynimpls, defns)

    implicit val meta = new Metadata(linked, proxies)

    val generated = Generate(Global.Top(config.mainClass), defns ++ proxies)
    val lowered   = lower(generated)
    nir.Show.dump(lowered, "lowered.hnir")
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
      .foreach { defns =>
        buf ++= defns
      }

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
            val impl   = new Impl(config.targetTriple, env, sorted)
            val buffer = impl.gen()
            buffer.flip
            workdir.write(Paths.get(s"$id.ll"), buffer)
        }

      // Generate a single LLVM IR file for the whole application.
      // This is an adhoc form of LTO. We use it in release mode if
      // Clang's LTO is not available.
      def single(): Unit = {
        val sorted = assembly.sortBy(_.name.show)
        val impl   = new Impl(config.targetTriple, env, sorted)
        val buffer = impl.gen()
        buffer.flip
        workdir.write(Paths.get("out.ll"), buffer)
      }

      (config.mode, config.LTO) match {
        case (build.Mode.Debug, _)           => separate()
        case (_: build.Mode.Release, "none") => single()
        case (_: build.Mode.Release, _)      => separate()
      }
    }

  private final class Impl(targetTriple: String,
                           env: Map[Global, Defn],
                           defns: Seq[Defn])(implicit meta: Metadata) {
    import Impl._

    var currentBlockName: Local = _
    var currentBlockSplit: Int  = _

    val copies    = mutable.Map.empty[Local, Val]
    val deps      = mutable.Set.empty[Global]
    val generated = mutable.Set.empty[String]
    val builder   = new ShowBuilder
    import builder._

    def gen(): ByteBuffer = {
      genDefns(defns)
      val body = builder.toString.getBytes("UTF-8")
      builder.clear
      genPrelude()
      genConsts()
      genDeps()
      val prelude = builder.toString.getBytes("UTF-8")
      val buffer  = ByteBuffer.allocate(prelude.length + body.length)
      buffer.put(prelude)
      buffer.put(body)
    }

    def genDeps() = deps.foreach { n =>
      val mn = mangled(n)
      if (!generated.contains(mn)) {
        newline()
        genDefn {
          env(n) match {
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

    def genDefns(defns: Seq[Defn]): Unit = {
      def onDefn(defn: Defn): Unit = {
        val mn = mangled(defn.name)
        if (!generated.contains(mn)) {
          newline()
          genDefn(defn)
          generated += mn
        }
      }

      defns.foreach { defn =>
        if (defn.isInstanceOf[Defn.Const]) onDefn(defn)
      }
      defns.foreach { defn =>
        if (defn.isInstanceOf[Defn.Var]) onDefn(defn)
      }
      defns.foreach { defn =>
        if (defn.isInstanceOf[Defn.Declare]) onDefn(defn)
      }
      defns.foreach { defn =>
        if (defn.isInstanceOf[Defn.Define]) onDefn(defn)
      }
    }

    def genPrelude(): Unit = {
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

    def genConsts() =
      constMap.toSeq.sortBy(_._2.show).foreach {
        case (v, name) =>
          newline()
          str("@")
          genGlobal(name)
          str(" = private unnamed_addr constant ")
          genVal(v)
      }

    def genDefn(defn: Defn): Unit = defn match {
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
                      rhs: nir.Val): Unit = {
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
                        fresh: Fresh): Unit = {
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
        if (attrs.inline ne Attr.MayInline) {
          str(" ")
          genAttr(attrs.inline)
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
        cfg.all.foreach { block =>
          genBlock(block)(cfg, fresh)
        }
        cfg.all.foreach { block =>
          genBlockLandingPads(block)(cfg, fresh)
        }
        newline()

        str("}")

        copies.clear()
      }
    }

    def genFunctionReturnType(retty: Type): Unit = {
      retty match {
        case refty: Type.RefKind =>
          genReferenceTypeAttribute(refty)
        case _ =>
          ()
      }
      genType(retty)
    }

    def genFunctionParam(param: Val.Local): Unit = {
      param.ty match {
        case refty: Type.RefKind =>
          genReferenceTypeAttribute(refty)
        case _ =>
          ()
      }
      genVal(param)
    }

    def genReferenceTypeAttribute(refty: Type.RefKind): Unit = {
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

    def genBlock(block: Block)(implicit cfg: CFG, fresh: Fresh): Unit = {
      val Block(name, params, insts, isEntry) = block
      currentBlockName = name
      currentBlockSplit = 0

      genBlockHeader()
      indent()
      genBlockPrologue(block)
      rep(insts) { inst =>
        genInst(inst)
      }
      unindent()
    }

    def genBlockHeader(): Unit = {
      newline()
      genBlockSplitName()
      str(":")
    }

    def genBlockSplitName(): Unit = {
      genLocal(currentBlockName)
      str(".")
      str(currentBlockSplit)
    }

    def genBlockPrologue(block: Block)(implicit cfg: CFG,
                                       fresh: Fresh): Unit = {
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
                                          fresh: Fresh): Unit = {
      block.insts.foreach {
        case Inst.Let(_, _, unwind: Next.Unwind) =>
          genLandingPad(unwind)
        case _ =>
          ()
      }
    }

    def genLandingPad(unwind: Next.Unwind)(implicit fresh: Fresh): Unit = {
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

    def genType(ty: Type): Unit = ty match {
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

    val constMap = mutable.Map.empty[Val, Global]
    val constTy  = mutable.Map.empty[Global, Type]
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

    def genJustVal(v: Val): Unit = deconstify(v) match {
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
      case Val.Chars(v) =>
        genChars(v)
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

    def genChars(value: String): Unit = {
      // `value` should contain a content of a CString literal as is in its source file
      // malformed literals are assumed absent
      str("c\"")
      @tailrec def loop(from: Int): Unit =
        value.indexOf('\\', from) match {
          case -1 => str(value.substring(from))
          case idx =>
            str(value.substring(from, idx))
            import Character.isDigit
            def isOct(c: Char): Boolean = isDigit(c) && c != '8' && c != '9'
            def isHex(c: Char): Boolean =
              isDigit(c) ||
                c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f' ||
                c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E' || c == 'F'
            value(idx + 1) match {
              case c @ (''' | '"' | '?') => str(c); loop(idx + 2)
              case '\\'                  => str("\\\\"); loop(idx + 2)
              case 'a'                   => str("\\07"); loop(idx + 2)
              case 'b'                   => str("\\08"); loop(idx + 2)
              case 'f'                   => str("\\0C"); loop(idx + 2)
              case 'n'                   => str("\\0A"); loop(idx + 2)
              case 'r'                   => str("\\0D"); loop(idx + 2)
              case 't'                   => str("\\09"); loop(idx + 2)
              case 'v'                   => str("\\0B"); loop(idx + 2)
              case d if isOct(d) =>
                val oct = value.drop(idx + 1).take(3).takeWhile(isOct)
                val hex =
                  Integer.toHexString(Integer.parseInt(oct, 8)).toUpperCase
                str {
                  if (hex.length < 2) "\\0" + hex
                  else "\\" + hex
                }
                loop(idx + 1 + oct.length)
              case 'x' =>
                val hex = value.drop(idx + 2).takeWhile(isHex).toUpperCase
                str {
                  if (hex.length < 2) "\\0" + hex
                  else "\\" + hex
                }
                loop(idx + 2 + hex.length)
              case unknown =>
                // clang warns but allows unknown escape sequences, while java emits errors
                str(unknown); loop(idx + 2)
            }
        }
      loop(0)
      str("\\00\"")
    }

    def genFloatHex(value: Float): Unit = {
      str("0x")
      str(jl.Long.toHexString(jl.Double.doubleToRawLongBits(value.toDouble)))
    }

    def genDoubleHex(value: Double): Unit = {
      str("0x")
      str(jl.Long.toHexString(jl.Double.doubleToRawLongBits(value)))
    }

    def genVal(value: Val): Unit = {
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

    def genGlobal(g: Global): Unit = {
      str("\"")
      str(mangled(g))
      str("\"")
    }

    def genLocal(local: Local): Unit = local match {
      case Local(id) =>
        str("_")
        str(id)
    }

    def genInst(inst: Inst)(implicit fresh: Fresh): Unit = inst match {
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
          genInst(Inst.Jump(thenNext))
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
          genInst(Inst.Jump(Next.Label(thenName, args)))
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

    def genLet(inst: Inst.Let)(implicit fresh: Fresh): Unit = {
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
          genCall(genBind, call, unwind)

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
        implicit fresh: Fresh): Unit = call match {
      case Op.Call(ty, Val.Global(pointee, _), args) if lookup(pointee) == ty =>
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

    def genCallFunctionType(ty: Type): Unit = ty match {
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

    def genCallArgument(v: Val): Unit = v match {
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

    def genOp(op: Op): Unit = op match {
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

    def genNext(next: Next) = next match {
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

    def genConv(conv: Conv): Unit =
      str(conv.show)

    def genAttr(attr: Attr): Unit =
      str(attr.show)
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
    buf
  }
}

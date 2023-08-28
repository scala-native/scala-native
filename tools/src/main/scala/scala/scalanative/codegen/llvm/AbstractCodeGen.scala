package scala.scalanative.codegen
package llvm

import java.nio.file.{Path, Paths}
import java.{lang => jl}
import scala.collection.mutable
import scala.scalanative.build.Discover
import scala.scalanative.codegen.llvm.compat.os.OsCompat
import scala.scalanative.io.VirtualDirectory
import scala.scalanative.nir.ControlFlow.{Block, Graph => CFG}
import scala.scalanative.nir._
import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.util.ShowBuilder.FileShowBuilder
import scala.scalanative.util.{ShowBuilder, unreachable, unsupported}
import scala.scalanative.{build, linker, nir}
import scala.util.control.NonFatal
import scala.scalanative.codegen.{Metadata => CodeGenMetadata}

import scala.language.implicitConversions
import scala.scalanative.codegen.llvm.Metadata.conversions._
import scala.scalanative.nir.Defn.Const
import scala.scalanative.nir.Defn.Declare
import scala.scalanative.nir.Defn.Var
import scala.scalanative.nir.Defn.Define
import scala.scalanative.nir.Defn.Trait

private[codegen] abstract class AbstractCodeGen(
    env: Map[Global, Defn],
    defns: Seq[Defn]
)(implicit val meta: CodeGenMetadata)
    extends MetadataCodeGen {
  import meta.platform
  import meta.config
  import platform._

  val os: OsCompat
  val pointerType = if (useOpaquePointers) "ptr" else "i8*"

  private var currentBlockName: Local = _
  private var currentBlockSplit: Int = _

  private val copies = mutable.Map.empty[Local, nir.Val]
  private val deps = mutable.Set.empty[Global.Member]
  private val generated = mutable.Set.empty[String]
  private val externSigMembers = mutable.Map.empty[Sig, Global.Member]

  def gen(id: String, dir: VirtualDirectory): Path = {
    val body = Paths.get(s"$id-body.ll")
    val headers = Paths.get(s"$id.ll")
    val metadata = Paths.get(s"$id-metadata.ll")

    dir.write(metadata) { metadataWriter =>
      implicit val metadata: MetadataCodeGen.Context =
        new MetadataCodeGen.Context(this, new FileShowBuilder(metadataWriter))
      genDebugMetadata()

      dir.write(body) { writer =>
        implicit val fsb: ShowBuilder = new FileShowBuilder(writer)
        genDefns(defns)
      }

      dir.write(headers) { writer =>
        implicit val sb: ShowBuilder = new FileShowBuilder(writer)
        genPrelude()
        genConsts()
        genDeps()
      }

      // Need to be generated after traversing all compilation units
      dbg("llvm.dbg.cu")(this.compilationUnits: _*)
    }

    dir.merge(Seq(body, metadata), headers)
  }

  private def genDebugMetadata()(implicit
      ctx: MetadataCodeGen.Context
  ): Unit = {
    import Metadata.Constants._
    import Metadata.ModFlagBehavior._
    dbg("llvm.module.flags")(
      tuple(Max, "Dwarf Version", DWARF_VERSION),
      tuple(Warning, "Debug Info Version", DEBUG_INFO_VERSION)
    )
  }

  private def genDeps()(implicit
      sb: ShowBuilder,
      metaCtx: MetadataCodeGen.Context
  ): Unit = deps.foreach { n =>
    val mn = mangled(n)
    if (!generated.contains(mn)) {
      sb.newline()
      genDefn {
        val defn = env(n)
        implicit val rootPos = defn.pos
        defn match {
          case defn @ Defn.Var(attrs, _, _, _) =>
            defn.copy(attrs.copy(isExtern = true))
          case defn @ Defn.Const(attrs, _, ty, _) =>
            defn.copy(attrs.copy(isExtern = true))
          case defn @ Defn.Declare(attrs, _, _) =>
            defn.copy(attrs.copy(isExtern = true))
          case defn @ Defn.Define(attrs, name, ty, _, _) =>
            Defn.Declare(attrs, name, ty)
          case _ =>
            unreachable
        }
      }
      generated += mn
    }
  }

  private def genDefns(
      defns: Seq[Defn]
  )(implicit
      sb: ShowBuilder,
      metaCtx: MetadataCodeGen.Context
  ): Unit = {
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
    defns.foreach { defn => if (defn.isInstanceOf[Defn.Declare]) onDefn(defn) }
    defns.foreach { defn => if (defn.isInstanceOf[Defn.Define]) onDefn(defn) }

  }

  protected final def touch(n: Global.Member): Unit =
    deps += n

  protected final def lookup(n: Global.Member): Type = n match {
    case Global.Member(Global.Top("__const"), _) =>
      constTy(n)
    case _ =>
      touch(n)
      env(n) match {
        case Defn.Var(_, _, ty, _)        => ty
        case Defn.Const(_, _, ty, _)      => ty
        case Defn.Declare(_, _, sig)      => sig
        case Defn.Define(_, _, sig, _, _) => sig
        case _                            => unreachable
      }
  }

  private def genPrelude()(implicit sb: ShowBuilder): Unit = {
    import sb._
    targetTriple.foreach { target =>
      str("target triple = \"")
      str(target)
      str("\"")
      newline()
    }
    os.genPrelude()
    if (config.debugMetadata) {
      newline()
      line("declare void @llvm.dbg.declare(metadata, metadata, metadata)")
      line("declare void @llvm.dbg.value(metadata, metadata, metadata)")
    }
  }

  private def genConsts()(implicit sb: ShowBuilder): Unit = {
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

  private def genDefn(defn: Defn)(implicit
      sb: ShowBuilder,
      metaCtx: MetadataCodeGen.Context
  ): Unit = defn match {
    case Defn.Var(attrs, name, ty, rhs) =>
      genGlobalDefn(attrs, name, isConst = false, ty, rhs)
    case Defn.Const(attrs, name, ty, rhs) =>
      genGlobalDefn(attrs, name, isConst = true, ty, rhs)
    case Defn.Declare(attrs, name, sig) =>
      genFunctionDefn(defn, Seq.empty, Fresh(), DebugInfo.empty)
    case Defn.Define(attrs, name, sig, insts, debugInfo) =>
      genFunctionDefn(defn, insts, Fresh(insts), debugInfo)
    case defn =>
      unsupported(defn)
  }

  private[codegen] def genGlobalDefn(
      attrs: Attrs,
      name: nir.Global,
      isConst: Boolean,
      ty: nir.Type,
      rhs: nir.Val
  )(implicit sb: ShowBuilder): Unit = {
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

  private[codegen] def genFunctionDefn(
      defn: nir.Defn,
      insts: Seq[Inst],
      fresh: Fresh,
      debugInfo: DebugInfo
  )(implicit
      sb: ShowBuilder,
      metaCtx: MetadataCodeGen.Context
  ): Unit = {
    import sb._
    import defn.{name, attrs, pos}

    val Type.Function(argtys, retty) = defn match {
      case defn: Declare => defn.ty
      case defn: Define  => defn.ty
      case _             => unreachable
    }

    val isDecl = insts.isEmpty

    newline()
    str(if (isDecl) "declare " else "define ")
    if (targetsWindows && !isDecl && attrs.isExtern) {
      // Generate export modifier only for extern (C-ABI compliant) signatures
      val Global.Member(_, sig) = name: @unchecked
      if (sig.isExtern) str("dllexport ")
    }
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

    defn match {
      case _: nir.Defn.Declare => ()
      case defn: nir.Defn.Define =>
        implicit lazy val defnScopes: DefnScopes = new DefnScopes(defn)
        str(" ")
        str(os.gxxPersonality)

        dbg(defnScopes.getDISubprogramScope)
        str(" {")
        insts.foreach {
          case Inst.Let(n, Op.Copy(v), _) => copies(n) = v
          case _                          => ()
        }

        locally {
          implicit val cfg: CFG = CFG(insts)
          implicit val _fresh: Fresh = fresh
          implicit val _debugInfo: DebugInfo = debugInfo
          cfg.all.foreach(genBlock)
          cfg.all.foreach(genBlockLandingPads)
          newline()
        }

        str("}")

        copies.clear()
      case _ => unreachable
    }
  }

  private[codegen] def genFunctionReturnType(
      retty: Type
  )(implicit sb: ShowBuilder): Unit = retty match {
    case refty: Type.RefKind if refty != Type.Unit =>
      genReferenceTypeAttribute(refty)
      genType(retty)
    case _ =>
      genType(retty)
  }

  private[codegen] def genReferenceTypeAttribute(
      refty: Type.RefKind
  )(implicit sb: ShowBuilder): Unit = {
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

  private[codegen] def toDereferenceable(
      refty: Type.RefKind
  ): (Boolean, String, Long) = {
    val size = meta.analysis.infos(refty.className) match {
      case info: linker.Trait =>
        meta.layout(meta.analysis.ObjectClass).size
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

  private[codegen] def genBlock(block: Block)(implicit
      cfg: CFG,
      fresh: Fresh,
      sb: ShowBuilder,
      debugInfo: DebugInfo,
      defnScopes: DefnScopes,
      metaCtx: MetadataCodeGen.Context
  ): Unit = {
    import sb._
    val Block(name, params, insts, isEntry) = block
    currentBlockName = name
    currentBlockSplit = 0

    genBlockHeader()
    indent()
    os.genBlockAlloca(block)
    genBlockPrologue(block)
    rep(insts)(genInst)
    unindent()
  }

  private[codegen] def genBlockHeader()(implicit sb: ShowBuilder): Unit = {
    import sb._
    newline()
    genBlockSplitName()
    str(":")
  }

  private[codegen] def genBlockSplitName()(implicit sb: ShowBuilder): Unit = {
    import sb._
    genLocal(currentBlockName)
    str(".")
    str(currentBlockSplit)
  }

  private[codegen] def genBlockPrologue(
      block: Block
  )(implicit
      cfg: CFG,
      fresh: Fresh,
      sb: ShowBuilder,
      debugInfo: DebugInfo,
      metadataCtx: MetadataCodeGen.Context,
      defnScopes: DefnScopes
  ): Unit = {
    import sb._
    val params = block.params.zipWithIndex
    if (!block.isEntry) {
      params.foreach {
        case (Val.Local(_, Type.Unit), n) => () // skip
        case (Val.Local(id, ty), n) =>
          newline()
          str("%")
          genLocal(id)
          str(" = phi ")
          genType(ty)
          str(" ")
          rep(block.inEdges.toSeq, sep = ", ") { edge =>
            def genRegularEdge(next: Next.Label): Unit = {
              val Next.Label(_, vals) = next
              genJustVal(vals(n))
              str(", %")
              genLocal(edge.from.id)
              str(".")
              str(edge.from.splitCount)
            }
            def genUnwindEdge(unwind: Next.Unwind): Unit = {
              val Next.Unwind(Val.Local(exc, _), Next.Label(_, vals)) =
                unwind: @unchecked
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
    if (generateDebugMetadata) {
      lazy val scopeId =
        if (block.isEntry) nir.ScopeId.TopLevel
        else
          block.insts
            .collectFirst { case let: Inst.Let => let.scopeId }
            .getOrElse(nir.ScopeId.TopLevel)
      params.foreach {
        case (Val.Local(id, ty), idx) =>
          // arg should be non-zero value
          val argIdx = if (block.isEntry) Some(idx + 1) else None
          dbgLocalValue(id, ty, argIdx)(
            srcPosition = block.pos,
            scopeId = scopeId
          )
      }
    }
  }

  private[codegen] def genBlockLandingPads(block: Block)(implicit
      cfg: CFG,
      fresh: Fresh,
      sb: ShowBuilder,
      debugInfo: DebugInfo,
      metaCtx: MetadataCodeGen.Context,
      defnScoeps: this.DefnScopes
  ): Unit = {
    block.insts.foreach {
      case inst @ Inst.Let(_, _, unwind: Next.Unwind) =>
        import inst.pos
        os.genLandingPad(unwind)
      case _ => ()
    }
  }

  private[codegen] def genType(ty: Type)(implicit sb: ShowBuilder): Unit = {
    import sb._
    ty match {
      case Type.Vararg => str("...")
      case Type.Unit   => str("void")
      case _: Type.RefKind | Type.Ptr | Type.Null | Type.Nothing =>
        str(pointerType)
      case Type.Bool          => str("i1")
      case i: Type.FixedSizeI => str("i"); str(i.width)
      case Type.Size =>
        str("i")
        str(platform.sizeOfPtrBits)
      case Type.Float  => str("float")
      case Type.Double => str("double")
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

  private val constMap = mutable.Map.empty[Val, Global.Member]
  private val constTy = mutable.Map.empty[Global.Member, Type]
  private[codegen] def constFor(v: Val): Global.Member =
    constMap.getOrElseUpdate(
      v, {
        val idx = constMap.size
        val name =
          Global.Member(Global.Top("__const"), Sig.Generated(idx.toString))
        constTy(name) = v.ty
        name
      }
    )
  private[codegen] def deconstify(v: Val): Val = v match {
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

  private[codegen] def genJustVal(v: Val)(implicit sb: ShowBuilder): Unit = {
    import sb._

    deconstify(v) match {
      case Val.True     => str("true")
      case Val.False    => str("false")
      case Val.Null     => str("null")
      case Val.Unit     => str("void")
      case Val.Zero(ty) => str("zeroinitializer")
      case Val.Byte(v)  => str(v)
      case Val.Size(v) =>
        if (!platform.is32Bit) str(v)
        else if (v.toInt == v) str(v.toInt)
        else unsupported("Emitting size values that exceed the platform bounds")
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
      case Val.ByteString(v) =>
        genByteString(v)
      case Val.Local(n, ty) =>
        str("%")
        genLocal(n)
      case Val.Global(n: Global.Member, ty) =>
        if (useOpaquePointers) {
          lookup(n)
          str("@")
          genGlobal(n)
        } else {
          str("bitcast (")
          genType(lookup(n))
          str("* @")
          genGlobal(n)
          str(" to i8*)")
        }
      case _ =>
        unsupported(v)
    }
  }

  private[codegen] def genByteString(
      bytes: Array[Byte]
  )(implicit sb: ShowBuilder): Unit = {
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

  private[codegen] def genFloatHex(
      value: Float
  )(implicit sb: ShowBuilder): Unit = {
    import sb._
    str("0x")
    str(jl.Long.toHexString(jl.Double.doubleToRawLongBits(value.toDouble)))
  }

  private[codegen] def genDoubleHex(
      value: Double
  )(implicit sb: ShowBuilder): Unit = {
    import sb._
    str("0x")
    str(jl.Long.toHexString(jl.Double.doubleToRawLongBits(value)))
  }

  private[codegen] def genVal(value: Val)(implicit sb: ShowBuilder): Unit = {
    import sb._
    if (value != Val.Unit) {
      genType(value.ty)
      str(" ")
    }
    genJustVal(value)
  }

  private[codegen] def mangled(g: Global): String = g match {
    case Global.None =>
      unsupported(g)
    case Global.Member(_, sig) if sig.isExtern =>
      val Sig.Extern(id) = sig.unmangled: @unchecked
      id
    case _ =>
      "_S" + g.mangle
  }

  private[codegen] def genGlobal(g: Global)(implicit sb: ShowBuilder): Unit = {
    import sb._
    str("\"")
    str(mangled(g))
    str("\"")
  }

  private[codegen] def genLocal(
      local: Local
  )(implicit sb: ShowBuilder): Unit = {
    import sb._
    local match {
      case Local(id) =>
        str("_")
        str(id)
    }
  }

  private[codegen] def genInst(inst: Inst)(implicit
      fresh: Fresh,
      sb: ShowBuilder,
      debugInfo: DebugInfo,
      defnScopes: DefnScopes,
      metaCtx: MetadataCodeGen.Context
  ): Unit = {
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
      case Inst.If(
            cond,
            thenNext @ Next.Label(thenId, thenArgs),
            elseNext @ Next.Label(elseId, elseArgs)
          ) if thenId == elseId =>
        if (thenArgs == elseArgs) {
          genInst(Inst.Jump(thenNext)(inst.pos))
        } else {
          val args = thenArgs.zip(elseArgs).map {
            case (thenV, elseV) =>
              val id = fresh()
              newline()
              str("%")
              genLocal(id)
              str(" = select ")
              genVal(cond)
              str(", ")
              genVal(thenV)
              str(", ")
              genVal(elseV)
              Val.Local(id, thenV.ty)
          }
          genInst(Inst.Jump(Next.Label(thenId, args))(inst.pos))
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

  private[codegen] def genLet(inst: Inst.Let)(implicit
      fresh: Fresh,
      sb: ShowBuilder,
      debugInfo: DebugInfo,
      defnScopes: DefnScopes,
      metaCtx: MetadataCodeGen.Context
  ): Unit = {
    import sb._
    def isVoid(ty: Type): Boolean =
      ty == Type.Unit || ty == Type.Nothing

    val op = inst.op
    val id = inst.id
    val unwind = inst.unwind
    val ty = inst.op.resty
    val scope = defnScopes.toDIScope(inst.scopeId)

    def genBind() =
      if (!isVoid(ty)) {
        str("%")
        genLocal(id)
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
        genCall(genBind, callDef, unwind, inst.pos, inst.scopeId)
        dbgLocalValue(id, ty)(inst.pos, inst.scopeId)

      case Op.Load(ty, ptr, syncAttrs) =>
        val pointee = fresh()
        val isAtomic = isMultithreadingEnabled && syncAttrs.isDefined
        val isVolatile =
          isMultithreadingEnabled && syncAttrs.exists(_.isVolatile)

        if (!useOpaquePointers) {
          newline()
          str("%")
          genLocal(pointee)
          str(" = bitcast ")
          genVal(ptr)
          str(" to ")
          genType(ty)
          str("*")
        }

        newline()
        genBind()
        str("load ")
        if (isAtomic) str("atomic ")
        if (isVolatile) str("volatile ")
        genType(ty)
        str(", ")
        if (useOpaquePointers) genVal(ptr)
        else {
          genType(ty)
          str("* %")
          genLocal(pointee)
        }
        if (isAtomic) {
          str(" ")
          syncAttrs.foreach(genSyncAttrs)
          str(", align ")
          str(MemoryLayout.alignmentOf(ty))
        } else {
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
          dbgLocalValue(id, ty)(inst.pos, inst.scopeId)
        }

      case Op.Store(ty, ptr, value, syncAttrs) =>
        val pointee = fresh()
        val isAtomic = isMultithreadingEnabled && syncAttrs.isDefined
        val isVolatile =
          isMultithreadingEnabled && syncAttrs.exists(_.isVolatile)

        if (!useOpaquePointers) {
          newline()
          str("%")
          genLocal(pointee)
          str(" = bitcast ")
          genVal(ptr)
          str(" to ")
          genType(ty)
          str("*")
        }

        newline()
        genBind()
        str("store ")
        if (isAtomic) str("atomic ")
        if (isVolatile) str("volatile ")
        genVal(value)
        if (useOpaquePointers) {
          str(", ptr")
          genJustVal(ptr)
        } else {
          str(", ")
          genType(ty)
          str("* %")
          genLocal(pointee)
        }
        if (isAtomic) syncAttrs.foreach {
          str(" ")
          genSyncAttrs(_)
        }
        str(", align ")
        str(MemoryLayout.alignmentOf(ty))

      case Op.Elem(ty, ptr, indexes) =>
        val pointee = fresh()
        val derived = fresh()

        if (!useOpaquePointers) {
          newline()
          str("%")
          genLocal(pointee)
          str(" = bitcast ")
          genVal(ptr)
          str(" to ")
          genType(ty)
          str("*")
        }

        newline()
        if (useOpaquePointers) genBind()
        else {
          str("%")
          genLocal(derived)
          str(" = ")
        }
        str("getelementptr ")
        genType(ty)
        str(", ")
        if (ty.isInstanceOf[Type.AggregateKind] || !useOpaquePointers) {
          genType(ty)
          str("*")
        } else str(pointerType)
        str(" ")
        if (useOpaquePointers) genJustVal(ptr)
        else {
          str("%")
          genLocal(pointee)
        }
        str(", ")
        rep(indexes, sep = ", ")(genVal)

        if (!useOpaquePointers) {
          newline()
          genBind()
          str("bitcast ")
          genType(ty.elemty(indexes.tail))
          str("* %")
          genLocal(derived)
          str(" to i8*")
        }
        dbgLocalValue(id, Type.Ptr)(inst.pos, inst.scopeId)

      case Op.Stackalloc(ty, n) =>
        val pointee = fresh()

        newline()
        if (useOpaquePointers) genBind()
        else {
          str("%")
          genLocal(pointee)
          str(" = ")
        }
        str("alloca ")
        genType(ty)
        str(", ")
        genVal(n)
        str(", align ")
        str(platform.sizeOfPtr)

        if (!useOpaquePointers) {
          newline()
          genBind()
          str("bitcast ")
          genType(ty)
          str("* %")
          genLocal(pointee)
          str(" to i8*")
        }
        dbgLocalVariable(pointee, ty)(inst.pos, inst.scopeId)

      case _ =>
        newline()
        genBind()
        genOp(op)
        dbgLocalValue(id, ty)(inst.pos, inst.scopeId)

    }

  }

  private[codegen] def genCall(
      genBind: () => Unit,
      call: Op.Call,
      unwind: Next,
      srcPos: Position,
      scopeId: ScopeId
  )(implicit
      fresh: Fresh,
      sb: ShowBuilder,
      metaCtx: MetadataCodeGen.Context,
      defnScopes: DefnScopes
  ): Unit = {
    import sb._

    /** There are situations where the position is empty, for example in
     *  situations where a null check is generated (and the function call is
     *  throwNullPointer) in this case we can only use NoPosition
     */
    val dbgPosition = toDILocation(srcPos, scopeId)
    def genDbgPosition() = dbg(",", dbgPosition)

    call match {
      case Op.Call(ty, Val.Global(pointee: Global.Member, _), args)
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
        if (unwind eq Next.None) genDbgPosition()
        else {
          str(" to label %")
          currentBlockSplit += 1
          genBlockSplitName()
          str(" unwind ")
          genNext(unwind)
          genDbgPosition()

          unindent()
          genBlockHeader()
          indent()
        }

      case Op.Call(ty, ptr, args) =>
        val Type.Function(_, resty) = ty

        val pointee = fresh()

        if (!useOpaquePointers) {
          newline()
          str("%")
          genLocal(pointee)
          str(" = bitcast ")
          genVal(ptr)
          str(" to ")
          genType(ty)
          str("*")
        }

        newline()
        genBind()
        str(if (unwind ne Next.None) "invoke " else "call ")
        genCallFunctionType(ty)
        str(" ")
        if (useOpaquePointers) genJustVal(ptr)
        else {
          str("%")
          genLocal(pointee)
        }
        str("(")
        rep(args, sep = ", ")(genCallArgument)
        str(")")
        if (unwind eq Next.None) genDbgPosition()
        else {
          str(" to label %")
          currentBlockSplit += 1
          genBlockSplitName()
          str(" unwind ")
          genNext(unwind)
          genDbgPosition()

          unindent()
          genBlockHeader()
          indent()
        }
    }
  }

  private[codegen] def genCallFunctionType(
      ty: Type
  )(implicit sb: ShowBuilder): Unit = {
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

  private[codegen] def genCallArgument(
      v: Val
  )(implicit sb: ShowBuilder): Unit = {
    import sb._
    v match {
      case Val.Local(_, refty: Type.RefKind) =>
        val (nonnull, deref, size) = toDereferenceable(refty)
        // Primitive unit value cannot be passed as argument, probably BoxedUnit is expected
        if (refty == Type.Unit) genType(Type.Ptr)
        else genType(refty)
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

  private[codegen] def genOp(op: Op)(implicit sb: ShowBuilder): Unit = {
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
        genConv(conv, v.ty, ty)
        str(" ")
        genVal(v)
        str(" to ")
        genType(ty)
      case Op.Fence(syncAttrs) =>
        str("fence ")
        genSyncAttrs(syncAttrs)

      case op =>
        unsupported(op)
    }
  }

  private def genSyncAttrs(
      attrs: SyncAttrs
  )(implicit sb: ShowBuilder): Unit = {
    import sb._
    val SyncAttrs(memoryOrder, _) = attrs
    str(memoryOrder match {
      case MemoryOrder.Unordered => "unordered"
      case MemoryOrder.Monotonic => "monotonic"
      case MemoryOrder.Acquire   => "acquire"
      case MemoryOrder.Release   => "release"
      case MemoryOrder.AcqRel    => "acq_rel"
      case MemoryOrder.SeqCst    => "seq_cst"
    })
  }

  private[codegen] def genNext(next: Next)(implicit sb: ShowBuilder): Unit = {
    import sb._
    next match {
      case Next.Case(v, next) =>
        genVal(v)
        str(", label %")
        genLocal(next.id)
        str(".0")
      case Next.Unwind(Val.Local(exc, _), _) =>
        str("label %_")
        str(exc.id)
        str(".landingpad")
      case next =>
        str("label %")
        genLocal(next.id)
        str(".0")
    }
  }

  private[codegen] def genConv(conv: Conv, fromType: Type, toType: Type)(
      implicit sb: ShowBuilder
  ): Unit = conv match {
    case Conv.ZSizeCast | Conv.SSizeCast =>
      val fromSize = fromType match {
        case Type.Size             => platform.sizeOfPtrBits
        case Type.FixedSizeI(s, _) => s
        case o                     => unsupported(o)
      }

      val toSize = toType match {
        case Type.Size             => platform.sizeOfPtrBits
        case Type.FixedSizeI(s, _) => s
        case o                     => unsupported(o)
      }

      val castOp =
        if (fromSize == toSize) "bitcast"
        else if (fromSize > toSize) "trunc"
        else if (conv == Conv.ZSizeCast) "zext"
        else "sext"

      sb.str(castOp)

    case o => sb.str(o.show)
  }

  private[codegen] def genAttr(attr: Attr)(implicit sb: ShowBuilder): Unit =
    sb.str(attr.show)

}

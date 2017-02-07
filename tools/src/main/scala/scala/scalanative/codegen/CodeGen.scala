package scala.scalanative
package codegen

import java.{lang => jl}
import java.nio.ByteBuffer
import scala.collection.mutable
import util.{ShowBuilder, unsupported}
import optimizer.analysis.ControlFlow.{Graph => CFG, Block, Edge}
import nir._

sealed trait CodeGen {

  /** Generate code for the assembly to given byte buffer. */
  def gen(buffer: ByteBuffer): Unit
}

object CodeGen {

  /** Create a new code generator for given assembly. */
  def apply(config: tools.Config, assembly: Seq[Defn]): CodeGen =
    new Impl(config.target, assembly)

  private final class Impl(target: String, assembly: Seq[Defn])
      extends CodeGen {
    import Impl._

    var currentBlockName: Local = _
    var currentBlockSplit: Int  = _
    val globals = assembly.collect {
      case Defn.Var(_, n, ty, _)     => n -> ty
      case Defn.Const(_, n, ty, _)   => n -> ty
      case Defn.Declare(_, n, sig)   => n -> sig
      case Defn.Define(_, n, sig, _) => n -> sig
    }.toMap
    val fresh   = new Fresh("gen")
    val builder = new ShowBuilder
    import builder._

    def gen(buffer: java.nio.ByteBuffer) = {
      genDefns(assembly)
      buffer.put(builder.toString.getBytes)
    }

    def genDefns(defns: Seq[Defn]): Unit = {
      if (target.nonEmpty) {
        str("target triple = \"")
        str(target)
        str("\"")
        newline()
      }

      genPrelude()

      defns
        .sortBy {
          case _: Defn.Struct  => 1
          case _: Defn.Const   => 2
          case _: Defn.Var     => 3
          case _: Defn.Declare => 4
          case _: Defn.Define  => 5
          case _               => -1
        }
        .foreach { defn =>
          newline()
          genDefn(defn)
        }
    }

    def genPrelude(): Unit = {
      line("declare i32 @llvm.eh.typeid.for(i8*)")
      line("declare i32 @__gxx_personality_v0(...)")
      line("declare i8* @__cxa_begin_catch(i8*)")
      line("declare void @__cxa_end_catch()")
      line(
        "@_ZTIN11scalanative16ExceptionWrapperE = external constant { i8*, i8*, i8* }")
    }

    def genDefn(defn: Defn): Unit = defn match {
      case Defn.Struct(attrs, name, tys) =>
        genStruct(attrs, name, tys)
      case Defn.Var(attrs, name, ty, rhs) =>
        genGlobalDefn(name, attrs.isExtern, isConst = false, ty, rhs)
      case Defn.Const(attrs, name, ty, rhs) =>
        genGlobalDefn(name, attrs.isExtern, isConst = true, ty, rhs)
      case Defn.Declare(attrs, name, sig) =>
        genFunctionDefn(attrs, name, sig, Seq())
      case Defn.Define(attrs, name, sig, blocks) =>
        genFunctionDefn(attrs, name, sig, blocks)
      case defn =>
        unsupported(defn)
    }

    def genStruct(attrs: Attrs, name: Global, tys: Seq[Type]): Unit = {
      str("%")
      genGlobal(name)
      str(" = type {")
      rep(tys, sep = ", ")(genType)
      str("}")
    }

    def genGlobalDefn(name: nir.Global,
                      isExtern: Boolean,
                      isConst: Boolean,
                      ty: nir.Type,
                      rhs: nir.Val): Unit = {
      str("@")
      genGlobal(name)
      str(" = ")
      str(if (isExtern) "external " else "")
      str(if (isConst) "constant" else "global")
      str(" ")
      rhs match {
        case Val.None => genType(ty)
        case _        => genVal(rhs)
      }
    }

    def genFunctionDefn(attrs: Attrs,
                        name: Global,
                        sig: Type,
                        insts: Seq[Inst]): Unit = {
      val Type.Function(argtys, retty) = sig

      val isDecl = insts.isEmpty

      str(if (isDecl) "declare " else "define ")
      genType(retty)
      str(" @")
      genGlobal(name)
      str("(")
      if (isDecl) {
        rep(argtys, sep = ", ")(genType)
      } else {
        insts.head match {
          case Inst.Label(_, params) =>
            rep(params, sep = ", ")(genVal)
        }
      }
      str(")")
      if (attrs.inline ne Attr.MayInline) {
        str(" ")
        genAttr(attrs.inline)
      }
      if (!attrs.isExtern && !isDecl) {
        str(" ")
        str(gxxpersonality)
      }
      if (!isDecl) {
        str(" {")
        val cfg = CFG(insts)
        cfg.foreach { block =>
          genBlock(block)(cfg)
        }
        newline()
        str("}")
      }
    }

    def genBlock(block: Block)(implicit cfg: CFG): Unit = {
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

    def genBlockPrologue(block: Block)(implicit cfg: CFG): Unit = {
      val params = block.params

      if (block.isEntry) {
        ()
      } else if (block.isRegular) {
        params.zipWithIndex.foreach {
          case (Val.Local(name, ty), n) =>
            newline()
            str("%")
            genLocal(name)
            str(" = phi ")
            genType(ty)
            str(" ")
            rep(block.inEdges, sep = ", ") { edge =>
              str("[")
              edge match {
                case Edge(from, _, Next.Label(_, vals)) =>
                  genJustVal(vals(n))
                  str(", %")
                  genLocal(from.name)
                  str(".")
                  str(from.splitCount)
              }
              str("]")
            }
        }
      } else if (block.isExceptionHandler) {
        val Seq(Val.Local(exc, _)) = params

        val rec, r0, r1, id, cmp = fresh().show
        val fail, succ           = fresh().show.substring(1)
        val w0, w1, w2           = fresh().show

        def line(s: String) = { newline(); str(s) }

        line(s"$rec = $landingpad")
        line(s"$r0 = extractvalue $excrecty $rec, 0")
        line(s"$r1 = extractvalue $excrecty $rec, 1")
        line(s"$id = $typeid")
        line(s"$cmp = icmp eq i32 $r1, $id")
        line(s"br i1 $cmp, label %$succ, label %$fail")
        unindent()
        line(s"$fail:")
        indent()
        line(s"resume $excrecty $rec")
        unindent()
        line(s"$succ:")
        indent()
        line(s"$w0 = call i8* @__cxa_begin_catch(i8* $r0)")
        line(s"$w1 = bitcast i8* $w0 to i8**")
        line(s"$w2 = getelementptr i8*, i8** $w1, i32 1")
        line(s"${exc.show} = load i8*, i8** $w2")
        line(s"call void @__cxa_end_catch()")
      }
    }

    def genType(ty: Type): Unit = ty match {
      case Type.Void   => str("void")
      case Type.Vararg => str("...")
      case Type.Ptr    => str("i8*")
      case Type.Bool   => str("i1")
      case Type.I8     => str("i8")
      case Type.I16    => str("i16")
      case Type.I32    => str("i32")
      case Type.I64    => str("i64")
      case Type.F32    => str("float")
      case Type.F64    => str("double")
      case Type.Array(ty, n) =>
        str("[")
        str(n)
        str(" x ")
        genType(ty)
        str("]")
      case Type.Function(args, ret) =>
        genType(ret)
        str(" (")
        rep(args, sep = ", ")(genType)
        str(")")
      case Type.Struct(Global.None, tys) =>
        str("{ ")
        rep(tys, sep = ", ")(genType)
        str(" }")
      case Type.Struct(name, _) =>
        str("%")
        genGlobal(name)
      case ty =>
        unsupported(ty)
    }

    def genJustVal(v: Val): Unit = v match {
      case Val.True      => str("true")
      case Val.False     => str("false")
      case Val.Zero(ty)  => str("zeroinitializer")
      case Val.Undef(ty) => str("undef")
      case Val.I8(v)     => str(v)
      case Val.I16(v)    => str(v)
      case Val.I32(v)    => str(v)
      case Val.I64(v)    => str(v)
      case Val.F32(v)    => genFloatHex(v)
      case Val.F64(v)    => genDoubleHex(v)
      case Val.Struct(_, vs) =>
        str("{ ")
        rep(vs, sep = ", ")(genVal)
        str(" }")
      case Val.Array(_, vs) =>
        str("[ ")
        rep(vs, sep = ", ")(genVal)
        str(" ]")
      case Val.Chars(v) =>
        str("c\"")
        str(v)
        str("\\00\"")
      case Val.Local(n, ty) =>
        str("%")
        genLocal(n)
      case Val.Global(n, ty) =>
        str("bitcast (")
        genType(globals(n))
        str("* @")
        genGlobal(n)
        str(" to i8*)")
      case _ =>
        unsupported(v)
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

    def genJustGlobal(g: Global): Unit = g match {
      case Global.None =>
        unsupported(g)
      case Global.Top(id) =>
        str(id)
      case Global.Member(n, id) =>
        genJustGlobal(n)
        str("::")
        str(id)
    }

    def genGlobal(g: Global): Unit = {
      str("\"")
      genJustGlobal(g)
      str("\"")
    }

    def genLocal(local: Local): Unit = local match {
      case Local(scope, id) =>
        str(scope)
        str(".")
        str(id)
    }

    def genInst(inst: Inst): Unit = inst match {
      case inst: Inst.Let =>
        genLet(inst)

      case Inst.Unreachable =>
        newline()
        str("unreachable")

      case Inst.Ret(Val.None) =>
        newline()
        str("ret void")

      case Inst.Ret(value) =>
        newline()
        str("ret ")
        genVal(value)

      case Inst.Jump(next) =>
        newline()
        str("br ")
        genNext(next)

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

      case Inst.None =>
        ()

      case cf =>
        unsupported(cf)
    }

    def genLet(inst: Inst.Let): Unit = {
      def isVoid(ty: Type): Boolean =
        ty == Type.Void || ty == Type.Unit || ty == Type.Nothing

      val op   = inst.op
      val name = inst.name

      def genBind() =
        if (!isVoid(op.resty)) {
          str("%")
          genLocal(name)
          str(" = ")
        }

      op match {
        case call: Op.Call =>
          genCall(genBind, call)

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
          if (n ne Val.None) {
            str(", ")
            genVal(n)
          }

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

    def genCall(genBind: () => Unit, call: Op.Call): Unit = call match {
      case Op.Call(ty, Val.Global(pointee, _), args, Next.None) =>
        val Type.Function(argtys, _) = ty

        newline()
        genBind()
        str("call ")
        genType(ty)
        str(" @")
        genGlobal(pointee)
        str("(")
        rep(args, sep = ", ")(genVal)
        str(")")

      case Op.Call(ty, Val.Global(pointee, _), args, unwind) =>
        val Type.Function(argtys, _) = ty

        val succ = fresh()

        newline()
        genBind()
        str("invoke ")
        genType(ty)
        str(" @")
        genGlobal(pointee)
        str("(")
        rep(args, sep = ", ")(genVal)
        str(")")
        str(" to label %")
        currentBlockSplit += 1
        genBlockSplitName()
        str(" unwind ")
        genNext(unwind)

        unindent()
        genBlockHeader()
        indent()

      case Op.Call(ty, ptr, args, Next.None) =>
        val Type.Function(argtys, _) = ty

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
        str("call ")
        genType(ty)
        str(" %")
        genLocal(pointee)
        str("(")
        rep(args, sep = ", ")(genVal)
        str(")")

      case Op.Call(ty, ptr, args, unwind) =>
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
        str("invoke ")
        genType(ty)
        str(" %")
        genLocal(pointee)
        str("(")
        rep(args, sep = ", ")(genVal)
        str(")")
        str(" to label %")
        currentBlockSplit += 1
        genBlockSplitName()
        str(" unwind ")
        genNext(unwind)

        unindent()
        genBlockHeader()
        indent()
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
      case Op.Select(cond, v1, v2) =>
        str("select ")
        genVal(cond)
        str(", ")
        genVal(v1)
        str(", ")
        genVal(v2)
      case op =>
        unsupported(op)
    }

    def genNext(next: Next) = next match {
      case Next.Case(v, n) =>
        genVal(v)
        str(", label %")
        genLocal(n)
        str(".0")
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
}

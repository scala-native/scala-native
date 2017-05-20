package scala.scalanative
package codegen

import java.{lang => jl}
import java.nio.ByteBuffer
import java.nio.file.Paths
import scala.collection.mutable
import scalanative.util.{Scope, ShowBuilder, ShowBuilderPosition, unsupported}
import scalanative.io.{VirtualDirectory, withScratchBuffer}
import scalanative.optimizer.analysis.ControlFlow.{Graph => CFG, Block, Edge}
import scalanative.nir._

object CodeGen {

  private final class Platform(target: String) {
    val isWindows = target.contains("indows")
    val ehWrapperString =
      if (isWindows) "@\"\\01??_R0?AVExceptionWrapper@scalanative@@@8\""
      else "@_ZTIN11scalanative16ExceptionWrapperE"
    val xx_personality =
      if (isWindows) "@__CxxFrameHandler3" else "@__gxx_personality_v0"
    val ehClassName = "%\"class.scalanative::ExceptionWrapper\""
    val ehVar       = "%eslot"
  }

  /** Generate code for given assembly. */
  def apply(config: tools.Config, assembly: Seq[Defn]): Unit =
    Scope { implicit in =>
      val env     = assembly.map(defn => defn.name -> defn).toMap
      val workdir = VirtualDirectory.real(config.workdir)

      def debug(): Unit = {
        val batches = mutable.Map.empty[String, mutable.Buffer[Defn]]
        assembly.foreach { defn =>
          val top = defn.name.top.id
          val key =
            if (top.startsWith("__")) top
            else if (top == "main") "__main"
            else {
              val pkg = top.split("\\.").init.mkString(".")
              if (pkg == "") "__empty"
              else pkg
            }
          if (!batches.contains(key)) {
            batches(key) = mutable.UnrolledBuffer.empty[Defn]
          }
          batches(key) += defn
        }
        batches.par.foreach {
          case (k, defns) =>
            val impl =
              new Impl(config.target,
                       env,
                       defns,
                       workdir,
                       new Platform(config.target))
            val outpath = k + ".ll"
            val buffer  = impl.gen()
            buffer.flip
            workdir.write(Paths.get(outpath), buffer)
        }
      }

      def release(): Unit = {
        val impl = new Impl(config.target,
                            env,
                            assembly,
                            workdir,
                            new Platform(config.target))
        val buffer = impl.gen()
        buffer.flip
        workdir.write(Paths.get("out.ll"), buffer)
      }

      config.mode match {
        case tools.Mode.Debug   => debug()
        case tools.Mode.Release => release()
      }
    }

  private final class Impl(target: String,
                           env: Map[Global, Defn],
                           defns: Seq[Defn],
                           workdir: VirtualDirectory,
                           platform: Platform) {
    import Impl._

    var currentBlockName: Local            = _
    var currentBlockSplit: Int             = _
    var ehVarPosition: ShowBuilderPosition = _ // position to insert for exception handler variable

    val fresh     = new Fresh("gen")
    val deps      = mutable.Set.empty[Global]
    val generated = mutable.Set.empty[Global]
    val builder   = new ShowBuilder
    import builder._

    def gen(): ByteBuffer = {
      genDefns(defns)
      val body = builder.toString.getBytes("UTF-8")
      builder.clear
      genPrelude()
      genDeps()
      val prelude = builder.toString.getBytes("UTF-8")
      val buffer  = ByteBuffer.allocate(prelude.length + body.length)
      buffer.put(prelude)
      buffer.put(body)
    }

    def genDeps() = deps.foreach { n =>
      if (!generated.contains(n)) {
        newline()
        genDefn {
          env(n) match {
            case defn: Defn.Struct =>
              defn
            case defn @ Defn.Var(attrs, _, _, _) =>
              defn.copy(attrs.copy(isExtern = true), rhs = Val.None)
            case defn @ Defn.Const(attrs, _, ty, _) =>
              defn.copy(attrs.copy(isExtern = true), rhs = Val.None)
            case defn @ Defn.Declare(attrs, _, _) =>
              defn.copy(attrs.copy(isExtern = true))
            case defn @ Defn.Define(attrs, _, _, _) =>
              defn.copy(attrs.copy(isExtern = true), insts = Seq())
          }
        }
      }
    }

    def touch(n: Global): Unit =
      deps += n

    def lookup(n: Global): Type = {
      touch(n)
      env(n) match {
        case Defn.Var(_, _, ty, _)     => ty
        case Defn.Const(_, _, ty, _)   => ty
        case Defn.Declare(_, _, sig)   => sig
        case Defn.Define(_, _, sig, _) => sig
      }
    }

    def genDefns(defns: Seq[Defn]): Unit =
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

    def genPrelude(): Unit = {
      if (target.nonEmpty) {
        str("target triple = \"")
        str(target)
        str("\"")
        newline()
      }
      line("declare i32 @llvm.eh.typeid.for(i8*)")
      line(s"declare i32 ${platform.xx_personality}(...)")
      if (platform.isWindows) {
        line("%struct.__std_exception_data = type { i8*, i8 }")
        line(
          "%\"class.std::exception\" = type { i32 (...)**, %struct.__std_exception_data }")
        line(
          "%\"class.scalanative::ExceptionWrapper\" = type { %\"class.std::exception\", i8* }")
        line("%rtti.TypeDescriptor34 = type { i8**, i8*, [35 x i8] }")
        line("@\"\\01??_7type_info@@6B@\" = external constant i8*")
        line("$\"\\01??_R0?AVExceptionWrapper@scalanative@@@8\" = comdat any")
        line(
          "@\"\\01??_R0?AVExceptionWrapper@scalanative@@@8\" = linkonce_odr global %rtti.TypeDescriptor34 { i8** @\"\\01??_7type_info@@6B@\", i8* null, [35 x i8] c\".?AVExceptionWrapper@scalanative@@\\00\" }, comdat")
        line(
          "@\"\\01?eglob@@3PEAVExceptionWrapper@scalanative@@EA\" = external global %\"class.scalanative::ExceptionWrapper\"*")
      } else {
        //todo: clang crashes when I use general intrinsics
        line("declare i8* @__cxa_begin_catch(i8*)") //should be: line("declare void @llvm.eh.begincatch(i8*, i8*)")
        line("declare void @__cxa_end_catch()")     //should be: line("declare void @llvm.eh.endcatch()")
        line(
          s"${platform.ehWrapperString} = external constant { i8*, i8*, i8* }")
      }
    }

    def genDefn(defn: Defn): Unit = {
      defn match {
        case Defn.Struct(attrs, name, tys) =>
          genStruct(attrs, name, tys)
        case Defn.Var(attrs, name, ty, rhs) =>
          genGlobalDefn(attrs, name, isConst = false, ty, rhs)
        case Defn.Const(attrs, name, ty, rhs) =>
          genGlobalDefn(attrs, name, isConst = true, ty, rhs)
        case Defn.Declare(attrs, name, sig) =>
          genFunctionDefn(attrs, name, sig, Seq())
        case Defn.Define(attrs, name, sig, blocks) =>
          genFunctionDefn(attrs, name, sig, blocks)
        case defn =>
          unsupported(defn)
      }
      generated += defn.name
    }

    def genStruct(attrs: Attrs, name: Global, tys: Seq[Type]): Unit = {
      str("%")
      genGlobal(name)
      str(" = type {")
      rep(tys, sep = ", ")(genType)
      str("}")
    }

    def genGlobalDefn(attrs: Attrs,
                      name: nir.Global,
                      isConst: Boolean,
                      ty: nir.Type,
                      rhs: nir.Val): Unit = {
      str("@")
      genGlobal(name)
      str(" = ")
      str(if (attrs.isExtern) "external " else "")
      str(if (isConst) "constant" else "global")
      str(" ")
      rhs match {
        case Val.None => genType(ty)
        case rhs      => genVal(rhs)
      }
      attrs.align.foreach { value =>
        str(", align ")
        str(value)
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
        str(gxxpersonality(platform))
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
      if (platform.isWindows && block.pred.isEmpty) {
        ehVarPosition = currentPosition
      }
      genBlockPrologue(block, ehVarPosition)
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

    def genBlockPrologue(block: Block, ehVarPosition: ShowBuilderPosition)(
        implicit cfg: CFG): Unit = {
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
        val exc = params match {
          case Seq()                  => fresh()
          case Seq(Val.Local(exc, _)) => exc
        }

        val rec, r0, r1, id, cmp = fresh().show
        val fail, succ, catch1   = fresh().show.substring(1)
        val w0, w1, w2, cpad     = fresh().show

        def line(s: String) = { newline(); str(s) }

        if (platform.isWindows) {
          line(
            s"$rec = catchswitch within none [label %$catch1] unwind to caller")
          unindent()
          line(s"$catch1:")
          indent()
          line(
            s"$cpad = catchpad within $rec [%rtti.TypeDescriptor34* ${platform.ehWrapperString}, i32 8, ${platform.ehClassName}** ${platform.ehVar}]")
          line(
            s"$w1 = load ${platform.ehClassName}*, ${platform.ehClassName}** ${platform.ehVar}")
          line(
            s"$w2 = getelementptr inbounds ${platform.ehClassName}, ${platform.ehClassName}* $w1, i32 0, i32 1")
          line(s"${exc.show} = load i8*, i8** $w2, align 8")
          line(s"catchret from $cpad to label %$succ")
          unindent()
          line(s"$succ:")
          indent()
          // insert alloca to block entry area
          insertLine(ehVarPosition,
                     s"${platform.ehVar} = alloca ${platform.ehClassName}*")
        } else {
          line(s"$rec = ${landingpad(platform)}")
          line(s"$r0 = extractvalue $excrecty $rec, 0")
          line(s"$r1 = extractvalue $excrecty $rec, 1")
          line(s"$id = ${typeid(platform)}")
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
    }

    def genType(ty: Type): Unit = ty match {
      case Type.Void   => str("void")
      case Type.Vararg => str("...")
      case Type.Ptr    => str("i8*")
      case Type.Bool   => str("i1")
      case i: Type.I   => str("i"); str(i.width)
      case Type.Float  => str("float")
      case Type.Double => str("double")
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
        touch(name)
        str("%")
        genGlobal(name)
      case ty =>
        unsupported(ty)
    }

    def genJustVal(v: Val): Unit = v match {
      case Val.True      => str("true")
      case Val.False     => str("false")
      case Val.Null      => str("null")
      case Val.Zero(ty)  => str("zeroinitializer")
      case Val.Undef(ty) => str("undef")
      case Val.Byte(v)   => str(v)
      case Val.Short(v)  => str(v)
      case Val.Int(v)    => str(v)
      case Val.Long(v)   => str(v)
      case Val.Float(v)  => genFloatHex(v)
      case Val.Double(v) => genDoubleHex(v)
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
        genType(lookup(n))
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
      case Global.Member(Global.Top("__extern"), id) =>
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

        case Op.Load(ty, ptr, isVolatile) =>
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
          if (isVolatile) {
            str("volatile ")
          }
          genType(ty)
          str(", ")
          genType(ty)
          str("* %")
          genLocal(pointee)

        case Op.Store(ty, ptr, value, isVolatile) =>
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
          if (isVolatile) {
            str("volatile ")
          }
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

        touch(pointee)

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

        touch(pointee)

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
    def gxxpersonality(platform: Platform) =
      s"personality i8* bitcast (i32 (...)* ${platform.xx_personality} to i8*)"
    val excrecty = "{ i8*, i32 }"
    def landingpad(platform: Platform) =
      s"landingpad { i8*, i32 } catch i8* bitcast ({ i8*, i8*, i8* }* ${platform.ehWrapperString} to i8*)"
    def typeid(platform: Platform) =
      s"call i32 @llvm.eh.typeid.for(i8* bitcast ({ i8*, i8*, i8* }* ${platform.ehWrapperString} to i8*))"
  }
}

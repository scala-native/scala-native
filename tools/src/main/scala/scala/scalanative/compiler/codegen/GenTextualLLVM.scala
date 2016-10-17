package scala.scalanative
package compiler
package codegen

import java.{lang => jl}

import scala.collection.mutable
import util.{Show, sh, unreachable, unsupported}
import util.Show.{Indent => i, Newline => nl, Repeat => r, Sequence => s, Unindent => ui}
import compiler.analysis.ControlFlow.{Graph => CFG, Block, Edge}
import nir.Shows.brace
import nir._

class GenTextualLLVM(assembly: Seq[Defn]) extends GenShow(assembly) {
  private val fresh = new Fresh("gen")
  private val globals = assembly.collect {
    case Defn.Var(_, n, ty, _)     => n -> ty
    case Defn.Const(_, n, ty, _)   => n -> ty
    case Defn.Declare(_, n, sig)   => n -> sig
    case Defn.Define(_, n, sig, _) => n -> sig
  }.toMap
  private val prelude = Seq(
      sh"declare i32 @llvm.eh.typeid.for(i8*)",
      sh"declare i32 @__gxx_personality_v0(...)",
      sh"declare i8* @__cxa_begin_catch(i8*)",
      sh"declare void @__cxa_end_catch()",
      sh"@_ZTIN11scalanative16ExceptionWrapperE = external constant { i8*, i8*, i8* }"
  )
  private val gxxpersonality =
    sh"personality i8* bitcast (i32 (...)* @__gxx_personality_v0 to i8*)"
  private val excrecty = sh"{ i8*, i32 }"
  private val landingpad =
    sh"landingpad { i8*, i32 } catch i8* bitcast ({ i8*, i8*, i8* }* @_ZTIN11scalanative16ExceptionWrapperE to i8*)"
  private val typeid =
    sh"call i32 @llvm.eh.typeid.for(i8* bitcast ({ i8*, i8*, i8* }* @_ZTIN11scalanative16ExceptionWrapperE to i8*))"

  implicit val showDefns: Show[Seq[Defn]] = Show { defns =>
    val sorted = defns.sortBy {
      case _: Defn.Struct  => 1
      case _: Defn.Const   => 2
      case _: Defn.Var     => 3
      case _: Defn.Declare => 4
      case _: Defn.Define  => 5
      case _               => -1
    }

    r(prelude ++: sorted.map(d => sh"$d"), sep = nl(""))
  }

  implicit val showDefn: Show[Defn] = Show {
    case Defn.Var(attrs, name, ty, rhs) =>
      showGlobalDefn(name, attrs.isExtern, isConst = false, ty, rhs)
    case Defn.Const(attrs, name, ty, rhs) =>
      showGlobalDefn(name, attrs.isExtern, isConst = true, ty, rhs)
    case Defn.Declare(attrs, name, sig) =>
      showFunctionDefn(attrs, name, sig, Seq())
    case Defn.Define(attrs, name, sig, blocks) =>
      showFunctionDefn(attrs, name, sig, blocks)
    case Defn.Struct(attrs, name, tys) =>
      sh"%$name = type {${r(tys, sep = ", ")}}"
    case defn =>
      unsupported(defn)
  }

  def showGlobalDefn(name: nir.Global,
                     isExtern: Boolean,
                     isConst: Boolean,
                     ty: nir.Type,
                     rhs: nir.Val) = {
    val external = if (isExtern) "external " else ""
    val keyword  = if (isConst) "constant" else "global"
    val init = rhs match {
      case Val.None => sh"$ty"
      case _        => sh"$rhs"
    }

    sh"@$name = $external$keyword $init"
  }

  def showFunctionDefn(attrs: Attrs,
                       name: Global,
                       sig: Type,
                       insts: Seq[Inst]) = {
    val Type.Function(argtys, retty) = sig

    val isDecl  = insts.isEmpty
    val keyword = if (isDecl) "declare" else "define"

    def showDefnArg(arg: Arg,
                    value: Val.Local): (Show.Result, Seq[Show.Result]) =
      arg match {
        case Arg(_, None) => (sh"${value: Val}", Seq.empty)
        case Arg(Type.Ptr, Some(PassConv.Byval(pointee))) =>
          val pointer = fresh()
          (sh"$pointee* byval %$pointer",
           Seq(sh"%${value.name} = bitcast $pointee* %$pointer to i8*"))
        case Arg(Type.Ptr, Some(PassConv.Sret(pointee))) =>
          val pointer = fresh()
          (sh"$pointee* sret %$pointer",
           Seq(sh"%${value.name} = bitcast $pointee* %$pointer to i8*"))
        case x => unsupported(x)
      }

    def showDeclArg(arg: Arg): Show.Result = arg match {
      case Arg(ty, None) => sh"$ty"
      case Arg(Type.Ptr, Some(PassConv.Byval(pointee))) =>
        sh"$pointee* byval"
      case Arg(Type.Ptr, Some(PassConv.Sret(pointee))) =>
        sh"$pointee* sret"
      case x => unsupported(x)
    }

    val (params, preInstrs) =
      if (isDecl) (r(argtys.map(showDeclArg), sep = ", "), Seq())
      else {
        val params = insts.head match {
          case Inst.Label(_, params) => params
          case _                     => unreachable
        }
        val results = (argtys zip params).map((showDefnArg _).tupled)
        (r(results.map(_._1), sep = ", "), results.flatMap(_._2))
      }
    val postattrs: Seq[Attr] =
      if (attrs.inline != Attr.MayInline) Seq(attrs.inline) else Seq()
    val personality = if (attrs.isExtern || isDecl) s() else gxxpersonality
    val body =
      if (isDecl) s()
      else {
        implicit val cfg = CFG(insts)
        val showblocks = cfg.map { block =>
          val isEntry = block eq cfg.entry
          showBlock(block,
                    isEntry = isEntry,
                    if (isEntry) r(preInstrs.map(nl)) else s())
        }
        s(" ", brace(r(showblocks)))
      }

    sh"$keyword $retty @$name($params)$postattrs$personality$body"
  }

  def showBlock(block: Block, isEntry: Boolean, preInstructions: Show.Result)(
      implicit cfg: CFG): Show.Result = {
    val Block(name, params, insts) = block

    val body  = r(showInsts(insts).map(i(_)))
    val label = sh"${block.name}:"
    val prologue: Show.Result =
      if (isEntry) s()
      else {
        val shows = block.pred match {
          case ExSucc(branches) =>
            params.zipWithIndex.map {
              case (Val.Local(name, ty), n) =>
                val branchshows = branches.map {
                  case (from, shows) =>
                    sh"[${shows(n)}, %$from]"
                }

                i(sh"%$name = phi $ty ${r(branchshows, sep = ", ")}")
            }

          case ExFail() =>
            val Seq(Val.Local(exc, _)) = params

            val rec, r0, r1, id, cmp, fail, succ, w0, w1, w2 = fresh()

            Seq(
                i(sh"%$rec = $landingpad"),
                i(sh"%$r0 = extractvalue $excrecty %$rec, 0"),
                i(sh"%$r1 = extractvalue $excrecty %$rec, 1"),
                i(sh"%$id = $typeid"),
                i(sh"%$cmp = icmp eq i32 %$r1, %$id"),
                i(sh"br i1 %$cmp, label %$succ, label %$fail"),
                nl(sh"$fail:"),
                i(sh"resume $excrecty %$rec"),
                nl(sh"$succ:"),
                i(sh"%$w0 = call i8* @__cxa_begin_catch(i8* %$r0)"),
                i(sh"%$w1 = bitcast i8* %$w0 to i8**"),
                i(sh"%$w2 = getelementptr i8*, i8** %$w1, i32 1"),
                i(sh"%$exc = load i8*, i8** %$w2"),
                i(sh"call void @__cxa_end_catch()")
            )
        }

        r(shows.map(s(_)))
      }

    sh"${nl("")}$label$prologue$preInstructions${nl("")}$body"
  }

  implicit val showType: Show[Type] = Show {
    case Type.Void                     => "void"
    case Type.Vararg                   => "..."
    case Type.Ptr                      => "i8*"
    case Type.Bool                     => "i1"
    case Type.I8                       => "i8"
    case Type.I16                      => "i16"
    case Type.I32                      => "i32"
    case Type.I64                      => "i64"
    case Type.F32                      => "float"
    case Type.F64                      => "double"
    case Type.Array(ty, n)             => sh"[$n x $ty]"
    case Type.Function(args, ret)      => sh"$ret (${r(args, sep = ", ")})"
    case Type.Struct(Global.None, tys) => sh"{ ${r(tys, sep = ", ")} }"
    case Type.Struct(name, _)          => sh"%$name"
    case ty                            => unsupported(ty)
  }

  implicit val showArg: Show[Arg] = Show {
    case Arg(ty, None)                                => sh"$ty"
    case Arg(Type.Ptr, Some(PassConv.Byval(pointee))) => sh"$pointee*"
    case Arg(Type.Ptr, Some(PassConv.Sret(pointee)))  => sh"$pointee*"
    case arg                                          => unsupported(arg)
  }

  def justVal(v: Val): Show.Result = v match {
    case Val.True          => "true"
    case Val.False         => "false"
    case Val.Zero(ty)      => "zeroinitializer"
    case Val.Undef(ty)     => "undef"
    case Val.I8(v)         => v.toString
    case Val.I16(v)        => v.toString
    case Val.I32(v)        => v.toString
    case Val.I64(v)        => v.toString
    case Val.F32(v)        => llvmFloatHex(v)
    case Val.F64(v)        => llvmDoubleHex(v)
    case Val.Struct(_, vs) => sh"{ ${r(vs, sep = ", ")} }"
    case Val.Array(_, vs)  => sh"[ ${r(vs, sep = ", ")} ]"
    case Val.Chars(v)      => s("c\"", v, "\\00", "\"")
    case Val.Local(n, ty)  => sh"%$n"
    case Val.Global(n, ty) => sh"bitcast (${globals(n)}* @$n to i8*)"
    case _                 => unsupported(v)
  }

  def llvmFloatHex(value: Float): String =
    "0x" + jl.Long.toHexString(jl.Double.doubleToRawLongBits(value.toDouble))

  def llvmDoubleHex(value: Double): String =
    "0x" + jl.Long.toHexString(jl.Double.doubleToRawLongBits(value))

  implicit val showVal: Show[Val] = Show { v =>
    val justv = justVal(v)
    val ty    = v.ty

    sh"$ty $justv"
  }

  private def quoted(sh: Show.Result) = s("\"", sh, "\"")

  private def justGlobal(g: Global): Show.Result = g match {
    case Global.None          => unsupported(g)
    case Global.Top(id)       => id
    case Global.Member(n, id) => s(justGlobal(n), "::", id)
  }

  implicit val showGlobal: Show[Global] = Show { g =>
    quoted(justGlobal(g))
  }

  implicit val showLocal: Show[Local] = Show {
    case Local(scope, id) => sh"$scope.$id"
  }

  def showInsts(insts: Seq[Inst])(implicit cfg: CFG): Seq[Show.Result] = {
    val buf = mutable.UnrolledBuffer.empty[Show.Result]
    insts.foreach(showInst(buf, _))
    buf.toSeq
  }

  def showInst(buf: mutable.UnrolledBuffer[Show.Result], inst: Inst)(
      implicit cfg: CFG): Unit =
    inst match {
      case _: Inst.Label =>
        unreachable

      case inst: Inst.Let =>
        showLet(buf, inst)

      case Inst.Unreachable =>
        buf += sh"unreachable"

      case Inst.Ret(Val.None) =>
        buf += sh"ret void"

      case Inst.Ret(value) =>
        buf += sh"ret $value"

      case Inst.Jump(next) =>
        buf += sh"br $next"

      case Inst.If(cond, thenp, elsep) =>
        buf += sh"br $cond, $thenp, $elsep"

      case Inst.Switch(scrut, default, cases) =>
        buf += sh"switch $scrut, $default [${r(cases.map(i(_)))}${nl("]")}"

      case Inst.Invoke(ty, Val.Global(pointee, _), args, succ, fail) =>
        val Type.Function(_, resty) = ty

        val name = cfg.find(succ.name).params.headOption.map(_.name)
        val bind = name.fold(sh"") { name =>
          sh"%$name.succ = "
        }

        buf +=
        sh"${bind}invoke $ty @$pointee(${r(args, sep = ", ")}) to $succ unwind $fail"

      case Inst.Invoke(ty, ptr, args, succ, fail) =>
        val Type.Function(_, resty) = ty

        val name = cfg.find(succ.name).params.headOption.map(_.name)
        val bind = name.fold(sh"") { name =>
          sh"%$name.succ = "
        }
        val pointee = fresh()

        buf += sh"%$pointee = bitcast $ptr to $ty*"
        buf +=
        sh"${bind}invoke $ty %$pointee(${r(args, sep = ", ")}) to $succ unwind $fail"

      case Inst.None =>
        ()

      case cf =>
        unsupported(cf)
    }

  def showCallArgs(args: Seq[Arg],
                   vals: Seq[Val]): (Seq[Show.Result], Seq[Show.Result]) = {
    val res = args.map(Some(_)).zipAll(vals, None, Val.None) map {
      case (_, Val.None) => (Seq.empty, Seq.empty)
      case (Some(Arg(Type.Ptr, Some(PassConv.Byval(pointee)))), v) =>
        val bitcasted = fresh()
        (Seq(sh"%$bitcasted = bitcast $v to $pointee*"),
         Seq(sh"$pointee* %$bitcasted"))
      case (Some(Arg(Type.Ptr, Some(PassConv.Sret(pointee)))), v) =>
        val bitcasted = fresh()
        (Seq(sh"%$bitcasted = bitcast $v to $pointee*"),
         Seq(sh"$pointee* %$bitcasted"))
      case (Some(Arg(_, None)) | None, v) => (Seq(), Seq(sh"$v"))
      case _                              => unsupported()
    }
    (res.flatMap(_._1), res.flatMap(_._2))
  }

  def showLet(buf: mutable.UnrolledBuffer[Show.Result], inst: Inst.Let): Unit = {
    def isVoid(ty: Type): Boolean =
      ty == Type.Void || ty == Type.Unit || ty == Type.Nothing

    val op   = inst.op
    val name = inst.name
    val bind = if (isVoid(op.resty)) s() else sh"%$name = "

    op match {
      case Op.Call(ty, Val.Global(pointee, _), args) =>
        val bind = if (isVoid(op.resty)) s() else sh"%$name = "

        val Type.Function(argtys, _) = ty

        val (preinsts, argshows) = showCallArgs(argtys, args)

        buf ++= preinsts
        buf += sh"${bind}call ${ty: Type} @$pointee(${r(argshows, sep = ", ")})"

      case Op.Call(ty, ptr, args) =>
        val pointee = fresh()
        val bind    = if (isVoid(op.resty)) s() else sh"%$name = "

        val Type.Function(argtys, _) = ty

        val (preinsts, argshows) = showCallArgs(argtys, args)

        buf ++= preinsts
        buf += sh"%$pointee = bitcast $ptr to $ty*"
        buf += sh"${bind}call ${ty: Type} %$pointee(${r(argshows, sep = ", ")})"

      case Op.Load(ty, ptr) =>
        val pointee = fresh()

        buf += sh"%$pointee = bitcast $ptr to $ty*"
        buf += sh"${bind}load $ty, $ty* %$pointee"

      case Op.Store(ty, ptr, value) =>
        val pointee = fresh()

        buf += sh"%$pointee = bitcast $ptr to $ty*"
        buf += sh"${bind}store $value, $ty* %$pointee"

      case Op.Elem(ty, ptr, indexes) =>
        val pointee = fresh()
        val derived = fresh()

        buf += sh"%$pointee = bitcast $ptr to $ty*"
        buf +=
        sh"%$derived = getelementptr $ty, $ty* %$pointee, ${r(indexes, sep = ", ")}"
        buf +=
        sh"${bind}bitcast ${ty.elemty(indexes.tail)}* %$derived to i8*"

      case Op.Stackalloc(ty, n) =>
        val pointee = fresh()
        val elems   = if (n == Val.None) sh"" else sh", $n"

        buf += sh"%$pointee = alloca $ty$elems"
        buf += sh"${bind}bitcast $ty* %$pointee to i8*"

      case _ =>
        buf += sh"${bind}$op"
    }
  }

  implicit val showOp: Show[Op] = Show {
    case Op.Call(_, _, _) =>
      unreachable
    case Op.Load(_, _) =>
      unreachable
    case Op.Store(_, _, _) =>
      unreachable
    case Op.Elem(_, _, _) =>
      unreachable
    case Op.Stackalloc(_, _) =>
      unreachable
    case Op.Extract(aggr, indexes) =>
      sh"extractvalue $aggr, ${r(indexes, sep = ", ")}"
    case Op.Insert(aggr, value, indexes) =>
      sh"insertvalue $aggr, $value, ${r(indexes, sep = ", ")}"
    case Op.Bin(opcode, ty, l, r) =>
      val bin = opcode match {
        case Bin.Iadd => "add"
        case Bin.Isub => "sub"
        case Bin.Imul => "mul"
        case _        => opcode.toString.toLowerCase
      }
      sh"$bin $l, ${justVal(r)}"
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
        case Comp.Feq => "fcmp ueq"
        case Comp.Fne => "fcmp une"
        case Comp.Flt => "fcmp ult"
        case Comp.Fle => "fcmp ule"
        case Comp.Fgt => "fcmp ugt"
        case Comp.Fge => "fcmp uge"
      }
      sh"$cmp $l, ${justVal(r)}"
    case Op.Conv(name, ty, v) =>
      sh"$name $v to $ty"
    case Op.Select(cond, v1, v2) =>
      sh"select $cond, $v1, $v2"
    case op =>
      unsupported(op)
  }

  implicit val showNext: Show[Next] = Show {
    case Next.Case(v, n) => sh"$v, label %$n"
    case next            => sh"label %${next.name}"
  }

  implicit def showConv: Show[Conv] = nir.Shows.showConv

  implicit def showAttrSeq: Show[Seq[Attr]] = nir.Shows.showAttrSeq

  private object ExSucc {
    def unapply(edges: Seq[Edge]): Option[Seq[(Local, Seq[Show.Result])]] = {
      Some(edges.map {
        case Edge(from, to, _: Next.Succ) =>
          val succ = to.params.headOption.map { p =>
            sh"%${p.name}.succ"
          }
          (from.name, succ.toSeq)
        case Edge(from, _, _: Next.Case) =>
          (from.name, Seq())
        case Edge(from, _, Next.Label(_, vals)) =>
          (from.name, vals.map(justVal))
        case Edge(_, _, _: Next.Fail) =>
          return None
      })
    }
  }

  private object ExFail {
    def unapply(edges: Seq[Edge]): Boolean =
      edges.forall {
        case Edge(_, _, _: Next.Fail) => true
        case _                        => false
      }
  }
}

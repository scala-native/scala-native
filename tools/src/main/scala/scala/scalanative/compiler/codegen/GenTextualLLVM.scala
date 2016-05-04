package scala.scalanative
package compiler
package codegen

import java.lang.{Integer => JInt, Long => JLong, Float => JFloat, Double => JDouble}
import scala.collection.mutable
import util.{unsupported, unreachable, sh, Show}
import util.Show.{Sequence => s, Indent => i, Unindent => ui, Repeat => r, Newline => nl}
import compiler.analysis.ControlFlow
import nir.Shows.brace
import nir._

class GenTextualLLVM(assembly: Seq[Defn]) extends GenShow(assembly){
  private val fresh = new Fresh("gen")
  private val globals = assembly.collect {
    case Defn.Var(_, n, ty, _)     => n -> ty
    case Defn.Const(_, n, ty, _)   => n -> ty
    case Defn.Declare(_, n, sig)   => n -> sig
    case Defn.Define(_, n, sig, _) => n -> sig
  }.toMap
  private val prelude = sh"declare i32 @__gxx_personality_v0(...)"
  private val gxxpersonality =
    sh"personality i8* bitcast (i32 (...)* @__gxx_personality_v0 to i8*)"

  implicit val showDefns: Show[Seq[Defn]] = Show { defns =>
    val sorted = defns.sortBy {
      case _: Defn.Struct  => 1
      case _: Defn.Const   => 2
      case _: Defn.Var     => 3
      case _: Defn.Declare => 4
      case _: Defn.Define  => 5
      case _               => -1
    }

    r(prelude +: sorted.map(d => sh"$d"), sep = nl(""))
  }

  implicit val showDefn: Show[Defn] = Show {
    case Defn.Var(attrs, name, ty, rhs) =>
      showGlobalDefn(name, attrs.isExtern, isConst = false, ty, rhs)
    case Defn.Const(attrs, name, ty, rhs) =>
      showGlobalDefn(name, attrs.isExtern, isConst = false, ty, rhs)
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
    val external =
      if (isExtern) "external " else ""
    val keyword =
      if (isConst) "const" else "global"
    val init =
      rhs match {
        case Val.None => sh"$ty"
        case _        => sh"$rhs"
      }

    sh"@$name = $external$keyword $init"
  }

  def showFunctionDefn(attrs: Attrs,
                       name: Global,
                       sig: Type,
                       blocks: Seq[Block]) = {
    val Type.Function(argtys, retty) = sig

    val isDecl = blocks.isEmpty
    val keyword =
      if (isDecl) "declare" else "define"
    val params =
      if (isDecl) r(argtys, sep = ", ")
      else r(blocks.head.params: Seq[Val], sep = ", ")
    val postattrs: Seq[Attr] =
      if (attrs.inline != Attr.MayInline) Seq(attrs.inline) else Seq()
    val personality =
      if (attrs.isExtern || isDecl) s() else gxxpersonality
    val body =
      if (isDecl) s()
      else {
        implicit val cfg = ControlFlow(blocks)
        val blockshows =
          cfg.map { node =>
            showBlock(node.block, node.pred, isEntry = node eq cfg.entry)
          }
        s(" ", brace(i(r(blockshows))))
      }

    sh"$keyword $retty @$name($params)$postattrs$personality$body"
  }

  private lazy val landingpad =
    sh"landingpad { i8*, i32 } catch i8* bitcast ({ i8*, i8*, i32, i8* }* @_ZTIPN11scalanative16ExceptionWrapperE to i8*)"

  def showBlock(block: Block, pred: Seq[ControlFlow.Edge], isEntry: Boolean)(
      implicit cfg: ControlFlow.Graph): Show.Result = {
    val Block(name, params, insts, cf) = block

    val body = r(showInsts(insts, block.cf), sep = nl(""))

    if (isEntry) body
    else {
      val label = ui(sh"${block.name}:")
      val shows = pred match {
        case ExSucc(branches) =>
          params.zipWithIndex.map {
            case (Val.Local(name, ty), i) =>
              val branchshows = branches.map {
                case (from, shows) =>
                  sh"[${shows(i)}, %$from]"
              }
              sh"%$name = phi $ty ${r(branchshows, sep = ", ")}"
          }
        case ExFail() =>
          val Seq(Val.Local(excrec, _)) = params
          Seq(
              sh"%$excrec = $landingpad"
          )
      }
      val prologue = r(shows.map(nl(_)))

      sh"$label$prologue${nl("")}$body"
    }
  }

  implicit val showType: Show[Type] = Show {
    case Type.Void                => "void"
    case Type.Label               => "label"
    case Type.Vararg              => "..."
    case Type.Ptr                 => "i8*"
    case Type.Bool                => "i1"
    case Type.I8                  => "i8"
    case Type.I16                 => "i16"
    case Type.I32                 => "i32"
    case Type.I64                 => "i64"
    case Type.F32                 => "float"
    case Type.F64                 => "double"
    case Type.Array(ty, n)        => sh"[$n x $ty]"
    case Type.Function(args, ret) => sh"$ret (${r(args, sep = ", ")})"
    case Type.Struct(name, _)     => sh"%$name"
    case ty                       => unsupported(ty)
  }

  def justVal(v: Val): Show.Result = v match {
    case Val.True          => "true"
    case Val.False         => "false"
    case Val.Zero(ty)      => "zeroinitializer"
    case Val.I8(v)         => v.toString
    case Val.I16(v)        => v.toString
    case Val.I32(v)        => v.toString
    case Val.I64(v)        => v.toString
    case Val.F32(v)        => v.toString
    case Val.F64(v)        => v.toString
    case Val.Struct(n, vs) => sh"{ ${r(vs, sep = ", ")} }"
    case Val.Array(_, vs)  => sh"[ ${r(vs, sep = ", ")} ]"
    case Val.Chars(v)      => s("c\"", v, "\"")
    case Val.Local(n, ty)  => sh"%$n"
    case Val.Global(n, ty) => sh"bitcast (${globals(n)}* @$n to i8*)"
    case _                 => unsupported(v)
  }

  implicit val showVal: Show[Val] = Show { v =>
    val justv = justVal(v)
    val ty    = v.ty

    sh"$ty $justv"
  }

  private def quoted(sh: Show.Result) = s("\"", sh, "\"")

  private def justGlobal(g: Global): Show.Result = g match {
    case Global.Val(id)       => id
    case Global.Type(id)      => id
    case Global.Member(n, id) => s(justGlobal(n), "::", id)
  }

  implicit val showGlobal: Show[Global] = Show { g =>
    quoted(justGlobal(g))
  }

  implicit val showLocal: Show[Local] = Show {
    case Local(scope, id) => sh"$scope.$id"
  }

  def showInsts(insts: Seq[Inst], cf: Cf)(implicit cfg: ControlFlow.Graph): Seq[Show.Result] = {
    val buf = mutable.UnrolledBuffer.empty[Show.Result]

    def bind(name: Local) = name match {
      case Local.empty => sh""
      case name        => sh"%$name = "
    }

    insts.foreach {
      case Inst(name, Op.Call(ty, Val.Global(pointee, _), args)) =>
        buf += sh"${bind(name)}call $ty @$pointee(${r(args, sep = ", ")})"

      case Inst(name, Op.Call(ty, ptr, args)) =>
        val pointee = fresh()

        buf += sh"${bind(pointee)}bitcast $ptr to $ty*"
        buf += sh"${bind(name)}call $ty %$pointee(${r(args, sep = ", ")})"

      case Inst(name, Op.Load(ty, ptr)) =>
        val pointee = fresh()

        buf += sh"${bind(pointee)}bitcast $ptr to $ty*"
        buf += sh"${bind(name)}load $ty, $ty* %$pointee"

      case Inst(name, Op.Store(ty, ptr, value)) =>
        val pointee = fresh()

        buf += sh"${bind(pointee)}bitcast $ptr to $ty*"
        buf += sh"${bind(name)}store $value, $ty* %$pointee"

      case inst @ Inst(name, Op.Elem(ty, ptr, indexes)) =>
        val pointee   = fresh()
        val derived   = fresh()

        buf += sh"${bind(pointee)}bitcast $ptr to $ty*"
        buf += sh"${bind(derived)}getelementptr $ty, $ty* %$pointee, ${r(indexes, sep = ", ")}"
        buf += sh"${bind(name)}bitcast ${ty.elemty(indexes.tail)}* %$derived to i8*"

      case Inst(name, op) =>
        buf += sh"${bind(name)}$op"
    }

    buf += sh"$cf"
    buf.toSeq
  }

  implicit def showCf(implicit cfg: ControlFlow.Graph): Show[Cf] = Show {
    case Cf.Unreachable =>
      "unreachable"
    case Cf.Ret(Val.None) =>
      sh"ret void"
    case Cf.Ret(value) =>
      sh"ret $value"
    case Cf.Jump(next) =>
      sh"br $next"
    case Cf.If(cond, thenp, elsep) =>
      sh"br $cond, $thenp, $elsep"
    case Cf.Switch(scrut, default, cases) =>
      sh"switch $scrut, $default [${r(cases.map(i(_)))}${nl("]")}"
    case Cf.Invoke(ty, f, args, succ, fail) =>
      val n = cfg.nodes(succ.name).block.params.head.name
      sh"%$n.succ = invoke $ty ${justVal(f)}(${r(args, sep = ", ")}) to $succ unwind $fail"
    case Cf.Resume(value) =>
      sh"resume $value"
    case cf =>
      unsupported(cf)
  }

  implicit val showOp: Show[Op] = Show {
    case Op.Call(ty, f, args) =>
      unreachable
    case Op.Load(ty, ptr) =>
      unreachable
    case Op.Store(ty, ptr, value) =>
      unreachable
    case Op.Elem(ty, ptr, indexes) =>
      unreachable
    case Op.Extract(aggr, indexes) =>
      sh"extractvalue $aggr, ${r(indexes, sep = ", ")}"
    case Op.Insert(aggr, value, indexes) =>
      sh"insertvalue $aggr, ${r(indexes, sep = ", ")}"
    case Op.Alloca(ty) =>
      sh"alloca $ty"
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
        case Comp.Feq => "fcmp oeq"
        case Comp.Fne => "fcmp one"
        case Comp.Flt => "fcmp olt"
        case Comp.Fle => "fcmp ole"
        case Comp.Fgt => "fcmp ogt"
        case Comp.Fge => "fcmp oge"
      }
      sh"$cmp $l, ${justVal(r)}"
    case Op.Conv(name, ty, v) =>
      sh"$name $v to $ty"
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
    def unapply(edges: Seq[ControlFlow.Edge]
        ): Option[Seq[(Local, Seq[Show.Result])]] = {
      Some(edges.map {
        case ControlFlow.Edge(from, to, _: Next.Succ) =>
          (from.block.name, Seq(sh"%${to.block.params.head.name}.succ"))
        case ControlFlow.Edge(from, _, _: Next.Case) =>
          (from.block.name, Seq())
        case ControlFlow.Edge(from, _, Next.Label(_, vals)) =>
          (from.block.name, vals.map(justVal))
        case ControlFlow.Edge(_, _, _: Next.Fail) =>
          return None
      })
    }
  }

  private object ExFail {
    def unapply(edges: Seq[ControlFlow.Edge]): Boolean =
      edges.forall {
        case ControlFlow.Edge(_, _, _: Next.Fail) => true
        case _                                    => false
      }
  }
}

package scala.scalanative
package compiler
package codegen

import java.lang.{Integer => JInt, Long => JLong, Float => JFloat, Double => JDouble}
import scala.collection.mutable
import util.unsupported
import util.{sh, Show}
import util.Show.{Sequence => s, Indent => i, Unindent => ui, Repeat => r, Newline => nl}
import compiler.analysis.ControlFlow
import nir.Shows.brace
import nir._

object GenTextualLLVM extends GenShow {
  private lazy val prelude = Seq(
      sh"declare i32 @__gxx_personality_v0(...)"
  )
  private lazy val personality =
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
    r(prelude ++ sorted.map(d => sh"$d"), sep = nl(""))
  }

  implicit val showDefn: Show[Defn] = Show {
    case Defn.Var(attrs, name, ty, Val.None) =>
      sh"@$name = ${attrs}global $ty"
    case Defn.Var(attrs, name, _, v) =>
      sh"@$name = ${attrs}global $v"
    case Defn.Const(attrs, name, ty, Val.None) =>
      sh"@$name = ${attrs}constant $ty"
    case Defn.Const(attrs, name, _, v) =>
      sh"@$name = ${attrs}constant $v"
    case Defn.Declare(attrs, name, Type.Function(argtys, retty)) =>
      sh"${attrs}declare $retty @$name(${r(argtys, sep = ", ")})"
    case Defn.Define(attrs, name, Type.Function(_, retty), blocks) =>
      showDefine(attrs, retty, name, blocks)
    case Defn.Struct(attrs, name, tys) =>
      sh"%$name = type {${r(tys, sep = ", ")}}"
    case defn =>
      unsupported(defn)
  }

  def showDefine(
      attrs: Seq[Attr], retty: Type, name: Global, blocks: Seq[Block]) = {
    implicit val cfg = ControlFlow(blocks)
    val blockshows = cfg.map { node =>
      showBlock(node.block, node.pred, isEntry = node eq cfg.entry)
    }
    val body   = brace(i(r(blockshows)))
    val params = sh"(${r(blocks.head.params: Seq[Val], sep = ", ")})"

    sh"${attrs}define $retty @$name$params$attrs $personality $body"
  }

  private lazy val landingpad =
    sh"landingpad { i8*, i32 } catch i8* bitcast ({ i8*, i8*, i32, i8* }* @_ZTIPN11scalanative16ExceptionWrapperE to i8*)"

  def showBlock(block: Block, pred: Seq[ControlFlow.Edge], isEntry: Boolean)(
      implicit cfg: ControlFlow.Graph): Show.Result = {
    val Block(name, params, insts, cf) = block

    val body = r(insts.map(i => sh"$i") :+ sh"${block.cf}", sep = nl(""))

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
    case Type.Struct(name)        => sh"%$name"
    case Type.AnonStruct(tys)     => sh"{${r(tys, sep = ", ")}}"
    case ty                       => unsupported(ty)
  }

  def justVal(v: Val): Show.Result = v match {
    case Val.True           => "true"
    case Val.False          => "false"
    case Val.Zero(ty)       => "zeroinitializer"
    case Val.I8(v)          => v.toString
    case Val.I16(v)         => v.toString
    case Val.I32(v)         => v.toString
    case Val.I64(v)         => v.toString
    case Val.F32(v)         => v.toString
    case Val.F64(v)         => v.toString
    case Val.Struct(n, vs)  => sh"{ ${r(vs, sep = ", ")} }"
    case Val.Array(_, vs)   => sh"[ ${r(vs, sep = ", ")} ]"
    case Val.Chars(v)       => s("c\"", v, "\"")
    case Val.Local(n, ty)   => sh"%$n"
    case Val.Global(n, ty)  => sh"@$n"
    case Val.Bitcast(ty, v) => sh"bitcast ($v to $ty)"
    case _                  => unsupported(v)
  }

  implicit val showVal: Show[Val] = Show { v =>
    val justv = justVal(v)
    val ty    = v.ty
    sh"$ty $justv"
  }

  implicit val showGlobal: Show[Global] = Show { g =>
    r(g.parts, "_")
  }

  implicit val showLocal: Show[Local] = Show {
    case Local(scope, id) => sh"$scope.$id"
  }

  implicit val showInst: Show[Inst] = Show {
    case Inst(Local.empty, op) => sh"$op"
    case Inst(name, op)        => sh"%$name = $op"
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
      sh"call $ty ${justVal(f)}(${r(args, sep = ", ")})"
    case Op.Load(ty, ptr) =>
      sh"load $ty, $ptr"
    case Op.Store(ty, ptr, value) =>
      sh"store $value, $ptr"
    case Op.Elem(_, ptr, indexes) =>
      val ty: nir.Type = ???
      sh"getelementptr $ty, $ptr, ${r(indexes, sep = ", ")}"
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

  implicit def showAttrs: Show[Seq[Attr]] = nir.Shows.showAttrs

  implicit def showConv: Show[Conv] = nir.Shows.showConv

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

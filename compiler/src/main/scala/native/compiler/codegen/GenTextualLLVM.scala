package native
package compiler
package codegen

import scala.collection.mutable
import native.nir._
import native.util.unsupported
import native.util.{sh, Show}
import native.util.Show.{Sequence => s, Indent => i, Unindent => ui, Repeat => r, Newline => nl}
import native.compiler.analysis.ControlFlow
import native.nir.Shows.brace

object GenTextualLLVM extends GenShow {
  implicit val showDefns: Show[Seq[Defn]] = Show { defns =>
    val sorted = defns.sortBy {
      case _: Defn.Struct  => 1
      case _: Defn.Const   => 2
      case _: Defn.Var     => 3
      case _: Defn.Declare => 4
      case _: Defn.Define  => 5
      case _               => -1
    }
    r(sorted, sep = nl(""))
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

  def showDefine(attrs: Seq[Attr], retty: Type, name: Global, blocks: Seq[Block]) = {
    val body = brace(i(showBlocks(blocks)))
    val params = sh"(${r(blocks.head.params: Seq[Val], sep = ", ")})"
    sh"${attrs}define $retty @$name$params $body"
  }

  def showBlocks(blocks: Seq[Block]) = {
    val cfg = ControlFlow(blocks)
    val visited = mutable.Set.empty[ControlFlow.Node]
    val worklist = mutable.Stack.empty[ControlFlow.Node]
    val result = mutable.UnrolledBuffer.empty[Show.Result]
    val entry = cfg(blocks.head.name)

    worklist.push(entry)
    while (worklist.nonEmpty) {
      val node = worklist.pop()
      if (!visited.contains(node)){
        visited += node
        node.succ.foreach(e => worklist.push(e.to))
        result += showBlock(node.block, node.pred, isEntry = node eq entry)
      }
    }

    r(result)
  }

  def showBlock(block: Block, pred: Seq[ControlFlow.Edge], isEntry: Boolean): Show.Result = {
    val instshows = block.insts.map(i => sh"$i") :+ sh"${block.cf}"
    val body = r(instshows, sep = nl(""))

    if (isEntry)
      body
    else {
      val label = ui(sh"${block.name}:")
      val phis = r(block.params.zipWithIndex.map {
        case (Val.Local(n, ty), i) =>
          val branches = pred.map { e =>
            e.next match {
              case Next.Label(name, values) =>
                sh"[${justVal(values(i))}, %$name]"
            }
          }
          sh"%$n = phi $ty ${r(branches, sep = ", ")}"
      }.map(nl(_)))
      sh"$label$phis${nl("")}$body"
    }
  }

  implicit val showType: Show[Type] = Show {
    case Type.Void                => "void"
    case Type.Label               => "label"
    case Type.Vararg              => "..."
    case Type.Bool                => "i1"
    case Type.I8                  => "i8"
    case Type.I16                 => "i16"
    case Type.I32                 => "i32"
    case Type.I64                 => "i64"
    case Type.F32                 => "float"
    case Type.F64                 => "double"
    case Type.Array(ty, n)        => sh"[$n x $ty]"
    case Type.Ptr(ty)             => sh"${ty}*"
    case Type.Function(args, ret) => sh"$ret (${r(args, sep = ", ")})"
    case Type.Struct(name)        => sh"%$name"
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
    case Val.Struct(n, vs) => sh"{ ${r(vs, sep = ", ")} }"
    case Val.Array(_, vs)  => sh"[ ${r(vs, sep = ", ")} ]"
    case Val.Chars(v)      => s("c\"", v, "\"")
    case Val.Local(n, ty)  => sh"%$n"
    case Val.Global(n, ty) => sh"@$n"
    case _                 => unsupported(v)
  }

  implicit val showVal: Show[Val] = Show { v =>
    val justv = justVal(v)
    val ty = v.ty
    sh"$ty $justv"
  }

  implicit val showGlobal: Show[Global] = Show { g =>
    if (g.isIntrinsic) unsupported(g)
    else r(g.parts, "_")
  }

  implicit val showLocal: Show[Local] = Show {
    case Local(scope, id) => sh"$scope.$id"
  }

  implicit val showInst: Show[Inst] = Show {
    case Inst(Local.empty, op) => sh"$op"
    case Inst(name, op)        => sh"%$name = $op"
  }

  implicit val showCf: Show[Cf] = Show {
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
    case Cf.Switch(scrut, default, cases)  =>
      "todo: switch"
    case Cf.Invoke(ty, f, args, succ, fail) =>
      "todo: invoke"
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
      val Type.Ptr(ty) = ptr.ty
      sh"getelementptr $ty, $ptr, ${r(indexes, sep = ", ")}"
    case Op.Extract(ty, aggr, index) =>
      "todo: extract"
    case Op.Insert(ty, aggr, value, index) =>
      "todo: insert"
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
      sh"unsupported: ${op.toString}"
  }

  implicit val showNext: Show[Next] = Show {
    case Next.Label(n, _) => sh"label %$n"
  }

  implicit def showAttrs: Show[Seq[Attr]] = nir.Shows.showAttrs
  implicit def showConv: Show[Conv] = nir.Shows.showConv
}

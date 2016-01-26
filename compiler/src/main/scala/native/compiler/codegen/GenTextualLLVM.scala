package native
package compiler
package codegen

import scala.collection.mutable
import native.nir._
import native.util.unsupported
import native.util.{sh, Show}
import native.util.Show.{Sequence => s, Indent => i, Unindent => ui, Repeat => r, Newline => nl}
import native.compiler.analysis.CFG
import native.nir.Shows.brace

object GenTextualLLVM extends GenShow {
  implicit val showDefns: Show[Seq[Defn]] = Show { defns =>
    r(defns, sep = nl(""))
  }

  implicit val showDefn: Show[Defn] = Show {
    case Defn.Var(attrs, name, ty, rhs) =>
      sh"@$name = global $ty $rhs"
    case Defn.Declare(attrs, name, Type.Function(argtys, retty)) =>
      sh"declare $retty @$name(${r(argtys, sep = ", ")})"
    case Defn.Define(attrs, name, _, blocks) =>
      showDefine(attrs, name, blocks)
    case Defn.Struct(attrs, name, tys) =>
      sh"type %$name = {${r(tys, sep = ", ")}}"
    case defn =>
      unsupported(defn)
  }

  def showDefine(attrs: Seq[Attr], name: Global, blocks: Seq[Block]) = {
    val body = brace(i(showBlocks(blocks)))
    sh"define $name $body"
  }

  def showBlocks(blocks: Seq[Block]) = {
    val cfg = CFG(blocks)
    val visited = mutable.Set.empty[CFG.Node]
    val worklist = mutable.Stack.empty[CFG.Node]
    val result = mutable.UnrolledBuffer.empty[Show.Result]

    worklist.push(cfg(blocks.head.name))
    while (worklist.nonEmpty) {
      val node = worklist.pop()
      if (!visited.contains(node)){
        visited += node
        node.succ.foreach(e => worklist.push(e.to))
        result += showBlock(node.block, node.pred)
      }
    }

    r(result, sep = nl(""))
  }

  def showBlock(block: Block, pred: Seq[CFG.Edge]): Show.Result = {
    val header = sh"${block.name.id}:"
    val phis = r(block.params.zipWithIndex.map {
      case (Param(n, ty), i) =>
        val branches = pred.map { e =>
          sh"[${e.values(i)}, ${e.from.block.name}]"
        }
        sh"phi $ty ${r(branches, sep = ", ")}"
    }.map(i(_)))
    val instructions = r(block.instrs.map(i(_)))

    sh"$header$phis$instructions"
  }

  implicit val showType: Show[Type] = Show {
    case Type.Void                => "void"
    case Type.Bool                => "i1"
    case Type.I8                  => "i8"
    case Type.I16                 => "i16"
    case Type.I32                 => "i32"
    case Type.I64                 => "i64"
    case Type.F32                 => "f32"
    case Type.F64                 => "f64"
    case Type.Array(ty, n)        => sh"[$ty x $n]"
    case Type.Ptr(ty)             => sh"${ty}*"
    case Type.Function(args, ret) => sh"$ret (${r(args, sep = ", ")})"
    case Type.Struct(name)        => sh"%$name"
    case ty                       => unsupported(ty)
  }

  implicit val showVal: Show[Val] = Show {
    case Val.True          => "true"
    case Val.False         => "false"
    case Val.Zero(ty)      => sh"$ty zeroinitializer"
    case Val.I8(v)         => sh"i8 $v"
    case Val.I16(v)        => sh"i16 $v"
    case Val.I32(v)        => sh"i32 $v"
    case Val.I64(v)        => sh"i64 $v"
    case Val.Struct(n, vs) => sh"%$n { ${r(vs, sep = ", ")} }"
    case Val.Array(_, vs)  => sh"[ ${r(vs, sep = ", ")} ]"
    case Val.Local(n, ty)  => sh"$ty %$n"
    case Val.Global(n, ty) => sh"$ty @$n"
    case v                 => unsupported(v)
  }

  implicit val showGlobal: Show[Global] = Show {
    case Global.Atom(id)              => id
    case Global.Nested(owner, member) => sh"${owner}__$member"
    case Global.Tagged(owner, tag)    => sh"${owner}.$tag"
  }

  implicit val showLocal: Show[Local] = Show {
    case Local(scope, id) => sh"$scope.$id"
  }

  implicit val showInstr: Show[Instr] = Show {
    case Instr(None,     attrs, op) => showOp(attrs, op)
    case Instr(Some(id), attrs, op) => sh"%$id = ${showOp(attrs, op)}"
  }

  def showOp(attrs: Seq[Attr], op: Op): Show.Result = op match {
    case Op.Unreachable =>
      "unreachable"
    case Op.Ret(Val.None) =>
      sh"ret"
    case Op.Ret(value) =>
      sh"ret $value"
    case Op.Jump(next) =>
      "todo: jump"
    case Op.If(cond, thenp, elsep) =>
      "todo: if"
    case Op.Switch(scrut, default, cases)  =>
      "todo: switch"
    case Op.Invoke(ty, f, args, succ, fail) =>
      "todo: invoke"

    case Op.Call(ty, f, args) =>
      "todo: call"
    case Op.Load(ty, ptr) =>
      "todo: load"
    case Op.Store(ty, ptr, value) =>
      "todo: store"
    case Op.Elem(ty, ptr, indexes) =>
      "todo: elem"
    case Op.Extract(ty, aggr, index) =>
      "todo: extract"
    case Op.Insert(ty, aggr, value, index) =>
      "todo: insert"
    case Op.Alloca(ty) =>
      sh"alloca[$ty]"
    case Op.Bin(name, ty, l, r) =>
      "todo: bin"
    case Op.Comp(name, ty, l, r) =>
      "todo: comp"
    case Op.Conv(name, ty, v) =>
      "todo: conv"

    case op =>
      sh"unsupported: ${op.toString}"
  }

  implicit val showNext: Show[Next] = Show {
    case Next(n, _) =>
      sh"label $n"
  }

  implicit def showCase: Show[Case] = ???
}

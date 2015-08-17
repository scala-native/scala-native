package salty
package ir

import salty.util.Show
import salty.ir.internal.ShowIR

sealed abstract class Tree {
  def show: Show.Result = ShowIR.showTree(this)
}

sealed abstract trait Type extends Tree
object Type {
  case object Unit    extends Type
  case object Null    extends Type
  case object Nothing extends Type
  case object Bool    extends Type

  sealed abstract case class I(width: Int) extends Type
  object I8 extends  Type.I(8)
  object I16 extends Type.I(16)
  object I32 extends Type.I(32)
  object I64 extends Type.I(64)

  sealed abstract case class F(width: Int) extends Type
  object F32 extends Type.F(32)
  object F64 extends Type.F(64)

  final case class Ptr(ty: Type) extends Type
  final case class Array(ty: Type) extends Type
}

sealed abstract trait Instr extends Tree
object Instr {
  final case class Assign(name: Name, expr: Expr) extends Instr
}

sealed abstract trait Termn extends Tree
object Termn {
  final case class Jump(to: Block) extends Termn
  final case class Return(value: Val) extends Termn
  final case class If(cond: Val, thenb: Block, elseb: Block) extends Termn
  final case class Switch(on: Val, cases: Seq[Branch], defaultb: Block) extends Termn
}

sealed abstract trait Expr extends Instr
object Expr {
  final case class Bin(op: Bin.Op, left: Val, right: Val) extends Expr
  object Bin {
    sealed abstract class Op
    final case object Add   extends Bin.Op
    final case object Sub   extends Bin.Op
    final case object Mul   extends Bin.Op
    final case object Div   extends Bin.Op
    final case object Mod   extends Bin.Op
    final case object Shl   extends Bin.Op
    final case object Lshr  extends Bin.Op
    final case object Ashr  extends Bin.Op
    final case object And   extends Bin.Op
    final case object Or    extends Bin.Op
    final case object Xor   extends Bin.Op
    final case object Eq    extends Bin.Op
    final case object Neq   extends Bin.Op
    final case object Lt    extends Bin.Op
    final case object Lte   extends Bin.Op
    final case object Gt    extends Bin.Op
    final case object Gte   extends Bin.Op
  }
  final case class Conv(op: Conv.Op, value: Val, to: Type) extends Expr
  object Conv {
    sealed abstract class Op
    final case object Trunc    extends Conv.Op
    final case object Zext     extends Conv.Op
    final case object Sext     extends Conv.Op
    final case object Fptrunc  extends Conv.Op
    final case object Fpext    extends Conv.Op
    final case object Fptoui   extends Conv.Op
    final case object Fptosi   extends Conv.Op
    final case object Uitofp   extends Conv.Op
    final case object Sitofp   extends Conv.Op
    final case object Ptrtoint extends Conv.Op
    final case object Inttoptr extends Conv.Op
    final case object Bitcast  extends Conv.Op
    final case object Dyncast  extends Conv.Op
  }
  final case class Is(value: Val, ty: Type) extends Expr
  final case class New(name: Name) extends Expr
  final case class Call(name: Name, args: Seq[Val]) extends Expr
  final case class Phi(branches: Seq[Branch]) extends Expr
}

sealed abstract trait Val extends Expr
object Val {
  case object Null extends Val
  case object Unit extends Val
  case object This extends Val
  final case class Bool(value: Boolean) extends Val
  final case class Number(repr: String, ty: Type) extends Val
  final case class Array(vs: Seq[Val]) extends Val
}

sealed abstract trait Stat extends Tree
object Stat {
  final case class Class(name: Name, parent: Name,
                         interfaces: Seq[Name], body: Seq[Stat]) extends Stat
  final case class Interface(name: Name, interfaces: Seq[Name],
                             body: Seq[Stat]) extends Stat
  final case class Module(name: Name, parent: Name,
                          interfaces: Seq[Name], body: Seq[Stat]) extends Stat
  final case class Field(name: Name, ty: Type) extends Stat
  final case class Declare(name: Name, params: Seq[Type],
                           ty: Type) extends Stat
  final case class Define(name: Name, params: Seq[LabeledType],
                          ty: Type, body: Block) extends Stat
}

sealed abstract class Name extends Val with Type
object Name {
  final case class Local(id: String) extends Name
  final case class Global(id: String) extends Name
  final case class Nested(parent: Name, child: Name) extends Name
}

final case class Branch(value: Val, block: Block) extends Tree
final case class LabeledType(name: Name, ty: Type) extends Tree
final case class LabeledVal(name: Name, value: Val) extends Tree
final class Block(val instrs: Seq[Instr], private var _termn: Termn) extends Tree {
  def termn = _termn

  def id: Name =
    Name.Local(java.lang.Integer.toHexString(this.##))

  def next: Seq[Block] = termn match {
    case Termn.Return(_) =>
      Seq()
    case Termn.Jump(b) =>
      Seq(b)
    case Termn.If(_, b1, b2) =>
      Seq(b1, b2)
    case Termn.Switch(_, branches, default) =>
      branches.map(_.block) :+ default
  }

  def chain(f: (Seq[Instr], Val) => Block)
           (implicit fresh: Fresh[Name.Local]): Block = {
    termn match {
      case Termn.Return(v) =>
        f(instrs, v)
      case Termn.Jump(block) =>
        Block(instrs, Termn.Jump(block.chain(f)))
      case Termn.If(cond, b1, b2) =>
        var n1, n2: Block = null
        b1 chain { (instrs1, v1) =>
          b2 chain { (instrs2, v2) =>
            n1 = Block(instrs1, Termn.Return(v1))
            n2 = Block(instrs2, Termn.Return(v2))
            val branches = Seq(Branch(v1, n1), Branch(v2, n2))
            val name = fresh.gen
            val next =
              f(Seq(Instr.Assign(name, Expr.Phi(branches))), name)
            n1._termn = Termn.Jump(next)
            n2._termn = Termn.Jump(next)
            n2
          }
          n1
        }
        Block(instrs, Termn.If(cond, n1, n2))
      case Termn.Switch(_, _, _) =>
        ???
    }
  }

  def zipChain(other: Block)
              (f: (Seq[Instr], Val, Val) => Block)
              (implicit fresh: Fresh[Name.Local]): Block =
    this.chain { (instrs1, v1) =>
      other.chain { (instrs2, v2) =>
        f(instrs1 ++ instrs2, v1, v2)
      }
    }
}
object Block {
  def apply(termn: Termn): Block =
    new Block(Seq(), termn)
  def apply(instrs: Seq[Instr], termn: Termn): Block =
    new Block(instrs, termn)
}

trait Fresh[T] {
  def gen: T
}

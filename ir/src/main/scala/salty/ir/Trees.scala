package salty.ir

import scala.collection.mutable
import salty.util.Sh
import salty.ir.Shows._

sealed abstract class Type
object Type {
  final case object Null    extends Type
  final case object Nothing extends Type

  sealed abstract class Primitive extends Type
  final object Unit extends Primitive
  final object Bool extends Primitive

  sealed abstract case class I(width: Int) extends Primitive
  final object I8 extends  Type.I(8)
  final object I16 extends Type.I(16)
  final object I32 extends Type.I(32)
  final object I64 extends Type.I(64)

  sealed abstract case class F(width: Int) extends Primitive
  final object F32 extends Type.F(32)
  final object F64 extends Type.F(64)

  final case class Ref(ty: Type) extends Type
  final case class Slice(ty: Type) extends Type
  final case class Array(ty: Type, length: Int) extends Type
  final case class Named(name: Name) extends Type
}

sealed abstract class Instr
object Instr {
  final case class Assign(name: Name, expr: Expr) extends Instr
}

sealed abstract class Termn
object Termn {
  sealed abstract class Leaf extends Termn
  final case object Undefined extends Leaf
  final case class Out(value: Val) extends Leaf
  final case class Return(value: Val) extends Leaf
  final case class Throw(value: Val) extends Leaf
  final case class Jump(to: Block) extends Termn
  final case class If(cond: Val, thenb: Block, elseb: Block) extends Termn
  final case class Switch(on: Val, default: Block, branches: Seq[Branch]) extends Termn
  final case class Try(body: Block,
                       catchb: Option[Block],
                       finallyb: Option[Block]) extends Termn
}

sealed abstract class Expr extends Instr
object Expr {
  final case class Bin(op: BinOp, left: Val, right: Val) extends Expr
  final case class Conv(op: ConvOp, value: Val, to: Type) extends Expr
  final case class Is(value: Val, ty: Type) extends Expr
  final case class Alloc(ty: Type, elements: Option[Val] = None) extends Expr
  final case class Call(name: Name, args: Seq[Val]) extends Expr
  final case class Phi(branches: Seq[Branch]) extends Expr
  final case class Load(ptr: Val) extends Expr
  final case class Store(ptr: Val, value: Val) extends Expr
  final case class Box(value: Val, ty: Type) extends Expr
  final case class Unbox(value: Val, ty: Type) extends Expr
  final case class Length(value: Val) extends Expr
  final case object Catchpad extends Expr
}

sealed abstract class Val extends Expr
object Val {
  final case object Null extends Val
  final case object Unit extends Val
  final case object This extends Val
  final case class Bool(value: Boolean) extends Val
  final case class Number(repr: String, ty: Type) extends Val
  final case class Array(vs: Seq[Val]) extends Val
  final case class Slice(ptr: Val, length: Val) extends Val
  final case class Elem(ptr: Val, value: Val) extends Val
  final case class Class(ty: Type) extends Val
  final case class Str(value: String) extends Val

  def apply(i: Int) = Val.Number(i.toString, Type.I32)
  def apply(b: Boolean) = Val.Bool(b)
}

sealed abstract class Stat
object Stat {
  final case class Class(name: Name, parent: Name,
                         interfaces: Seq[Name], body: Seq[Stat]) extends Stat
  final case class Interface(name: Name, interfaces: Seq[Name],
                             body: Seq[Stat]) extends Stat
  final case class Module(name: Name, parent: Name,
                          interfaces: Seq[Name], body: Seq[Stat]) extends Stat
  final case class Var(name: Name, ty: Type) extends Stat
  final case class Declare(name: Name, params: Seq[Type],
                           ty: Type) extends Stat
  final case class Define(name: Name, params: Seq[LabeledType],
                          ty: Type, body: Block) extends Stat
}

sealed abstract class Name extends Val
object Name {
  final case class Local(id: String) extends Name
  final case class Global(id: String) extends Name
  final case class Nested(parent: Name, child: Name) extends Name
}

final case class Branch(value: Val, block: Block)
final case class LabeledType(name: Name, ty: Type)
final case class Block(var name: Name,
                       var instrs: Seq[Instr],
                       var termn: Termn)
object Block {
  def apply(termn: Termn)(implicit fresh: Fresh): Block =
    new Block(fresh("block"), Nil, termn)
  def apply(instrs: Seq[Instr], termn: Termn)(implicit fresh: Fresh): Block =
    new Block(fresh("block"), instrs, termn)
}

sealed abstract class BinOp
object BinOp {
  final case object Add    extends BinOp
  final case object Sub    extends BinOp
  final case object Mul    extends BinOp
  final case object Div    extends BinOp
  final case object Mod    extends BinOp
  final case object Shl    extends BinOp
  final case object Lshr   extends BinOp
  final case object Ashr   extends BinOp
  final case object And    extends BinOp
  final case object Or     extends BinOp
  final case object Xor    extends BinOp
  final case object Eq     extends BinOp
  final case object Equals extends BinOp
  final case object Neq    extends BinOp
  final case object Lt     extends BinOp
  final case object Lte    extends BinOp
  final case object Gt     extends BinOp
  final case object Gte    extends BinOp
}

sealed abstract class ConvOp
object ConvOp {
  final case object Trunc    extends ConvOp
  final case object Zext     extends ConvOp
  final case object Sext     extends ConvOp
  final case object Fptrunc  extends ConvOp
  final case object Fpext    extends ConvOp
  final case object Fptoui   extends ConvOp
  final case object Fptosi   extends ConvOp
  final case object Uitofp   extends ConvOp
  final case object Sitofp   extends ConvOp
  final case object Ptrtoint extends ConvOp
  final case object Inttoptr extends ConvOp
  final case object Bitcast  extends ConvOp
  final case object Cast     extends ConvOp
}



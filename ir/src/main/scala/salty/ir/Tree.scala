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
  case object I8      extends Type
  case object I16     extends Type
  case object I32     extends Type
  case object I64     extends Type
  case object F32     extends Type
  case object F64     extends Type
  final case class Ptr(ty: Type) extends Type
  final case class Array(ty: Type) extends Type
  final case class FixedArray(ty: Type, n: Val) extends Type
  final case class Struct(tys: Seq[LabeledType]) extends Type
}

sealed abstract trait Instr extends Tree
object Instr {
  final case class If(cond: Val, thenp: Expr, elsep: Expr) extends Instr
  final case class Switch(on: Val, cases: Seq[Branch], default: Expr) extends Instr
  final case class Assign(name: Name, expr: Expr) extends Instr
  final case class While(cond: Expr, body: Expr) extends Instr
  final case class DoWhile(body: Expr, cond: Expr) extends Instr
}

sealed abstract trait Expr extends Instr
object Expr {
  final case class Binary(op: BinOp, left: Val, right: Val) extends Expr
  final case class Cast(op: CastOp, value: Val, to: Type) extends Expr
  final case class Is(value: Val, ty: Type) extends Expr
  final case class Select(target: Val, name: Name) extends Expr
  final case class Alloc(name: Name) extends Expr
  final case class Call(name: Name, args: Seq[Val]) extends Expr
  final case class Phi(names: Seq[Name]) extends Expr
  final case class Block(instrs: Seq[Instr], ret: Val) extends Expr
  object Block {
    def apply(ret: Val): Block = Block(Seq(), ret)
  }
}

sealed abstract trait Val extends Expr
object Val {
  case object Null extends Val
  case object Unit extends Val
  final case class Bool(value: Boolean) extends Val
  final case class Integer(repr: String, ty: Type) extends Val
  final case class Float(repr: String, ty: Type) extends Val
  final case class Struct(vs: Seq[LabeledVal]) extends Val
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
  final case class Struct(name: Name, body: Seq[Stat]) extends Stat
  final case class Const(name: Name, ty: Type, init: Val) extends Stat
  final case class Var(name: Name, ty: Type, init: Val) extends Stat
  final case class Decl(name: Name, params: Seq[Type],
                        ty: Type) extends Stat
  final case class Def(name: Name, params: Seq[LabeledType],
                       ty: Type, body: Expr) extends Stat
}

final case class Name(repr: String) extends Type with Val
final case class Branch(value: Val, expr: Expr) extends Tree
final case class LabeledType(name: Name, ty: Type) extends Tree
final case class LabeledVal(name: Name, value: Val) extends Tree

sealed abstract class BinOp
object BinOp {
  final case object +   extends BinOp
  final case object -   extends BinOp
  final case object *   extends BinOp
  final case object /   extends BinOp
  final case object %   extends BinOp
  final case object |   extends BinOp
  final case object ^   extends BinOp
  final case object &   extends BinOp
  final case object <<  extends BinOp
  final case object >>> extends BinOp
  final case object >>  extends BinOp
  final case object ==  extends BinOp
  final case object !=  extends BinOp
  final case object >   extends BinOp
  final case object >=  extends BinOp
  final case object <   extends BinOp
  final case object <=  extends BinOp
  final case object &&  extends BinOp
  final case object ||  extends BinOp
}

sealed abstract trait CastOp
object CastOp {
  final case object Bitcast  extends CastOp
  final case object Dyncast  extends CastOp
}

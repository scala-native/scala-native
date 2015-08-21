package salty
package ir

import salty.util.Show
import salty.ir.internal.ShowIR

sealed abstract class Tree {
  def show: String = ShowIR.showTree(this).build
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
  sealed abstract class Leaf extends Termn
  final case class Out(value: Val) extends Leaf
  final case class Return(value: Val) extends Leaf
  final case class Throw(value: Val) extends Leaf
  final case class Jump(to: Block) extends Termn
  final case class If(cond: Val, thenb: Block, elseb: Block) extends Termn
  final case class Switch(on: Val, default: Block, branches: Seq[Branch]) extends Termn
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
  final case class Alloc(ty: Type) extends Expr
  final case class Call(name: Name, args: Seq[Val]) extends Expr
  final case class Phi(branches: Seq[Branch]) extends Expr
  final case class Load(ptr: Val) extends Expr
  final case class Store(ptr: Val, value: Val) extends Expr
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
  final case class Var(name: Name, ty: Type) extends Stat
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

final case class Block(name: Name, var instrs: Seq[Instr], var termn: Termn) extends Tree {
  def foreachNext(f: Block => Unit): Unit = termn match {
    case _: Termn.Leaf =>
      ()
    case Termn.Jump(b) =>
      f(b)
    case Termn.If(_, b1, b2) =>
      f(b1)
      f(b2)
    case Termn.Switch(_, default, branches) =>
      f(default)
      branches.foreach(br => f(br.block))
  }

  def foreach(f: Block => Unit): Unit = {
    var visited = List.empty[Block]
    def loop(block: Block): Unit =
      if (visited.contains(block))
        ()
      else {
        visited = block :: visited
        f(block)
        block.foreachNext(loop)
      }
    loop(this)
  }

  def foreachBreadthFirst(f: Block => Unit): Unit = {
    var visited = List.empty[Block]
    def loop(blocks: Seq[Block]): Unit = blocks match {
      case Seq() => ()
      case block +: rest =>
        if (visited.contains(block))
          loop(rest)
        else {
          visited = block :: visited
          f(block)
          var next = List.empty[Block]
          block.foreachNext { n => next = n :: next }
          loop(rest ++ next.reverse)
        }
    }
    loop(Seq(this))
  }

  def foreachLeaf(f: Block => Unit): Unit =
    foreach {
      case b @ Block(_, _, _: Termn.Leaf) =>
        f(b)
      case _ =>
        ()
    }

  def outs: Seq[Branch] = {
    var branches = List.empty[Branch]
    foreachLeaf {
      case b @ Block(_, _, Termn.Out(v)) =>
        branches = Branch(v, b) :: branches
      case _ =>
        ()
    }
    branches
  }

  def merge(f: (Seq[Instr], Val) => Block)(implicit fresh: Fresh): Block = this match {
    case Block(_, instrs, Termn.Out(value)) =>
      val Block(name, finstrs, ftermn) = f(Seq(), value)
      Block(name, instrs ++ finstrs, ftermn)
    case _ =>
      outs match {
        case Seq() => ()
        case Seq(Branch(v, block)) =>
          val target = f(Seq(), v)
          println(s"merging ${this.name} with ${target.name}")
          block.termn = Termn.Jump(target)
        case branches =>
          val name = fresh()
          val instr = Instr.Assign(name, Expr.Phi(branches))
          val target = f(Seq(instr), name)
          println(s"merging ${this.name} with ${target.name}")
          branches.foreach { br =>
            br.block.termn = Termn.Jump(target)
          }
      }
      this
  }

  def chain(other: Block)(f: (Seq[Instr], Val, Val) => Block)
           (implicit fresh: Fresh): Block = {
    println(s"chaining ${this.name} with ${other.name}")
    this.merge { (instrs1, v1) =>
      other.merge { (instrs2, v2) =>
        f(instrs1 ++ instrs2, v1, v2)
      }
    }
  }
}
object Block {
  def apply(termn: Termn)(implicit fresh: Fresh): Block =
    new Block(fresh("block"), Seq(), termn)
  def apply(instrs: Seq[Instr], termn: Termn)(implicit fresh: Fresh): Block = {
    new Block(fresh("block"), instrs, termn)
  }

  def chain(blocks: Seq[Block])(f: (Seq[Instr], Seq[Val]) => Block)
           (implicit fresh: Fresh): Block = {
    println(s"-- chaining:")
    blocks.foreach { b =>
      println("--")
      println(b.show)
    }

    def loop(blocks: Seq[Block], instrs: Seq[Instr], values: Seq[Val]): Block =
      blocks match {
        case Seq() =>
          f(instrs, values)
        case init +: rest =>
          init.merge { (ninstrs, nv) =>
            loop(rest, instrs ++ ninstrs, values :+ nv)
          }
      }
    val res = loop(blocks, Seq(), Seq())
    println(s"-- res")
    println(res.show)
    res
  }
}

class Fresh {
  private var i: Int = 0
  def apply(prefix: String = "") = {
    val res = Name.Local(prefix + i)
    i += 1
    res
  }
}

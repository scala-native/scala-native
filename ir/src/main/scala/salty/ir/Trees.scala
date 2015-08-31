package salty.ir

import scala.collection.mutable

sealed abstract trait Type
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

  final case class Ptr(ty: Type) extends Type
  final case class Slice(ty: Type) extends Type
  final case class Array(ty: Type, length: Int) extends Type
}

sealed abstract trait Instr
object Instr {
  final case class Assign(name: Name, expr: Expr) extends Instr
}

sealed abstract trait Termn
object Termn {
  sealed abstract class Leaf extends Termn
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

sealed abstract trait Expr extends Instr
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

sealed abstract trait Val extends Expr
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

sealed abstract trait Stat
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

final case class Block(var name: Name,
                       var instrs: Seq[Instr],
                       var termn: Termn) {
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
    case Termn.Try(b1, b2, b3) =>
      f(b1)
      b2.foreach(f)
      b3.foreach(f)
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
    def loop(blocks: List[Block]): Unit = blocks match {
      case Nil => ()
      case block :: rest =>
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
    loop(List(this))
  }

  def foreachLeaf(f: Block => Unit): Unit =
    foreach {
      case b @ Block(_, _, _: Termn.Leaf) =>
        f(b)
      case _ =>
        ()
    }

  def outs: List[Branch] = {
    var branches = List.empty[Branch]
    foreachLeaf {
      case b @ Block(_, _, Termn.Out(v)) =>
        branches = Branch(v, b) :: branches
      case _ =>
        ()
    }
    branches
  }

  def merge(f: Val => Block)(implicit fresh: Fresh): Block = {
    outs match {
      case Nil =>
        ()
      case Branch(v, block) :: Nil =>
        val target = f(v)
        block.termn = Termn.Jump(target)
      case branches =>
        val name = fresh()
        val instr = Instr.Assign(name, Expr.Phi(branches))
        val termn = Termn.Jump(Block(Seq(instr), Termn.Jump(f(name))))
        branches.foreach { br =>
          br.block.termn = termn
        }
    }
    this
  }

  def chain(other: Block)(f: (Val, Val) => Block)
           (implicit fresh: Fresh): Block = {
    this.merge { v1 =>
      other.merge { v2 =>
        f(v1, v2)
      }
    }
  }

  def simplify: Block = {
    val usecount = mutable.Map.empty[Name, Int].withDefault(_ => 0)
    this.foreach { block =>
      block.foreachNext { next =>
        usecount(next.name) += 1
      }
    }
    val mergemap = mutable.Map.empty[Name, Block]
    def fixphi(instr: Instr) = instr match {
      case Instr.Assign(n, Expr.Phi(branches)) =>
        Instr.Assign(n, Expr.Phi(
          branches.map { case br @ Branch(v, block) =>
            Branch(v, mergemap.get(block.name).getOrElse(block))
          }
        ))
      case _ => instr
    }
    this.foreach { block =>
      def loop() = block match {
        case Block(_, instrs1, Termn.Jump(next @ Block(_, instrs2, termn)))
             if usecount(next.name) == 1 =>
          block.instrs = instrs1 ++ instrs2
          block.termn = termn
          mergemap(next.name) = block
          true
        case _ =>
          false
      }
      while (loop()) ()
    }
    this.foreach { block =>
      block.instrs = block.instrs.map(fixphi)
    }
    this
  }
}
object Block {
  def apply(termn: Termn)(implicit fresh: Fresh): Block =
    new Block(fresh("block"), Nil, termn)
  def apply(instrs: Seq[Instr], termn: Termn)(implicit fresh: Fresh): Block = {
    new Block(fresh("block"), instrs, termn)
  }

  def chain(blocks: Seq[Block])(f: Seq[Val] => Block)
           (implicit fresh: Fresh): Block = {
    def loop(blocks: Seq[Block], values: Seq[Val]): Block =
      blocks match {
        case Seq() =>
          f(values)
        case init +: rest =>
          init.merge { nv =>
            loop(rest, values :+ nv)
          }
      }
    loop(blocks, Seq())
  }
}

final case class Branch(value: Val, block: Block)
final case class LabeledType(name: Name, ty: Type)



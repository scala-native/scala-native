package salty.ir

import salty.util.sh

object Combinators {
  implicit class RichBlock(val self: Block) extends AnyVal {
    import self._

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

    def foreach(f: Block => Unit): Unit =
      (new DFPass[Unit] {
        def result = ()
        override def onBlock(b: Block) = f(b)
      }).run(self)

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
      loop(List(self))
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
      self
    }

    def chain(other: Block)(f: (Val, Val) => Block)
             (implicit fresh: Fresh): Block = {
      self.merge { v1 =>
        other.merge { v2 =>
          f(v1, v2)
        }
      }
    }

    def simplify: Block = {
      (new SimplifyCFG).run(self)
      self
    }

    def verify: Block = {
      var blocks = List.empty[Block]
      self.foreach { b => blocks = b :: blocks }
      blocks.foreach {
        _.instrs.foreach {
          case Instr.Assign(_, Expr.Phi(branches)) =>
            branches.foreach { br =>
              assert(blocks.contains(br.block),
                sh"block referenced but doesn't exist in the call graph ${br.block}")
            }
          case _ =>
            ()
        }
      }
      self
    }
  }

  implicit class RichBlocks(val blocks: Seq[Block]) extends AnyVal {
    def chain(f: Seq[Val] => Block)
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
}

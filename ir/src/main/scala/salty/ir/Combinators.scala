package salty.ir

object Combinators {
  implicit class RichNode(val self: Node) extends AnyVal {
    import self._

    def merge: Node = self

    def chain(other: Node): (Node, Node) = ???
  }

  implicit class RichNodes(val blocks: Seq[Node]) extends AnyVal {
    def chain: Seq[Node] = ???
  }
}

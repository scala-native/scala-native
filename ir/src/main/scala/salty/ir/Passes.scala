package salty.ir

abstract class Pass {
  def onNode(node: Node): Unit
}
object Pass {
  def run(entry: Node, pass: Pass) = {
    val epoch = Node.nextEpoch
    def loop(node: Node): Unit =
      if (node.epoch < epoch) {
        node.epoch = epoch
        pass.onNode(node)
        Edge.of(node).foreach(e => loop(e.node))
      }
    loop(entry)
  }
}



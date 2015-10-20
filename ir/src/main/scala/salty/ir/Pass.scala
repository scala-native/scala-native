package salty.ir

abstract class Pass {
  def onNode(node: Node): Unit
}
object Pass {
  def run(pass: Pass, entry: Node): Unit = {
    val epoch = Node.nextEpoch
    def loop(node: Node): Unit =
      if (node._epoch < epoch) {
        node._epoch = epoch
        pass.onNode(node)
        node.deps.foreach(loop)
      }
    loop(entry)
  }
}

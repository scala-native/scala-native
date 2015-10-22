package salty.ir

abstract class Pass {
  def onNode(node: Node): Unit
}
object Pass {
  def run(pass: Pass, entry: Node,
          followDefn: Boolean = true,
          followEf: Boolean = true,
          followCf: Boolean = true,
          followVal: Boolean = true) = {
    val epoch = Node.nextEpoch
    def loop(node: Node): Unit =
      if (node._epoch < epoch) {
        node._epoch = epoch
        pass.onNode(node)
        node.deps.foreach { d =>
          if ((d.isDefn && followDefn) ||
              (d.isEf   && followEf  ) ||
              (d.isCf   && followCf  ) ||
              (d.isVal  && followVal )  ) loop(d.dep)
        }
      }
    loop(entry)
  }
}

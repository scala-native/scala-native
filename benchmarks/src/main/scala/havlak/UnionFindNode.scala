package havlak

import som._

/**
 * class UnionFindNode
 *
 * The algorithm uses the Union/Find algorithm to collapse
 * complete loops into a single node. These nodes and the
 * corresponding functionality are implemented with this class
 */
final class UnionFindNode {
  private var parent: UnionFindNode = _
  private var bb: BasicBlock        = _
  private var loop: SimpleLoop      = _
  private var dfsNumber: Int        = _

  // Initialize this node.
  //
  def initNode(bb: BasicBlock, dfsNumber: Int): Unit = {
    this.parent = this
    this.bb = bb
    this.dfsNumber = dfsNumber
    this.loop = null
  }

  // Union/Find Algorithm - The find routine.
  //
  // Implemented with Path Compression (inner loops are only
  // visited and collapsed once, however, deep nests would still
  // result in significant traversals).
  //
  def findSet(): UnionFindNode = {
    val nodeList = new Vector[UnionFindNode]();

    var node = this
    while (node != node.parent) {
      if (node.parent != node.parent.parent) {
        nodeList.append(node)
      }
      node = node.parent
    }

    // Path Compression, all nodes' parents point to the 1st level parent.
    nodeList.forEach(iter => iter.union(parent))
    node
  }

  // Union/Find Algorithm - The union routine.
  //
  // Trivial. Assigning parent pointer is enough,
  // we rely on path compression.
  //
  def union(basicBlock: UnionFindNode): Unit = {
    parent = basicBlock;
  }

  // Getters/Setters
  //
  def getBb() = bb

  def getLoop() = loop

  def getDfsNumber() = dfsNumber

  def setLoop(loop: SimpleLoop): Unit = {
    this.loop = loop;
  }
}

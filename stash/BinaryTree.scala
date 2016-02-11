package test

object BinaryTree {
  def max(a: Int, b: Int) =
    if (a > b) a else b
  def run(n: Int): Unit = {
    val minDepth = 4
    val maxDepth = max(n, (minDepth+2))
    val longLivedTree = Tree(0, maxDepth)
    var depth = minDepth
    while (depth <= maxDepth) {
      val iterations = 1 << (maxDepth - depth + minDepth)
      var i,sum = 0
      while (i < iterations) {
        i += 1
        sum += Tree(i,depth).isum + Tree(-i,depth).isum
      }
      depth += 2
    }
  }
  final class Tree(i: Int, left: Tree, right: Tree) {
    def isum: Int = {
      val tl = left
      if (tl eq null) i
      else i + tl.isum - right.isum
    }
  }
  object Tree {
    def apply(i: Int, depth: Int): Tree = {
      if (depth > 0) new Tree(i, Tree(i*2-1, depth-1), Tree(i*2, depth-1))
      else new Tree(i, null, null)
    }
  }
}

object Test {
  def main(args: Array[String]): Unit = BinaryTree.run(20)
}

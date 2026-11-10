import java.lang.ref.WeakReference

final class Node(val value: Long, val next: Node)

object Main {
  // With a 128 MiB maximum heap the pool holds about 650 packets of 254
  // entries. Marking the array queues one packet per 512 elements and pushes
  // every node, and the weak references alone need more entries than the
  // pool has.
  private final val Nodes = 600000
  private final val WeakRefs = 300000
  private final val Collections = 5

  def main(args: Array[String]): Unit = {
    val nodes = new Array[Node](Nodes)
    var i = 0
    while (i < Nodes) {
      nodes(i) = new Node(i, if (i > 0) nodes(i - 1) else null)
      i += 1
    }
    val weakRefs = new Array[WeakReference[Node]](WeakRefs)
    i = 0
    while (i < WeakRefs) {
      // Odd referents are reachable only through their weak reference.
      val referent = if (i % 2 == 0) nodes(i) else new Node(-i, null)
      weakRefs(i) = new WeakReference(referent)
      i += 1
    }

    var gc = 0
    while (gc < Collections) { System.gc(); gc += 1 }

    i = 0
    while (i < Nodes) {
      val node = nodes(i)
      assert(node.value == i, s"node $i holds ${node.value}")
      assert(i == 0 || (node.next eq nodes(i - 1)), s"node $i lost its link")
      i += 1
    }
    var cleared = 0
    i = 0
    while (i < WeakRefs) {
      val referent = weakRefs(i).get()
      if (i % 2 == 0) assert(referent eq nodes(i), s"weak reference $i lost")
      else if (referent == null) cleared += 1
      i += 1
    }
    // Conservative stack scanning may keep a few referents alive.
    assert(
      cleared > WeakRefs / 2 - 100,
      s"only $cleared weak references cleared"
    )
    println(s"ok, $cleared weak references cleared")
  }
}

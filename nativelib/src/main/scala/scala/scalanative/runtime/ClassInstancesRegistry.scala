package scala.scalanative.runtime

/** Registry for created instances of java.lang.Class
 *  Its only purpose is to prevent the GC from collecting instances of java.lang.Class
 */
private[runtime] object ClassInstancesRegistry {
  case class Node(head: _Class[_], tail: Node)
  private var node: Node = _

  def add(cls: _Class[_]): _Class[_] = {
    val newNode = Node(cls, node)
    node = newNode
    cls
  }
}

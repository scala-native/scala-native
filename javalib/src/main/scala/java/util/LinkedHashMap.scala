// Ported from Scala.js commit: f7be410 dated: 2020-10-12

/*
 *  Static method newLinkedHashMap added for Scala Native.
 */

package java.util

import java.{util => ju}
import java.util.function.BiConsumer

class LinkedHashMap[K, V](
    initialCapacity: Int,
    loadFactor: Float,
    accessOrder: Boolean
) extends HashMap[K, V](initialCapacity, loadFactor)
    with SequencedMap[K, V] {
  self =>

  import LinkedHashMap._

  /** Node that was least recently created (or accessed under access-order). */
  private var eldest: Node[K, V] = _

  /** Node that was most recently created (or accessed under access-order). */
  private var youngest: Node[K, V] = _

  def this(initialCapacity: Int, loadFactor: Float) =
    this(initialCapacity, loadFactor, false)

  def this(initialCapacity: Int) =
    this(initialCapacity, HashMap.DEFAULT_LOAD_FACTOR)

  def this() =
    this(HashMap.DEFAULT_INITIAL_CAPACITY)

  def this(m: Map[_ <: K, _ <: V]) = {
    this(m.size())
    putAll(m)
  }

  private def asMyNode(node: HashMap.Node[K, V]): Node[K, V] =
    node.asInstanceOf[Node[K, V]]

  override private[util] def newNode(
      key: K,
      hash: Int,
      value: V,
      previous: HashMap.Node[K, V],
      next: HashMap.Node[K, V]
  ): HashMap.Node[K, V] = {
    new Node(key, hash, value, previous, next, null, null)
  }

  override private[util] def nodeWasAccessed(node: HashMap.Node[K, V]): Unit = {
    if (accessOrder) {
      val myNode = asMyNode(node)
      if (myNode.younger ne null) {
        removeFromOrderedList(myNode)
        appendToOrderedList(myNode)
      }
    }
  }

  override private[util] def nodeWasAdded(node: HashMap.Node[K, V]): Unit = {
    appendToOrderedList(asMyNode(node))
    if (removeEldestEntry(eldest))
      removeNode(eldest)
  }

  override private[util] def nodeWasRemoved(node: HashMap.Node[K, V]): Unit =
    removeFromOrderedList(asMyNode(node))

  private def appendToOrderedList(node: Node[K, V]): Unit = {
    val older = youngest
    if (older ne null)
      older.younger = node
    else
      eldest = node
    node.older = older
    node.younger = null
    youngest = node
  }

  private def removeFromOrderedList(node: Node[K, V]): Unit = {
    val older = node.older
    val younger = node.younger
    if (older eq null)
      eldest = younger
    else
      older.younger = younger
    if (younger eq null)
      youngest = older
    else
      younger.older = older
  }

  override def clear(): Unit = {
    super.clear()

    /* #4195 HashMap.clear() won't call `nodeWasRemoved` for every node, which
     * would be inefficient, so `eldest` and `yougest` are not automatically
     * updated. We must explicitly set them to `null` here.
     */
    eldest = null
    youngest = null
  }

  protected def removeEldestEntry(eldest: Map.Entry[K, V]): Boolean = false

  override def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit = {
    var node = eldest
    while (node ne null) {
      action.accept(node.key, node.value)
      node = node.younger
    }
  }

  override private[util] def nodeIterator(): ju.Iterator[HashMap.Node[K, V]] =
    new NodeIterator

  override private[util] def keyIterator(): ju.Iterator[K] =
    new KeyIterator

  override private[util] def valueIterator(): ju.Iterator[V] =
    new ValueIterator

  private final class NodeIterator
      extends AbstractLinkedHashMapIterator[HashMap.Node[K, V]] {
    protected def extract(node: Node[K, V]): Node[K, V] = node
  }

  private final class KeyIterator extends AbstractLinkedHashMapIterator[K] {
    protected def extract(node: Node[K, V]): K = node.key
  }

  private final class ValueIterator extends AbstractLinkedHashMapIterator[V] {
    protected def extract(node: Node[K, V]): V = node.value
  }

  private abstract class AbstractLinkedHashMapIterator[A]
      extends ju.Iterator[A] {
    private var nextNode: Node[K, V] = eldest
    private var lastNode: Node[K, V] = _

    protected def extract(node: Node[K, V]): A

    def hasNext(): Boolean =
      nextNode ne null

    def next(): A = {
      if (!hasNext())
        throw new NoSuchElementException("next on empty iterator")
      val node = nextNode
      lastNode = node
      nextNode = node.younger
      extract(node)
    }

    override def remove(): Unit = {
      val last = lastNode
      if (last eq null)
        throw new IllegalStateException(
          "next must be called at least once before remove"
        )
      removeNode(last)
      lastNode = null
    }
  }

  override def clone(): AnyRef = {
    val result = new LinkedHashMap[K, V](size(), loadFactor, accessOrder)
    result.putAll(this)
    result
  }
}

object LinkedHashMap {

  private final class Node[K, V](
      key: K,
      hash: Int,
      value: V,
      previous: HashMap.Node[K, V],
      next: HashMap.Node[K, V],
      var older: Node[K, V],
      var younger: Node[K, V]
  ) extends HashMap.Node[K, V](key, hash, value, previous, next)

  // Since: Java 19
  def newLinkedHashMap[K, V](numElements: Int): LinkedHashMap[K, V] = {
    if (numElements < 0) {
      throw new IllegalArgumentException(
        s"Negative number of elements: ${numElements}"
      )
    }

    val loadFactor = 0.75f // as defined in JVM method description.

    val desiredCapacity = Math.ceil(numElements * (1.0f / loadFactor)).toInt

    val clampedCapacity = Math.clamp(desiredCapacity, 0, Integer.MAX_VALUE)

    new LinkedHashMap[K, V](clampedCapacity.toInt, loadFactor)
  }
}

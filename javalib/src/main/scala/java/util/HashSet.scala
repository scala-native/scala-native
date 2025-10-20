package java.util

import scala.collection.mutable
import ScalaOps.*

class HashSet[E]
    extends AbstractSet[E]
    with Set[E]
    with Cloneable
    with Serializable { self =>
  def this(initialCapacity: Int, loadFactor: Float) =
    this()

  def this(initialCapacity: Int) =
    this()

  def this(c: Collection[? <: E]) = {
    this()
    addAll(c)
  }

  protected val inner: mutable.Set[Box[E]] = new mutable.HashSet[Box[E]]()

  override def contains(o: Any): Boolean =
    inner.contains(Box(o.asInstanceOf[E]))

  override def remove(o: Any): Boolean =
    inner.remove(Box(o.asInstanceOf[E]))

  override def containsAll(c: Collection[?]): Boolean =
    c.iterator().scalaOps.forall(contains)

  override def removeAll(c: Collection[?]): Boolean = {
    val iter = c.iterator()
    var changed = false
    while (iter.hasNext()) changed = remove(iter.next()) || changed
    changed
  }

  override def retainAll(c: Collection[?]): Boolean = {
    val iter = iterator()
    var changed = false
    while (iter.hasNext()) {
      val value = iter.next()
      if (!c.contains(value))
        changed = remove(value) || changed
    }
    changed
  }

  override def add(e: E): Boolean =
    inner.add(Box(e))

  override def addAll(c: Collection[? <: E]): Boolean = {
    val iter = c.iterator()
    var changed = false
    while (iter.hasNext()) changed = add(iter.next()) || changed
    changed
  }

  override def clear(): Unit = inner.clear()

  override def size(): Int = inner.size

  def iterator(): Iterator[E] = {
    new Iterator[E] {
      private val iter = inner.clone.iterator

      private var last: Option[E] = None

      def hasNext(): Boolean = iter.hasNext

      def next(): E = {
        last = Some(iter.next().inner)
        last.get
      }

      override def remove(): Unit = {
        if (last.isEmpty) {
          throw new IllegalStateException()
        } else {
          last.foreach(self.remove(_))
          last = None
        }
      }
    }
  }
}

object HashSet {

  // Since: Java 19
  def newHashSet[E](numElements: Int): HashSet[E] = {
    if (numElements < 0) {
      throw new IllegalArgumentException(
        s"Negative number of elements: ${numElements}"
      )
    }

    val loadFactor = 0.75f // as defined in JVM method description.

    val desiredCapacity = Math.ceil(numElements * (1.0f / loadFactor)).toInt

    val clampedCapacity = Math.clamp(desiredCapacity, 0, Integer.MAX_VALUE)

    new HashSet[E](clampedCapacity.toInt, loadFactor)
  }
}

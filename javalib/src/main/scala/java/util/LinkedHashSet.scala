package java.util

import scala.collection.mutable

class LinkedHashSet[E]
    extends HashSet[E]
    with SequencedSet[E]
    with Cloneable
    with Serializable {
  def this(initialCapacity: Int, loadFactor: Float) =
    this()

  def this(initialCapacity: Int) =
    this()

  def this(c: java.util.Collection[_ <: E]) = {
    this()
    addAll(c)
  }

  override protected val inner: mutable.Set[Box[E]] =
    new mutable.LinkedHashSet[Box[E]]()
}

object LinkedHashSet {

  // Since: Java 19
  def newLinkedHashSet[T](numElements: Int): LinkedHashSet[T] = {
    if (numElements < 0) {
      throw new IllegalArgumentException(
        s"Negative number of elements: ${numElements}"
      )
    }

    val loadFactor = 0.75f // as defined in JVM method description.

    val desiredCapacity = Math.ceil(numElements * (1.0f / loadFactor)).toInt

    val clampedCapacity = Math.clamp(desiredCapacity, 0, Integer.MAX_VALUE)

    new LinkedHashSet[T](clampedCapacity.toInt, loadFactor)
  }
}

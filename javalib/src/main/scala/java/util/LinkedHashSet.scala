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

  def this(c: java.util.Collection[? <: E]) = {
    this()
    addAll(c)
  }

  override protected val inner: mutable.Set[Box[E]] =
    new mutable.LinkedHashSet[Box[E]]()

  /* JEP 431 Design Note:
   *
   * Implement only the abstract reversed() method required by SequencedSet.
   * Use the default SequencedCollection implementation for other JEP 431
   * methods.
   *
   * This differs from the Java 24 Javadoc, which would lead one to expect
   * more efficient local overrides here.
   *
   * It also violates, in several places, the JDK requirement:
   *   "If this set already contains the element, it is
   *   relocated if necessary so that it is first in encounter order."
   *
   * The reason for the minimal is that the current SN implementation
   * relies on scala.collection.mutable. There has been a long-standing
   * effort in javalib to remove possibly circular scala collection
   * dependencies.
   *
   * HashSet, LinkedHashSet, and LinkedHashMap all need to be re-ported
   * from Scala.js, where such dependencies have been eliminated.
   * The expected overrides of JEP 431 methods can be implemented after
   * that. If the required API for the method can be provided, no sense
   * spending time on a complicated implementation which will be thrown
   * away.
   */

  /* scala.collection.mutable.LinkedHashSet[Box[E]]() seems to not
   * have the concept of "add-to-front", so hard-fail until this class
   * can be re-written. Inherit SequencedCollection#addFirst() which
   * will throw.
   *
   *  def addFirst(e: E): Unit
   */

  override def addLast(e: E): Unit =
    inner.add(Box(e))

  /** @since JDK 21 */
  def reversed(): SequencedSet[E] =
    new ReverseOrderSequencedSetView(this)
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

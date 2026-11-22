package java.util

class LinkedHashSet[E](initialCapacity: Int, loadFactor: Float)
    extends HashSet[E](
      new LinkedHashMap[E, AnyRef](initialCapacity, loadFactor)
    )
    with SequencedSet[E]
    with Cloneable
    with Serializable {

  def this(initialCapacity: Int) =
    this(initialCapacity, HashMap.DEFAULT_LOAD_FACTOR)

  def this() =
    this(HashMap.DEFAULT_INITIAL_CAPACITY)

  def this(c: java.util.Collection[_ <: E]) = {
    this(Math.max(2 * c.size(), 11))
    addAll(c)
  }

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
   * The reason for the minimal is that the SN implementation used to
   * rely on scala.collection.mutable. There has been a long-standing
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

  /* LinkedHashMap does not have the concept of "add-to-front" yet, so
   * hard-fail until it does. Inherit SequencedCollection#addFirst() which
   * will throw.
   *
   *  def addFirst(e: E): Unit
   */

  override def addLast(e: E): Unit =
    add(e)

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

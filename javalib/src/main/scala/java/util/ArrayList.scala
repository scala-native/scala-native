package java.util

import java.io.Serializable
import java.util.function.Consumer

// Added extra private constructors to handle all of the overloads.
// To preserve method signatures, we cannot take ClassTag via implicit parameters.
// We use an Array[Any] as an underlying storage and box/unbox AnyVals when needed.
// inner: The underlying array
// _size: Keeps the track of the effective size of the underlying array. a.k.a. end index exclusive
class ArrayList[E] private (
    private[this] var inner: Array[Any],
    private[this] var _size: Int
) extends AbstractList[E]
    with List[E]
    with RandomAccess
    with Cloneable
    with Serializable {
  private def this(initialCollection: Collection[E], initialCapacity: Int) =
    this(
      {
        if (initialCapacity < 0) {
          throw new IllegalArgumentException(
            "Illegal Capacity: " + initialCapacity
          )
        }
        val initialArr =
          Array.ofDim[Any](initialCollection.size().max(initialCapacity))

        System.arraycopy(
          initialCollection.toArray(),
          0,
          initialArr,
          0,
          initialCollection.size()
        )
        initialArr
      },
      initialCollection.size()
    )

  def this(c: Collection[E]) =
    this(
      if (c != null)
        c
      else
        throw new NullPointerException,
      c.size()
    )

  def this(initialCapacity: Int) =
    this(Collections.emptyList(): Collection[E], initialCapacity)

  def this() = this(10)

  // by default, doubles the capacity. this mimicks C++ <vector> compiled by clang++-4.0.0
  private[this] def expand(): Unit = expand(inner.length * 2 max 1)

  private[this] def expand(newCapacity: Int): Unit = {
    val newArr = Array.ofDim[Any](newCapacity)
    inner.copyToArray(newArr, 0, size())
    inner = newArr
  }

  private[this] def capacity(): Int = inner.length

  def trimToSize(): Unit = expand(size())

  def ensureCapacity(minCapacity: Int): Unit =
    if (capacity() < minCapacity)
      expand(minCapacity)

  def size(): Int = _size

  // tests/compile:nativeLinkNIR fails without this override (issue: #375)
  // cannot link: @java.util.ArrayList::isEmpty_bool
  override def isEmpty(): Boolean = _size == 0

  override def indexOf(o: Any): Int = inner.indexOf(o)

  override def lastIndexOf(o: Any): Int = inner.lastIndexOf(o)

  // shallow-copy
  override def clone(): AnyRef = new ArrayList(inner, _size)

  override def toArray(): Array[AnyRef] =
    inner.slice(0, _size).map(_.asInstanceOf[AnyRef])

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] =
    if (a == null)
      throw new NullPointerException
    else if (a.length < size())
      toArray().asInstanceOf[Array[T]]
    else {
      // TODO: this copy should result in ArrayStoreException when not T >: E
      // need to detect type mismatch at runtime. related: #858
      inner.asInstanceOf[Array[T]].copyToArray(a, 0, size())
      // fill the rest of the elements in a by null as explained in JDK Javadoc
      for (i <- size() until a.length) {
        a(i) = null.asInstanceOf[T]
      }
      a
    }

  def get(index: Int): E = {
    checkIndexInBounds(index)
    inner(index).asInstanceOf[E]
  }

  override def set(index: Int, element: E): E = {
    val original = get(index)
    inner(index) = element
    original
  }

  override def add(element: E): Boolean = {
    add(size(), element)
    true
  }

  override def add(index: Int, element: E): Unit = {
    checkIndexOnBounds(index)

    if (size() >= capacity())
      expand()
    // shift each element
    for (i <- _size to (index + 1) by -1) {
      inner(i) = inner(i - 1)
    }
    inner(index) = element
    _size += 1
  }

  override def remove(index: Int): E = {
    val removed = get(index)

    // shift each element, overwriting inner(index)
    for (i <- index until (_size - 1)) {
      inner(i) = inner(i + 1)
    }
    inner(_size - 1) = null
    _size -= 1

    removed
  }

  override def remove(o: Any): Boolean =
    inner.indexOf(o) match {
      case -1 => false
      case idx =>
        remove(idx)
        true
    }

  override def removeRange(fromIndex: Int, toIndex: Int): Unit = {

    // JVM documents fromIndex == toIndex as having 'no effect'
    if (fromIndex != toIndex) {
      if ((fromIndex < 0) || (fromIndex >= _size) || (toIndex > size())
          || (toIndex < fromIndex)) {
        // N.B.: JVM docs specify IndexOutOfBounds but use de facto.
        throw new ArrayIndexOutOfBoundsException()
      } else {
        val srcIndex = toIndex
        val dstIndex = fromIndex
        val tailSize = _size - toIndex

        System.arraycopy(inner, srcIndex, inner, dstIndex, tailSize)

        _size -= (toIndex - fromIndex)
      }
    }
  }

  override def clear(): Unit = {
    // fill the content of inner by null so that the elements can be garbage collected
    for (i <- (0 until _size)) {
      inner(i) = null
    }
    _size = 0
  }

  override def spliterator(): Spliterator[E] = {
    /* Provide a more efficient spliterator.
     *
     * 'inner' has type Array[Any]. There is no Arrays.spliterator() method
     * for element type Any. Closest is AnyRef but that is not close enough.
     *
     * Default spliterator from Collection.scala is provided by
     * Spliterators.spliterator(collection) method. That uses the
     * collection-in-question's iterator: here ArrayList
     *
     * ArrayList uses an iterator() implementation inherited from
     * AbstractList.scala. That, eventually, returns a heavyweight
     * RandomAccessListIterator. Given all that, custom spliterator has
     * a good chance of having better performance, especially for large
     * collections.
     */

    new Spliterators.AbstractSpliterator[E](
      _size,
      Spliterator.SIZED | Spliterator.SUBSIZED
    ) {
      private var cursor = 0
      private val limit = _size

      def tryAdvance(action: Consumer[_ >: E]): Boolean = {
        if (cursor >= limit) false
        else {
          action.accept(inner(cursor).asInstanceOf[E])
          cursor += 1
          true
        }
      }
    }
  }
}

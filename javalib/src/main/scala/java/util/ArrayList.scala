package java.util

import java.io.Serializable

// Added extra private constructors to handle all of the overloads.
// To preserve method signatures, we cannot take ClassTag via implicit parameters.
// We use an Array[AnyRef] for underlying storage and box/unbox AnyVals where needed as the JDK class would do.
/**
 * @param inner The underlying array
 * @param _size The effective size of the underlying array. a.k.a. end index exclusive
 */
class ArrayList[E] private (private[this] var inner: Array[AnyRef],
                            private[this] var _size: Int)
    extends AbstractList[E]
    with List[E]
    with RandomAccess
    with Cloneable
    with Serializable {
  private def this(initialCollection: Collection[E], initialCapacity: Int) =
    this(
      {
        val initialArr =
          Array.ofDim[AnyRef](initialCollection.size() max initialCapacity)
        import scala.collection.JavaConverters._
        initialCollection.asScala
          .map(_.asInstanceOf[AnyRef])
          .copyToArray(initialArr)
        initialArr
      },
      initialCollection.size()
    )

  def this(c: Collection[E]) = this(c, c.size())

  def this(initialCapacity: Int) =
    this(Collections.emptyList(): Collection[E], initialCapacity)

  def this() = this(10)

  // by default, doubles the capacity. this mimicks C++ <vector> compiled by clang++-4.0.0
  private[this] def expand(): Unit = expand(inner.length * 2 max 1)

  private[this] def expand(newCapacity: Int): Unit = {
    val newArr = Array.ofDim[AnyRef](newCapacity)
    inner.copyToArray(newArr, 0, size())
    inner = newArr
  }

  private[this] def capacity(): Int = inner.length

  def trimToSize(): Unit = expand(size())

  def ensureCapacity(minCapacity: Int): Unit =
    if (capacity() < minCapacity)
      expand(minCapacity)

  def size(): Int = _size

  // tests/compile:nativeLinkNIR fails without this override for some reason
  // cannot link: @java.util.ArrayList::isEmpty_bool
  override def isEmpty(): Boolean = _size == 0

  override def indexOf(o: Any): Int = inner.indexOf(o)

  override def lastIndexOf(o: Any): Int = inner.lastIndexOf(o)

  // shallow-copy
  override def clone(): AnyRef = new ArrayList(inner, _size)

  override def toArray(): Array[AnyRef] = {
    val result = Array.ofDim[AnyRef](size())
    inner.copyToArray(result, 0, size())
    result
  }

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] =
    if (a.length < size())
      toArray().asInstanceOf[Array[T]]
    else {
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
    inner(index) = element.asInstanceOf[AnyRef]
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
    inner(index) = element.asInstanceOf[AnyRef]
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

  override def clear(): Unit = {
    // fill the content of inner by null so that the elements can be garbage collected
    for (i <- (0 until _size)) {
      inner(i) = null
    }
    _size = 0
  }

  // define for better performance
  //override def contains(o: Any): Boolean =
  //override def addAll(c: Collection[_ <: E]): Boolean =
  //override def addAll(index: Index, c: Collection[_ <: E]): Boolean =
  //override def removeRange(fromIndex: Int, toIndex: Int): Boolean =
  //override def removeAll(c: Collection[_ <: AnyRef]): Boolean =
  //override def retainAll(c: Collection[_ <: AnyRef]): Boolean =
  //override def listIterator(index: Int): ListIterator[E] =
  //override def listIterator(): ListIterator[E] =
  //override def iterator(): Iterator[E] =
  //override def subList(fromIndex: Int, toIndex: Int): List[E] =

  // JDK 1.8
  // def forEach(action: Consumer[_ >: E]): Unit =
  // def spliterator(): Spliterator[E] =
  // def removeIf(filter: Predicate[_ >: E]): Boolean =
  // def replaceAll(operator: UnaryOperator[E]): Unit =
  // def sort(c: Comparator[_ >: E]): Unit =
}

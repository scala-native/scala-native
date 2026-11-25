package java.util

import java.io.Serializable
import java.util.function.{Consumer, UnaryOperator}

// Added extra private constructors to handle all of the overloads.
// To preserve method signatures, we cannot take ClassTag via implicit parameters.
// We use an Array[Any] as an underlying storage and box/unbox AnyVals when needed.
// inner: The underlying array
// _size: Keeps the track of the effective size of the underlying array. a.k.a. end index exclusive
class ArrayList[E] private (
    private var inner: Array[Any],
    private var _size: Int
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

  // by default, doubles the capacity. this mimics C++ <vector> compiled by clang++-4.0.0
  private def expand(): Unit = expand(inner.length * 2 max 1)

  private def expand(newCapacity: Int): Unit = {
    modCount += 1
    val newArr = Array.ofDim[Any](newCapacity)
    inner.copyToArray(newArr, 0, size())
    inner = newArr
  }

  private def capacity(): Int = inner.length

  def trimToSize(): Unit = expand(size())

  def ensureCapacity(minCapacity: Int): Unit =
    if (capacity() < minCapacity)
      expand(minCapacity)

  def size(): Int = _size

  // tests/compile:nativeLinkNIR fails without this override (issue: #375)
  // cannot link: @java.util.ArrayList::isEmpty_bool
  override def isEmpty(): Boolean = _size == 0

  override def indexOf(o: Any): Int = {
    var i = 0
    while (i < _size) {
      if (Objects.equals(o, inner(i))) return i
      i += 1
    }
    -1
  }

  override def lastIndexOf(o: Any): Int = {
    var i = _size - 1
    while (i >= 0) {
      if (Objects.equals(o, inner(i))) return i
      i -= 1
    }
    -1
  }

  // shallow-copy
  override def clone(): AnyRef = new ArrayList(inner.clone(), _size)

  override def toArray(): Array[AnyRef] =
    inner.slice(0, _size).map(_.asInstanceOf[AnyRef])

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    Objects.requireNonNull(a, "a")

    if (a.length < size())
      Arrays.copyOf[T, AnyRef](
        inner.asInstanceOf[Array[AnyRef]],
        size(),
        a.getClass()
      )
    else {
      System.arraycopy(inner, 0, a, 0, size())

      // fill only the first slot, if any, with a null - see JDK javadoc
      if (size() < a.length)
        a(size()) = null.asInstanceOf[T]

      a
    }
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
    modCount += 1
  }

  override def remove(index: Int): E = {
    val removed = get(index)

    // shift each element, overwriting inner(index)
    for (i <- index until (_size - 1)) {
      inner(i) = inner(i + 1)
    }
    inner(_size - 1) = null
    _size -= 1
    modCount += 1

    removed
  }

  override def addAll(c: Collection[_ <: E]): Boolean =
    addAll(_size, c)

  override def addAll(index: Int, c: Collection[_ <: E]): Boolean = {
    checkIndexOnBounds(index)
    // Copy first: `c` may be this list or one of its views
    val added = c.toArray()
    val count = added.length
    modCount += 1
    if (count == 0) false
    else {
      val newSize = _size + count
      if (newSize > capacity())
        expand(Math.max(capacity() * 2, newSize))
      System.arraycopy(inner, index, inner, index + count, _size - index)
      System.arraycopy(added, 0, inner, index, count)
      _size = newSize
      true
    }
  }

  override def removeAll(c: Collection[_]): Boolean =
    batchRemove(c, retain = false)

  override def retainAll(c: Collection[_]): Boolean =
    batchRemove(c, retain = true)

  /* Decides about every element before changing the list, so that `c` may be
   * a view of this list.
   */
  private def batchRemove(c: Collection[_], retain: Boolean): Boolean = {
    Objects.requireNonNull(c)
    val elems = inner
    val size = _size
    var first = 0
    while (first < size && c.contains(elems(first)) == retain)
      first += 1
    if (first == size) false
    else {
      val keep = new Array[Boolean](size - first)
      var i = first + 1
      while (i < size) {
        keep(i - first) = c.contains(elems(i)) == retain
        i += 1
      }
      var w = first
      i = first + 1
      while (i < size) {
        if (keep(i - first)) {
          elems(w) = elems(i)
          w += 1
        }
        i += 1
      }
      java.util.Arrays.fill(elems.asInstanceOf[Array[AnyRef]], w, size, null)
      _size = w
      modCount += 1
      true
    }
  }

  override def remove(o: Any): Boolean = {
    val idx = indexOf(o)
    if (idx >= 0) {
      remove(idx)
      true
    } else false
  }

  override private[util] def subListsDecideBeforeRemoving: Boolean = true

  override def removeRange(fromIndex: Int, toIndex: Int): Unit = {
    modCount += 1

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

        val newSize = _size - (toIndex - fromIndex)
        java.util.Arrays.fill(
          inner.asInstanceOf[Array[AnyRef]],
          newSize,
          _size,
          null
        )
        _size = newSize
      }
    }
  }

  override def clear(): Unit = {
    modCount += 1
    // fill the content of inner with null so that the elements can be garbage collected
    java.util.Arrays.fill(inner.asInstanceOf[Array[AnyRef]], 0, _size, null)
    _size = 0
  }

  override def iterator(): Iterator[E] = new Itr(0)

  override def listIterator(): ListIterator[E] = new Itr(0)

  override def listIterator(index: Int): ListIterator[E] = {
    checkIndexOnBounds(index)
    new Itr(index)
  }

  override def forEach(action: Consumer[_ >: E]): Unit = {
    Objects.requireNonNull(action)
    val expectedModCount = modCount
    val elems = inner
    val size = _size
    var i = 0
    while (modCount == expectedModCount && i < size) {
      action.accept(elems(i).asInstanceOf[E])
      i += 1
    }
    if (modCount != expectedModCount)
      throw new ConcurrentModificationException()
  }

  override def replaceAll(operator: UnaryOperator[E]): Unit = {
    Objects.requireNonNull(operator)
    val expectedModCount = modCount
    val elems = inner
    val size = _size
    var i = 0
    while (modCount == expectedModCount && i < size) {
      elems(i) = operator.apply(elems(i).asInstanceOf[E])
      i += 1
    }
    if (modCount != expectedModCount)
      throw new ConcurrentModificationException()
    modCount += 1
  }

  override def sort(c: Comparator[_ >: E]): Unit = {
    val expectedModCount = modCount
    java.util.Arrays.sort[AnyRef & E](
      inner.asInstanceOf[Array[AnyRef & E]],
      0,
      _size,
      c
    )
    if (modCount != expectedModCount)
      throw new ConcurrentModificationException()
    modCount += 1
  }

  private final class Itr(private var cursor: Int) extends ListIterator[E] {
    private var lastRet = -1
    private var expectedModCount = modCount

    @inline
    private def checkForComodification(): Unit =
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException()

    def hasNext(): Boolean = cursor != _size

    def next(): E = {
      checkForComodification()
      val i = cursor
      if (i >= _size)
        throw new NoSuchElementException()
      cursor = i + 1
      lastRet = i
      inner(i).asInstanceOf[E]
    }

    def hasPrevious(): Boolean = cursor != 0

    def previous(): E = {
      checkForComodification()
      val i = cursor - 1
      if (i < 0)
        throw new NoSuchElementException()
      cursor = i
      lastRet = i
      inner(i).asInstanceOf[E]
    }

    def nextIndex(): Int = cursor

    def previousIndex(): Int = cursor - 1

    override def remove(): Unit = {
      if (lastRet < 0)
        throw new IllegalStateException()
      checkForComodification()
      ArrayList.this.remove(lastRet)
      cursor = lastRet
      lastRet = -1
      expectedModCount = modCount
    }

    def set(e: E): Unit = {
      if (lastRet < 0)
        throw new IllegalStateException()
      checkForComodification()
      inner(lastRet) = e
    }

    def add(e: E): Unit = {
      checkForComodification()
      ArrayList.this.add(cursor, e)
      cursor += 1
      lastRet = -1
      expectedModCount = modCount
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      val size = _size
      var i = cursor
      if (i < size) {
        val elems = inner
        while (modCount == expectedModCount && i < size) {
          action.accept(elems(i).asInstanceOf[E])
          i += 1
        }
        cursor = i
        lastRet = i - 1
        checkForComodification()
      }
    }
  }

  override def spliterator(): Spliterator[E] =
    new ArrayListSpliterator(0, -1, 0)

  /* Late-binding: the fence and the expected modCount are bound on the
   * first traversal, split or size estimate.
   */
  private final class ArrayListSpliterator(
      private var index: Int,
      private var fence: Int,
      private var expectedModCount: Int
  ) extends Spliterator[E] {

    private def getFence(): Int = {
      if (fence < 0) {
        expectedModCount = modCount
        fence = _size
      }
      fence
    }

    def trySplit(): Spliterator[E] = {
      val hi = getFence()
      val lo = index
      val mid = (lo + hi) >>> 1
      if (lo >= mid) null
      else {
        index = mid
        new ArrayListSpliterator(lo, mid, expectedModCount)
      }
    }

    def tryAdvance(action: Consumer[_ >: E]): Boolean = {
      Objects.requireNonNull(action)
      val hi = getFence()
      val i = index
      if (i >= hi) false
      else {
        index = i + 1
        action.accept(inner(i).asInstanceOf[E])
        if (modCount != expectedModCount)
          throw new ConcurrentModificationException()
        true
      }
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      val hi = getFence()
      val elems = inner
      var i = index
      index = hi
      while (i < hi && modCount == expectedModCount) {
        action.accept(elems(i).asInstanceOf[E])
        i += 1
      }
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException()
    }

    def estimateSize(): Long = (getFence() - index).toLong

    def characteristics(): Int =
      Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED
  }
}

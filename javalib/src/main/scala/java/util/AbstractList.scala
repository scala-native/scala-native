package java.util

import java.util.function.Predicate

import scala.annotation.tailrec

abstract class AbstractList[E] protected ()
    extends AbstractCollection[E]
    with List[E] { self =>

  @transient
  protected[util] var modCount: Int = 0

  override def add(element: E): Boolean = {
    add(size(), element)
    true
  }

  // tests/compile:nativeLinkNIR fails without this re-declaration (issue: #375)
  // cannot link: @java.util.AbstractList::get_i32_java.lang.Object
  def get(index: Int): E

  def set(index: Int, element: E): E =
    throw new UnsupportedOperationException

  def add(index: Int, element: E): Unit =
    throw new UnsupportedOperationException

  override def remove(index: Int): E =
    throw new UnsupportedOperationException

  def indexOf(o: Any): Int = {
    var idx = -1
    var i = 0
    val iter = listIterator()
    while (idx == -1 && iter.hasNext()) {
      if (iter.next() === o) idx = i
      i += 1
    }
    idx
  }

  def lastIndexOf(o: Any): Int = {
    @tailrec
    def findIndex(iter: ListIterator[E]): Int = {
      if (!iter.hasPrevious()) -1
      else if (iter.previous() === o) iter.nextIndex()
      else findIndex(iter)
    }
    findIndex(listIterator(size()))
  }

  override def clear(): Unit =
    removeRange(0, size())

  def addAll(index: Int, c: Collection[_ <: E]): Boolean = {
    checkIndexOnBounds(index)
    var i = index
    val iter = c.iterator()
    while (iter.hasNext()) {
      add(i, iter.next())
      i += 1
    }
    !c.isEmpty()
  }

  def iterator(): Iterator[E] =
    listIterator()

  def listIterator(): ListIterator[E] =
    listIterator(0)

  def listIterator(index: Int): ListIterator[E] = {
    checkIndexOnBounds(index)
    new AbstractListIterator(self, index)
  }

  def subList(fromIndex: Int, toIndex: Int): List[E] = {
    AbstractList.checkSubListRange(fromIndex, toIndex, size())
    self match {
      case _: RandomAccess =>
        new RandomAccessSubList(self, fromIndex, toIndex - fromIndex)
      case _ =>
        new SubList(self, fromIndex, toIndex - fromIndex)
    }
  }

  override def equals(o: Any): Boolean = {
    if (o.asInstanceOf[AnyRef] eq this) {
      true
    } else {
      o match {
        case o: List[_] =>
          val oIter = o.listIterator()
          val iter = listIterator()
          var result = true

          while (result && iter.hasNext() && oIter.hasNext()) {
            result = iter.next() === oIter.next()
          }

          result && !iter.hasNext() && !oIter.hasNext()
        case _ => false
      }
    }
  }

  override def hashCode(): Int = {
    var hash = 1
    val iter = listIterator()
    while (iter.hasNext()) {
      val elem = iter.next()
      hash = 31 * hash + (if (elem == null) 0 else elem.hashCode)
    }
    hash
  }

  protected def removeRange(fromIndex: Int, toIndex: Int): Unit = {
    // JVM documents fromIndex as inclusive, toIndex as exclusive.
    // Someday the arguments should be bounds checked.
    var i = fromIndex
    val iter = listIterator(fromIndex)
    while (iter.hasNext() && (i < toIndex)) {
      iter.next()
      iter.remove()
      i += 1
    }
  }

  private[util] final def removeRangeFromSubList(
      fromIndex: Int,
      toIndex: Int
  ): Unit =
    removeRange(fromIndex, toIndex)

  /* Whether bulk removals on sublists of this list decide about every
   * element before removing any, so that the argument may be another view
   * of this list.
   */
  private[util] def subListsDecideBeforeRemoving: Boolean = false

  protected def checkIndexInBounds(index: Int): Unit = {
    if (index < 0 || index >= size())
      throw new IndexOutOfBoundsException(index.toString)
  }

  protected def checkIndexOnBounds(index: Int): Unit = {
    if (index < 0 || index > size())
      throw new IndexOutOfBoundsException(index.toString)
  }
}

private[util] object AbstractList {
  def checkSubListRange(fromIndex: Int, toIndex: Int, size: Int): Unit = {
    if (fromIndex < 0)
      throw new IndexOutOfBoundsException(fromIndex.toString)
    else if (toIndex > size)
      throw new IndexOutOfBoundsException(toIndex.toString)
    else if (fromIndex > toIndex)
      throw new IllegalArgumentException
  }
}

/* Fail-fast ListIterator based on the random access operations of the list.
 * Structural modifications made outside of this iterator are detected by
 * comparing the modCount of the list with the value it had when this
 * iterator last changed the list.
 */
private[util] class AbstractListIterator[E](
    list: AbstractList[E],
    private var cursor: Int
) extends ListIterator[E] {

  private var lastRet = -1
  private var expectedModCount = list.modCount

  protected def currentSize(): Int = list.size()

  protected def elementAt(index: Int): E = list.get(index)

  @inline
  private def checkForComodification(): Unit =
    if (list.modCount != expectedModCount)
      throw new ConcurrentModificationException()

  def hasNext(): Boolean = cursor != currentSize()

  def next(): E = {
    checkForComodification()
    val i = cursor
    if (i >= currentSize())
      throw new NoSuchElementException()
    val elem = elementAt(i)
    lastRet = i
    cursor = i + 1
    elem
  }

  def hasPrevious(): Boolean = cursor != 0

  def previous(): E = {
    checkForComodification()
    val i = cursor - 1
    if (i < 0 || i >= currentSize())
      throw new NoSuchElementException()
    val elem = elementAt(i)
    lastRet = i
    cursor = i
    elem
  }

  def nextIndex(): Int = cursor

  def previousIndex(): Int = cursor - 1

  override def remove(): Unit = {
    if (lastRet < 0)
      throw new IllegalStateException()
    checkForComodification()
    list.remove(lastRet)
    if (lastRet < cursor)
      cursor -= 1
    lastRet = -1
    expectedModCount = list.modCount
  }

  def set(e: E): Unit = {
    if (lastRet < 0)
      throw new IllegalStateException()
    checkForComodification()
    list.set(lastRet, e)
    expectedModCount = list.modCount
  }

  def add(e: E): Unit = {
    checkForComodification()
    list.add(cursor, e)
    lastRet = -1
    cursor += 1
    expectedModCount = list.modCount
  }
}

/* View returned by AbstractList.subList. Every operation first checks that
 * the parent was not structurally modified other than through this view.
 */
private[util] class SubList[E](
    protected val parent: AbstractList[E],
    protected val offset: Int,
    private var _size: Int
) extends AbstractList[E] {

  /* Every structural change, made through any view, reaches the root list.
   * Tracking its modCount detects changes made through an ancestor or a
   * sibling of this view, not only through the parent.
   */
  private val root: AbstractList[E] = parent match {
    case p: SubList[E @unchecked] => p.root
    case _                        => parent
  }

  // A view made from a stale parent is stale too.
  private var expectedRootModCount: Int = parent match {
    case p: SubList[E @unchecked] => p.expectedRootModCount
    case _                        => root.modCount
  }

  @inline
  protected final def checkForComodification(): Unit =
    if (root.modCount != expectedRootModCount)
      throw new ConcurrentModificationException()

  private def updateSizeAndModCount(delta: Int): Unit = {
    val rootChanged = root.modCount != expectedRootModCount
    expectedRootModCount = root.modCount
    _size += delta
    if (delta != 0 || rootChanged)
      modCount += 1
  }

  private[util] final def uncheckedSize(): Int = _size

  def size(): Int = {
    checkForComodification()
    _size
  }

  def get(index: Int): E = {
    Objects.checkIndex(index, _size)
    checkForComodification()
    parent.get(offset + index)
  }

  override def set(index: Int, element: E): E = {
    Objects.checkIndex(index, _size)
    checkForComodification()
    parent.set(offset + index, element)
  }

  override def add(index: Int, element: E): Unit = {
    checkIndexForAdd(index)
    checkForComodification()
    parent.add(offset + index, element)
    updateSizeAndModCount(1)
  }

  override def remove(index: Int): E = {
    Objects.checkIndex(index, _size)
    checkForComodification()
    val elem = parent.remove(offset + index)
    updateSizeAndModCount(-1)
    elem
  }

  override protected def removeRange(fromIndex: Int, toIndex: Int): Unit = {
    checkForComodification()
    parent.removeRangeFromSubList(offset + fromIndex, offset + toIndex)
    updateSizeAndModCount(fromIndex - toIndex)
  }

  override def addAll(c: Collection[_ <: E]): Boolean =
    addAll(_size, c)

  override def removeAll(c: Collection[_]): Boolean =
    if (root.subListsDecideBeforeRemoving) {
      Objects.requireNonNull(c)
      removeDecided(c.contains(_))
    } else super.removeAll(c)

  override def retainAll(c: Collection[_]): Boolean =
    if (root.subListsDecideBeforeRemoving) {
      Objects.requireNonNull(c)
      removeDecided(!c.contains(_))
    } else super.retainAll(c)

  override def removeIf(filter: Predicate[_ >: E]): Boolean =
    if (root.subListsDecideBeforeRemoving) {
      Objects.requireNonNull(filter)
      removeDecided(filter.test(_))
    } else super.removeIf(filter)

  private def removeDecided(shouldRemove: E => Boolean): Boolean = {
    checkForComodification()
    val expectedModCount = modCount
    val n = _size
    val remove = new Array[Boolean](n)
    var any = false
    var i = 0
    // Stop once the decision changed this view: its elements have moved.
    while (i < n && modCount == expectedModCount) {
      if (shouldRemove(get(i))) {
        remove(i) = true
        any = true
      }
      i += 1
    }
    checkForComodification()
    if (modCount != expectedModCount)
      throw new ConcurrentModificationException()
    // Remove runs from the end, so that the indices of earlier ones hold.
    var to = n
    while (to > 0) {
      if (!remove(to - 1)) to -= 1
      else {
        var from = to - 1
        while (from > 0 && remove(from - 1)) from -= 1
        removeRange(from, to)
        to = from
      }
    }
    any
  }

  override def addAll(index: Int, c: Collection[_ <: E]): Boolean = {
    checkIndexForAdd(index)
    val count = c.size()
    if (count == 0) false
    else {
      checkForComodification()
      parent.addAll(offset + index, c)
      updateSizeAndModCount(count)
      true
    }
  }

  override def listIterator(index: Int): ListIterator[E] = {
    checkForComodification()
    checkIndexForAdd(index)
    val view = this
    val it = parent.listIterator(offset + index)
    new ListIterator[E] {
      def hasNext(): Boolean = nextIndex() < view._size

      def next(): E =
        if (hasNext()) it.next()
        else throw new NoSuchElementException()

      def hasPrevious(): Boolean = previousIndex() >= 0

      def previous(): E =
        if (hasPrevious()) it.previous()
        else throw new NoSuchElementException()

      def nextIndex(): Int = it.nextIndex() - offset

      def previousIndex(): Int = it.previousIndex() - offset

      override def remove(): Unit = {
        it.remove()
        view.updateSizeAndModCount(-1)
      }

      def set(e: E): Unit = it.set(e)

      def add(e: E): Unit = {
        it.add(e)
        view.updateSizeAndModCount(1)
      }
    }
  }

  override def subList(fromIndex: Int, toIndex: Int): List[E] = {
    AbstractList.checkSubListRange(fromIndex, toIndex, _size)
    this match {
      case _: RandomAccess =>
        new RandomAccessSubList(this, fromIndex, toIndex - fromIndex)
      case _ =>
        new SubList(this, fromIndex, toIndex - fromIndex)
    }
  }

  private def checkIndexForAdd(index: Int): Unit =
    if (index < 0 || index > _size)
      throw new IndexOutOfBoundsException(index.toString)
}

private[util] class RandomAccessSubList[E](
    parentList: AbstractList[E],
    fromIndex: Int,
    initialSize: Int
) extends SubList[E](parentList, fromIndex, initialSize)
    with RandomAccess {

  override def listIterator(index: Int): ListIterator[E] = {
    checkForComodification()
    if (index < 0 || index > uncheckedSize())
      throw new IndexOutOfBoundsException(index.toString)
    new AbstractListIterator(this, index) {
      override protected def currentSize(): Int = uncheckedSize()

      override protected def elementAt(index: Int): E = {
        checkForComodification()
        parent.get(offset + index)
      }
    }
  }
}

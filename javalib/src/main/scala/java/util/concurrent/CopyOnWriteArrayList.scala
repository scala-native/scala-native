// Ported from Scala.js commit: e7f1ff7 dated: 2022-06-01
// Modified to use ju.ArrayList instead of js.Array

package java.util.concurrent

import java.lang.{Cloneable, reflect => jlr}
import java.util._
import java.util.function.{Predicate, UnaryOperator}

import scala.annotation.tailrec

class CopyOnWriteArrayList[E <: AnyRef] private (
    private var inner: ArrayList[E]
) extends List[E]
    with RandomAccess
    with Cloneable
    with Serializable {
  self =>

  // requiresCopyOnWrite is false if and only if no other object
  // (like the iterator) may have a reference to inner
  private var requiresCopyOnWrite = false

  def this() = {
    this(new ArrayList[E])
  }

  def this(c: Collection[_ <: E]) = {
    this()
    addAll(c)
  }

  def this(toCopyIn: Array[E]) = {
    this()
    for (i <- 0 until toCopyIn.length)
      add(toCopyIn(i))
  }

  def size(): Int =
    inner.size()

  def isEmpty(): Boolean =
    size() == 0

  def contains(o: scala.Any): Boolean =
    inner.contains(o)

  def indexOf(o: scala.Any): Int =
    indexOf(o.asInstanceOf[E], 0)

  def indexOf(e: E, index: Int): Int = {
    @tailrec
    def findIndex(iter: ListIterator[E]): Int = {
      if (!iter.hasNext()) -1
      else if (Objects.equals(iter.next(), e)) iter.previousIndex()
      else findIndex(iter)
    }
    findIndex(listIterator(index))
  }

  def lastIndexOf(o: scala.Any): Int =
    lastIndexOf(o.asInstanceOf[E], 0)

  def lastIndexOf(e: E, index: Int): Int = {
    @tailrec
    def findIndex(iter: ListIterator[E]): Int = {
      if (!iter.hasPrevious()) -1
      else if (Objects.equals(iter.previous(), e)) iter.nextIndex()
      else findIndex(iter)
    }
    findIndex(listIterator(size()))
  }

  override def clone(): AnyRef =
    new CopyOnWriteArrayList[E](this)

  def toArray(): Array[AnyRef] =
    toArray(new Array[AnyRef](size()))

  def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    val componentType = a.getClass.getComponentType
    val toFill: Array[T] =
      if (a.length >= size()) a
      else jlr.Array.newInstance(componentType, size()).asInstanceOf[Array[T]]

    val iter = iterator()
    for (i <- 0 until size())
      toFill(i) = iter.next().asInstanceOf[T]
    if (toFill.length > size())
      toFill(size()) = null.asInstanceOf[T]
    toFill
  }

  def get(index: Int): E = {
    checkIndexInBounds(index)
    innerGet(index)
  }

  def set(index: Int, element: E): E = {
    checkIndexInBounds(index)
    copyIfNeeded()
    val oldValue = innerGet(index)
    innerSet(index, element)
    oldValue
  }

  def add(e: E): Boolean = {
    copyIfNeeded()
    innerPush(e)
    true
  }

  def add(index: Int, element: E): Unit = {
    checkIndexOnBounds(index)
    copyIfNeeded()
    innerInsert(index, element)
  }

  def remove(index: Int): E = {
    checkIndexInBounds(index)
    copyIfNeeded()
    innerRemove(index)
  }

  def remove(o: scala.Any): Boolean = {
    val index = indexOf(o)
    if (index == -1) false
    else {
      remove(index)
      true
    }
  }

  def addIfAbsent(e: E): Boolean = {
    if (contains(e)) false
    else {
      copyIfNeeded()
      innerPush(e)
      true
    }
  }

  def containsAll(c: Collection[_]): Boolean =
    inner.containsAll(c)

  def removeAll(c: Collection[_]): Boolean = {
    copyIfNeeded()
    inner.removeAll(c)
  }

  def retainAll(c: Collection[_]): Boolean = {
    copyIfNeeded()
    inner.retainAll(c)
  }

  def addAllAbsent(c: Collection[_ <: E]): Int = {
    var added = 0
    c.forEach { e =>
      if (addIfAbsent(e))
        added += 1
    }
    added
  }

  def clear(): Unit = {
    inner = new ArrayList[E]
    requiresCopyOnWrite = false
  }

  def addAll(c: Collection[_ <: E]): Boolean =
    addAll(size(), c)

  def addAll(index: Int, c: Collection[_ <: E]): Boolean = {
    checkIndexOnBounds(index)
    copyIfNeeded()
    innerInsertMany(index, c)
    !c.isEmpty()
  }

  /* Override Collection.removeIf() because our iterators do not support
   * the `remove()` method.
   */
  override def removeIf(filter: Predicate[_ >: E]): Boolean = {
    // scalastyle:off return
    /* The outer loop iterates as long as no element passes the filter (and
     * hence no modification is required).
     */
    val iter = iterator()
    var index = 0
    while (iter.hasNext()) {
      if (filter.test(iter.next())) {
        /* We found the first element that needs to be removed: copy and
         * truncate at the current index.
         */
        copyIfNeeded()
        innerRemoveMany(index, size() - index)
        /* Now keep iterating, but push elements that do not pass the test.
         * `index` is useless from now on, so do not keep updating it.
         */
        while (iter.hasNext()) {
          val elem = iter.next()
          if (!filter.test(elem))
            innerPush(elem)
        }
        return true
      }
      index += 1
    }
    false // the outer loop finished without entering the inner one
    // scalastyle:on return
  }

  override def replaceAll(operator: UnaryOperator[E]): Unit = {
    val size = this.size()
    if (size != 0) {
      copyIfNeeded()
      var i = 0
      while (i != size) {
        innerSet(i, operator.apply(innerGet(i)))
        i += 1
      }
    }
  }

  override def toString: String =
    inner.toString()

  override def equals(obj: Any): Boolean = {
    if (obj.asInstanceOf[AnyRef] eq this) {
      true
    } else {
      inner.equals(obj)
    }
  }

  override def hashCode(): Int =
    inner.hashCode()

  def iterator(): Iterator[E] =
    listIterator()

  def listIterator(): ListIterator[E] =
    listIterator(0)

  def listIterator(index: Int): ListIterator[E] = {
    checkIndexOnBounds(index)
    new CopyOnWriteArrayListIterator[E](innerSnapshot(), index, 0, size())
  }

  def subList(fromIndex: Int, toIndex: Int): List[E] = {
    if (fromIndex < 0 || fromIndex > toIndex || toIndex > size())
      throw new IndexOutOfBoundsException
    new CopyOnWriteArrayListView(fromIndex, toIndex)
  }

  protected def innerGet(index: Int): E =
    inner.get(index)

  protected def innerSet(index: Int, elem: E): Unit =
    inner.set(index, elem)

  protected def innerPush(elem: E): Unit =
    inner.add(elem)

  protected def innerInsert(index: Int, elem: E): Unit =
    inner.add(index, elem)

  protected def innerInsertMany(index: Int, items: Collection[_ <: E]): Unit =
    inner.addAll(index, items)

  protected def innerRemove(index: Int): E =
    inner.remove(index)

  protected def innerRemoveMany(index: Int, count: Int): Unit =
    inner.removeRange(index, index + count)

  protected def copyIfNeeded(): Unit = {
    if (requiresCopyOnWrite) {
      inner = new ArrayList(inner)
      requiresCopyOnWrite = false
    }
  }

  protected def innerSnapshot(): ArrayList[E] = {
    requiresCopyOnWrite = true
    inner
  }

  private class CopyOnWriteArrayListView(
      fromIndex: Int,
      private var toIndex: Int
  ) extends CopyOnWriteArrayList[E](null: ArrayList[E]) {
    viewSelf =>

    override def size(): Int =
      toIndex - fromIndex

    override def clear(): Unit = {
      copyIfNeeded()
      self.innerRemoveMany(fromIndex, size())
      changeSize(-size())
    }

    override def listIterator(index: Int): ListIterator[E] = {
      checkIndexOnBounds(index)
      new CopyOnWriteArrayListIterator[E](
        innerSnapshot(),
        fromIndex + index,
        fromIndex,
        toIndex
      ) {
        override protected def onSizeChanged(delta: Int): Unit =
          viewSelf.changeSize(delta)
      }
    }

    override def subList(fromIndex: Int, toIndex: Int): List[E] = {
      if (fromIndex < 0 || fromIndex > toIndex || toIndex > size())
        throw new IndexOutOfBoundsException

      new CopyOnWriteArrayListView(
        viewSelf.fromIndex + fromIndex,
        viewSelf.fromIndex + toIndex
      ) {
        override protected def changeSize(delta: Int): Unit = {
          super.changeSize(delta)
          viewSelf.changeSize(delta)
        }
      }
    }

    override def clone(): AnyRef =
      new CopyOnWriteArrayList[E](this)

    override protected def innerGet(index: Int): E =
      self.innerGet(fromIndex + index)

    override protected def innerSet(index: Int, elem: E): Unit =
      self.innerSet(fromIndex + index, elem)

    override protected def innerPush(elem: E): Unit = {
      changeSize(1)
      self.innerInsert(toIndex - 1, elem)
    }

    override protected def innerInsert(index: Int, elem: E): Unit = {
      changeSize(1)
      self.innerInsert(fromIndex + index, elem)
    }

    override protected def innerInsertMany(
        index: Int,
        items: Collection[_ <: E]
    ): Unit = {
      changeSize(items.size())
      self.innerInsertMany(fromIndex + index, items)
    }

    override protected def innerRemove(index: Int): E = {
      changeSize(-1)
      self.innerRemove(fromIndex + index)
    }

    override protected def innerRemoveMany(index: Int, count: Int): Unit = {
      changeSize(-count)
      self.innerRemoveMany(index, count)
    }

    override protected def copyIfNeeded(): Unit =
      self.copyIfNeeded()

    override protected def innerSnapshot(): ArrayList[E] =
      self.innerSnapshot()

    protected def changeSize(delta: Int): Unit =
      toIndex += delta
  }

  protected def checkIndexInBounds(index: Int): Unit = {
    if (index < 0 || index >= size())
      throw new IndexOutOfBoundsException(index.toString)
  }

  protected def checkIndexOnBounds(index: Int): Unit = {
    if (index < 0 || index > size())
      throw new IndexOutOfBoundsException(index.toString)
  }
}

private class CopyOnWriteArrayListIterator[E](
    arraySnapshot: ArrayList[E],
    i: Int,
    start: Int,
    end: Int
) extends AbstractRandomAccessListIterator[E](i, start, end) {
  override def remove(): Unit =
    throw new UnsupportedOperationException

  override def set(e: E): Unit =
    throw new UnsupportedOperationException

  override def add(e: E): Unit =
    throw new UnsupportedOperationException

  protected def get(index: Int): E =
    arraySnapshot.get(index)

  protected def remove(index: Int): Unit =
    throw new UnsupportedOperationException

  protected def set(index: Int, e: E): Unit =
    throw new UnsupportedOperationException

  protected def add(index: Int, e: E): Unit =
    throw new UnsupportedOperationException
}

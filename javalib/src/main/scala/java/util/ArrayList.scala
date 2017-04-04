package java.util

import java.util.function.{Consumer, Predicate, UnaryOperator}

import scala.collection.mutable.ArrayBuffer

class ArrayList[E] private (var underlying: ArrayBuffer[E])
    extends AbstractList[E]
    with List[E]
    with RandomAccess
    with Cloneable
    with Serializable {
  def this(initialCapacity: Int) =
    this(new ArrayBuffer[E](initialCapacity))

  def this() = this(10)

  def this(c: Collection[_ <: E]) =
    this(ArrayList.toArrayBuffer[E](c))

  // TODO:
  // def trimToSize(): Unit =
  //   ???

  // TODO:
  // def ensureCapacity(minCapacity: Int): Unit =
  //   ???

  def size(): Int =
    underlying.size

  override def isEmpty(): Boolean =
    underlying.isEmpty

  override def contains(o: Any): Boolean =
    underlying.contains(o)

  override def indexOf(o: Any): Int =
    underlying.indexOf(o)

  override def lastIndexOf(o: Any): Int =
    underlying.lastIndexOf(o)

  override def clone(): Object =
    new ArrayList(underlying.clone())

  override def toArray(): Array[Object] = {
    val array = new Array[Object](size())
    underlying.zipWithIndex.foreach {
      case (v: Object, i) => array(i) = v
    }
    array
  }

  // TODO:
  // override def toArray[T](a: Array[T]): Array[T] =
  //   ???

  def get(index: Int): E =
    underlying(index)

  override def set(index: Int, element: E): E = {
    underlying(index) = element
    element
  }

  override def add(e: E): Boolean = {
    underlying += e
    true
  }

  override def add(index: Int, element: E): Unit =
    underlying = underlying.patch(index, Seq(element), 0)

  override def remove(index: Int): E =
    underlying.remove(index)

  def remove(o: Object): Boolean = {
    val idx = indexOf(o)
    if (idx == -1) false
    else {
      remove(idx)
      true
    }
  }

  override def clear(): Unit =
    underlying.clear()

  override def addAll(c: Collection[_ <: E]): Boolean =
    addAll(size(), c)

  override def addAll(index: Int, c: Collection[_ <: E]): Boolean = {
    val elements = ArrayList.toArrayBuffer(c)
    underlying = underlying.patch(index, elements, 0)
    true
  }

  override def removeRange(fromIndex: Int, toIndex: Int): Unit = {
    val count = toIndex - fromIndex + 1
    underlying.remove(fromIndex, count)
  }

  override def removeAll(c: Collection[_]): Boolean = {
    val it      = c.iterator()
    var removed = false
    while (it.hasNext()) {
      removed = remove(it.next()) || removed
    }
    removed
  }

  override def retainAll(c: Collection[_]): Boolean = {
    val elements = ArrayList.toArrayBuffer(c)
    val it       = iterator()
    var removed  = false
    while (it.hasNext()) {
      val element = it.next()
      if (!elements.contains(element)) {
        remove(element)
        removed = true
      }
    }
    removed
  }

  override def listIterator(index: Int): ListIterator[E] =
    new ListIterator[E] {
      private var i                       = index
      override def add(e: E)              = throw new UnsupportedOperationException()
      override def hasPrevious(): Boolean = i > 0
      override def previous(): E =
        if (!hasPrevious) throw new NoSuchElementException()
        else {
          val element = get(i - 1)
          i -= 1
          element
        }
      override def previousIndex(): Int = i - 1
      override def hasNext(): Boolean   = i < size()
      override def nextIndex(): Int     = i
      override def next(): E =
        if (!hasNext) throw new NoSuchElementException()
        else {
          val element = get(i)
          i += 1
          element
        }
      override def set(e: E): Unit = throw new UnsupportedOperationException()
      override def remove(): Unit  = throw new UnsupportedOperationException()
    }

  override def listIterator(): ListIterator[E] =
    listIterator(0)

  override def iterator(): Iterator[E] =
    listIterator(0)

  override def subList(fromIndex: Int, toIndex: Int): List[E] =
    new ArrayList(underlying.slice(fromIndex, toIndex + 1))

  def forEach(action: Consumer[_ >: E]): Unit =
    underlying.foreach(action.accept)

  // TODO:
  // def spliterator(): Spliterator[E] =
  //   ???

  def removeIf(filter: Predicate[_ >: E]): Boolean = {
    var removed = false
    underlying.foreach { e =>
      if (filter.test(e)) {
        remove(e)
        removed = true
      }
    }
    removed
  }

  def replaceAll(operator: UnaryOperator[E]): Unit =
    underlying = underlying.map(operator.apply)

  def sort(c: Comparator[_ >: E]): Unit =
    underlying = underlying.sortWith((e1, e2) => c.compare(e1, e2) < 0)

}

private object ArrayList {
  def toArrayBuffer[E](c: Collection[_ <: E]): ArrayBuffer[E] = {
    val buffer = new ArrayBuffer[E](c.size)
    val it     = c.iterator()
    while (it.hasNext()) {
      buffer += it.next()
    }
    buffer
  }
}

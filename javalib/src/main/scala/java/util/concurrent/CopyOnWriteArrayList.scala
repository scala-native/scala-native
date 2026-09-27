/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/*
 * Mostly a mechanical migration, on purpose. Structure and control flow
 * follow the original JSR-166 source as closely as possible.
 */

package java.util.concurrent

import java.io.{ObjectInputStream, ObjectOutputStream, Serializable}
import java.lang.invoke.VarHandle
import java.util.function.{Consumer, Predicate, UnaryOperator}
import java.util.{
  ArrayList, Arrays, Collection, Comparator, ConcurrentModificationException,
  Iterator, List, ListIterator, NoSuchElementException, Objects, RandomAccess,
  Spliterator, Spliterators
}

// scalafmt: { maxColumn = 120}

object CopyOnWriteArrayList {

  private def indexOfRange(o: Any, es: Array[Object], from: Int, to: Int): Int = {
    if (o == null) {
      var i = from
      while (i < to) {
        if (es(i) == null)
          return i
        i += 1
      }
    } else {
      var i = from
      while (i < to) {
        if (o.equals(es(i)))
          return i
        i += 1
      }
    }
    -1
  }

  private def lastIndexOfRange(o: Any, es: Array[Object], from: Int, to: Int): Int = {
    if (o == null) {
      var i = to - 1
      while (i >= from) {
        if (es(i) == null)
          return i
        i -= 1
      }
    } else {
      var i = to - 1
      while (i >= from) {
        if (o.equals(es(i)))
          return i
        i -= 1
      }
    }
    -1
  }

  private[concurrent] def elementAt[E](a: Array[Object], index: Int): E = {
    a(index).asInstanceOf[E]
  }

  private[concurrent] def outOfBounds(index: Int, size: Int): String = {
    "Index: " + index + ", Size: " + size
  }

  private def nBits(n: Int): Array[Long] = {
    new Array[Long](((n - 1) >> 6) + 1)
  }
  private def setBit(bits: Array[Long], i: Int): Unit = {
    bits(i >> 6) |= 1L << i
  }
  private def isClear(bits: Array[Long], i: Int): Boolean = {
    (bits(i >> 6) & (1L << i)) == 0
  }

  private def hashCodeOfRange(es: Array[Object], from: Int, to: Int): Int = {
    var hashCode = 1
    var i = from
    while (i < to) {
      val x = es(i)
      hashCode = 31 * hashCode + (if (x == null) 0 else x.hashCode())
      i += 1
    }
    hashCode
  }

  private[concurrent] final class COWIterator[E](
      private val snapshot: Array[Object],
      private var cursor: Int
  ) extends ListIterator[E] {

    def hasNext(): Boolean = {
      cursor < snapshot.length
    }

    def hasPrevious(): Boolean = {
      cursor > 0
    }

    def next(): E = {
      if (!hasNext())
        throw new NoSuchElementException()
      val i = cursor
      cursor = i + 1
      snapshot(i).asInstanceOf[E]
    }

    def previous(): E = {
      if (!hasPrevious())
        throw new NoSuchElementException()
      cursor -= 1
      snapshot(cursor).asInstanceOf[E]
    }

    def nextIndex(): Int = {
      cursor
    }

    def previousIndex(): Int = {
      cursor - 1
    }

    override def remove(): Unit = {
      throw new UnsupportedOperationException()
    }

    def set(e: E): Unit = {
      throw new UnsupportedOperationException()
    }

    def add(e: E): Unit = {
      throw new UnsupportedOperationException()
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      val size = snapshot.length
      var i = cursor
      cursor = size
      while (i < size) {
        action.accept(elementAt(snapshot, i))
        i += 1
      }
    }
  }

  private class COWSubListIterator[E](
      l: List[E],
      index: Int,
      private val offset: Int,
      private val size: Int
  ) extends ListIterator[E] {
    private val it: ListIterator[E] = l.listIterator(index + offset)

    def hasNext(): Boolean = {
      nextIndex() < size
    }

    def next(): E = {
      if (hasNext())
        it.next()
      else
        throw new NoSuchElementException()
    }

    def hasPrevious(): Boolean = {
      previousIndex() >= 0
    }

    def previous(): E = {
      if (hasPrevious())
        it.previous()
      else
        throw new NoSuchElementException()
    }

    def nextIndex(): Int = {
      it.nextIndex() - offset
    }

    def previousIndex(): Int = {
      it.previousIndex() - offset
    }

    override def remove(): Unit = {
      throw new UnsupportedOperationException()
    }

    def set(e: E): Unit = {
      throw new UnsupportedOperationException()
    }

    def add(e: E): Unit = {
      throw new UnsupportedOperationException()
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      while (hasNext()) {
        action.accept(it.next())
      }
    }
  }
}

@SerialVersionUID(8673264195747942595L)
class CopyOnWriteArrayList[E <: AnyRef]() extends List[E] with RandomAccess with Cloneable with Serializable {
  import CopyOnWriteArrayList._

  // Java: final transient Object lock. SN uses var so resetLock can rebind without
  // classFieldRawPtr (rejected for immutable fields).
  @transient private[concurrent] var lock: AnyRef = new AnyRef()

  @volatile @transient private var array: Array[Object] = new Array[Object](0)

  private[concurrent] final def getArray(): Array[Object] = {
    array
  }

  private[concurrent] final def setArray(a: Array[Object]): Unit = {
    array = a
  }

  // Scala auxiliary constructors must call this() first, then setArray.
  def this(c: Collection[_ <: E]) = {
    this()
    var es: Array[Object] = null
    if (c.getClass() == classOf[CopyOnWriteArrayList[_]])
      es = c.asInstanceOf[CopyOnWriteArrayList[_]].getArray()
    else {
      es = c.toArray()
      if (c.getClass() != classOf[ArrayList[_]])
        es = Arrays.copyOf(es, es.length, classOf[Array[Object]])
    }
    setArray(es)
  }

  def this(toCopyIn: Array[E]) = {
    this()
    setArray(
      Arrays.copyOf(
        toCopyIn.asInstanceOf[Array[Object]],
        toCopyIn.length,
        classOf[Array[Object]]
      )
    )
  }

  def size(): Int = {
    getArray().length
  }

  override def isEmpty(): Boolean = {
    size() == 0
  }

  override def contains(o: Any): Boolean = {
    indexOf(o) >= 0
  }

  def indexOf(o: Any): Int = {
    val es = getArray()
    indexOfRange(o, es, 0, es.length)
  }

  def indexOf(e: E, index: Int): Int = {
    val es = getArray()
    indexOfRange(e, es, index, es.length)
  }

  def lastIndexOf(o: Any): Int = {
    val es = getArray()
    lastIndexOfRange(o, es, 0, es.length)
  }

  def lastIndexOf(e: E, index: Int): Int = {
    val es = getArray()
    lastIndexOfRange(e, es, 0, index + 1)
  }

  override def clone(): AnyRef = {
    try {
      val clone = super.clone().asInstanceOf[CopyOnWriteArrayList[E]]
      clone.resetLock()
      // Unlike in readObject, here we cannot visibility-piggyback on the
      // volatile write in setArray().
      VarHandle.releaseFence()
      clone
    } catch {
      case e: CloneNotSupportedException =>
        // this shouldn't happen, since we are Cloneable
        throw new InternalError()
    }
  }

  def toArray(): Array[Object] = {
    getArray().clone()
  }

  def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    val es = getArray()
    val len = es.length
    if (a.length < len)
      Arrays.copyOf(es, len, a.getClass().asInstanceOf[Class[_ <: Array[T]]])
    else {
      System.arraycopy(es, 0, a, 0, len)
      if (a.length > len)
        a(len) = null.asInstanceOf[T]
      a
    }
  }

  // Positional Access Operations

  def get(index: Int): E = {
    elementAt(getArray(), index)
  }

  def set(index: Int, element: E): E = {
    lock.synchronized {
      val es0 = getArray()
      val oldValue: E = elementAt(es0, index)
      var es = es0

      if (oldValue.asInstanceOf[AnyRef] ne element.asInstanceOf[AnyRef]) {
        es = es.clone()
        es(index) = element.asInstanceOf[Object]
      }
      // Ensure volatile write semantics even when oldvalue == element
      setArray(es)
      oldValue
    }
  }

  override def add(e: E): Boolean = {
    lock.synchronized {
      var es = getArray()
      val len = es.length
      es = Arrays.copyOf(es, len + 1)
      es(len) = e.asInstanceOf[Object]
      setArray(es)
      true
    }
  }

  def add(index: Int, element: E): Unit = {
    lock.synchronized {
      val es = getArray()
      val len = es.length
      if (index > len || index < 0)
        throw new IndexOutOfBoundsException(outOfBounds(index, len))
      var newElements: Array[Object] = null
      val numMoved = len - index
      if (numMoved == 0)
        newElements = Arrays.copyOf(es, len + 1)
      else {
        newElements = new Array[Object](len + 1)
        System.arraycopy(es, 0, newElements, 0, index)
        System.arraycopy(es, index, newElements, index + 1, numMoved)
      }
      newElements(index) = element.asInstanceOf[Object]
      setArray(newElements)
    }
  }

  def remove(index: Int): E = {
    lock.synchronized {
      val es = getArray()
      val len = es.length
      val oldValue: E = elementAt(es, index)
      val numMoved = len - index - 1
      var newElements: Array[Object] = null
      if (numMoved == 0)
        newElements = Arrays.copyOf(es, len - 1)
      else {
        newElements = new Array[Object](len - 1)
        System.arraycopy(es, 0, newElements, 0, index)
        System.arraycopy(es, index + 1, newElements, index, numMoved)
      }
      setArray(newElements)
      oldValue
    }
  }

  override def remove(o: Any): Boolean = {
    val snapshot = getArray()
    val index = indexOfRange(o, snapshot, 0, snapshot.length)
    index >= 0 && remove(o, snapshot, index)
  }

  private def remove(o: Any, snapshot: Array[Object], index0: Int): Boolean = {
    lock.synchronized {
      var index = index0
      val current = getArray()
      val len = current.length
      var ok = true
      if (snapshot ne current) {
        var breakFindIndex = false
        val prefix = Math.min(index, len)
        var i = 0
        while (i < prefix && !breakFindIndex) {
          if ((current(i) ne snapshot(i)) && Objects.equals(o, current(i))) {
            index = i
            breakFindIndex = true
          }
          i += 1
        }
        if (!breakFindIndex) {
          if (index >= len)
            ok = false
          else if (current(index) eq o.asInstanceOf[AnyRef])
            () // break findIndex
          else {
            index = indexOfRange(o, current, index, len)
            if (index < 0)
              ok = false
          }
        }
      }
      if (ok) {
        val newElements = new Array[Object](len - 1)
        System.arraycopy(current, 0, newElements, 0, index)
        System.arraycopy(current, index + 1, newElements, index, len - index - 1)
        setArray(newElements)
        true
      } else {
        false
      }
    }
  }

  private[concurrent] def removeRange(fromIndex: Int, toIndex: Int): Unit = {
    lock.synchronized {
      val es = getArray()
      val len = es.length

      if (fromIndex < 0 || toIndex > len || toIndex < fromIndex)
        throw new IndexOutOfBoundsException()
      val newlen = len - (toIndex - fromIndex)
      val numMoved = len - toIndex
      if (numMoved == 0)
        setArray(Arrays.copyOf(es, newlen))
      else {
        val newElements = new Array[Object](newlen)
        System.arraycopy(es, 0, newElements, 0, fromIndex)
        System.arraycopy(es, toIndex, newElements, fromIndex, numMoved)
        setArray(newElements)
      }
    }
  }

  def addIfAbsent(e: E): Boolean = {
    val snapshot = getArray()
    indexOfRange(e, snapshot, 0, snapshot.length) < 0 && addIfAbsent(e, snapshot)
  }

  private def addIfAbsent(e: E, snapshot: Array[Object]): Boolean = {
    lock.synchronized {
      val current = getArray()
      val len = current.length
      var ok = true
      if (snapshot ne current) {
        // Optimize for lost race to another addXXX operation
        val common = Math.min(snapshot.length, len)
        var i = 0
        while (i < common && ok) {
          if ((current(i) ne snapshot(i)) && Objects.equals(e, current(i)))
            ok = false
          i += 1
        }
        if (ok && indexOfRange(e, current, common, len) >= 0)
          ok = false
      }
      if (ok) {
        val newElements = Arrays.copyOf(current, len + 1)
        newElements(len) = e.asInstanceOf[Object]
        setArray(newElements)
        true
      } else {
        false
      }
    }
  }

  override def containsAll(c: Collection[_]): Boolean = {
    val es = getArray()
    val len = es.length
    val it = c.iterator()
    while (it.hasNext()) {
      val e = it.next()
      if (indexOfRange(e, es, 0, len) < 0)
        return false
    }
    true
  }

  override def removeAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    bulkRemove(new Predicate[E] {
      def test(e: E): Boolean = c.contains(e)
    })
  }

  override def retainAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    bulkRemove(new Predicate[E] {
      def test(e: E): Boolean = !c.contains(e)
    })
  }

  def addAllAbsent(c: Collection[_ <: E]): Int = {
    var cs = c.toArray()
    if (c.getClass() != classOf[ArrayList[_]]) {
      cs = cs.clone()
    }
    if (cs.length == 0)
      return 0
    lock.synchronized {
      val es = getArray()
      val len = es.length
      var added = 0
      // uniquify and compact elements in cs
      var i = 0
      while (i < cs.length) {
        val e = cs(i)
        if (indexOfRange(e, es, 0, len) < 0 && indexOfRange(e, cs, 0, added) < 0) {
          cs(added) = e
          added += 1
        }
        i += 1
      }
      if (added > 0) {
        val newElements = Arrays.copyOf(es, len + added)
        System.arraycopy(cs, 0, newElements, len, added)
        setArray(newElements)
      }
      added
    }
  }

  override def clear(): Unit = {
    lock.synchronized {
      setArray(new Array[Object](0))
    }
  }

  override def addAll(c: Collection[_ <: E]): Boolean = {
    val cs =
      if (c.getClass() == classOf[CopyOnWriteArrayList[_]])
        c.asInstanceOf[CopyOnWriteArrayList[_]].getArray()
      else c.toArray()
    if (cs.length == 0)
      return false
    lock.synchronized {
      val es = getArray()
      val len = es.length
      var newElements: Array[Object] = null
      if (len == 0 && (c.getClass() == classOf[CopyOnWriteArrayList[_]] ||
          c.getClass() == classOf[ArrayList[_]])) {
        newElements = cs
      } else {
        newElements = Arrays.copyOf(es, len + cs.length)
        System.arraycopy(cs, 0, newElements, len, cs.length)
      }
      setArray(newElements)
      true
    }
  }

  def addAll(index: Int, c: Collection[_ <: E]): Boolean = {
    val cs = c.toArray()
    lock.synchronized {
      val es = getArray()
      val len = es.length
      if (index > len || index < 0)
        throw new IndexOutOfBoundsException(outOfBounds(index, len))
      if (cs.length == 0)
        false
      else {
        val numMoved = len - index
        var newElements: Array[Object] = null
        if (numMoved == 0)
          newElements = Arrays.copyOf(es, len + cs.length)
        else {
          newElements = new Array[Object](len + cs.length)
          System.arraycopy(es, 0, newElements, 0, index)
          System.arraycopy(es, index, newElements, index + cs.length, numMoved)
        }
        System.arraycopy(cs, 0, newElements, index, cs.length)
        setArray(newElements)
        true
      }
    }
  }

  override def forEach(action: Consumer[_ >: E]): Unit = {
    Objects.requireNonNull(action)
    val es = getArray()
    var i = 0
    while (i < es.length) {
      val e: E = es(i).asInstanceOf[E]
      action.accept(e)
      i += 1
    }
  }

  override def removeIf(filter: Predicate[_ >: E]): Boolean = {
    Objects.requireNonNull(filter)
    bulkRemove(filter)
  }

  // A tiny bit set implementation — see companion nBits/setBit/isClear

  private def bulkRemove(filter: Predicate[_ >: E]): Boolean = {
    lock.synchronized {
      bulkRemove(filter, 0, getArray().length)
    }
  }

  private[concurrent] def bulkRemove(filter: Predicate[_ >: E], i0: Int, end: Int): Boolean = {
    // assert Thread.holdsLock(lock);
    val es = getArray()
    // Optimize for initial run of survivors
    var i = i0
    while (i < end && !filter.test(elementAt(es, i))) {
      i += 1
    }
    if (i < end) {
      val beg = i
      val deathRow = nBits(end - beg)
      var deleted = 1
      deathRow(0) = 1L // set bit 0
      i = beg + 1
      while (i < end) {
        if (filter.test(elementAt(es, i))) {
          setBit(deathRow, i - beg)
          deleted += 1
        }
        i += 1
      }
      // Did filter reentrantly modify the list?
      if (es ne getArray())
        throw new ConcurrentModificationException()
      val newElts = Arrays.copyOf(es, es.length - deleted)
      var w = beg
      i = beg
      while (i < end) {
        if (isClear(deathRow, i - beg)) {
          newElts(w) = es(i)
          w += 1
        }
        i += 1
      }
      System.arraycopy(es, i, newElts, w, es.length - i)
      setArray(newElts)
      true
    } else {
      if (es ne getArray())
        throw new ConcurrentModificationException()
      false
    }
  }

  override def replaceAll(operator: UnaryOperator[E]): Unit = {
    lock.synchronized {
      replaceAllRange(operator, 0, getArray().length)
    }
  }

  private[concurrent] def replaceAllRange(operator: UnaryOperator[E], i0: Int, end: Int): Unit = {
    // assert Thread.holdsLock(lock);
    Objects.requireNonNull(operator)
    val es = getArray().clone()
    var i = i0
    while (i < end) {
      es(i) = operator.apply(elementAt(es, i)).asInstanceOf[Object]
      i += 1
    }
    setArray(es)
  }

  override def sort(c: Comparator[_ >: E]): Unit = {
    lock.synchronized {
      sortRange(c, 0, getArray().length)
    }
  }

  private[concurrent] def sortRange(c: Comparator[_ >: E], i: Int, end: Int): Unit = {
    // assert Thread.holdsLock(lock);
    val es = getArray().clone()
    Arrays.sort[AnyRef](
      es.asInstanceOf[Array[AnyRef]],
      i,
      end,
      c.asInstanceOf[Comparator[_ >: AnyRef]]
    )
    setArray(es)
  }

  private def writeObject(s: ObjectOutputStream): Unit = {
    s.defaultWriteObject()

    val es = getArray()
    // Write out array length
    s.writeInt(es.length)

    // Write out all elements in the proper order.
    var i = 0
    while (i < es.length) {
      s.writeObject(es(i))
      i += 1
    }
  }

  private def readObject(s: ObjectInputStream): Unit = {
    s.defaultReadObject()

    // bind to new lock
    resetLock()

    // Read in array length and allocate array
    val len = s.readInt()
    // jsr166.Platform.checkArray omitted
    val es = new Array[Object](len)

    // Read in all elements in the proper order.
    var i = 0
    while (i < len) {
      es(i) = s.readObject()
      i += 1
    }
    setArray(es)
  }

  override def toString(): String = {
    Arrays.toString(getArray().asInstanceOf[Array[AnyRef]])
  }

  override def equals(o: Any): Boolean = {
    if (o.asInstanceOf[AnyRef] eq this)
      return true
    if (!o.isInstanceOf[List[_]])
      return false

    val list = o.asInstanceOf[List[_]]
    val it = list.iterator()
    val es = getArray()
    var i = 0
    while (i < es.length) {
      val element = es(i)
      if (!it.hasNext() || !Objects.equals(element, it.next()))
        return false
      i += 1
    }
    !it.hasNext()
  }

  override def hashCode(): Int = {
    val es = getArray()
    hashCodeOfRange(es, 0, es.length)
  }

  def iterator(): Iterator[E] = {
    new COWIterator[E](getArray(), 0)
  }

  def listIterator(): ListIterator[E] = {
    new COWIterator[E](getArray(), 0)
  }

  def listIterator(index: Int): ListIterator[E] = {
    val es = getArray()
    val len = es.length
    if (index < 0 || index > len)
      throw new IndexOutOfBoundsException(outOfBounds(index, len))

    new COWIterator[E](es, index)
  }

  override def spliterator(): Spliterator[E] = {
    Spliterators.spliterator(getArray(), Spliterator.IMMUTABLE | Spliterator.ORDERED)
  }

  def subList(fromIndex: Int, toIndex: Int): List[E] = {
    lock.synchronized {
      val es = getArray()
      val len = es.length
      val size = toIndex - fromIndex
      if (fromIndex < 0 || toIndex > len || size < 0)
        throw new IndexOutOfBoundsException()
      new COWSubList(es, fromIndex, size)
    }
  }

  /** Sublist for CopyOnWriteArrayList. */
  private class COWSubList(
      es0: Array[Object],
      private val offset: Int,
      // Java field name is size; Scala cannot share that name with size()
      private var sz: Int
  ) extends List[E]
      with RandomAccess {
    private var expectedArray: Array[Object] = es0

    private def checkForComodification(): Unit = {
      // assert Thread.holdsLock(lock);
      if (getArray() ne expectedArray)
        throw new ConcurrentModificationException()
    }

    private def getArrayChecked(): Array[Object] = {
      // assert Thread.holdsLock(lock);
      val a = getArray()
      if (a ne expectedArray)
        throw new ConcurrentModificationException()
      a
    }

    private def rangeCheck(index: Int): Unit = {
      // assert Thread.holdsLock(lock);
      if (index < 0 || index >= sz)
        throw new IndexOutOfBoundsException(outOfBounds(index, sz))
    }

    private def rangeCheckForAdd(index: Int): Unit = {
      // assert Thread.holdsLock(lock);
      if (index < 0 || index > sz)
        throw new IndexOutOfBoundsException(outOfBounds(index, sz))
    }

    def toArray(): Array[Object] = {
      var es: Array[Object] = null
      var offset0 = 0
      var size0 = 0
      lock.synchronized {
        es = getArrayChecked()
        offset0 = this.offset
        size0 = this.sz
      }
      Arrays.copyOfRange(es, offset0, offset0 + size0)
    }

    def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
      var es: Array[Object] = null
      var offset0 = 0
      var size0 = 0
      lock.synchronized {
        es = getArrayChecked()
        offset0 = this.offset
        size0 = this.sz
      }
      if (a.length < size0)
        Arrays.copyOfRange(es, offset0, offset0 + size0, a.getClass().asInstanceOf[Class[_ <: Array[T]]])
      else {
        System.arraycopy(es, offset0, a, 0, size0)
        if (a.length > size0)
          a(size0) = null.asInstanceOf[T]
        a
      }
    }

    def indexOf(o: Any): Int = {
      var es: Array[Object] = null
      var offset0 = 0
      var size0 = 0
      lock.synchronized {
        es = getArrayChecked()
        offset0 = this.offset
        size0 = this.sz
      }
      val i = indexOfRange(o, es, offset0, offset0 + size0)
      if (i == -1) -1 else i - offset0
    }

    def lastIndexOf(o: Any): Int = {
      var es: Array[Object] = null
      var offset0 = 0
      var size0 = 0
      lock.synchronized {
        es = getArrayChecked()
        offset0 = this.offset
        size0 = this.sz
      }
      val i = lastIndexOfRange(o, es, offset0, offset0 + size0)
      if (i == -1) -1 else i - offset0
    }

    override def contains(o: Any): Boolean = {
      indexOf(o) >= 0
    }

    override def containsAll(c: Collection[_]): Boolean = {
      var es: Array[Object] = null
      var offset0 = 0
      var size0 = 0
      lock.synchronized {
        es = getArrayChecked()
        offset0 = this.offset
        size0 = this.sz
      }
      val it = c.iterator()
      while (it.hasNext()) {
        val o = it.next()
        if (indexOfRange(o, es, offset0, offset0 + size0) < 0)
          return false
      }
      true
    }

    override def isEmpty(): Boolean = {
      size() == 0
    }

    override def toString(): String = {
      Arrays.toString(toArray().asInstanceOf[Array[AnyRef]])
    }

    override def hashCode(): Int = {
      var es: Array[Object] = null
      var offset0 = 0
      var size0 = 0
      lock.synchronized {
        es = getArrayChecked()
        offset0 = this.offset
        size0 = this.sz
      }
      hashCodeOfRange(es, offset0, offset0 + size0)
    }

    override def equals(o: Any): Boolean = {
      if (o.asInstanceOf[AnyRef] eq this)
        return true
      if (!o.isInstanceOf[List[_]])
        return false
      val it = o.asInstanceOf[List[_]].iterator()

      var es: Array[Object] = null
      var offset0 = 0
      var size0 = 0
      lock.synchronized {
        es = getArrayChecked()
        offset0 = this.offset
        size0 = this.sz
      }

      var i = offset0
      val end = offset0 + size0
      while (i < end) {
        if (!it.hasNext() || !Objects.equals(es(i), it.next()))
          return false
        i += 1
      }
      !it.hasNext()
    }

    def set(index: Int, element: E): E = {
      lock.synchronized {
        rangeCheck(index)
        checkForComodification()
        val x = CopyOnWriteArrayList.this.set(offset + index, element)
        expectedArray = getArray()
        x
      }
    }

    def get(index: Int): E = {
      lock.synchronized {
        rangeCheck(index)
        checkForComodification()
        CopyOnWriteArrayList.this.get(offset + index)
      }
    }

    def size(): Int = {
      lock.synchronized {
        checkForComodification()
        sz
      }
    }

    override def add(element: E): Boolean = {
      lock.synchronized {
        checkForComodification()
        CopyOnWriteArrayList.this.add(offset + sz, element)
        expectedArray = getArray()
        sz += 1
      }
      true
    }

    def add(index: Int, element: E): Unit = {
      lock.synchronized {
        checkForComodification()
        rangeCheckForAdd(index)
        CopyOnWriteArrayList.this.add(offset + index, element)
        expectedArray = getArray()
        sz += 1
      }
    }

    override def addAll(c: Collection[_ <: E]): Boolean = {
      lock.synchronized {
        val oldArray = getArrayChecked()
        val modified = CopyOnWriteArrayList.this.addAll(offset + sz, c)
        expectedArray = getArray()
        sz += expectedArray.length - oldArray.length
        modified
      }
    }

    def addAll(index: Int, c: Collection[_ <: E]): Boolean = {
      lock.synchronized {
        rangeCheckForAdd(index)
        val oldArray = getArrayChecked()
        val modified = CopyOnWriteArrayList.this.addAll(offset + index, c)
        expectedArray = getArray()
        sz += expectedArray.length - oldArray.length
        modified
      }
    }

    override def clear(): Unit = {
      lock.synchronized {
        checkForComodification()
        removeRange(offset, offset + sz)
        expectedArray = getArray()
        sz = 0
      }
    }

    def remove(index: Int): E = {
      lock.synchronized {
        rangeCheck(index)
        checkForComodification()
        val result = CopyOnWriteArrayList.this.remove(offset + index)
        expectedArray = getArray()
        sz -= 1
        result
      }
    }

    override def remove(o: Any): Boolean = {
      lock.synchronized {
        checkForComodification()
        val index = indexOf(o)
        if (index == -1)
          false
        else {
          remove(index)
          true
        }
      }
    }

    def iterator(): Iterator[E] = {
      listIterator(0)
    }

    def listIterator(): ListIterator[E] = {
      listIterator(0)
    }

    def listIterator(index: Int): ListIterator[E] = {
      lock.synchronized {
        checkForComodification()
        rangeCheckForAdd(index)
        new COWSubListIterator[E](CopyOnWriteArrayList.this, index, offset, sz)
      }
    }

    def subList(fromIndex: Int, toIndex: Int): List[E] = {
      lock.synchronized {
        checkForComodification()
        if (fromIndex < 0 || toIndex > sz || fromIndex > toIndex)
          throw new IndexOutOfBoundsException()
        new COWSubList(expectedArray, fromIndex + offset, toIndex - fromIndex)
      }
    }

    override def forEach(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      var i = 0
      var end = 0
      var es: Array[Object] = null
      lock.synchronized {
        es = getArrayChecked()
        i = offset
        end = i + sz
      }
      while (i < end) {
        action.accept(elementAt(es, i))
        i += 1
      }
    }

    override def replaceAll(operator: UnaryOperator[E]): Unit = {
      lock.synchronized {
        checkForComodification()
        replaceAllRange(operator, offset, offset + sz)
        expectedArray = getArray()
      }
    }

    override def sort(c: Comparator[_ >: E]): Unit = {
      lock.synchronized {
        checkForComodification()
        sortRange(c, offset, offset + sz)
        expectedArray = getArray()
      }
    }

    override def removeAll(c: Collection[_]): Boolean = {
      Objects.requireNonNull(c)
      bulkRemove(new Predicate[E] {
        def test(e: E): Boolean = c.contains(e)
      })
    }

    override def retainAll(c: Collection[_]): Boolean = {
      Objects.requireNonNull(c)
      bulkRemove(new Predicate[E] {
        def test(e: E): Boolean = !c.contains(e)
      })
    }

    override def removeIf(filter: Predicate[_ >: E]): Boolean = {
      Objects.requireNonNull(filter)
      bulkRemove(filter)
    }

    private def bulkRemove(filter: Predicate[_ >: E]): Boolean = {
      lock.synchronized {
        val oldArray = getArrayChecked()
        val modified = CopyOnWriteArrayList.this.bulkRemove(filter, offset, offset + sz)
        expectedArray = getArray()
        sz += expectedArray.length - oldArray.length
        modified
      }
    }

    override def spliterator(): Spliterator[E] = {
      lock.synchronized {
        Spliterators.spliterator(
          getArrayChecked(),
          offset,
          offset + sz,
          Spliterator.IMMUTABLE | Spliterator.ORDERED
        )
      }
    }

  }

  /** Initializes the lock; for use when deserializing or cloning. */
  private def resetLock(): Unit = {
    // Java uses AccessController + Field.set on a final; SN rebinds the var.
    lock = new AnyRef()
  }
}

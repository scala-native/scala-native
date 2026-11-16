package java.util

trait SequencedMap[K /* <: AnyRef */, V /* <: AnyRef */ ] extends Map[K, V] {

  def reversed(): SequencedMap[K, V]

  def firstEntry(): Map.Entry[K, V] = {
    val it = entrySet().iterator()
    if (it.hasNext()) SequencedMap.CopyOfEntry(it.next()) else null
  }

  def lastEntry(): Map.Entry[K, V] = {
    val it = reversed().entrySet().iterator()
    if (it.hasNext()) SequencedMap.CopyOfEntry(it.next()) else null
  }

  def pollFirstEntry(): Map.Entry[K, V] = {
    val it = entrySet().iterator()
    if (it.hasNext()) {
      val entry = SequencedMap.CopyOfEntry(it.next())
      it.remove()
      entry
    } else null
  }

  def pollLastEntry(): Map.Entry[K, V] = {
    val it = this.reversed().entrySet().iterator()
    if (it.hasNext()) {
      val entry = SequencedMap.CopyOfEntry(it.next())
      it.remove()
      entry
    } else null
  }

  def putFirst(key: K, value: V): V = throw new UnsupportedOperationException()

  def putLast(key: K, value: V): V = throw new UnsupportedOperationException()

  def sequencedEntrySet(): SequencedSet[Map.Entry[K, V]] =
    new SequencedEntrySet[K, V](this)

  def sequencedKeySet(): SequencedSet[K] =
    new SequencedKeySet[K, V](this)

  def sequencedValues(): SequencedCollection[V] =
    new SequencedValues(this)

  private def createOrderedSpliterator[E](
      col: Collection[E]
  ): Spliterator[E] = {
    // precondition: collection is a actually a SequencedCollection
    val startingCharacteristics = col.spliterator().characteristics()
    val orderedCharacteristics = startingCharacteristics | Spliterator.ORDERED

    Spliterators.spliterator[E](col, orderedCharacteristics)
  }

  /* To aid JDK compatibility, the implementations of sequencedEntrySet(),
   * sequencedKeySet(), and sequencedValues() closely follow their
   * respective JDK 27 API "Implementation Requirements" descriptions.
   */

  private class SequencedEntrySet[K, V](originMap: SequencedMap[K, V])
      extends AbstractSet[Map.Entry[K, V]]
      with SequencedSet[Map.Entry[K, V]] {

    type E = Map.Entry[K, V]

    // entrySet() has defined encounter order, use it to prevent recursion.
    val es = originMap.entrySet()

    override def add(e: E): Boolean =
      throw new UnsupportedOperationException

    override def addAll(coll: java.util.Collection[_ <: E]): Boolean =
      throw new UnsupportedOperationException

    override def addFirst(e: E): Unit =
      throw new UnsupportedOperationException

    override def addLast(e: E): Unit =
      throw new UnsupportedOperationException

    override def getFirst(): E = {
      val fe = originMap.firstEntry()

      if (fe == null)
        throw new NoSuchElementException()
      else
        fe
    }

    override def getLast(): E = {
      val le = originMap.lastEntry()

      if (le == null)
        throw new NoSuchElementException()
      else
        le
    }

    override def removeFirst(): E = {
      val fe = originMap.pollFirstEntry()

      if (fe == null)
        throw new NoSuchElementException()
      else
        fe
    }

    override def removeLast(): E = {
      val le = originMap.pollLastEntry()

      if (le == null)
        throw new NoSuchElementException()
      else
        le
    }

    override def reversed(): SequencedSet[E] =
      originMap.reversed().sequencedEntrySet()

    override def spliterator(): Spliterator[E] = {
      /* Ugly cast is necessary for Scala 2.12 to compile.
       * Scala 2.13 not tried. Scala 3 compiles without cast.
       */
      createOrderedSpliterator(es.asInstanceOf[Collection[E]])
    }

    /// pass-through Set methods

    override def clear(): Unit = es.clear()

    override def contains(obj: Any): Boolean = es.contains(obj)

    override def containsAll(c: java.util.Collection[_]): Boolean =
      es.containsAll(c)

    override def isEmpty(): Boolean = es.isEmpty()

    override def iterator(): java.util.Iterator[E] = es.iterator()

    override def remove(obj: Any): Boolean = es.remove(obj)

    override def removeAll(c: java.util.Collection[_]): Boolean =
      es.removeAll(c)

    override def retainAll(c: java.util.Collection[_]): Boolean =
      es.retainAll(c)

    override def size(): Int = es.size()

    override def toArray[T <: AnyRef](a: Array[T]): Array[T] =
      es.toArray(a)

    override def toArray(): Array[AnyRef] = es.toArray()
  }

  private class SequencedKeySet[K, V](originMap: SequencedMap[K, V])
      extends AbstractSet[K]
      with SequencedSet[K] {

    // keySet() has defined encounter order, use it to prevent recursion.
    val ks = originMap.keySet()

    override def add(e: K): Boolean =
      throw new UnsupportedOperationException

    override def addAll(coll: java.util.Collection[_ <: K]): Boolean =
      throw new UnsupportedOperationException

    override def addFirst(e: K): Unit =
      throw new UnsupportedOperationException

    override def addLast(e: K): Unit =
      throw new UnsupportedOperationException

    override def getFirst(): K = {
      val fe = originMap.firstEntry()

      if (fe == null)
        throw new NoSuchElementException()
      else
        fe.getKey()
    }

    override def getLast(): K = {
      val le = originMap.lastEntry()

      if (le == null)
        throw new NoSuchElementException()
      else
        le.getKey()
    }

    override def removeFirst(): K = {
      val fe = originMap.pollFirstEntry()

      if (fe == null)
        throw new NoSuchElementException()
      else
        fe.getKey()
    }

    override def removeLast(): K = {
      val le = originMap.pollLastEntry()

      if (le == null)
        throw new NoSuchElementException()
      else
        le.getKey()
    }

    override def reversed(): SequencedSet[K] =
      originMap.reversed().sequencedKeySet()

    override def spliterator(): Spliterator[K] =
      createOrderedSpliterator[K](ks)

    /// pass-through Set methods

    override def clear(): Unit = ks.clear()

    override def contains(obj: Any): Boolean = ks.contains(obj)

    override def containsAll(c: java.util.Collection[_]): Boolean =
      ks.containsAll(c)

    override def isEmpty(): Boolean = ks.isEmpty()

    override def iterator(): java.util.Iterator[K] = ks.iterator()

    override def remove(obj: Any): Boolean = ks.remove(obj)

    override def removeAll(c: java.util.Collection[_]): Boolean =
      ks.removeAll(c)

    override def retainAll(c: java.util.Collection[_]): Boolean =
      ks.retainAll(c)

    override def size(): Int = ks.size()

    override def toArray[T <: AnyRef](a: Array[T]): Array[T] =
      ks.toArray(a)

    override def toArray(): Array[AnyRef] = ks.toArray()
  }

  private class SequencedValues[K, V](originMap: SequencedMap[K, V])
      extends AbstractCollection[V]
      with SequencedCollection[V] {

    // values() has defined encounter order, use it to prevent recursion.
    val values = originMap.values()

    override def add(e: V): Boolean =
      throw new UnsupportedOperationException

    override def addAll(coll: java.util.Collection[_ <: V]): Boolean =
      throw new UnsupportedOperationException

    override def addFirst(e: V): Unit =
      throw new UnsupportedOperationException

    override def addLast(e: V): Unit =
      throw new UnsupportedOperationException

    override def getFirst(): V = {
      val fe = originMap.firstEntry()

      if (fe == null)
        throw new NoSuchElementException()
      else
        fe.getValue()
    }

    override def getLast(): V = {
      val le = originMap.lastEntry()

      if (le == null)
        throw new NoSuchElementException()
      else
        le.getValue()
    }

    override def removeFirst(): V = {
      val fe = originMap.pollFirstEntry()

      if (fe == null)
        throw new NoSuchElementException()
      else
        fe.getValue()
    }

    override def removeLast(): V = {
      val le = originMap.pollLastEntry()

      if (le == null)
        throw new NoSuchElementException()
      else
        le.getValue()
    }

    override def reversed(): SequencedCollection[V] =
      originMap.reversed().sequencedValues()

    override def spliterator(): Spliterator[V] =
      createOrderedSpliterator[V](values)

    /// pass-through Collection methods

    override def clear(): Unit = values.clear()

    override def contains(obj: Any): Boolean = values.contains(obj)

    override def containsAll(c: java.util.Collection[_]): Boolean =
      values.containsAll(c)

    override def isEmpty(): Boolean = values.isEmpty()

    override def iterator(): java.util.Iterator[V] = values.iterator()

    override def remove(obj: Any): Boolean = values.remove(obj)

    override def removeAll(c: java.util.Collection[_]): Boolean =
      values.removeAll(c)

    override def retainAll(c: java.util.Collection[_]): Boolean =
      values.retainAll(c)

    override def size(): Int = values.size()

    override def toArray[T <: AnyRef](a: Array[T]): Array[T] =
      values.toArray(a)

    override def toArray(): Array[AnyRef] = values.toArray()
  }
}

private[util] object SequencedMap {
  private[util] object CopyOfEntry {
    def apply[K /* <: AnyRef */, V /* <: AnyRef */ ](entry: Map.Entry[K, V]) = {
      Objects.requireNonNull(entry)
      new CopyOfEntry(
        key = entry.getKey(),
        value = entry.getValue(),
        entryHashCode = entry.hashCode()
      )
    }
  }

  private[util] class CopyOfEntry[
      K /* <: AnyRef */,
      V /* <: AnyRef */
  ] private (
      key: K,
      value: V,
      entryHashCode: Int
  ) extends Map.Entry[K, V] {
    override def getKey(): K = key
    override def getValue(): V = value
    override def setValue(value: V): V =
      throw new UnsupportedOperationException()

    override def equals(o: Any): Boolean = o match {
      case entry: Map.Entry[K, V] @unchecked =>
        Objects.equals(key, entry.getKey()) &&
          Objects.equals(value, entry.getValue())
      case _ => false
    }

    override def hashCode(): Int = entryHashCode

    override def toString(): String = s"$key=$value"
  }
}

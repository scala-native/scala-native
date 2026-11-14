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

  /* To aid JDK compatibility, the implementations of sequencedKeySet(),
   * sequencedValues(), and sequencedEntrySet() closely follow their
   * respective JDK 27 API "Implementation Requirements" descriptions.
   */

  def sequencedKeySet(): SequencedSet[K] = {
    val originMap = this

    new AbstractSet[K] with SequencedSet[K] {
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

      // pass-through Set methods
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
  }

  def sequencedValues(): SequencedCollection[V] = {
    val originMap = this

    new SequencedCollection[V] {
      val vc = originMap.values()

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
        val le = originMap.lastEntry()

        if (le == null)
          throw new NoSuchElementException()
        else
          le.getValue()
      }

      override def reversed(): SequencedCollection[V] =
        originMap.reversed().sequencedValues()

      // pass-through Collection methods
      override def clear(): Unit = vc.clear()

      override def contains(obj: Any): Boolean = vc.contains(obj)

      override def containsAll(c: java.util.Collection[_]): Boolean =
        vc.containsAll(c)

      override def isEmpty(): Boolean = vc.isEmpty()

      override def iterator(): java.util.Iterator[V] = vc.iterator()

      override def remove(obj: Any): Boolean = vc.remove(obj)

      override def removeAll(c: java.util.Collection[_]): Boolean =
        vc.removeAll(c)

      override def retainAll(c: java.util.Collection[_]): Boolean =
        vc.retainAll(c)

      override def size(): Int = vc.size()

      override def toArray[T <: AnyRef](a: Array[T]): Array[T] =
        vc.toArray(a)

      override def toArray(): Array[AnyRef] = vc.toArray()
    }
  }

  def sequencedEntrySet(): SequencedSet[Map.Entry[K, V]] = {
    type E = Map.Entry[K, V]
    val originMap = this

    new AbstractSet[E] with SequencedSet[E] {
      val es = originMap.entrySet()

      override def add(e: E): Boolean =
        throw new UnsupportedOperationException

      override def addAll(coll: java.util.Collection[_ <: E]): Boolean =
        throw new UnsupportedOperationException

      override def addFirst(e: E): Unit =
        throw new UnsupportedOperationException

      override def addLast(e: E): Unit =
        throw new UnsupportedOperationException

      // pass-through Set methods
      override def getFirst(): E = originMap.firstEntry()

      override def getLast(): E = originMap.lastEntry()

      override def removeFirst(): E = originMap.pollFirstEntry()

      override def removeLast(): E = originMap.pollLastEntry()

      override def reversed(): SequencedSet[E] =
        originMap.reversed().sequencedEntrySet()

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
  }
}

private[util] object SequencedMap {
  private[util] object CopyOfEntry {
    def apply[K /* <: AnyRef */, V /* <: AnyRef */ ](entry: Map.Entry[K, V]) = {
      Objects.requireNonNull(entry)
      new CopyOfEntry(
        key = entry.getKey(),
        value = entry.getValue()
      )
    }
  }
  private[util] class CopyOfEntry[
      K /* <: AnyRef */,
      V /* <: AnyRef */
  ] private (
      key: K,
      value: V
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
    override def hashCode(): Int = {
      def hash(obj: Any) = if (obj == null) 0 else obj.##
      hash(key) ^ hash(value)
    }
    override def toString(): String = s"$key=$value"
  }
}

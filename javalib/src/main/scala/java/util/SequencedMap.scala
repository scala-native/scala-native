package java.util

trait SequencedMap[K /* <: AnyRef */, V /* <: AnyRef */ ] extends Map[K, V] {
  /* Commented out until we're able to provide reversed views for collections
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

  def sequencedKeySet(): SequencedSet[K] = ???
  def sequencedValues(): SequencedCollection[V] = ???
  def sequencedEntrySet(): SequencedSet[Map.Entry[K, V]] = ???
}

private object SequencedMap {
  private object CopyOfEntry {
    def apply[K /* <: AnyRef */, V /* <: AnyRef */](entry: Map.Entry[K, V]) = {
      Objects.requireNonNull(entry)
      new CopyOfEntry(
        key = entry.getKey(),
        value = entry.getValue()
      )
    }
  }
  private class CopyOfEntry[K /* <: AnyRef */, V /* <: AnyRef */] private (key: K, value: V)
      extends Map.Entry[K, V] {
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
   */
}

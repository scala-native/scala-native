package java.util

import java.util as ju

import scala.collection.mutable
import ScalaOps.*

class Hashtable[K, V] private (inner: mutable.HashMap[Box[Any], V])
    extends ju.Dictionary[K, V]
    with ju.Map[K, V]
    with Cloneable
    with Serializable {

  def this() =
    this(mutable.HashMap.empty[Box[Any], V])

  def this(initialCapacity: Int) = this()

  def this(initialCapacity: Int, loadFactor: Float) = this()

  def this(t: ju.Map[? <: K, ? <: V]) = {
    this()
    putAll(t)
  }

  def size(): Int =
    inner.size

  def isEmpty(): Boolean =
    inner.isEmpty

  def keys(): ju.Enumeration[K] = Collections.enumeration(keySet())

  def elements(): ju.Enumeration[V] =
    Collections.enumeration(values())

  def contains(value: Any): Boolean =
    containsValue(value)

  def containsValue(value: Any): Boolean =
    inner.valuesIterator.contains(value)

  def containsKey(key: Any): Boolean =
    inner.contains(Box(key))

  @throws[NullPointerException]
  def get(key: Any): V = {
    if (key == null)
      throw new NullPointerException
    inner.getOrElse(Box(key), null.asInstanceOf[V])
  }

  // Not implemented
  // protected def rehash(): Unit

  @throws[NullPointerException]
  def put(key: K, value: V): V = {
    if (key == null || value == null)
      throw new NullPointerException
    inner
      .put(Box(key.asInstanceOf[AnyRef]), value)
      .getOrElse(null.asInstanceOf[V])
  }

  @throws[NullPointerException]
  def remove(key: Any): V = {
    if (key == null)
      throw new NullPointerException
    inner.remove(Box(key)).getOrElse(null.asInstanceOf[V])
  }

  def putAll(m: ju.Map[? <: K, ? <: V]): Unit =
    m.entrySet().scalaOps.foreach { e =>
      inner.put(Box(e.getKey()), e.getValue())
    }

  def clear(): Unit =
    inner.clear()

  override def clone(): AnyRef =
    new ju.Hashtable[K, V](this)

  override def toString(): String =
    inner.iterator
      .map(kv => "" + kv._1.inner + "=" + kv._2)
      .mkString("{", ", ", "}")

  def keySet(): ju.Set[K] = {
    val b = new LinkedHashSet[K]()
    inner.keySet.foreach { key => b.add(key.inner.asInstanceOf[K]) }
    b
  }

  private class UnboxedEntry(
      private[UnboxedEntry] val boxedEntry: ju.Map.Entry[Box[Any], V]
  ) extends ju.Map.Entry[K, V] {
    def getKey(): K = boxedEntry.getKey().inner.asInstanceOf[K]
    def getValue(): V = boxedEntry.getValue()
    def setValue(value: V): V = boxedEntry.setValue(value)
    override def equals(o: Any): Boolean = o match {
      case o: UnboxedEntry => boxedEntry.equals(o.boxedEntry)
      case _               => false
    }
    override def hashCode(): Int = boxedEntry.hashCode()
  }

  def entrySet(): ju.Set[ju.Map.Entry[K, V]] = {
    val entries = new LinkedHashSet[ju.Map.Entry[K, V]]
    inner.foreach {
      case (key, value) =>
        val entry = new UnboxedEntry(
          new ju.AbstractMap.SimpleEntry[Box[Any], V](key, value)
        )
        entries.add(entry)
    }
    entries
  }

  def values(): ju.Collection[V] = {
    val b = new LinkedList[V]()
    inner.values.foreach(b.add)
    b
  }
}

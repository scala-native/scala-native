package java.util

import java.util.function.{BiConsumer, BiFunction, Function}
import java.{util => ju}

class Hashtable[K, V] private (inner: NullRejectingHashMap[K, V])
    extends ju.Dictionary[K, V]
    with ju.Map[K, V]
    with Cloneable
    with Serializable {

  def this(initialCapacity: Int, loadFactor: Float) =
    this(new NullRejectingHashMap[K, V](initialCapacity, loadFactor))

  def this(initialCapacity: Int) =
    this(initialCapacity, HashMap.DEFAULT_LOAD_FACTOR)

  def this() =
    this(11)

  def this(t: ju.Map[_ <: K, _ <: V]) = {
    this(Math.max(2 * t.size(), 11))
    putAll(t)
  }

  def size(): Int = synchronized(inner.size())

  def isEmpty(): Boolean = synchronized(inner.isEmpty())

  // Enumerations are not fail-fast: they iterate over a snapshot.
  def keys(): ju.Enumeration[K] = synchronized {
    Collections.enumeration(new ArrayList[K](inner.keySet()))
  }

  def elements(): ju.Enumeration[V] = synchronized {
    Collections.enumeration(new ArrayList[V](inner.values()))
  }

  def contains(value: Any): Boolean = containsValue(value)

  def containsValue(value: Any): Boolean =
    synchronized(inner.containsValue(value))

  def containsKey(key: Any): Boolean = synchronized(inner.containsKey(key))

  def get(key: Any): V = synchronized(inner.get(key))

  // Not implemented
  // protected def rehash(): Unit

  def put(key: K, value: V): V = synchronized(inner.put(key, value))

  def remove(key: Any): V = synchronized(inner.remove(key))

  def putAll(m: ju.Map[_ <: K, _ <: V]): Unit = synchronized(inner.putAll(m))

  def clear(): Unit = synchronized(inner.clear())

  override def getOrDefault(key: Any, defaultValue: V): V = synchronized {
    val value = inner.get(key)
    if (value == null) defaultValue else value
  }

  override def putIfAbsent(key: K, value: V): V =
    synchronized(inner.putIfAbsent(key, value))

  override def remove(key: Any, value: Any): Boolean =
    synchronized(inner.remove(key, value))

  override def replace(key: K, oldValue: V, newValue: V): Boolean =
    synchronized(inner.replace(key, oldValue, newValue))

  override def replace(key: K, value: V): V =
    synchronized(inner.replace(key, value))

  override def computeIfAbsent(
      key: K,
      mappingFunction: Function[_ >: K, _ <: V]
  ): V = synchronized {
    Objects.requireNonNull(key)
    Objects.requireNonNull(mappingFunction)
    inner.computeIfAbsent(key, mappingFunction)
  }

  override def computeIfPresent(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = synchronized {
    Objects.requireNonNull(key)
    Objects.requireNonNull(remappingFunction)
    inner.computeIfPresent(key, remappingFunction)
  }

  override def compute(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = synchronized {
    Objects.requireNonNull(key)
    Objects.requireNonNull(remappingFunction)
    inner.compute(key, remappingFunction)
  }

  override def merge(
      key: K,
      value: V,
      remappingFunction: BiFunction[_ >: V, _ >: V, _ <: V]
  ): V = synchronized {
    Objects.requireNonNull(key)
    Objects.requireNonNull(remappingFunction)
    inner.merge(key, value, remappingFunction)
  }

  override def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit =
    synchronized(inner.forEach(action))

  override def replaceAll(function: BiFunction[_ >: K, _ >: V, _ <: V]): Unit =
    synchronized(inner.replaceAll(function))

  override def clone(): AnyRef = synchronized(new ju.Hashtable[K, V](this))

  override def equals(o: Any): Boolean = synchronized {
    (o.asInstanceOf[AnyRef] eq this) || inner.equals(o)
  }

  override def hashCode(): Int = synchronized(inner.hashCode())

  override def toString(): String = synchronized(inner.toString())

  def keySet(): ju.Set[K] = inner.keySet()

  def entrySet(): ju.Set[ju.Map.Entry[K, V]] = inner.entrySet()

  def values(): ju.Collection[V] = inner.values()
}

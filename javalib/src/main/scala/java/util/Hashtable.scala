package java.util

import java.util.function.{BiConsumer, BiFunction, Function}
import java.{util => ju}

import scala.collection.mutable

import ScalaOps._

class Hashtable[K, V] private (inner: mutable.HashMap[Box[Any], V])
    extends ju.Dictionary[K, V]
    with ju.Map[K, V]
    with Cloneable
    with Serializable {
  self =>

  def this() =
    this(mutable.HashMap.empty[Box[Any], V])

  def this(initialCapacity: Int) = this()

  def this(initialCapacity: Int, loadFactor: Float) = this()

  def this(t: ju.Map[_ <: K, _ <: V]) = {
    this()
    putAll(t)
  }

  private def requireKey(key: Any): Box[Any] = {
    if (key == null)
      throw new NullPointerException
    Box(key)
  }

  private def requireValue(value: Any): Unit = {
    if (value == null)
      throw new NullPointerException
  }

  private def boxedKeysSnapshot(): scala.collection.immutable.List[Box[Any]] =
    self.synchronized(inner.keysIterator.toList)

  def size(): Int =
    self.synchronized(inner.size)

  def isEmpty(): Boolean =
    self.synchronized(inner.isEmpty)

  def keys(): ju.Enumeration[K] = Collections.enumeration(keySet())

  def elements(): ju.Enumeration[V] =
    Collections.enumeration(values())

  def contains(value: Any): Boolean =
    containsValue(value)

  def containsValue(value: Any): Boolean = self.synchronized {
    requireValue(value)
    inner.valuesIterator.exists(Objects.equals(_, value))
  }

  def containsKey(key: Any): Boolean =
    self.synchronized(inner.contains(requireKey(key)))

  @throws[NullPointerException]
  def get(key: Any): V =
    self.synchronized(inner.getOrElse(requireKey(key), null.asInstanceOf[V]))

  // Not implemented
  // protected def rehash(): Unit

  @throws[NullPointerException]
  def put(key: K, value: V): V = self.synchronized {
    val boxedKey = requireKey(key)
    requireValue(value)
    inner
      .put(boxedKey, value)
      .getOrElse(null.asInstanceOf[V])
  }

  @throws[NullPointerException]
  def remove(key: Any): V =
    self.synchronized(
      inner.remove(requireKey(key)).getOrElse(null.asInstanceOf[V])
    )

  def putAll(m: ju.Map[_ <: K, _ <: V]): Unit = self.synchronized {
    m.entrySet().scalaOps.foreach(e => put(e.getKey(), e.getValue()))
  }

  def clear(): Unit =
    self.synchronized(inner.clear())

  override def clone(): AnyRef =
    new ju.Hashtable[K, V](this)

  override def toString(): String = self.synchronized {
    inner.iterator
      .map(kv => "" + kv._1.inner + "=" + kv._2)
      .mkString("{", ", ", "}")
  }

  def keySet(): ju.Set[K] =
    new ju.AbstractSet[K] {
      override def size(): Int = self.size()

      override def contains(o: Any): Boolean =
        self.containsKey(o)

      override def remove(o: Any): Boolean =
        self.remove(o) != null

      override def clear(): Unit =
        self.clear()

      def iterator(): ju.Iterator[K] =
        new ju.Iterator[K] {
          private val iter = boxedKeysSnapshot().iterator
          private var last: Box[Any] = null

          def hasNext(): Boolean =
            iter.hasNext

          def next(): K = {
            val key = iter.next()
            last = key
            key.inner.asInstanceOf[K]
          }

          override def remove(): Unit = {
            if (last == null)
              throw new IllegalStateException
            self.synchronized(inner.remove(last))
            last = null
          }
        }
    }

  private final class UnboxedEntry(private val boxedKey: Box[Any])
      extends ju.Map.Entry[K, V] {
    def getKey(): K =
      boxedKey.inner.asInstanceOf[K]

    def getValue(): V = self.synchronized {
      inner.getOrElse(boxedKey, throw new IllegalStateException)
    }

    def setValue(value: V): V = self.synchronized {
      requireValue(value)
      val oldValue = inner.getOrElse(boxedKey, throw new IllegalStateException)
      inner.update(boxedKey, value)
      oldValue
    }

    override def equals(o: Any): Boolean = o match {
      case o: ju.Map.Entry[_, _] =>
        Objects.equals(getKey(), o.getKey()) &&
          Objects.equals(getValue(), o.getValue())
      case _ =>
        false
    }

    override def hashCode(): Int =
      Objects.hashCode(getKey()) ^ Objects.hashCode(getValue())

    override def toString(): String =
      "" + getKey() + "=" + getValue()
  }

  def entrySet(): ju.Set[ju.Map.Entry[K, V]] =
    new ju.AbstractSet[ju.Map.Entry[K, V]] {
      override def size(): Int = self.size()

      override def contains(o: Any): Boolean = o match {
        case o: ju.Map.Entry[_, _] =>
          val boxedKey = requireKey(o.getKey())
          self.synchronized {
            inner.get(boxedKey).exists(Objects.equals(_, o.getValue()))
          }
        case _ =>
          false
      }

      override def remove(o: Any): Boolean = o match {
        case o: ju.Map.Entry[_, _] =>
          val boxedKey = requireKey(o.getKey())
          self.synchronized {
            if (inner.get(boxedKey).exists(Objects.equals(_, o.getValue()))) {
              inner.remove(boxedKey)
              true
            } else {
              false
            }
          }
        case _ =>
          false
      }

      override def clear(): Unit =
        self.clear()

      def iterator(): ju.Iterator[ju.Map.Entry[K, V]] =
        new ju.Iterator[ju.Map.Entry[K, V]] {
          private val iter = boxedKeysSnapshot().iterator
          private var last: Box[Any] = null

          def hasNext(): Boolean =
            iter.hasNext

          def next(): ju.Map.Entry[K, V] = {
            last = iter.next()
            new UnboxedEntry(last)
          }

          override def remove(): Unit = {
            if (last == null)
              throw new IllegalStateException
            self.synchronized(inner.remove(last))
            last = null
          }
        }
    }

  def values(): ju.Collection[V] =
    new ju.AbstractCollection[V] {
      override def size(): Int = self.size()

      override def contains(o: Any): Boolean =
        self.containsValue(o)

      override def clear(): Unit =
        self.clear()

      def iterator(): ju.Iterator[V] =
        new ju.Iterator[V] {
          private val iter = boxedKeysSnapshot().iterator
          private var last: Box[Any] = null

          def hasNext(): Boolean =
            iter.hasNext

          def next(): V = {
            last = iter.next()
            self.synchronized(
              inner.getOrElse(last, throw new IllegalStateException)
            )
          }

          override def remove(): Unit = {
            if (last == null)
              throw new IllegalStateException
            self.synchronized(inner.remove(last))
            last = null
          }
        }
    }

  override def getOrDefault(key: Any, defaultValue: V): V =
    self.synchronized {
      inner.getOrElse(requireKey(key), defaultValue)
    }

  override def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit = {
    Objects.requireNonNull(action)
    boxedKeysSnapshot().foreach { boxedKey =>
      self.synchronized {
        inner
          .get(boxedKey)
          .foreach(value =>
            action.accept(boxedKey.inner.asInstanceOf[K], value)
          )
      }
    }
  }

  override def replaceAll(
      function: BiFunction[_ >: K, _ >: V, _ <: V]
  ): Unit = {
    Objects.requireNonNull(function)
    boxedKeysSnapshot().foreach { boxedKey =>
      self.synchronized {
        inner.get(boxedKey).foreach { oldValue =>
          val newValue =
            function.apply(boxedKey.inner.asInstanceOf[K], oldValue)
          requireValue(newValue)
          inner.update(boxedKey, newValue)
        }
      }
    }
  }

  override def putIfAbsent(key: K, value: V): V = self.synchronized {
    val boxedKey = requireKey(key)
    requireValue(value)
    inner.get(boxedKey) match {
      case Some(oldValue) =>
        oldValue
      case None =>
        inner.put(boxedKey, value)
        null.asInstanceOf[V]
    }
  }

  override def remove(key: Any, value: Any): Boolean = self.synchronized {
    val boxedKey = requireKey(key)
    requireValue(value)
    if (inner.get(boxedKey).exists(Objects.equals(_, value))) {
      inner.remove(boxedKey)
      true
    } else {
      false
    }
  }

  override def replace(key: K, oldValue: V, newValue: V): Boolean =
    self.synchronized {
      val boxedKey = requireKey(key)
      requireValue(oldValue)
      requireValue(newValue)
      if (inner.get(boxedKey).exists(Objects.equals(_, oldValue))) {
        inner.update(boxedKey, newValue)
        true
      } else {
        false
      }
    }

  override def replace(key: K, value: V): V = self.synchronized {
    val boxedKey = requireKey(key)
    requireValue(value)
    inner.get(boxedKey) match {
      case Some(oldValue) =>
        inner.update(boxedKey, value)
        oldValue
      case None =>
        null.asInstanceOf[V]
    }
  }

  override def computeIfAbsent(
      key: K,
      mappingFunction: Function[_ >: K, _ <: V]
  ): V = self.synchronized {
    val boxedKey = requireKey(key)
    Objects.requireNonNull(mappingFunction)
    inner.get(boxedKey) match {
      case Some(oldValue) =>
        oldValue
      case None =>
        val newValue = mappingFunction.apply(key)
        if (newValue != null)
          inner.put(boxedKey, newValue)
        newValue
    }
  }

  override def computeIfPresent(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = self.synchronized {
    val boxedKey = requireKey(key)
    Objects.requireNonNull(remappingFunction)
    inner.get(boxedKey) match {
      case Some(oldValue) =>
        val newValue = remappingFunction.apply(key, oldValue)
        if (newValue == null) inner.remove(boxedKey)
        else inner.update(boxedKey, newValue)
        newValue
      case None =>
        null.asInstanceOf[V]
    }
  }

  override def compute(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = self.synchronized {
    val boxedKey = requireKey(key)
    Objects.requireNonNull(remappingFunction)
    val oldValue = inner.getOrElse(boxedKey, null.asInstanceOf[V])
    val newValue = remappingFunction.apply(key, oldValue)
    if (newValue == null) inner.remove(boxedKey)
    else inner.put(boxedKey, newValue)
    newValue
  }

  override def merge(
      key: K,
      value: V,
      remappingFunction: BiFunction[_ >: V, _ >: V, _ <: V]
  ): V = self.synchronized {
    val boxedKey = requireKey(key)
    Objects.requireNonNull(remappingFunction)
    val newValue = inner.get(boxedKey) match {
      case Some(oldValue) =>
        remappingFunction.apply(oldValue, value)
      case None =>
        value
    }
    if (newValue == null) inner.remove(boxedKey)
    else inner.put(boxedKey, newValue)
    newValue
  }
}

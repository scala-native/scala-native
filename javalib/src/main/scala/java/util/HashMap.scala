package java.util

import scala.collection.mutable

class HashMap[K, V] protected (inner: mutable.Map[AnyRef, V])
    extends AbstractMap[K, V]
    with Serializable
    with Cloneable { self =>

  protected def boxKey(key: K): AnyRef =
    key.asInstanceOf[AnyRef]
  protected def unboxKey(box: AnyRef): K =
    box.asInstanceOf[K]

  def this() =
    this(mutable.AnyRefMap.empty[AnyRef, V])

  def this(initialCapacity: Int, loadFactor: Float) = {
    this()
    if (initialCapacity < 0)
      throw new IllegalArgumentException("initialCapacity < 0")
    else if (loadFactor < 0.0)
      throw new IllegalArgumentException("loadFactor <= 0.0")
  }

  def this(initialCapacity: Int) =
    this(initialCapacity, HashMap.DEFAULT_LOAD_FACTOR)

  def this(m: Map[_ <: K, _ <: V]) = {
    this()
    putAll(m)
  }

  override def clear(): Unit =
    inner.clear()

  override def clone(): AnyRef = {
    new HashMap(inner.clone())
  }

  override def containsKey(key: Any): Boolean =
    inner.contains(boxKey(key.asInstanceOf[K]))

  override def containsValue(value: Any): Boolean =
    inner.valuesIterator.contains(value.asInstanceOf[V])

  override def entrySet(): Set[Map.Entry[K, V]] =
    new EntrySet

  override def get(key: Any): V = inner match {
    case _: mutable.AnyRefMap[_, _] =>
      val inner = this.inner.asInstanceOf[mutable.AnyRefMap[AnyRef, V]]
      inner.getOrNull(boxKey(key.asInstanceOf[K]))
    case _ =>
      inner.get(boxKey(key.asInstanceOf[K])).getOrElse(null.asInstanceOf[V])
  }

  override def isEmpty(): Boolean =
    inner.isEmpty

  override def keySet(): Set[K] =
    new KeySet

  override def put(key: K, value: V): V =
    inner.put(boxKey(key), value).getOrElse(null.asInstanceOf[V])

  override def remove(key: Any): V = {
    val boxedKey = boxKey(key.asInstanceOf[K])
    inner.get(boxedKey).fold(null.asInstanceOf[V]) { value =>
      inner -= boxedKey
      value
    }
  }

  override def size(): Int =
    inner.size

  override def values(): Collection[V] =
    new ValuesView

  private class EntrySet
      extends AbstractSet[Map.Entry[K, V]]
      with AbstractMapView[Map.Entry[K, V]] {
    override def iterator(): Iterator[Map.Entry[K, V]] = {
      new AbstractMapViewIterator[Map.Entry[K, V]] {
        override protected def getNextForm(key: AnyRef): Map.Entry[K, V] = {
          new AbstractMap.SimpleEntry(unboxKey(key), inner(key)) {
            override def setValue(value: V): V = {
              inner.update(key, value)
              super.setValue(value)
            }
          }
        }
      }
    }
  }

  private class KeySet extends AbstractSet[K] with AbstractMapView[K] {
    override def remove(o: Any): Boolean = {
      val boxedKey = boxKey(o.asInstanceOf[K])
      val contains = inner.contains(boxedKey)
      if (contains)
        inner -= boxedKey
      contains
    }

    override def iterator(): Iterator[K] = {
      new AbstractMapViewIterator[K] {
        protected def getNextForm(key: AnyRef): K =
          unboxKey(key)
      }
    }
  }

  private class ValuesView extends AbstractMapView[V] {
    override def size(): Int =
      inner.size

    override def iterator(): Iterator[V] = {
      new AbstractMapViewIterator[V] {
        protected def getNextForm(key: AnyRef): V = inner(key)
      }
    }
  }

  private trait AbstractMapView[E] extends AbstractCollection[E] {
    override def size(): Int =
      inner.size

    override def clear(): Unit =
      inner.clear()
  }

  private abstract class AbstractMapViewIterator[E] extends Iterator[E] {
    protected val innerIterator = inner.keySet.iterator

    protected var lastKey: Option[AnyRef] = None

    protected def getNextForm(key: AnyRef): E

    final override def next(): E = {
      lastKey = Some(innerIterator.next())
      getNextForm(lastKey.get)
    }

    final override def hasNext(): Boolean =
      innerIterator.hasNext

    final override def remove(): Unit = {
      lastKey match {
        case Some(key) =>
          inner.remove(key)
          lastKey = None
        case None =>
          throw new IllegalStateException
      }
    }
  }
}

object HashMap {
  private[HashMap] final val DEFAULT_INITIAL_CAPACITY = 16
  private[HashMap] final val DEFAULT_LOAD_FACTOR      = 0.75f
}

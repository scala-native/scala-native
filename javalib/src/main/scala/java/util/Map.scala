// Ported from Scala.js commit f7be410 dated: 2020-10-07

package java.util

import java.util.function.{BiConsumer, BiFunction, Function}
import scala.scalanative.annotation.alwaysinline
import ScalaOps._

trait Map[K, V] {
  def size(): Int
  def isEmpty(): Boolean
  def containsKey(key: Any): Boolean
  def containsValue(value: Any): Boolean
  def get(key: Any): V
  def put(key: K, value: V): V
  def remove(key: Any): V
  def putAll(m: Map[_ <: K, _ <: V]): Unit
  def clear(): Unit
  def keySet(): Set[K]
  def values(): Collection[V]
  def entrySet(): Set[Map.Entry[K, V]]
  def equals(o: Any): Boolean
  def hashCode(): Int

  def getOrDefault(key: Any, defaultValue: V): V =
    if (containsKey(key)) get(key)
    else defaultValue

  def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit = {
    Objects.requireNonNull(action)
    entrySet().forEach(usingEntry(_)(action.accept))
  }

  def replaceAll(function: BiFunction[_ >: K, _ >: V, _ <: V]): Unit = {
    Objects.requireNonNull(function)
    entrySet().forEach(entry =>
      usingEntry(entry) { (k, v) =>
        val newValue = function.apply(k, v)
        try entry.setValue(newValue)
        catch {
          case ex: IllegalStateException =>
            throw new ConcurrentModificationException(ex)
        }
      }
    )
  }

  def putIfAbsent(key: K, value: V): V = {
    val prevValue = get(key)
    if (prevValue == null)
      put(key, value) // will return null
    else
      prevValue
  }

  def remove(key: Any, value: Any): Boolean = {
    if (containsKey(key) && Objects.equals(get(key), value)) {
      remove(key)
      true
    } else {
      false
    }
  }

  def replace(key: K, oldValue: V, newValue: V): Boolean = {
    if (containsKey(key) && Objects.equals(get(key), oldValue)) {
      put(key, newValue)
      true
    } else {
      false
    }
  }

  def replace(key: K, value: V): V =
    if (containsKey(key)) put(key, value)
    else null.asInstanceOf[V]

  def computeIfAbsent(key: K, mappingFunction: Function[_ >: K, _ <: V]): V = {
    val oldValue = get(key)
    if (oldValue != null) {
      oldValue
    } else {
      val newValue = mappingFunction.apply(key)
      if (newValue != null)
        put(key, newValue)
      newValue
    }
  }

  def computeIfPresent(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = {
    val oldValue = get(key)
    if (oldValue == null) {
      oldValue
    } else {
      val newValue = remappingFunction.apply(key, oldValue)
      putOrRemove(key, newValue)
      newValue
    }
  }

  def compute(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = {
    val oldValue = get(key)
    val newValue = remappingFunction.apply(key, oldValue)

    /* The "Implementation Requirements" section of the JavaDoc for this method
     * does not correspond to the textual specification in the case where both
     * a) there was a null mapping, and
     * b) the remapping function returned null.
     *
     * The Implementation Requirements would leave the null mapping, whereas
     * the specification says to remove it.
     *
     * We implement the specification, as it appears that the actual Map
     * implementations on the JVM behave like the spec.
     */
    putOrRemove(key, newValue)

    newValue
  }

  def merge(
      key: K,
      value: V,
      remappingFunction: BiFunction[_ >: V, _ >: V, _ <: V]
  ): V = {
    Objects.requireNonNull(value)

    val oldValue = get(key)
    val newValue =
      if (oldValue == null) value
      else remappingFunction.apply(oldValue, value)
    putOrRemove(key, newValue)
    newValue
  }

  private def putOrRemove(key: K, value: V): Unit = {
    if (value != null)
      put(key, value)
    else
      remove(key)
  }

  /** Helper method used to detect concurrent modification exception when
   *  accessing map entires. IllegalStateException means the entry is no longer
   *  available (remove)
   */
  @alwaysinline
  protected[util] def usingEntry[T](
      entry: Map.Entry[K, V]
  )(apply: (K, V) => T) = {
    var key: K = null.asInstanceOf[K]
    var value: V = null.asInstanceOf[V]

    try {
      key = entry.getKey()
      value = entry.getValue()
    } catch {
      case ex: IllegalStateException =>
        throw new ConcurrentModificationException(ex)
    }
    apply(key, value)
  }
}

object Map {
  trait Entry[K, V] {
    def getKey(): K
    def getValue(): V
    def setValue(value: V): V
    def equals(o: Any): Boolean
    def hashCode(): Int
  }
}

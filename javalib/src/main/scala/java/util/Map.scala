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

  // Since: Java 10
  def copyOf[K, V](map: Map[_ <: K, _ <: V]): Map[K, V] = {
    Objects.requireNonNull(map)

    val mapSize = map.size()

    val underlying = HashMap.newHashMap[K, V](mapSize)

    map.forEach((k, v) => {
      Objects.requireNonNull(k)
      underlying.put(k, v)
    })

    Collections.unmodifiableMap(underlying)
  }

  // Since: Java 9
  def entry[K, V](k: K, v: V): Map.Entry[K, V] = {
    Objects.requireNonNull(k)
    Objects.requireNonNull(v)

    new Map.Entry[K, V]() {
      def getKey(): K = k

      def getValue(): V = v

      def setValue(value: V): V =
        throw new UnsupportedOperationException("not supported")

      override def equals(o: Any): Boolean = o match {
        case o: Map.Entry[_, _] =>
          Objects.equals(getKey(), o.getKey()) &&
            Objects.equals(getValue(), o.getValue())
        case _ =>
          false
      }

      override def hashCode(): Int = {
        // vals k and v are known to be not null at this point.
        val res = 31 * 1 + k.hashCode()
        31 * res + v.hashCode()
      }
    }
  }

  // Since: Java 9
  def of[K, V](): Map[K, V] = {
    val mapSize = 0

    val underlying = HashMap.newHashMap[K, V](mapSize)

    Collections.unmodifiableMap(underlying)
  }

  private def appendMapOfElement[K, V](
      key: K,
      value: V,
      hm: HashMap[K, V]
  ): Unit = {
    Objects.requireNonNull(key)
    Objects.requireNonNull(value)

    if (hm.putIfAbsent(key, value) != null)
      throw new IllegalArgumentException("duplicate key: ${key}")
  }

  // Since: Java 9
  def of[K, V](k1: K, v1: V): Map[K, V] = {
    val mapSize = 1

    val underlying = HashMap.newHashMap[K, V](mapSize)

    appendMapOfElement[K, V](k1, v1, underlying)

    Collections.unmodifiableMap(underlying)
  }

  // Since: Java 9
  def of[K, V](
// format: off
      k1: K, v1: V,
      k2: K, v2: V
// format: on
  ): Map[K, V] = {
    val mapSize = 2

    val underlying = HashMap.newHashMap[K, V](mapSize)

    appendMapOfElement[K, V](k1, v1, underlying)
    appendMapOfElement[K, V](k2, v2, underlying)

    Collections.unmodifiableMap(underlying)
  }

  // Since: Java 9
  def of[K, V](
// format: off
      k1: K, v1: V,
      k2: K, v2: V,
      k3: K, v3: V,
// format: on
  ): Map[K, V] = {

    val mapSize = 3

    val underlying = HashMap.newHashMap[K, V](mapSize)

    appendMapOfElement[K, V](k1, v1, underlying)
    appendMapOfElement[K, V](k2, v2, underlying)
    appendMapOfElement[K, V](k3, v3, underlying)

    Collections.unmodifiableMap(underlying)
  }

  // Since: Java 9
  def of[K, V](
// format: off
      k1: K, v1: V,
      k2: K, v2: V,
      k3: K, v3: V,
      k4: K, v4: V
// format: on
  ): Map[K, V] = {
    val mapSize = 4

    val underlying = HashMap.newHashMap[K, V](mapSize)

    appendMapOfElement[K, V](k1, v1, underlying)
    appendMapOfElement[K, V](k2, v2, underlying)
    appendMapOfElement[K, V](k3, v3, underlying)
    appendMapOfElement[K, V](k4, v4, underlying)

    Collections.unmodifiableMap(underlying)
  }

  // Since: Java 9
  def of[K, V](
// format: off
      k1: K, v1: V,
      k2: K, v2: V,
      k3: K, v3: V,
      k4: K, v4: V,
      k5: K, v5: V
// format: on
  ): Map[K, V] = {
    val mapSize = 5

    val underlying = HashMap.newHashMap[K, V](mapSize)

    appendMapOfElement[K, V](k1, v1, underlying)
    appendMapOfElement[K, V](k2, v2, underlying)
    appendMapOfElement[K, V](k3, v3, underlying)
    appendMapOfElement[K, V](k4, v4, underlying)
    appendMapOfElement[K, V](k5, v5, underlying)

    Collections.unmodifiableMap(underlying)
  }

  // Since: Java 9
  def of[K, V](
// format: off
      k1: K, v1: V,
      k2: K, v2: V,
      k3: K, v3: V,
      k4: K, v4: V,
      k5: K, v5: V,
      k6: K, v6: V
// format: on
  ): Map[K, V] = {
    val mapSize = 6

    val underlying = HashMap.newHashMap[K, V](mapSize)

    appendMapOfElement[K, V](k1, v1, underlying)
    appendMapOfElement[K, V](k2, v2, underlying)
    appendMapOfElement[K, V](k3, v3, underlying)
    appendMapOfElement[K, V](k4, v4, underlying)
    appendMapOfElement[K, V](k5, v5, underlying)
    appendMapOfElement[K, V](k6, v6, underlying)

    Collections.unmodifiableMap(underlying)
  }

  // Since: Java 9
  def of[K, V](
// format: off
      k1: K, v1: V,
      k2: K, v2: V,
      k3: K, v3: V,
      k4: K, v4: V,
      k5: K, v5: V,
      k6: K, v6: V,
      k7: K, v7: V
// format: on
  ): Map[K, V] = {
    val mapSize = 7

    val underlying = HashMap.newHashMap[K, V](mapSize)

    appendMapOfElement[K, V](k1, v1, underlying)
    appendMapOfElement[K, V](k2, v2, underlying)
    appendMapOfElement[K, V](k3, v3, underlying)
    appendMapOfElement[K, V](k4, v4, underlying)
    appendMapOfElement[K, V](k5, v5, underlying)
    appendMapOfElement[K, V](k6, v6, underlying)
    appendMapOfElement[K, V](k7, v7, underlying)

    Collections.unmodifiableMap(underlying)
  }

  // Since: Java 9
  def of[K, V](
// format: off
      k1: K, v1: V,
      k2: K, v2: V,
      k3: K, v3: V,
      k4: K, v4: V,
      k5: K, v5: V,
      k6: K, v6: V,
      k7: K, v7: V,
      k8: K, v8: V
// format: on
  ): Map[K, V] = {
    val mapSize = 8

    val underlying = HashMap.newHashMap[K, V](mapSize)

    appendMapOfElement[K, V](k1, v1, underlying)
    appendMapOfElement[K, V](k2, v2, underlying)
    appendMapOfElement[K, V](k3, v3, underlying)
    appendMapOfElement[K, V](k4, v4, underlying)
    appendMapOfElement[K, V](k5, v5, underlying)
    appendMapOfElement[K, V](k6, v6, underlying)
    appendMapOfElement[K, V](k7, v7, underlying)
    appendMapOfElement[K, V](k8, v8, underlying)

    Collections.unmodifiableMap(underlying)
  }

  // Since: Java 9
  def of[K, V](
// format: off
      k1: K, v1: V,
      k2: K, v2: V,
      k3: K, v3: V,
      k4: K, v4: V,
      k5: K, v5: V,
      k6: K, v6: V,
      k7: K, v7: V,
      k8: K, v8: V,
      k9: K, v9: V
// format: on
  ): Map[K, V] = {
    val mapSize = 9

    val underlying = HashMap.newHashMap[K, V](mapSize)

    appendMapOfElement[K, V](k1, v1, underlying)
    appendMapOfElement[K, V](k2, v2, underlying)
    appendMapOfElement[K, V](k3, v3, underlying)
    appendMapOfElement[K, V](k4, v4, underlying)
    appendMapOfElement[K, V](k5, v5, underlying)
    appendMapOfElement[K, V](k6, v6, underlying)
    appendMapOfElement[K, V](k7, v7, underlying)
    appendMapOfElement[K, V](k8, v8, underlying)
    appendMapOfElement[K, V](k9, v9, underlying)

    Collections.unmodifiableMap(underlying)
  }

  // Since: Java 9
  def of[K, V](
// format: off
      k1: K,  v1: V,
      k2: K,  v2: V,
      k3: K,  v3: V,
      k4: K,  v4: V,
      k5: K,  v5: V,
      k6: K,  v6: V,
      k7: K,  v7: V,
      k8: K,  v8: V,
      k9: K,  v9: V,
      k10: K, v10: V
// format: on
  ): Map[K, V] = {
    val mapSize = 10

    val underlying = HashMap.newHashMap[K, V](mapSize)

    appendMapOfElement[K, V](k1, v1, underlying)
    appendMapOfElement[K, V](k2, v2, underlying)
    appendMapOfElement[K, V](k3, v3, underlying)
    appendMapOfElement[K, V](k4, v4, underlying)
    appendMapOfElement[K, V](k5, v5, underlying)
    appendMapOfElement[K, V](k6, v6, underlying)
    appendMapOfElement[K, V](k7, v7, underlying)
    appendMapOfElement[K, V](k8, v8, underlying)
    appendMapOfElement[K, V](k9, v9, underlying)
    appendMapOfElement[K, V](k10, v10, underlying)

    Collections.unmodifiableMap(underlying)
  }

  def ofEntries[K, V](entries: Array[Map.Entry[K, V]]): Map[K, V] = {
    Objects.requireNonNull(entries)

    val mapSize = entries.size

    val underlying = HashMap.newHashMap[K, V](mapSize)

    for (j <- 0 until mapSize) {
      val e = entries(j)
      appendMapOfElement[K, V](e.getKey(), e.getValue(), underlying)
    }

    Collections.unmodifiableMap(underlying)
  }
}

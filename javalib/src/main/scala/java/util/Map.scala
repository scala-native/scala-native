package java.util

import java.util.function.{BiConsumer, BiFunction, Function}

// These declarations follow the Scala.js standard of listing
// methods in the order of their full, not summary,
// description in the Java 8 documentation.

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

  // Java 8 default methods

  def getOrDefault(key: Any, default: V): V = {
    val v = this.get(key)
    if (v != null.asInstanceOf[V]) {
      v
    } else if (this.containsKey(key)) {
      null.asInstanceOf[V]
    } else {
      default
    }
  }

  def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit = {
    if (action == null)
      throw new NullPointerException

    val it = this.entrySet.iterator
    while (it.hasNext()) {
      val entry = it.next()
      action.accept(entry.getKey(), entry.getValue())
    }
  }

  def replaceAll(function: BiFunction[_ >: K, _ >: V, _ <: V]): Unit = {
    if (function == null)
      throw new NullPointerException

    val it = this.entrySet.iterator
    while (it.hasNext()) {
      val entry = it.next()
      entry.setValue(function.apply(entry.getKey(), entry.getValue()))
    }
  }

  def putIfAbsent(key: K, value: V): V = {
    val v = this.get(key)
    if ((v != null.asInstanceOf[V]) || this.containsKey(key))
      v
    else
      this.put(key, value)
  }

  def remove(key: Any, value: Any): Boolean = {
    if (!((this.containsKey(key)) && (this.get(key) == value))) {
      false
    } else {
      this.remove(key)
      true
    }
  }

  def replace(key: K, oldValue: V, newValue: V): Boolean = {
    if (!((this.containsKey(key)) && (this.get(key) == oldValue))) {
      false
    } else {
      this.put(key, newValue)
      true
    }
  }

  def replace(key: K, value: V): V = {
    if (!this.containsKey(key))
      null.asInstanceOf[V]
    else
      this.put(key, value)
  }

  def computeIfAbsent(key: K, mappingFunction: Function[_ >: K, _ <: V]): V = {
    if (mappingFunction == null)
      throw new NullPointerException

    val oldValue = this.get(key)

    if (oldValue != null.asInstanceOf[V]) {
      oldValue
    } else {
      val newValue = mappingFunction.apply(key)
      if (newValue != null.asInstanceOf[V]) {
        this.put(key, newValue)
      }
      newValue
    }
  }

  def computeIfPresent(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]): V = {
    if (remappingFunction == null)
      throw new NullPointerException

    val oldValue = this.get(key)

    if (oldValue == null.asInstanceOf[V]) {
      null.asInstanceOf[V]
    } else {
      val newValue = remappingFunction.apply(key, oldValue)
      if (newValue == null.asInstanceOf[V]) {
        this.remove(key)
        null.asInstanceOf[V]
      } else {
        this.put(key, newValue)
        newValue
      }
    }
  }

  def compute(key: K,
              remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]): V = {
    if (remappingFunction == null)
      throw new NullPointerException

    val oldValue = this.get(key)
    val newValue = remappingFunction.apply(key, oldValue)

    if (oldValue != null.asInstanceOf[V]) {
      if (newValue != null.asInstanceOf[V]) {
        this.put(key, newValue)
      } else {
        this.remove(key)
      }
    } else {
      if (newValue == null.asInstanceOf[V])
        null.asInstanceOf[V]
      else
        this.put(key, newValue)
    }
  }

  def merge(key: K,
            value: V,
            remappingFunction: BiFunction[_ >: V, _ >: V, _ <: V]): V = {
    if ((value == null.asInstanceOf[V]) || (remappingFunction == null))
      throw new NullPointerException

    val oldValue = this.get(key)

    if (oldValue == null.asInstanceOf[V]) {
      this.put(key, value)
      value
    } else {
      val newValue = remappingFunction.apply(oldValue, value)

      if (newValue == null.asInstanceOf[V]) {
        this.remove(key)
        null.asInstanceOf[V]
      } else {
        this.put(key, newValue)
        newValue
      }
    }
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

/*
 * Ported from JSR-166
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

import java.util.*
import java.util.function.{BiFunction, Function, BiConsumer}
import java.util as ju

trait ConcurrentMap[K, V] extends Map[K, V] {
  def putIfAbsent(key: K, value: V): V
  def remove(key: Any, value: Any): Boolean
  def replace(key: K, oldValue: V, newValue: V): Boolean
  def replace(key: K, value: V): V

  // Concurrency aware overrides
  // JDK assumes ConcurrentMap cannot contain null values
  override def getOrDefault(key: Any, defaultValue: V): V = get(key) match {
    case null => defaultValue
    case v    => v
  }

  override def forEach(action: BiConsumer[? >: K, ? >: V]): Unit = {
    Objects.requireNonNull(action)
    entrySet().forEach(usingEntry(_)(action.accept))
  }

  override def replaceAll(
      function: BiFunction[? >: K, ? >: V, ? <: V]
  ): Unit = {
    Objects.requireNonNull(function)
    forEach { (k, _v) =>
      var break = false
      var v = _v
      while (!break && !replace(k, v, function.apply(k, v))) {
        v = get(k)
        if (v == null) break = true
      }
    }
  }

  override def computeIfAbsent(
      key: K,
      mappingFunction: Function[? >: K, ? <: V]
  ): V = {
    Objects.requireNonNull(mappingFunction)

    val oldValue = get(key)
    if (oldValue != null) oldValue
    else {
      val newValue = mappingFunction.apply(key)
      if (newValue == null) oldValue
      else {
        putIfAbsent(key, newValue) match {
          case null     => newValue
          case oldValue => oldValue
        }
      }
    }
  }

  override def computeIfPresent(
      key: K,
      remappingFunction: BiFunction[? >: K, ? >: V, ? <: V]
  ): V = {
    Objects.requireNonNull(remappingFunction)
    while ({
      val oldValue = get(key)
      if (oldValue == null) return null.asInstanceOf[V]
      else {
        val newValue = remappingFunction.apply(key, oldValue)
        val updated =
          if (newValue == null) remove(key, oldValue)
          else replace(key, oldValue, newValue)
        if (updated) return newValue
        true
      }
    }) ()
    // unreachable
    null.asInstanceOf[V]
  }

  override def compute(
      key: K,
      remappingFunction: BiFunction[? >: K, ? >: V, ? <: V]
  ): V = {
    var oldValue = get(key)
    while (true) { // haveOldValue
      // if putIfAbsent fails, opportunistically use its return value
      val newValue = remappingFunction.apply(key, oldValue)
      if (newValue != null) {
        if (oldValue != null) {
          if (replace(key, oldValue, newValue)) return newValue
        } else {
          oldValue = putIfAbsent(key, newValue)
          if (oldValue == null) return newValue
          else () // continue haveOldValue
        }
      } else if (oldValue == null || remove(key, oldValue)) {
        return null.asInstanceOf[V]
      } else oldValue = get(key)
    }
    // unreachable
    return null.asInstanceOf[V]
  }

  override def merge(
      key: K,
      value: V,
      remappingFunction: BiFunction[? >: V, ? >: V, ? <: V]
  ): V = {
    Objects.requireNonNull(remappingFunction)
    Objects.requireNonNull(value)
    var oldValue = get(key)
    while (true) { // haveOldValue
      if (oldValue != null) {
        val newValue = remappingFunction.apply(oldValue, value)
        if (newValue != null) {
          if (replace(key, oldValue, newValue)) return newValue
        } else if (remove(key, oldValue)) return null.asInstanceOf[V]
        else oldValue = get(key)
      } else {
        oldValue = putIfAbsent(key, value)
        if (oldValue == null) return value
        else () // continue haveOldValue
      }
    }
    // unreachable
    return null.asInstanceOf[V]
  }
}

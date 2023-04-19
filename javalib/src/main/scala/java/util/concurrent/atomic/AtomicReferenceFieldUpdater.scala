/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent.atomic

import java.util.function.BinaryOperator
import java.util.function.UnaryOperator

object AtomicReferenceFieldUpdater {
  // Imposible to define currently in Scala Native, requires reflection
  // Don't define it, allow to fail at linktime instead of runtime
  // def newUpdater[U <: AnyRef, W <: AnyRef](
  //     tclass: Class[U],
  //     vclass: Class[W],
  //     fieldName: String
  // ): AtomicReferenceFieldUpdater[U, W] = ???
}

abstract class AtomicReferenceFieldUpdater[
    T <: AnyRef,
    V <: AnyRef
] protected () {
  def compareAndSet(obj: T, expect: V, update: V): Boolean
  def weakCompareAndSet(obj: T, expect: V, update: V): Boolean
  def set(obj: T, newValue: V): Unit
  def lazySet(obj: T, newValue: V): Unit
  def get(obj: T): V

  def getAndSet(obj: T, newValue: V): V = {
    var prev: V = null.asInstanceOf[V]
    while ({
      prev = get(obj)
      !compareAndSet(obj, prev, newValue)
    }) ()
    prev
  }

  final def getAndUpdate(obj: T, updateFunction: UnaryOperator[V]): V = {
    var prev: V = null.asInstanceOf[V]
    while ({
      prev = get(obj)
      val next = updateFunction(prev)
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  final def updateAndGet(obj: T, updateFunction: UnaryOperator[V]): V = {
    var next: V = null.asInstanceOf[V]
    while ({
      val prev = get(obj)
      next = updateFunction(prev)
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  final def getAndAccumulate(
      obj: T,
      x: V,
      accumulatorFunction: BinaryOperator[V]
  ): V = {
    var prev: V = null.asInstanceOf[V]
    while ({
      prev = get(obj)
      val next = accumulatorFunction(prev, x)
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  final def accumulateAndGet(
      obj: T,
      x: V,
      accumulatorFunction: BinaryOperator[V]
  ): V = {
    var next: V = null.asInstanceOf[V]
    while ({
      val prev = get(obj)
      next = accumulatorFunction(prev, x)
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }
}

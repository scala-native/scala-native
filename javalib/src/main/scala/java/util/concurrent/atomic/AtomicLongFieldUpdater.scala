/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent.atomic

import java.util.function.BinaryOperator
import java.util.function.UnaryOperator

object AtomicLongFieldUpdater {
  // Imposible to define currently in Scala Native, requires reflection
  // Don't define it, allow to fail at linktime instead of runtime
  // def newUpdater[U <: AnyRef](
  //     tclass: Class[U],
  //     fieldName: String
  // ): AtomicLongFieldUpdater[U] = ???
}

abstract class AtomicLongFieldUpdater[T <: AnyRef] protected () {
  def compareAndSet(obj: T, expect: Long, update: Long): Boolean
  def weakCompareAndSet(obj: T, expect: Long, update: Long): Boolean
  def set(obj: T, newLongalue: Long): Unit
  def lazySet(obj: T, newLongalue: Long): Unit
  def get(obj: T): Long

  def getAndSet(obj: T, newLongalue: Long): Long = {
    var prev: Long = null.asInstanceOf[Long]
    while ({
      prev = get(obj)
      !compareAndSet(obj, prev, newLongalue)
    }) ()
    prev
  }

  final def getAndUpdate(obj: T, updateFunction: UnaryOperator[Long]): Long = {
    var prev: Long = null.asInstanceOf[Long]
    while ({
      prev = get(obj)
      val next = updateFunction(prev)
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  final def updateAndGet(obj: T, updateFunction: UnaryOperator[Long]): Long = {
    var next: Long = null.asInstanceOf[Long]
    while ({
      val prev = get(obj)
      next = updateFunction(prev)
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  final def getAndAccumulate(
      obj: T,
      x: Long,
      accumulatorFunction: BinaryOperator[Long]
  ): Long = {
    var prev: Long = null.asInstanceOf[Long]
    while ({
      prev = get(obj)
      val next = accumulatorFunction(prev, x)
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  final def accumulateAndGet(
      obj: T,
      x: Long,
      accumulatorFunction: BinaryOperator[Long]
  ): Long = {
    var next: Long = null.asInstanceOf[Long]
    while ({
      val prev = get(obj)
      next = accumulatorFunction(prev, x)
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  def getAndIncrement(obj: T): Long = {
    var prev = 0L
    while ({
      prev = get(obj)
      val next = prev + 1L
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  def getAndDecrement(obj: T): Long = {
    var prev = 0L
    while ({
      prev = get(obj)
      val next = prev - 1L
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  def getAndAdd(obj: T, delta: Long): Long = {
    var prev = 0L
    while ({
      prev = get(obj)
      val next = prev + delta
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  def incrementAndGet(obj: T): Long = {
    var next = 0L
    while ({
      val prev = get(obj)
      next = prev + 1L
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  def decrementAndGet(obj: T): Long = {
    var next = 0L
    while ({
      val prev = get(obj)
      next = prev - 1L
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  def addAndGet(obj: T, delta: Long): Long = {
    var next = 0L
    while ({
      val prev = get(obj)
      next = prev + delta
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }
}

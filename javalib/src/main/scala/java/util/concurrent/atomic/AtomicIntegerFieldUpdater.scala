/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent.atomic

import java.util.function.BinaryOperator
import java.util.function.UnaryOperator

object AtomicIntegerFieldUpdater {
  // Imposible to define currently in Scala Native, requires reflection
  // Don't define it, allow to fail at linktime instead of runtime
  // def newUpdater[U <: AnyRef](
  //     tclass: Class[U],
  //     fieldName: String
  // ): AtomicIntegerFieldUpdater[U] = ???
}

abstract class AtomicIntegerFieldUpdater[T <: AnyRef] protected () {
  def compareAndSet(obj: T, expect: Int, update: Int): Boolean
  def weakCompareAndSet(obj: T, expect: Int, update: Int): Boolean
  def set(obj: T, newIntalue: Int): Unit
  def lazySet(obj: T, newIntalue: Int): Unit
  def get(obj: T): Int

  def getAndSet(obj: T, newIntalue: Int): Int = {
    var prev: Int = null.asInstanceOf[Int]
    while ({
      prev = get(obj)
      !compareAndSet(obj, prev, newIntalue)
    }) ()
    prev
  }

  final def getAndUpdate(obj: T, updateFunction: UnaryOperator[Int]): Int = {
    var prev: Int = null.asInstanceOf[Int]
    while ({
      prev = get(obj)
      val next = updateFunction(prev)
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  final def updateAndGet(obj: T, updateFunction: UnaryOperator[Int]): Int = {
    var next: Int = null.asInstanceOf[Int]
    while ({
      val prev = get(obj)
      next = updateFunction(prev)
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  final def getAndAccumulate(
      obj: T,
      x: Int,
      accumulatorFunction: BinaryOperator[Int]
  ): Int = {
    var prev: Int = null.asInstanceOf[Int]
    while ({
      prev = get(obj)
      val next = accumulatorFunction(prev, x)
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  final def accumulateAndGet(
      obj: T,
      x: Int,
      accumulatorFunction: BinaryOperator[Int]
  ): Int = {
    var next: Int = null.asInstanceOf[Int]
    while ({
      val prev = get(obj)
      next = accumulatorFunction(prev, x)
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  def getAndIncrement(obj: T): Int = {
    var prev = 0
    while ({
      prev = get(obj)
      val next = prev + 1
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  def getAndDecrement(obj: T): Int = {
    var prev = 0
    while ({
      prev = get(obj)
      val next = prev - 1
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  def getAndAdd(obj: T, delta: Int): Int = {
    var prev = 0
    while ({
      prev = get(obj)
      val next = prev + delta
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  def incrementAndGet(obj: T): Int = {
    var next = 0
    while ({
      val prev = get(obj)
      next = prev + 1
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  def decrementAndGet(obj: T): Int = {
    var next = 0
    while ({
      val prev = get(obj)
      next = prev - 1
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  def addAndGet(obj: T, delta: Int): Int = {
    var next = 0
    while ({
      val prev = get(obj)
      next = prev + delta
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

}

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent.atomic

import java.util.function.{IntBinaryOperator, IntUnaryOperator}

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.AtomicInt
import scala.scalanative.libc.stdatomic.memory_order.memory_order_release
import scala.scalanative.unsafe.Ptr

object AtomicIntegerFieldUpdater {
  private def intrinsic = throw new AssertionError(
    "Intrinsic call was not handled by the toolchain"
  )

  def newUpdater[U <: AnyRef](
      tclass: Class[U],
      fieldName: String
  ): AtomicIntegerFieldUpdater[U] = intrinsic

  final class Impl[T <: AnyRef](binding: AnyRef => Ptr[Int])
      extends AtomicIntegerFieldUpdater[T] {
    @alwaysinline private def atomic(obj: T) =
      new AtomicInt(binding(obj))
    def compareAndSet(obj: T, expect: Int, update: Int) =
      atomic(obj).compareExchangeStrong(expect, update)
    def weakCompareAndSet(obj: T, expect: Int, update: Int) =
      atomic(obj).compareExchangeWeak(expect, update)
    def set(obj: T, value: Int): Unit = atomic(obj).store(value)
    def lazySet(obj: T, value: Int): Unit =
      atomic(obj).store(value, memory_order_release)
    def get(obj: T): Int = atomic(obj).load()
  }
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

  final def getAndUpdate(obj: T, updateFunction: IntUnaryOperator): Int = {
    var prev: Int = null.asInstanceOf[Int]
    while ({
      prev = get(obj)
      val next = updateFunction.applyAsInt(prev)
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  final def updateAndGet(obj: T, updateFunction: IntUnaryOperator): Int = {
    var next: Int = null.asInstanceOf[Int]
    while ({
      val prev = get(obj)
      next = updateFunction.applyAsInt(prev)
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  final def getAndAccumulate(
      obj: T,
      x: Int,
      accumulatorFunction: IntBinaryOperator
  ): Int = {
    var prev: Int = null.asInstanceOf[Int]
    while ({
      prev = get(obj)
      val next = accumulatorFunction.applyAsInt(prev, x)
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  final def accumulateAndGet(
      obj: T,
      x: Int,
      accumulatorFunction: IntBinaryOperator
  ): Int = {
    var next: Int = null.asInstanceOf[Int]
    while ({
      val prev = get(obj)
      next = accumulatorFunction.applyAsInt(prev, x)
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

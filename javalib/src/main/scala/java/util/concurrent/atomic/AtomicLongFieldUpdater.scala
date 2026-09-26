/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent.atomic

import java.util.function.{LongBinaryOperator, LongUnaryOperator}

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.AtomicLongLong
import scala.scalanative.libc.stdatomic.memory_order.memory_order_release
import scala.scalanative.unsafe.Ptr

object AtomicLongFieldUpdater {
  private def intrinsic = throw new AssertionError(
    "Intrinsic call was not handled by the toolchain"
  )

  def newUpdater[U <: AnyRef](
      tclass: Class[U],
      fieldName: String
  ): AtomicLongFieldUpdater[U] = intrinsic

  final class Impl[T <: AnyRef](binding: AnyRef => Ptr[Long])
      extends AtomicLongFieldUpdater[T] {
    @alwaysinline private def atomic(obj: T) =
      new AtomicLongLong(binding(obj))
    def compareAndSet(obj: T, expect: Long, update: Long) =
      atomic(obj).compareExchangeStrong(expect, update)
    def weakCompareAndSet(obj: T, expect: Long, update: Long) =
      atomic(obj).compareExchangeWeak(expect, update)
    def set(obj: T, value: Long): Unit = atomic(obj).store(value)
    def lazySet(obj: T, value: Long): Unit =
      atomic(obj).store(value, memory_order_release)
    def get(obj: T): Long = atomic(obj).load()
  }
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

  final def getAndUpdate(obj: T, updateFunction: LongUnaryOperator): Long = {
    var prev: Long = null.asInstanceOf[Long]
    while ({
      prev = get(obj)
      val next = updateFunction.applyAsLong(prev)
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  final def updateAndGet(obj: T, updateFunction: LongUnaryOperator): Long = {
    var next: Long = null.asInstanceOf[Long]
    while ({
      val prev = get(obj)
      next = updateFunction.applyAsLong(prev)
      !compareAndSet(obj, prev, next)
    }) ()
    next
  }

  final def getAndAccumulate(
      obj: T,
      x: Long,
      accumulatorFunction: LongBinaryOperator
  ): Long = {
    var prev: Long = null.asInstanceOf[Long]
    while ({
      prev = get(obj)
      val next = accumulatorFunction.applyAsLong(prev, x)
      !compareAndSet(obj, prev, next)
    }) ()
    prev
  }

  final def accumulateAndGet(
      obj: T,
      x: Long,
      accumulatorFunction: LongBinaryOperator
  ): Long = {
    var next: Long = null.asInstanceOf[Long]
    while ({
      val prev = get(obj)
      next = accumulatorFunction.applyAsLong(prev, x)
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

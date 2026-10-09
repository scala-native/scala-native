/*
 * Based on JSR-166 originally written by Doug Lea with assistance
 * from members of JCP JSR-166 Expert Group and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic

import java.io.Serializable
import java.lang.invoke.{MethodHandles, VarHandle}
import java.util.function.{LongBinaryOperator, LongUnaryOperator}

import scala.annotation.tailrec

@SerialVersionUID(1927816293512124184L)
class AtomicLong(private var value: Long) extends Number with Serializable {

  def this() = {
    this(0)
  }

  /** Returns the current value, with memory effects as specified by
   *  `VarHandle#getVolatile`.
   *
   *  @return
   *    the current value
   */
  final def get(): Long = AtomicLong.VALUE.getVolatile(this)

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setVolatile`.
   *
   *  @param newValue
   *    the new value
   */
  final def set(newValue: Long): Unit = AtomicLong.VALUE.setVolatile(this, newValue)

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setRelease`.
   *
   *  @param newValue
   *    the new value
   *  @since 1.6
   */
  final def lazySet(newValue: Long): Unit = {
    AtomicLong.VALUE.setRelease(this, newValue)
  }

  /** Atomically sets the value to {@code newValue} and returns the old value,
   *  with memory effects as specified by `VarHandle#getAndSet`.
   *
   *  @param newValue
   *    the new value
   *  @return
   *    the previous value
   */
  final def getAndSet(newValue: Long): Long = {
    AtomicLong.VALUE.getAndSet(this, newValue)
  }

  /** Atomically sets the value to {@code newValue} if the current value {@code
   *  \== expectedValue}, with memory effects as specified by
   *  `VarHandle#compareAndSet`.
   *
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful. False return indicates that the actual value
   *    was not equal to the expected value.
   */
  final def compareAndSet(expectedValue: Long, newValue: Long): Boolean = {
    AtomicLong.VALUE.compareAndSet(this, expectedValue, newValue)
  }

  /** Possibly atomically sets the value to {@code newValue} if the current
   *  value {@code == expectedValue}, with memory effects as specified by
   *  `VarHandle#weakCompareAndSetPlain`.
   *
   *  @deprecated
   *    This method has plain memory effects but the method name implies
   *    volatile memory effects (see methods such as {@link #compareAndExchange}
   *    and {@link #compareAndSet}). To avoid confusion over plain or volatile
   *    memory effects it is recommended that the method
   *    [[#weakCompareAndSetPlain]] be used instead.
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful
   *  @see
   *    #weakCompareAndSetPlain
   */
  @deprecated("", "9")
  final def weakCompareAndSet(expectedValue: Long, newValue: Long): Boolean = {
    AtomicLong.VALUE.weakCompareAndSet(this, expectedValue, newValue)
  }

  /** Possibly atomically sets the value to {@code newValue} if the current
   *  value {@code == expectedValue}, with memory effects as specified by
   *  `VarHandle#weakCompareAndSetPlain`.
   *
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful
   *  @since 9
   */
  final def weakCompareAndSetPlain(
      expectedValue: Long,
      newValue: Long
  ): Boolean = {
    AtomicLong.VALUE.weakCompareAndSetPlain(this, expectedValue, newValue)
  }

  /** Atomically increments the current value, with memory effects as specified
   *  by `VarHandle#getAndAdd`.
   *
   *  <p>Equivalent to {@code getAndAdd(1)}.
   *
   *  @return
   *    the previous value
   */
  final def getAndIncrement(): Long = getAndAdd(1)

  /** Atomically decrements the current value, with memory effects as specified
   *  by `VarHandle#getAndAdd`.
   *
   *  <p>Equivalent to {@code getAndAdd(-1)}.
   *
   *  @return
   *    the previous value
   */
  final def getAndDecrement(): Long = getAndAdd(-1)

  /** Atomically adds the given value to the current value, with memory effects
   *  as specified by `VarHandle#getAndAdd`.
   *
   *  @param delta
   *    the value to add
   *  @return
   *    the previous value
   */
  final def getAndAdd(delta: Long): Long = {
    AtomicLong.VALUE.getAndAdd(this, delta)
  }

  /** Atomically increments the current value, with memory effects as specified
   *  by `VarHandle#getAndAdd`.
   *
   *  <p>Equivalent to {@code addAndGet(1)}.
   *
   *  @return
   *    the updated value
   */
  final def incrementAndGet(): Long = addAndGet(1)

  /** Atomically decrements the current value, with memory effects as specified
   *  by `VarHandle#getAndAdd`.
   *
   *  <p>Equivalent to {@code addAndGet(-1)}.
   *
   *  @return
   *    the updated value
   */
  final def decrementAndGet(): Long = addAndGet(-1)

  /** Atomically adds the given value to the current value, with memory effects
   *  as specified by `VarHandle#getAndAdd`.
   *
   *  @param delta
   *    the value to add
   *  @return
   *    the updated value
   */
  final def addAndGet(delta: Long): Long = (AtomicLong.VALUE.getAndAdd(this, delta): Long) + delta

  /** Atomically updates (with memory effects as specified by
   *  `VarHandle#compareAndSet`) the current value with the results of applying
   *  the given function, returning the previous value. The function should be
   *  side-effect-free, since it may be re-applied when attempted updates fail
   *  due to contention among threads.
   *
   *  @param updateFunction
   *    a side-effect-free function
   *  @return
   *    the previous value
   *  @since 1.8
   */
  final def getAndUpdate(updateFunction: LongUnaryOperator): Long = {
    @tailrec
    def loop(prev: Long, next: Long, haveNext: Boolean): Long = {
      val newNext =
        if (!haveNext) updateFunction.applyAsLong(prev)
        else next

      if (weakCompareAndSetVolatile(prev, newNext)) prev
      else {
        val newPrev = get()
        loop(newPrev, newNext, haveNext = prev == newPrev)
      }
    }
    loop(get(), 0L, false)
  }

  /** Atomically updates (with memory effects as specified by
   *  `VarHandle#compareAndSet`) the current value with the results of applying
   *  the given function, returning the updated value. The function should be
   *  side-effect-free, since it may be re-applied when attempted updates fail
   *  due to contention among threads.
   *
   *  @param updateFunction
   *    a side-effect-free function
   *  @return
   *    the updated value
   *  @since 1.8
   */
  final def updateAndGet(updateFunction: LongUnaryOperator): Long = {
    @tailrec
    def loop(prev: Long, next: Long, haveNext: Boolean): Long = {
      val newNext =
        if (!haveNext) updateFunction.applyAsLong(prev)
        else next

      if (weakCompareAndSetVolatile(prev, newNext)) newNext
      else {
        val newPrev = get()
        loop(newPrev, newNext, prev == newPrev)
      }
    }
    loop(get(), 0L, false)
  }

  /** Atomically updates (with memory effects as specified by
   *  `VarHandle#compareAndSet`) the current value with the results of applying
   *  the given function to the current and given values, returning the previous
   *  value. The function should be side-effect-free, since it may be re-applied
   *  when attempted updates fail due to contention among threads. The function
   *  is applied with the current value as its first argument, and the given
   *  update as the second argument.
   *
   *  @param x
   *    the update value
   *  @param accumulatorFunction
   *    a side-effect-free function of two arguments
   *  @return
   *    the previous value
   *  @since 1.8
   */
  final def getAndAccumulate(
      x: Long,
      accumulatorFunction: LongBinaryOperator
  ): Long = {
    @tailrec
    def loop(prev: Long, next: Long, haveNext: Boolean): Long = {
      val newNext =
        if (!haveNext) accumulatorFunction.applyAsLong(prev, x)
        else next

      if (weakCompareAndSetVolatile(prev, newNext)) prev
      else {
        val newPrev = get()
        loop(newPrev, newNext, prev == newPrev)
      }
    }
    loop(get(), 0, false)
  }

  /** Atomically updates (with memory effects as specified by
   *  `VarHandle#compareAndSet`) the current value with the results of applying
   *  the given function to the current and given values, returning the updated
   *  value. The function should be side-effect-free, since it may be re-applied
   *  when attempted updates fail due to contention among threads. The function
   *  is applied with the current value as its first argument, and the given
   *  update as the second argument.
   *
   *  @param x
   *    the update value
   *  @param accumulatorFunction
   *    a side-effect-free function of two arguments
   *  @return
   *    the updated value
   *  @since 1.8
   */
  final def accumulateAndGet(
      x: Long,
      accumulatorFunction: LongBinaryOperator
  ): Long = {
    @tailrec
    def loop(prev: Long, next: Long, haveNext: Boolean): Long = {
      val newNext =
        if (!haveNext) accumulatorFunction.applyAsLong(prev, x)
        else next

      if (weakCompareAndSetVolatile(prev, newNext)) newNext
      else {
        val newPrev = get()
        loop(newPrev, newNext, prev == newPrev)
      }
    }
    loop(get(), 0, false)
  }

  /** Returns the String representation of the current value.
   *
   *  @return
   *    the String representation of the current value
   */
  override def toString(): String = get().toString()

  /** Returns the current value of this {@code AtomicInteger} as an {@code int},
   *  with memory effects as specified by `VarHandle#getVolatile`.
   *
   *  Equivalent to {@link #get ( )}.
   */
  override def intValue(): Int = get().toInt

  /** Returns the current value of this {@code AtomicInteger} as a {@code long}
   *  after a widening primitive conversion, with memory effects as specified by
   *  `VarHandle#getVolatile`.
   */
  override def longValue(): Long = get().toLong

  /** Returns the current value of this {@code AtomicInteger} as a {@code float}
   *  after a widening primitive conversion, with memory effects as specified by
   *  `VarHandle#getVolatile`.
   */
  override def floatValue(): Float = get().toFloat

  /** Returns the current value of this {@code AtomicInteger} as a {@code
   *  double} after a widening primitive conversion, with memory effects as
   *  specified by `VarHandle#getVolatile`.
   */
  override def doubleValue(): Double = get().toDouble

  /** Returns the current value, with memory semantics of reading as if the
   *  variable was declared non-{@code volatile}.
   *
   *  @return
   *    the value
   *  @since 9
   */
  final def getPlain(): Long = value

  /** Sets the value to {@code newValue}, with memory semantics of setting as if
   *  the variable was declared non-{@code volatile} and non-{@code final}.
   *
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setPlain(newValue: Long): Unit = {
    value = newValue
  }

  /** Returns the current value, with memory effects as specified by
   *  `VarHandle#getOpaque`.
   *
   *  @return
   *    the value
   *  @since 9
   */
  final def getOpaque(): Long = AtomicLong.VALUE.getOpaque(this)

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setOpaque`.
   *
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setOpaque(newValue: Long): Unit =
    AtomicLong.VALUE.setOpaque(this, newValue)

  /** Returns the current value, with memory effects as specified by
   *  `VarHandle#getAcquire`.
   *
   *  @return
   *    the value
   *  @since 9
   */
  final def getAcquire: Long = AtomicLong.VALUE.getAcquire(this)

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setRelease`.
   *
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setRelease(newValue: Long): Unit =
    AtomicLong.VALUE.setRelease(this, newValue)

  /** Atomically sets the value to {@code newValue} if the current value,
   *  referred to as the <em>witness value</em>, {@code == expectedValue}, with
   *  memory effects as specified by `VarHandle#compareAndExchange`.
   *
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    the witness value, which will be the same as the expected value if
   *    successful
   *  @since 9
   */
  final def compareAndExchange(expectedValue: Long, newValue: Long): Long =
    AtomicLong.VALUE.compareAndExchange(this, expectedValue, newValue)

  /** Atomically sets the value to {@code newValue} if the current value,
   *  referred to as the <em>witness value</em>, {@code == expectedValue}, with
   *  memory effects as specified by `VarHandle#compareAndExchangeAcquire`.
   *
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    the witness value, which will be the same as the expected value if
   *    successful
   *  @since 9
   */
  final def compareAndExchangeAcquire(
      expectedValue: Long,
      newValue: Long
  ): Long =
    AtomicLong.VALUE.compareAndExchangeAcquire(this, expectedValue, newValue)

  /** Atomically sets the value to {@code newValue} if the current value,
   *  referred to as the <em>witness value</em>, {@code == expectedValue}, with
   *  memory effects as specified by `VarHandle#compareAndExchangeRelease`.
   *
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    the witness value, which will be the same as the expected value if
   *    successful
   *  @since 9
   */
  final def compareAndExchangeRelease(
      expectedValue: Long,
      newValue: Long
  ): Long =
    AtomicLong.VALUE.compareAndExchangeRelease(this, expectedValue, newValue)

  /** Possibly atomically sets the value to {@code newValue} if the current
   *  value {@code == expectedValue}, with memory effects as specified by
   *  `VarHandle#weakCompareAndSet`.
   *
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful
   *  @since 9
   */
  final def weakCompareAndSetVolatile(
      expectedValue: Long,
      newValue: Long
  ): Boolean = {
    AtomicLong.VALUE.weakCompareAndSet(this, expectedValue, newValue)
  }

  /** Possibly atomically sets the value to {@code newValue} if the current
   *  value {@code == expectedValue}, with memory effects as specified by
   *  `VarHandle#weakCompareAndSetAcquire`.
   *
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful
   *  @since 9
   */
  final def weakCompareAndSetAcquire(
      expectedValue: Long,
      newValue: Long
  ): Boolean = {
    AtomicLong.VALUE.weakCompareAndSetAcquire(this, expectedValue, newValue)
  }

  /** Possibly atomically sets the value to {@code newValue} if the current
   *  value {@code == expectedValue}, with memory effects as specified by
   *  `VarHandle#weakCompareAndSetRelease`.
   *
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful
   *  @since 9
   */
  final def weakCompareAndSetRelease(
      expectedValue: Long,
      newValue: Long
  ): Boolean = {
    AtomicLong.VALUE.weakCompareAndSetRelease(this, expectedValue, newValue)
  }
}

object AtomicLong {
  private val VALUE: VarHandle = MethodHandles
    .privateLookupIn(classOf[AtomicLong], MethodHandles.lookup())
    .findVarHandle(classOf[AtomicLong], "value", classOf[Long])

}

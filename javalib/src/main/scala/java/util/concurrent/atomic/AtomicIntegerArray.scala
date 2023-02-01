/*
 * Based on JSR-166 originally written by Doug Lea with assistance
 * from members of JCP JSR-166 Expert Group and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic

import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.unsafe._
import scala.scalanative.libc.atomic.CAtomicInt
import scala.scalanative.libc.atomic.memory_order._
import java.util.function.IntBinaryOperator
import java.util.function.IntUnaryOperator
import scala.scalanative.runtime.IntArray

@SerialVersionUID(2862133569453604235L)
class AtomicIntegerArray extends Serializable {
  final private var array: Array[Int] = null

  @alwaysinline
  private[concurrent] def nativeArray: IntArray = array.asInstanceOf[IntArray]

  @alwaysinline
  private implicit def ptrIntToAtomicInt(ptr: Ptr[Int]): CAtomicInt =
    new CAtomicInt(ptr)

  /** Creates a new AtomicIntegerArray of the given length, with all elements
   *  initially zero.
   *
   *  @param length
   *    the length of the array
   */
  def this(length: Int) = {
    this()
    this.array = new Array[Int](length)
  }

  /** Creates a new AtomicIntegerArray with the same length as, and all elements
   *  copied from, the given array.
   *
   *  @param array
   *    the array to copy elements from
   *  @throws NullPointerException
   *    if array is null
   */
  def this(array: Array[Int]) = {
    this()
    this.array = array.clone()
  }

  /** Returns the length of the array.
   *
   *  @return
   *    the length of the array
   */
  final def length(): Int = array.length

  /** Returns the current value of the element at index {@code i}, with memory
   *  effects as specified by {@link VarHandle#getVolatile}.
   *
   *  @param i
   *    the index
   *  @return
   *    the current value
   */
  final def get(i: Int): Int = {
    nativeArray.at(i).load()
  }

  /** Sets the element at index {@code i} to {@code newValue}, with memory
   *  effects as specified by {@link VarHandle#setVolatile}.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   */
  final def set(i: Int, newValue: Int): Unit = {
    nativeArray.at(i).store(newValue)
  }

  /** Sets the element at index {@code i} to {@code newValue}, with memory
   *  effects as specified by {@link VarHandle#setRelease}.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   *  @since 1.6
   */
  final def lazySet(i: Int, newValue: Int): Unit = {
    nativeArray.at(i).store(newValue, memory_order_release)
  }

  /** Atomically sets the element at index {@code i} to {@code newValue} and
   *  returns the old value, with memory effects as specified by {@link
   *  VarHandle#getAndSet}.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   *  @return
   *    the previous value
   */
  final def getAndSet(i: Int, newValue: Int): Int = {
    nativeArray.at(i).exchange(newValue)
  }

  /** Atomically sets the element at index {@code i} to {@code newValue} if the
   *  element's current value {@code == expectedValue}, with memory effects as
   *  specified by {@link VarHandle#compareAndSet}.
   *
   *  @param i
   *    the index
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful. False return indicates that the actual value
   *    was not equal to the expected value.
   */
  final def compareAndSet(i: Int, expectedValue: Int, newValue: Int): Boolean =
    nativeArray.at(i).compareExchangeStrong(expectedValue, newValue)

  /** Possibly atomically sets the element at index {@code i} to {@code
   *  newValue} if the element's current value {@code == expectedValue}, with
   *  memory effects as specified by {@link VarHandle#weakCompareAndSetPlain}.
   *
   *  @deprecated
   *    This method has plain memory effects but the method name implies
   *    volatile memory effects (see methods such as {@link #compareAndExchange}
   *    and {@link #compareAndSet}). To avoid confusion over plain or volatile
   *    memory effects it is recommended that the method {@link
   *    #weakCompareAndSetPlain} be used instead.
   *
   *  @param i
   *    the index
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
  final def weakCompareAndSet(
      i: Int,
      expectedValue: Int,
      newValue: Int
  ): Boolean =
    weakCompareAndSetPlain(i, expectedValue, newValue)

  /** Possibly atomically sets the element at index {@code i} to {@code
   *  newValue} if the element's current value {@code == expectedValue}, with
   *  memory effects as specified by {@link VarHandle#weakCompareAndSetPlain}.
   *
   *  @param i
   *    the index
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful
   *  @since 9
   */
  final def weakCompareAndSetPlain(
      i: Int,
      expectedValue: Int,
      newValue: Int
  ): Boolean = {
    val ref = nativeArray.at(i)
    if (!ref == expectedValue) {
      !ref = newValue
      true
    } else false
  }

  /** Atomically increments the value of the element at index {@code i}, with
   *  memory effects as specified by {@link VarHandle#getAndAdd}.
   *
   *  <p>Equivalent to {@code getAndAdd(i, 1)}.
   *
   *  @param i
   *    the index
   *  @return
   *    the previous value
   */
  final def getAndIncrement()(i: Int): Int = {
    nativeArray.at(i).fetchAdd(1)
  }

  /** Atomically decrements the value of the element at index {@code i}, with
   *  memory effects as specified by {@link VarHandle#getAndAdd}.
   *
   *  <p>Equivalent to {@code getAndAdd(i, -1)}.
   *
   *  @param i
   *    the index
   *  @return
   *    the previous value
   */
  final def getAndDecrement(i: Int): Int =
    nativeArray.at(i).fetchAdd(-1)

  /** Atomically adds the given value to the element at index {@code i}, with
   *  memory effects as specified by {@link VarHandle#getAndAdd}.
   *
   *  @param i
   *    the index
   *  @param delta
   *    the value to add
   *  @return
   *    the previous value
   */
  final def getAndAdd(i: Int, delta: Int): Int =
    nativeArray.at(i).fetchAdd(delta)

  /** Atomically increments the value of the element at index {@code i}, with
   *  memory effects as specified by {@link VarHandle#getAndAdd}.
   *
   *  <p>Equivalent to {@code addAndGet(i, 1)}.
   *
   *  @param i
   *    the index
   *  @return
   *    the updated value
   */
  final def incrementAndGet(i: Int): Int =
    nativeArray.at(i).fetchAdd(1) + 1

  /** Atomically decrements the value of the element at index {@code i}, with
   *  memory effects as specified by {@link VarHandle#getAndAdd}.
   *
   *  <p>Equivalent to {@code addAndGet(i, -1)}.
   *
   *  @param i
   *    the index
   *  @return
   *    the updated value
   */
  final def decrementAndGet(i: Int): Int =
    nativeArray.at(i).fetchAdd(-1) - 1

  /** Atomically adds the given value to the element at index {@code i}, with
   *  memory effects as specified by {@link VarHandle#getAndAdd}.
   *
   *  @param i
   *    the index
   *  @param delta
   *    the value to add
   *  @return
   *    the updated value
   */
  final def addAndGet(i: Int, delta: Int): Int =
    nativeArray.at(i).fetchAdd(delta) + delta

  /** Atomically updates (with memory effects as specified by {@link
   *  VarHandle#compareAndSet}) the element at index {@code i} with the results
   *  of applying the given function, returning the previous value. The function
   *  should be side-effect-free, since it may be re-applied when attempted
   *  updates fail due to contention among threads.
   *
   *  @param i
   *    the index
   *  @param updateFunction
   *    a side-effect-free function
   *  @return
   *    the previous value
   *  @since 1.8
   */
  final def getAndUpdate(i: Int, updateFunction: IntUnaryOperator): Int = {
    @tailrec
    def loop(prev: Int, next: Int, haveNext: Boolean): Int = {
      val newNext =
        if (!haveNext) updateFunction.applyAsInt(prev)
        else next

      if (weakCompareAndSetVolatile(i, prev, newNext)) prev
      else {
        val newPrev = get(i)
        loop(newPrev, newNext, prev == newPrev)
      }
    }
    loop(get(i), 0, false)
  }

  /** Atomically updates (with memory effects as specified by {@link
   *  VarHandle#compareAndSet}) the element at index {@code i} with the results
   *  of applying the given function, returning the updated value. The function
   *  should be side-effect-free, since it may be re-applied when attempted
   *  updates fail due to contention among threads.
   *
   *  @param i
   *    the index
   *  @param updateFunction
   *    a side-effect-free function
   *  @return
   *    the updated value
   *  @since 1.8
   */
  final def updateAndGet(i: Int, updateFunction: IntUnaryOperator): Int = {
    @tailrec
    def loop(prev: Int, next: Int, haveNext: Boolean): Int = {
      val newNext =
        if (!haveNext) updateFunction.applyAsInt(prev)
        else next

      if (weakCompareAndSetVolatile(i, prev, newNext)) newNext
      else {
        val newPrev = get(i)
        loop(newPrev, newNext, prev == newPrev)
      }
    }
    loop(get(i), 0, false)
  }

  /** Atomically updates (with memory effects as specified by {@link
   *  VarHandle#compareAndSet}) the element at index {@code i} with the results
   *  of applying the given function to the current and given values, returning
   *  the previous value. The function should be side-effect-free, since it may
   *  be re-applied when attempted updates fail due to contention among threads.
   *  The function is applied with the current value of the element at index
   *  {@code i} as its first argument, and the given update as the second
   *  argument.
   *
   *  @param i
   *    the index
   *  @param x
   *    the update value
   *  @param accumulatorFunction
   *    a side-effect-free function of two arguments
   *  @return
   *    the previous value
   *  @since 1.8
   */
  final def getAndAccumulate(
      i: Int,
      x: Int,
      accumulatorFunction: IntBinaryOperator
  ): Int = {
    @tailrec
    def loop(prev: Int, next: Int, haveNext: Boolean): Int = {
      val newNext =
        if (!haveNext) accumulatorFunction.applyAsInt(prev, x)
        else next

      if (weakCompareAndSetVolatile(i, prev, newNext)) prev
      else {
        val newPrev = get(i)
        loop(newPrev, newNext, prev == newPrev)
      }
    }
    loop(get(i), 0, false)
  }

  /** Atomically updates (with memory effects as specified by {@link
   *  VarHandle#compareAndSet}) the element at index {@code i} with the results
   *  of applying the given function to the current and given values, returning
   *  the updated value. The function should be side-effect-free, since it may
   *  be re-applied when attempted updates fail due to contention among threads.
   *  The function is applied with the current value of the element at index
   *  {@code i} as its first argument, and the given update as the second
   *  argument.
   *
   *  @param i
   *    the index
   *  @param x
   *    the update value
   *  @param accumulatorFunction
   *    a side-effect-free function of two arguments
   *  @return
   *    the updated value
   *  @since 1.8
   */
  final def accumulateAndGet(
      i: Int,
      x: Int,
      accumulatorFunction: IntBinaryOperator
  ): Int = {
    @tailrec
    def loop(prev: Int, next: Int, haveNext: Boolean): Int = {
      val newNext =
        if (!haveNext) accumulatorFunction.applyAsInt(prev, x)
        else next

      if (weakCompareAndSetVolatile(i, prev, newNext)) newNext
      else {
        val newPrev = get(i)
        loop(newPrev, newNext, prev == newPrev)
      }
    }
    loop(get(i), 0, false)
  }

  /** Returns the String representation of the current values of array.
   *  @return
   *    the String representation of the current values of array
   */
  override def toString: String = {
    array.indices.map(get(_)).mkString("[", ", ", "]")
  }

  /** Returns the current value of the element at index {@code i}, with memory
   *  semantics of reading as if the variable was declared non-{@code volatile}.
   *
   *  @param i
   *    the index
   *  @return
   *    the value
   *  @since 9
   */
  final def getPlain(i: Int): Int =
    array(i)

  /** Sets the element at index {@code i} to {@code newValue}, with memory
   *  semantics of setting as if the variable was declared non-{@code volatile}
   *  and non-{@code final}.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setPlain(i: Int, newValue: Int): Unit = {
    array(i) = newValue
  }

  /** Returns the current value of the element at index {@code i}, with memory
   *  effects as specified by {@link VarHandle#getOpaque}.
   *
   *  @param i
   *    the index
   *  @return
   *    the value
   *  @since 9
   */
  final def getOpaque(i: Int): Int =
    nativeArray.at(i).load(memory_order_relaxed)

  /** Sets the element at index {@code i} to {@code newValue}, with memory
   *  effects as specified by {@link VarHandle#setOpaque}.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setOpaque(i: Int, newValue: Int): Unit = {
    nativeArray.at(i).store(newValue, memory_order_relaxed)
  }

  /** Returns the current value of the element at index {@code i}, with memory
   *  effects as specified by {@link VarHandle#getAcquire}.
   *
   *  @param i
   *    the index
   *  @return
   *    the value
   *  @since 9
   */
  final def getAcquire(i: Int): Int = {
    nativeArray.at(i).load(memory_order_acquire)
  }

  /** Sets the element at index {@code i} to {@code newValue}, with memory
   *  effects as specified by {@link VarHandle#setRelease}.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setRelease(i: Int, newValue: Int): Unit = {
    nativeArray.at(i).store(newValue, memory_order_release)
  }

  /** Atomically sets the element at index {@code i} to {@code newValue} if the
   *  element's current value, referred to as the <em>witness value</em>, {@code
   *  \== expectedValue}, with memory effects as specified by {@link
   *  VarHandle#compareAndExchange}.
   *
   *  @param i
   *    the index
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    the witness value, which will be the same as the expected value if
   *    successful
   *  @since 9
   */
  final def compareAndExchange(
      i: Int,
      expectedValue: Int,
      newValue: Int
  ): Int = {
    val expected = stackalloc[Int]()
    !expected = expectedValue
    nativeArray
      .at(i)
      .compareExchangeStrong(expected, newValue)
    !expected
  }

  /** Atomically sets the element at index {@code i} to {@code newValue} if the
   *  element's current value, referred to as the <em>witness value</em>, {@code
   *  \== expectedValue}, with memory effects as specified by {@link
   *  VarHandle#compareAndExchangeAcquire}.
   *
   *  @param i
   *    the index
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
      i: Int,
      expectedValue: Int,
      newValue: Int
  ): Int = {
    val expected = stackalloc[Int]()
    !expected = expectedValue
    nativeArray
      .at(i)
      .compareExchangeStrong(expected, newValue, memory_order_acquire)
    !expected
  }

  /** Atomically sets the element at index {@code i} to {@code newValue} if the
   *  element's current value, referred to as the <em>witness value</em>, {@code
   *  \== expectedValue}, with memory effects as specified by {@link
   *  VarHandle#compareAndExchangeRelease}.
   *
   *  @param i
   *    the index
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
      i: Int,
      expectedValue: Int,
      newValue: Int
  ): Int = {
    val expected = stackalloc[Int]()
    !expected = expectedValue
    nativeArray
      .at(i)
      .compareExchangeStrong(expected, newValue, memory_order_release)
    !expected
  }

  /** Possibly atomically sets the element at index {@code i} to {@code
   *  newValue} if the element's current value {@code == expectedValue}, with
   *  memory effects as specified by {@link VarHandle#weakCompareAndSet}.
   *
   *  @param i
   *    the index
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful
   *  @since 9
   */
  final def weakCompareAndSetVolatile(
      i: Int,
      expectedValue: Int,
      newValue: Int
  ): Boolean =
    nativeArray
      .at(i)
      .compareExchangeWeak(expectedValue, newValue)

  /** Possibly atomically sets the element at index {@code i} to {@code
   *  newValue} if the element's current value {@code == expectedValue}, with
   *  memory effects as specified by {@link VarHandle#weakCompareAndSetAcquire}.
   *
   *  @param i
   *    the index
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful
   *  @since 9
   */
  final def weakCompareAndSetAcquire(
      i: Int,
      expectedValue: Int,
      newValue: Int
  ): Boolean =
    nativeArray
      .at(i)
      .compareExchangeWeak(expectedValue, newValue, memory_order_acquire)

  /** Possibly atomically sets the element at index {@code i} to {@code
   *  newValue} if the element's current value {@code == expectedValue}, with
   *  memory effects as specified by {@link VarHandle#weakCompareAndSetRelease}.
   *
   *  @param i
   *    the index
   *  @param expectedValue
   *    the expected value
   *  @param newValue
   *    the new value
   *  @return
   *    {@code true} if successful
   *  @since 9
   */
  final def weakCompareAndSetRelease(
      i: Int,
      expectedValue: Int,
      newValue: Int
  ): Boolean =
    nativeArray
      .at(i)
      .compareExchangeWeak(expectedValue, newValue, memory_order_release)
}

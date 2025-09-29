/*
 * Based on JSR-166 originally written by Doug Lea with assistance
 * from members of JCP JSR-166 Expert Group and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic

import java.util.Arrays
import java.util.function.{BinaryOperator, UnaryOperator}

import scala.annotation.tailrec
import scala.language.implicitConversions

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.AtomicRef
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.runtime.ObjectArray
import scala.scalanative.unsafe._

class AtomicReferenceArray[E <: AnyRef] extends Serializable {

  private final var array: Array[E] = null

  @alwaysinline
  private[concurrent] def nativeArray: ObjectArray =
    array.asInstanceOf[ObjectArray]

  @alwaysinline
  private implicit def ptrRefToAtomicRef(ptr: Ptr[Object]): AtomicRef[E] =
    new AtomicRef[E](ptr.asInstanceOf[Ptr[E]])

  /** Creates a new AtomicReferenceArray of the given length, with all elements
   *  initially null.
   *
   *  @param length
   *    the length of the array
   */
  def this(length: Int) = {
    this()
    this.array = new Array[AnyRef](length).asInstanceOf[Array[E]]
  }

  /** Creates a new AtomicReferenceArray with the same length as, and all
   *  elements copied from, the given array.
   *
   *  @param array
   *    the array to copy elements from
   *  @throws java.lang.NullPointerException
   *    if array is null
   */
  def this(array: Array[E]) = {
    this()
    this.array = Arrays.copyOf[E](array, array.length)
  }

  /** Returns the length of the array.
   *
   *  @return
   *    the length of the array
   */
  final def length(): Int = array.length

  /** Returns the current value of the element at index {@code i}, with memory
   *  effects as specified by `VarHandle#getVolatile`.
   *
   *  @param i
   *    the index
   *  @return
   *    the current value
   */
  final def get(i: Int): E = nativeArray.at(i).load()

  /** Sets the element at index {@code i} to {@code newValue}, with memory
   *  effects as specified by `VarHandle#setVolatile`.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   */
  final def set(i: Int, newValue: E): Unit = {
    nativeArray.at(i).store(newValue)
  }

  /** Sets the element at index {@code i} to {@code newValue}, with memory
   *  effects as specified by `VarHandle#setRelease`.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   *  @since 1.6
   */
  final def lazySet(i: Int, newValue: E): Unit = {
    nativeArray.at(i).store(newValue, memory_order_release)
  }

  /** Atomically sets the element at index {@code i} to {@code newValue} and
   *  returns the old value, with memory effects as specified by
   *  `VarHandle#getAndSet`.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   *  @return
   *    the previous value
   */
  final def getAndSet(i: Int, newValue: E): E =
    nativeArray.at(i).exchange(newValue)

  /** Atomically sets the element at index {@code i} to {@code newValue} if the
   *  element's current value {@code == expectedValue}, with memory effects as
   *  specified by `VarHandle#compareAndSet`.
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
  final def compareAndSet(i: Int, expectedValue: E, newValue: E): Boolean =
    nativeArray.at(i).compareExchangeStrong(expectedValue, newValue)

  /** Possibly atomically sets the element at index {@code i} to {@code
   *  newValue} if the element's current value {@code == expectedValue}, with
   *  memory effects as specified by `VarHandle#weakCompareAndSetPlain`.
   *
   *  @deprecated
   *    This method has plain memory effects but the method name implies
   *    volatile memory effects (see methods such as {@link #compareAndExchange}
   *    and {@link #compareAndSet}). To avoid confusion over plain or volatile
   *    memory effects it is recommended that the method
   *    [[#weakCompareAndSetPlain]] be used instead.
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
  final def weakCompareAndSet(i: Int, expectedValue: E, newValue: E): Boolean =
    weakCompareAndSetPlain(i, expectedValue, newValue)

  /** Possibly atomically sets the element at index {@code i} to {@code
   *  newValue} if the element's current value {@code == expectedValue}, with
   *  memory effects as specified by `VarHandle#weakCompareAndSetPlain`.
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
      expectedValue: E,
      newValue: E
  ): Boolean =
    if (array(i) eq expectedValue) {
      array(i) = newValue
      true
    } else false

  /** Atomically updates (with memory effects as specified by
   *  `VarHandle#compareAndSet`) the element at index {@code i} with the results
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
  final def getAndUpdate(i: Int, updateFunction: UnaryOperator[E]): E = {
    @tailrec
    def loop(prev: E, next: E, haveNext: Boolean): E = {
      val newNext =
        if (!haveNext) updateFunction.apply(prev)
        else next

      if (weakCompareAndSetVolatile(i, prev, newNext)) prev
      else {
        val newPrev = get(i)
        loop(newPrev, newNext, prev eq newPrev)
      }
    }
    loop(get(i), null.asInstanceOf[E], false)
  }

  /** Atomically updates (with memory effects as specified by
   *  `VarHandle#compareAndSet`) the element at index {@code i} with the results
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
  final def updateAndGet(i: Int, updateFunction: UnaryOperator[E]): E = {
    @tailrec
    def loop(prev: E, next: E, haveNext: Boolean): E = {
      val newNext =
        if (!haveNext) updateFunction.apply(prev)
        else next

      if (weakCompareAndSetVolatile(i, prev, newNext)) newNext
      else {
        val newPrev = get(i)
        loop(newPrev, newNext, prev eq newPrev)
      }
    }
    loop(get(i), null.asInstanceOf[E], false)
  }

  /** Atomically updates (with memory effects as specified by
   *  `VarHandle#compareAndSet`) the element at index {@code i} with the results
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
      x: E,
      accumulatorFunction: BinaryOperator[E]
  ): E = {
    @tailrec
    def loop(prev: E, next: E, haveNext: Boolean): E = {
      val newNext =
        if (!haveNext) accumulatorFunction.apply(prev, x)
        else next

      if (weakCompareAndSetVolatile(i, prev, newNext)) prev
      else {
        val newPrev = get(i)
        loop(newPrev, newNext, prev eq newPrev)
      }
    }
    loop(get(i), null.asInstanceOf[E], false)
  }

  /** Atomically updates (with memory effects as specified by
   *  `VarHandle#compareAndSet`) the element at index {@code i} with the results
   *  of applying the given function to the current and given values, returning
   *  tnewNexthe updated value. The function should be side-effect-free, since
   *  it may be re-applied when attempted updates fail due to contention among
   *  threads. The function is applied with the current value of the element at
   *  index {@code i} as its first argument, and the given update as the second
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
      x: E,
      accumulatorFunction: BinaryOperator[E]
  ): E = {
    @tailrec
    def loop(prev: E, next: E, haveNext: Boolean): E = {
      val newNext =
        if (!haveNext) accumulatorFunction.apply(prev, x)
        else next

      if (weakCompareAndSetVolatile(i, prev, newNext)) newNext
      else {
        val newPrev = get(i)
        loop(newPrev, newNext, prev eq newPrev)
      }
    }
    loop(get(i), null.asInstanceOf[E], false)
  }

  /** Returns the String representation of the current values of array.
   *  @return
   *    the String representation of the current values of array
   */
  override def toString(): String = {
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
  final def getPlain(i: Int): E = {
    array(i)
  }

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
  final def setPlain(i: Int, newValue: E): Unit = {
    array(i) = newValue
  }

  /** Returns the current value of the element at index {@code i}, with memory
   *  effects as specified by `VarHandle#getOpaque`.
   *
   *  @param i
   *    the index
   *  @return
   *    the value
   *  @since 9
   */
  final def getOpaque(i: Int): E = nativeArray.at(i).load(memory_order_relaxed)

  /** Sets the element at index {@code i} to {@code newValue}, with memory
   *  effects as specified by `VarHandle#setOpaque`.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setOpaque(i: Int, newValue: E): Unit =
    nativeArray.at(i).store(newValue, memory_order_relaxed)

  /** Returns the current value of the element at index {@code i}, with memory
   *  effects as specified by `VarHandle#getAcquire`.
   *
   *  @param i
   *    the index
   *  @return
   *    the value
   *  @since 9
   */
  final def getAcquire(i: Int): E = nativeArray.at(i).load(memory_order_acquire)

  /** Sets the element at index {@code i} to {@code newValue}, with memory
   *  effects as specified by `VarHandle#setRelease`.
   *
   *  @param i
   *    the index
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setRelease(i: Int, newValue: E): Unit = {
    nativeArray.at(i).store(newValue, memory_order_release)
  }

  /** Atomically sets the element at index {@code i} to {@code newValue} if the
   *  element's current value, referred to as the <em>witness value</em>, {@code
   *  \== expectedValue}, with memory effects as specified by
   *  `VarHandle#compareAndExchange`.
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
  final def compareAndExchange(i: Int, expectedValue: E, newValue: E): E = {
    val expected = stackalloc[AnyRef]()
    !expected = expectedValue
    nativeArray
      .at(i)
      .compareExchangeStrong(expected.asInstanceOf[Ptr[E]], newValue)
    (!expected).asInstanceOf[E]
  }

  /** Atomically sets the element at index {@code i} to {@code newValue} if the
   *  element's current value, referred to as the <em>witness value</em>, {@code
   *  \== expectedValue}, with memory effects as specified by
   *  `VarHandle#compareAndExchangeAcquire`.
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
      expectedValue: E,
      newValue: E
  ): E = {
    val expected = stackalloc[AnyRef]()
    !expected = expectedValue
    nativeArray
      .at(i)
      .compareExchangeStrong(
        expected.asInstanceOf[Ptr[E]],
        newValue,
        memory_order_acquire
      )
    (!expected).asInstanceOf[E]
  }

  /** Atomically sets the element at index {@code i} to {@code newValue} if the
   *  element's current value, referred to as the <em>witness value</em>, {@code
   *  \== expectedValue}, with memory effects as specified by
   *  `VarHandle#compareAndExchangeRelease`.
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
      expectedValue: E,
      newValue: E
  ): E = {
    val expectedAny = stackalloc[AnyRef]()
    !expectedAny = expectedValue
    nativeArray
      .at(i)
      .compareExchangeStrong(
        expectedAny.asInstanceOf[Ptr[E]],
        newValue,
        memory_order_release
      )
    (!expectedAny).asInstanceOf[E]
  }

  /** Possibly atomically sets the element at index {@code i} to {@code
   *  newValue} if the element's current value {@code == expectedValue}, with
   *  memory effects as specified by `VarHandle#weakCompareAndSet`.
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
      expectedValue: E,
      newValue: E
  ): Boolean = {
    nativeArray
      .at(i)
      .compareExchangeWeak(expectedValue, newValue)
  }

  /** Possibly atomically sets the element at index {@code i} to {@code
   *  newValue} if the element's current value {@code == expectedValue}, with
   *  memory effects as specified by `VarHandle#weakCompareAndSetAcquire`.
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
      expectedValue: E,
      newValue: E
  ): Boolean = {
    val expected = stackalloc[AnyRef]()
    !expected = expectedValue
    nativeArray
      .at(i)
      .compareExchangeWeak(
        expected.asInstanceOf[Ptr[E]],
        newValue,
        memory_order_acquire
      )
  }

  /** Possibly atomically sets the element at index {@code i} to {@code
   *  newValue} if the element's current value {@code == expectedValue}, with
   *  memory effects as specified by `VarHandle#weakCompareAndSetRelease`.
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
      expectedValue: E,
      newValue: E
  ): Boolean = {
    val expected = stackalloc[Object]()
    !expected = expectedValue
    nativeArray
      .at(i)
      .compareExchangeWeak(
        expected.asInstanceOf[Ptr[E]],
        newValue,
        memory_order_release
      )
  }
}

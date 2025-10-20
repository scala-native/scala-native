/*
 * Based on JSR-166 originally written by Doug Lea with assistance
 * from members of JCP JSR-166 Expert Group and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic

import scala.annotation.tailrec
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.unsafe.*
import scala.scalanative.libc.stdatomic.AtomicRef
import scala.scalanative.libc.stdatomic.memory_order.*
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}
import java.util.function.{BinaryOperator, UnaryOperator}

@SerialVersionUID(-1848883965231344442L)
class AtomicReference[V <: AnyRef](@volatile private var value: V)
    extends Serializable {
  def this() = {
    this(null.asInstanceOf[V])
  }

  assert(valueRef.load() == value, "Value reference does not match field")

  // Pointer to field containing underlying V.
  @alwaysinline
  private[concurrent] def valueRef: AtomicRef[V] =
    new AtomicRef[V](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "value"))
    )

  /** Returns the current value, with memory effects as specified by
   *  `VarHandle#getVolatile`.
   *
   *  @return
   *    the current value
   */
  final def get(): V = value

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setVolatile`.
   *
   *  @param newValue
   *    the new value
   */
  final def set(newValue: V): Unit = value = newValue

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setRelease`.
   *
   *  @param newValue
   *    the new value
   *  @since 1.6
   */
  final def lazySet(newValue: V): Unit = {
    valueRef.store(newValue, memory_order_release)
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
  final def compareAndSet(expectedValue: V, newValue: V): Boolean =
    valueRef.compareExchangeStrong(expectedValue, newValue)

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
   *
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
  final def weakCompareAndSet(expectedValue: V, newValue: V): Boolean = {
    weakCompareAndSetPlain(expectedValue, newValue)
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
  final def weakCompareAndSetPlain(expectedValue: V, newValue: V): Boolean = {
    if (value eq expectedValue) {
      value = newValue
      true
    } else false
  }

  /** Atomically sets the value to {@code newValue} and returns the old value,
   *  with memory effects as specified by `VarHandle#getAndSet`.
   *
   *  @param newValue
   *    the new value
   *  @return
   *    the previous value
   */
  final def getAndSet(newValue: V): V = {
    valueRef.exchange(newValue)
  }

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
  final def getAndUpdate(updateFunction: UnaryOperator[V]): V = {
    @tailrec
    def loop(prev: V, next: V, haveNext: Boolean): V = {
      val newNext =
        if (!haveNext) updateFunction.apply(prev)
        else next
      if (weakCompareAndSetVolatile(prev, newNext)) prev
      else {
        val newPrev = get()
        loop(newPrev, newNext, prev == newPrev)
      }
    }
    loop(get(), null.asInstanceOf[V], false)
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
  final def updateAndGet(updateFunction: UnaryOperator[V]): V = {
    @tailrec
    def loop(prev: V, next: V, haveNext: Boolean): V = {
      val newNext =
        if (!haveNext) updateFunction.apply(prev)
        else next
      if (weakCompareAndSetVolatile(prev, newNext)) newNext
      else {
        val newPrev = get()
        loop(newPrev, newNext, prev == newPrev)
      }
    }
    loop(get(), null.asInstanceOf[V], false)
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
      x: V,
      accumulatorFunction: BinaryOperator[V]
  ): V = {
    @tailrec
    def loop(prev: V, next: V, hasNext: Boolean): V = {
      val newNext = if (hasNext) next else accumulatorFunction.apply(prev, x)
      if (weakCompareAndSetVolatile(prev, newNext)) prev
      else {
        val newPrev = get()
        loop(newPrev, newNext, prev == newPrev)
      }
    }
    loop(get(), null.asInstanceOf[V], false)
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
      x: V,
      accumulatorFunction: BinaryOperator[V]
  ): V = {
    @tailrec
    def loop(prev: V, next: Option[V]): V = {
      val newNext = next.getOrElse(accumulatorFunction.apply(prev, x))
      if (weakCompareAndSetVolatile(prev, newNext)) newNext
      else {
        val newPrev = get()
        loop(newPrev, if (newPrev eq prev) Some(newNext) else None)
      }
    }
    loop(get(), None)
  }

  /** Returns the String representation of the current value.
   *  @return
   *    the String representation of the current value
   */
  override def toString(): String = String.valueOf(get())

  /** Returns the current value, with memory semantics of reading as if the
   *  variable was declared non-{@code volatile}.
   *
   *  @return
   *    the value
   *  @since 9
   */
  final def getPlain(): V = value

  /** Sets the value to {@code newValue}, with memory semantics of setting as if
   *  the variable was declared non-{@code volatile} and non-{@code final}.
   *
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setPlain(newValue: V): Unit = {
    value = newValue
  }

  /** Returns the current value, with memory effects as specified by
   *  `VarHandle#getOpaque`.
   *
   *  @return
   *    the value
   *  @since 9
   */
  final def getOpaque(): V = valueRef.load(memory_order_relaxed)

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setOpaque`.
   *
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setOpaque(newValue: V): Unit =
    valueRef.store(newValue, memory_order_relaxed)

  /** Returns the current value, with memory effects as specified by
   *  `VarHandle#getAcquire`.
   *
   *  @return
   *    the value
   *  @since 9
   */
  final def getAcquire: V = {
    valueRef.load(memory_order_acquire)
  }

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setRelease`.
   *
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setRelease(newValue: V): Unit = {
    valueRef.store(newValue, memory_order_release)
  }

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
  final def compareAndExchange(expectedValue: V, newValue: V): V = {
    val expected = stackalloc[AnyRef]()
    !expected = expectedValue
    valueRef
      .compareExchangeStrong(
        expected.asInstanceOf[Ptr[V]],
        newValue
      )
    (!expected).asInstanceOf[V]
  }

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
  final def compareAndExchangeAcquire(expectedValue: V, newValue: V): V = {
    val expected = stackalloc[AnyRef]()
    !expected = expectedValue
    valueRef
      .compareExchangeStrong(
        expected.asInstanceOf[Ptr[V]],
        newValue,
        memory_order_acquire
      )
    (!expected).asInstanceOf[V]
  }

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
  final def compareAndExchangeRelease(expectedValue: V, newValue: V): V = {
    val expected = stackalloc[AnyRef]()
    !expected = expectedValue
    valueRef
      .compareExchangeStrong(
        expected.asInstanceOf[Ptr[V]],
        newValue,
        memory_order_release
      )
    (!expected).asInstanceOf[V]
  }

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
      expectedValue: V,
      newValue: V
  ): Boolean = {
    valueRef.compareExchangeWeak(expectedValue, newValue)
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
  final def weakCompareAndSetAcquire(expectedValue: V, newValue: V): Boolean = {
    val expected = stackalloc[AnyRef]()
    !expected = expectedValue
    valueRef
      .compareExchangeWeak(
        expected.asInstanceOf[Ptr[V]],
        newValue,
        memory_order_acquire
      )
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
  final def weakCompareAndSetRelease(expectedValue: V, newValue: V): Boolean = {
    val expected = stackalloc[AnyRef]()
    !expected = expectedValue
    valueRef
      .compareExchangeWeak(
        expected.asInstanceOf[Ptr[V]],
        newValue,
        memory_order_release
      )
  }
}

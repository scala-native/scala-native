/*
 * Based on JSR-166 originally written by Doug Lea with assistance
 * from members of JCP JSR-166 Expert Group and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic

import scala.language.implicitConversions
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.unsafe._
import scala.scalanative.libc.atomic.memory_order._
import scala.scalanative.libc.atomic.CAtomicByte
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}

@SerialVersionUID(4654671469794556979L)
class AtomicBoolean private (private var value: Byte) extends Serializable {

  // Pointer to field containing underlying Byte.
  @alwaysinline
  private[concurrent] def valueRef: CAtomicByte = new CAtomicByte(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "value"))
  )

  def this() = {
    this(0.toByte)
  }

  def this(initialValue: Boolean) = {
    this(if (initialValue) 1.toByte else 0.toByte)
  }

  private implicit def byteToBoolean(v: Byte): Boolean = v != 0
  private implicit def booleanToByte(v: scala.Boolean): Byte = if (v) 1 else 0

  /** Returns the current value, with memory effects of volatile read
   *
   *  @return
   *    the current value
   */
  final def get(): Boolean = value

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
  final def compareAndSet(
      expectedValue: Boolean,
      newValue: Boolean
  ): Boolean = {
    valueRef.compareExchangeStrong(expectedValue, newValue)
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
  def weakCompareAndSet(expectedValue: Boolean, newValue: Boolean): Boolean =
    valueRef.compareExchangeWeak(expectedValue, newValue)

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
  def weakCompareAndSetPlain(
      expectedValue: Boolean,
      newValue: Boolean
  ): Boolean = {
    if (byteToBoolean(value) == expectedValue) {
      value = newValue
      true
    } else false
  }

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setVolatile`.
   *
   *  @param newValue
   *    the new value
   */
  final def set(newValue: Boolean): Unit = {
    valueRef.store(newValue)
  }

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setRelease`.
   *
   *  @param newValue
   *    the new value
   *  @since 1.6
   */
  final def lazySet(newValue: Boolean): Unit = {
    valueRef.store(newValue, memory_order_release)
  }

  /** Atomically sets the value to {@code newValue} and returns the old value,
   *  with memory effects as specified by `VarHandle#getAndSet`.
   *
   *  @param newValue
   *    the new value
   *  @return
   *    the previous value
   */
  final def getAndSet(newValue: Boolean): Boolean = {
    valueRef.exchange(newValue)
  }

  /** Returns the String representation of the current value.
   *  @return
   *    the String representation of the current value
   */
  override def toString(): String = java.lang.Boolean.toString(get())

  /** Returns the current value, with memory semantics of reading as if the
   *  variable was declared non-{@code volatile}.
   *
   *  @return
   *    the value
   *  @since 9
   */
  final def getPlain(): Boolean = value

  /** Sets the value to {@code newValue}, with memory semantics of setting as if
   *  the variable was declared non-{@code volatile} and non-{@code final}.
   *
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setPlain(newValue: Boolean): Unit = {
    value = newValue
  }

  /** Returns the current value, with memory effects as specified by
   *  `VarHandle#getOpaque`.
   *
   *  @return
   *    the value
   *  @since 9
   */
  final def getOpaque: Boolean = {
    valueRef.load(memory_order_relaxed)
  }

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setOpaque`.
   *
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setOpaque(newValue: Boolean): Unit = {
    valueRef.store(newValue, memory_order_relaxed)
  }

  /** Returns the current value, with memory effects as specified by
   *  `VarHandle#getAcquire`.
   *
   *  @return
   *    the value
   *  @since 9
   */
  final def getAcquire: Boolean = {
    valueRef.load(memory_order_acquire)
  }

  /** Sets the value to {@code newValue}, with memory effects as specified by
   *  `VarHandle#setRelease`.
   *
   *  @param newValue
   *    the new value
   *  @since 9
   */
  final def setRelease(newValue: Boolean): Unit = {
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
  final def compareAndExchange(
      expectedValue: Boolean,
      newValue: Boolean
  ): Boolean = {
    val expected = stackalloc[Byte]()
    !expected = expectedValue.toByte
    valueRef.compareExchangeStrong(expected, newValue)
    !expected
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
  final def compareAndExchangeAcquire(
      expectedValue: Boolean,
      newValue: Boolean
  ): Boolean = {
    val expected = stackalloc[Byte]()
    !expected = expectedValue.toByte
    valueRef.compareExchangeStrong(expected, newValue, memory_order_acquire)
    !expected
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
  final def compareAndExchangeRelease(
      expectedValue: Boolean,
      newValue: Boolean
  ): Boolean = {
    val expected = stackalloc[Byte]()
    !expected = expectedValue.toByte
    valueRef.compareExchangeStrong(expected, newValue, memory_order_release)
    !expected
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
      expectedValue: Boolean,
      newValue: Boolean
  ): Boolean =
    valueRef.compareExchangeWeak(expectedValue, newValue)

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
      expectedValue: Boolean,
      newValue: Boolean
  ): Boolean =
    valueRef.compareExchangeWeak(expectedValue, newValue, memory_order_acquire)

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
      expectedValue: Boolean,
      newValue: Boolean
  ): Boolean =
    valueRef.compareExchangeWeak(expectedValue, newValue, memory_order_release)
}

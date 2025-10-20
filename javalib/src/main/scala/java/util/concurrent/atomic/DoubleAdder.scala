/* Ported from JSR 166 revision 1.23, dated: 2020-11-27
 *   https://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/jsr166/src/main/
 *     java/util/concurrent/atomic/DoubleAdder.java?revision=1.23&view=markup
 *
 * Serialization has not been implemted on Scala Native.
 *
 * The code style is neither idiomatic nor demo quality Scala.
 * Rather the code is written to hew closely, but not exactly, to the
 * JSR-166 Java original, Java idioms, a.k.a Scala warts, and all.
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic

import java.lang as jl

/** One or more variables that together maintain an initially zero
 *  {@code double} sum. When updates (method {@code add}) are contended across
 *  threads, the set of variables may grow dynamically to reduce contention.
 *  Method {@code sum} (or, equivalently {@code doubleValue}) returns the
 *  current total combined across the variables maintaining the sum. The order
 *  of accumulation within or across threads is not guaranteed. Thus, this class
 *  may not be applicable if numerical stability is required, especially when
 *  combining values of substantially different orders of magnitude.
 *
 *  <p>This class is usually preferable to alternatives when multiple threads
 *  update a common value that is used for purposes such as summary statistics
 *  that are frequently updated but less frequently read.
 *
 *  <p>This class extends {@code Number}, but does <em>not</em> define methods
 *  such as {@code equals}, {@code hashCode} and {@code compareTo} because
 *  instances are expected to be mutated, and so are not useful as collection
 *  keys.
 *
 *  @since 1.8
 *  @author
 *    Doug Lea
 */

class DoubleAdder() extends Striped64 {
  import Striped64.{Cell, getProbe}

  /*
   * Note that we must use "long" for underlying representations,
   * because there is no compareAndSet for double, due to the fact
   * that the bitwise equals used in any CAS implementation is not
   * the same as double-precision equals.  However, we use CAS only
   * to detect and alleviate contention, for which bitwise equals
   * works best anyway. In principle, the long/double conversions
   * used here should be essentially free on most platforms since
   * they just re-interpret bits.
   */

  /** Adds the given value.
   *
   *  @param x
   *    the value to add
   */
  def add(x: Double): Unit = {
    var cs: Array[Cell] = null
    var b: Long = 0
    var v: Long = 0
    var m: Int = 0
    var c: Cell = null

    if ({ cs = cells; cs != null } || !casBase(
          { b = base; b },
          jl.Double.doubleToRawLongBits(jl.Double.longBitsToDouble(b) + x)
        )) {

      val index = getProbe()
      var uncontended = true
      if ((cs == null) ||
          { m = cells.length - 1; m < 0 } ||
          { c = cells(index & m); c == null } ||
          {
            uncontended = c.cas(
              { v = c.value; v },
              jl.Double.doubleToRawLongBits(jl.Double.longBitsToDouble(v) + x)
            )
            !uncontended
          }) {
        doubleAccumulate(x, null, uncontended, index)
      }
    }
  }

  /** Returns the current sum. The returned value is <em>NOT</em> an atomic
   *  snapshot; invocation in the absence of concurrent updates returns an
   *  accurate result, but concurrent updates that occur while the sum is being
   *  calculated might not be incorporated. Also, because floating-point
   *  arithmetic is not strictly associative, the returned result need not be
   *  identical to the value that would be obtained in a sequential series of
   *  updates to a single variable.
   *
   *  @return
   *    the sum
   */
  def sum: Double = {
    val cs = cells
    var sum = jl.Double.longBitsToDouble(base)

    if (cs != null) for (c <- cs) {
      if (c != null)
        sum += jl.Double.longBitsToDouble(c.value)
    }
    sum
  }

  /** Resets variables maintaining the sum to zero. This method may be a useful
   *  alternative to creating a new adder, but is only effective if there are no
   *  concurrent updates. Because this method is intrinsically racy, it should
   *  only be used when it is known that no threads are concurrently updating.
   */
  def reset(): Unit = {
    val cs = cells

    base = 0L // relies on fact that double 0 must have same rep as long
    if (cs != null) for (c <- cs) {
      if (c != null) c.reset()
    }
  }

  /** Equivalent in effect to {@code sum} followed by {@code reset}. This method
   *  may apply for example during quiescent points between multithreaded
   *  computations.
   *
   *  If there are updates concurrent with this method, the computations. If
   *  there are updates concurrent with this method, the returned value is
   *  <em>not</em> guaranteed to be the final value occurring before the reset.
   *
   *  @return
   *    the sum
   */

  def sumThenReset: Double = {
    val cs = cells
    var sum = jl.Double.longBitsToDouble(getAndSetBase(0L))

    if (cs != null) for (c <- cs) {
      if (c != null)
        sum += jl.Double.longBitsToDouble(c.getAndSet(0L))
    }
    sum
  }

  /** Returns the String representation of the {@code sum}.
   *  @return
   *    the String representation of the {@code sum}
   */

  override def toString: String = sum.toString()

  /** Equivalent to {@code sum}.
   *
   *  @return
   *    the sum
   */

  override def doubleValue(): Double = sum.toDouble

  /** Returns the {@code sum} as a {@code long} after a narrowing primitive
   *  conversion.
   */

  def longValue(): Long = sum.toLong

  /** Returns the {@code sum} as an {@code int} after a narrowing primitive
   *  conversion.
   */

  override def intValue(): Int = sum.toInt

  /** Returns the {@code sum} as a {@code float} after a narrowing primitive
   *  conversion.
   */

  def floatValue(): Float = sum.toFloat
}

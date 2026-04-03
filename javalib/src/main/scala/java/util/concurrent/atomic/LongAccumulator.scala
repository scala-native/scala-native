/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 *  file: src/main/java/util/concurrent/atomic/LongAccumulator.java
 *  revision 1.38, dated: 2020-11-27
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic

import java.io.Serializable
import java.util.function.LongBinaryOperator

/* One or more variables that together maintain a running {@code long} value
 *  updated using a supplied function. When updates (method {@link #accumulate})
 *  are contended across threads, the set of variables may grow dynamically to
 *  reduce contention. Method {@link #get} (or, equivalently,
 *  {@link #longValue}) returns the current value across the variables
 *  maintaining updates.
 *
 *  <p>This class is usually preferable to {@link AtomicLong} when multiple
 *  threads update a common value that is used for purposes such as collecting
 *  statistics, not for fine-grained synchronization control. Under low update
 *  contention, the two classes have similar characteristics. But under high
 *  contention, expected throughput of this class is significantly higher, at
 *  the expense of higher space consumption.
 *
 *  <p>The order of accumulation within or across threads is not guaranteed and
 *  cannot be depended upon, so this class is only applicable to functions for
 *  which the order of accumulation does not matter. The supplied accumulator
 *  function should be side-effect-free, since it may be re-applied when
 *  attempted updates fail due to contention among threads. For predictable
 *  results, the accumulator function should be associative and commutative. The
 *  function is applied with an existing value (or identity) as one argument,
 *  and a given update as the other argument. For example, to maintain a running
 *  maximum value, you could supply {@code Long::max} along with
 *  {@code Long.MIN_VALUE} as the identity.
 *
 *  <p>Class {@link LongAdder} provides analogs of the functionality of this
 *  class for the common special case of maintaining counts and sums. The call
 *  {@code new LongAdder()} is equivalent to {@code new LongAccumulator((x, y)
 *  -> x + y, 0L)}.
 *
 *  <p>This class extends {@link Number}, but does <em>not</em> define methods
 *  such as {@code equals}, {@code hashCode} and {@code compareTo} because
 *  instances are expected to be mutated, and so are not useful as collection
 *  keys.
 *
 *  @since 1.8
 *  @author
 *    Doug Lea
 */

/* Creates a new instance using the given accumulator function and identity
 *  element.
 *  @param accumulatorFunction
 *    a side-effect-free function of two arguments
 *  @param identity
 *    identity (initial value) for the accumulator function
 */

/* Scala Native Devo Notes:
 *
 *  1) LongAccumulator extends Serializable so that the signature matches but
 *  Serialization & SerializationProxy are not implemented.
 *
 *  2) Converted javadoc "/** */" comments to simple "/* */" block comments
 *     since the former currently break SN doc generation. The original
 *     JSR-166 comments are invaluable to javalib maintainers.
 */
class LongAccumulator(
    accumulatorFunction: LongBinaryOperator,
    identity: scala.Long
) extends Striped64
    with Serializable {
  import Striped64.{Cell, getProbe}

  private val function = accumulatorFunction

  base = identity

  /* Updates with the given value.
   *
   *  @param x
   *    the value
   */
  def accumulate(x: scala.Long): Unit = {
    val cs: Array[Cell] = cells
    var b: scala.Long = base
    var r: scala.Long = function.applyAsLong(b, x)
    var m: scala.Int = -1
    var c: Cell = null

    if (cs != null
        || ((r != b) && !casBase(b, r))) {
      val index = getProbe()
      var uncontended = true
      if (cs == null
          || ({ m = cs.length - 1; m }) < 0
          || ({ c = cs(index & m); c }) == null
          || {
            val v: scala.Long = c.value
            r = function.applyAsLong(v, x)
            uncontended = !((r == v) || c.cas(v, r))
            uncontended
          })

        longAccumulate(x, function, uncontended, index)
    }
  }

  /* Returns the current value. The returned value is <em>NOT</em> an atomic
   *  snapshot; invocation in the absence of concurrent updates returns an
   *  accurate result, but concurrent updates that occur while the value is
   *  being calculated might not be incorporated.
   *
   *  @return
   *    the current value
   */
  def get(): scala.Long = {
    val cs: Array[Cell] = cells
    var result = base
    if (cs != null) {
      for (j <- 0 until cs.length) {
        val c = cs(j)
        if (c != null)
          result = function.applyAsLong(result, c.value)
      }
    }

    result
  }

  /* Resets variables maintaining updates to the identity value. This method
   *  may be a useful alternative to creating a new updater, but is only
   *  effective if there are no concurrent updates. Because this method is
   *  intrinsically racy, it should only be used when it is known that no
   *  threads are concurrently updating.
   */
  def reset(): Unit = {
    val cs: Array[Cell] = cells
    base = identity
    if (cs != null) {
      for (j <- 0 until cs.length) {
        val c = cs(j)
        if (c != null)
          c.reset(identity)
      }
    }
  }

  /* Equivalent in effect to {@link #get} followed by {@link #reset}. This
   *  method may apply for example during quiescent points between multithreaded
   *  computations. If there are updates concurrent with this method, the
   *  returned value is <em>not</em> guaranteed to be the final value occurring
   *  before the reset.
   *
   *  @return
   *    the value before reset
   */
  def getThenReset(): scala.Long = {
    val cs: Array[Cell] = cells
    var result = getAndSetBase(identity)

    if (cs != null) {
      for (j <- 0 until cs.length) {
        val c = cs(j)
        if (c != null) {
          val v = c.getAndSet(identity)
          result = function.applyAsLong(result, v)
        }
      }
    }

    result
  }

  /* Returns the String representation of the current value.
   *  @return
   *    the String representation of the current value
   */
  override def toString(): String =
    get().toString()

  /* Equivalent to {@link #get}.
   *
   *  @return
   *    the current value
   */
  def longValue(): scala.Long =
    get()

  /* Returns the {@linkplain #get current value} as an {@code int} after a
   *  narrowing primitive conversion.
   */
  def intValue(): scala.Int =
    get().toInt

  /* Returns the {@linkplain #get current value} as a {@code float} after a
   *  widening primitive conversion.
   */
  def floatValue(): scala.Float =
    get().toFloat

  /* Returns the {@linkplain #get current value} as a {@code double} after a
   *  widening primitive conversion.
   */
  def doubleValue(): scala.Double =
    get().toDouble
}

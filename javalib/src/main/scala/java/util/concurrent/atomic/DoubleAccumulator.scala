/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 *  file: src/main/java/util/concurrent/atomic/LongAccumulator.java
 *  revision 1.44, dated: 2020-11-27
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic

import java.io.Serializable
import java.lang.Double.{doubleToRawLongBits, longBitsToDouble}
import java.util.function.DoubleBinaryOperator;

/* One or more variables that together maintain a running {@code double} value
 *  updated using a supplied function. When updates (method {@link #accumulate})
 *  are contended across threads, the set of variables may grow dynamically to
 *  reduce contention. Method {@link #get} (or, equivalently,
 *  {@link #doubleValue}) returns the current value across the variables
 *  maintaining updates.
 *
 *  <p>This class is usually preferable to alternatives when multiple threads
 *  update a common value that is used for purposes such as summary statistics
 *  that are frequently updated but less frequently read.
 *
 *  <p>The supplied accumulator function should be side-effect-free, since it
 *  may be re-applied when attempted updates fail due to contention among
 *  threads. For predictable results, the accumulator function should be
 *  commutative and associative within the floating point tolerance required in
 *  usage contexts. The function is applied with an existing value (or identity)
 *  as one argument, and a given update as the other argument. For example, to
 *  maintain a running maximum value, you could supply {@code Double::max} along
 *  with {@code Double.NEGATIVE_INFINITY} as the identity. The order of
 *  accumulation within or across threads is not guaranteed. Thus, this class
 *  may not be applicable if numerical stability is required, especially when
 *  combining values of substantially different orders of magnitude.
 *
 *  <p>Class {@link DoubleAdder} provides analogs of the functionality of this
 *  class for the common special case of maintaining sums. The call
 *  {@code new DoubleAdder()} is equivalent to {@code new DoubleAccumulator((x,
 *  y) -> x + y, 0.0)}.
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
/* Scala Native Devo Notes:
 *
 *  1) DoubleAccumulator extends Serializable so that the signature matches but
 *  Serialization & SerializationProxy are not implemented.
 *
 *  2) Converted javadoc "/** */" comments to simple "/* */" block comments
 *     since the former currently break SN doc generation. The original
 *     JSR-166 comments are invaluable to javalib maintainers.
 */
class DoubleAccumulator(
    accumulatorFunction: DoubleBinaryOperator,
    _identity: scala.Double
) extends Striped64
    with Serializable {
  import Striped64.{Cell, getProbe}

  private val function = accumulatorFunction

  // use long representation
  val identity = doubleToRawLongBits(_identity)
  base = this.identity

  /* Updates with the given value.
   *
   *  @param x
   *    the value
   */
  def accumulate(x: scala.Double): Unit = {
    val cs: Array[Cell] = cells
    var b: scala.Long = base
    var r: scala.Long = doubleToRawLongBits(
      function.applyAsDouble(longBitsToDouble(b), x)
    )
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
            r = doubleToRawLongBits(
              function.applyAsDouble(longBitsToDouble(v), x)
            )
            uncontended = !((r == v) || c.cas(v, r))
            uncontended
          })

        doubleAccumulate(x, function, uncontended, index);
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
  def get(): scala.Double = {
    val cs: Array[Cell] = cells

    var result = longBitsToDouble(base)

    if (cs != null) {
      for (j <- 0 until cs.length) {
        val c = cs(j)
        if (c != null)
          result = function.applyAsDouble(result, longBitsToDouble(c.value))
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
  def getThenReset(): scala.Double = {
    val cs: Array[Cell] = cells
    var result = longBitsToDouble(getAndSetBase(identity))

    if (cs != null) {
      for (j <- 0 until cs.length) {
        val c = cs(j)
        if (c != null) {
          val v = longBitsToDouble(c.getAndSet(identity))
          result = function.applyAsDouble(result, v)
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
  def doubleValue(): scala.Double =
    get()

  /* Returns the {@linkplain #get current value} as a {@code long} after a
   *  narrowing primitive conversion.
   */
  def longValue(): scala.Long =
    get().toLong

  /* Returns the {@linkplain #get current value} as an {@code int} after a
   *  narrowing primitive conversion.
   */
  def intValue(): scala.Int =
    get().toInt

  /* Returns the {@linkplain #get current value} as a {@code float} after a
   *  narrowing primitive conversion.
   */
  def floatValue(): scala.Float =
    get().toFloat
}

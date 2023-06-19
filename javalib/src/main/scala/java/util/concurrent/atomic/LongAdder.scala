package java.util.concurrent.atomic

import java.io.Serializable

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/** One or more variables that together maintain an initially zero {@code long}
 *  sum. When updates (method {@link # add}) are contended across threads, the
 *  set of variables may grow dynamically to reduce contention. Method {@link #
 *  sum} (or, equivalently, {@link #longValue}) returns the current total
 *  combined across the variables maintaining the sum.
 *
 *  <p>This class is usually preferable to {@link AtomicLong} when multiple
 *  threads update a common sum that is used for purposes such as collecting
 *  statistics, not for fine-grained synchronization control. Under low update
 *  contention, the two classes have similar characteristics. But under high
 *  contention, expected throughput of this class is significantly higher, at
 *  the expense of higher space consumption.
 *
 *  <p>LongAdders can be used with a {@link
 *  java.util.concurrent.ConcurrentHashMap} to maintain a scalable frequency map
 *  (a form of histogram or multiset). For example, to add a count to a {@code
 *  ConcurrentHashMap<String,LongAdder> freqs}, initializing if not already
 *  present, you can use {@code freqs.computeIfAbsent(key, k -> new
 *  LongAdder()).increment();}
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
@SerialVersionUID(7249069246863182397L)
object LongAdder {

  /** Serialization proxy, used to avoid reference to the non-public Striped64
   *  superclass in serialized forms.
   *
   *  @serial
   *    include
   */
  @SerialVersionUID(7249069246863182397L)
  private class SerializationProxy private[atomic] (val a: LongAdder)
      extends Serializable {
    value = a.sum

    /** The current value returned by sum().
     *
     *  @serial
     */
    final private var value = 0L

    /** Returns a {@code LongAdder} object with initial state held by this
     *  proxy.
     *
     *  @return
     *    a {@code LongAdder} object with initial state held by this proxy
     */
    private def readResolve = {
      val a = new LongAdder
      a.base = value
      a
    }
  }
}

@SerialVersionUID(7249069246863182397L)
class LongAdder()

/** Creates a new adder with initial sum of zero.
 */
    extends Striped64
    with Serializable {

  /** Adds the given value.
   *
   *  @param x
   *    the value to add
   */
  def add(x: Long): Unit = {
    var cs = null
    var b = 0L
    var v = 0L
    var m = 0
    var c = null
    if ((cs = cells) != null || !casBase(b = base, b + x)) {
      val index = getProbe
      var uncontended = true
      if (cs == null || (m = cs.length - 1) < 0 || (c =
            cs(index & m)) == null || !(uncontended =
            c.cas(v = c.value, v + x)))
        longAccumulate(x, null, uncontended, index)
    }
  }

  /** Equivalent to {@code add(1)}.
   */
  def increment(): Unit = {
    add(1L)
  }

  /** Equivalent to {@code add(-1)}.
   */
  def decrement(): Unit = {
    add(-1L)
  }

  /** Returns the current sum. The returned value is <em>NOT</em> an atomic
   *  snapshot; invocation in the absence of concurrent updates returns an
   *  accurate result, but concurrent updates that occur while the sum is being
   *  calculated might not be incorporated.
   *
   *  @return
   *    the sum
   */
  def sum: Long = {
    val cs = cells
    var sum = base
    if (cs != null) for (c <- cs) {
      if (c != null) sum += c.value
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
    base = 0L
    if (cs != null) for (c <- cs) {
      if (c != null) c.reset()
    }
  }

  /** Equivalent in effect to {@link # sum} followed by {@link #reset}. This
   *  method may apply for example during quiescent points between multithreaded
   *  computations. If there are updates concurrent with this method, the
   *  returned value is <em>not</em> guaranteed to be the final value occurring
   *  before the reset.
   *
   *  @return
   *    the sum
   */
  def sumThenReset: Long = {
    val cs = cells
    var sum = getAndSetBase(0L)
    if (cs != null) for (c <- cs) {
      if (c != null) sum += c.getAndSet(0L)
    }
    sum
  }

  /** Returns the String representation of the {@link # sum}.
   *
   *  @return
   *    the String representation of the {@link # sum}
   */
  override def toString: String = Long.toString(sum)

  /** Equivalent to {@link # sum}.
   *
   *  @return
   *    the sum
   */
  override def longValue: Long = sum

  /** Returns the {@link # sum} as an {@code int} after a narrowing primitive
   *  conversion.
   */
  override def intValue: Int = sum.toInt

  /** Returns the {@link # sum} as a {@code float} after a widening primitive
   *  conversion.
   */
  override def floatValue: Float = sum.toFloat

  /** Returns the {@link # sum} as a {@code double} after a widening primitive
   *  conversion.
   */
  override def doubleValue: Double = sum.toDouble

  /** Returns a <a href="{@docRoot
   *  }/serialized-form.html#java.util.concurrent.atomic.LongAdder.SerializationProxy">
   *  SerializationProxy</a> representing the state of this instance.
   *
   *  @return
   *    a {@link SerializationProxy} representing the state of this instance
   */
  private def writeReplace = new LongAdder.SerializationProxy(this)

  /** @param s
   *    the stream
   *  @throws java.io.InvalidObjectException
   *    always
   */
  @throws[java.io.InvalidObjectException]
  private def readObject(s: ObjectInputStream): Unit = {
    throw new InvalidObjectException("Proxy required")
  }
}

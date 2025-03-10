package java.util

import java.{lang => jl}
import java.util.function.{DoubleConsumer, IntConsumer, LongConsumer}
import java.util.random.RandomGenerator
import java.util.stream.StreamSupport
import java.util.stream.{DoubleStream, IntStream, LongStream}

import scala.annotation.tailrec

/** Ported from Apache Harmony and described by Donald E. Knuth in The Art of
 *  Computer Programming, Volume 2: Seminumerical Algorithms, section 3.2.1.
 */
class Random(seed_in: Long)
    extends AnyRef
    with RandomGenerator
    with java.io.Serializable {

  private var seed: Long = calcSeed(seed_in)

  // see nextGaussian()
  private var nextNextGaussian: Double = _
  private var haveNextNextGaussian: Boolean = false

  private def calcSeed(seed_in: Long): Long =
    (seed_in ^ 0x5deece66dL) & ((1L << 48) - 1)

  def this() = {
    this(0) // ensure hashCode is set for this object
    seed = calcSeed(System.currentTimeMillis() + hashCode)
  }

  def setSeed(seed_in: Long): Unit = {
    seed = calcSeed(seed_in)
    haveNextNextGaussian = false
  }

  protected def next(bits: Int): Int = {
    seed = (seed * 0x5deece66dL + 0xbL) & ((1L << 48) - 1)
    (seed >>> (48 - bits)).toInt
  }

  def nextDouble(): Double =
    ((next(26).toLong << 27) + next(27)) / (1L << 53).toDouble

  def nextBoolean(): Boolean = next(1) != 0

  override def nextInt(): Int = next(32)

  override def nextInt(n: Int): Int = {
    if (n <= 0)
      throw new IllegalArgumentException("n must be positive")

    if ((n & -n) == n) // i.e., n is a power of 2
      ((n * next(31).toLong) >> 31).toInt
    else {
      @tailrec
      def loop(): Int = {
        val bits = next(31)
        val value = bits % n
        if (bits - value + (n - 1) < 0) loop()
        else value
      }

      loop()
    }
  }

  /* Implementation Note:
   *   The two private methods nextInt(origin, bound) and
   *   nextLong(origin, bound) use the algorithms documented by
   *   JDK 8.
   *
   *   The same basic algorithms are implemented in the JSR-166 code
   *   in the Scala Native code for java.util.concurrent.
   *
   *   This class is documented as requiring the capability of setting
   *   a "seed" value for the random number generator.  The JSR-166
   *   code does not allow that.  So these two methods can not delegate
   *   to the corresponding JSR-166 methods.  That would be too easy, by far.
   *
   *   Anyone interested in robust code will note that these methods
   *   use unbounded "while" loops. Those loops are in the original
   *   algorithms. Yes, including the JSR-166 code. Such unbounded
   *   loops can easily become what appear to be time consuming if not
   *   infinite loops.
   *
   *   TL; DR
   *      Each of the loops will terminate but have a worst case which can
   *      take substantial time.
   */

  /*   The int while() loop will terminate but has poor worst case performance.
   *   By the pigeon hole principle of discrete math, we know that the
   *   maximum number of iterations for an int is abs(jl.Integer.MIN_VALUE).
   *   A noticeable but finite pause.
   *
   *   The expected behavior is more acceptable. The probability of a
   *   "successful draw" for any one draw is
   *   (nInBounds/(abs(MIN_VALUE) + MAX_VALUE)). Call this probSuccess.
   *   probFailure is (1 - probSuccess).
   *   For a series of n draws, the probability of overall success is
   *   (1 - exp(probFailure), n)). For 10 draws, the probability of success
   *   is greater than 0.999.
   */

  // By convention, caller has checked that origin < bound

  private def nextInt(origin: Int, bound: Int): Int = {
    val n = bound - origin;
    if (n > 0) {
      nextInt(n) + origin
    } else { // range not representable as int. e.g. [-1, Integer.MAX_VALUE]
      var r = 0
      while ({ r = nextInt(); (r < origin || r >= bound); }) ()
      r
    }
  }

  /* See the comments above nextInt(origin, bound) above.
   * Also read the  "def internalNextLong(origin: Long, bound: Long)"
   * code in Scala Native java.util.concurrent.ThreadLocalRandom.scala
   * The loop code in the "reject over-represented" clause is adapted
   * from there. Same algorithm as Java code but already in Scala.
   */

  /*  The long while() loop will terminate but has poor worst case performance.
   *  It is more difficult to analyze. The JSR-166 algorithm
   *  documents that nextLong() might not return all possible long values.
   *
   *  With a range where "bound - origin" is 1 and that value is exactly one of
   *  the values which nextLong() could not return, the loop would not
   *  terminate.
   *
   *  However, the while() loop is called only when the range
   *  is larger than Long.MAX_VALUE. The probability of nextLong() not being
   *  able to return _any_ of those values is vanishing low.
   *  From there, the reasoning is similar to the ints case, but with
   *  larger values.
   *  In particular, the denominator for probSuccess is much larger, making
   *  the probability of success on any given draw smaller. That, in turn,
   *  requires a larger number of draws for a given overall probability of
   *  success.
   *
   *  The worst case could look like a "busy wait" non-terminating loop.
   */

  // By convention, caller has checked that origin < bound

  private def nextLong(origin: Long, bound: Long): Long = {
    var r = nextLong()
    val n = bound - origin
    val m = n - 1

    if ((n & m) == 0L) // power of two
      r = (r & m) + origin
    else if (n > 0L) { // reject over-represented candidates
      var u: Long = r >>> 1 // ensure nonnegative
      r = u % n
      while ((u + m - r) < 0L) // rejection check
        u = nextLong() >>> 1 // retry

      r += origin;
    } else { // range not representable as long. e.g. [-1, Long.MAX_VALUE]
      while (r < origin || r >= bound)
        r = nextLong()
    }

    r
  }

  def nextLong(): Long = (next(32).toLong << 32) + next(32)

  def nextFloat(): Float = next(24) / (1 << 24).toFloat

  def nextBytes(bytes: Array[Byte]): Unit = {
    var i = 0
    while (i < bytes.length) {
      var rnd = nextInt()
      var n = Math.min(bytes.length - i, 4)
      while (n > 0) {
        bytes(i) = rnd.toByte
        rnd >>= 8
        n -= 1
        i += 1
      }
    }
  }

  def nextGaussian(): Double = {
    /* The Box-Muller algorithm produces two random numbers at once. We save
     * the second one in `nextNextGaussian` to be used by the next call to
     * nextGaussian().
     *
     * See http://www.protonfish.com/jslib/boxmuller.shtml
     */
    if (haveNextNextGaussian) {
      haveNextNextGaussian = false
      return nextNextGaussian
    }

    var x, y, rds: Double = 0

    /* Get two random numbers from -1 to 1.
     * If the radius is zero or greater than 1, throw them out and pick two new
     * ones.
     * Rejection sampling throws away about 20% of the pairs.
     */
    while ({
      x = nextDouble() * 2 - 1
      y = nextDouble() * 2 - 1
      rds = x * x + y * y
      rds == 0 || rds > 1
    }) ()

    val c = Math.sqrt(-2 * Math.log(rds) / rds)

    // Save y*c for next time
    nextNextGaussian = y * c
    haveNextNextGaussian = true

    // And return x*c
    x * c
  }

  private val invalidStreamSizeMsg = "size must be non-negative"

  // The elements of the stream are random, not the Characteristics themselves.
  final val randomStreamCharacteristics =
    Spliterator.SIZED | Spliterator.SUBSIZED |
      Spliterator.NONNULL | Spliterator.IMMUTABLE //  0x4540, decimal 17728

  // Algorithm from JDK 17 Random Class documentation.
  private def nextDouble(origin: Double, bound: Double): Double = {
    val r = nextDouble() * (bound - origin) + origin

    if (r >= bound) Math.nextDown(bound) // correct for rounding
    else r
  }

  /* The same algorithm is used in the three Random*Spliterator methods,
   * specialized by type.  This algorithm is heavily influenced by the
   * public domain JSR-166 code in
   * java.util.concurrent.ThreadLocalRandom.scala and bears a debt of
   * gratitude to Doug Lea & Co.
   */

  final private class RandomDoublesSpliterator(
      var index: Long,
      fence: Long,
      origin: Double,
      bound: Double
  ) extends Spliterator.OfDouble {

    override def trySplit(): RandomDoublesSpliterator = {
      val m = (index + fence) >>> 1
      if (m <= index) null
      else {
        val i = index
        index = m
        new RandomDoublesSpliterator(i, m, origin, bound)
      }
    }

    override def estimateSize(): Long = fence - index
    override def characteristics(): Int = randomStreamCharacteristics

    override def tryAdvance(consumer: DoubleConsumer): Boolean = {
      if (consumer == null)
        throw new NullPointerException

      if (index >= fence) false
      else {
        consumer.accept(nextDouble(origin, bound))
        index += 1
        true
      }
    }
  }

  def doubles(): DoubleStream =
    doubles(jl.Long.MAX_VALUE)

  def doubles(
      randomNumberOrigin: Double,
      randomNumberBound: Double
  ): DoubleStream = {
    doubles(jl.Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)
  }

  def doubles(streamSize: Long): DoubleStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(invalidStreamSizeMsg)

    val spliter =
      new RandomDoublesSpliterator(0L, streamSize, 0.0, 1.0)

    StreamSupport.doubleStream(spliter, parallel = false)
  }

  def doubles(
      streamSize: Long,
      randomNumberOrigin: Double,
      randomNumberBound: Double
  ): DoubleStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(invalidStreamSizeMsg)

    if (!(randomNumberOrigin < randomNumberBound))
      throw new IllegalArgumentException("bound must be greater than origin")

    val spliter =
      new RandomDoublesSpliterator(
        0L,
        streamSize,
        randomNumberOrigin,
        randomNumberBound
      )

    StreamSupport.doubleStream(spliter, parallel = false)
  }

  final private class RandomIntsSpliterator(
      var index: Long,
      fence: Long,
      origin: Int,
      bound: Int
  ) extends Spliterator.OfInt {

    override def trySplit(): RandomIntsSpliterator = {
      val m = (index + fence) >>> 1
      if (m <= index) null
      else {
        val i = index
        index = m
        new RandomIntsSpliterator(i, m, origin, bound)
      }
    }

    override def estimateSize(): Long = fence - index
    override def characteristics(): Int = randomStreamCharacteristics

    override def tryAdvance(consumer: IntConsumer): Boolean = {
      if (consumer == null)
        throw new NullPointerException

      if (index >= fence) false
      else {
        consumer.accept(nextInt(origin, bound))
        index += 1
        true
      }
    }
  }

  def ints(): IntStream =
    ints(jl.Long.MAX_VALUE)

  def ints(
      randomNumberOrigin: Int,
      randomNumberBound: Int
  ): IntStream = {
    ints(jl.Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)
  }

  def ints(streamSize: Long): IntStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(invalidStreamSizeMsg)

    val spliter =
      new RandomIntsSpliterator(
        0L,
        streamSize,
        jl.Integer.MIN_VALUE,
        jl.Integer.MAX_VALUE
      )

    StreamSupport.intStream(spliter, parallel = false)
  }

  def ints(
      streamSize: Long,
      randomNumberOrigin: Int,
      randomNumberBound: Int
  ): IntStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(invalidStreamSizeMsg)

    if (!(randomNumberOrigin < randomNumberBound))
      throw new IllegalArgumentException("bound must be greater than origin")

    val spliter =
      new RandomIntsSpliterator(
        0L,
        streamSize,
        randomNumberOrigin,
        randomNumberBound
      )

    StreamSupport.intStream(spliter, parallel = false)
  }

  final private class RandomLongsSpliterator(
      var index: Long,
      fence: Long,
      origin: Long,
      bound: Long
  ) extends Spliterator.OfLong {

    override def trySplit(): RandomLongsSpliterator = {
      val m = (index + fence) >>> 1
      if (m <= index) null
      else {
        val i = index
        index = m
        new RandomLongsSpliterator(i, m, origin, bound)
      }
    }

    override def estimateSize(): Long = fence - index
    override def characteristics(): Int = randomStreamCharacteristics

    override def tryAdvance(consumer: LongConsumer): Boolean = {
      if (consumer == null)
        throw new NullPointerException

      if (index >= fence) false
      else {
        consumer.accept(nextLong(origin, bound))
        index += 1
        true
      }
    }
  }

  def longs(): LongStream =
    longs(jl.Long.MAX_VALUE)

  def longs(
      randomNumberOrigin: Long,
      randomNumberBound: Long
  ): LongStream = {
    longs(jl.Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)
  }

  def longs(streamSize: Long): LongStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(invalidStreamSizeMsg)

    val spliter =
      new RandomLongsSpliterator(
        0L,
        streamSize,
        jl.Long.MIN_VALUE,
        jl.Long.MAX_VALUE
      )

    StreamSupport.longStream(spliter, parallel = false)
  }

  def longs(
      streamSize: Long,
      randomNumberOrigin: Long,
      randomNumberBound: Long
  ): LongStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(invalidStreamSizeMsg)

    if (!(randomNumberOrigin < randomNumberBound))
      throw new IllegalArgumentException("bound must be greater than origin")

    val spliter =
      new RandomLongsSpliterator(
        0L,
        streamSize,
        randomNumberOrigin,
        randomNumberBound
      )

    StreamSupport.longStream(spliter, parallel = false)
  }

}

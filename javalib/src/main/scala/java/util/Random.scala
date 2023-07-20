package java.util

import java.{lang => jl}
import java.util.function.DoubleConsumer
import java.util.stream.{StreamSupport, DoubleStream}

import scala.annotation.tailrec

/** Ported from Apache Harmony and described by Donald E. Knuth in The Art of
 *  Computer Programming, Volume 2: Seminumerical Algorithms, section 3.2.1.
 */
class Random(seed_in: Long) extends AnyRef with java.io.Serializable {

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

  def nextInt(): Int = next(32)

  def nextInt(n: Int): Int = {
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

  // The elements of the stream are random, not the Characteristics themselves.
  final val randomStreamCharacteristics =
    Spliterator.SIZED | Spliterator.SUBSIZED |
      Spliterator.NONNULL | Spliterator.IMMUTABLE //  0x4540, decimal 17728

  def doubles(): DoubleStream = {

    val spliter =
      new Spliterators.AbstractDoubleSpliterator(
        jl.Long.MAX_VALUE,
        randomStreamCharacteristics
      ) {
        def tryAdvance(action: DoubleConsumer): Boolean = {
          action.accept(nextDouble())
          true
        }
      }

    StreamSupport.doubleStream(spliter, parallel = false)
  }

  def doubles(
      randomNumberOrigin: Double,
      randomNumberBound: Double
  ): DoubleStream = {
    doubles(jl.Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)
  }

  private val invalidStreamSizeMsg = "size must be non-negative"

  def doubles(streamSize: Long): DoubleStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(invalidStreamSizeMsg)

    val spliter =
      new Spliterators.AbstractDoubleSpliterator(
        streamSize,
        randomStreamCharacteristics
      ) {
        var nSeen = 0L

        def tryAdvance(action: DoubleConsumer): Boolean = {
          if (nSeen >= streamSize) false
          else {
            action.accept(nextDouble())
            nSeen += 1
            true
          }
        }
      }

    StreamSupport.doubleStream(spliter, parallel = false)

  }

  // Algorithm from JDK 17 Random Class documentation.
  private def nextDouble(origin: Double, bound: Double): Double = {
    val r = nextDouble() * (bound - origin) + origin

    if (r >= bound) Math.nextDown(bound) // correct for rounding
    else r
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
      new Spliterators.AbstractDoubleSpliterator(
        streamSize,
        randomStreamCharacteristics
      ) {
        var nSeen = 0L

        def tryAdvance(action: DoubleConsumer): Boolean = {
          if (nSeen >= streamSize) false
          else {
            action.accept(nextDouble(randomNumberOrigin, randomNumberBound))
            nSeen += 1
            true
          }
        }
      }

    StreamSupport.doubleStream(spliter, parallel = false)
  }

}

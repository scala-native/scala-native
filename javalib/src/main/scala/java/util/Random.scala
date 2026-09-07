package java.util

import java.util.random.{JuRandomFactory, RandomGenerator}
import java.util.stream._
import java.{lang => jl}

import scala.annotation.tailrec

/** Ported from Apache Harmony and described by Donald E. Knuth in The Art of
 *  Computer Programming, Volume 2: Seminumerical Algorithms, section 3.2.1.
 *
 *  Contains Scala Native modifications, see Scala Native Git Repository for
 *  audit trail.
 *
 *  In particular, implements Java 17 Java Enhancement Proposal (JEP) 356
 *  changes to inherit from java.util.random.RandomGenerator.
 */

/* A number of declarations from random.RandomGenerator are overriden
 * in order to preserve the historical sequence of values. RandomGenerator
 * uses different algorithms which return valid but different results.
 *
 * Knuth and the JVM both document that the series of results should be the
 * same when the RNG is initialized to the same seed.
 */

class Random(seed_in: Long)
    extends AnyRef
    with RandomGenerator
    with java.io.Serializable {

  override def isDeprecated(): Boolean =
    JuRandomFactory.isDeprecated()

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

  override def nextDouble(): Double =
    ((next(26).toLong << 27) + next(27)) / (1L << 53).toDouble

  override def nextBoolean(): Boolean = next(1) != 0

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

  override def nextLong(): Long = (next(32).toLong << 32) + next(32)

  override def nextFloat(): Float = next(24) / (1 << 24).toFloat

  override def nextBytes(bytes: Array[Byte]): Unit = {
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

  override def nextGaussian(): Double = {
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

}

object Random {

  private final class RandomFromGenerator(generator: RandomGenerator)
      extends Random {

    /* protected def next(bits: Int): Int
     * does not delegate to 'generator' because RandomGenerator does
     * not have that method. This is not a problem, since the the method
     * in this class can never be accessed outside of it.
     * The super Random method can never is protected and this class is
     * private final.
     */

    /* There must be a more elegant way of doing these declarations.
     */

    override def doubles(): DoubleStream =
      generator.doubles()

    override def doubles(
        randomNumberOrigin: scala.Double,
        randomNumberBound: scala.Double
    ): DoubleStream =
      generator.doubles(randomNumberOrigin, randomNumberBound)

    override def doubles(streamSize: Long): DoubleStream =
      generator.doubles(streamSize)

    override def doubles(
        streamSize: Long,
        randomNumberOrigin: Double,
        randomNumberBound: Double
    ): DoubleStream =
      generator.doubles(
        streamSize,
        randomNumberOrigin,
        randomNumberBound
      )

    override def equiDoubles(
        left: scala.Double,
        right: scala.Double,
        isLeftIncluded: Boolean,
        isRightIncluded: Boolean
    ): DoubleStream =
      generator.equiDoubles(left, right, isLeftIncluded, isRightIncluded)

    override def ints(): IntStream =
      generator.ints()

    override def ints(
        randomNumberOrigin: Int,
        randomNumberBound: Int
    ): IntStream =
      generator.ints(
        randomNumberOrigin,
        randomNumberBound
      )

    override def ints(streamSize: scala.Long): IntStream =
      generator.ints(streamSize)

    override def ints(
        streamSize: scala.Long,
        randomNumberOrigin: Int,
        randomNumberBound: Int
    ): IntStream =
      generator.ints(
        streamSize,
        randomNumberOrigin,
        randomNumberBound
      )

    override def isDeprecated() =
      generator.isDeprecated()

    override def longs(): LongStream =
      generator.longs(jl.Long.MAX_VALUE)

    override def longs(
        randomNumberOrigin: scala.Long,
        randomNumberBound: scala.Long
    ): LongStream =
      generator.longs(
        randomNumberOrigin,
        randomNumberBound
      )

    override def longs(streamSize: Long): LongStream =
      generator.longs(streamSize)

    override def longs(
        streamSize: Long,
        randomNumberOrigin: Long,
        randomNumberBound: Long
    ): LongStream =
      generator.longs(
        streamSize,
        randomNumberOrigin,
        randomNumberBound
      )

    override def nextBoolean(): Boolean =
      generator.nextBoolean()

    override def nextBytes(bytes: Array[Byte]): Unit =
      generator.nextBytes(bytes)

    override def nextDouble(): Double =
      generator.nextDouble()

    override def nextDouble(bound: scala.Double): scala.Double =
      generator.nextDouble(bound)

    override def nextDouble(
        origin: scala.Double,
        bound: scala.Double
    ): scala.Double =
      generator.nextDouble(origin, bound)

    override def nextExponential(): scala.Double =
      generator.nextExponential()

    override def nextFloat(): Float =
      generator.nextFloat()

    override def nextFloat(bound: scala.Float): scala.Float =
      generator.nextFloat(bound)

    override def nextFloat(
        origin: scala.Float,
        bound: scala.Float
    ): scala.Float =
      generator.nextFloat(origin, bound)

    override def nextGaussian(): Double =
      generator.nextGaussian()

    override def nextGaussian(
        mean: scala.Double,
        stddev: scala.Double
    ): scala.Double =
      generator.nextGaussian(mean, stddev)

    override def nextInt(): Int =
      generator.nextInt()

    override def nextInt(n: Int): Int =
      generator.nextInt(n)

    override def nextInt(origin: Int, bound: Int): Int =
      generator.nextInt(origin, bound)

    override def nextLong(): Long =
      generator.nextLong()

    override def nextLong(bound: scala.Long): scala.Long =
      generator.nextLong(bound)

    override def nextLong(origin: scala.Long, bound: scala.Long): scala.Long =
      generator.nextLong(origin, bound)

    override def setSeed(seed: Long): Unit =
      throw new java.lang.UnsupportedOperationException
  }

  /** Since: Java 19 */
  def from(generator: RandomGenerator): Random = {
    if (generator.isInstanceOf[Random]) generator.asInstanceOf[Random]
    else new RandomFromGenerator(generator)
  }
}

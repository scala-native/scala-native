package java.util

import java.util.random.{JuRandomFactory, RandomGenerator}

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

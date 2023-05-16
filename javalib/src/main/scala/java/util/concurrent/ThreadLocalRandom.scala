/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util._
import java.util.function._
import java.util.stream._
import java.util.concurrent.atomic._

@SerialVersionUID(-5851777807851030925L)
object ThreadLocalRandom {
  private def mix64(z0: Long) = {
    var z = z0
    z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL
    z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L
    z ^ (z >>> 33)
  }

  private def mix32(z0: Long) = {
    var z = z0
    z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL
    (((z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L) >>> 32).toInt
  }

  private[concurrent] def localInit(): Unit = {
    val p = probeGenerator.addAndGet(PROBE_INCREMENT)
    val probe =
      if (p == 0) 1
      else p // skip 0
    val seed = mix64(seeder.getAndAdd(SEEDER_INCREMENT))
    val t = Thread.currentThread()
    t.threadLocalRandomSeed = seed
    t.threadLocalRandomProbe = probe
  }

  def current(): ThreadLocalRandom = {
    if (Thread.currentThread().threadLocalRandomProbe == 0)
      localInit()
    instance
  }

  /** Spliterator for int streams. We multiplex the four int versions into one
   *  class by treating a bound less than origin as unbounded, and also by
   *  treating "infinite" as equivalent to Long.MAX_VALUE. For splits, it uses
   *  the standard divide-by-two approach. The long and double versions of this
   *  class are identical except for types.
   */
  final private class RandomIntsSpliterator(
      var index: Long,
      fence: Long,
      origin: Int,
      bound: Int
  ) extends Spliterator.OfInt {
    override def trySplit(): ThreadLocalRandom.RandomIntsSpliterator = {
      val i = index
      val m = (i + fence) >>> 1
      if (m <= i) null
      else {
        index = m
        new ThreadLocalRandom.RandomIntsSpliterator(i, m, origin, bound)
      }
    }

    override def estimateSize(): Long = fence - index

    override def characteristics(): Int = {
      Spliterator.SIZED |
        Spliterator.SUBSIZED |
        Spliterator.NONNULL |
        Spliterator.IMMUTABLE
    }

    override def tryAdvance(consumer: IntConsumer): Boolean = {
      if (consumer == null)
        throw new NullPointerException

      if (index < fence) {
        consumer.accept(
          ThreadLocalRandom.current().internalNextInt(origin, bound)
        )
        index += 1
        return true
      }
      false
    }

    override def forEachRemaining(consumer: IntConsumer): Unit = {
      if (consumer == null)
        throw new NullPointerException

      if (index < fence) {
        var i = index

        index = fence
        val rng = ThreadLocalRandom.current()

        while ({
          consumer.accept(rng.internalNextInt(origin, bound))
          i += 1
          i < fence
        }) ()
      }
    }
  }

  final private class RandomLongsSpliterator(
      var index: Long,
      fence: Long,
      origin: Long,
      bound: Long
  ) extends Spliterator.OfLong {

    override def trySplit(): ThreadLocalRandom.RandomLongsSpliterator = {
      val i = index
      val m = (i + fence) >>> 1
      if (m <= index) null
      else {
        index = m
        new ThreadLocalRandom.RandomLongsSpliterator(i, m, origin, bound)
      }
    }

    override def estimateSize(): Long = fence - index
    override def characteristics(): Int = {
      Spliterator.SIZED |
        Spliterator.SUBSIZED |
        Spliterator.NONNULL |
        Spliterator.IMMUTABLE
    }

    override def tryAdvance(consumer: LongConsumer): Boolean = {
      if (consumer == null)
        throw new NullPointerException

      if (index < fence) {
        consumer.accept(
          ThreadLocalRandom.current().internalNextLong(origin, bound)
        )
        index += 1
        return true
      }
      false
    }

    override def forEachRemaining(consumer: LongConsumer): Unit = {
      if (consumer == null)
        throw new NullPointerException

      if (index < fence) {
        val rng = ThreadLocalRandom.current()

        var i = index
        index = fence
        while ({
          consumer.accept(rng.internalNextLong(origin, bound))
          i += 1
          i < fence
        }) ()
      }
    }
  }

  final private class RandomDoublesSpliterator(
      var index: Long,
      fence: Long,
      origin: Double,
      bound: Double
  ) extends Spliterator.OfDouble {

    override def trySplit(): ThreadLocalRandom.RandomDoublesSpliterator = {
      val m = (index + fence) >>> 1
      if (m <= index) null
      else {
        val i = index
        index = m
        new ThreadLocalRandom.RandomDoublesSpliterator(i, m, origin, bound)
      }
    }
    override def estimateSize(): Long = fence - index
    override def characteristics(): Int = {
      Spliterator.SIZED |
        Spliterator.SUBSIZED |
        Spliterator.NONNULL |
        Spliterator.IMMUTABLE
    }
    override def tryAdvance(consumer: DoubleConsumer): Boolean = {
      if (consumer == null)
        throw new NullPointerException

      if (index < fence) {
        consumer.accept(
          ThreadLocalRandom.current().internalNextDouble()(origin, bound)
        )
        index += 1
        return true
      }
      false
    }
    override def forEachRemaining(consumer: DoubleConsumer): Unit = {
      if (consumer == null)
        throw new NullPointerException

      if (index < fence) {
        val rng = ThreadLocalRandom.current()
        var i = index
        index = fence
        while ({
          rng.internalNextDouble()(origin, bound)
          i += 1
          i < fence
        }) ()
      }
    }
  }

  private[concurrent] def getProbe(): Int =
    Thread.currentThread().threadLocalRandomProbe

  private[concurrent] def advanceProbe(probe0: Int) = {
    var probe = probe0
    probe ^= probe << 13 // xorshift
    probe ^= probe >>> 17
    probe ^= probe << 5
    Thread.currentThread().threadLocalRandomProbe = probe
    probe
  }

  private[concurrent] def nextSecondarySeed(): Int = {
    val t = Thread.currentThread()
    var r: Int = t.threadLocalRandomSecondarySeed
    if (r != 0) {
      r ^= r << 13
      r ^= r >>> 17
      r ^= r << 5
    } else {
      r = mix32(seeder.getAndAdd(SEEDER_INCREMENT))
      if (r == 0) r = 1 // avoid zero
    }
    // U.putInt(t, SECONDARY, r)
    t.threadLocalRandomSecondarySeed = r
    r
  }

  private[concurrent] def eraseThreadLocals(thread: Thread): Unit = {
    thread.threadLocals = null
    thread.inheritableThreadLocals = null
  }

  private val GAMMA = 0x9e3779b97f4a7c15L

  private val PROBE_INCREMENT = 0x9e3779b9
  private val SEEDER_INCREMENT = 0xbb67ae8584caa73bL

  private val DOUBLE_UNIT = 1.0 / (1L << 53)
  private val FLOAT_UNIT = 1.0f / (1 << 24)

  // IllegalArgumentException messages
  private[concurrent] val BAD_BOUND = "bound must be positive"
  private[concurrent] val BAD_RANGE = "bound must be greater than origin"
  private[concurrent] val BAD_SIZE = "size must be non-negative"

  private val nextLocalGaussian = new ThreadLocal[java.lang.Double]

  private val probeGenerator = new AtomicInteger

  private[concurrent] val instance = new ThreadLocalRandom

  private val seeder = new AtomicLong(
    mix64(System.currentTimeMillis()) ^ mix64(System.nanoTime())
  )
}

@SerialVersionUID(-5851777807851030925L)
class ThreadLocalRandom private () extends Random {

  private[concurrent] var initialized = true

  override def setSeed(seed: Long): Unit = { // only allow call from super() constructor
    if (initialized)
      throw new UnsupportedOperationException
  }
  final private[concurrent] def nextSeed(): Long = {
    val t = Thread.currentThread()
    t.threadLocalRandomSeed += ThreadLocalRandom.GAMMA // read and update per-thread seed
    t.threadLocalRandomSeed
  }

  override protected def next(bits: Int): Int = nextInt() >>> (32 - bits)

  final private[concurrent] def internalNextLong(origin: Long, bound: Long) = {
    var r = ThreadLocalRandom.mix64(nextSeed())
    if (origin < bound) {
      val n = bound - origin
      val m = n - 1
      if ((n & m) == 0L) { // power of two
        r = (r & m) + origin
      } else if (n > 0L) { // reject over-represented candidates
        var u = r >>> 1 // ensure nonnegative
        r = u % n
        while ((u + m - r) < 0L) { // rejection check
          // retry
          u = ThreadLocalRandom.mix64(nextSeed()) >>> 1
        }
        r += origin
      } else { // range not representable as long
        while ({ r < origin || r >= bound }) {
          r = ThreadLocalRandom.mix64(nextSeed())
        }
      }
    }
    r
  }

  final private[concurrent] def internalNextInt(origin: Int, bound: Int) = {
    var r = ThreadLocalRandom.mix32(nextSeed())
    if (origin < bound) {
      val n = bound - origin
      val m = n - 1
      if ((n & m) == 0) r = (r & m) + origin
      else if (n > 0) {
        var u = r >>> 1
        r = u % n
        while ((u + m - r) < 0)
          u = ThreadLocalRandom.mix32(nextSeed()) >>> 1
        r += origin
      } else
        while ({ r < origin || r >= bound }) {
          r = ThreadLocalRandom.mix32(nextSeed())
        }
    }
    r
  }

  final private[concurrent] def internalNextDouble()(
      origin: Double,
      bound: Double
  ) = {
    var r = (nextLong() >>> 11) * ThreadLocalRandom.DOUBLE_UNIT
    if (origin < bound) {
      r = r * (bound - origin) + origin
      if (r >= bound) { // correct for rounding
        r = java.lang.Double.longBitsToDouble(
          java.lang.Double.doubleToLongBits(bound) - 1
        )
      }
    }
    r
  }

  override def nextInt(): Int = ThreadLocalRandom.mix32(nextSeed())

  override def nextInt(bound: Int): Int = {
    if (bound <= 0)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_BOUND)
    var r = ThreadLocalRandom.mix32(nextSeed())
    val m = bound - 1
    if ((bound & m) == 0) // power of two
      r &= m
    else { // reject over-represented candidates
      var u = r >>> 1
      while ({
        r = u % bound
        (u + m - r) < 0
      }) {
        u = ThreadLocalRandom.mix32(nextSeed()) >>> 1
      }
    }
    assert(r < bound, s"r:$r < bound: $bound")
    r
  }

  def nextInt(origin: Int, bound: Int): Int = {
    if (origin >= bound)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_RANGE)
    internalNextInt(origin, bound)
  }

  override def nextLong(): Long = ThreadLocalRandom.mix64(nextSeed())

  def nextLong(bound: Long): Long = {
    if (bound <= 0)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_BOUND)
    var r = ThreadLocalRandom.mix64(nextSeed())
    val m = bound - 1
    if ((bound & m) == 0L) r &= m
    else {
      var u: Long = r >>> 1
      r = u % bound
      while ({
        r = u % bound
        (u + m - r) < 0L
      })
        u = ThreadLocalRandom.mix64(nextSeed()) >>> 1
    }
    r
  }

  def nextLong(origin: Long, bound: Long): Long = {
    if (origin >= bound)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_RANGE)
    internalNextLong(origin, bound)
  }

  override def nextDouble(): Double =
    (ThreadLocalRandom.mix64(nextSeed()) >>> 11) * ThreadLocalRandom.DOUBLE_UNIT

  def nextDouble(bound: Double): Double = {
    if (!(bound > 0.0))
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_BOUND)
    val result =
      (ThreadLocalRandom.mix64(
        nextSeed()
      ) >>> 11) * ThreadLocalRandom.DOUBLE_UNIT * bound
    if (result < bound) result
    else
      java.lang.Double
        .longBitsToDouble(java.lang.Double.doubleToLongBits(bound) - 1)
  }

  def nextDouble(origin: Double, bound: Double): Double = {
    if (!(origin < bound))
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_RANGE)
    internalNextDouble()(origin, bound)
  }

  override def nextBoolean(): Boolean = ThreadLocalRandom.mix32(nextSeed()) < 0

  override def nextFloat(): Float =
    (ThreadLocalRandom.mix32(nextSeed()) >>> 8) * ThreadLocalRandom.FLOAT_UNIT
  override def nextGaussian()
      : Double = { // Use nextLocalGaussian instead of nextGaussian field
    val d =
      ThreadLocalRandom.nextLocalGaussian.get().asInstanceOf[java.lang.Double]
    if (d != null) {
      ThreadLocalRandom.nextLocalGaussian.set(null.asInstanceOf[Double])
      return d.doubleValue()
    }
    var v1 = .0
    var v2 = .0
    var s = .0
    while ({
      v1 = 2 * nextDouble() - 1 // between -1 and 1

      v2 = 2 * nextDouble() - 1
      s = v1 * v1 + v2 * v2
      s >= 1 || s == 0
    }) ()

    val multiplier = Math.sqrt(-2 * Math.log(s) / s)
    ThreadLocalRandom.nextLocalGaussian.set(
      java.lang.Double.valueOf(v2 * multiplier).doubleValue()
    )
    v1 * multiplier
  }

  def ints(streamSize: Long): IntStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_SIZE)
    StreamSupport.intStream(
      new ThreadLocalRandom.RandomIntsSpliterator(
        0L,
        streamSize,
        Integer.MAX_VALUE,
        0
      ),
      false
    )
  }

  def ints(): IntStream =
    StreamSupport.intStream(
      new ThreadLocalRandom.RandomIntsSpliterator(
        0L,
        java.lang.Long.MAX_VALUE,
        Integer.MAX_VALUE,
        0
      ),
      false
    )

  def ints(
      streamSize: Long,
      randomNumberOrigin: Int,
      randomNumberBound: Int
  ): IntStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_SIZE)
    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_RANGE)
    StreamSupport.intStream(
      new ThreadLocalRandom.RandomIntsSpliterator(
        0L,
        streamSize,
        randomNumberOrigin,
        randomNumberBound
      ),
      false
    )
  }

  def ints(randomNumberOrigin: Int, randomNumberBound: Int): IntStream = {
    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_RANGE)
    StreamSupport.intStream(
      new ThreadLocalRandom.RandomIntsSpliterator(
        0L,
        java.lang.Long.MAX_VALUE,
        randomNumberOrigin,
        randomNumberBound
      ),
      false
    )
  }

  def longs(streamSize: Long): LongStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_SIZE)
    StreamSupport.longStream(
      new ThreadLocalRandom.RandomLongsSpliterator(
        0L,
        streamSize,
        java.lang.Long.MAX_VALUE,
        0L
      ),
      false
    )
  }

  def longs(): LongStream =
    StreamSupport.longStream(
      new ThreadLocalRandom.RandomLongsSpliterator(
        0L,
        java.lang.Long.MAX_VALUE,
        java.lang.Long.MAX_VALUE,
        0L
      ),
      false
    )

  def longs(
      streamSize: Long,
      randomNumberOrigin: Long,
      randomNumberBound: Long
  ): LongStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_SIZE)
    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_RANGE)
    StreamSupport.longStream(
      new ThreadLocalRandom.RandomLongsSpliterator(
        0L,
        streamSize,
        randomNumberOrigin,
        randomNumberBound
      ),
      false
    )
  }

  def longs(randomNumberOrigin: Long, randomNumberBound: Long): LongStream = {
    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_RANGE)
    StreamSupport.longStream(
      new ThreadLocalRandom.RandomLongsSpliterator(
        0L,
        java.lang.Long.MAX_VALUE,
        randomNumberOrigin,
        randomNumberBound
      ),
      false
    )
  }

  def doubles(streamSize: Long): DoubleStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_SIZE)
    StreamSupport.doubleStream(
      new ThreadLocalRandom.RandomDoublesSpliterator(
        0L,
        streamSize,
        java.lang.Double.MAX_VALUE,
        0.0
      ),
      false
    )
  }

  def doubles(): DoubleStream =
    StreamSupport.doubleStream(
      new ThreadLocalRandom.RandomDoublesSpliterator(
        0L,
        java.lang.Long.MAX_VALUE,
        java.lang.Double.MAX_VALUE,
        0.0
      ),
      false
    )

  def doubles(
      streamSize: Long,
      randomNumberOrigin: Double,
      randomNumberBound: Double
  ): DoubleStream = {
    if (streamSize < 0L)
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_SIZE)

    if (!(randomNumberOrigin < randomNumberBound))
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_RANGE)

    StreamSupport.doubleStream(
      new ThreadLocalRandom.RandomDoublesSpliterator(
        0L,
        streamSize,
        randomNumberOrigin,
        randomNumberBound
      ),
      false
    )
  }

  def doubles(
      randomNumberOrigin: Double,
      randomNumberBound: Double
  ): DoubleStream = {
    if (!(randomNumberOrigin < randomNumberBound))
      throw new IllegalArgumentException(ThreadLocalRandom.BAD_RANGE)
    StreamSupport.doubleStream(
      new ThreadLocalRandom.RandomDoublesSpliterator(
        0L,
        java.lang.Long.MAX_VALUE,
        randomNumberOrigin,
        randomNumberBound
      ),
      false
    )
  }

}

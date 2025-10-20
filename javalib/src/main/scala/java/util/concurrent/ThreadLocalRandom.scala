/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/* See SN Repository git history for Scala Native additions & changes.
 *
 * The Java 17 JEP356 work to extend RandomGenerator caused many changes.
 * Following the roughly equivalent Scala.js work, a large amount of
 * now redundant code has been removed. The previous Scala Native
 * Stream support is now provided by RandomGenerator default methods.
 * Simplifying the code makes it easier to discover the main intent.
 *
 * Some of the "private [concurrent]" values and methods are used
 * by Scala Native in other files, such as ForkJoinPool.
 *
 * Another pass or two of simplification could be done to reduce now
 * extraneous code.
 */

package java.util.concurrent

import java.util.*
import java.util.function.*
import java.util.concurrent.atomic.*

import scala.scalanative.annotation.safePublish
import scala.scalanative.meta.LinktimeInfo

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

  // IllegalArgumentException messages
  private[concurrent] val BAD_BOUND = "bound must be positive"
  private[concurrent] val BAD_RANGE = "bound must be greater than origin"
  private[concurrent] val BAD_SIZE = "size must be non-negative"

  private val nextLocalGaussian = new ThreadLocal[java.lang.Double]

  @safePublish
  private val probeGenerator = new AtomicInteger

  @safePublish
  private[concurrent] val instance = new ThreadLocalRandom

  private val seeder = new AtomicLong(
    mix64(System.currentTimeMillis()) ^ mix64(System.nanoTime())
  )
}

@SerialVersionUID(-5851777807851030925L)
class ThreadLocalRandom extends Random {

  private var initialized = true

  override def setSeed(seed: Long): Unit = {
    if (initialized)
      throw new UnsupportedOperationException

    super.setSeed(seed)
  }

  private final def nextSeed(): Long = {
    val t = Thread.currentThread()
    t.threadLocalRandomSeed +=
      ThreadLocalRandom.GAMMA // read and update per-thread seed
    t.threadLocalRandomSeed
  }

  /* Given the override of nextLong() below, there is no need to
   * override nextDouble(). The RandomGenerator default method produces
   * exactly the same bits.
   */

  override def nextInt(): Int = ThreadLocalRandom.mix32(nextSeed())

  override def nextLong(): Long = ThreadLocalRandom.mix64(nextSeed())

}

package scala.concurrent.forkjoin

import java.util.Random

class ThreadLocalRandom extends Random {

  import ThreadLocalRandom._

  private var rnd: Long = _

  var initialized: Boolean = true

  private var pad0, pad1, pad2, pad3, pad4, pad5, pad6, pad7: Long = _

  override def setSeed(seed: Long): Unit = {
    if (initialized)
      throw new UnsupportedOperationException
    rnd = (seed ^ multiplier) & mask
  }

  override protected def next(bits: Int): Int = {
    rnd = (rnd * multiplier + addend) & mask
    (rnd >>> (48 - bits)) toInt
  }

  def nextInt(least: Int, bound: Int): Int = {
    if (least >= bound)
      throw new IllegalArgumentException
    nextInt(bound - least) + least
  }

  def nextLong(l: Long): Long = {
    var n: Long = l
    if (n <= 0)
      throw new IllegalArgumentException
    // Divide n by two until small enough for nextInt. On each
    // iteration (at most 31 of them but usually much less),
    // randomly choose both whether to include high bit in result
    // (offset) and whether to continue with the lower vs upper
    // half (which makes a difference only if odd).
    var offset: Long = 0L
    while (n >= Integer.MAX_VALUE) {
      val bits: Int   = next(2)
      val half: Long  = n >>> 1
      val nextn: Long = if ((bits & 2) == 0) half else n - half
      if ((bits & 1) == 0)
        offset += n - nextn
      n = nextn
    }
    offset + nextInt(n toInt)
  }

  def nextLong(least: Long, bound: Long): Long = {
    if (least >= bound)
      throw new IllegalArgumentException
    nextLong(bound - least) + least
  }

  def nextDouble(n: Double): Double = {
    if (n <= 0)
      throw new IllegalArgumentException("n must be positive")
    nextDouble() * n
  }

  def nextDouble(least: Double, bound: Double): Double = {
    if (least >= bound)
      throw new IllegalArgumentException
    nextDouble() * (bound - least) + least
  }

}

object ThreadLocalRandom {

  private final val serialVersionUID: Long = -5851777807851030925L

  // same constants as Random, but must be redeclared because private
  private final val multiplier: Long = 0x5DEECE66DL
  private final val addend: Long     = 0xBL
  private final val mask: Long       = (1L << 48) - 1

  private final val localRandom: ThreadLocal[ThreadLocalRandom] =
    new ThreadLocal[ThreadLocalRandom] {
      override protected def initialValue(): ThreadLocalRandom =
        new ThreadLocalRandom
    }

  def current: ThreadLocalRandom = localRandom.get()
}

package java.util.random

trait RandomGenerator {

  def nextLong(): Long // Abstract

  // Incomplete implementation. Needs population.

  def nextInt(): Int =
    (nextLong() >> 32).toInt

  def nextInt(bound: Int): Int = {
    // Algorithm adapted from java.util.concurrent.ThreadLocalRandom.
    if (bound <= 0)
      throw new IllegalArgumentException("bound must be positive")

    var r = nextInt()
    val m = bound - 1

    if ((bound & m) == 0) { // power of two
      r &= m
    } else { // reject over-represented candidates
      var u = r >>> 1
      while ({
        r = u % bound
        (u + m - r) < 0
      }) {
        u = nextInt() >>> 1
      }
    }

    r
  }
}

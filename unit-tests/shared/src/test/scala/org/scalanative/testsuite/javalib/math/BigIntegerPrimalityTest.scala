package org.scalanative.testsuite.javalib.math

import java.math.BigInteger
import java.util.Random

import org.junit.Assert._
import org.junit.Test

class BigIntegerPrimalityTest {
  private val Two = BigInteger.valueOf(2)

  private def isPrimeByTrialDivision(n: Long): Boolean = {
    if (n < 2) false
    else if (n < 4) true
    else if (n % 2 == 0) false
    else {
      var d = 3L
      while (d * d <= n && n % d != 0) d += 2
      d * d > n
    }
  }

  // Miller-Rabin over fixed bases using only modPow/multiply/mod;
  // deterministic for n < 3.3 * 10^24, probabilistic above.
  private val Bases = Array(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43,
    47, 53, 59, 61, 67, 71)

  private def isPrime(n: BigInteger): Boolean = {
    if (n.bitLength() <= 62) isPrimeByTrialDivisionOrMR(n)
    else millerRabin(n)
  }

  private def isPrimeByTrialDivisionOrMR(n: BigInteger): Boolean = {
    val v = n.longValue()
    if (v < (1L << 40)) isPrimeByTrialDivision(v)
    else millerRabin(n)
  }

  private def millerRabin(n: BigInteger): Boolean = {
    if (!n.testBit(0)) return false
    val nMinus1 = n.subtract(BigInteger.ONE)
    val s = nMinus1.getLowestSetBit()
    val d = nMinus1.shiftRight(s)
    Bases.forall { b =>
      val a = BigInteger.valueOf(b)
      if (a.compareTo(nMinus1) >= 0) true
      else {
        var x = a.modPow(d, n)
        if (x == BigInteger.ONE || x == nMinus1) true
        else {
          var r = 1
          while (r < s && x != nMinus1) {
            x = x.multiply(x).mod(n)
            r += 1
          }
          x == nMinus1
        }
      }
    }
  }

  private def nextPrimeBruteForce(n: BigInteger): BigInteger = {
    var c = n.add(BigInteger.ONE)
    while (!isPrime(c)) c = c.add(BigInteger.ONE)
    c
  }

  @Test def probablePrimeReturnsPrimesOfExactBitLength(): Unit = {
    val rnd = new Random(5350L)
    val bitLengths = (2 to 70) ++ Seq(96, 127, 128, 129, 160, 256)
    for (bits <- bitLengths; _ <- 0 until 5) {
      val p = BigInteger.probablePrime(bits, rnd)
      assertEquals(s"bitLength of $p", bits, p.bitLength())
      assertTrue(s"$p ($bits bits) should be prime", isPrime(p))
      assertTrue(p.isProbablePrime(100))
    }
  }

  @Test def primeConstructorReturnsPrimesOfExactBitLength(): Unit = {
    val rnd = new Random(42L)
    for (bits <- Seq(11, 16, 31, 32, 33, 48, 64, 65, 100, 200);
        _ <- 0 until 5) {
      val p = new BigInteger(bits, 50, rnd)
      assertEquals(s"bitLength of $p", bits, p.bitLength())
      assertTrue(s"$p ($bits bits) should be prime", isPrime(p))
    }
  }

  @Test def isProbablePrimeMatchesTrialDivision(): Unit = {
    for (n <- (0L to 3000L) ++ (1065000L to 1066000L)) {
      assertEquals(
        s"isProbablePrime($n)",
        isPrimeByTrialDivision(n),
        BigInteger.valueOf(n).isProbablePrime(50)
      )
    }
  }

  @Test def isProbablePrimeKnownValues(): Unit = {
    val mersenne127 = BigInteger.ONE.shiftLeft(127).subtract(BigInteger.ONE)
    assertTrue(mersenne127.isProbablePrime(100))
    assertFalse(
      BigInteger.ONE.shiftLeft(128).add(BigInteger.ONE).isProbablePrime(100)
    )
    // 2^64 - 59 is the largest prime below 2^64
    val p64 = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.valueOf(59))
    assertTrue(p64.isProbablePrime(100))
    assertFalse(mersenne127.multiply(p64).isProbablePrime(100))
    // strong pseudoprime to bases 2, 3, 5 and 7
    assertFalse(BigInteger.valueOf(3215031751L).isProbablePrime(100))
  }

  @Test def nextProbablePrimeSmallRange(): Unit = {
    for (n <- 0L to 2100L) {
      val bn = BigInteger.valueOf(n)
      assertEquals(
        s"nextProbablePrime($n)",
        nextPrimeBruteForce(bn),
        bn.nextProbablePrime()
      )
    }
  }

  @Test def nextProbablePrimeBeyondSieveSquare(): Unit = {
    // above 1031^2 candidates surviving the sieve can be composite
    var n = BigInteger.valueOf(1064000L)
    val end = BigInteger.valueOf(1068000L)
    while (n.compareTo(end) < 0) {
      assertEquals(
        s"nextProbablePrime($n)",
        nextPrimeBruteForce(n),
        n.nextProbablePrime()
      )
      n = n.add(BigInteger.valueOf(7))
    }
  }

  @Test def nextProbablePrimeAcrossWordBoundaries(): Unit = {
    val starts = Seq(
      BigInteger.valueOf(Int.MaxValue).subtract(BigInteger.valueOf(30)),
      BigInteger.valueOf(Int.MaxValue),
      BigInteger.ONE.shiftLeft(32).subtract(BigInteger.valueOf(40)),
      BigInteger.valueOf(4294967291L), // largest prime below 2^32
      BigInteger.ONE.shiftLeft(32),
      BigInteger.valueOf(1000000000000L),
      BigInteger.ONE.shiftLeft(64).subtract(BigInteger.valueOf(59)),
      BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE),
      BigInteger.ONE.shiftLeft(127).subtract(BigInteger.ONE)
    )
    for (start <- starts) {
      var n = start
      for (_ <- 0 until 10) {
        val next = n.nextProbablePrime()
        assertEquals(s"nextProbablePrime($n)", nextPrimeBruteForce(n), next)
        n = next
      }
    }
  }

  @Test def nextProbablePrimeLargeValues(): Unit = {
    val rnd = new Random(1234L)
    for (bits <- Seq(100, 256, 512)) {
      val n = new BigInteger(bits, rnd).setBit(bits - 1)
      val next = n.nextProbablePrime()
      assertTrue(next.compareTo(n) > 0)
      assertTrue(s"$next should be prime", isPrime(next))
      var c = n.add(BigInteger.ONE)
      while (c.compareTo(next) < 0) {
        assertFalse(s"skipped prime $c", isPrime(c))
        c = c.add(BigInteger.ONE)
      }
    }
  }
}

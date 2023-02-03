/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 *
 * It also contains tests ported from Scala.js
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

import JSR166Test._
import org.scalanative.testsuite.utils.Platform._

import org.junit.{Test, Ignore}
import org.junit.Assert._

import scala.math.{max, min}

object ThreadLocalRandomTest {
  // max numbers of calls to detect getting stuck on one value
  /*
   * Testing coverage notes:
   *
   * We don't test randomness properties, but only that repeated
   * calls, up to NCALLS tries, produce at least one different
   * result.  For bounded versions, we sample various intervals
   * across multiples of primes.
   */
  val NCALLS = 10000
  // max sampled int bound
  val MAX_INT_BOUND: Int = 1 << 28
  // max sampled long bound
  val MAX_LONG_BOUND: Long = 1L << 42
  // Number of replications for other checks
  val REPS = 20
}
class ThreadLocalRandomTest extends JSR166Test {

  /** setSeed throws UnsupportedOperationException
   */
  @Test def testSetSeed(): Unit = {
    try {
      ThreadLocalRandom.current.setSeed(17)
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>

    }
  }

  /** Repeated calls to next (only accessible via reflection) produce at least
   *  two distinct results, and repeated calls produce all possible values.
   */
  @throws[ReflectiveOperationException]
  @Ignore("Test needs reflective access to 'next' method")
  @Test def testNext(): Unit = {}

  /** Repeated calls to nextInt produce at least two distinct results
   */
  @Test def testNextInt(): Unit = {
    val f = ThreadLocalRandom.current.nextInt
    var i = 0
    while ({
      i < ThreadLocalRandomTest.NCALLS && ThreadLocalRandom.current.nextInt == f
    }) i += 1
    assertTrue(i < ThreadLocalRandomTest.NCALLS)
  }

  /** Repeated calls to nextLong produce at least two distinct results
   */
  @Test def testNextLong(): Unit = {
    val f = ThreadLocalRandom.current.nextLong
    var i = 0
    while ({
      i < ThreadLocalRandomTest.NCALLS && ThreadLocalRandom.current.nextLong == f
    }) i += 1
    assertTrue(i < ThreadLocalRandomTest.NCALLS)
  }

  /** Repeated calls to nextBoolean produce at least two distinct results
   */
  @Test def testNextBoolean(): Unit = {
    val f = ThreadLocalRandom.current.nextBoolean
    var i = 0
    while ({
      i < ThreadLocalRandomTest.NCALLS && ThreadLocalRandom.current.nextBoolean == f
    }) i += 1
    assertTrue(i < ThreadLocalRandomTest.NCALLS)
  }

  /** Repeated calls to nextFloat produce at least two distinct results
   */
  @Test def testNextFloat(): Unit = {
    val f = ThreadLocalRandom.current.nextFloat
    var i = 0
    while ({
      i < ThreadLocalRandomTest.NCALLS && ThreadLocalRandom.current.nextFloat == f
    }) i += 1
    assertTrue(i < ThreadLocalRandomTest.NCALLS)
  }

  /** Repeated calls to nextDouble produce at least two distinct results
   */
  @Test def testNextDouble(): Unit = {
    val f = ThreadLocalRandom.current.nextDouble
    var i = 0
    while ({
      i < ThreadLocalRandomTest.NCALLS && ThreadLocalRandom.current.nextDouble == f
    }) i += 1
    assertTrue(i < ThreadLocalRandomTest.NCALLS)
  }

  /** Repeated calls to nextGaussian produce at least two distinct results
   */
  @Test def testNextGaussian(): Unit = {
    val f = ThreadLocalRandom.current.nextGaussian
    var i = 0
    while ({
      i < ThreadLocalRandomTest.NCALLS && ThreadLocalRandom.current.nextGaussian == f
    }) i += 1
    assertTrue(i < ThreadLocalRandomTest.NCALLS)
  }

  /** nextInt(non-positive) throws IllegalArgumentException
   */
  @Test def testNextIntBoundNonPositive(): Unit = {
    val rnd = ThreadLocalRandom.current
    for (bound <- Array[Int](0, -17, Integer.MIN_VALUE)) {
      try {
        rnd.nextInt(bound)
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
    }
  }

  /** nextInt(least >= bound) throws IllegalArgumentException
   */
  @Test def testNextIntBadBounds(): Unit = {
    val badBoundss = Array(
      Array(17, 2),
      Array(-42, -42),
      Array(Integer.MAX_VALUE, Integer.MIN_VALUE)
    )
    val rnd = ThreadLocalRandom.current
    for (badBounds <- badBoundss) {
      try {
        rnd.nextInt(badBounds(0), badBounds(1))
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
    }
  }

  /** nextInt(bound) returns 0 <= value < bound; repeated calls produce at least
   *  two distinct results
   */
  @Test def testNextIntBounded()
      : Unit = { // sample bound space across prime number increments
    var bound = 2
    while ({ bound < ThreadLocalRandomTest.MAX_INT_BOUND }) {
      val f = ThreadLocalRandom.current.nextInt(bound)
      assertTrue(0 <= f && f < bound)
      var i = 0
      var j = 0
      while (i < ThreadLocalRandomTest.NCALLS && {
            j = ThreadLocalRandom.current.nextInt(bound)
            j == f
          }) {
        assertTrue(0 <= j && j < bound)
        i += 1
      }
      assertTrue(i < ThreadLocalRandomTest.NCALLS)

      bound += 524959
    }
  }

  /** nextInt(least, bound) returns least <= value < bound; repeated calls
   *  produce at least two distinct results
   */
  @Test def testNextIntBounded2(): Unit = {
    var least = -15485863
    while ({ least < ThreadLocalRandomTest.MAX_INT_BOUND }) {
      var bound = least + 2
      while ({ bound > least && bound < ThreadLocalRandomTest.MAX_INT_BOUND }) {
        val f = ThreadLocalRandom.current.nextInt(least, bound)
        assertTrue(least <= f && f < bound)
        var i = 0
        var j = 0
        while (i < ThreadLocalRandomTest.NCALLS && {
              j = ThreadLocalRandom.current.nextInt(least, bound)
              j == f
            }) {
          assertTrue(least <= j && j < bound)
          i += 1
        }
        assertTrue(i < ThreadLocalRandomTest.NCALLS)

        bound += 49979687
      }

      least += 524959
    }
  }

  /** nextLong(non-positive) throws IllegalArgumentException
   */
  @Test def testNextLongBoundNonPositive(): Unit = {
    val rnd = ThreadLocalRandom.current
    for (bound <- Array[Long](0L, -17L, java.lang.Long.MIN_VALUE)) {
      try {
        rnd.nextLong(bound)
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
    }
  }

  /** nextLong(least >= bound) throws IllegalArgumentException
   */
  @Test def testNextLongBadBounds(): Unit = {
    val badBoundss = Array(
      Array(17L, 2L),
      Array(-42L, -42L),
      Array(java.lang.Long.MAX_VALUE, java.lang.Long.MIN_VALUE)
    )
    val rnd = ThreadLocalRandom.current
    for (badBounds <- badBoundss) {
      try {
        rnd.nextLong(badBounds(0), badBounds(1))
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
    }
  }

  /** nextLong(bound) returns 0 <= value < bound; repeated calls produce at
   *  least two distinct results
   */
  @Test def testNextLongBounded(): Unit = {
    var bound = 2L
    while (bound < ThreadLocalRandomTest.MAX_LONG_BOUND) {
      val f = ThreadLocalRandom.current.nextLong(bound)
      assertTrue(0 <= f && f < bound)
      var i = 0
      var j = 0L
      while (i < ThreadLocalRandomTest.NCALLS && {
            j = ThreadLocalRandom.current.nextLong(bound)
            j == f
          }) {
        assertTrue(0 <= j && j < bound)
        i += 1
      }
      assertTrue(i < ThreadLocalRandomTest.NCALLS)

      bound += 15485863
    }
  }

  /** nextLong(least, bound) returns least <= value < bound; repeated calls
   *  produce at least two distinct results
   */
  @Test def testNextLongBounded2(): Unit = {
    var least: Long = -86028121
    while (least < ThreadLocalRandomTest.MAX_LONG_BOUND) {
      var bound = least + 2
      while (bound > least && bound < ThreadLocalRandomTest.MAX_LONG_BOUND) {
        val f = ThreadLocalRandom.current.nextLong(least, bound)
        assertTrue(least <= f && f < bound)
        var i = 0
        var j = 0L
        while (i < ThreadLocalRandomTest.NCALLS && {
              j = ThreadLocalRandom.current.nextLong(least, bound)
              j == f
            }) {
          assertTrue(least <= j && j < bound)
          i += 1
        }
        assertTrue(i < ThreadLocalRandomTest.NCALLS)

        bound += Math.abs(bound * 7919)
      }

      least += 982451653L
    }
  }

  /** nextDouble(non-positive) throws IllegalArgumentException
   */
  @Test def testNextDoubleBoundNonPositive(): Unit = {
    val rnd = ThreadLocalRandom.current
    val badBounds = Array(
      0.0d,
      -17.0d,
      -java.lang.Double.MIN_VALUE,
      java.lang.Double.NEGATIVE_INFINITY,
      java.lang.Double.NaN
    )
    for (bound <- badBounds) {
      try {
        rnd.nextDouble(bound)
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
    }
  }

  /** nextDouble(least, bound) returns least <= value < bound; repeated calls
   *  produce at least two distinct results
   */
  @Test def testNextDoubleBounded2(): Unit = {
    var least = 0.0001
    while ({ least < 1.0e20 }) {
      var bound = least * 1.001
      while ({ bound < 1.0e20 }) {
        val f = ThreadLocalRandom.current.nextDouble(least, bound)
        assertTrue(least <= f && f < bound)
        var i = 0
        var j = .0
        while (i < ThreadLocalRandomTest.NCALLS && {
              j = ThreadLocalRandom.current.nextDouble(least, bound)
              j == f
            }) {
          assertTrue(least <= j && j < bound)
          i += 1
        }
        assertTrue(i < ThreadLocalRandomTest.NCALLS)

        bound *= 16
      }

      least *= 8
    }
  }

  /** Different threads produce different pseudo-random sequences
   */
  @Test def testDifferentSequences()
      : Unit = { // Don't use main thread's ThreadLocalRandom - it is likely to
    // be polluted by previous tests.
    val threadLocalRandom =
      new AtomicReference[ThreadLocalRandom]
    val rand = new AtomicLong
    var firstRand = 0L
    var firstThreadLocalRandom: ThreadLocalRandom = null
    val getRandomState = new CheckedRunnable() {
      override def realRun(): Unit = {
        val current = ThreadLocalRandom.current
        assertSame(current, ThreadLocalRandom.current)
        // test bug: the following is not guaranteed and not true in JDK8
        //                assertNotSame(current, threadLocalRandom.get());
        rand.set(current.nextLong)
        threadLocalRandom.set(current)
      }
    }
    val first = newStartedThread(getRandomState)
    awaitTermination(first)
    firstRand = rand.get
    firstThreadLocalRandom = threadLocalRandom.get
    var i = 0
    while (i < ThreadLocalRandomTest.NCALLS) {
      val t = newStartedThread(getRandomState)
      awaitTermination(t)
      if (firstRand != rand.get) return
      i += 1
    }
    fail("all threads generate the same pseudo-random sequence")
  }

  /** Repeated calls to nextBytes produce at least values of different signs for
   *  every byte
   */
  @Test def testNextBytes(): Unit = {
    import scala.util.control.Breaks._
    val rnd = ThreadLocalRandom.current
    val n = rnd.nextInt(1, 20)
    val bytes = new Array[Byte](n)
    breakable {
      for (i <- 0 until n) {
        var tries = ThreadLocalRandomTest.NCALLS
        while ({ { tries -= 1; tries + 1 } > 0 }) {
          val before = bytes(i)
          rnd.nextBytes(bytes)
          val after = bytes(i)
          if (after * before < 0) break()
        }
        fail("not enough variation in random bytes")
      }
    }
  }

  /** Filling an empty array with random bytes succeeds without effect.
   */
  @Test def testNextBytes_emptyArray(): Unit = {
    ThreadLocalRandom.current.nextBytes(new Array[Byte](0))
  }
  @Test def testNextBytes_nullArray(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      () => ThreadLocalRandom.current.nextBytes(null)
    )
  }

  // Tests ported from Scala.js commit: bbf0314 dated: Mon, 13 Jun 2022
  @Test def setSeedThrows(): Unit = {
    val tlr = ThreadLocalRandom.current()

    assertThrows(classOf[UnsupportedOperationException], () => tlr.setSeed(1))
  }

  def checkIntBounds(b1: Int, b2: Int)(implicit
      tlr: ThreadLocalRandom
  ): Unit = {
    val least = min(b1, b2)
    val bound = max(b1, b2)

    val next = tlr.nextInt(least, bound)
    assertTrue((next >= least) && (next < bound))
  }

  @Test def nextIntIntInt(): Unit = {
    implicit val tlr = ThreadLocalRandom.current()

    checkIntBounds(Int.MinValue, Int.MaxValue)
    checkIntBounds(Int.MinValue + 1, 0)
    checkIntBounds(Int.MaxValue, 0)
    checkIntBounds(200669844, -1811735300)
    checkIntBounds(876754740, -1860444935)
    checkIntBounds(-1253039209, 1615444321)
    checkIntBounds(-2046491282, 884358868)
    checkIntBounds(230412412, -1250818247)
    checkIntBounds(1328421012, 366374199)
    checkIntBounds(-1846600801, 1097254116)
    checkIntBounds(-1524411227, -585917314)
    checkIntBounds(-892995854, 669219125)
    checkIntBounds(-1869354815, 468973375)
    checkIntBounds(-1070373088, 1803352529)
    checkIntBounds(473495784, 640351934)
    checkIntBounds(107531509, 863732412)
    checkIntBounds(407937031, 611909285)
    checkIntBounds(1256055036, 931541808)
    checkIntBounds(-264729035, -798914572)
    checkIntBounds(610944361, -1983315023)
    checkIntBounds(169723705, 819603253)
    checkIntBounds(1900794425, -1321498275)
    checkIntBounds(1946895695, 1953614324)
    checkIntBounds(-1107099753, 1228937864)
    checkIntBounds(-436632533, 1753515886)
    checkIntBounds(-1432284543, -1086838648)
    checkIntBounds(1780299838, -971587448)
    checkIntBounds(-1883639893, -215751988)
    checkIntBounds(-606882249, -2027042046)
    checkIntBounds(1793439907, 1932556083)
    checkIntBounds(913297100, 304847852)
    checkIntBounds(1792841525, 1417249690)
    checkIntBounds(-1206771015, 1461069144)
    checkIntBounds(-17212656, -1300788041)
    checkIntBounds(-974900472, 67787600)
    checkIntBounds(-1416245847, 467570213)
    checkIntBounds(1723707795, -173665270)
    checkIntBounds(-830611361, 1951201215)
    checkIntBounds(-206580281, -1389152422)
    checkIntBounds(317003999, 2002562096)
    checkIntBounds(862632362, 1142026599)
    checkIntBounds(1427890121, 1219286218)
    checkIntBounds(-1574108386, 1636228257)
    checkIntBounds(-906455661, -1634427241)
    checkIntBounds(-600941210, -1326622990)
    checkIntBounds(784503213, -1214499667)
    checkIntBounds(1887012585, 966620723)
    checkIntBounds(-1028952090, -1629844538)
    checkIntBounds(1177745206, 2060996577)
    checkIntBounds(-1530572787, 1311494927)
    checkIntBounds(-225091256, -201029616)
    checkIntBounds(-1624577061, 404594240)
    checkIntBounds(582850058, -1481614433)
    checkIntBounds(1140369168, -609542932)
    checkIntBounds(-1779201251, 2104334764)
    checkIntBounds(-922485285, -625675495)
    checkIntBounds(464947671, 787431498)
    checkIntBounds(640742782, 1992656659)
    checkIntBounds(-391198065, -1625837455)
    checkIntBounds(1713074993, 2137774205)
    checkIntBounds(788987927, 1092726069)
    checkIntBounds(-1010524857, 1602499752)
    checkIntBounds(-841705591, 838703675)
    checkIntBounds(1750248079, 610753575)
    checkIntBounds(-1201819578, 698330472)
    checkIntBounds(1408484348, -1200755294)
    checkIntBounds(1165496379, -1131214886)
    checkIntBounds(182115464, 1925130730)
    checkIntBounds(1227659366, 49343003)
    checkIntBounds(-44588204, 1581213006)
    checkIntBounds(-746652264, -1877313645)
    checkIntBounds(-1367804909, -236733908)
    checkIntBounds(-688797316, 1502002495)
    checkIntBounds(1505454505, -621424438)
    checkIntBounds(1012590551, 1373499296)
    checkIntBounds(742127374, 1999360102)
    checkIntBounds(-132299759, -474606603)
    checkIntBounds(453028472, -1910125173)
    checkIntBounds(1126185715, 1540655275)
    checkIntBounds(1684537017, 824396197)
    checkIntBounds(-534387535, -1457839852)
    checkIntBounds(-252616987, -1445423144)
    checkIntBounds(1353546539, -2021734313)
    checkIntBounds(93831223, 1735736076)
    checkIntBounds(-1952489361, 1322311591)
    checkIntBounds(706836020, -1872129716)
    checkIntBounds(1876199810, -989606985)
    checkIntBounds(1180083473, -1987354544)
    checkIntBounds(358830432, -1054448275)
    checkIntBounds(-331221423, 1964906328)
    checkIntBounds(-692586432, 1473855957)
    checkIntBounds(-1850379342, -1891837382)
    checkIntBounds(2115982107, 515638616)
    checkIntBounds(1250405449, -562976322)
    checkIntBounds(1238265711, -1316997587)
    checkIntBounds(-174356501, 2506025)
    checkIntBounds(1205481279, -1674427890)
    checkIntBounds(-217617201, -833593065)
    checkIntBounds(20848991, -1440699601)
    checkIntBounds(2010553201, 797241229)
    checkIntBounds(658643437, 315920491)
    checkIntBounds(-1507203912, -507923122)

    assertThrows(classOf[IllegalArgumentException], () => tlr.nextInt(2, 1))
    assertThrows(classOf[IllegalArgumentException], () => tlr.nextInt(1, 1))
  }

  def checkLongUpperBound(
      bound: Long
  )(implicit tlr: ThreadLocalRandom): Unit = {
    val next = tlr.nextLong(bound)
    assertTrue(next < bound)
  }

  @Test def nextLongLessThanBound(): Unit = {
    implicit val tlr = ThreadLocalRandom.current()

    checkLongUpperBound(Long.MaxValue)
    checkLongUpperBound(5885960878454149260L)
    checkLongUpperBound(3528483944557011070L)
    checkLongUpperBound(5484180277171382326L)
    checkLongUpperBound(1490599099190018502L)
    checkLongUpperBound(3724760864513005121L)
    checkLongUpperBound(1172568958686779677L)
    checkLongUpperBound(8897848747790774453L)
    checkLongUpperBound(2396404752488550104L)
    checkLongUpperBound(5834511226585292361L)
    checkLongUpperBound(3076738620588564168L)
    checkLongUpperBound(8131404710222798692L)
    checkLongUpperBound(5370840994636935207L)
    checkLongUpperBound(162174391769041403L)
    checkLongUpperBound(4418960713477816452L)
    checkLongUpperBound(3861432956028599070L)
    checkLongUpperBound(4459354002462522558L)
    checkLongUpperBound(8117366326929626927L)
    checkLongUpperBound(8673067706081895585L)
    checkLongUpperBound(3410063222586309647L)
    checkLongUpperBound(3613546991519814900L)
    checkLongUpperBound(794235732280983726L)
    checkLongUpperBound(7785275145339378114L)
    checkLongUpperBound(4100457636061052898L)
    checkLongUpperBound(1018444320500755548L)
    checkLongUpperBound(9001409979785351255L)
    checkLongUpperBound(4075331949461069116L)
    checkLongUpperBound(31652439407451369L)
    checkLongUpperBound(3646525310865559959L)
    checkLongUpperBound(2806789474679250239L)
    checkLongUpperBound(4163962294215624856L)
    checkLongUpperBound(3510840945218300842L)
    checkLongUpperBound(2405660290506064846L)
    checkLongUpperBound(3395851088679001094L)
    checkLongUpperBound(2511845110478737749L)
    checkLongUpperBound(2070138108624959242L)
    checkLongUpperBound(2674601391118469061L)
    checkLongUpperBound(2267390941557653168L)
    checkLongUpperBound(8879840962642255324L)
    checkLongUpperBound(2522558163820509001L)
    checkLongUpperBound(8762376946098098079L)
    checkLongUpperBound(7156146337989773092L)
    checkLongUpperBound(2886784943793786222L)
    checkLongUpperBound(7979230018726139828L)
    checkLongUpperBound(5265068789516370997L)
    checkLongUpperBound(5016186842980385468L)
    checkLongUpperBound(670336532416458804L)
    checkLongUpperBound(5716088979570456146L)
    checkLongUpperBound(2286722881428761318L)
    checkLongUpperBound(5802288328763952405L)
    checkLongUpperBound(5484324605810025101L)
    checkLongUpperBound(6117498799840113187L)
    checkLongUpperBound(6287906655856893939L)
    checkLongUpperBound(194037451184373822L)
    checkLongUpperBound(8203984136473124403L)
    checkLongUpperBound(240868966398084888L)
    checkLongUpperBound(274646322154193481L)
    checkLongUpperBound(990278556758554577L)
    checkLongUpperBound(4082559561918452490L)
    checkLongUpperBound(5005809272567803740L)
    checkLongUpperBound(2448996442117761309L)
    checkLongUpperBound(2485615017157150754L)
    checkLongUpperBound(7814186341888340673L)
    checkLongUpperBound(5542611725517079214L)
    checkLongUpperBound(7922071822271160840L)
    checkLongUpperBound(3701987054744384230L)
    checkLongUpperBound(4054437358544640978L)
    checkLongUpperBound(5303406621773616445L)
    checkLongUpperBound(4926583183994031220L)
    checkLongUpperBound(1718588246079623569L)
    checkLongUpperBound(750567898109091861L)
    checkLongUpperBound(2942474255612652774L)
    checkLongUpperBound(8746666313015576654L)
    checkLongUpperBound(7925716930346762441L)
    checkLongUpperBound(4207362475410336507L)
    checkLongUpperBound(3897283832649512270L)
    checkLongUpperBound(2604786423326482461L)
    checkLongUpperBound(8513774996935440400L)
    checkLongUpperBound(4131798407110110491L)
    checkLongUpperBound(8278790084147518379L)
    checkLongUpperBound(6609895570178025534L)
    checkLongUpperBound(6747180076584888225L)
    checkLongUpperBound(3914184650366328674L)
    checkLongUpperBound(8518790439050981969L)
    checkLongUpperBound(3282457251029518870L)
    checkLongUpperBound(6522533840416377503L)
    checkLongUpperBound(2283521020011024908L)
    checkLongUpperBound(7921397828855501388L)
    checkLongUpperBound(3432357545099202765L)
    checkLongUpperBound(3473444099901771044L)
    checkLongUpperBound(2199609404535362905L)
    checkLongUpperBound(5234237725584523546L)
    checkLongUpperBound(8987269161093090697L)
    checkLongUpperBound(5592627078482398521L)
    checkLongUpperBound(4329118373247807610L)
    checkLongUpperBound(7190616425187681568L)
    checkLongUpperBound(4094848023681988657L)
    checkLongUpperBound(4142021276770100118L)
    checkLongUpperBound(1654923938086137521L)
    checkLongUpperBound(7594229781671800374L)
    checkLongUpperBound(358723396249334066L)

    assertThrows(classOf[IllegalArgumentException], () => tlr.nextLong(0L))
    assertThrows(classOf[IllegalArgumentException], () => tlr.nextLong(-1L))
    assertThrows(
      classOf[IllegalArgumentException],
      () => tlr.nextLong(Long.MinValue)
    )
  }

  def checkLongBounds(b1: Long, b2: Long)(implicit
      tlr: ThreadLocalRandom
  ): Unit = {
    val least = min(b1, b2)
    val bound = max(b1, b2)

    val next = tlr.nextLong(least, bound)
    assertTrue((next >= least) && (next < bound))
  }

  @Test def nextLongLongLong(): Unit = {
    implicit val tlr = ThreadLocalRandom.current()

    checkLongBounds(Long.MinValue, Long.MaxValue)
    checkLongBounds(Long.MinValue + 1L, 0L)
    checkLongBounds(Long.MaxValue, 0L)
    checkLongBounds(-1039837701034497990L, -8308698755549249034L)
    checkLongBounds(-2069434638433553634L, -6933192775725954083L)
    checkLongBounds(-651999308369245177L, -1874966875207646432L)
    checkLongBounds(7181913712461759345L, 6504342096862593231L)
    checkLongBounds(59977460129715521L, 6279062141381183657L)
    checkLongBounds(-6259074936267690470L, -6458162556369944440L)
    checkLongBounds(-2037582489881382418L, 5110744689259784990L)
    checkLongBounds(-4062940926760593448L, 346906180244363745L)
    checkLongBounds(8636071285524115241L, -5937211472528242108L)
    checkLongBounds(-4182402779516853824L, -7020432699720490457L)
    checkLongBounds(3119531345109469087L, -7478787228513435761L)
    checkLongBounds(-5619021195449114695L, 7604098963032018841L)
    checkLongBounds(-3826398054814432752L, -1954838802635988821L)
    checkLongBounds(-4081633848311947521L, 3180169880186823661L)
    checkLongBounds(9095807553990877140L, 4846733349061808631L)
    checkLongBounds(-1807685282703623007L, -3865505888849238325L)
    checkLongBounds(8722839571037805395L, 1479121172186720517L)
    checkLongBounds(5215508873722268675L, -7326049775082262447L)
    checkLongBounds(-927462278277892468L, 2177629967367708444L)
    checkLongBounds(3069937019735389L, 1976611941393580941L)
    checkLongBounds(-8264945996711929457L, 2601323231825499062L)
    checkLongBounds(-5886633547928521671L, 5669169602080520454L)
    checkLongBounds(7577703176704246019L, 7266080231695326978L)
    checkLongBounds(8088283460073143801L, 1995443058189449524L)
    checkLongBounds(-2393582343848952671L, -6487899221906115485L)
    checkLongBounds(-948491768762001330L, -6797034821486606589L)
    checkLongBounds(-1565498017677689418L, -891533307933518609L)
    checkLongBounds(6681172269409228738L, 1153641757113965141L)
    checkLongBounds(2391651322083521957L, 8718235753053606384L)
    checkLongBounds(-7156980071896580560L, -6443446189128545667L)
    checkLongBounds(4469219439373648995L, -2428450088988893337L)
    checkLongBounds(-8275306914499309242L, -3903014868948350780L)
    checkLongBounds(1606864893401364217L, 7638143322305853060L)
    checkLongBounds(5152848141051789578L, -6111234236372997401L)
    checkLongBounds(2165372015563576838L, -5012547946107795409L)
    checkLongBounds(-878766955521597870L, -2135786011517991529L)
    checkLongBounds(8188318368710394939L, 5616809898698768259L)
    checkLongBounds(6655383875627835722L, 8692004764665747192L)
    checkLongBounds(-4813079347574133539L, 3996679913545897037L)
    checkLongBounds(-8186407653293244430L, 5995152520624865570L)
    checkLongBounds(4560628660195213894L, 5612537594098937233L)
    checkLongBounds(-2640642448602803042L, -7050786745645919069L)
    checkLongBounds(-7904959629724808093L, -2531987517853969402L)
    checkLongBounds(-6849057933191867276L, -3056613757421720836L)
    checkLongBounds(-2386646297867974857L, 6752252990853952661L)
    checkLongBounds(6330040729441981937L, 5692102808539943199L)
    checkLongBounds(-7530267365179240105L, 551109681065587421L)
    checkLongBounds(-8391845266138388635L, -5688536092297674248L)
    checkLongBounds(-2044821628451722643L, 1628942734307756978L)
    checkLongBounds(-8648402666908748430L, -7191816448813649695L)
    checkLongBounds(8025532776117387702L, -9213168952111495270L)
    checkLongBounds(-4911181136149708399L, -2109630237148371925L)
    checkLongBounds(7681029602998162563L, 7953672991788383567L)
    checkLongBounds(618994211566364813L, 1401850179837534108L)
    checkLongBounds(2348298012851281084L, 4681701469003867199L)
    checkLongBounds(8911380097553430789L, -4181443527611425044L)
    checkLongBounds(-5181330326153293992L, 318895093008430863L)
    checkLongBounds(3929875392063216110L, 866245630634090567L)
    checkLongBounds(6426629223139207910L, 5214420315026318868L)
    checkLongBounds(-7109301247711248113L, -6360390314216046898L)
    checkLongBounds(3253699413831554567L, -176948813024323112L)
    checkLongBounds(4496854970256947588L, 3067323481867836693L)
    checkLongBounds(7680378981861936625L, -8308800439771085413L)
    checkLongBounds(5112952282397243964L, -1350698529253892185L)
    checkLongBounds(-1858733202193062674L, -6377630524268770865L)
    checkLongBounds(-4352042425224868741L, -1938404468483360899L)
    checkLongBounds(8010379491960279259L, 7874919461803714203L)
    checkLongBounds(6743734004028441176L, -5231804031534433141L)
    checkLongBounds(-7791589840737465943L, 6723467150208302682L)
    checkLongBounds(-4622592110323647168L, 1143988043667200052L)
    checkLongBounds(5369167545508378592L, 4072681384640817177L)
    checkLongBounds(5859250533873992817L, 3127889117299949520L)
    checkLongBounds(6838471430244348695L, 7306022610351411740L)
    checkLongBounds(8939031186276707200L, -4874917791143248083L)
    checkLongBounds(8452307066066522237L, -6906630582179941287L)
    checkLongBounds(5417097305649891540L, -3870743278039821557L)
    checkLongBounds(-1710233066881679021L, -4440748796794088709L)
    checkLongBounds(-4352858134288647128L, -929442011313777761L)
    checkLongBounds(-4192589067617713808L, 3814570672143716576L)
    checkLongBounds(-141971227720956659L, 9191837767583821585L)
    checkLongBounds(-5307146185544936004L, 3438306191704461852L)
    checkLongBounds(-5551540891085723291L, 1285256720494326782L)
    checkLongBounds(-6475933122106664267L, 4792676713709383284L)
    checkLongBounds(-7259335235955889174L, 5815170345819712502L)
    checkLongBounds(-6893858514313141523L, -4387170127069334556L)
    checkLongBounds(-4408791311457250651L, -3001946252718012929L)
    checkLongBounds(7557700532431938953L, -6591581189418141414L)
    checkLongBounds(-6023983568342958729L, -3031468300486487792L)
    checkLongBounds(624766591230360772L, -1467041168259694600L)
    checkLongBounds(-1120516802939941741L, 6880536964990944919L)
    checkLongBounds(-5926047551823285142L, 7929917894325004310L)
    checkLongBounds(-3266110634183043326L, -1899984018205711116L)
    checkLongBounds(-593218177692194723L, -4060221477906681539L)
    checkLongBounds(2636344344116900126L, -5962338786983306757L)
    checkLongBounds(471599638600463124L, 8954456753017228781L)
    checkLongBounds(-5954860235887426793L, 1963379810943155574L)
    checkLongBounds(7474020234467929111L, 755879431392888280L)
    checkLongBounds(4152230168026050417L, 7548604285400505249L)
    checkLongBounds(5611183948112311940L, 5576981966367959141L)
    checkLongBounds(7501725046819604868L, 2498819089300049836L)

    assertThrows(classOf[IllegalArgumentException], () => tlr.nextLong(2L, 1L))
    assertThrows(classOf[IllegalArgumentException], () => tlr.nextLong(1L, 1L))
  }

  def checkDoubleUpperBound(
      bound: Double
  )(implicit tlr: ThreadLocalRandom): Unit = {
    val next = tlr.nextDouble(bound)

    assertTrue(next < bound)
  }

  @Test def nextDoubleDouble(): Unit = {
    implicit val tlr = ThreadLocalRandom.current()

    checkDoubleUpperBound(Double.MaxValue)
    checkDoubleUpperBound(0.30461415569610606)
    checkDoubleUpperBound(0.45763741504623)
    checkDoubleUpperBound(0.5376054133901769)
    checkDoubleUpperBound(0.4484731212448333)
    checkDoubleUpperBound(0.39034055689678804)
    checkDoubleUpperBound(0.05730329822405311)
    checkDoubleUpperBound(0.63563298995727)
    checkDoubleUpperBound(0.08129593746568475)
    checkDoubleUpperBound(0.5731680747226203)
    checkDoubleUpperBound(0.6203051830669098)
    checkDoubleUpperBound(0.42736916725651564)
    checkDoubleUpperBound(0.06746716227703886)
    checkDoubleUpperBound(0.4470853195765113)
    checkDoubleUpperBound(0.7983753770662275)
    checkDoubleUpperBound(0.8142041468255999)
    checkDoubleUpperBound(0.48989336054216415)
    checkDoubleUpperBound(0.1286674897186728)
    checkDoubleUpperBound(0.8955391706630679)
    checkDoubleUpperBound(0.7518054046845716)
    checkDoubleUpperBound(0.8833239344428898)
    checkDoubleUpperBound(0.18282199465015303)
    checkDoubleUpperBound(0.16741777059880292)
    checkDoubleUpperBound(0.5797028800630278)
    checkDoubleUpperBound(0.7661564944015873)
    checkDoubleUpperBound(0.5714305532060087)
    checkDoubleUpperBound(0.14041421977378654)
    checkDoubleUpperBound(0.3394843703897348)
    checkDoubleUpperBound(0.8186053404299279)
    checkDoubleUpperBound(0.16007516175543357)
    checkDoubleUpperBound(0.22351821820281148)
    checkDoubleUpperBound(0.9219636388507496)
    checkDoubleUpperBound(0.2734259809203087)
    checkDoubleUpperBound(0.6861982226004079)
    checkDoubleUpperBound(0.042691750513262794)
    checkDoubleUpperBound(0.8924730783678572)
    checkDoubleUpperBound(0.5082396209556176)
    checkDoubleUpperBound(0.9914619829149804)
    checkDoubleUpperBound(0.8662743573904478)
    checkDoubleUpperBound(0.8834714190939048)
    checkDoubleUpperBound(0.532603535627163)
    checkDoubleUpperBound(0.7517361609326059)
    checkDoubleUpperBound(0.2095734501324391)
    checkDoubleUpperBound(0.5149463012734043)
    checkDoubleUpperBound(0.048324566491369625)
    checkDoubleUpperBound(0.9000568974990854)
    checkDoubleUpperBound(0.2077811249234438)
    checkDoubleUpperBound(0.9056304737907922)
    checkDoubleUpperBound(0.028114550134090588)
    checkDoubleUpperBound(0.43106384997652214)
    checkDoubleUpperBound(0.6285864088200106)
    checkDoubleUpperBound(0.9718394424656539)
    checkDoubleUpperBound(0.30553844095755334)
    checkDoubleUpperBound(0.299836951134698)
    checkDoubleUpperBound(0.45932746961167914)
    checkDoubleUpperBound(0.8757775960551799)
    checkDoubleUpperBound(0.498306601532463)
    checkDoubleUpperBound(0.6837176145076539)
    checkDoubleUpperBound(0.848255608044494)
    checkDoubleUpperBound(0.18144879455893537)
    checkDoubleUpperBound(0.697315317509338)
    checkDoubleUpperBound(0.9626139748584198)
    checkDoubleUpperBound(0.8054589474580296)
    checkDoubleUpperBound(0.5038462329989879)
    checkDoubleUpperBound(0.7454403844730811)
    checkDoubleUpperBound(0.3914534107735953)
    checkDoubleUpperBound(0.47622053513168194)
    checkDoubleUpperBound(0.6958861076485113)
    checkDoubleUpperBound(0.6029406063865022)
    checkDoubleUpperBound(0.587859611019135)
    checkDoubleUpperBound(0.9880622370989479)
    checkDoubleUpperBound(0.9075878116172037)
    checkDoubleUpperBound(0.2504292128440786)
    checkDoubleUpperBound(0.6387958618327038)
    checkDoubleUpperBound(0.8424517776251073)
    checkDoubleUpperBound(0.17329329142305794)
    checkDoubleUpperBound(0.8157234078918284)
    checkDoubleUpperBound(0.8418298716146202)
    checkDoubleUpperBound(0.5731278705352951)
    checkDoubleUpperBound(0.5352564380247649)
    checkDoubleUpperBound(0.12748306287231725)
    checkDoubleUpperBound(0.8398398175259664)
    checkDoubleUpperBound(0.9252238570337776)
    checkDoubleUpperBound(0.09572348143135034)
    checkDoubleUpperBound(0.696401626933412)
    checkDoubleUpperBound(0.18239526282067398)
    checkDoubleUpperBound(0.12284746297207705)
    checkDoubleUpperBound(0.8046631202192683)
    checkDoubleUpperBound(0.20381390805953825)
    checkDoubleUpperBound(0.15271052685731623)
    checkDoubleUpperBound(0.8875008782211234)
    checkDoubleUpperBound(0.2365952399378467)
    checkDoubleUpperBound(0.9379364002391153)
    checkDoubleUpperBound(0.035982528097754485)
    checkDoubleUpperBound(0.7457015355959284)
    checkDoubleUpperBound(0.08750598119304409)
    checkDoubleUpperBound(0.2595582507236297)
    checkDoubleUpperBound(0.8730886334922273)
    checkDoubleUpperBound(0.8213908293563262)
    checkDoubleUpperBound(0.6316252201145239)
    checkDoubleUpperBound(0.10185176522791717)

    assertThrows(classOf[IllegalArgumentException], () => tlr.nextDouble(0.0))
    assertThrows(classOf[IllegalArgumentException], () => tlr.nextDouble(-1.0))
    assertThrows(
      classOf[IllegalArgumentException],
      () => tlr.nextDouble(Double.MinValue)
    )
  }

  def checkDoubleBounds(b1: Double, b2: Double)(implicit
      tlr: ThreadLocalRandom
  ): Unit = {
    val least = min(b1, b2)
    val bound = max(b1, b2)

    val next = tlr.nextDouble(least, bound)
    assertTrue((next >= least) && (next < bound))
  }

  @Test def nextDoubleDoubleDouble(): Unit = {
    implicit val tlr = ThreadLocalRandom.current()

    if (!executingInJVM) {
      // This test fails with JDK 17 due to failed bounds check
      checkDoubleBounds(Double.MinValue, Double.MaxValue)
    }
    checkDoubleBounds(Double.MinValue, 0L)
    checkDoubleBounds(Double.MaxValue, 0L)
    checkDoubleBounds(0.14303466203185822, 0.7471945354839639)
    checkDoubleBounds(0.9178826051178738, 0.7130731758731785)
    checkDoubleBounds(0.7482067005480265, 0.5483251459348717)
    checkDoubleBounds(0.05714662279720417, 0.33627617380045116)
    checkDoubleBounds(0.13839516533824114, 0.35389294530716364)
    checkDoubleBounds(0.5538906481497655, 0.2867620780548301)
    checkDoubleBounds(0.4027227824817562, 0.572619440844722)
    checkDoubleBounds(0.26971878200430466, 0.935841772582903)
    checkDoubleBounds(0.6830228579085871, 0.7334228113504305)
    checkDoubleBounds(0.2712232514578353, 0.4385867668812312)
    checkDoubleBounds(0.31787799611818546, 0.5360720512378534)
    checkDoubleBounds(0.5109347241585122, 0.6535978666220456)
    checkDoubleBounds(0.7134434960017081, 0.7830830966025459)
    checkDoubleBounds(0.017665127254386292, 0.594421408975085)
    checkDoubleBounds(0.05534382469064125, 0.7712562073260051)
    checkDoubleBounds(0.031332551299375955, 0.9250949127486744)
    checkDoubleBounds(0.6253444881066392, 0.40973103097597086)
    checkDoubleBounds(0.307395922485463, 0.4664053622143831)
    checkDoubleBounds(0.6671657567599689, 0.8011624068051623)
    checkDoubleBounds(0.6373172175558369, 0.4147949604183252)
    checkDoubleBounds(0.4577189183253101, 0.27359554503475325)
    checkDoubleBounds(0.48400694702580627, 0.9924506207846631)
    checkDoubleBounds(0.4832092844569361, 0.8828472545130348)
    checkDoubleBounds(0.5149988099370096, 0.5449652364238221)
    checkDoubleBounds(0.39396513455075133, 0.2186752647642909)
    checkDoubleBounds(0.7311374910578777, 0.6820602787228435)
    checkDoubleBounds(0.7175146319453928, 0.9427446432188954)
    checkDoubleBounds(0.8348534482248177, 0.9172106646286674)
    checkDoubleBounds(0.14634814754092285, 0.8623772655199232)
    checkDoubleBounds(0.45963697494107203, 0.403614468065966)
    checkDoubleBounds(0.5849663354090479, 0.5012959747342978)
    checkDoubleBounds(0.8911133248087306, 0.786802009665243)
    checkDoubleBounds(0.04022910561470172, 0.06705272741197044)
    checkDoubleBounds(0.9501593723176215, 0.8982795757923677)
    checkDoubleBounds(0.8696842615260117, 0.4345554537062294)
    checkDoubleBounds(0.7797919470921422, 0.9999555326043813)
    checkDoubleBounds(0.8644690538172136, 0.2660858765287115)
    checkDoubleBounds(0.3800959187933144, 0.555697396834288)
    checkDoubleBounds(0.13654165674274543, 0.6704265944876738)
    checkDoubleBounds(0.8692508872437965, 0.05422058676995378)
    checkDoubleBounds(0.8044133689409166, 0.8671922722985317)
    checkDoubleBounds(0.6137523606750158, 0.2366103775267232)
    checkDoubleBounds(0.02721737310510719, 0.16718659184532758)
    checkDoubleBounds(0.5672142732871579, 0.192131376981163)
    checkDoubleBounds(0.02386278867697622, 0.20558304145956685)
    checkDoubleBounds(0.3846772999954965, 0.17757888406521338)
    checkDoubleBounds(0.33218758728665754, 0.7719542116117082)
    checkDoubleBounds(0.13813733375171333, 0.6882792433409614)
    checkDoubleBounds(0.7124377615594696, 0.7696508134642741)
    checkDoubleBounds(0.7490474507233023, 0.2629474028460165)
    checkDoubleBounds(0.780064031912043, 0.8067580681082349)
    checkDoubleBounds(0.5748351032192293, 0.7399613724783147)
    checkDoubleBounds(0.6647419540205698, 0.6718341142494464)
    checkDoubleBounds(0.9390164592457185, 0.19921512297361488)
    checkDoubleBounds(0.7356845252021958, 0.4798610413040666)
    checkDoubleBounds(0.7782776978465014, 0.6215626326388634)
    checkDoubleBounds(0.7077313953500877, 0.5873161147601307)
    checkDoubleBounds(0.9949331859789483, 0.37696785996307325)
    checkDoubleBounds(0.2483621400363376, 0.46999740996463557)
    checkDoubleBounds(0.5494584097586519, 0.012826428081115782)
    checkDoubleBounds(0.5426953874501679, 0.6332140813760382)
    checkDoubleBounds(0.805335974533688, 0.45552701679135266)
    checkDoubleBounds(0.14169956586732335, 0.28117878903078775)
    checkDoubleBounds(0.14724060471141664, 0.6611710978093759)
    checkDoubleBounds(0.818255473914, 0.9109158642131583)
    checkDoubleBounds(0.43362908096170216, 0.9554723848629075)
    checkDoubleBounds(0.08637423717551496, 0.21572523141563182)
    checkDoubleBounds(0.4160901115007323, 0.7882078211557633)
    checkDoubleBounds(0.500788826287339, 0.6842195990858123)
    checkDoubleBounds(0.8603473201250029, 0.394194354383801)
    checkDoubleBounds(0.8473013853947472, 0.06317751540478178)
    checkDoubleBounds(0.7375989310558742, 0.9006165477919463)
    checkDoubleBounds(0.8586821110736994, 0.41593290694779395)
    checkDoubleBounds(0.5199154667916854, 0.7496324782706943)
    checkDoubleBounds(0.14658041663222143, 0.8527472088150932)
    checkDoubleBounds(0.3097068270345842, 0.915536071145142)
    checkDoubleBounds(0.6268221431879527, 0.1355876101356409)
    checkDoubleBounds(0.26080859515989396, 0.2873562049581082)
    checkDoubleBounds(0.8336314368397639, 0.26696047894351516)
    checkDoubleBounds(0.5075268121209552, 0.7606243977205505)
    checkDoubleBounds(0.16772966509067377, 0.8609267931250674)
    checkDoubleBounds(0.6080193356204278, 0.03614403132631461)
    checkDoubleBounds(0.3039277663425398, 0.5641520233943196)
    checkDoubleBounds(0.32968095028347844, 0.8589460453948421)
    checkDoubleBounds(0.6957424902527402, 0.04581977263818504)
    checkDoubleBounds(0.45673874654951907, 0.44721765852305817)
    checkDoubleBounds(0.35773122812975533, 0.10746538138897332)
    checkDoubleBounds(0.18405273506318132, 0.1588418643893179)
    checkDoubleBounds(0.8806540745110499, 0.27726163344919064)
    checkDoubleBounds(0.5761566383812626, 0.02228706662534119)
    checkDoubleBounds(0.9402357463396348, 0.8480157994812402)
    checkDoubleBounds(0.5168619649603614, 0.6189383939669729)
    checkDoubleBounds(0.39721404453750286, 0.6941135429266562)
    checkDoubleBounds(0.5522879061902004, 0.9455627854406636)
    checkDoubleBounds(0.45452610639843205, 0.359871933633517)
    checkDoubleBounds(0.03896897948687339, 0.30845240071614766)
    checkDoubleBounds(0.23689666502572537, 0.8502400163723647)
    checkDoubleBounds(0.04873083469340511, 0.004891910693304746)
    checkDoubleBounds(0.5887579571381444, 0.27451268823686337)
    checkDoubleBounds(0.5533138714786693, 0.5329471271772576)

    assertThrows(
      classOf[IllegalArgumentException],
      () => tlr.nextDouble(2.0, 1.0)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      () => tlr.nextDouble(1.0, 1.0)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      () => tlr.nextDouble(0.0, 0.0)
    )
  }
}

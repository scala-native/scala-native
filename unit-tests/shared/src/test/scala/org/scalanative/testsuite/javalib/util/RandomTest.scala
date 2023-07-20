package org.scalanative.testsuite.javalib.util

import java.{lang => jl}

import java.util._
import java.util.function.DoubleConsumer

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import scala.scalanative.junit.utils.AssumesHelper._

class RandomTest {

  final val epsilon = 0.00000000 // tolerance for Floating point comparisons.

  /** Helper class to access next */
  class HackRandom(seed: Long) extends Random(seed) {
    override def next(bits: Int): Int = super.next(bits)
  }

  /* // FIXME
  @Test def seed10(): Unit = {
    val random = new HackRandom(10)

    assertTrue(random.next(10) == 747)
    assertTrue(random.next(1) == 0)
    assertTrue(random.next(6) == 16)
    assertTrue(random.next(20) == 432970)
    assertTrue(random.next(32) == 254270492)
  }

  @Test def seedNegative5(): Unit = {
    val random = new HackRandom(-5)

    assertTrue(random.next(10) == 275)
    assertTrue(random.next(1) == 0)
    assertTrue(random.next(6) == 21)
    assertTrue(random.next(20) == 360349)
    assertTrue(random.next(32) == 1635930704)
  }

  @Test def seedMaxLong(): Unit = {
    val random = new HackRandom(Long.MaxValue)

    assertTrue(random.next(10) == 275)
    assertTrue(random.next(1) == 0)
    assertTrue(random.next(6) == 0)
    assertTrue(random.next(20) == 574655)
    assertTrue(random.next(32) == -1451336087)
  }

  @Test def seedMaxInt(): Unit = {
    val random = new HackRandom(Int.MinValue)

    assertTrue(random.next(10) == 388)
    assertTrue(random.next(1) == 0)
    assertTrue(random.next(6) == 25)
    assertTrue(random.next(20) == 352095)
    assertTrue(random.next(32) == -2140124682)
  }

  @Test def seedReset(): Unit = {
    val random = new HackRandom(11)
    assertTrue(random.next(10) == 747)
    assertTrue(random.next(1) == 1)
    assertTrue(random.next(6) == 27)

    random.setSeed(11)
    assertTrue(random.next(10) == 747)
    assertTrue(random.next(1) == 1)
    assertTrue(random.next(6) == 27)
  }

  @Test def resetNextGaussian(): Unit = {
    assumeNot32Bit()
    val random = new Random(-1)
    assertTrue(random.nextGaussian() == 1.7853314409882288)
    random.setSeed(-1)
    assertTrue(random.nextGaussian() == 1.7853314409882288)
  }

  @Test def nextDouble(): Unit = {
    val random = new Random(-45)
    assertTrue(random.nextDouble() == 0.27288421395636253)
    assertTrue(random.nextDouble() == 0.5523165360074201)
    assertTrue(random.nextDouble() == 0.5689979434708298)
    assertTrue(random.nextDouble() == 0.9961166166874871)
    assertTrue(random.nextDouble() == 0.5368984665202684)
    assertTrue(random.nextDouble() == 0.19849067496547423)
    assertTrue(random.nextDouble() == 0.6021019223595357)
    assertTrue(random.nextDouble() == 0.06132131151816378)
    assertTrue(random.nextDouble() == 0.7303867762743866)
    assertTrue(random.nextDouble() == 0.7426529384056163)
  }

  @Test def nextBoolean(): Unit = {
    val random = new Random(4782934)
    assertTrue(random.nextBoolean() == false)
    assertTrue(random.nextBoolean() == true)
    assertTrue(random.nextBoolean() == true)
    assertTrue(random.nextBoolean() == false)
    assertTrue(random.nextBoolean() == false)
    assertTrue(random.nextBoolean() == false)
    assertTrue(random.nextBoolean() == true)
    assertTrue(random.nextBoolean() == false)
  }

  @Test def nextInt(): Unit = {
    val random = new Random(-84638)
    assertTrue(random.nextInt() == -1217585344)
    assertTrue(random.nextInt() == 1665699216)
    assertTrue(random.nextInt() == 382013296)
    assertTrue(random.nextInt() == 1604432482)
    assertTrue(random.nextInt() == -1689010196)
    assertTrue(random.nextInt() == 1743354032)
    assertTrue(random.nextInt() == 454046816)
    assertTrue(random.nextInt() == 922172344)
    assertTrue(random.nextInt() == -1890515287)
    assertTrue(random.nextInt() == 1397525728)
  }

  @Test def nextIntN(): Unit = {
    val random = new Random(7)
    assertTrue(random.nextInt(76543) == 32736)
    assertTrue {
      try {
        random.nextInt(0)
        false
      } catch {
        case _: Throwable => true
      }
    }
    assertTrue(random.nextInt(45) == 29)
    assertTrue(random.nextInt(945) == 60)
    assertTrue(random.nextInt(35694839) == 20678044)
    assertTrue(random.nextInt(35699) == 23932)
    assertTrue(random.nextInt(3699) == 2278)
    assertTrue(random.nextInt(10) == 8)
  }

  @Test def nextInt2Pow(): Unit = {
    val random = new Random(-56938)

    assertTrue(random.nextInt(32) == 8)
    assertTrue(random.nextInt(8) == 3)
    assertTrue(random.nextInt(128) == 3)
    assertTrue(random.nextInt(4096) == 1950)
    assertTrue(random.nextInt(8192) == 3706)
    assertTrue(random.nextInt(8192) == 4308)
    assertTrue(random.nextInt(8192) == 3235)
    assertTrue(random.nextInt(8192) == 7077)
    assertTrue(random.nextInt(8192) == 2392)
    assertTrue(random.nextInt(32) == 31)
  }

  @Test def nextLong(): Unit = {
    val random = new Random(205620432625028L)
    assertTrue(random.nextLong() == 3710537363280377478L)
    assertTrue(random.nextLong() == 4121778334981170700L)
    assertTrue(random.nextLong() == 289540773990891960L)
    assertTrue(random.nextLong() == 307008980197674441L)
    assertTrue(random.nextLong() == 7527069864796025013L)
    assertTrue(random.nextLong() == -4563192874520002144L)
    assertTrue(random.nextLong() == 7619507045427546529L)
    assertTrue(random.nextLong() == -7888117030898487184L)
    assertTrue(random.nextLong() == -3499168703537933266L)
    assertTrue(random.nextLong() == -1998975913933474L)
  }

  @Test def nextFloat(): Unit = {
    val random = new Random(-3920005825473L)

    def closeTo(num: Float, exp: Double): Boolean =
      ((num < (exp + 0.0000001)) && (num > (exp - 0.0000001)))

    assertTrue(closeTo(random.nextFloat(), 0.059591234))
    assertTrue(closeTo(random.nextFloat(), 0.7007871))
    assertTrue(closeTo(random.nextFloat(), 0.39173192))
    assertTrue(closeTo(random.nextFloat(), 0.0647918))
    assertTrue(closeTo(random.nextFloat(), 0.9029677))
    assertTrue(closeTo(random.nextFloat(), 0.18226051))
    assertTrue(closeTo(random.nextFloat(), 0.94444054))
    assertTrue(closeTo(random.nextFloat(), 0.008844078))
    assertTrue(closeTo(random.nextFloat(), 0.08891684))
    assertTrue(closeTo(random.nextFloat(), 0.06482434))
  }

  @Test def nextBytes(): Unit = {
    val random = new Random(7399572013373333L)

    def test(exps: Array[Int]) = {
      val exp = exps.map(_.toByte)
      val buf = new Array[Byte](exp.length)
      random.nextBytes(buf)
      var i = 0
      var res = true
      assertTrue {
        while (i < buf.size && res == true) {
          res = (buf(i) == exp(i))
          i += 1
        }
        res
      }
    }

    test(Array[Int](62, 89, 68, -91, 10, 0, 85))
    test(
      Array[Int](-89, -76, 88, 121, -25, 47, 58, -8, 78, 20, -77, 84, -3, -33,
        58, -9, 11, 57, -118, 40, -74, -86, 78, 123, 58)
    )
    test(Array[Int](-77, 112, -116))
    test(Array[Int]())
    test(Array[Int](-84, -96, 108))
    test(Array[Int](57, -106, 42, -100, -47, -84, 67, -48, 45))
  }

  @Test def nextGaussian(): Unit = {
    assumeNot32Bit()
    val random = new Random(2446004)
    assertTrue(random.nextGaussian() == -0.5043346938630431)
    assertTrue(random.nextGaussian() == -0.3250983270156675)
    assertTrue(random.nextGaussian() == -0.23799457294994966)
    assertTrue(random.nextGaussian() == 0.4164610631507695)
    assertTrue(random.nextGaussian() == 0.22086348814760687)
    assertTrue(random.nextGaussian() == -0.706833209972521)
    assertTrue(random.nextGaussian() == 0.6730758289772553)
    assertTrue(random.nextGaussian() == 0.2797393696191283)
    assertTrue(random.nextGaussian() == -0.2979099632667685)
    assertTrue(random.nextGaussian() == 0.37443415981434314)
    assertTrue(random.nextGaussian() == 0.9584801742918951)
    assertTrue(random.nextGaussian() == 1.1762179112229345)
    assertTrue(random.nextGaussian() == 0.8736960092848826)
    assertTrue(random.nextGaussian() == 0.12301554931271008)
    assertTrue(random.nextGaussian() == -0.6052081187207353)
    assertTrue(random.nextGaussian() == -0.2015925608755316)
    assertTrue(random.nextGaussian() == -1.0071216119742104)
    assertTrue(random.nextGaussian() == 0.6734222041441913)
    assertTrue(random.nextGaussian() == 0.3990565555091522)
    assertTrue(random.nextGaussian() == 2.0051627385915154)
  }

  @Test def defaultSeed(): Unit = {
    // added for #849
    val random1 = new Random()
    val random2 = new Random()
    assertTrue(random1.hashCode != random2.hashCode)
    assertTrue(random1.nextInt != random2.nextInt)
  }
   */ // FIXME

  final val expectedCharacteristics =
    Spliterator.SIZED | Spliterator.IMMUTABLE |
      Spliterator.NONNULL | Spliterator.SUBSIZED //  0x4540, decimal 17728

  @Test def doublesZeroArg(): Unit = {
    // doubles()

    val seed = 0xa5a5a5a5a5a5a5a5L

    val rng1 = new Random(seed)
    val ds1 = rng1.doubles()

    val ds1Spliter = ds1.spliterator()

    assertEquals(
      "characteristics",
      expectedCharacteristics,
      ds1Spliter.characteristics()
    )

    assertEquals("estimated size", jl.Long.MAX_VALUE, ds1Spliter.estimateSize())

    assertEquals(
      s"getExactSizeIfKnown",
      jl.Long.MAX_VALUE,
      ds1Spliter.getExactSizeIfKnown()
    )

    assertFalse(
      "Expected sequential stream",
      ds1.isParallel()
    )

    val rng2 = new Random(seed)
    val ds2 = rng2.doubles()

    val expectedContent = 0.10435154059121454

    // for the skipTo element to be right, everything before it should be OK.
    val actualContent = ds2
      .skip(10)
      .findFirst()
      .orElse(0.0)

    assertEquals("content", expectedContent, actualContent, epsilon)
  }

  @Test def doublesOneArg(): Unit = {
    // doubles(long streamSize)
    val seed = 0xa5a5a5a5a5a5a5a5L
    val streamSize = 7

    val rng1 = new Random(seed)
    val ds1 = rng1.doubles(streamSize)

    val ds1Spliter = ds1.spliterator()

    assertEquals(
      "characteristics",
      expectedCharacteristics,
      ds1Spliter.characteristics()
    )

    assertEquals("estimated size", streamSize, ds1Spliter.estimateSize())

    assertEquals(
      s"getExactSizeIfKnown",
      streamSize,
      ds1Spliter.getExactSizeIfKnown()
    )

    assertFalse(
      "Expected sequential stream",
      ds1.isParallel()
    )

    val rng2 = new Random(seed)
    val ds2 = rng2.doubles(streamSize)

    assertEquals("count", streamSize, ds2.count())

    val rng3 = new Random(seed)
    val ds3 = rng3.doubles(streamSize)

    val expectedContent = 0.6915186905201246

    // for the skipTo element to be right, everything before it should be OK.
    val actualContent = ds3
      .skip(5)
      .findFirst()
      .orElse(0.0)

    assertEquals("content", expectedContent, actualContent, epsilon)
  }

  @Test def doublesTwoArg(): Unit = {
    // doubles(double randomNumberOrigin, double randomNumberBound)

    // This test is not guaranteed. It samples to build correctness confidence.

    val seed = 0xa5a5a5a5a5a5a5a5L
    val streamSize = 100

    val rnOrigin = 2.0
    val rnBound = 80.0

    val rng1 = new Random(seed)
    val ds1 = rng1.doubles(rnOrigin, rnBound)

    val ds1Spliter = ds1.spliterator()

    assertEquals(
      "characteristics",
      expectedCharacteristics,
      ds1Spliter.characteristics()
    )

    assertEquals("estimated size", jl.Long.MAX_VALUE, ds1Spliter.estimateSize())

    assertEquals(
      s"getExactSizeIfKnown",
      jl.Long.MAX_VALUE,
      ds1Spliter.getExactSizeIfKnown()
    )

    assertFalse(
      "Expected sequential stream",
      ds1.isParallel()
    )

    val rng2 = new Random(seed)
    val ds2 = rng2
      .doubles(rnOrigin, rnBound)
      .limit(streamSize)

    // Keep Scala 2 happy. Can use lambda when only Scala > 2 is supported.
    val doubleConsumer = new DoubleConsumer {
      def accept(d: Double): Unit = {
        assertTrue(
          s"Found value ${d} < low bound ${rnOrigin}",
          d >= rnOrigin
        )

        assertTrue(
          s"Found value ${d} >= high bound ${rnBound}",
          d < rnBound
        )
      }
    }

    ds2.forEach(doubleConsumer)
  }

  @Test def doublesThreeArg(): Unit = {
    // doubles(long streamSize, double randomNumberOrigin,
    //         double randomNumberBound)

    // This test is not guaranteed. It samples to build correctness confidence.

    val seed = 0xa5a5a5a5a5a5a5a5L
    val streamSize = 100

    val rnOrigin = 1.0
    val rnBound = 90.0

    val rng1 = new Random(seed)
    val ds1 = rng1.doubles(streamSize, rnOrigin, rnBound)

    val ds1Spliter = ds1.spliterator()

    assertEquals(
      "characteristics",
      expectedCharacteristics,
      ds1Spliter.characteristics()
    )

    assertEquals("estimated size", streamSize, ds1Spliter.estimateSize())

    assertEquals(
      s"getExactSizeIfKnown",
      streamSize,
      ds1Spliter.getExactSizeIfKnown()
    )

    assertFalse(
      "Expected sequential stream",
      ds1.isParallel()
    )

    val rng2 = new Random(seed)
    val ds2 = rng2.doubles(streamSize, rnOrigin, rnBound)

    assertEquals("count", streamSize, ds2.count())

    val rng3 = new Random(seed)
    val ds3 = rng3.doubles(streamSize, rnOrigin, rnBound)

    // Keep Scala 2 happy. Can use lambda when only Scala > 2 is supported.
    val doubleConsumer = new DoubleConsumer {
      def accept(d: Double): Unit = {
        assertTrue(
          s"Found value ${d} < low bound ${rnOrigin}",
          d >= rnOrigin
        )

        assertTrue(
          s"Found value ${d} >= high bound ${rnBound}",
          d < rnBound
        )
      }
    }

    ds3.forEach(doubleConsumer)
  }

}

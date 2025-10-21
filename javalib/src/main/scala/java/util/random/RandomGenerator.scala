package java.util.random

/* Licenses and selected References:
 *
 * Most of the JEP 356 RandomGenerator work is covered by the Apache license.
 * This includes work ported from Scala.js and original work for Scala Native.
 *
 * Some useful references for fine and/or obscure points of algorithm
 * implementations and specific license information.
 *
 * - JEP 356: Enhanced Pseudo-Random Number Generators
 *     https://openjdk.org/jeps/356
 *
 * - Guy L. Steele Jr. and Sebastiano Vigna. LXM: Better splittable
 *   pseudorandom number generators (and almost as fast). Proc. ACM Program.
 *   Lang., 5(OOPSLA):1−31, 2021.
 *
 *     "This work is licensed under a Creative Commons Attribution 4.0
 *      international license © 2021 Copyright held by the owner/author(s)."
 *
 *      The terms of that license requires this notice that the code
 *      from that paper has been altered by being translated to Scala.
 *
 * - Sebastiano Vigna: "Generating uniform doubles in the unit interval""
 *     https://prng.di.unimi.it/
 *
 * - Sebastiano Vigna "DSI Utilities", source code.
 *     "The DSI utilities are free software distributed under either the
 *	GNU Lesser General Public License 2.1+ or the
 *	Apache Software License 2.0. "
 *
 *     Accessed under the Apache Software License.
 *     https://dsiutils.di.unimi.it/docs/it/unimi/dsi/util/package-summary.html
 *
 * - Sebastiano Vigna source code.
 *     https://github.com/vigna/dsiutils/tree/master/src/it/unimi/dsi/util
 *	   /XoRoShiRo128PlusRandomGenerator.java
 *	   /XoShiRo256PlusRandomGenerator.java
 *     Additional source there was studied.
 *
 *     Accessed under the Apache Software License.
 *     https://dsiutils.di.unimi.it/docs/it/unimi/dsi/util/package-summary.html
 *
 *     Code in this file modifies that code by converting it to Scala.
 *
 * - David Blackman and Sebastiano Vigna, 2019
 *
 *     https://prng.di.unimi.it/xoshiro256plusplus.c and similar studied
 *
 *    The authors state in the code:
 *      To the extent possible under law, the author has dedicated all
 *      copyright and related and neighboring rights to this software to the
 *      public domain worldwide.
 *
 *      Permission to use, copy, modify, and/or distribute this software for
 *      any purpose with or without fee is hereby granted.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *      WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *      WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *      AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 *      DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA
 *      OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 *      TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *      PERFORMANCE OF THIS SOFTWARE.
 *
 * - Guy L. Steele Jr., Doug Lea, and Christine H. Flood. 2014.
 *   "Fast Splittable Pseudorandom Number Generators"". In OOPSLA
 *   ’14: Proceedings of the 2014 ACM International Conference on
 *   Object-oriented Programming, Systems, Languages, and Applications
 *   (Portland, Oregon, USA) (OOPSLA ’14). ACM, New York, New York, USA,
 *   453-472. ISBN 9781450325851
 *
 *     https://gee.cs.oswego.edu/dl/papers/oopsla14.pdf
 *
 * - Apache Commons: many studied, particularly.
 *     https://commons.apache.org/proper/commons-rng/commons-rng-core/
 *       apidocs/org/apache/commons/rng/core/source32/
 *       L32X64Mix.html?is-external=true#%3Cinit%3E(int%5B%5D)
 *
 * - David Stafford, 2011-09-28
 *   "Better Bit Mixing - Improving on MurmurHash3's 64-bit Finalizer"
 *     http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html
 */

/* Implementation Notes
 *  1) By convention, types scala.Double, scala.Float, and scala.Long are
 *     used where a Java primitive (AnyVal) is expected, at least on method
 *     arguments & return types.
 *
 *     This makes the code easier for, at least the author, to read and
 *     robust to changes in which files are imported. Let the compiler catch
 *     AnyVal/AnyRef mismatches.
 *
 *     Java does not have an Int type so an Int is always a scala.Int and
 *     need not be qualified.
 */

import java.util.Spliterators._
import java.util._
import java.util.function._
import java.util.stream._
import java.{lang => jl}

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.unistd
import scala.scalanative.unsafe.Size.intToSize
import scala.scalanative.unsafe._

trait RandomGenerator {

  def nextLong(): scala.Long // Abstract

  /* Default methods
   */

  /* Normally methods would be declared in roughly alphabetical order.
   * Here they are split into two sections, each in roughly alphabetical
   * order.
   *
   * RandomGenerator default methods which do not return a Stream were ported
   * from Scala.js for numerical accuracy and to ease future corrections and/or
   * re-ports.
   *
   * The intention is that section have minimal Scala Native changes.
   * Some types have been changed to ensure that Scala, not Java, types are
   * used.
   *
   * Scala.js does not implement RandomGenerator methods which return Streams.
   * The Scala Native work which does follows the Scala.js section.
   */

  import java.util.ScalaOps._

// Begin Ported from Scala.js commit: 9cb865f dated: 2025-03-16
  import scala.annotation.tailrec

  // Comments starting with `// >` are cited from the JavaDoc.
  def nextBoolean(): scala.Boolean =
    nextInt() < 0 // is the sign bit 1?

  def nextBytes(bytes: Array[Byte]): Unit = {
    val len = bytes.length // implicit NPE
    var i = 0

    for (_ <- 0 until (len >> 3)) {
      var rnd = nextLong()
      for (_ <- 0 until 8) {
        bytes(i) = rnd.toByte
        rnd >>>= 8
        i += 1
      }
    }

    if (i != len) {
      var rnd = nextLong()
      while (i != len) {
        bytes(i) = rnd.toByte
        rnd >>>= 8
        i += 1
      }
    }
  }

  def nextFloat(): scala.Float = {
    // > Uses the 24 high-order bits from a call to nextInt()
    val bits = nextInt() >>> (32 - 24)
    bits.toFloat * (1.0f / (1 << 24)) // lossless multiplication
  }

  def nextFloat(bound: scala.Float): scala.Float = {
    // false for NaN
    if (bound > 0 && bound != Float.PositiveInfinity)
      ensureBelowBound(nextFloatBoundedInternal(bound), bound)
    else
      throw new IllegalArgumentException(s"Illegal bound: $bound")
  }

  def nextFloat(origin: scala.Float, bound: scala.Float): scala.Float = {
    // `origin < bound` is false if either input is NaN
    if (origin != Float.NegativeInfinity && origin < bound &&
        bound != Float.PositiveInfinity) {
      val difference = bound - origin
      val result = if (difference != Float.PositiveInfinity) {
        // Easy case
        origin + nextFloatBoundedInternal(difference)
      } else {
        // Overflow: scale everything down by 0.5 then scale it back up by 2.0
        val halfOrigin = origin * 0.5f
        val halfBound = bound * 0.5f
        (halfOrigin + nextFloatBoundedInternal(halfBound - halfOrigin)) * 2.0f
      }

      ensureBelowBound(result, bound)
    } else {
      throw new IllegalArgumentException(s"Illegal bounds: [$origin, $bound)")
    }
  }

  @inline
  private def nextFloatBoundedInternal(bound: scala.Float): scala.Float =
    nextFloat() * bound

  @inline
  private def ensureBelowBound(
      value: scala.Float,
      bound: scala.Float
  ): scala.Float = {
    /* Based on documentation for Random.doubles to avoid issue #2144 and other
     * possible rounding up issues:
     * https://docs.oracle.com/javase/8/docs/api/java/util/Random.html#doubles-double-double-
     */
    if (value < bound) value
    else Math.nextDown(bound)
  }

  def nextDouble(): scala.Double = {
    // > Uses the 53 high-order bits from a call to nextLong()
    val bits = nextLong() >>> (64 - 53)
    bits.toDouble * (1.0 / (1L << 53)) // lossless multiplication
  }

  def nextDouble(bound: scala.Double): scala.Double = {
    // false for NaN
    if (bound > 0 && bound != Double.PositiveInfinity)
      ensureBelowBound(nextDoubleBoundedInternal(bound), bound)
    else
      throw new IllegalArgumentException(s"Illegal bound: $bound")
  }

  def nextDouble(origin: scala.Double, bound: scala.Double): scala.Double = {
    // `origin < bound` is false if either input is NaN
    if (origin != Double.NegativeInfinity && origin < bound &&
        bound != Double.PositiveInfinity) {
      val difference = bound - origin
      val result = if (difference != Double.PositiveInfinity) {
        // Easy case
        origin + nextDoubleBoundedInternal(difference)
      } else {
        // Overflow: scale everything down by 0.5 then scale it back up by 2.0
        val halfOrigin = origin * 0.5
        val halfBound = bound * 0.5
        (halfOrigin + nextDoubleBoundedInternal(halfBound - halfOrigin)) * 2.0
      }

      ensureBelowBound(result, bound)
    } else {
      throw new IllegalArgumentException(s"Illegal bounds: [$origin, $bound)")
    }
  }

  @inline
  private def nextDoubleBoundedInternal(bound: scala.Double): scala.Double =
    nextDouble() * bound

  @inline
  private def ensureBelowBound(
      value: scala.Double,
      bound: scala.Double
  ): Double = {
    /* Based on documentation for Random.doubles to avoid issue #2144 and other
     * possible rounding up issues:
     * https://docs.oracle.com/javase/8/docs/api/java/util/Random.html#doubles-double-double-
     */
    if (value < bound) value
    else Math.nextDown(bound)
  }

  def nextInt(): Int = {
    // > Uses the 32 high-order bits from a call to nextLong()
    (nextLong() >>> 32).toInt
  }

  /* The algorithms used in nextInt() with bounds were initially part of
   * ThreadLocalRandom. That implementation had been written by Doug Lea with
   * assistance from members of JCP JSR-166 Expert Group and released to the
   * public domain, as explained at
   * http://creativecommons.org/publicdomain/zero/1.0/
   */

  def nextInt(bound: Int): Int = {
    if (bound <= 0)
      throw new IllegalArgumentException(s"Illegal bound: $bound")

    nextIntBoundedInternal(bound)
  }

  def nextInt(origin: Int, bound: Int): Int = {
    if (bound <= origin)
      throw new IllegalArgumentException(s"Illegal bounds: [$origin, $bound)")

    val difference = bound - origin
    if (difference > 0 || difference == Int.MinValue) {
      /* Either the difference did not overflow, or it is the only power of 2
       * that overflows. In both cases, use the straightforward algorithm.
       * It works for `MinValue` because the code path for powers of 2
       * basically interprets the bound as unsigned.
       */
      origin + nextIntBoundedInternal(difference)
    } else {
      /* The interval size here is greater than Int.MaxValue,
       * so the loop will exit with a probability of at least 1/2.
       */
      @tailrec
      def loop(): Int = {
        val rnd = nextInt()
        if (rnd >= origin && rnd < bound)
          rnd
        else
          loop()
      }

      loop()
    }
  }

  private def nextIntBoundedInternal(bound: Int): Int = {
    // bound > 0 || bound == Int.MinValue

    if ((bound & -bound) == bound) { // i.e., bound is a power of 2
      // > If bound is a power of two then limiting is a simple masking operation.
      nextInt() & (bound - 1)
    } else {
      /* > Otherwise, the result is re-calculated by invoking nextInt() until
       * > the result is greater than or equal zero and less than bound.
       */

      /* Taken literally, that spec would lead to huge rejection rates for
       * small bounds.
       * Instead, we start from a random 31-bit (non-negative) int `rnd`, and
       * we compute `rnd % bound`.
       * In order to get a uniform distribution, we must reject and retry if
       * we get an `rnd` that is >= the largest int multiple of `bound`.
       */

      @tailrec
      def loop(): Int = {
        val rnd = nextInt() >>> 1
        val value = rnd % bound // candidate result

        // largest multiple of bound that is <= rnd
        val multiple = rnd - value

        // if multiple + bound overflows
        if (multiple + bound < 0) {
          /* then `multiple` is the largest multiple of bound, and
           * `rnd >= multiple`, so we must retry.
           */
          loop()
        } else {
          value
        }
      }

      loop()
    }
  }

  /* The algorithms for nextLong() with bounds are copy-pasted from the ones
   * for nextInt(), mutatis mutandis.
   */

  def nextLong(bound: scala.Long): scala.Long = {
    if (bound <= 0)
      throw new IllegalArgumentException(s"Illegal bound: $bound")

    nextLongBoundedInternal(bound)
  }

  def nextLong(origin: scala.Long, bound: scala.Long): scala.Long = {
    if (bound <= origin)
      throw new IllegalArgumentException(s"Illegal bounds: [$origin, $bound)")

    val difference = bound - origin
    if (difference > 0 || difference == Long.MinValue) {
      /* Either the difference did not overflow, or it is the only power of 2
       * that overflows. In both cases, use the straightforward algorithm.
       * It works for `MinValue` because the code path for powers of 2
       * basically interprets the bound as unsigned.
       */
      origin + nextLongBoundedInternal(difference)
    } else {
      /* The interval size here is greater than Long.MaxValue,
       * so the loop will exit with a probability of at least 1/2.
       */
      @tailrec
      def loop(): scala.Long = {
        val rnd = nextLong()
        if (rnd >= origin && rnd < bound)
          rnd
        else
          loop()
      }

      loop()
    }
  }

  private def nextLongBoundedInternal(bound: scala.Long): scala.Long = {
    // bound > 0 || bound == Long.MinValue

    if ((bound & -bound) == bound) { // i.e., bound is a power of 2
      // > If bound is a power of two then limiting is a simple masking operation.
      nextLong() & (bound - 1L)
    } else {
      /* > Otherwise, the result is re-calculated by invoking nextLong() until
       * > the result is greater than or equal zero and less than bound.
       */

      /* Taken literally, that spec would lead to huge rejection rates for
       * small bounds.
       * Instead, we start from a random 63-bit (non-negative) int `rnd`, and
       * we compute `rnd % bound`.
       * In order to get a uniform distribution, we must reject and retry if
       * we get an `rnd` that is >= the largest int multiple of `bound`.
       */

      @tailrec
      def loop(): scala.Long = {
        val rnd = nextLong() >>> 1
        val value = rnd % bound // candidate result

        // largest multiple of bound that is <= rnd
        val multiple = rnd - value

        // if multiple + bound overflows
        if (multiple + bound < 0L) {
          /* then `multiple` is the largest multiple of bound, and
           * `rnd >= multiple`, so we must retry.
           */
          loop()
        } else {
          value
        }
      }

      loop()
    }
  }

// End Ported from Scala.js commit: 9cb865f dated: 2025-03-16

  def doubles(): DoubleStream =
    doubles(jl.Long.MAX_VALUE)

  def doubles(
      randomNumberOrigin: scala.Double,
      randomNumberBound: scala.Double
  ): DoubleStream = {
    RandomSupport.checkBounds(randomNumberOrigin, randomNumberBound)

    doubles(jl.Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)
  }

  def doubles(streamSize: Long): DoubleStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    /* DoubleStream.limit() can not be used because it clears some
     * required/expected upstream characteristics before passing them to
     * downstream. This is explicit JVM behavior in various Java versions.
     *
     * Just to avoid a foolish consistency, JVM (23) Jumpable stream methods
     * which specify a streamSize _do_ clear all characteristics.
     * This leads one to suspect .limit() is being called in the
     * implementation.
     */

    val rngSupplier =
      new DoubleSupplier() {
        def getAsDouble(): scala.Double =
          nextDouble()
      }

    val upstreamSpliter = DoubleStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractDoubleSpliterator(
      streamSize,
      RandomSupport.randomStreamCharacteristics
    ) {
      var nSeen = 0L
      def tryAdvance(action: DoubleConsumer): Boolean = {
        if (nSeen >= streamSize) false
        else {
          nSeen += 1
          upstreamSpliter.tryAdvance(e => action.accept(e))
        }
      }
    }

    StreamSupport.doubleStream(downstreamSpliter, parallel = false)
  }

  def doubles(
      streamSize: Long,
      randomNumberOrigin: Double,
      randomNumberBound: Double
  ): DoubleStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    RandomSupport.checkBounds(randomNumberOrigin, randomNumberBound)

    /* See comment in doubles(streamSize): about why DoubleStream.limit()
     * can not be used. TL;DR - it clears some bits expected here.
     */

    val rngSupplier =
      new DoubleSupplier() {
        def getAsDouble(): scala.Double =
          nextDouble(randomNumberOrigin, randomNumberBound)
      }

    val upstreamSpliter = DoubleStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractDoubleSpliterator(
      streamSize,
      RandomSupport.randomStreamCharacteristics
    ) {
      var nSeen = 0L
      def tryAdvance(action: DoubleConsumer): Boolean = {
        if (nSeen >= streamSize) false
        else {
          nSeen += 1
          upstreamSpliter.tryAdvance(e => action.accept(e))
        }
      }
    }

    StreamSupport.doubleStream(downstreamSpliter, parallel = false)
  }

  /*
  // Since: Java 22
   def equiDoubles(
       left: scala.Double,
       right: scala.Double,
       isLeftIncluded: Boolean,
      isRightIncluded: Boolean): DoubleStream
   */

  def ints(): IntStream =
    ints(jl.Long.MAX_VALUE)

  def ints(
      randomNumberOrigin: Int,
      randomNumberBound: Int
  ): IntStream = {
    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(RandomSupport.BAD_RANGE)

    ints(jl.Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)
  }

  def ints(streamSize: scala.Long): IntStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    val rngSupplier =
      new IntSupplier() {
        def getAsInt(): scala.Int =
          nextInt()
      }

    val upstreamSpliter = IntStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractIntSpliterator(
      streamSize,
      RandomSupport.randomStreamCharacteristics
    ) {
      var nSeen = 0L
      def tryAdvance(action: IntConsumer): Boolean = {
        if (nSeen >= streamSize) false
        else {
          nSeen += 1
          upstreamSpliter.tryAdvance(e => action.accept(e))
        }
      }
    }

    StreamSupport.intStream(downstreamSpliter, parallel = false)
  }

  def ints(
      streamSize: scala.Long,
      randomNumberOrigin: Int,
      randomNumberBound: Int
  ): IntStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(RandomSupport.BAD_RANGE)

    val rngSupplier =
      new IntSupplier() {
        def getAsInt(): scala.Int =
          nextInt(randomNumberOrigin, randomNumberBound)
      }

    val upstreamSpliter = IntStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractIntSpliterator(
      streamSize,
      RandomSupport.randomStreamCharacteristics
    ) {
      var nSeen = 0L
      def tryAdvance(action: IntConsumer): Boolean = {
        if (nSeen >= streamSize) false
        else {
          nSeen += 1
          upstreamSpliter.tryAdvance(e => action.accept(e))
        }
      }
    }

    StreamSupport.intStream(downstreamSpliter, parallel = false)
  }

  /* Java 22 - Not Yet Implemented
   * def equiDoubles(
   *     left: scala.Double,
   *     right: scala.Double,
   *     isLeftIncluded: Boolean,
   *     isRightIncluded: Boolean):  DoubleStream
   */

  /* There is a slight variance from the JDK 23 documentation here.
   * Java says that a "@Deprecated" annotation is examined and its
   * contents used here.  That is hard to do in Scala; hard as in
   * "not in economic developer time" or, more probably, as in
   * "not possible at all with current Scala Native and probably not
   * possible in current Scala itself."
   *
   * In Scala Native, algorithms declare themselves as "isDeprecated = false"
   * in the algorighm description they present to RandomGeneratorFactory.scala.
   */

  def isDeprecated() =
    true // implementations must override in order to be enabled.

  def longs(): LongStream =
    longs(jl.Long.MAX_VALUE)

  def longs(
      randomNumberOrigin: scala.Long,
      randomNumberBound: scala.Long
  ): LongStream = {
    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(RandomSupport.BAD_RANGE)

    longs(jl.Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)
  }

  def longs(streamSize: Long): LongStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    val rngSupplier =
      new LongSupplier() {
        def getAsLong(): scala.Long =
          nextLong()
      }

    val upstreamSpliter = LongStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractLongSpliterator(
      streamSize,
      RandomSupport.randomStreamCharacteristics
    ) {
      var nSeen = 0L
      def tryAdvance(action: LongConsumer): Boolean = {
        if (nSeen >= streamSize) false
        else {
          nSeen += 1
          upstreamSpliter.tryAdvance(e => action.accept(e))
        }
      }
    }

    StreamSupport.longStream(downstreamSpliter, parallel = false)
  }

  def longs(
      streamSize: Long,
      randomNumberOrigin: Long,
      randomNumberBound: Long
  ): LongStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(RandomSupport.BAD_RANGE)

    val rngSupplier =
      new LongSupplier() {
        def getAsLong(): scala.Long =
          nextLong(randomNumberOrigin, randomNumberBound)
      }

    val upstreamSpliter = LongStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractLongSpliterator(
      streamSize,
      RandomSupport.randomStreamCharacteristics
    ) {
      var nSeen = 0L
      def tryAdvance(action: LongConsumer): Boolean = {
        if (nSeen >= streamSize) false
        else {
          nSeen += 1
          upstreamSpliter.tryAdvance(e => action.accept(e))
        }
      }
    }

    StreamSupport.longStream(downstreamSpliter, parallel = false)
  }

  /** This implementation uses inverse transform sampling.
   *
   *  Java 17 through 23 state the "implementation uses McFarland's fast
   *  modified ziggurat algorithm".
   */

  def nextExponential(): scala.Double =
    -Math.log(nextDouble())

  /** This implementation uses the polar Box-Muller algorithm from Random.scala
   *  and concurrent.ThreadLocalRandom.scala
   *
   *  The Java 17 through 23 docs say the "implementation uses McFarland's fast
   *  modified ziggurat algorithm".
   *
   *  Eventually both nextExponential() and this method should use the ziggurat
   *  algorithm. Random.scala is documented as continuing to use its historical
   *  Box-Muller.
   *
   *  Providing the ziggurate algorithm here is left as an exercise for the
   *  reader.
   */

  def nextGaussian(): scala.Double = {
    var nextNextGaussian: Double = 0.0
    var haveNextNextGaussian: Boolean = false

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

    var x, y, rds: Double = 0.0

    /* Get two random numbers from -1.0 to 1.0.
     * If the radius is zero or greater than 1, throw them out and pick two new
     * ones.
     * Rejection sampling throws away about 20% of the pairs.
     */
    while ({
      x = nextDouble() * 2.0 - 1
      y = nextDouble() * 2.0 - 1
      rds = x * x + y * y
      rds == 0.0 || rds > 1
    }) ()

    val c = Math.sqrt(-2.0 * Math.log(rds) / rds)

    // Save y*c for next time
    nextNextGaussian = y * c
    haveNextNextGaussian = true

    // And return x*c
    x * c
  }

  def nextGaussian(mean: scala.Double, stddev: scala.Double): scala.Double =
    mean + (stddev * nextGaussian())
}

object RandomGenerator {

  trait ArbitrarilyJumpableGenerator extends LeapableGenerator {

    // Abstract methods
    def copy(): RandomGenerator.ArbitrarilyJumpableGenerator

    def jump(distance: scala.Long): Unit

    def jumpPowerOfTwo(logDistance: Int): Unit

    // Default methods are implemented in AbstractJumpableRandomGenerator
  }

  object ArbitrarilyJumpableGenerator {
    def of(name: String): RandomGenerator.ArbitrarilyJumpableGenerator = {
      Objects.requireNonNull(name)

      RandomGeneratorFactory
        .all()
        .filter(f => ((f.name() == name) && f.isArbitrarilyJumpable()))
        .findFirst()
        .orElseThrow(new Supplier[Exception]() {
          def get(): Exception =
            new IllegalArgumentException(
              RandomSupport
                .noSuchInterfaceMsg(name, "ArbitrarilyJumpableGenerator")
            )
        })
        .create()
        .asInstanceOf[RandomGenerator.ArbitrarilyJumpableGenerator]
    }
  }

  trait JumpableGenerator extends StreamableGenerator {

    // Abstract methods
    def copy(): RandomGenerator.JumpableGenerator

    def jump(): Unit

    def jumpDistance(): scala.Double

    // Default methods

    def copyAndJump(): RandomGenerator = {
      val copiedRng = copy()
      jump()
      copiedRng
    }

    def jumps(): Stream[RandomGenerator] = {
      val rngSupplier =
        new Supplier[RandomGenerator]() {
          def get(): RandomGenerator = {
            val copiedRng = copy()
            jump()
            copiedRng
          }
        }

      val upstreamSpliter = Stream.generate(rngSupplier).spliterator()

      val downstreamSpliter =
        new AbstractSpliterator[RandomGenerator](
          jl.Long.MAX_VALUE,
          Spliterator.IMMUTABLE //  0x400, decimal 1024
        ) {
          def tryAdvance(action: Consumer[_ >: RandomGenerator]): Boolean =
            upstreamSpliter.tryAdvance(e => action.accept(e))
        }

      StreamSupport.stream(downstreamSpliter, parallel = false)
    }

    def jumps(streamSize: scala.Long): Stream[RandomGenerator] = {
      if (streamSize < 0)
        throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

      jumps().limit(streamSize)
    }

    def rngs(): Stream[RandomGenerator] =
      jumps()

    override def rngs(streamSize: scala.Long): Stream[RandomGenerator] = {
      if (streamSize < 0)
        throw new IllegalArgumentException(RandomSupport.BAD_SIZE)
      jumps(streamSize)
    }
  }

  object JumpableGenerator {
    def of(name: String): RandomGenerator.JumpableGenerator = {
      Objects.requireNonNull(name)

      RandomGeneratorFactory
        .all()
        .filter(f => ((f.name() == name) && f.isJumpable()))
        .findFirst()
        .orElseThrow(new Supplier[Exception]() {
          def get(): Exception =
            new IllegalArgumentException(
              RandomSupport.noSuchInterfaceMsg(name, "JumpableGenerator")
            )
        })
        .create()
        .asInstanceOf[RandomGenerator.JumpableGenerator]
    }
  }

  trait LeapableGenerator extends JumpableGenerator {

    // Abstract methods

    def copy(): RandomGenerator.LeapableGenerator

    def leap(): Unit

    def leapDistance(): scala.Double

    // Default methods
    def copyAndLeap(): RandomGenerator.JumpableGenerator = {
      val copiedRng = copy()
      leap()
      copiedRng
    }

    // Some Default methods are implemented in AbstractJumpableRandomGenerator

    def leaps(): Stream[RandomGenerator.JumpableGenerator] = {
      val rngSupplier =
        new Supplier[RandomGenerator.JumpableGenerator]() {
          def get(): RandomGenerator.JumpableGenerator = {
            val copiedRng = copy()
            leap()
            copiedRng
          }
        }

      val upstreamSpliter = Stream.generate(rngSupplier).spliterator()

      val downstreamSpliter =
        new AbstractSpliterator[RandomGenerator.JumpableGenerator](
          jl.Long.MAX_VALUE,
          Spliterator.IMMUTABLE //  0x400, decimal 1024
        ) {
          var nSeen = 0L

          def tryAdvance(
              action: Consumer[_ >: RandomGenerator.JumpableGenerator]
          ): Boolean = {
            upstreamSpliter.tryAdvance(e => action.accept(e))
          }
        }

      StreamSupport.stream(downstreamSpliter, parallel = false)
    }

    def leaps(
        streamSize: scala.Long
    ): Stream[RandomGenerator.JumpableGenerator] = {
      if (streamSize < 0)
        throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

      leaps().limit(streamSize)
    }

  }

  object LeapableGenerator {
    def of(name: String): RandomGenerator.LeapableGenerator = {
      Objects.requireNonNull(name)

      RandomGeneratorFactory
        .all()
        .filter(f => ((f.name() == name) && f.isLeapable()))
        .findFirst()
        .orElseThrow(new Supplier[Exception]() {
          def get(): Exception =
            new IllegalArgumentException(
              RandomSupport.noSuchInterfaceMsg(name, "LeapableGenerator")
            )
        })
        .create()
        .asInstanceOf[RandomGenerator.LeapableGenerator]
    }
  }

  trait SplittableGenerator extends StreamableGenerator {

    /* rngs(streamSize) is overridden in AbstractSplittableRandomGenerator
     * to provide the expected stream characteristics, which differ from
     * those of splits(). The rngs(streamSize) call here will never happen
     * for the LXM generators, which extend that Abstract class.
     * Circuitous but effective.
     */

    def rngs(): Stream[RandomGenerator] =
      rngs(jl.Long.MAX_VALUE)

    override def rngs(streamSize: scala.Long): Stream[RandomGenerator] =
      splits(streamSize).asInstanceOf[Stream[RandomGenerator]]

    def split(): RandomGenerator.SplittableGenerator

    def split(
        source: RandomGenerator.SplittableGenerator
    ): RandomGenerator.SplittableGenerator

    def splits(): Stream[RandomGenerator.SplittableGenerator] =
      splits(this)

    def splits(
        streamSize: scala.Long
    ): Stream[RandomGenerator.SplittableGenerator]

    def splits(
        streamSize: scala.Long,
        source: RandomGenerator.SplittableGenerator
    ): Stream[RandomGenerator.SplittableGenerator]

    def splits(
        source: RandomGenerator.SplittableGenerator
    ): Stream[RandomGenerator.SplittableGenerator]
  }

  object SplittableGenerator {
    def of(name: String): RandomGenerator.SplittableGenerator = {
      Objects.requireNonNull(name)

      RandomGeneratorFactory
        .all()
        .filter(f => ((f.name() == name) && f.isSplittable()))
        .findFirst()
        .orElseThrow(new Supplier[Exception]() {
          def get(): Exception =
            new IllegalArgumentException(
              RandomSupport.noSuchInterfaceMsg(name, "SplittableGenerator")
            )
        })
        .create()
        .asInstanceOf[RandomGenerator.SplittableGenerator]
    }
  }

  trait StreamableGenerator extends RandomGenerator {

    def rngs(): Stream[RandomGenerator] // Abstract

    def rngs(streamSize: scala.Long): Stream[RandomGenerator] =
      rngs().limit(streamSize)
  }

  object StreamableGenerator {
    def of(name: String): RandomGenerator.StreamableGenerator = {
      Objects.requireNonNull(name)

      RandomGeneratorFactory
        .all()
        .filter(f => ((f.name() == name) && f.isStreamable()))
        .findFirst()
        .orElseThrow(new Supplier[Exception]() {
          def get(): Exception =
            new IllegalArgumentException(
              RandomSupport.noSuchInterfaceMsg(name, "StreamableGenerator")
            )
        })
        .create()
        .asInstanceOf[RandomGenerator.StreamableGenerator]
    }
  }

  def getDefault(): RandomGenerator = {
    /* per JVM 22, August 2024:
     *	"The default implementation selects L32X64MixRandom.
     *	but default is subject to change."
     *
     * JVM 23 omits specifying the algorithm.
     */
    RandomGenerator.of("L32X64MixRandom")
  }

  def of(name: String): RandomGenerator = {
    Objects.requireNonNull(name)

    RandomGeneratorFactory.of(name).create()
  }
}

private[util] object RandomSupport {
  // The elements of the stream are random, not the Characteristics themselves.
  final val randomStreamCharacteristics =
    Spliterator.SIZED | Spliterator.SUBSIZED |
      Spliterator.NONNULL | Spliterator.IMMUTABLE //  0x4540, decimal 17728

  // IllegalArgumentException messages, from ThreadLocalRandom.scala. match JVM
  final val BAD_RANGE = "bound must be greater than origin"
  final val BAD_SIZE = "size must be non-negative"

  // IllegalArgumentException messages to match JVM util.random
  final val BAD_FLOATING_POINT_BOUND = "bound must be finite and positive"

  /* JVM 23 and probably earlier uses the "bound must be greater than origin"
   * message if this argument check fails.
   * This is appropriate in the (bound <= offset) but at least two
   * conditions lead to counter-factual nonsense:
   *   rng.nextDouble(Double.NEGATIVE_INFINITY, 5.0)  // bound is > origin
   *   rng.nextDouble(1.0 , Double.POSITIVE_INFINITY) // bound is > origin
   *
   * Cases where either or both arguments are Double.NaN give the
   * same message instead of reporting NaN. Technically correct but
   * less than helpful.
   *
   * These two methods handle NaNs invisibly. Recall
   * jl.Double.isFinite(scala.NaN) == false.
   */

  def checkBounds(origin: scala.Double, bound: scala.Double): Unit = {
    if (!jl.Double.isFinite(origin) || !jl.Double.isFinite(bound) ||
        (origin >= bound))
      throw new IllegalArgumentException(RandomSupport.BAD_RANGE)
  }

  def checkBounds(origin: scala.Float, bound: scala.Float): Unit = {
    if (!jl.Float.isFinite(origin) || !jl.Float.isFinite(bound) ||
        (origin >= bound))
      throw new IllegalArgumentException(RandomSupport.BAD_RANGE)
  }

  def noSuchInterfaceMsg(
      algName: String,
      interfaceName: String
  ): String = {
    val doubleQuote = '\u0022' // Scala 2 awkwardness
    "The random number generator algorithm" +
      s"${doubleQuote}${algName}${doubleQuote}" +
      "is not implemented with the interface" +
      s"${doubleQuote}${interfaceName}${doubleQuote}"
  }

  private def getEntropyLongs(nLongs: Int): Array[Long] = {

    def getEntropyTlr(buffer: Array[Long]): Array[Long] = {
      val currentThread = java.util.concurrent.ThreadLocalRandom.current()
      for (j <- 0 until buffer.size)
        buffer(j) = currentThread.nextLong()

      buffer
    }

    val buffer = new Array[Long](nLongs)

    // No getentropy() in Linux used in Scala Native CI on 32 bit platforms.
    if (LinktimeInfo.isWindows || LinktimeInfo.is32BitPlatform) {
      getEntropyTlr(buffer)
    } else { // Linux, macOs. Should work but is untested on *BSD.
      val err =
        unistd.getentropy(buffer.at(0), buffer.size.toUInt * sizeof[Long])
      if (err == 0) buffer
      else getEntropyTlr(buffer) // Punt!
    }
  }

  def getEntropy2x64(): Array[Long] =
    getEntropyLongs(2)

  def getEntropy4x32(): Array[Int] = {
    val longs = getEntropyLongs(2)
    val buffer = new Array[Int](4)

    /* There is probably a more elegant way of doing this than inchworming
     * along.
     * Calling common code during development & infancy is well worth
     * the inelegance & inefficiency.
     */
    for (j <- 0 until longs.size) {
      val idx = j * 2
      buffer(idx + 0) = (longs(j) & 0x00000000ffffffffL).toInt
      buffer(idx + 1) = ((longs(j) & 0xffffffff00000000L) >>> 32).toInt
    }

    buffer
  }

  def getEntropy4x64(): Array[Long] =
    getEntropyLongs(4)

  private def getVignaInitialState(seed: Long, nLongs: Int): Array[Long] = {
    /* Choose an implementation which guarantees that sequences match
     * when given the same seed twice, or more.
     */

    val rng = new SplittableRandom(seed)
    val result = new Array[scala.Long](nLongs)

    for (j <- 0 until result.length)
      result(j) = rng.nextLong()

    result
  }

  def getVignaInitialState2x64(seed: Long): Array[Long] =
    getVignaInitialState(seed, 2)

  def getVignaInitialState4x64(seed: Long): Array[Long] =
    getVignaInitialState(seed, 4)

}

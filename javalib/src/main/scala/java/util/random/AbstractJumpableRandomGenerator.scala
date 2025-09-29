package java.util.random

import java.util.Spliterators._
import java.util.function._
import java.util.stream._
import java.util.{Spliterator, Spliterators}
import java.{lang => jl}

/* This apparent duplication & proliferation of RandomGenerator stream
 * methods springs from fact that the characteristics for the Streams
 * produced on the JVM for JumpableRandomGenerator methods returning a Stream
 * differ from those of the parent class. Use overrides to maintain consistency
 * with JVM practice.
 */

private trait AbstractJumpableRandomGenerator
    extends RandomGenerator.JumpableGenerator {

  final val jumpableCharacteristics =
    Spliterator.IMMUTABLE //  0x400, decimal 1024

  override def doubles(): DoubleStream = {
    val rngSupplier =
      new DoubleSupplier() {
        def getAsDouble(): scala.Double =
          nextDouble()
      }

    val upstreamSpliter = DoubleStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractDoubleSpliterator(
      jl.Long.MAX_VALUE, // since known not SIZED
      jumpableCharacteristics
    ) {

      def tryAdvance(action: DoubleConsumer): Boolean =
        upstreamSpliter.tryAdvance(e => action.accept(e))
    }

    StreamSupport.doubleStream(downstreamSpliter, parallel = false)
  }

  override def doubles(
      randomNumberOrigin: scala.Double,
      randomNumberBound: scala.Double
  ): DoubleStream = {
    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(RandomSupport.BAD_RANGE)

    val rngSupplier =
      new DoubleSupplier() {
        def getAsDouble(): scala.Double =
          nextDouble(randomNumberOrigin, randomNumberBound)
      }

    val upstreamSpliter = DoubleStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractDoubleSpliterator(
      jl.Long.MAX_VALUE,
      jumpableCharacteristics
    ) {

      def tryAdvance(action: DoubleConsumer): Boolean =
        upstreamSpliter.tryAdvance(e => action.accept(e))
    }

    StreamSupport.doubleStream(downstreamSpliter, parallel = false)
  }

  override def doubles(streamSize: scala.Long): DoubleStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    // .limit() also sets expected characteristics to the required 0
    doubles().limit(streamSize)
  }

  override def doubles(
      streamSize: scala.Long,
      randomNumberOrigin: Double,
      randomNumberBound: Double
  ): DoubleStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    // .limit() also sets expected characteristics to the required 0
    doubles(randomNumberOrigin, randomNumberBound).limit(streamSize)
  }

  override def ints(): IntStream = {
    val rngSupplier =
      new IntSupplier() {
        def getAsInt(): scala.Int =
          nextInt()
      }

    val upstreamSpliter = IntStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractIntSpliterator(
      jl.Long.MAX_VALUE, // since known not SIZED
      jumpableCharacteristics
    ) {

      def tryAdvance(action: IntConsumer): Boolean =
        upstreamSpliter.tryAdvance(e => action.accept(e))
    }

    StreamSupport.intStream(downstreamSpliter, parallel = false)
  }

  override def ints(
      randomNumberOrigin: scala.Int,
      randomNumberBound: scala.Int
  ): IntStream = {
    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(RandomSupport.BAD_RANGE)

    val rngSupplier =
      new IntSupplier() {
        def getAsInt(): scala.Int =
          nextInt(randomNumberOrigin, randomNumberBound)
      }

    val upstreamSpliter = IntStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractIntSpliterator(
      jl.Long.MAX_VALUE,
      jumpableCharacteristics
    ) {

      def tryAdvance(action: IntConsumer): Boolean =
        upstreamSpliter.tryAdvance(e => action.accept(e))
    }

    StreamSupport.intStream(downstreamSpliter, parallel = false)
  }

  override def ints(streamSize: scala.Long): IntStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    // .limit() also sets expected characteristics to required 0
    ints().limit(streamSize)
  }

  override def ints(
      streamSize: scala.Long,
      randomNumberOrigin: Int,
      randomNumberBound: Int
  ): IntStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    // .limit() also sets expected characteristics to required 0
    ints(randomNumberOrigin, randomNumberBound).limit(streamSize)
  }

  // longs()
  override def longs(): LongStream = {
    val rngSupplier =
      new LongSupplier() {
        def getAsLong(): scala.Long =
          nextLong()
      }

    val upstreamSpliter = LongStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractLongSpliterator(
      jl.Long.MAX_VALUE, // since known not SIZED
      jumpableCharacteristics
    ) {

      def tryAdvance(action: LongConsumer): Boolean =
        upstreamSpliter.tryAdvance(e => action.accept(e))
    }

    StreamSupport.longStream(downstreamSpliter, parallel = false)
  }

  override def longs(
      randomNumberOrigin: scala.Long,
      randomNumberBound: scala.Long
  ): LongStream = {
    if (randomNumberOrigin >= randomNumberBound)
      throw new IllegalArgumentException(RandomSupport.BAD_RANGE)

    val rngSupplier =
      new LongSupplier() {
        def getAsLong(): scala.Long =
          nextLong(randomNumberOrigin, randomNumberBound)
      }

    val upstreamSpliter = LongStream.generate(rngSupplier).spliterator()

    val downstreamSpliter = new AbstractLongSpliterator(
      jl.Long.MAX_VALUE,
      jumpableCharacteristics
    ) {

      def tryAdvance(action: LongConsumer): Boolean =
        upstreamSpliter.tryAdvance(e => action.accept(e))
    }

    StreamSupport
      .longStream(downstreamSpliter, parallel = false)
  }

  override def longs(streamSize: Long): LongStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    // .limit() also sets expected characteristics to required 0
    longs().limit(streamSize)
  }

  override def longs(
      streamSize: Long,
      randomNumberOrigin: Long,
      randomNumberBound: Long
  ): LongStream = {
    if (streamSize < 0)
      throw new IllegalArgumentException(RandomSupport.BAD_SIZE)

    // .limit() also sets expected characteristics to required 0
    longs(randomNumberOrigin, randomNumberBound).limit(streamSize)
  }
}

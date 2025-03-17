package java.util.random

import java.{lang => jl}

import java.util.{Spliterator, Spliterators}
import java.util.Spliterators.AbstractSpliterator

import java.util.function.{Consumer, Supplier}

import java.util.stream.{Stream, StreamSupport}

private[util] trait AbstractSplittableRandomGenerator
    extends RandomGenerator.SplittableGenerator {

  private def splitsImpl(
      streamSize: scala.Long,
      source: RandomGenerator.SplittableGenerator,
      characteristics: Int
  ): Stream[RandomGenerator.SplittableGenerator] = {
    /* On the JVM, spliterators for streams returned by rngs() and
     * splits() are different. On Scala Native, the code to create the streams
     * is common and consolidated here, ensuring the proper
     * characteristics are used to create each kind of method.
     */

    val rngSupplier =
      new Supplier[RandomGenerator.SplittableGenerator]() {
        def get(): RandomGenerator.SplittableGenerator =
          split(source)
      }

    val upstreamSpliter = Stream.generate(rngSupplier).spliterator()

    val downstreamSpliter =
      new AbstractSpliterator[RandomGenerator.SplittableGenerator](
        streamSize,
        characteristics
      ) {
        var nSeen = 0L

        def tryAdvance(
            action: Consumer[_ >: RandomGenerator.SplittableGenerator]
        ): Boolean = {
          if (nSeen >= streamSize) false
          else {
            nSeen += 1
            upstreamSpliter.tryAdvance(e => action.accept(e))
          }
        }
      }

    StreamSupport.stream(downstreamSpliter, parallel = false)
  }

  // Members declared in java.util.random.RandomGenerator.StreamableGenerator

  override def rngs(
      streamSize: scala.Long
  ): Stream[RandomGenerator] = {
    val rngsCharacteristics =
      Spliterator.SIZED | Spliterator.SUBSIZED //  0x4040, decimal 16448

    splitsImpl(streamSize, this, rngsCharacteristics)
      .asInstanceOf[Stream[RandomGenerator]]
  }

  // Members declared in java.util.random.RandomGenerator.SplittableGenerator

  def split(): RandomGenerator.SplittableGenerator

  def split(
      source: RandomGenerator.SplittableGenerator
  ): RandomGenerator.SplittableGenerator =
    source.split()

  // The elements of the stream are random, not the Characteristics themselves.
  final val randomStreamCharacteristics =
    Spliterator.SIZED | Spliterator.SUBSIZED |
      Spliterator.NONNULL | Spliterator.IMMUTABLE //  0x4540, decimal 17728

  def splits(
      streamSize: scala.Long
  ): Stream[RandomGenerator.SplittableGenerator] =
    splits(streamSize, this)

  def splits(
      streamSize: scala.Long,
      source: RandomGenerator.SplittableGenerator
  ): Stream[RandomGenerator.SplittableGenerator] =
    splitsImpl(streamSize, source, RandomSupport.randomStreamCharacteristics)

  def splits(
      source: RandomGenerator.SplittableGenerator
  ): Stream[RandomGenerator.SplittableGenerator] =
    splits(jl.Long.MAX_VALUE, source)

}

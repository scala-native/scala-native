package java.util

import java.util.function.*

import Spliterator.*

object Spliterator {
  final val DISTINCT = 0x00000001
  final val SORTED = 0x00000004
  final val ORDERED = 0x00000010
  final val SIZED = 0x00000040
  final val NONNULL = 0x00000100
  final val IMMUTABLE = 0x00000400
  final val CONCURRENT = 0x00001000
  final val SUBSIZED = 0x00004000

  trait OfPrimitive[
      T,
      T_CONS,
      T_SPLITR <: Spliterator.OfPrimitive[T, T_CONS, T_SPLITR]
  ] extends Spliterator[T] {
    override def trySplit(): T_SPLITR
    def tryAdvance(action: T_CONS): Boolean
    def forEachRemaining(action: T_CONS): Unit = {
      while (tryAdvance(action)) ()
    }
  }

  trait OfInt
      extends OfPrimitive[java.lang.Integer, IntConsumer, Spliterator.OfInt] {
    override def trySplit(): OfInt
    override def tryAdvance(action: IntConsumer): Boolean
    override def forEachRemaining(action: IntConsumer): Unit =
      while (tryAdvance(action)) ()
    override def tryAdvance(action: Consumer[? >: Integer]): Boolean =
      action match {
        case action: IntConsumer => tryAdvance(action: IntConsumer)
        case _                   => tryAdvance((action.accept(_)): IntConsumer)
      }
    override def forEachRemaining(action: Consumer[? >: Integer]): Unit =
      action match {
        case action: IntConsumer => forEachRemaining(action: IntConsumer)
        case _ => forEachRemaining((action.accept(_)): IntConsumer)
      }

  }
  trait OfLong
      extends OfPrimitive[java.lang.Long, LongConsumer, Spliterator.OfLong] {
    override def trySplit(): OfLong
    override def tryAdvance(action: LongConsumer): Boolean
    override def forEachRemaining(action: LongConsumer): Unit =
      while (tryAdvance(action)) ()
    override def tryAdvance(action: Consumer[? >: java.lang.Long]): Boolean =
      action match {
        case action: LongConsumer => tryAdvance(action: LongConsumer)
        case _ => tryAdvance((action.accept(_)): LongConsumer)
      }
    override def forEachRemaining(action: Consumer[? >: java.lang.Long]): Unit =
      action match {
        case action: LongConsumer => forEachRemaining(action: LongConsumer)
        case _ => forEachRemaining((action.accept(_)): LongConsumer)
      }
  }
  trait OfDouble
      extends OfPrimitive[
        java.lang.Double,
        DoubleConsumer,
        Spliterator.OfDouble
      ] {
    override def trySplit(): OfDouble
    override def tryAdvance(action: DoubleConsumer): Boolean
    override def forEachRemaining(action: DoubleConsumer): Unit =
      while (tryAdvance(action)) ()
    override def tryAdvance(action: Consumer[? >: java.lang.Double]): Boolean =
      action match {
        case action: DoubleConsumer => tryAdvance(action: DoubleConsumer)
        case _ => tryAdvance((action.accept(_)): DoubleConsumer)
      }
    override def forEachRemaining(
        action: Consumer[? >: java.lang.Double]
    ): Unit =
      action match {
        case action: DoubleConsumer => forEachRemaining(action: DoubleConsumer)
        case _ => forEachRemaining((action.accept(_)): DoubleConsumer)
      }
  }

}

trait Spliterator[T] {

  def characteristics(): Int

  def estimateSize(): Long

  def forEachRemaining(action: Consumer[? >: T]): Unit =
    while (tryAdvance(action)) {}

  def getComparator(): Comparator[? >: T] = throw new IllegalStateException()

  def getExactSizeIfKnown(): Long =
    if (hasCharacteristics(SIZED)) estimateSize() else -1L

  def hasCharacteristics(chars: Int): Boolean =
    (characteristics() & chars) == chars

  def tryAdvance(action: Consumer[? >: T]): Boolean

  def trySplit(): Spliterator[T]

}

package java.util

import java.util.function.Consumer

import Spliterator._

object Spliterator {
  final val DISTINCT = 0x00000001
  final val SORTED = 0x00000004
  final val ORDERED = 0x00000010
  final val SIZED = 0x00000040
  final val NONNULL = 0x00000100
  final val IMMUTABLE = 0x00000400
  final val CONCURRENT = 0x00001000
  final val SUBSIZED = 0x00004000
}

trait Spliterator[T] {

  def characteristics(): Int

  def estimateSize(): Long

  def forEachRemaining(action: Consumer[_ >: T]): Unit =
    while (tryAdvance(action)) {}

  def getComparator(): Comparator[_ >: T] = throw new IllegalStateException()

  def getExactSizeIfKnown(): Long =
    if (hasCharacteristics(SIZED)) estimateSize() else -1L

  def hasCharacteristics(chars: Int): Boolean =
    (characteristics() & chars) == chars

  def tryAdvance(action: Consumer[_ >: T]): Boolean

  def trySplit(): Spliterator[T]

}

package java.util.stream

import java.util.{Collections, HashSet, Set}
import java.util.function._

trait Collector[T, A, R] {

  def accumulator(): BiConsumer[A, T]

  def characteristics(): Set[Collector.Characteristics]

  def combiner(): BinaryOperator[A]

  def finisher(): Function[A, R]

  def supplier(): Supplier[A]
}

object Collector {
  sealed class Characteristics(name: String, ordinal: Int)
      extends _Enum[Characteristics](name, ordinal) {
    override def toString() = this.name
  }

  object Characteristics {
    final val CONCURRENT = new Characteristics("CONCURRENT", 0)
    final val UNORDERED = new Characteristics("UNORDERED", 1)
    final val IDENTITY_FINISH = new Characteristics("IDENTITY_FINISH", 2)

    private[this] val cachedValues =
      Array(CONCURRENT, IDENTITY_FINISH, UNORDERED)

    def values(): Array[Characteristics] = cachedValues.clone()

    def valueOf(name: String): Characteristics = {
      cachedValues.find(_.name() == name).getOrElse {
        throw new IllegalArgumentException(
          s"No enum const Collector.Characteristics. ${name}"
        )
      }
    }
  }

  private def createCharacteristicsSet(
      addIdentity: Boolean,
      ccs: Collector.Characteristics*
  ): Set[Collector.Characteristics] = {
    val hs = new HashSet[Collector.Characteristics]()

    if (addIdentity)
      hs.add(Characteristics.IDENTITY_FINISH)

    for (c <- ccs)
      hs.add(c)

    Collections.unmodifiableSet(hs)
  }

  def of[T, A, R](
      _supplier: Supplier[A],
      _accumulator: BiConsumer[A, T],
      _combiner: BinaryOperator[A],
      _finisher: Function[A, R], // Note trailing comma
      _characteristics: Collector.Characteristics*
  ): Collector[T, A, R] = {
    new Collector[T, A, R] {
      def accumulator(): BiConsumer[A, T] = _accumulator

      def characteristics(): Set[Collector.Characteristics] =
        createCharacteristicsSet(false, _characteristics: _*)

      def combiner(): BinaryOperator[A] = _combiner

      def finisher(): Function[A, R] = _finisher

      def supplier(): Supplier[A] = _supplier
    }
  }

  def of[T, R](
      _supplier: Supplier[R],
      _accumulator: BiConsumer[R, T],
      _combiner: BinaryOperator[R],
      _characteristics: Collector.Characteristics*
  ): Collector[T, R, R] = {
    new Collector[T, R, R] {
      def accumulator(): BiConsumer[R, T] = _accumulator

      def characteristics(): Set[Collector.Characteristics] =
        createCharacteristicsSet(true, _characteristics: _*)

      def combiner(): BinaryOperator[R] = _combiner

      def finisher(): Function[R, R] = (r: R) => r

      def supplier(): Supplier[R] = _supplier
    }
  }
}

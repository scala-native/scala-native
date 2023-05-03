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

  /* Design Note:
   *   enums are a tangle in each of Java and Scala.
   *   To make things more fun, enum changes between Scala 2 and Scala 3.
   *
   *   Scala Native currently supports both Scala 2 & 3. There are mechanisms
   *   for handling code which must vary by Scala version.
   *
   *   This is an acknowledgedly incomplete & inaccurate Scala 2 style
   *   emulation of the parts of the Java Collector.Characteristics
   *   enum needed to implement the Collectors class. It is intended to
   *   hit the Pareto point of benefit/cost.
   *
   *   Of course, somebody, somewhere, is going to use one of the features
   *   not emulated.  Then it will be time for a better iteration.
   */

  case class Characteristics(name: String)

  object Characteristics {

    final val CONCURRENT = new Characteristics("CONCURRENT")
    final val IDENTITY_FINISH = new Characteristics("IDENTITIY_FINISH")
    final val UNORDERED = new Characteristics("UNORDERED")

    def valueOf(name: String): Collector.Characteristics = {
      name match {
        case nom if (nom == "CONCURRENT") => Characteristics.CONCURRENT
        case nom if (nom == "UNORDERED")  => Characteristics.UNORDERED
        case nom if (nom == "IDENTITY_FINISH") =>
          Characteristics.IDENTITY_FINISH
        case _ =>
          val missingName =
            s"java.util.stream.Collector.Characteristics.${name}"
          throw new IllegalArgumentException(s"No enum constant ${missingName}")
      }
    }

    def values(): Array[Collector.Characteristics] = {
      // Fill the array in the same order as shown by the JVM.
      // Since an Array is mutable, create & fill on each call, rather than
      // returning a static array.
      val va = new Array[Collector.Characteristics](3)
      va(0) = Characteristics.CONCURRENT
      va(1) = Characteristics.UNORDERED
      va(2) = Characteristics.IDENTITY_FINISH

      va
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

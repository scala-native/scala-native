// Ported from Scala.js, commit sha:7b4e8a80b dated:2022-12-06
package java.util.function

@FunctionalInterface
trait IntPredicate { self =>
  def test(t: Int): Boolean

  def and(other: IntPredicate): IntPredicate = {
    new IntPredicate {
      def test(value: Int): Boolean =
        self.test(value) && other.test(value) // the order and short-circuit are by-spec
    }
  }

  def negate(): IntPredicate = {
    new IntPredicate {
      def test(value: Int): Boolean =
        !self.test(value)
    }
  }

  def or(other: IntPredicate): IntPredicate = {
    new IntPredicate {
      def test(value: Int): Boolean =
        self.test(value) || other.test(value) // the order and short-circuit are by-spec
    }
  }
}
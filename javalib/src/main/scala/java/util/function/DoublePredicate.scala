// Ported from Scala.js, commit sha:7b4e8a80b dated:2022-12-06
package java.util.function

@FunctionalInterface
trait DoublePredicate { self =>
  def test(t: Double): Boolean

  def and(other: DoublePredicate): DoublePredicate = {
    new DoublePredicate {
      def test(value: Double): Boolean =
        self.test(value) && other.test(value) // the order and short-circuit are by-spec
    }
  }

  def negate(): DoublePredicate = {
    new DoublePredicate {
      def test(value: Double): Boolean =
        !self.test(value)
    }
  }

  def or(other: DoublePredicate): DoublePredicate = {
    new DoublePredicate {
      def test(value: Double): Boolean =
        self.test(value) || other.test(value) // the order and short-circuit are by-spec
    }
  }
}
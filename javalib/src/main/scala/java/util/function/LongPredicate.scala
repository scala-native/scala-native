// Ported from Scala.js, commit sha:7b4e8a80b dated:2022-12-06
package java.util.function

@FunctionalInterface
trait LongPredicate { self =>
  def test(t: Long): Boolean

  def and(other: LongPredicate): LongPredicate = {
    new LongPredicate {
      def test(value: Long): Boolean =
        self.test(value) && other.test(value) // the order and short-circuit are by-spec
    }
  }

  def negate(): LongPredicate = {
    new LongPredicate {
      def test(value: Long): Boolean =
        !self.test(value)
    }
  }

  def or(other: LongPredicate): LongPredicate = {
    new LongPredicate {
      def test(value: Long): Boolean =
        self.test(value) || other.test(value) // the order and short-circuit are by-spec
    }
  }
}
package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class NextParserTest extends FunSuite {
  val local = Local(1)

  Seq[Next](
    Next.Unwind(local),
    Next.Case(Val.None, local),
    Next.Label(local, Seq.empty)
  ).zipWithIndex.foreach {
    case (next, idx) =>
      test(s"parse next `${next.show}`") {
        val Parsed.Success(result, _) = parser.Next.parser.parse(next.show)
        assert(result == next)
      }
  }
}

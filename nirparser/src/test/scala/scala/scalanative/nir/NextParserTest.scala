package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class NextParserTest extends FunSuite {
  val local = Local(1)
  val value = Val.Int(42)
  val exc   = Val.Local(local, nir.Rt.Object)

  Seq[Next](
    Next.Unwind(exc, Next.Label(local, Seq.empty)),
    Next.Unwind(exc, Next.Label(local, Seq(value))),
    Next.Unwind(exc, Next.Label(local, Seq(value, value))),
    Next.Case(value, Next.Label(local, Seq.empty)),
    Next.Case(value, Next.Label(local, Seq(value))),
    Next.Case(value, Next.Label(local, Seq(value, value))),
    Next.Label(local, Seq.empty),
    Next.Label(local, Seq(value)),
    Next.Label(local, Seq(value, value))
  ).zipWithIndex.foreach {
    case (next, idx) =>
      test(s"parse next `${next.show}`") {
        val Parsed.Success(result, _) = parser.Next.parser.parse(next.show)
        assert(result == next)
      }
  }
}

package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class NextParserTest extends FlatSpec with Matchers {

  val local = Local("test", 1)

  it should "parse `Next.Unwind`" in {
    val fail: Next                = Next.Unwind(local)
    val Parsed.Success(result, _) = parser.Next.Unwind.parse(fail.show)
    result should be(fail)
  }

  it should "parse `Next.Case`" in {
    val `case`: Next              = Next.Case(Val.None, local)
    val Parsed.Success(result, _) = parser.Next.Case.parse(`case`.show)
    result should be(`case`)
  }

  it should "parse `Next.Label`" in {
    val label: Next               = Next.Label(local, Seq.empty)
    val Parsed.Success(result, _) = parser.Next.Label.parse(label.show)
    result should be(label)
  }
}

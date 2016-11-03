package scala.scalanative
package nir

import util.sh
import Shows._

import fastparse.all.Parsed
import org.scalatest._

class NextParserTest extends FlatSpec with Matchers {

  val local = Local("test", 1)

  "The NIR parser" should "parse `Next.Succ`" in {
    val succ: Next = Next.Succ(local)
    val Parsed.Success(result, _) =
      parser.Next.Succ.parse(sh"$succ".toString)
    result should be(succ)
  }

  it should "parse `Next.Fail`" in {
    val fail: Next = Next.Fail(local)
    val Parsed.Success(result, _) =
      parser.Next.Fail.parse(sh"$fail".toString)
    result should be(fail)
  }

  it should "parse `Next.Case`" in {
    val `case`: Next = Next.Case(Val.None, local)
    val Parsed.Success(result, _) =
      parser.Next.Case.parse(sh"${`case`}".toString)
    result should be(`case`)
  }

  it should "parse `Next.Label`" in {
    val label: Next = Next.Label(local, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Next.Label.parse(sh"$label".toString)
    result should be(label)
  }

}

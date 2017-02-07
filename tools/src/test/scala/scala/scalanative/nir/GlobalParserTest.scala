package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class GlobalParserTest extends FlatSpec with Matchers {

  "The NIR parser" should "parse `Global.Top`" in {
    val top: Global               = Global.Top("java.lang.String")
    val Parsed.Success(result, _) = parser.Global.Top.parse(top.show)
    result should be(top)
  }

  it should "parse `Global.Member`" in {
    val member: Global            = Global.Top("java.lang.String") member "foobar"
    val Parsed.Success(result, _) = parser.Global.Member.parse(member.show)
    result should be(member)
  }

  it should "parse a global with a decoded name" in {
    val global =
      """@scala.collection.TraversableOnce::/:_class.java.lang.Object_trait.scala.Function2_class.java.lang.Object"""
    val Parsed.Success(result, _) = parser.Global(global)
    result.show should be(global)
  }
}

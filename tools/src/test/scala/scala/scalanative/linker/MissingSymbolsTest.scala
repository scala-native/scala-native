package scala.scalanative.linker

import scala.scalanative.checker.Check
import scala.scalanative.LinkerSpec

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.nir._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.scalanative.optimizer.assertContainsAll
import java.sql.Time

class MissingSymbolsTest extends LinkerSpec {
  private val mainClass = "Test"
  private val sourceFile = "Test.scala"

  @Test def missingSymbolStacktrace(): Unit = {
    doesNotLink(
      entry = mainClass,
      Map(sourceFile -> s"""
        |object Foo{
        |  def getTimeString(): String = Bar.getTimeString()
        |}
        |
        |object Bar{
        |  def getTimeString(): String = {
        |    val time = java.sql.Time.valueOf("")
        |    val time2 = new java.sql.Time(0L)
        |    ???
        |  }
        |}
        |
        |object $mainClass{
        | def main(args: Array[String]): Unit = {
        |   val unreachable = Foo.getTimeString()
        | }
        |}
        """.stripMargin)
    ) {
      case (config, result) =>
        assertEquals("unreachable", 3, result.unreachable.size)
        assertContainsAll(
          "kind-symbols",
          Seq(
            "type" -> "java.sql.Time",
            "constructor" -> "java.sql.Time(long)",
            "method" -> "java.sql.Time.valueOf"
          ),
          result.unreachable.map(v => (v.kind, v.symbol))
        )
        val TimeType = Global.Top("java.sql.Time")
        val TimeCtor = TimeType.member(Sig.Ctor(Seq(Type.Long)))
        val TimeValueOf = TimeType.member(
          Sig.Method(
            "valueOf",
            Seq(Rt.String, Type.Ref(TimeType)),
            Sig.Scope.PublicStatic
          )
        )
        assertContainsAll(
          "names",
          Seq(TimeType, TimeCtor, TimeValueOf),
          result.unreachable.map(_.name)
        )

        result.unreachable.foreach { symbol =>
          val backtrace =
            symbol.backtrace.map(v => (v.kind, v.symbol, v.filename, v.line))
          // format: off
          assertEquals("backtrace", List(
            ("method", "Bar$.getTimeString", sourceFile, if(symbol.name == TimeCtor) 9 else 8),
            ("method", "Foo$.getTimeString", sourceFile, 3),
            ("method", "Test$.main", sourceFile, 16),
            ("method", "Test.main", sourceFile, 15)
          ), backtrace)
          // format: on
        }
    }
  }

  @Test def unreachableInTypeParent(): Unit = {
    doesNotLink(
      entry = mainClass,
      Map(sourceFile -> s"""
        |object $mainClass{
        | def main(args: Array[String]): Unit = {
        |   class Foo(n: Long) extends java.sql.Time(n)
        |   val x = new Foo(0L)
        | }
        |}
        """.stripMargin)
    ) {
      case (config, result) =>
        scala.scalanative.build.ScalaNative.logLinked(config, result, "test")
        assertEquals("unreachable", 2, result.unreachable.size)
        assertContainsAll(
          "kind-symbols",
          Seq(
            "type" -> "java.sql.Time",
            "constructor" -> "java.sql.Time(long)"
          ),
          result.unreachable.map(v => (v.kind, v.symbol))
        )

        result.unreachable
          .find(_.symbol == "java.sql.Time")
          .map { symbol =>
            val from = symbol.backtrace.head
            assertEquals("type", from.kind)
            assertEquals("Test$Foo$1", from.symbol)
          }
          .getOrElse(fail("Not found required unreachable symbol"))
    }
  }

}

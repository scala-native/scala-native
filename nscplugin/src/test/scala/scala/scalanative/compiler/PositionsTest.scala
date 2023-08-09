package scala.scalanative
package compiler

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.StaticForwardersSuite.compileAndLoad
import scala.scalanative.buildinfo.ScalaNativeBuildInfo._

class PositionsTest {

  @Test def sourcePositions(): Unit = compileAndLoad(
    "Test.scala" -> """class TopLevel()
    |object Foo {
    |  var field: Int = 42
    |  def defn: Unit = println("Hello World")
    |  def defn2: Unit = {
    |    val x: Any = 10
    |    def innerDefn(x: Any) = { 
    |      println("foo")
    |    }
    |    innerDefn(x)
    |  }
    |}
    """.stripMargin
  ) { loaded =>
    import nir._

    val TopLevel = Global.Top("TopLevel")
    val Foo = Global.Top("Foo")
    val FooModule = Global.Top("Foo$")
    val sourceFile = "Test.scala"

    object Definition {
      def unapply(
          defn: Defn
      ): Option[(Global.Top, Sig.Unmangled, nir.Position, Seq[nir.Inst])] =
        defn match {
          case Defn.Define(
                _,
                Global.Member(top: Global.Top, sig),
                _,
                insts,
                _
              ) =>
            Some((top, sig.unmangled, defn.pos, insts))
          case Defn.Var(_, Global.Member(top: Global.Top, sig), _, _) =>
            Some((top, sig.unmangled, defn.pos, Nil))
          case _ => None
        }
    }

    def `isScala2.12` = scalaVersion.startsWith("2.12.")
    def `isScala2.13` = scalaVersion.startsWith("2.13.")
    def isScala3 = scalaVersion.startsWith("3.")

    for (defn <- loaded) {
      def checkPos(line: Int, column: Int)(pos: nir.Position) = {
        val clue =
          s"${defn.name} - expected=$line:$column, actual=${pos.line}:${pos.column}"
        assertTrue(clue, pos.source.getPath().endsWith(sourceFile))
        assertEquals(clue, line, pos.line)
        assertEquals(clue, column, pos.column)
      }
      def checkLinesRange(range: Range)(
          positions: Iterable[nir.Position]
      ): Unit = {
        positions.foreach { pos =>
          assertTrue(s"${defn.name}", pos.source.getPath().endsWith(sourceFile))
          assertTrue(s"${defn.name}", range.contains(pos.line))
        }
      }
      val pos = defn.pos
      assertTrue(pos.source.getPath().endsWith(sourceFile))
      defn match {
        case Defn.Class(_, TopLevel, _, _) =>
          checkPos(0, 6)(pos)
        case Definition(TopLevel, Sig.Ctor(Nil), pos, insts) =>
          if (`isScala2.12`) {
            checkPos(1, 0)(pos) // wrong
            checkLinesRange(1 to 1)(insts.map(_.pos))
          } else {
            checkPos(0, 14)(pos)
            checkLinesRange(0 to 0)(insts.map(_.pos))
          }
        case Defn.Class(_, Foo, _, _) =>
          checkPos(1, 7)(pos)
        case Defn.Module(_, FooModule, _, _) =>
          checkPos(1, 7)(pos)
        case Definition(FooModule, Sig.Ctor(Nil), pos, insts) =>
          if (`isScala2.13`) checkPos(1, 11)(pos)
          else if (isScala3) checkPos(2, 2)(pos)
          if (`isScala2.12`) () // scalac sets wrong position, line 12
          else checkLinesRange(1 to 2)(insts.map(_.pos))
        // proxies to module implemention
        case Definition(
              Foo,
              Sig.Method("field" | "field_$eq", _, _),
              pos,
              insts
            ) =>
          (pos +: insts.map(_.pos)).foreach(checkPos(2, 6))
        case Definition(Foo, Sig.Method("defn", _, _), pos, insts) =>
          (pos +: insts.map(_.pos)).foreach(checkPos(3, 6))
        case Definition(Foo, Sig.Method("defn2", _, _), pos, insts) =>
          (pos +: insts.map(_.pos)).foreach(checkPos(4, 6))
        // Actual methods
        case Definition(
              FooModule,
              Sig.Method("field", _, _) | Sig.Method("field_$eq", _, _) |
              Sig.Field("field", _),
              pos,
              insts
            ) =>
          checkPos(2, 6)(pos)
          checkLinesRange(2 to 2)(insts.map(_.pos))
        case Definition(FooModule, Sig.Method("defn", _, _), pos, insts) =>
          checkPos(3, 6)(pos)
          checkLinesRange(3 to 3)(insts.map(_.pos))
        case Definition(FooModule, Sig.Method("defn2", _, _), pos, insts) =>
          checkPos(4, 6)(pos)
          checkLinesRange(4 to 9)(insts.map(_.pos))
        case Definition(
              FooModule,
              Sig.Method("innerDefn$1", _, _),
              pos,
              insts
            ) =>
          checkPos(6, 8)(pos)
          checkLinesRange(6 to 8)(insts.map(_.pos))

        case Definition(
              FooModule,
              Sig.Method("writeReplace", _, _),
              pos,
              insts
            ) =>
          checkPos(1, 7)(pos)

        case other => fail(s"Unexpected defn: ${nir.Show(other)}")
      }
    }
  }

}

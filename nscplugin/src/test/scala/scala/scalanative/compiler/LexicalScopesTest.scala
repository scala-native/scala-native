package scala.scalanative
package compiler

import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.StaticForwardersSuite.compileAndLoad
import scala.scalanative.buildinfo.ScalaNativeBuildInfo._
import scala.reflect.ClassTag

class LexicalScopesTest {
  import nir._

  def assertContainsAll[T](
      msg: String,
      expected: Iterable[T],
      actual: Iterable[T]
  ) = {
    val left = expected.toSeq
    val right = actual.toSeq
    val diff = left.diff(right)
    assertTrue(s"$msg - not found ${diff} in $right", diff.isEmpty)
  }

  def assertContains[T](msg: String, expected: T, actual: Iterable[T]) = {
    assertTrue(
      s"$msg - not found ${expected} in ${actual.toSeq}",
      actual.find(_ == expected).isDefined
    )
  }

  def assertDistinct(localNames: Iterable[LocalName]) = {
    val duplicated =
      localNames.groupBy(identity).filter(_._2.size > 1).map(_._1)
    assertTrue(s"Found duplicated names of ${duplicated}", duplicated.isEmpty)
  }
  private object TestMain {
    val companionMain = Global
      .Top("Test$")
      .member(Rt.ScalaMainSig.copy(scope = Sig.Scope.Public))

    def unapply(name: Global): Boolean = name == companionMain
  }
  private def findDefinition(linked: Seq[Defn]) = linked
    .collectFirst {
      case defn @ Defn.Define(_, TestMain(), _, _, _) =>
        defn
    }
    .ensuring(_.isDefined, "Not found linked method")

  // Ensure to use all the vals/vars, otherwise they might not be emmited by the compiler
  @Test def simpleLexicalScoeps(): Unit = compileAndLoad(
    sources = "Test.scala" -> """
    |object Test {
    |  def main(args: Array[String]): Unit = {
    |    val a = args.size
    |    val b = a + this.##
    |    val scopeResult = {
    |      val innerA = args.size + a
    |      val innerB = innerA + b
    |      innerA + innerB
    |     }
    |    assert(scopeResult != 0)
    |  }
    |}
    """.stripMargin
  ) { loaded =>
    findDefinition(loaded).foreach { defn =>
      defn.debugInfo.lexicalScopes.foreach(println)
      println(defn.show)
    //   val lets = namedLets(defn).values
    //   val expectedLetNames =
    //     Seq("localVal", "localVar", "innerVal", "innerVar", "scoped")
    //   val expectedNames = Seq("args", "this") ++ expectedLetNames
    //   assertContainsAll("lets defined", expectedLetNames, lets)
    //   assertContainsAll("vals defined", expectedNames, defn.localNames.values)
    //   assertDistinct(lets)
    //   defn.insts.head match {
    //     case Inst.Label(
    //           _,
    //           Seq(
    //             Val.Local(thisId, Type.Ref(Global.Top("Test$"), _, _)),
    //             Val.Local(argsId, Type.Array(Rt.String, _))
    //           )
    //         ) =>
    //       assertTrue("thisArg", defn.localNames.get(thisId).contains("this"))
    //       assertTrue("argsArg", defn.localNames.get(argsId).contains("args"))
    //     case _ => fail("Invalid input label")
    //   }
    // }
    }
  }
}

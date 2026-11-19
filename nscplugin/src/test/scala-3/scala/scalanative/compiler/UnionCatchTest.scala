package scala.scalanative
package compiler

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.linker.compileAndLoad
import scala.scalanative.nir._

/** A catch case `case e: (A | B)` must test each alternative. Since
 *  scala/scala3#27294 (Scala 3.10.2) such a case of disjoint types stays a
 *  catch case, whose pattern type is erased to the alternatives' common
 *  superclass by the time the plugin sees it; before, the pattern matcher tests
 *  the alternatives. Either way the generated code tests `A` and `B`, never
 *  their common superclass.
 */
class UnionCatchTest {

  private def testedTypes(defn: Defn.Define): Seq[Type] =
    defn.insts.collect { case Inst.Let(_, Op.Is(ty, _), _) => ty }

  @Test def unionCatchCaseTestsEachAlternative(): Unit = {
    compileAndLoad(
      "Catcher.scala" ->
        """|class FirstException extends Exception
           |class SecondException extends RuntimeException
           |object Catcher {
           |  def classify(e: Throwable): Int =
           |    try throw e
           |    catch { case _: (FirstException | SecondException) => 1 }
           |}
           |""".stripMargin
    ) { defns =>
      val method = Global
        .Top("Catcher$")
        .member(Sig.Method("classify", Seq(Rt.Throwable, Type.Int)))
      val defn = defns.collectFirst {
        case d: Defn.Define if d.name == method => d
      }
      assertTrue(s"missing $method in ${defns.map(_.name)}", defn.isDefined)

      val tested = testedTypes(defn.get)
      def ref(name: String): Type = Type.Ref(Global.Top(name))
      assertTrue(
        s"FirstException is not tested: $tested",
        tested.contains(ref("FirstException"))
      )
      assertTrue(
        s"SecondException is not tested: $tested",
        tested.contains(ref("SecondException"))
      )
      assertFalse(
        s"the alternatives' common superclass is tested: $tested",
        tested.contains(ref("java.lang.Exception"))
      )
    }
  }
}

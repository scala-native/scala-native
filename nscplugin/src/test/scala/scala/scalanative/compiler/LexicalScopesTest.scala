package scala.scalanative
package compiler

import scala.collection.mutable
import scala.reflect.ClassTag

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.buildinfo.ScalaNativeBuildInfo._
import scala.scalanative.linker.compileAndLoad
import scala.scalanative.nir.Defn.Define.DebugInfo.LexicalScope

class LexicalScopesTest {

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

  def assertDistinct(localNames: Iterable[nir.LocalName]) = {
    val duplicated =
      localNames.groupBy(identity).filter(_._2.size > 1).map(_._1)
    assertTrue(s"Found duplicated names of ${duplicated}", duplicated.isEmpty)
  }
  private object TestMain {
    val companionMain = nir.Global
      .Top("Test$")
      .member(nir.Rt.ScalaMainSig.copy(scope = nir.Sig.Scope.Public))

    def unapply(name: nir.Global): Boolean = name == companionMain
  }
  private def findDefinition(linked: Seq[nir.Defn]) = linked
    .collectFirst {
      case defn @ nir.Defn.Define(_, TestMain(), _, _, _) =>
        defn
    }
    .ensuring(_.isDefined, "Not found linked method")

  def namedLets(defn: nir.Defn.Define): Map[nir.Inst.Let, nir.LocalName] =
    defn.insts.collect {
      case inst: nir.Inst.Let if defn.debugInfo.localNames.contains(inst.id) =>
        inst -> defn.debugInfo.localNames(inst.id)
    }.toMap

  def scopeOf(localName: nir.LocalName)(implicit defn: nir.Defn.Define) =
    namedLets(defn)
      .collectFirst {
        case (let @ nir.Inst.Let(id, _, _), `localName`) => let.scopeId
      }
      .orElse { fail(s"Not found a local named: ${localName}"); None }
      .flatMap(defn.debugInfo.lexicalScopeOf.get)
      .orElse { fail(s"Not found defined scope for ${localName}"); None }
      .get

  def scopeParents(
      scope: LexicalScope
  )(implicit defn: nir.Defn.Define): List[nir.ScopeId] = {
    if (scope.isTopLevel) Nil
    else {
      val stack = List.newBuilder[nir.ScopeId]
      var current = scope
      while ({
        val parent = defn.debugInfo.lexicalScopeOf(current.parent)
        current = parent
        stack += current.id
        !parent.isTopLevel
      }) ()
      stack.result()
    }
  }

  // Ensure to use all the vals/vars, otherwise they might not be emmited by the compiler
  @Test def scopesHierarchy(): Unit = compileAndLoad(
    sources = "Test.scala" -> """
    |object Test {
    |  def main(args: Array[String]): Unit = {
    |    val a = args.size
    |    val b = a + this.##
    |    val result = {
    |      val innerA = args.size + a
    |      val innerB = innerA + b
    |      val innerResult = {
    |         val deep = innerA + innerB
    |         deep * 42
    |      }
    |      innerA * innerB * innerResult
    |     }
    |    assert(result != 0)
    |  }
    |}
    """.stripMargin
  ) { loaded =>
    findDefinition(loaded).foreach { implicit defn =>
      assertContainsAll(
        "named vals",
        Seq("a", "b", "result", "innerA", "innerB", "innerResult", "deep"),
        namedLets(defn).values
      )
      // top-level
      val innerA = scopeOf("innerA")
      val innerB = scopeOf("innerB")
      val innerResult = scopeOf("innerResult")
      val deep = scopeOf("deep")
      val result = scopeOf("result")
      assertTrue("scope-a", scopeOf("a").isTopLevel)
      assertTrue("scope-b", scopeOf("b").isTopLevel)
      assertFalse("inner-A", innerA.isTopLevel)
      assertFalse("inner-B", innerB.isTopLevel)
      assertFalse("inner-result", innerResult.isTopLevel)
      assertFalse("deep", deep.isTopLevel)
      assertTrue("result", result.isTopLevel)

      assertEquals("innerA-parent", result.id, innerA.parent)
      assertEquals("innerB-parent", innerA.parent, innerB.parent)
      assertEquals("innerResult-parent", result.id, innerResult.parent)
      assertEquals("deep-parent", innerResult.id, deep.parent)
    }
  }

  @Test def tryCatchFinalyBlocks(): Unit = compileAndLoad(
    sources = "Test.scala" -> """
    |object Test {
    |  def main(args: Array[String]): Unit = {
    |    val a = args.size
    |    val b =
    |      try {
    |        val inTry = args(0).toInt
    |        inTry + 1
    |      }catch{
    |        case ex1: Exception =>
    |          val n = args(0)
    |          n.size
    |        case ex2: Throwable =>
    |          val m = args.size
    |          throw ex2
    |      } finally {
    |        val finalVal = "fooBar"
    |        println(finalVal)
    |      }
    |  }
    |}
    """.stripMargin
  ) { loaded =>
    findDefinition(loaded).foreach { implicit defn =>
      assertContainsAll(
        "named vals",
        // b passed as label argument
        Seq("a", "inTry", "ex1", "n", "ex2", "m", "finalVal"),
        namedLets(defn).values
      )
      // top-level
      val a = scopeOf("a")
      val inTry = scopeOf("inTry")
      val ex1 = scopeOf("ex1")
      val ex2 = scopeOf("ex2")
      val n = scopeOf("n")
      val m = scopeOf("m")
      val finalVal = scopeOf("finalVal")
      assertTrue("scope-a", scopeOf("a").isTopLevel)
      assertFalse(Seq(inTry, ex1, ex2, n, m, finalVal).exists(_.isTopLevel))

      assertNotEquals(a.id, inTry.id)
      assertContains("inTry-parents", a.id, scopeParents(inTry))
      assertEquals(ex1.id, n.parent)
      assertEquals(ex2.id, m.parent)
    }
  }

}

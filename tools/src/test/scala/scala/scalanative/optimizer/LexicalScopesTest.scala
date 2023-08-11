package scala.scalanative
package optimizer

import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable

class LexicalScopesTest extends OptimizerSpec {
  import nir._

  override def optimize[T](
      entry: String,
      sources: Map[String, String],
      setupConfig: build.NativeConfig => build.NativeConfig = identity
  )(fn: (build.Config, linker.Result) => T) =
    super.optimize(
      entry,
      sources,
      setupConfig.andThen(
        _.withDebugMetadata(true)
          .withMode(build.Mode.releaseFull)
      )
    )(fn)

  def scopeOf(localName: LocalName)(implicit defn: Defn.Define) =
    namedLets(defn)
      .collectFirst {
        case (Inst.Let(id, _, _), `localName`) => id
      }
      .orElse { fail(s"Not found a local named: ${localName}"); None }
      .flatMap(defn.debugInfo.scopeOf)
      .orElse { fail(s"Not found defined scope for ${localName}"); None }
      .get

  // Ensure to use all the vals/vars, otherwise they might not be emmited by the compiler
  @Test def scopesHierarchy(): Unit = optimize(
    entry = "Test",
    sources = Map("Test.scala" -> """
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
    """.stripMargin)
  ) {
    case (config, result) =>
      findEntry(result.defns).foreach { implicit defn =>
        defn.debugInfo.lexicalScopes.foreach(println)
        println(defn.show)

        assertContainsAll(
          "named vals",
          Seq("a", "b", "result", "innerA", "innerB", "innerResult", "deep"),
          namedLets(defn).values
        )
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
}

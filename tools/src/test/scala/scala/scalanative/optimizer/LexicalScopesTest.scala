package scala.scalanative
package optimizer

import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable
import scala.scalanative.build.NativeConfig
import scala.scalanative.nir.Defn.Define.DebugInfo.LexicalScope

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
      { (config: NativeConfig) =>
        config
          .withDebugMetadata(true)
          .withMode(build.Mode.releaseFull)
      }.andThen(setupConfig)
    )(fn)

  def scopeOf(localName: LocalName)(implicit defn: Defn.Define) =
    namedLets(defn)
      .collectFirst {
        case (let @ Inst.Let(id, _, _), `localName`) => let.scope
      }
      .orElse { fail(s"Not found a local named: ${localName}"); None }
      .flatMap(id => defn.debugInfo.lexicalScopeOf.get(id))
      .orElse { fail(s"Not found defined scope for ${localName}"); None }
      .get

  def scopeParents(
      scope: LexicalScope
  )(implicit defn: Defn.Define): List[ScopeId] = {
    if (scope.isTopLevel) Nil
    else {
      val stack = List.newBuilder[ScopeId]
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
  @Test def scopesHierarchyDebug(): Unit = optimize(
    entry = "Test",
    sources = Map(
      "Test.scala" -> """
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
    ),
    setupConfig = _.withMode(build.Mode.debug)
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
        // top-level
        val a = scopeOf("a")
        val b = scopeOf("b")
        val innerA = scopeOf("innerA")
        val innerB = scopeOf("innerB")
        val innerResult = scopeOf("innerResult")
        val deep = scopeOf("deep")
        val result = scopeOf("result")
        assertTrue("scope-a", a.isTopLevel)
        assertTrue("scope-b", b.isTopLevel)
        assertFalse("inner-A", innerA.isTopLevel)
        assertFalse("inner-B", innerB.isTopLevel)
        assertFalse("inner-result", innerResult.isTopLevel)
        assertFalse("deep", deep.isTopLevel)
        assertTrue("result", result.isTopLevel)

        // In debug mode calls to Array.size should not be inlined, so a and b should be defined in the same scope
        assertEquals("a-b-scope", a.id, b.id)
        assertEquals("result-scope", a.id, result.id)
        assertEquals("innerA-parent", result.id, innerA.parent)
        assertEquals("innerB-parent", innerA.parent, innerB.parent)
        assertEquals("innerResult-parent", result.id, innerResult.parent)
        assertEquals("deep-parent", innerResult.id, deep.parent)
      }
  }

  @Test def scopesHierarchyRelease(): Unit = optimize(
    entry = "Test",
    sources = Map(
      "Test.scala" -> """
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
    )
  ) {
    case (config, result) =>
      assertEquals(config.compilerConfig.mode, build.Mode.releaseFull)
      findEntry(result.defns).foreach { implicit defn =>
        defn.debugInfo.lexicalScopes.foreach(println)
        println(defn.show)

        assertContainsAll(
          "named vals",
          Seq("a", "b", "result", "innerA", "innerB", "innerResult", "deep"),
          namedLets(defn).values
        )
        // top-level
        val a = scopeOf("a")
        val b = scopeOf("b")
        val innerA = scopeOf("innerA")
        val innerB = scopeOf("innerB")
        val innerResult = scopeOf("innerResult")
        val deep = scopeOf("deep")
        val result = scopeOf("result")

        val aParents = scopeParents(a)
        val bParents = scopeParents(b)
        // value of 'a' is inlined call to Array.size, so it's scope would be different then b, but they should have a common ancestor
        assertNotEquals("a-b-diff-scope", a.id, b.id)
        assertTrue("a-b-common-parent", aParents.diff(bParents).nonEmpty)
        // result and b don't have inlined calls so they shall be defined in the same scope
        assertEquals("result-eq-b-scope", b.id, result.id)

        assertEquals("innerA-parent", result.id, innerA.parent)
        assertEquals("innerB-parent", innerA.parent, innerB.parent)
        assertEquals("innerResult-parent", result.id, innerResult.parent)
        assertEquals("deep-parent", innerResult.id, deep.parent)

        val duplicateIds =
          defn.debugInfo.lexicalScopes.groupBy(_.id).filter(_._2.size > 1)
        assertEquals("duplicateIds", Map.empty, duplicateIds)

        for (scope <- defn.debugInfo.lexicalScopes) {
          assertTrue(
            "state parent not defined",
            defn.debugInfo.lexicalScopeOf.contains(scope.parent)
          )
        }
      }
  }
}

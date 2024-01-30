package scala.scalanative
package optimizer

import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable
import scala.scalanative.build.NativeConfig
import scala.scalanative.linker.ReachabilityAnalysis
import scala.scalanative.nir.Defn.Define.DebugInfo.LexicalScope

class LexicalScopesTest extends OptimizerSpec {

  override def optimize[T](
      entry: String,
      sources: Map[String, String],
      setupConfig: build.NativeConfig => build.NativeConfig = identity
  )(fn: (build.Config, ReachabilityAnalysis.Result) => T) =
    super.optimize(
      entry,
      sources,
      { (config: NativeConfig) =>
        config
          .withSourceLevelDebuggingConfig(_.enableAll)
          .withMode(build.Mode.releaseFull)
      }.andThen(setupConfig)
    )(fn)

  def scopeOf(localName: nir.LocalName)(implicit defn: nir.Defn.Define) =
    namedLets(defn)
      .collectFirst {
        case (let @ nir.Inst.Let(id, _, _), `localName`) => let.scopeId
      }
      .orElse { fail(s"Not found a local named: ${localName}"); None }
      .flatMap(id => defn.debugInfo.lexicalScopeOf.get(id))
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
      def test(defns: Seq[nir.Defn]): Unit = findEntry(defns).foreach {
        implicit defn =>
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
      test(result.defns)
      afterLowering(config, result)(test)
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
      def test(defns: Seq[nir.Defn]): Unit = findEntry(defns).foreach {
        implicit defn =>
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
          assertEquals("a-b-diff-scope", a.id, b.id)
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
      test(result.defns)
      afterLowering(config, result)(test)
  }

  @Test def inlinedCall(): Unit = optimize(
    entry = "Test",
    sources = Map(
      "Test.scala" -> """
    |object Test {
    |  @scala.scalanative.annotation.alwaysinline
    |  def calc(x: Int, y: Int): Int = {
    |    val myMin = x min y
    |    val myTmp = myMin * y + x
    |    println(myTmp)
    |    myTmp + 1
    |  }
    |  def main(args: Array[String]): Unit = {
    |    val a = calc(42, args.size)
    |    println(a + 1)
    |    println(a.toString)
    |  }
    |}
    """.stripMargin
    ),
    setupConfig = _.withMode(build.Mode.releaseFast)
  ) {
    case (config, result) =>
      findEntry(result.defns).foreach { implicit defn =>
        assertContainsAll(
          "named vals",
          Seq("a", "myTmp"),
          namedLets(defn).values
        )
        // a and b can move moved to seperate scopes in transofrmation, but shall still have common parent
        val a = scopeOf("a")
        assertEquals("a-parent", a.id, nir.ScopeId.TopLevel)

        // TODO: Try to preserve inlined values
        // val myMin = scopeOf("myMin")
        // assertNotEquals("myMin-scope", a.id, myMin.id)
        // assertEquals("myMin-parent", a.id, myMin.parent)

        val myTmp = scopeOf("myTmp")
        assertNotEquals("myTmp-scope", a.id, myTmp.id)
        assertEquals("myTmp-parent", a.id, myTmp.parent)
      }
  }

  @Test def multipleInlinedCalls(): Unit = optimize(
    entry = "Test",
    sources = Map(
      "Test.scala" -> """
    |object Test {
    |  @scala.scalanative.annotation.alwaysinline
    |  def calc(x: Int, y: Int): Int = {
    |    val myMin = x min y
    |    val myTmp = myMin * y + x
    |    println(myTmp)
    |    myTmp + 1
    |  }
    |  def main(args: Array[String]): Unit = {
    |    val a = calc(42, args.size)
    |    val b = calc(24, this.##)
    |    println(a + 1)
    |    println(b + 1)
    |    println(a.toString -> b.toString)
    |  }
    |}
    """.stripMargin
    ),
    setupConfig = _.withMode(build.Mode.releaseFast)
  ) {
    case (config, result) =>
      findEntry(result.defns).foreach { implicit defn =>
        assertContainsAll(
          "named vals",
          Seq("a", "b", "myTmp"),
          namedLets(defn).values
        )
        val nameDuplicates = namedLets(defn).groupBy(_._2).map {
          case (key, values) => (key, values.map(_._1).toList.sortBy(_.id.id))
        }

        val a = scopeOf("a")
        val b = scopeOf("b")
        assertEquals("a-b-scopes", a.id, b.id)
        assertEquals("a-b-parent", a.parent, b.parent)
        assertTrue("a-b-toplevel", a.isTopLevel)

        nameDuplicates("myTmp") match {
          case Seq(first, second) =>
            assertNotEquals(
              "first-second scope ids",
              first.scopeId,
              second.scopeId
            )
            assertEquals(
              defn.debugInfo.lexicalScopeOf(first.scopeId).parent,
              defn.debugInfo.lexicalScopeOf(second.scopeId).parent
            )
          case unexpected =>
            fail(s"Unexpected ammount of myMin duplicates: $unexpected")
        }
      }
  }

}

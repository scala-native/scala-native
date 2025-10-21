package scala.scalanative
package linker

import org.junit.Assert._
import org.junit.Test

class StubSpec extends LinkerSpec {

  val entry = "Main"
  val stubMethodSource = """object Main {
                           |  def main(args: Array[String]): Unit =
                           |    stubMethod
                           |  @scala.scalanative.annotation.stub
                           |  def stubMethod: Int = ???
                           |}""".stripMargin
  val stubClassSource = """@scalanative.annotation.stub class StubClass
                           |object Main {
                           |  def main(args: Array[String]): Unit =
                           |    new StubClass
                           |}""".stripMargin
  val stubModuleSource = """@scalanative.annotation.stub object StubModule
                           |object Main {
                           |  def main(args: Array[String]): Unit = {
                           |    val x = StubModule
                           |  }
                           |}""".stripMargin

  @Test def ignoreMethods(): Unit = {
    doesNotLink(entry, stubMethodSource, _.withLinkStubs(false)) {
      (cfg, result: ReachabilityAnalysis.Failure) =>
        assertTrue(!cfg.linkStubs)
        assertTrue(result.unreachable.length == 1)
        assertEquals(
          nir.Global
            .Top("Main$")
            .member(nir.Sig.Method("stubMethod", Seq(nir.Type.Int))),
          result.unreachable.head.name
        )
    }
  }

  @Test def includeMethods(): Unit = {
    link(entry, stubMethodSource, _.withLinkStubs(true)) { (cfg, result) =>
      assertTrue(cfg.linkStubs)
      assertTrue(result.isSuccessful)
    }
  }

  @Test def ignoreClasses(): Unit = {
    doesNotLink(entry, stubClassSource, _.withLinkStubs(false)) {
      (cfg, result: ReachabilityAnalysis.Failure) =>
        assertTrue(!cfg.linkStubs)
        assertTrue(result.unreachable.length == 1)
        assertTrue(result.unreachable.head.name == nir.Global.Top("StubClass"))
    }
  }

  @Test def includeClasses(): Unit = {
    link(entry, stubClassSource, _.withLinkStubs(true)) { (cfg, result) =>
      assertTrue(cfg.linkStubs)
      assertTrue(result.isSuccessful)
    }
  }

  @Test def ignoreModules(): Unit = {
    doesNotLink(entry, stubModuleSource, _.withLinkStubs(false)) {
      case (cfg, result) =>
        assertTrue(!cfg.linkStubs)
        assertTrue(result.unreachable.length == 1)
        assertTrue(
          result.unreachable.head.name == nir.Global.Top("StubModule$")
        )
    }
  }

  @Test def includeModules(): Unit = {
    link(entry, stubModuleSource, _.withLinkStubs(true)) { (cfg, result) =>
      assertTrue(cfg.linkStubs)
      assertTrue(result.isSuccessful)
    }
  }

}

package scala.scalanative
package linker

import nir.{Sig, Type, Global}

import org.junit.Test
import org.junit.Assert._

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
    link(entry, stubMethodSource, _.withLinkStubs(false)) { (cfg, result) =>
      assertTrue(!cfg.linkStubs)
      assertTrue(result.unavailable.length == 1)
      assertEquals(
        Global
          .Top("Main$")
          .member(Sig.Method("stubMethod", Seq(Type.Int))),
        result.unavailable.head
      )
    }
  }

  @Test def includeMethods(): Unit = {
    link(entry, stubMethodSource, _.withLinkStubs(true)) { (cfg, result) =>
      assert(cfg.linkStubs)
      assert(result.unavailable.length == 1)
      assert(
        result.unavailable.head == Global
          .Top("Main$")
          .member(Sig.Method("stubMethod", Seq(Type.Int)))
      )
    }
  }

  @Test def ignoreClasses(): Unit = {
    link(entry, stubClassSource, _.withLinkStubs(false)) { (cfg, result) =>
      assertTrue(!cfg.linkStubs)
      assertTrue(result.unavailable.length == 1)
      assertTrue(result.unavailable.head == Global.Top("StubClass"))
    }
  }

  @Test def includeClasses(): Unit = {
    link(entry, stubClassSource, _.withLinkStubs(true)) { (cfg, result) =>
      assert(cfg.linkStubs)
      assert(result.unavailable.length == 1)
      assert(result.unavailable.head == Global.Top("StubClass"))
    }
  }

  @Test def ignoreModules(): Unit = {
    link(entry, stubModuleSource, _.withLinkStubs(false)) { (cfg, result) =>
      assertTrue(!cfg.linkStubs)
      assertTrue(result.unavailable.length == 1)
      assertTrue(result.unavailable.head == Global.Top("StubModule$"))
    }
  }

  @Test def includeModules(): Unit = {
    link(entry, stubModuleSource, _.withLinkStubs(true)) { (cfg, result) =>
      assert(cfg.linkStubs)
      assert(result.unavailable.length == 1)
      assert(result.unavailable.head == Global.Top("StubModule$"))
    }
  }

}

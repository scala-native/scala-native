package scala.scalanative
package linker

import nir.{Sig, Type, Global}

class StubSpec extends LinkerSpec {

  val entry            = "Main$"
  val stubMethodSource = """object Main {
                           |  def main(args: Array[String]): Unit =
                           |    stubMethod
                           |  @scala.scalanative.native.stub
                           |  def stubMethod: Int = ???
                           |}""".stripMargin
  val stubClassSource  = """@scalanative.native.stub class StubClass
                          |object Main {
                          |  def main(args: Array[String]): Unit =
                          |    new StubClass
                          |}""".stripMargin
  val stubModuleSource = """@scalanative.native.stub object StubModule
                           |object Main {
                           |  def main(args: Array[String]): Unit =
                           |    StubModule
                           |}""".stripMargin

  "Stub methods" should "be ignored by the linker when `linkStubs = false`" in {
    link(entry, stubMethodSource, linkStubs = false) { (cfg, result) =>
      assert(!cfg.linkStubs)
      assert(result.unavailable.length == 1)
      assert(
        result.unavailable.head == Global
          .Top("Main$")
          .member(Sig.Method("stubMethod", Seq(Type.Int))))
    }
  }

  it should "be included when `linkStubs = true`" in {
    link(entry, stubMethodSource, linkStubs = true) { (cfg, result) =>
      assert(cfg.linkStubs)
      assert(result.unavailable.isEmpty)
    }
  }

  "Stub classes" should "be ignored by the linker when `linkStubs = false`" in {
    link(entry, stubClassSource, linkStubs = false) { (cfg, result) =>
      assert(!cfg.linkStubs)
      assert(result.unavailable.length == 1)
      assert(result.unavailable.head == Global.Top("StubClass"))
    }
  }

  it should "be included when `linkStubs = true`" in {
    link(entry, stubClassSource, linkStubs = true) { (cfg, result) =>
      assert(cfg.linkStubs)
      assert(result.unavailable.isEmpty)
    }
  }

  "Stub modules" should "be ignored by the linker when `linkStubs = false`" in {
    link(entry, stubModuleSource, linkStubs = false) { (cfg, result) =>
      assert(!cfg.linkStubs)
      assert(result.unavailable.length == 1)
      assert(result.unavailable.head == Global.Top("StubModule$"))
    }
  }

  it should "be included when `linkStubs = true`" in {
    link(entry, stubModuleSource, linkStubs = true) { (cfg, result) =>
      assert(cfg.linkStubs)
      assert(result.unavailable.isEmpty)
    }
  }

}

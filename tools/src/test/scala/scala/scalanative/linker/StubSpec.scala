package scala.scalanative
package linker

import nir.Global.Top

class StubSpec extends LinkerSpec {

  val entry  = "Main$"
  val source = """object Main {
                 |  def main(args: Array[String]): Unit =
                 |    stubMethod
                 |  @scala.scalanative.native.stub
                 |  def stubMethod: Int = ???
                 |}""".stripMargin

  "Stubs" should "be ignored by the linker when `linkStubs = false`" in {
    link(entry, source, linkStubs = false) { (cfg, result) =>
      assert(!cfg.linkStubs)
      assert(result.unresolved.length == 1)
      assert(result.unresolved.head == Top("Main$").member("stubMethod_i32"))
    }
  }

  it should "be included when `linkStubs = true`" in {
    link(entry, source, linkStubs = true) { (cfg, result) =>
      assert(cfg.linkStubs)
      assert(result.unresolved.isEmpty)
    }
  }

}

package scala.scalanative
package linker

import org.junit._

class IdentifiersSuite extends ReachabilitySuite {

  @Test def replaceDoubleQuotedIdentifiers(): Unit = testReachable() {
    val source = """
        |object `"Foo"Bar"` {
        |  val x: `"Foo"Bar"`.type       = this
        |  val `"x"`: `"Foo"Bar"`.type   = this
        |  val `"x"x"`: `"Foo"Bar"`.type = this
        |
        |  def y(): `"Foo"Bar"`.type     = this
        |  def `"y"`(): `"Foo"Bar"`.type   = this
        |  def `"y"y"`(): `"Foo"Bar"`.type = this
        |}
        |
        |object Main {
        |  def main(args: Array[String]): Unit = ()
        |  import `"Foo"Bar"`._
        |  x
        |  `x`
        |  `"x"`
        |  `"x"x"`
        |
        |  y()
        |  `y`()
        |  `"y"`()
        |  `"y"y"`()
        |}
        |""".stripMargin

    val FooBar = nir.Global.Top("$u0022Foo$u0022Bar$u0022$")
    val Main = nir.Global.Top("Main")
    val MainModule = nir.Global.Top("Main$")

    val entry = Main.member(nir.Rt.ScalaMainSig)
    val privateFooBar = nir.Sig.Scope.Private(FooBar)

    val reachable = Seq(
      nir.Rt.Object.name,
      nir.Rt.Object.name.member(nir.Sig.Ctor(Seq.empty)),
      Main,
      Main.member(nir.Rt.ScalaMainSig),
      MainModule,
      MainModule.member(nir.Sig.Ctor(Seq.empty)),
      MainModule.member(
        nir.Sig.Method("main", nir.Rt.ScalaMainSig.types, nir.Sig.Scope.Public)
      ),
      FooBar,
      FooBar.member(nir.Sig.Ctor(Seq.empty)),
      // fields
      FooBar.member(nir.Sig.Field("x", privateFooBar)),
      FooBar.member(nir.Sig.Field("$u0022x$u0022", privateFooBar)),
      FooBar.member(nir.Sig.Field("$u0022x$u0022x$u0022", privateFooBar)),
      // accessors
      FooBar.member(nir.Sig.Method("x", Seq(nir.Type.Ref(FooBar)))),
      FooBar.member(nir.Sig.Method("$u0022x$u0022", Seq(nir.Type.Ref(FooBar)))),
      FooBar.member(
        nir.Sig.Method("$u0022x$u0022x$u0022", Seq(nir.Type.Ref(FooBar)))
      ),
      // methods
      FooBar.member(nir.Sig.Method("y", Seq(nir.Type.Ref(FooBar)))),
      FooBar.member(nir.Sig.Method("$u0022y$u0022", Seq(nir.Type.Ref(FooBar)))),
      FooBar.member(
        nir.Sig.Method("$u0022y$u0022y$u0022", Seq(nir.Type.Ref(FooBar)))
      )
    )
    (source, entry, reachable)
  }

}

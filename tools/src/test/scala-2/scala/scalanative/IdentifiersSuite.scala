package scala.scalanative.linker

import scala.scalanative.nir.{Global, Rt, Sig, Type}

class IdentifiersSuite extends ReachabilitySuite {

  testReachable() {
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
        |  `$u0022x"`
        |  `"x$u0022`
        |  `"x$u0022x"`
        |
        |  y()
        |  `y`()
        |  `"y"`()
        |  `"y"y"`()
        |  `$u0022y"`()
        |  `"y$u0022`()
        |  `"y$u0022y"`()
        |}
        |""".stripMargin

    val FooBar = Global.Top("$u0022Foo$u0022Bar$u0022$")
    val Main = Global.Top("Main")
    val MainModule = Global.Top("Main$")

    val entry = Main.member(Rt.ScalaMainSig)
    val privateFooBar = Sig.Scope.Private(FooBar)

    val reachable = Seq(
      Rt.Object.name,
      Rt.Object.name.member(Sig.Ctor(Seq.empty)),
      Main,
      Main.member(Rt.ScalaMainSig),
      MainModule,
      MainModule.member(Sig.Ctor(Seq.empty)),
      MainModule.member(
        Sig.Method("main", Rt.ScalaMainSig.types, Sig.Scope.Public)
      ),
      FooBar,
      FooBar.member(Sig.Ctor(Seq.empty)),
      // fields
      FooBar.member(Sig.Field("x", privateFooBar)),
      FooBar.member(Sig.Field("$u0022x$u0022", privateFooBar)),
      FooBar.member(Sig.Field("$u0022x$u0022x$u0022", privateFooBar)),
      // accessors
      FooBar.member(Sig.Method("x", Seq(Type.Ref(FooBar)))),
      FooBar.member(Sig.Method("$u0022x$u0022", Seq(Type.Ref(FooBar)))),
      FooBar.member(Sig.Method("$u0022x$u0022x$u0022", Seq(Type.Ref(FooBar)))),
      // methods
      FooBar.member(Sig.Method("y", Seq(Type.Ref(FooBar)))),
      FooBar.member(Sig.Method("$u0022y$u0022", Seq(Type.Ref(FooBar)))),
      FooBar.member(Sig.Method("$u0022y$u0022y$u0022", Seq(Type.Ref(FooBar))))
    )
    (source, entry, reachable)
  }

}

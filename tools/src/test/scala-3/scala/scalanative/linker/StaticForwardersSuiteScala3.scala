package scala.scalanative.linker

import org.scalatest._
import scala.scalanative.LinkerSpec
import scala.scalanative.nir._
import scala.scalanative.linker.StaticForwardersSuite._

class StaticForwardersSuiteScala3 extends LinkerSpec {
  "Static forwarder methods" should "be generated for @main annotated method" in {
    val MainClass = Global.Top("myMainFunction")
    val Package = Global.Top("Main$package")
    val PackageModule = Global.Top("Main$package$")

    compileAndLoad(
      "Main.scala" -> "@main def myMainFunction(): Unit = ()"
    ) { defns =>
      val expected = Seq(
        MainClass,
        MainClass.member(Sig.Ctor(Nil)),
        MainClass.member(Rt.ScalaMainSig),
        Package.member(
          Sig.Method("myMainFunction", Seq(Type.Unit), Sig.Scope.PublicStatic)
        ),
        PackageModule.member(Sig.Ctor(Nil)),
        PackageModule.member(Sig.Method("myMainFunction", Seq(Type.Unit)))
      )
      val names = defns.map(_.name)
      assert(expected.diff(names).isEmpty)
    }
  }
}

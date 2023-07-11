package scala.scalanative.linker

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.LinkerSpec
import scala.scalanative.nir._
import scala.scalanative.linker.StaticForwardersSuite._

class StaticForwardersSuiteScala3 extends LinkerSpec {

  @Test def mainAnnotation(): Unit = {
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
      assertTrue(expected.diff(names).isEmpty)
    }
  }
}

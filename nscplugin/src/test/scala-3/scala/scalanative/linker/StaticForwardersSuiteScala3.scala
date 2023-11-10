package scala.scalanative
package linker

import org.junit.Test
import org.junit.Assert._

class StaticForwardersSuiteScala3 {

  @Test def mainAnnotation(): Unit = {
    val MainClass = nir.Global.Top("myMainFunction")
    val Package = nir.Global.Top("Main$package")
    val PackageModule = nir.Global.Top("Main$package$")

    compileAndLoad(
      "Main.scala" -> "@main def myMainFunction(): Unit = ()"
    ) { defns =>
      val expected = Seq(
        MainClass,
        MainClass.member(nir.Sig.Ctor(Nil)),
        MainClass.member(nir.Rt.ScalaMainSig),
        Package.member(
          nir.Sig.Method(
            "myMainFunction",
            Seq(nir.Type.Unit),
            nir.Sig.Scope.PublicStatic
          )
        ),
        PackageModule.member(nir.Sig.Ctor(Nil)),
        PackageModule.member(
          nir.Sig.Method("myMainFunction", Seq(nir.Type.Unit))
        )
      )
      val names = defns.map(_.name)
      assertTrue(expected.diff(names).isEmpty)
    }
  }

}

package scala.scalanative.linker

import org.scalatest._
import scala.scalanative.LinkerSpec
import scala.scalanative.nir._

class StaticForwardersSuite extends LinkerSpec {
  "Static forwarder methods" should "be generated for @main annotated method" in {
    val MainClass = Global.Top("myMainFunction")
    val MainModule = Global.Top("myMainFunction$")
    val mainMethod = Sig.Method("main", Seq(Type.Array(Rt.String), Type.Unit))

    link(
      entry = MainModule.id,
      sources = Map(
        "Main.scala" -> "@main def myMainFunction(): Unit = ()"
      )
    ) { (cfg, result) =>
      assert(cfg.mainClass == MainModule.id)
      assert(result.unavailable.isEmpty)

      Seq(
        MainClass,
        MainClass.member(mainMethod),
        MainModule,
        MainModule.member(mainMethod)
      ).foreach { name =>
        assert(result.infos.contains(name))
      }
    }
  }
}

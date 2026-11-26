package scala.scalanative
package codegen

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.linker.{Class, Trait}

/* Linker-defined array classes mirror covariance: classes of element
 * superclasses as parents, marker traits for element traits.
 */
class ArrayClassHierarchyTest extends CodeGenSpec {

  private val sources = Map(
    "Test.scala" ->
      """|trait T
         |trait U extends T
         |class A
         |class B extends A with U
         |object Test {
         |  @noinline def opaque(x: AnyRef): AnyRef = x
         |  def main(args: Array[String]): Unit = {
         |    val bs = opaque(new Array[B](1))
         |    val strings = opaque(Array.ofDim[String](1, 1))
         |    println(bs.isInstanceOf[Array[A]])
         |    println(bs.isInstanceOf[Array[T]])
         |    println(strings.isInstanceOf[Array[Array[CharSequence]]])
         |    println(strings.isInstanceOf[Array[Array[AnyRef]]])
         |  }
         |}
         |""".stripMargin
  )

  @Test def arrayClassesMirrorCovariance(): Unit =
    optimize("Test", sources) {
      case (config, optimized) =>
        implicit val platform: PlatformInfo = PlatformInfo(config)
        val proxies =
          GenerateReflectiveProxies(optimized.dynimpls, optimized.defns)
        val meta = new Metadata(optimized, config, proxies)
        def info(name: String) = optimized.infos(nir.Global.Top(name))
        def cls(name: String) = info(name).asInstanceOf[Class]
        def names(infos: Seq[linker.ScopeInfo]) = infos.map(_.name.id)

        assertEquals(Some("[LA;"), cls("[LB;").parent.map(_.name.id))
        assertEquals(
          Some("scala.scalanative.runtime.ObjectArray"),
          cls("[LA;").parent.map(_.name.id)
        )
        assertEquals(
          Some("[[Ljava.lang.Object;"),
          cls("[[Ljava.lang.String;").parent.map(_.name.id)
        )
        // dyndispatch.c finds ObjectArray at lineage index 2
        for (name <- Seq("[LB;", "[[Ljava.lang.String;"))
          assertEquals(
            "scala.scalanative.runtime.ObjectArray",
            meta.lineage(cls(name))(2).name.id
          )

        assertTrue(names(meta.itableTraits(cls("[LB;"))).contains("#[LT;"))
        assertTrue(
          names(meta.itableTraits(cls("[[Ljava.lang.String;")))
            .contains("#[[Ljava.lang.CharSequence;")
        )
        // No test targets U[]: its marker is in no itable
        assertTrue(info("#[LU;").isInstanceOf[Trait])
        assertFalse(names(meta.traits).contains("#[LU;"))
        assertFalse(names(meta.itableTraits(cls("[LB;"))).contains("#[LU;"))

        // Reflection sees the interfaces of arrays only
        for (name <- Seq("[LB;", "[[Ljava.lang.String;"))
          assertEquals(
            Seq("java.io.Serializable", "java.lang.Cloneable").sorted,
            names(meta.interfaces(cls(name))).sorted
          )
    }
}

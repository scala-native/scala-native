package scala.scalanative
package linker

import org.junit.Assert._
import org.junit.Test

class TopLevelExternsTest {

  @Test def topLevelExternAnnotations(): Unit = {
    val PackageModule = nir.Global.Top("Main$package$")
    val ExternFunctionSymbol =
      PackageModule.member(nir.Sig.Extern("externFunction"))

    compileAndLoad(
      "Main.scala" -> """
        |import scala.scalanative.unsafe.{link, define, extern}
        |@extern
        |@link("MyCustomLink")
        |@define("MyCustomDefn") 
        |def externFunction(): Unit = extern
      """.stripMargin
    ) { defns =>
      defns
        .find(_.name == ExternFunctionSymbol)
        .orElse { fail("Not found extern function definition"); None }
        .foreach { defn =>
          assertTrue("isExtern", defn.attrs.isExtern)
          assertEquals(
            "link",
            Some(nir.Attr.Link("MyCustomLink")),
            defn.attrs.links.headOption
          )
          assertEquals(
            "define",
            Some(nir.Attr.Define("MyCustomDefn")),
            defn.attrs.preprocessorDefinitions.headOption
          )
        }
    }
  }

}

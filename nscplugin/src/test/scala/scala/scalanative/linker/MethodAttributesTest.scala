package scala.scalanative
package linker

import org.junit.Test
import org.junit.Assert._

class MethodAttributesTest {

  @Test def explicitLinkOrDefine(): Unit = {
    compileAndLoad(
      "Test.scala" ->
        """
          |import scala.scalanative.unsafe.{extern, link, define}
          |@link("common-lib")
          |@define("common-define")
          |@extern object Foo {
          |  @link("custom-lib") def withLink(): Int = extern
          |  @define("custom-define") def withDefine(): Int = extern
          |  def default(): Int = extern 
          |}
          """.stripMargin
    ) { defns =>
      val Module = nir.Global.Top("Foo$")
      val WithLinkMethod = Module.member(nir.Sig.Extern("withLink"))
      val WithDefineMethod = Module.member(nir.Sig.Extern("withDefine"))
      val DefaultMethod = Module.member(nir.Sig.Extern("default"))

      val CommonLink = nir.Attr.Link("common-lib")
      val CustomLink = nir.Attr.Link("custom-lib")
      val CommonDefine = nir.Attr.Define("common-define")
      val CustomDefine = nir.Attr.Define("custom-define")

      val expected = Seq(Module, WithLinkMethod, WithDefineMethod, DefaultMethod)
      val found = defns.filter { defn =>
        def checkLink(value: nir.Attr.Link, expected: Boolean) = assertEquals(
          s"${defn.name} - ${value}",
          expected,
          defn.attrs.links.contains(value)
        )
        def checkDefine(value: nir.Attr.Define, expected: Boolean) =
          assertEquals(
            s"${defn.name} - ${value}",
            expected,
            defn.attrs.preprocessorDefinitions.contains(value)
          )

        defn.name match {
          case Module =>
            checkLink(CommonLink, true)
            checkLink(CustomLink, false)
            checkDefine(CommonDefine, true)
            checkDefine(CustomDefine, false)
          case WithLinkMethod =>
            checkLink(CommonLink, false) // defined in module
            checkLink(CustomLink, true)
            checkDefine(CommonDefine, false) // defined in module
            checkDefine(CustomDefine, false)
          case WithDefineMethod =>
            checkLink(CommonLink, false) // defined in module
            checkLink(CustomLink, false)
            checkDefine(CommonDefine, false) // defined in module
            checkDefine(CustomDefine, true)
          case DefaultMethod =>
            checkLink(CommonLink, false) // defined in module
            checkLink(CustomLink, false)
            checkDefine(CommonDefine, false) // defined in module
            checkDefine(CustomDefine, false)
          case _ => ()
        }
        expected.contains(defn.name)
      }
      assertTrue(s"not found some defns, ${found.map(_.name)}", found.size == expected.size)
    }
  }

}

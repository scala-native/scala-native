package scala.scalanative

import java.nio.file.Files

import org.scalatest._

import scala.scalanative.api.CompilationFailedException

class NIRCompilerTest extends FlatSpec with Matchers with Inspectors {

  "The compiler" should "return products of compilation" in {
    val files =
      NIRCompiler { _ compile "class A" }
        .filter(Files.isRegularFile(_))
        .map(_.getFileName.toString)
    val expectedNames = Seq("A.class", "A.nir")
    files should contain theSameElementsAs expectedNames
  }

  it should "compile whole directories" in {
    val sources = Map(
      "A.scala" -> "class A",
      "B.scala" -> "class B extends A",
      "C.scala" -> "trait C",
      "D.scala" -> """class D extends B with C
                     |object E""".stripMargin
    )

    NIRCompiler.withSources(sources) {
      case (sourcesDir, compiler) =>
        val nirFiles =
          compiler.compile(sourcesDir) filter (Files
            .isRegularFile(_)) map (_.getFileName.toString)
        val expectedNames =
          Seq("A.class",
              "A.nir",
              "B.class",
              "B.nir",
              "C.class",
              "C.nir",
              "D.class",
              "D.nir",
              "E$.class",
              "E$.nir",
              "E.class")
        nirFiles should contain theSameElementsAs expectedNames
    }
  }

  it should "report compilation errors" in {
    assertThrows[api.CompilationFailedException] {
      NIRCompiler { _ compile "invalid" }
    }
  }

  it should "compile to a specified directory" in {
    val temporaryDir = Files.createTempDirectory("my-target")
    val nirFiles =
      NIRCompiler(outDir = temporaryDir) { _ compile "class A" }
        .filter(Files.isRegularFile(_))
    forAll(nirFiles) { _.getParent should be(temporaryDir) }
  }

  it should "report error for extern method without result type" in {
    // given
    val code =
      """import scala.scalanative.unsafe.extern
        |
        |@extern
        |object Dummy {
        |  def foo() = extern
        |}""".stripMargin

    // when
    val caught = intercept[CompilationFailedException] {
      NIRCompiler(_.compile(code))
    }

    // then
    caught.getMessage should include("extern method foo needs result type")
  }

}

package scala.scalanative

import java.nio.file.Files

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

import scala.scalanative.api.CompilationFailedException

class NIRCompilerTest extends AnyFlatSpec with Matchers with Inspectors {

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
        val expectedNames = Seq("A", "B", "C", "D", "E", "E$")
          .flatMap(name => Seq(s"$name.class", s"$name.nir"))
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
  }
  it should "report error for extern in val definition" in {
    // given
    val code =
      """import scala.scalanative.unsafe.extern
        |
        |@extern
        |object Dummy {
        |  val foo: Int = extern
        |}""".stripMargin
    // when
    val caught = intercept[CompilationFailedException] {
      NIRCompiler(_.compile(code))
    }
    caught.getMessage() should include(
      "`extern` cannot be used in val definition"
    )
  }

  it should "compile extern var definition" in {
    // given
    val code =
      """import scala.scalanative.unsafe.extern
        |
        |@extern
        |object Dummy {
        |  var foo: Int = extern
        |}""".stripMargin
    // when
    NIRCompiler(_.compile(code))
  }

  it should "not allow members of extern object to reference other externs" in {
    val code =
      """import scala.scalanative.unsafe.extern
          |
          |@extern object Dummy {
          |  def foo(): Int = extern
          |  def bar(): Int = foo()
          |}
          |""".stripMargin
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile(code))
    }.getMessage() should include(
      "Referencing other extern symbols in not supported"
    )
  }

  it should "allow to extend extern traits" in {
    val code =
      """import scala.scalanative.unsafe.extern
          |
          |@extern trait Dummy {
          |   var x: Int = extern
          |   def foo(): Int = extern
          |}
          |
          |@extern trait Dummy2 extends Dummy {
          |  def bar(): Int = extern
          |}
          |
          |@extern object Dummy extends Dummy
          |@extern object Dummy2 extends Dummy2
          |""".stripMargin

    NIRCompiler(_.compile(code))
  }

  it should "not allow to mix extern object with regular traits" in {
    val code =
      """
      |import scala.scalanative.unsafe.extern
      |
      |trait Dummy {
      |  def foo(): Int = ???
      |}
      |
      |@extern object Dummy extends Dummy
      |""".stripMargin
    intercept[CompilationFailedException](NIRCompiler(_.compile(code)))
      .getMessage() should include(
      "Extern object can only extend extern traits"
    )
  }

  it should "not allow to mix extern object with class" in {
    val code =
      """import scala.scalanative.unsafe.extern
        |
        |class Dummy {
        |  def foo(): Int = ???
        |}
        |
        |@extern object Dummy extends Dummy
        |""".stripMargin
    intercept[CompilationFailedException](NIRCompiler(_.compile(code)))
      .getMessage() should include(
      "Extern object can only extend extern traits"
    )
  }

  it should "not allow to mix extern traits with regular object" in {
    val code =
      """import scala.scalanative.unsafe.extern
          |
          |@extern trait Dummy {
          |  def foo(): Int = extern
          |}
          |
          |object Dummy extends Dummy
          |""".stripMargin
    intercept[CompilationFailedException](NIRCompiler(_.compile(code)))
      .getMessage() should include(
      "Extern traits can be only mixed with extern traits or objects"
    )
  }

  it should "not allow to mix extern traits with class" in {
    val code =
      """import scala.scalanative.unsafe.extern
          |
          |@extern trait Dummy {
          |  def foo(): Int = extern
          |}
          |
          |class DummyImpl extends Dummy
          |""".stripMargin
    intercept[CompilationFailedException](NIRCompiler(_.compile(code)))
      .getMessage() should include(
      "Extern traits can be only mixed with extern traits or objects"
    )
  }

  it should "report error for intrinsic resolving of not existing field" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.runtime.Intrinsics
          |class Foo {
          | val fieldRawPtr =  Intrinsics.classFieldRawPtr(this, "myField")
          |}""".stripMargin
        )
      )
    }.getMessage should include("class Foo does not contain field myField")
  }

  it should "report error for intrinsic resolving of immutable field" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.runtime.Intrinsics
           |class Foo {
           | val myField = 42
           | val fieldRawPtr =  Intrinsics.classFieldRawPtr(this, "myField")
           |}""".stripMargin
        )
      )
    }.getMessage should include(
      "Resolving pointer of immutable field myField in class Foo is not allowed"
    )
  }

  it should "report error for intrinsic resolving of immutable field introduced by trait" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.runtime.Intrinsics
            |trait Foo { val myField = 42}
            |class Bar extends Foo {
            | val fieldRawPtr =  Intrinsics.classFieldRawPtr(this, "myField")
            |}""".stripMargin
        )
      )
    }.getMessage should include(
      // In Scala 3 trait would be inlined into class
      "Resolving pointer of immutable field myField in "
    ) // trait Foo is not allowed")
  }

  it should "report error for intrinsic resolving of immutable field introduced by inheritence" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.runtime.Intrinsics
             |abstract class Foo { val myField = 42}
             |class Bar extends Foo {
             | val fieldRawPtr =  Intrinsics.classFieldRawPtr(this, "myField")
             |}""".stripMargin
        )
      )
    }.getMessage should include(
      "Resolving pointer of immutable field myField in class Foo is not allowed"
    )
  }

  it should "handle extern methods with generic types" in {
    // issue #2727
    NIRCompiler(_.compile("""
      |import scala.scalanative.unsafe._
      |@extern
      |object foo {
      |  def baz[A](a: Ptr[A]): Unit = extern
      |}
      |
      |object Test {
      |  def main() = foo.baz(???)
      |}
      |""".stripMargin))
  }

  it should "report error on default argument in extern method" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
      |import scala.scalanative.unsafe._
      |@extern
      |object foo {
      |  def baz(a:Int = 1): Unit = extern
      |}
      |""".stripMargin))
    }.getMessage should include(
      "extern method cannot have default argument"
    )
  }
  it should "report error on default argument mixed with general argument in extern method" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
      |import scala.scalanative.unsafe._
      |@extern
      |object foo {
      |  def baz(a: Double, b:Int = 1): Unit = extern
      |}
      |""".stripMargin))
    }.getMessage should include(
      "extern method cannot have default argument"
    )
  }
  it should "report error on default arguments in extern method" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
      |import scala.scalanative.unsafe._
      |@extern
      |object foo {
      |  def baz(a: Double=1.0, b:Int = 1): Unit = extern
      |}
      |""".stripMargin))
    }.getMessage should include(
      "extern method cannot have default argument"
    )
  }

  it should "report error when closing over local statein CFuncPtr" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """
          |import scala.scalanative.unsafe._
          |object Main {
          |  val z = 12
          |  def f(ptr: CFuncPtr1[CInt, CInt]): Unit = println(ptr(3))
          |
          |  def test(): Unit = {
          |    val x = 10
          |    f(CFuncPtr1.fromScalaFunction(y => x + y + z))
          |  }
          |
          |  def main(args: Array[String]): Unit = test()
          |}
          |""".stripMargin
        )
      )
    }.getMessage should include(
      "Closing over local state of value x in function transformed to CFuncPtr results in undefined behaviour"
    )
  }

  it should "allow to export module method" in {
    try
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
              |object ExportInModule {
              |  @exported
              |  def foo(l: Int): Int = l
              |  @exportAccessors()
              |  val bar: Double = 0.42d
              |}""".stripMargin
        )
      )
    catch {
      case ex: CompilationFailedException =>
        fail(s"Unexpected compilation failure: ${ex.getMessage()}", ex)
    }
  }
  val MustBeStatic =
    "Exported members must be statically reachable, definition within class or trait is currently unsupported"

  it should "report error when exporting class method" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
            |class ExportInClass() {
            |  @exported
            |  def foo(l: Int): Int = l
            |}""".stripMargin
        )
      )
    }.getMessage should include(MustBeStatic)
  }

  it should "report error when exporting non static module method" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
          |class Wrapper() {
          | object inner {
          |   @exported
          |   def foo(l: Int): Int = l
          | }
          |}""".stripMargin
        )
      )
    }.getMessage should include(MustBeStatic)
  }

  val CannotExportField =
    "Cannot export field, use `@exportAccessors()` annotation to generate external accessors"
  it should "report error when exporting module field" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
          |object valuesNotAllowed {
          |  @exported val foo: Int = 0
          |}""".stripMargin
        )
      )
    }.getMessage should include(CannotExportField)
  }

  it should "report error when exporting module variable" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
          |object variableNotAllowed {
          |  @exported var foo: Int = 0
          |}""".stripMargin
        )
      )
    }.getMessage should include(CannotExportField)
  }

  // https://github.com/scala-native/scala-native/issues/3228
  it should "allow to define fields in extern object" in NIRCompiler(_.compile {
    """
    |import scala.scalanative.unsafe._
    |
    |@extern
    |object Foo {
    |  final val bar = 42
    |}""".stripMargin
  })

}

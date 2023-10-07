package scala.scalanative

import java.nio.file.Files

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.compileAndLoad
import scala.scalanative.buildinfo.ScalaNativeBuildInfo._

class NIRCompilerTest {

  private def linkWithProps(
      sources: (String, String)*
  ): Unit = {
    val outDir = Files.createTempDirectory("native-test-out")
    val compiler = NIRCompiler.getCompiler(outDir)
    val sourcesDir = NIRCompiler.writeSources(sources.toMap)
    compiler.compile(sourcesDir)
  }

  @Test def returnCompilationProducts(): Unit = {
    val files =
      NIRCompiler { _ compile "class A" }
        .filter(Files.isRegularFile(_))
        .map(_.getFileName.toString)
    val expectedNames = Seq("A.class", "A.nir")
    assertTrue(files.diff(expectedNames).isEmpty)
  }

  @Test def compileDirectory(): Unit = {
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
        assertTrue(nirFiles.diff(expectedNames).isEmpty)
    }
  }

  @Test def reportCompilationErrors(): Unit = {
    assertThrows(
      classOf[api.CompilationFailedException],
      () => NIRCompiler { _ compile "invalid" }
    )
  }

  @Test def compileSpecifiedDirectory(): Unit = {
    val temporaryDir = Files.createTempDirectory("my-target")
    val nirFiles =
      NIRCompiler(outDir = temporaryDir) { _ compile "class A" }
        .filter(Files.isRegularFile(_))
    nirFiles.foreach { file =>
      assertEquals(temporaryDir, file.getParent())
    }
  }

  @Test def externMethodWithoutResultType(): Unit = {
    // given
    val code =
      """import scala.scalanative.unsafe.extern
        |
        |@extern
        |object Dummy {
        |  def foo() = extern
        |}""".stripMargin

    // when
    assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile(code))
    )
  }

  @Test def externInValDefinition(): Unit = {
    // given
    val code =
      """import scala.scalanative.unsafe.extern
        |
        |@extern
        |object Dummy {
        |  val foo: Int = extern
        |}""".stripMargin
    // when
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile(code))
    )
    assertTrue(
      err
        .getMessage()
        .contains(
          "`extern` cannot be used in val definition"
        )
    )
  }

  @Test def externVarDefiniton(): Unit = {
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

  @Test def externMemberReferencingExtern(): Unit = {
    val code =
      """import scala.scalanative.unsafe.extern
          |
          |@extern object Dummy {
          |  def foo(): Int = extern
          |  def bar(): Int = foo()
          |}
          |""".stripMargin

    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile(code))
    )
    assertTrue(
      err
        .getMessage()
        .contains("Referencing other extern symbols in not supported")
    )
  }

  @Test def externExternTrait(): Unit = {
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

  @Test def mixExternObjectWithNonExternTrait(): Unit = {
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
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile(code))
    )

    assertTrue(
      err
        .getMessage()
        .contains(
          "Extern object can only extend extern traits"
        )
    )
  }

  @Test def mixExternObjectWithNonExternClass(): Unit = {
    val code =
      """import scala.scalanative.unsafe.extern
        |
        |class Dummy {
        |  def foo(): Int = ???
        |}
        |
        |@extern object Dummy extends Dummy
        |""".stripMargin
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile(code))
    )

    assertTrue(
      err
        .getMessage()
        .contains(
          "Extern object can only extend extern traits"
        )
    )
  }

  @Test def mixExternTraitWithNonExternObject(): Unit = {
    val code =
      """import scala.scalanative.unsafe.extern
          |
          |@extern trait Dummy {
          |  def foo(): Int = extern
          |}
          |
          |object Dummy extends Dummy
          |""".stripMargin
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile(code))
    )

    assertTrue(
      err
        .getMessage()
        .contains(
          "Extern traits can be only mixed with extern traits or objects"
        )
    )
  }

  @Test def mixExternTraitsWithNonExternClass(): Unit = {
    val code =
      """import scala.scalanative.unsafe.extern
          |
          |@extern trait Dummy {
          |  def foo(): Int = extern
          |}
          |
          |class DummyImpl extends Dummy
          |""".stripMargin
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile(code))
    )

    assertTrue(
      err
        .getMessage()
        .contains(
          "Extern traits can be only mixed with extern traits or objects"
        )
    )
  }

  @Test def allowImplicitClassInExtern(): Unit = NIRCompiler(
    _.compile(
      """import scala.scalanative.unsafe.extern
          |@extern object Dummy { 
          |  implicit class Ext(val v: Int) { 
          |    def convert(): Long = Dummy.implicitConvert(v) + Dummy.doConvert(v) 
          |  }
          |  implicit def implicitConvert(v: Int): Long = extern
          |  def doConvert(v: Int): Long = extern
          |}
          |""".stripMargin
    )
  )

  @Test def disallowNonExternImplicitInExtern(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe.extern
              |@extern object Dummy {
              |  implicit def implicitFunction: Long = 42
              |}
              |""".stripMargin
          )
        )
    )
    assertTrue(
      err
        .getMessage()
        .contains("methods in extern objects must have extern body")
    )
  }

  @Test def nonExistingClassFieldPointer(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.runtime.Intrinsics
          |class Foo {
          | val fieldRawPtr =  Intrinsics.classFieldRawPtr(this, "myField")
          |}""".stripMargin
          )
        )
    )
    assertTrue(
      err.getMessage().contains("class Foo does not contain field myField")
    )
  }

  @Test def immutableClassFieldPointer(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.runtime.Intrinsics
           |class Foo {
           | val myField = 42
           | val fieldRawPtr =  Intrinsics.classFieldRawPtr(this, "myField")
           |}""".stripMargin
          )
        )
    )
    assertTrue(
      err
        .getMessage()
        .contains(
          "Resolving pointer of immutable field myField in class Foo is not allowed"
        )
    )
  }

  @Test def traitImmutableFieldPointer(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.runtime.Intrinsics
            |trait Foo { val myField = 42}
            |class Bar extends Foo {
            | val fieldRawPtr =  Intrinsics.classFieldRawPtr(this, "myField")
            |}""".stripMargin
          )
        )
    )
    assertTrue(
      err
        .getMessage()
        .contains(
          // In Scala 3 trait would be inlined into class
          "Resolving pointer of immutable field myField in "
        )
    ) // trait Foo is not allowed")
  }

  @Test def classImmutableFieldPointer(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.runtime.Intrinsics
             |abstract class Foo { val myField = 42}
             |class Bar extends Foo {
             | val fieldRawPtr =  Intrinsics.classFieldRawPtr(this, "myField")
             |}""".stripMargin
          )
        )
    )
    assertTrue(
      err
        .getMessage()
        .contains(
          "Resolving pointer of immutable field myField in class Foo is not allowed"
        )
    )
  }

  @Test def genericExternMethod(): Unit = {
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

  @Test def externMethodDefaultArgument(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
      |import scala.scalanative.unsafe._
      |@extern
      |object foo {
      |  def baz(a:Int = 1): Unit = extern
      |}
      |""".stripMargin))
    )
    assertTrue(
      err
        .getMessage()
        .contains(
          "extern method cannot have default argument"
        )
    )
  }

  @Test def externMethodWithMixedDefaultArguments(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
      |import scala.scalanative.unsafe._
      |@extern
      |object foo {
      |  def baz(a: Double, b:Int = 1): Unit = extern
      |}
      |""".stripMargin))
    )

    assertTrue(
      err.getMessage.contains(
        "extern method cannot have default argument"
      )
    )
  }

  @Test def externMethodDefaultArguments(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
      |import scala.scalanative.unsafe._
      |@extern
      |object foo {
      |  def baz(a: Double=1.0, b:Int = 1): Unit = extern
      |}
      |""".stripMargin))
    )
    assertTrue(
      err
        .getMessage()
        .contains(
          "extern method cannot have default argument"
        )
    )
  }

  @Test def cFuncPtrWithLocalState(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
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
    )
    assertTrue(
      err
        .getMessage()
        .contains(
          "Closing over local state of value x in function transformed to CFuncPtr results in undefined behaviour"
        )
    )
  }

  @Test def exportModuleMethod(): Unit = {
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
        fail(s"Unexpected compilation failure: ${ex}")
    }
  }
  val MustBeStatic =
    "Exported members must be statically reachable, definition within class or trait is currently unsupported"

  @Test def exportClassMethod(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
            |class ExportInClass() {
            |  @exported
            |  def foo(l: Int): Int = l
            |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(MustBeStatic))
  }

  @Test def exportNonStaticModuleMethod(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
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
    )
    assertTrue(err.getMessage().contains(MustBeStatic))
  }

  val CannotExportField =
    "Cannot export field, use `@exportAccessors()` annotation to generate external accessors"
  @Test def exportModuleField(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object valuesNotAllowed {
          |  @exported val foo: Int = 0
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(CannotExportField))
  }

  @Test def exportModuleVariable(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object variableNotAllowed {
          |  @exported var foo: Int = 0
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(CannotExportField))
  }

  // https://github.com/scala-native/scala-native/issues/3228
  @Test def externObjectFields(): Unit = NIRCompiler(_.compile {
    """
    |import scala.scalanative.unsafe._
    |
    |@extern
    |object Foo {
    |  final val bar = 42
    |}""".stripMargin
  })

  @Test def linktimeResolvedValsInBlocks(): Unit = {
    val caught = assertThrows(
      classOf[CompilationFailedException],
      () =>
        linkWithProps(
          "props.scala" ->
            """package scala.scalanative
            |object props{
            |   @scalanative.unsafe.resolvedAtLinktime
            |   def linktimeProperty = {
            |     val foo = 42
            |     foo
            |  }
            |}""".stripMargin,
          "main.scala" ->
            """import scala.scalanative.props._
            |object Main {
            |  def main(args: Array[String]): Unit = {
            |    if(linktimeProperty != 42) ???
            |  }
            |}""".stripMargin
        )
    )
    // Multiple errors
    // caught.assertTrue(getMessage.contains("Linktime resolved block can only contain other linktime resolved def defintions"))
  }

  @Test def propertyWithoutResolvedRhs(): Unit = {
    val caught = assertThrows(
      classOf[CompilationFailedException],
      () =>
        linkWithProps(
          "props.scala" ->
            """package scala.scalanative
            |object props{
            |   @scalanative.unsafe.resolvedAtLinktime("foo")
            |   def linktimeProperty: Boolean = true
            |}""".stripMargin,
          "main.scala" ->
            """import scala.scalanative.props._
            |object Main {
            |  def main(args: Array[String]): Unit = {
            |    if(linktimeProperty) ???
            |  }
            |}""".stripMargin
        )
    )
    assertTrue(
      caught.getMessage.matches(
        "Link-time resolved property must have scala.scalanative.*resolved as body"
      )
    )
  }

  @Test def propertyWithNullRhs(): Unit = {
    val caught = assertThrows(
      classOf[CompilationFailedException],
      () =>
        linkWithProps(
          "props.scala" -> """
             |package scala.scalanative
             |object props{
             |   @scalanative.unsafe.resolvedAtLinktime("prop")
             |   def linktimeProperty: Boolean = null.asInstanceOf[Boolean]
             |}
             |""".stripMargin,
          "main.scala" -> """
            |import scala.scalanative.props._
            |object Main {
            |  def main(args: Array[String]): Unit = {
            |    if(linktimeProperty) ???
            |  }
            |}""".stripMargin
        )
    )
    assertTrue(
      caught.getMessage.matches(
        "Link-time resolved property must have scala.scalanative.*resolved as body"
      )
    )
  }

  @Test def propertyWithNullName(): Unit = {
    val caught = assertThrows(
      classOf[CompilationFailedException],
      () =>
        linkWithProps(
          "props.scala" ->
            """package scala.scalanative
            |object props{
            |   @scalanative.unsafe.resolvedAtLinktime(withName = null.asInstanceOf[String])
            |   def linktimeProperty: Boolean = scala.scalanative.unsafe.resolved
            |}""".stripMargin,
          "main.scala" ->
            """import scala.scalanative.props._
            |object Main {
            |  def main(args: Array[String]): Unit = {
            |    if(linktimeProperty) ???
            |  }
            |}""".stripMargin
        )
    )
    assertEquals(
      "Name used to resolve link-time property needs to be non-null literal constant",
      caught.getMessage()
    )
  }

  @Test def propertyNoResultType(): Unit = {
    val caught = assertThrows(
      classOf[CompilationFailedException],
      () =>
        linkWithProps(
          "props.scala" ->
            """package scala.scalanative
            |object props{
            |   @scalanative.unsafe.resolvedAtLinktime("foo")
            |   def linktimeProperty = scala.scalanative.unsafe.resolved
            |}""".stripMargin,
          "main.scala" ->
            """import scala.scalanative.props._
            |object Main {
            |  def main(args: Array[String]): Unit = {
            |    if(linktimeProperty) ???
            |  }
            |}""".stripMargin
        )
    )
    assertEquals(
      "value resolved at link-time linktimeProperty needs result type",
      caught.getMessage()
    )
  }

  @Test def mixLinktimeAndRuntimeConditions(): Unit = {
    val caught = assertThrows(
      classOf[CompilationFailedException],
      () =>
        linkWithProps(
          "props.scala" ->
            """package scala.scalanative
            |
            |object props{
            |   @scalanative.unsafe.resolvedAtLinktime("prop")
            |   def linktimeProperty: Boolean = scala.scalanative.unsafe.resolved
            |
            |   def runtimeProperty = true
            |}
            |""".stripMargin,
          "main.scala" -> """
           |import scala.scalanative.props._
           |object Main {
           |  def main(args: Array[String]): Unit = {
           |    if(linktimeProperty || runtimeProperty) ??? 
           |  }
           |}""".stripMargin
        )
    )
    assertEquals(
      "Mixing link-time and runtime conditions is not allowed",
      caught.getMessage()
    )
  }

}

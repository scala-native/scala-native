package scala.scalanative

import java.nio.file.Files

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.StaticForwardersSuite.compileAndLoad
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

  @Test def sourcePositions(): Unit = compileAndLoad(
    "Test.scala" -> """class TopLevel()
    |object Foo {
    |  var field: Int = 42
    |  def defn: Unit = println("Hello World")
    |  def defn2: Unit = {
    |    val x: Any = 10
    |    def innerDefn(x: Any) = { 
    |      println("foo")
    |    }
    |    innerDefn(x)
    |  }
    |}
    """.stripMargin
  ) { loaded =>
    import nir._

    val TopLevel = Global.Top("TopLevel")
    val Foo = Global.Top("Foo")
    val FooModule = Global.Top("Foo$")
    val sourceFile = "Test.scala"

    object Definition {
      def unapply(
          defn: Defn
      ): Option[(Global.Top, Sig.Unmangled, nir.Position, Seq[nir.Inst])] =
        defn match {
          case Defn.Define(_, Global.Member(top: Global.Top, sig), _, insts) =>
            Some((top, sig.unmangled, defn.pos, insts))
          case Defn.Var(_, Global.Member(top: Global.Top, sig), _, _) =>
            Some((top, sig.unmangled, defn.pos, Nil))
          case _ => None
        }
    }

    def `isScala2.12` = scalaVersion.startsWith("2.12.")
    def `isScala2.13` = scalaVersion.startsWith("2.13.")
    def isScala3 = scalaVersion.startsWith("3.")

    for (defn <- loaded) {
      def checkPos(line: Int, column: Int)(pos: nir.Position) = {
        val clue =
          s"${defn.name} - expected=$line:$column, actual=${pos.line}:${pos.column}"
        assertTrue(clue, pos.source.getPath().endsWith(sourceFile))
        assertEquals(clue, line, pos.line)
        assertEquals(clue, column, pos.column)
      }
      def checkLinesRange(range: Range)(
          positions: Iterable[nir.Position]
      ): Unit = {
        positions.foreach { pos =>
          assertTrue(s"${defn.name}", pos.source.getPath().endsWith(sourceFile))
          assertTrue(s"${defn.name}", range.contains(pos.line))
        }
      }
      val pos = defn.pos
      assertTrue(pos.source.getPath().endsWith(sourceFile))
      defn match {
        case Defn.Class(_, TopLevel, _, _) =>
          checkPos(0, 6)(pos)
        case Definition(TopLevel, Sig.Ctor(Nil), pos, insts) =>
          if (`isScala2.12`) {
            checkPos(1, 0)(pos) // wrong
            checkLinesRange(1 to 1)(insts.map(_.pos))
          } else {
            checkPos(0, 14)(pos)
            checkLinesRange(0 to 0)(insts.map(_.pos))
          }
        case Defn.Class(_, Foo, _, _) =>
          checkPos(1, 7)(pos)
        case Defn.Module(_, FooModule, _, _) =>
          checkPos(1, 7)(pos)
        case Definition(FooModule, Sig.Ctor(Nil), pos, insts) =>
          if (`isScala2.13`) checkPos(1, 11)(pos)
          else if (isScala3) checkPos(2, 2)(pos)
          if (`isScala2.12`) () // scalac sets wrong position, line 12
          else checkLinesRange(1 to 2)(insts.map(_.pos))
        // proxies to module implemention
        case Definition(
              Foo,
              Sig.Method("field" | "field_$eq", _, _),
              pos,
              insts
            ) =>
          (pos +: insts.map(_.pos)).foreach(checkPos(2, 6))
        case Definition(Foo, Sig.Method("defn", _, _), pos, insts) =>
          (pos +: insts.map(_.pos)).foreach(checkPos(3, 6))
        case Definition(Foo, Sig.Method("defn2", _, _), pos, insts) =>
          (pos +: insts.map(_.pos)).foreach(checkPos(4, 6))
        // Actual methods
        case Definition(
              FooModule,
              Sig.Method("field", _, _) | Sig.Method("field_$eq", _, _) |
              Sig.Field("field", _),
              pos,
              insts
            ) =>
          checkPos(2, 6)(pos)
          checkLinesRange(2 to 2)(insts.map(_.pos))
        case Definition(FooModule, Sig.Method("defn", _, _), pos, insts) =>
          checkPos(3, 6)(pos)
          checkLinesRange(3 to 3)(insts.map(_.pos))
        case Definition(FooModule, Sig.Method("defn2", _, _), pos, insts) =>
          checkPos(4, 6)(pos)
          checkLinesRange(4 to 9)(insts.map(_.pos))
        case Definition(
              FooModule,
              Sig.Method("innerDefn$1", _, _),
              pos,
              insts
            ) =>
          checkPos(6, 8)(pos)
          checkLinesRange(6 to 8)(insts.map(_.pos))

        case Definition(
              FooModule,
              Sig.Method("writeReplace", _, _),
              pos,
              insts
            ) =>
          checkPos(1, 7)(pos)

        case other => fail(s"Unexpected defn: ${nir.Show(other)}")
      }
    }
  }

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

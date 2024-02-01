package org.scalanative

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.api._
import scala.scalanative.util.Scope
import scala.scalanative.io.VirtualDirectory
import scalanative.NIRCompiler
import java.nio.file.Files

class NativeCompilerTest:

  def nativeCompilation(source: String): Unit = {
    try NIRCompiler(_.compile(source))
    catch {
      case ex: CompilationFailedException =>
        fail(s"Failed to compile source: ${ex}")
    }
  }

  def compileAll(sources: (String, String)*): Unit = {
    Scope { implicit in =>
      val outDir = Files.createTempDirectory("native-test-out")
      val compiler = scalanative.NIRCompiler.getCompiler(outDir)
      val sourcesDir = scalanative.NIRCompiler.writeSources(sources.toMap)
      val dir = VirtualDirectory.real(outDir)

      try scalanative.NIRCompiler(_.compile(sourcesDir))
      catch {
        case ex: CompilationFailedException =>
          fail(s"Failed to compile source: $ex")
      }
    }
  }

  @Test def issue8612(): Unit = nativeCompilation(
    """
    |object Foo1:
    |  def assert1(x: Boolean) = if !x then ???
    |  inline def assert2(x: Boolean) = if !x then ???
    |  inline def assert3(inline x: Boolean) = if !x then ???
    |
    |  assert1(???)
    |  assert2(???)
    |  assert3(???)
    |
    |object Foo2:
    |  def assert1(x: Boolean) = if !x then ???
    |  transparent inline def assert2(x: Boolean) = if !x then ???
    |  transparent inline def assert3(inline x: Boolean) = if !x then ???
    |
    |  assert1(???)
    |  assert2(???)
    |  assert3(???)
    |""".stripMargin
  )

  @Test def issue505(): Unit = nativeCompilation("""
  |object Test {
  |  def main(args: Array[String]): Unit = {
  |    val a: Int = synchronized(1)
  |    val b: Long = synchronized(1L)
  |    val c: Boolean = synchronized(true)
  |    val d: Float = synchronized(1f)
  |    val e: Double = synchronized(1.0)
  |    val f: Byte = synchronized(1.toByte)
  |    val g: Char = synchronized('1')
  |    val h: Short = synchronized(1.toShort)
  |    val i: String = synchronized("Hello")
  |    val j: List[Int] = synchronized(List(1))
  |    synchronized(())
  |  }
  |}
  """.stripMargin)

  // Reproducer for https://github.com/typelevel/shapeless-3/pull/61#discussion_r779376350
  @Test def inlineMacroWithLazyVals(): Unit = {
    compileAll(
      "Test.scala" -> "@main def run(): Unit = Macros.foo()",
      "Macros.scala" -> """
        |import scala.quoted.*
        |object Macros:
        |  def foo_impl()(using q: Quotes): Expr[Unit] = '{
        |     ${val x = ReflectionUtils(quotes).Mirror(); '{()} }
        |     println()
        |   }
        |
        |  inline def foo(): Unit = ${foo_impl()}
        |end Macros
        |
        |class ReflectionUtils[Q <: Quotes](val q: Q) {
        |  given q.type = q // Internally defined as lazy val, leading to problems
        |  import q.reflect._
        |
        |  case class Mirror(arg: String)
        |  object Mirror{
        |    def apply(): Mirror = Mirror("foo")
        |  }
        |}""".stripMargin
    )
  }

  @Test def allowExtensionInExtern(): Unit = nativeCompilation(
    """import scala.scalanative.unsafe.extern
      |@extern object Dummy { 
      |  extension(v: Int) { 
      |    def convert(): Long = Dummy.implicitConvert(v) + Dummy.doConvert(v) 
      |  }
      |  implicit def implicitConvert(v: Int): Long = extern
      |  def doConvert(v: Int): Long = extern
      |}
      |""".stripMargin
  )

  @Test def disallowExtensionInExternWithExtern(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""import scala.scalanative.unsafe.extern
        |@extern object Dummy { 
        |  extension(v: Int) { 
        |    def convert(): Long = extern
        |  }
        |}
        |""".stripMargin))
    )
    assertTrue(
      err
        .getMessage()
        .contains("Extensions cannot be defined as extern methods")
    )
  }

  @Test def issue3231MultiLevelExport(): Unit = {
    // Exporting extern function should work for recursive exports
    compileAll(
      "level_1.scala" -> s"""
        |package issue.level1
        |
        |import scala.scalanative.unsafe.extern
        |
        |@extern
        |private[issue] object extern_functions:
        |  def test(bla: Int, args: Any*): Unit = extern
        |
        |export extern_functions.* // should comppile
        |
        """.stripMargin,
      "level_2.scala" -> s"""
        |package issue.level2
        |
        |export _root_.issue.level1.test
        |
        """.stripMargin,
      "level_3.scala" -> s"""
        |package issue.level3
        |
        |export _root_.issue.level2.test
        |
        """.stripMargin,
      "level_4.scala" -> s"""
        |package issue.level4
        |
        |export _root_.issue.level3.test
        |
      """.stripMargin
    )
  }

  // https://github.com/scala-native/scala-native/issues/3726
  @Test def issue3726(): Unit = compileAll(
    "Test.scala" -> s"""
    |import _root_.scala.scalanative.unsafe.*
    |
    |object structs:
    |  opaque type MyStruct = CStruct1[CInt]
    |  object MyStruct:
    |    given _tag: Tag[MyStruct] = ???
    |    extension (struct: MyStruct)
    |      def field: CInt = struct._1
    |      def field_=(value: CInt): Unit = !struct.at1 = value
    |
    |object all:
    |  export structs.MyStruct
    |
    |object nested:
    |  export all.MyStruct
    |
    |def Test1 = {
    |  import structs.*
    |  val myStruct = stackalloc[MyStruct]()
    |  (!myStruct).field = 2
    |  println((!myStruct).field)
    |}
    |
    |def Test2 = {
    |  import all.*
    |  val myStruct = stackalloc[MyStruct]()
    |  (!myStruct).field = 2
    |  println((!myStruct).field)
    |}
    |
    |def Test3 = {
    |  import nested.*
    |  val myStruct = stackalloc[MyStruct]()
    |  (!myStruct).field = 2
    |  println((!myStruct).field)
    |}
    |""".stripMargin
  )

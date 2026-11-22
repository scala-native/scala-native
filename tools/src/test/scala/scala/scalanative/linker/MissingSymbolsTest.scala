package scala.scalanative
package linker

import java.io.File
import java.nio.file.{Files, Paths}
import java.sql.Time

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.build.{Config, Discover, IO, Logger, ScalaNative}
import scala.scalanative.buildinfo.ScalaNativeBuildInfo
import scala.scalanative.checker.Check
import scala.scalanative.optimizer.assertContainsAll
import scala.scalanative.util.Scope
import scala.scalanative.{LinkerSpec, NIRCompiler}

class MissingSymbolsTest extends LinkerSpec {

  private val mainClass = "Test"
  private val sourceFile = "Test.scala"

  @Test def missingSymbolStacktrace(): Unit = {
    doesNotLink(
      entry = mainClass,
      Map(
        sourceFile ->
          s"""|
              |object Foo{
              |  def getTimeString(): String = Bar.getTimeString()
              |}
              |
              |object Bar{
              |  def getTimeString(): String = {
              |    val time = java.sql.Time.valueOf("")
              |    val time2 = new java.sql.Time(0L)
              |    ???
              |  }
              |}
              |
              |object $mainClass{
              | def main(args: Array[String]): Unit = {
              |   val unreachable = Foo.getTimeString()
              | }
              |}
              |""".stripMargin
      )
    ) {
      case (config, result) =>
        assertEquals("unreachable", 3, result.unreachable.size)
        assertContainsAll(
          "kind-symbols",
          Seq(
            ("type", "java.sql.Time", None),
            ("constructor", "java.sql.Time", Some(Seq("long"))),
            ("method", "java.sql.Time.valueOf", Some(Seq("java.lang.String")))
          ),
          result.unreachable
            .map(_.symbol)
            .map(v => (v.kind, v.name, v.argTypes))
        )
        val TimeType = nir.Global.Top("java.sql.Time")
        val TimeCtor = TimeType.member(nir.Sig.Ctor(Seq(nir.Type.Long)))
        val TimeValueOf = TimeType.member(
          nir.Sig.Method(
            "valueOf",
            Seq(nir.Rt.String, nir.Type.Ref(TimeType)),
            nir.Sig.Scope.PublicStatic
          )
        )
        assertContainsAll(
          "names",
          Seq(TimeType, TimeCtor, TimeValueOf),
          result.unreachable.map(_.name)
        )

        result.unreachable.foreach { symbol =>
          val backtrace =
            symbol.backtrace.map(v =>
              (v.symbol.kind, v.symbol.name, v.filename, v.line)
            )
          // format: off
          assertEquals("backtrace", List(
            ("method", "Bar$.getTimeString", sourceFile, if(symbol.name == TimeCtor) 9 else 8),
            ("method", "Foo$.getTimeString", sourceFile, 3),
            ("method", "Test$.main", sourceFile, 16),
            ("method", "Test.main", sourceFile, 15)
          ), backtrace)
          // format: on
        }
    }
  }

  @Test def unreachableInTypeParent(): Unit = {
    doesNotLink(
      entry = mainClass,
      Map(
        sourceFile ->
          s"""|
              |object $mainClass{
              | def main(args: Array[String]): Unit = {
              |   class Foo(n: Long) extends java.sql.Time(n)
              |   val x = new Foo(0L)
              | }
              |}
              |""".stripMargin
      )
    ) {
      case (config, result) =>
        assertEquals("unreachable", 2, result.unreachable.size)
        assertContainsAll(
          "kind-symbols",
          Seq(
            "type" -> "java.sql.Time",
            "constructor" -> "java.sql.Time"
          ),
          result.unreachable.map(_.symbol).map(v => (v.kind, v.name))
        )

        result.unreachable
          .find(_.symbol.name == "java.sql.Time")
          .map { symbol =>
            val from = symbol.backtrace.head
            assertEquals("type", from.symbol.kind)
            assertEquals("Test$Foo$1", from.symbol.name)
          }
          .getOrElse(fail("Not found required unreachable symbol"))
    }
  }

  // Methods of allocated classess have a special delayed handling needed to correctly
  // distinguish unimplemented methods from not yet reached
  @Test def unreachableDelayedMethod(): Unit = {
    doesNotLink(
      entry = mainClass,
      Map(
        sourceFile ->
          s"""|
              |object $mainClass{
              |  def main(args: Array[String]): Unit = {
              |    val theFields = this.getClass().getDeclaredFields
              |    println(theFields)
              |  }
              |}
              |""".stripMargin
      )
    ) {
      case (config, result) =>
        // Testing if is able to get non-empty backtrace.
        // If reference tacking of delayed methods is invalid we would get empty list here
        result.unreachable
          .find(_.symbol.name == "java.lang.Class.getDeclaredFields")
          .map { symbol =>
            assertTrue("no-backtrace", symbol.backtrace.nonEmpty)
          }
          .getOrElse(fail("Not found required unreachable symbol"))
    }
  }

  // Compiles `linked` and `sources` in separate runs: `linked` provides the
  // NIR, while `sources` are compiled against their own (Java) API stubs
  private def linkSeparately(
      linked: Map[String, String],
      sources: Map[String, String]
  ): ReachabilityAnalysis = {
    val outDir = Files.createTempDirectory("native-test-out")
    try
      Scope { implicit in =>
        Seq(linked, sources).foreach { srcs =>
          NIRCompiler.withSourceDir(srcs)(
            NIRCompiler.getCompiler(outDir).compile
          )
        }
        val classpath = ScalaNativeBuildInfo.nativeRuntimeClasspath
          .split(File.pathSeparator)
          .map(Paths.get(_)) :+ outDir
        val config = Config.empty
          .withBaseDir(outDir)
          .withClassPath(classpath.toSeq)
          .withMainClass(Some(mainClass))
          .withCompilerConfig(
            _.withTargetTriple("x86_64-unknown-unknown")
              .withClang(Discover.clang())
              .withClangPP(Discover.clangpp())
          )
          .withLogger(Logger.nullLogger)
        Link(config, ScalaNative.entries(config))
      }
    finally IO.deleteRecursive(outDir)
  }

  // Issue #5324: Baz is allocated only via its subclass, the caller is
  // compiled against the Java API declaring a method missing in Baz's NIR
  @Test def unreachableMethodOfClassAllocatedOnlyViaSubclass(): Unit = {
    val result = linkSeparately(
      linked = Map(
        "Baz.scala" ->
          """|package foo.bar
             |class Baz
             |object Baz extends Baz {
             |  def get(): Baz = this
             |}
             |""".stripMargin
      ),
      sources = Map(
        "Baz.java" ->
          """|package foo.bar;
             |public class Baz {
             |  public static Baz get() { return null; }
             |  public native long missing();
             |}
             |""".stripMargin,
        sourceFile ->
          s"""|
              |object $mainClass{
              |  def main(args: Array[String]): Unit = {
              |    println(foo.bar.Baz.get().missing())
              |  }
              |}
              |""".stripMargin
      )
    )
    result match {
      case result: ReachabilityAnalysis.Failure =>
        result.unreachable
          .find(_.symbol.name == "foo.bar.Baz.missing")
          .map { symbol =>
            assertEquals("method", symbol.symbol.kind)
            val from = symbol.backtrace.head
            assertEquals("Test$.main", from.symbol.name)
            assertEquals(sourceFile, from.filename)
            assertEquals(4, from.line)
          }
          .getOrElse(fail("Not found required unreachable symbol"))
      case _ => fail("Expected code to not link")
    }
  }

  @Test def unsupportedFeature(): Unit = {
    doesNotLink(
      entry = mainClass,
      Map(
        sourceFile ->
          s"""|
              |object $mainClass{
              |  import scala.scalanative.meta.LinktimeInfo._
              |  def doUnsupported() = {
              |    if(isWindows && isLinux && isMac) // mutal exclusion, would always yield false
              |      scala.scalanative.runtime.UnsupportedFeature.threads
              |    println("unreachable")
              |  }
              |  def main(args: Array[String]): Unit = {
              |    doUnsupported()
              |  }
              |}
              |""".stripMargin
      )
    ) {
      case (config, result) =>
        assertTrue(result.unreachable.isEmpty)
        assertFalse(result.unsupportedFeatures.isEmpty)
        result.unsupportedFeatures
          .collectFirst {
            case Reach.UnsupportedFeature(kind, backtrace) =>
              assertEquals(
                "wrong kind",
                Reach.UnsupportedFeature.SystemThreads,
                kind
              )
              assertTrue("no-backtrace", backtrace.nonEmpty)
          }
          .getOrElse(fail("Not found required unreachable symbol"))
    }
  }

}

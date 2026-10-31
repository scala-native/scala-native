package scala.scalanative

import java.io.{File, PrintWriter}
import java.nio.file.attribute.FileTime
import java.nio.file.{Files, Path, Paths}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, duration}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.build._
import scala.scalanative.buildinfo.ScalaNativeBuildInfo
import scala.scalanative.util.Scope

// The test is used for incremental compilation

class IncCompilationTest extends codegen.CodeGenSpec {
  private def buildAwait(config: Config)(implicit scope: Scope) =
    Await.result(Build.build(config), duration.Duration.Inf)

  // The directory is deleted once the scope has closed what it opened there
  private def withOutDir(body: Path => Unit): Unit = {
    val outDir = Files.createTempDirectory("native-test-out")
    try body(outDir)
    finally IO.deleteRecursive(outDir)
  }

  @Test def generateIRForSingleType(): Unit = withOutDir { outDir =>
    Scope { implicit in =>
      val source =
        """|
           |object A {
           |  def print(x: String): Unit = {
           |    println(x)
           |  }
           |  def print(x: Int): Unit = {
           |    println(x)
           |  }
           |  def returnInt(): Int = {
           |    val a = 2
           |    val b = "helloworld"
           |    val c = a + b.length
           |    c
           |  }
           |  def main(args: Array[String]): Unit = {
           |    val b = returnInt()
           |    print(b)
           |  }
           |}""".stripMargin
      val entry = "A"
      val changedTop = Set[String]("A", "A$")
      val files = NIRCompiler.getCompiler(outDir).compile(source)
      makeChanged(outDir, changedTop)
      val nativeConfig = defaultNativeConfig.withOptimize(false)
      val config = makeConfig(outDir, "out", entry, nativeConfig)
      buildAwait(config)
    }
  }

  @Test def generateIRForMultipleTypes(): Unit = withOutDir { outDir =>
    val sources = Map(
      "A.scala" ->
        """|
           |object A {
           |  def print(x: String): Unit = {
           |    println(x)
           |  }
           |  def print(x: Int): Unit = {
           |    println(x)
           |  }
           |  def getB(): B = {
           |    val b = new B
           |    b.bb = 1
           |    b
           |  }
           |  def main(args: Array[String]): Unit = {
           |    val b = getB()
           |    println(b.add())
           |    println(b.sub())
           |  }
           |}""".stripMargin,
      "B.scala" ->
        """|
           |class B {
           |  var bb = 2
           |  def add(): Int = 3
           |  def sub(): Int = 4
           |}""".stripMargin
    )
    NIRCompiler.withSourceDir(sources) { sourcesDir =>
      Scope { implicit in =>
        val entry = "A"
        val changedTop = Set[String]("A", "A$")
        val compiler = NIRCompiler.getCompiler(outDir)
        val files = compiler.compile(sourcesDir)
        makeChanged(outDir, changedTop)
        val config = makeConfig(outDir, "out1", entry, defaultNativeConfig)

        buildAwait(config)
      }
    }
  }

  @Test def reuseNativeLibraryObjects(): Unit = withOutDir { outDir =>
    Scope { implicit in =>
      // Keep the build outside of the classpath: building twice would
      // otherwise pick up the NIR of the unpacked native libraries.
      val classesDir = Files.createDirectory(outDir.resolve("classes"))
      val compiler = NIRCompiler.getCompiler(classesDir)
      val config = makeConfig(
        classesDir,
        "out2",
        "A",
        defaultNativeConfig.withOptimize(false)
      ).withBaseDir(outDir.resolve("build"))
      def nativeLibObjects(): Map[Path, FileTime] = {
        val dependencies = config.workDir.resolve("dependencies")
        IO.getAll(dependencies, "glob:**.o")
          .map(p => p -> Files.getLastModifiedTime(p))
          .toMap
      }

      compiler.compile(
        """|object A {
           |  def main(args: Array[String]): Unit = println("hello")
           |}""".stripMargin
      )
      buildAwait(config)
      val initial = nativeLibObjects()
      assertTrue("no native library objects", initial.nonEmpty)

      buildAwait(config)
      assertEquals(
        "native library objects recompiled with an unchanged config",
        initial,
        nativeLibObjects()
      )

      // Reaching an `@define` adds a preprocessor flag to every native
      // library, so they all need to be recompiled.
      compiler.compile(
        """|import scala.scalanative.unsafe._
           |import scala.scalanative.libc.locale._
           |object A {
           |  def main(args: Array[String]): Unit =
           |    println(setlocale(LC_ALL, c"C") != null)
           |}""".stripMargin
      )
      buildAwait(config)
      val afterDefine = nativeLibObjects()
      val reused = initial.filter {
        case (p, t) => afterDefine.get(p).contains(t)
      }
      assertTrue(
        s"native library objects not recompiled after a new @define: ${reused.keys.take(5)}",
        reused.isEmpty
      )
    }
  }

  private def makeChanged(outDir: Path, changedTop: Set[String])(implicit
      in: Scope
  ): Unit = {
    val pw = new PrintWriter(
      new File(outDir.toFile, "changed")
    )
    changedTop.foreach(changedTop => pw.write(changedTop + "\n"))
    pw.close()
  }

  private def makeClasspath(outDir: Path)(implicit in: Scope) = {
    val parts: Array[Path] =
      ScalaNativeBuildInfo.nativeRuntimeClasspath
        .split(File.pathSeparator)
        .map(Paths.get(_))

    parts :+ outDir
  }

  private def makeConfig(
      outDir: Path,
      moduleName: String,
      entry: String,
      setupNativeConfig: NativeConfig
  )(implicit in: Scope): Config = {
    val classpath = makeClasspath(outDir)
    Config.empty
      .withBaseDir(outDir)
      .withModuleName(moduleName)
      .withClassPath(classpath.toSeq)
      .withMainClass(Some(entry))
      .withCompilerConfig(setupNativeConfig)
      .withLogger(Logger.nullLogger)
  }

  private lazy val defaultNativeConfig = build.NativeConfig.empty
    .withClang(Discover.clang())
    .withClangPP(Discover.clangpp())
    .withCompileOptions(Discover.compileOptions())
    .withLinkingOptions(Discover.linkingOptions())
    .withLTO(Discover.LTO())
    .withGC(Discover.GC())
    .withMode(Discover.mode())
    .withOptimize(Discover.optimize())
    .withBaseName("out")

}

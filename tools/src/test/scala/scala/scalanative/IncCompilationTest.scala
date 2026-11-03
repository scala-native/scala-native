package scala.scalanative

import java.io.{File, PrintWriter}
import java.nio.file.attribute.FileTime
import java.nio.file.{Files, Path, Paths}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, duration}
import scala.util.Try

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

  /* Builds outside of the classpath: building twice would otherwise pick up
   * the NIR of the unpacked native libraries.
   */
  private def withNativeLibBuild(
      body: Scope => (Config, NIRCompiler) => Unit
  ): Unit = withOutDir { outDir =>
    Scope { implicit in =>
      val classesDir = Files.createDirectory(outDir.resolve("classes"))
      val compiler = NIRCompiler.getCompiler(classesDir)
      val config = makeConfig(
        classesDir,
        "out2",
        "A",
        defaultNativeConfig.withOptimize(false)
      ).withBaseDir(outDir.resolve("build"))
      compiler.compile(
        """|object A {
           |  def main(args: Array[String]): Unit = println("hello")
           |}""".stripMargin
      )
      body(in)(config, compiler)
    }
  }

  private def objects(dir: Path): Map[Path, FileTime] =
    IO.getAll(dir, "glob:**.o")
      .map(p => p -> Files.getLastModifiedTime(p))
      .toMap

  private def nativeLibObjects(config: Config): Map[Path, FileTime] =
    objects(config.workDir.resolve("dependencies"))

  private def generatedIRObjects(config: Config): Map[Path, FileTime] = {
    val dependencies = config.workDir.resolve("dependencies")
    objects(config.workDir).filter {
      case (p, _) => !p.startsWith(dependencies)
    }
  }

  private def unchanged(
      before: Map[Path, FileTime],
      after: Map[Path, FileTime]
  ): Map[Path, FileTime] =
    before.filter { case (p, t) => after.get(p).contains(t) }

  @Test def reuseNativeLibraryObjects(): Unit = withNativeLibBuild {
    implicit in => (config, compiler) =>
      buildAwait(config)
      val initial = nativeLibObjects(config)
      assertTrue("no native library objects", initial.nonEmpty)

      buildAwait(config)
      assertEquals(
        "native library objects recompiled with an unchanged config",
        initial,
        nativeLibObjects(config)
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
      val reused = unchanged(initial, nativeLibObjects(config))
      assertTrue(
        s"native library objects not recompiled after a new @define: ${reused.keys.take(5)}",
        reused.isEmpty
      )
  }

  @Test def recompileNativeLibraryAfterFailedConfigChange(): Unit =
    withNativeLibBuild { implicit in => (config, compiler) =>
      /* A native library of the program itself, with one source failing to
       * compile once the program reaches `@define("SCALANATIVE_TEST_FAIL")`.
       * The user config does not change, so the unpacked library is kept.
       */
      val nativeDir = Files.createDirectories(
        config.classPath.last.resolve(NativeLib.nativeCodeDir)
      )
      IO.write(nativeDir.resolve("ok.c"), "int test_ok(void) { return 1; }\n")
      IO.write(
        nativeDir.resolve("fail.c"),
        """|#ifdef SCALANATIVE_TEST_FAIL
           |#error "compiled with SCALANATIVE_TEST_FAIL"
           |#endif
           |""".stripMargin
      )
      def okObject() =
        nativeLibObjects(config).find(_._1.toString.endsWith("ok.c.o")).get
      buildAwait(config)
      val initial = okObject()

      compiler.compile(
        """|import scala.scalanative.unsafe._
           |@extern @define("SCALANATIVE_TEST_FAIL")
           |object failing { def test_ok(): CInt = extern }
           |object A {
           |  def main(args: Array[String]): Unit = println(failing.test_ok())
           |}""".stripMargin
      )
      assertTrue(
        "build reaching SCALANATIVE_TEST_FAIL succeeded",
        Try(buildAwait(config)).isFailure
      )
      // Let compilations still running after the failure finish writing.
      def settled(previous: Map[Path, FileTime], attempts: Int): Unit = {
        Thread.sleep(2000)
        val current = nativeLibObjects(config)
        if (current != previous && attempts > 0) settled(current, attempts - 1)
      }
      settled(nativeLibObjects(config), attempts = 30)
      val overwritten = okObject()
      assertNotEquals(
        "ok.c not compiled with the failing config",
        initial,
        overwritten
      )

      compiler.compile(
        """|object A {
           |  def main(args: Array[String]): Unit = println("hello")
           |}""".stripMargin
      )
      buildAwait(config)
      assertNotEquals(
        "ok.c object compiled with the failing config reused",
        overwritten,
        okObject()
      )
    }

  @Test def nativeLibraryChangeKeepsGeneratedIR(): Unit =
    withNativeLibBuild { implicit in => (config, _) =>
      def rebuiltIR(): Set[Path] = {
        val before = generatedIRObjects(config)
        buildAwait(config)
        val after = generatedIRObjects(config)
        (before -- unchanged(before, after).keys).keySet
      }
      buildAwait(config)
      assertTrue("no generated IR objects", generatedIRObjects(config).nonEmpty)
      // Some IR, such as `__buildInfo.ll`, is regenerated on every build.
      rebuiltIR()
      val alwaysRebuilt = rebuiltIR()
      val nativeObjects = nativeLibObjects(config)

      // Touching a native source recompiles its object only.
      val source = nativeObjects.keys
        .map(o => Paths.get(o.toString.stripSuffix(".o")))
        .find(_.toString.endsWith(".c"))
        .get
      Files.setLastModifiedTime(
        source,
        FileTime.fromMillis(System.currentTimeMillis() + 1000)
      )
      assertEquals(
        "generated IR recompiled after a native library change",
        alwaysRebuilt,
        rebuiltIR()
      )
      val recompiled =
        nativeObjects -- unchanged(nativeObjects, nativeLibObjects(config)).keys
      assertEquals(
        "recompiled native library objects",
        Set(Paths.get(source.toString + ".o")),
        recompiled.keySet
      )
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

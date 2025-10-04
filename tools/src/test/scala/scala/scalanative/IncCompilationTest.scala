package scala.scalanative

import org.junit.Test
import org.junit.Assert._

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Path, Paths}
import scala.scalanative.build.{Config, NativeConfig, _}
import scala.scalanative.util.Scope
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, duration}

// The test is used for incremental compilation

class IncCompilationTest extends codegen.CodeGenSpec {
  private def buildAwait(config: Config)(implicit scope: Scope) =
    Await.result(Build.build(config), duration.Duration.Inf)

  @Test def generateIRForSingleType(): Unit = {
    Scope { implicit in =>
      val source = """
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
      val outDir = Files.createTempDirectory("native-test-out")
      val files = NIRCompiler.getCompiler(outDir).compile(source)
      makeChanged(outDir, changedTop)
      val nativeConfig = defaultNativeConfig.withOptimize(false)
      val config = makeConfig(outDir, "out", entry, nativeConfig)
      buildAwait(config)
    }
  }

  @Test def generateIRForMultipleTypes(): Unit = {
    Scope { implicit in =>
      val sources = Map(
        "A.scala" -> """
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
        "B.scala" -> """
          |class B {
          |  var bb = 2
          |  def add(): Int = 3
          |  def sub(): Int = 4
          |}""".stripMargin
      )
      val entry = "A"
      val changedTop = Set[String]("A", "A$")
      val outDir = Files.createTempDirectory("native-test-out")
      val compiler = NIRCompiler.getCompiler(outDir)
      val sourcesDir = NIRCompiler.writeSources(sources)
      val files = compiler.compile(sourcesDir)
      makeChanged(outDir, changedTop)
      val config = makeConfig(outDir, "out1", entry, defaultNativeConfig)

      buildAwait(config)
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
      buildinfo.ScalaNativeBuildInfo.nativeRuntimeClasspath
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

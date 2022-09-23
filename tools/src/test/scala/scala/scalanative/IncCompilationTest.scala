package scala.scalanative

import org.scalatest.matchers.should.Matchers

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Path, Paths}
import scala.scalanative.build.{Config, NativeConfig, _}
import scala.scalanative.util.Scope

// The test is used for incremental compilation

class IncCompilationTest extends codegen.CodeGenSpec with Matchers {
  "The test framework" should "generate the llvm IR of object A" in {
    Scope { implicit in =>
      val sources = """object A {
                      |  def print(x: String): Unit = {
                      |    println(x)
                      |  }
                      |  def print(x: Int): Unit = {
                      |    println(x)
                      |  }
                      |  def main(args: Array[String]): Unit = {
                      |    print(1)
                      |    print("hello world?")
                      |  }
                      |}""".stripMargin
      val entry = "A"
      val changedTop = Set[String]("A", "A$")
      val outDir = Files.createTempDirectory("native-test-out")
      nativeLink(outDir, sources, entry)
    }

  }

  "The test framework" should "generate the llvm IR of object A and B" in {
    Scope { implicit in =>
      val sources = Map(
        "A.scala" -> """object A {
                       |  def print(x: String): Unit = {
                       |    println(x)
                       |  }
                       |  def print(x: Int): Unit = {
                       |    println(x)
                       |  }
                       |  def main(args: Array[String]): Unit = {
                       |    val b = new B
                       |    println(b.add())
                       |    println(b.sub())
                       |  }
                       |}""".stripMargin,
        "B.scala" -> """class B {
                       |  def add(): Int = 3
                       |  def sub(): Int = 4
                       |}""".stripMargin
      )
      val entry = "A"

      val outDir = Files.createTempDirectory("native-test-out")
      nativeLink(outDir, sources, entry)
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
      sys
        .props("scalanative.nativeruntime.cp")
        .split(File.pathSeparator)
        .map(Paths.get(_))

    parts :+ outDir
  }

  private def makeConfig(
      outDir: Path,
      entry: String,
      setupNativeConfig: NativeConfig
  )(implicit in: Scope): Config = {
    val classpath = makeClasspath(outDir)
    Config.empty
      .withWorkdir(outDir)
      .withClassPath(classpath.toSeq)
      .withMainClass(entry)
      .withCompilerConfig(setupNativeConfig)
  }

  private def nativeLink(
      outDir: Path,
      sources: Map[String, String],
      entry: String
  )(implicit scope: Scope): Unit = {
    val compiler = NIRCompiler.getCompiler(outDir)
    val sourcesDir = NIRCompiler.writeSources(sources)
    val files = compiler.compile(sourcesDir)

    val classpath = makeClasspath(outDir)
    val nativeConfig = build.NativeConfig.empty
      .withClang(Discover.clang())
      .withClangPP(Discover.clangpp())
      .withCompileOptions(Discover.compileOptions())
      .withLinkingOptions(Discover.linkingOptions())
      .withLTO(Discover.LTO())
      .withGC(Discover.GC())
      .withMode(Discover.mode())
      .withOptimize(Discover.optimize())
    val config = makeConfig(outDir, entry, nativeConfig)
    val ext = if (Platform.isWindows) ".exe" else ""
    val artifact = outDir.resolve("result" + ext)

    Build.build(config, artifact)
  }
}

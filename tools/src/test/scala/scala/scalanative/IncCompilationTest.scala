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
      val optimizerConfig = build.OptimizerConfig.empty
        .withMaxCallerSize(10000)
        .withMaxInlineSize(1)
      val nativeConfig = defaultNativeConfig
        .withOptimizerConfig(optimizerConfig)
      val config = makeConfig(outDir, "out", entry, nativeConfig)
      Build.build(config)
    }
  }

  "The test framework" should "generate the llvm IR of object A and B" in {
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
      val outDir = Files.createTempDirectory("native-test-out1")
      val compiler = NIRCompiler.getCompiler(outDir)
      val sourcesDir = NIRCompiler.writeSources(sources)
      val files = compiler.compile(sourcesDir)
      makeChanged(outDir, changedTop)
      val config = makeConfig(outDir, "out1", entry, defaultNativeConfig)

      Build.build(config)
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

}

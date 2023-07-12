package scala.scalanative

import scala.language.implicitConversions
import java.io.File
import java.nio.file.{Files, Path, Paths}
import scalanative.build.{Config, NativeConfig, Logger, ScalaNative, Discover}
import scalanative.util.Scope
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/** Base class to test the linker. */
abstract class LinkerSpec {

  /** Runs the linker using `driver` with `entry` as entry point on `sources`,
   *  and applies `fn` to the definitions.
   *
   *  @param entry
   *    The entry point for the linker.
   *  @param sources
   *    Map from file name to file content representing all the code to compile
   *    and link.
   *  @param fn
   *    A function to apply to the products of the compilation.
   *  @return
   *    The result of applying `fn` to the resulting definitions.
   */
  def link[T](
      entry: String,
      sources: Map[String, String],
      setupConfig: NativeConfig => NativeConfig = identity
  )(fn: (Config, linker.Result) => T): T =
    Scope { implicit in =>
      val outDir = Files.createTempDirectory("native-test-out")
      val compiler = NIRCompiler.getCompiler(outDir)
      val sourcesDir = NIRCompiler.writeSources(sources)
      val files = compiler.compile(sourcesDir)
      val config = makeConfig(outDir, entry, setupConfig)
      val entries = ScalaNative.entries(config)
      val link = ScalaNative.link(config, entries)
      val result = Await.result(link, 1.minute)
      fn(config, result)
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
      setupNativeConfig: NativeConfig => NativeConfig
  )(implicit in: Scope): Config = {
    val classpath = makeClasspath(outDir)
    Config.empty
      .withBaseDir(outDir)
      .withClassPath(classpath.toSeq)
      .withMainClass(Some(entry))
      .withCompilerConfig(setupNativeConfig.andThen(withDefaults))
      .withLogger(Logger.nullLogger)
  }

  private def withDefaults(config: NativeConfig): NativeConfig = {
    config
      .withTargetTriple("x86_64-unknown-unknown")
      .withClang(Discover.clang())
      .withClangPP(Discover.clangpp())

  }

  protected implicit def String2MapStringString(
      code: String
  ): Map[String, String] =
    Map("source.scala" -> code)

}

package scala.scalanative

import java.io.File
import java.nio.file.{Files, Path, Paths}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.implicitConversions

import org.junit.Assert.fail

import scala.scalanative.buildinfo.ScalaNativeBuildInfo
import scala.scalanative.linker.ReachabilityAnalysis
import scala.scalanative.util.unreachable
import scalanative.build.{Config, Discover, Logger, NativeConfig, ScalaNative}
import scalanative.util.Scope

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
  )(fn: (Config, ReachabilityAnalysis.Result) => T): T =
    mayLink(entry, sources, setupConfig) {
      case (config, result: ReachabilityAnalysis.Result) => fn(config, result)
      case _ => fail("Expected code to link"); unreachable
    }

  def doesNotLink[T](
      entry: String,
      sources: Map[String, String],
      setupConfig: NativeConfig => NativeConfig = identity
  )(fn: (Config, ReachabilityAnalysis.Failure) => T): T =
    mayLink(entry, sources, setupConfig) {
      case (config, result: ReachabilityAnalysis.Failure) =>
        fn(config, result)
      case _ => fail("Expected code to not link"); unreachable
    }

  protected def mayLink[T](
      entry: String,
      sources: Map[String, String],
      setupConfig: NativeConfig => NativeConfig = identity
  )(fn: (Config, ReachabilityAnalysis) => T): T =
    Scope { implicit in =>
      val outDir = Files.createTempDirectory("native-test-out")
      val compiler = NIRCompiler.getCompiler(outDir)
      val sourcesDir = NIRCompiler.writeSources(sources)
      val files = compiler.compile(sourcesDir)
      val config = makeConfig(outDir, entry, setupConfig)
      val entries = ScalaNative.entries(config)
      val result = linker.Link(config, entries)
      fn(config, result)
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
      entry: String,
      setupNativeConfig: NativeConfig => NativeConfig
  )(implicit in: Scope): Config = {
    val classpath = makeClasspath(outDir)
    Config.empty
      .withBaseDir(outDir)
      .withClassPath(classpath.toSeq)
      .withMainClass(Some(entry))
      .withCompilerConfig(setupNativeConfig.compose(withDefaults))
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

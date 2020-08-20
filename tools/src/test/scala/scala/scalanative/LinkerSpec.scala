package scala.scalanative

import scala.language.implicitConversions

import java.io.File
import java.nio.file.{Files, Path, Paths}

import scalanative.build.{ScalaNative, Config}
import scalanative.util.Scope

import org.scalatest.flatspec.AnyFlatSpec

/** Base class to test the linker. */
abstract class LinkerSpec extends AnyFlatSpec {

  /**
   * Runs the linker using `driver` with `entry` as entry point on `sources`,
   * and applies `fn` to the definitions.
   *
   * @param entry   The entry point for the linker.
   * @param sources Map from file name to file content representing all the code
   *                to compile and link.
   * @param fn      A function to apply to the products of the compilation.
   * @return The result of applying `fn` to the resulting definitions.
   */
  def link[T](entry: String,
              sources: Map[String, String],
              linkStubs: Boolean = false)(fn: (Config, linker.Result) => T): T =
    Scope { implicit in =>
      val outDir     = Files.createTempDirectory("native-test-out")
      val compiler   = NIRCompiler.getCompiler(outDir)
      val sourcesDir = NIRCompiler.writeSources(sources)
      val files      = compiler.compile(sourcesDir)
      val config     = makeConfig(outDir, entry, linkStubs)
      val entries    = ScalaNative.entries(config)
      val result     = ScalaNative.link(config, entries)

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

  private def makeConfig(outDir: Path, entry: String, linkStubs: Boolean)(
      implicit in: Scope): Config = {
    val classpath = makeClasspath(outDir)
    Config.empty
      .withWorkdir(outDir)
      .withClassPath(classpath)
      .withMainClass(entry)
      .withCompilerConfig(_.withLinkStubs(linkStubs))
  }

  protected implicit def String2MapStringString(
      code: String): Map[String, String] =
    Map("source.scala" -> code)

}

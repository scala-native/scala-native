package scala.scalanative

import scala.language.implicitConversions

import java.io.File
import java.nio.file.Files

import util.Scope
import nir.Global
import tools.Config
import optimizer.Driver

import org.scalatest.FlatSpec

/** Base class to test the linker. */
abstract class LinkerSpec extends FlatSpec {

  /**
   * Runs the linker using `driver` with `entry` as entry point on `sources`,
   * and applies `fn` to the definitions.
   *
   * @param entry   The entry point for the linker.
   * @param sources Map from file name to file content representing all the code
   *                to compile and link.
   * @param driver  Optional custom driver that defines the pipeline.
   * @param fn      A function to apply to the products of the compilation.
   * @return The result of applying `fn` to the resulting definitions.
   */
  def link[T](
      entry: String,
      sources: Map[String, String],
      driver: Option[Driver] = None)(f: (Config, linker.Result) => T): T =
    Scope { implicit in =>
      val outDir     = Files.createTempDirectory("native-test-out").toFile()
      val compiler   = NIRCompiler.getCompiler(outDir)
      val sourcesDir = NIRCompiler.writeSources(sources)
      val files      = compiler.compile(sourcesDir)
      val config     = makeConfig(outDir, entry)
      val driver_    = driver.fold(Driver(config))(identity)
      val result     = tools.link(config, driver_)

      f(config, result)
    }

  private def makePaths(outDir: File)(implicit in: Scope) = {
    val parts: Array[File] =
      sys
        .props("scalanative.nativeruntime.cp")
        .split(File.pathSeparator)
        .map(new File(_))

    parts :+ outDir
  }

  private def makeConfig(outDir: File, entryName: String)(
      implicit in: Scope): Config = {
    val entry = Global.Top(entryName)
    val paths = makePaths(outDir)
    Config.empty
      .withWorkdir(outDir)
      .withPaths(paths)
      .withEntry(entry)
  }

  protected implicit def String2MapStringString(
      code: String): Map[String, String] =
    Map("source.scala" -> code)

}

package scala.scalanative
package linker

import org.scalatest._
import java.io.File
import java.nio.file.{Files, Path, Paths}
import scalanative.util.Scope
import scalanative.nir.{Sig, Global}
import scalanative.build.ScalaNative

trait ReachabilitySuite extends FunSuite {

  def g(top: String): Global =
    Global.Top(top)

  def g(top: String, sig: Sig): Global =
    Global.Member(Global.Top(top), sig)

  def testReachable(label: String)(f: => (String, Global, Seq[Global])) =
    test(label) {
      val (source, entry, expected) = f
      link(Seq(entry), Seq(source)) { res =>
        val left  = res.defns.map(_.name).toSet
        val right = expected.toSet
        assert(res.unavailable.isEmpty, "unavailable")
        assert((left -- right).isEmpty, "underapproximation")
        assert((right -- left).isEmpty, "overapproximation")
      }
    }

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
  def link[T](entries: Seq[Global], sources: Seq[String])(
      f: linker.Result => T): T =
    Scope { implicit in =>
      val outDir   = Files.createTempDirectory("native-test-out")
      val compiler = NIRCompiler.getCompiler(outDir)
      val sourceMap = sources.zipWithIndex.map {
        case (b, i) => (s"file$i.scala", b)
      }.toMap
      val sourcesDir = NIRCompiler.writeSources(sourceMap)
      val files      = compiler.compile(sourcesDir)
      val config     = makeConfig(outDir)
      val result     = ScalaNative.link(config, entries)

      f(result)
    }

  private def makeClasspath(outDir: Path)(implicit in: Scope) = {
    val parts: Array[Path] =
      sys
        .props("scalanative.nativeruntime.cp")
        .split(File.pathSeparator)
        .map(Paths.get(_))

    parts :+ outDir
  }

  private def makeConfig(outDir: Path)(implicit in: Scope): build.Config = {
    val paths = makeClasspath(outDir)
    build.Config.empty
      .withWorkdir(outDir)
      .withClassPath(paths)
  }
}

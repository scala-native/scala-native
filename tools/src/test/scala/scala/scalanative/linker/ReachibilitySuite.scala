package scala.scalanative
package linker

import org.scalatest._
import java.io.File
import java.nio.file.Files
import scalanative.util.Scope
import scalanative.nir.Global
import scalanative.tools
import scalanative.optimizer.Driver

trait ReachabilitySuite extends FunSuite {

  def sources: Seq[String]

  def g(top: String, members: String*): Global =
    members.foldLeft[Global](Global.Top(top))(Global.Member(_, _))

  def testReachable(label: String)(entries: Global*)(expected: Global*) =
    test(label) {
      link(entries, sources) { res =>
        val left  = res.defns.map(_.name).toSet
        val right = expected.toSet
        assert((left -- right).isEmpty, "overapproximation")
        assert((right -- left).isEmpty, "underapproximation")
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
      val outDir   = Files.createTempDirectory("native-test-out").toFile()
      val compiler = NIRCompiler.getCompiler(outDir)
      val sourceMap = sources.zipWithIndex.map {
        case (b, i) => (s"file$i.scala", b)
      }.toMap
      val sourcesDir = NIRCompiler.writeSources(sourceMap)
      val files      = compiler.compile(sourcesDir)
      val config     = makeConfig(outDir)
      val result     = tools.linkRaw(config, entries)

      f(result)
    }

  private def makePaths(outDir: File)(implicit in: Scope) = {
    val parts: Array[File] =
      sys
        .props("scalanative.nativeruntime.cp")
        .split(File.pathSeparator)
        .map(new File(_))

    parts :+ outDir
  }

  private def makeConfig(outDir: File)(implicit in: Scope): tools.Config = {
    val paths = makePaths(outDir)
    tools.Config.empty
      .withWorkdir(outDir)
      .withPaths(paths)
  }
}

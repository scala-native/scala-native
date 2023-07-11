package scala.scalanative
package linker

import org.junit.Test
import org.junit.Assert._

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scalanative.util.Scope
import scalanative.nir.{Sig, Global}
import scalanative.build.ScalaNative
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait ReachabilitySuite {

  def g(top: String): Global =
    Global.Top(top)

  def g(top: String, sig: Sig): Global =
    Global.Member(Global.Top(top), sig)

  private val MainMethodDependencies = Set(
    Global.Top("java.lang.String"),
    Global.Top("java.lang.CharSequence"),
    Global.Top("java.lang.Comparable"),
    Global.Top("java.io.Serializable"),
    Global.Top("java.lang.constant.Constable"),
    Global.Top("java.lang.constant.ConstantDesc")
  )

  def testReachable(includeMainDeps: Boolean = true)(
      f: => (String, Global, Seq[Global])
  ) = {
    val (source, entry, expected) = f
    // When running reachability tests disable loading static constructors
    // ReachabilitySuite tests are designed to check that exactly given group
    // of symbols is reachable. By default we always try to load all static
    // constructrs - this mechanism is used by junit-plugin to mitigate lack
    // of reflection. We need to disable it, otherwise we would be swarmed
    // with definitions introduced by static constructors
    val reachStaticConstructorsKey =
      "scala.scalanative.linker.reachStaticConstructors"
    sys.props += reachStaticConstructorsKey -> false.toString()
    try {
      link(Seq(entry), Seq(source), entry.top.id) { res =>
        val left = res.defns.map(_.name).toSet
        val extraDeps = if (includeMainDeps) MainMethodDependencies else Nil
        val right = expected.toSet ++ extraDeps
        assertTrue("unavailable", res.unavailable.isEmpty)
        assertTrue("underapproximation", (left -- right).isEmpty)
        assertTrue("overapproximation", (right -- left).isEmpty)
      }
    } finally {
      sys.props -= reachStaticConstructorsKey
    }
  }

  /** Runs the linker using `driver` with `entry` as entry point on `sources`,
   *  and applies `fn` to the definitions.
   *
   *  @param entry
   *    The entry point for the linker.
   *  @param sources
   *    Map from file name to file content representing all the code to compile
   *    and link.
   *  @param driver
   *    Optional custom driver that defines the pipeline.
   *  @param fn
   *    A function to apply to the products of the compilation.
   *  @return
   *    The result of applying `fn` to the resulting definitions.
   */
  def link[T](
      entries: Seq[Global],
      sources: Seq[String],
      mainClass: String
  )(
      f: linker.Result => T
  ): T =
    Scope { implicit in =>
      val outDir = Files.createTempDirectory("native-test-out")
      val compiler = NIRCompiler.getCompiler(outDir)
      val sourceMap = sources.zipWithIndex.map {
        case (b, i) => (s"file$i.scala", b)
      }.toMap
      val sourcesDir = NIRCompiler.writeSources(sourceMap)
      val files = compiler.compile(sourcesDir)
      val config = makeConfig(outDir, mainClass)
      val link = ScalaNative.link(config, entries)
      val result = Await.result(link, 1.minute)
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

  private def makeConfig(outDir: Path, mainClass: String)(implicit
      in: Scope
  ): build.Config = {
    val paths = makeClasspath(outDir)
    val default = build.Config.empty
    default
      .withBaseDir(outDir)
      .withClassPath(paths.toSeq)
      .withCompilerConfig {
        _.withTargetTriple("x86_64-unknown-unknown")
      }
      .withMainClass(Some(mainClass))
  }
}

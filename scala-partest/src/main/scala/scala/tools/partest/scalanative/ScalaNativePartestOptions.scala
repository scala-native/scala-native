// Ported from Scala.js commit: 060c3397 dated: 2021-02-09

package scala.tools.partest.scalanative
import java.nio.file.{Path, Paths}
import java.io.File.pathSeparator
import scalanative.build

case class ScalaNativePartestOptions private (
    testFilter: ScalaNativePartestOptions.TestFilter,
    nativeClasspath: Seq[Path],
    precompiledLibrariesPaths: Seq[Path],
    parallelism: Option[Int],
    shouldPrecompileLibraries: Boolean,
    showDiff: Boolean,
    optimize: Boolean,
    buildMode: build.Mode,
    gc: build.GC,
    lto: build.LTO
) {
  def javaOptions: Seq[String] = Seq(
    s"-Dscalanative.partest.optimize=$optimize",
    s"-Dscalanative.partest.mode=${buildMode.name}",
    s"-Dscalanative.partest.gc=${gc.name}",
    s"-Dscalanative.partest.lto=${lto.name}",
    s"-Dscalanative.partest.nativeCp=${nativeClasspath.mkString(pathSeparator)}",
    s"-Dscalanative.build.paths.libobj=${precompiledLibrariesPaths.mkString(pathSeparator)}"
  )

  def show: String =
    s"""Scala Native options are:
       |- optimized:       $optimize
       |- mode:            $buildMode
       |- gc:              $gc
       |- lto:             $lto
       |- showDiff:        $showDiff
       |- testFilter:      ${testFilter.descr}
       |- precompile libs: $shouldPrecompileLibraries
       |- parallel tests:  ${parallelism.getOrElse("default")}
       |""".stripMargin
}

object ScalaNativePartestOptions {

  sealed abstract class TestFilter {
    def descr: String
  }
  case object BlacklistedTests extends TestFilter {
    override def descr: String = "Blacklisted"
  }
  case object WhitelistedTests extends TestFilter {
    override def descr: String = "Whitelisted"
  }
  case class SomeTests(names: List[String]) extends TestFilter {
    override def descr: String = "Custom " + this.toString
    override def toString() =
      names.map(x => s""""$x"""").mkString("[", ", ", "]")
  }

  def apply(
      args: Array[String],
      errorReporter: String => Unit
  ): Option[ScalaNativePartestOptions] = {

    var failed = false
    var filter: Option[TestFilter] = None
    var showDiff: Boolean = false
    var parallelism: Option[Int] = None
    var mode: build.Mode = build.Mode.default
    var gc: build.GC = build.GC.default
    var lto: build.LTO = build.LTO.default
    var optimize: Boolean = true
    var precompileLibs: Boolean = true
    val nativeClassPath = Seq.newBuilder[Path]

    def error(msg: String) = {
      failed = true
      errorReporter(msg)
    }

    def setFilter(newFilter: TestFilter) = (filter, newFilter) match {
      case (Some(SomeTests(oldNames)), SomeTests(newNames)) =>
        // Merge test names
        filter = Some(SomeTests(oldNames ++ newNames))
      case (Some(fil), newFilter) =>
        error(
          s"You cannot specify twice what tests to use (already specified: $fil, new: $newFilter)"
        )
      case (None, newFilter) =>
        filter = Some(newFilter)
    }

    def splitOption(str: String): Array[String] = {
      if (!str.trim.startsWith("--")) Array.empty
      else str.stripPrefix("--").split('=')
    }

    object Switch {
      def unapply(str: String): Option[String] = {
        splitOption(str) match {
          case Array(arg) => Some(arg)
          case _          => None
        }
      }
    }
    object Argument {
      def unapply(str: String): Option[(String, String)] = {
        splitOption(str) match {
          case Array(arg, value) => Some(arg -> value)
          case _                 => None
        }
      }
    }

    for (arg <- args) arg match {
      case Switch("blacklisted")      => setFilter(BlacklistedTests)
      case Switch("whitelisted")      => setFilter(WhitelistedTests)
      case Switch("showDiff")         => showDiff = true
      case Switch("noOptimize")       => optimize = false
      case Switch("noPrecompileLibs") => precompileLibs = false
      case Argument("parallelism", value) =>
        parallelism = util.Try(Integer.parseInt(value)).filter(_ > 0).toOption
      case Argument("mode", value) => mode = build.Mode(value)
      case Argument("gc", value)   => gc = build.GC(value)
      case Argument("lto", value)  => lto = build.LTO(value)
      case Argument("nativeClasspath", classpath) =>
        classpath
          .split(java.io.File.pathSeparatorChar)
          .map(Paths.get(_))
          .foreach(nativeClassPath += _)
      case _ => setFilter(SomeTests(arg :: Nil))
    }

    if (failed) None
    else
      Some {
        new ScalaNativePartestOptions(
          filter.getOrElse(WhitelistedTests),
          nativeClassPath.result(),
          showDiff = showDiff,
          parallelism = parallelism,
          optimize = optimize,
          buildMode = mode,
          shouldPrecompileLibraries = precompileLibs,
          precompiledLibrariesPaths = Seq.empty,
          gc = gc,
          lto = lto
        )
      }
  }

}

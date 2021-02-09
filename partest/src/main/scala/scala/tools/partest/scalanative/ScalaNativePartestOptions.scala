/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.tools.partest.scalanative
import java.nio.file.{Path, Paths}
import scalanative.build

class ScalaNativePartestOptions private (
    val testFilter: ScalaNativePartestOptions.TestFilter,
    val nativeClasspath: Seq[Path],
    val showDiff: Boolean,
    val optimize: Boolean,
    val buildMode: build.Mode,
    val gc: build.GC,
    val lto: build.LTO
)

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
      errorReporter: String => Unit): Option[ScalaNativePartestOptions] = {

    var failed                     = false
    var filter: Option[TestFilter] = None
    var showDiff: Boolean          = false
    var mode: build.Mode           = build.Mode.default
    var gc: build.GC               = build.GC.default
    var lto: build.LTO             = build.LTO.default
    var optimize: Boolean          = true
    val nativeClassPath            = Seq.newBuilder[Path]

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
          s"You cannot specify twice what tests to use (already specified: $fil, new: $newFilter)")
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
      case Switch("blacklisted")   => setFilter(BlacklistedTests)
      case Switch("whitelisted")   => setFilter(WhitelistedTests)
      case Switch("showDiff")      => showDiff = true
      case Switch("noOptimize")    => optimize = false
      case Argument("mode", value) => mode = build.Mode(value)
      case Argument("gc", value)   => gc = build.GC(value)
      case Argument("lto", value)  => lto = build.LTO(value)
      case Argument("nativeClasspath", classpath) =>
        classpath
          .split(":")
          .map(Paths.get(_))
          .foreach(nativeClassPath += _)
      case _ => setFilter(SomeTests(arg :: Nil))
    }

    if (failed) None
    else
      Some {
        new ScalaNativePartestOptions(filter.getOrElse(WhitelistedTests),
                                      nativeClassPath.result(),
                                      showDiff = showDiff,
                                      optimize = optimize,
                                      buildMode = mode,
                                      gc = gc,
                                      lto = lto)
      }
  }

}

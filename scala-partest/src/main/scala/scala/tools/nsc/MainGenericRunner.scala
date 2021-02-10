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

package scala.tools.nsc

/* Super hacky overriding of the MainGenericRunner used by partest */

import java.nio.file._
import java.util.function.BinaryOperator
import java.util.{Comparator, function}
import scala.scalanative.build._
import scala.scalanative.util.Scope
import scala.tools.nsc.GenericRunnerCommand._
import scala.tools.nsc.Properties.{copyrightString, versionString}

class MainGenericRunner {
  def errorFn(ex: Throwable): Boolean = {
    ex.printStackTrace()
    false
  }
  def errorFn(str: String): Boolean = {
    scala.Console.err println str
    false
  }

  def process(args: Array[String]): Boolean = {
    val command =
      new GenericRunnerCommand(args.toList, (x: String) => errorFn(x))

    if (!command.ok) return errorFn("\n" + command.shortUsageMsg)
    else if (command.settings.version)
      return errorFn(
        "Scala code runner %s -- %s".format(versionString, copyrightString))
    else if (command.shouldStopWithInfo) return errorFn("shouldStopWithInfo")

    if (command.howToRun != AsObject)
      return errorFn("Scala.js runner can only run an object")

    val logger = Logger(traceFn = _ => (),
                        debugFn = _ => (),
                        infoFn = _ => (),
                        warnFn = errorFn(_),
                        errorFn = errorFn(_))

    def loadSetting[T](name: String, default: => T)(fn: String => T) =
      Option(System.getProperty(s"scalanative.partest.$name")).fold(default)(fn)

    val dir = Files.createTempDirectory("partest-")
    val execPath: Path = {
      val config = Config.empty
        .withCompilerConfig(
          NativeConfig.empty
            .withClang(Discover.clang())
            .withClangPP(Discover.clangpp())
            .withCheck(true)
            .withLinkStubs(false)
            .withDump(false)
            .withOptimize(
              loadSetting("optimize", Discover.optimize())(_.toBoolean))
            .withMode(loadSetting("mode", Discover.mode())(
              scalanative.build.Mode(_)))
            .withGC(loadSetting("gc", Discover.GC())(GC.apply))
            .withLTO(loadSetting("lto", Discover.LTO())(LTO(_)))
            .withLinkingOptions(Discover.linkingOptions())
            .withCompileOptions(Discover.compileOptions())
        )
        .withClassPath {
          val nativeClasspath = loadSetting("nativeCp", Seq.empty[Path]) {
            _.split(":")
              .map(Paths.get(_))
          }
          command.settings.classpathURLs.map(urlToPath) ++ nativeClasspath
        }
        .withMainClass(command.thingToRun + "$")
        .withLogger(logger)
        .withWorkdir(dir)

      Scope { implicit s => Build.build(config, dir.resolve("output")) }
    }

    val res = {
      val cmd = execPath.toAbsolutePath.toFile.toString :: command.arguments
      sys.process.Process(cmd, dir.toFile).run().exitValue() == 0
    }

    val deleteFn = new function.Function[Path, Boolean] {
      override def apply(path: Path): Boolean = path.toFile.delete()
    }

    val reduceBool = new BinaryOperator[Boolean] {
      override def apply(l: Boolean, r: Boolean): Boolean = l && r
    }

    Files
      .walk(dir)
      .sorted(Comparator.reverseOrder[Path]())
      .map[Boolean](deleteFn)
      .reduce(true, reduceBool) &&
    Files.deleteIfExists(dir)

    res
  }

  private def urlToPath(url: java.net.URL) = {
    try {
      Paths.get(url.toURI())
    } catch {
      case e: java.net.URISyntaxException => Paths.get(url.getPath())
    }
  }
}

object MainGenericRunner extends MainGenericRunner {
  def main(args: Array[String]): Unit = {
    if (!process(args))
      System.exit(1)
  }
}

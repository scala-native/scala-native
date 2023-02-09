// Ported from Scala.js commit: 060c3397 dated: 2021-02-09

package scala.tools.nsc

/* Super hacky overriding of the MainGenericRunner used by partest */

import java.nio.file._
import java.util.function.BinaryOperator
import java.util.{Comparator, function}
import scala.scalanative.build._
import scala.scalanative.util.Scope
import scala.tools.nsc.GenericRunnerCommand._
import scala.tools.partest.scalanative.Defaults
import scala.tools.nsc.Properties.{copyrightString, versionString}

class MainGenericRunner {
  private def errorFn(str: String) = Defaults.errorFn(str)

  def process(args: Array[String]): Boolean = {
    val command =
      new GenericRunnerCommand(args.toList, (x: String) => errorFn(x))

    if (!command.ok) return errorFn("\n" + command.shortUsageMsg)
    else if (command.settings.version.value)
      return errorFn(
        "Scala code runner %s -- %s".format(versionString, copyrightString)
      )
    else if (command.shouldStopWithInfo) return errorFn("shouldStopWithInfo")

    if (command.howToRun != AsObject)
      return errorFn("Scala Native runner can only run an object")

    def loadSetting[T](name: String, default: => T)(fn: String => T) =
      Option(System.getProperty(s"scalanative.partest.$name")).fold(default)(fn)

    val dir = Defaults.workDir()
    val execPath: Path = {
      val config = Defaults.config
        .withCompilerConfig {
          _.withOptimize(
            loadSetting("optimize", Discover.optimize())(_.toBoolean)
          )
            .withMode(
              loadSetting("mode", Discover.mode())(scalanative.build.Mode(_))
            )
            .withGC(loadSetting("gc", Discover.GC())(GC.apply))
            .withLTO(loadSetting("lto", Discover.LTO())(LTO(_)))
            .withLinkingOptions {
              // If we precompile libs we need to make sure, that we link libraries needed by Scala Native
              Defaults.config.linkingOptions ++
                Option(System.getProperty("scalanative.build.paths.libobj"))
                  .filter(_.nonEmpty)
                  .fold(Seq.empty[String]) { _ =>
                    Defaults.links.map(_.name).map("-l" + _)
                  }
            }
            .withBaseName("output")
        }
        .withClassPath {
          val nativeClasspath = loadSetting("nativeCp", Seq.empty[Path]) {
            _.split(java.io.File.pathSeparatorChar).toSeq
              .map(Paths.get(_))
          }

          // Classpaths may contain "/" as first element after invalid conversion from URL `jrt:/`
          // This can create significant overhead in Classloader
          val commandClasspath = command.settings.classpathURLs
            .map(urlToPath)
            .filterNot(_.getNameCount == 0)

          commandClasspath ++ nativeClasspath
        }
        .withMainClass(Some(command.thingToRun))
        .withBaseDir(dir)

      Scope { implicit s => Build.build(config) }
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

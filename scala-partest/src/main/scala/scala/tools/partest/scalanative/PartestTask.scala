// Ported from Scala.js commit: 060c3397 dated: 2021-02-09

/* NOTE
 * Most of this file is copy-pasted from
 * https://github.com/scala/scala-partest-interface
 * It is unfortunately not configurable enough, hence the duplication
 */

package scala.tools.partest
package scalanative

import scala.language.reflectiveCalls

import _root_.sbt.testing._
import java.net.URLClassLoader
import java.io.File
import scala.scalanative.build.Build
import scala.scalanative.linker.ReachabilityAnalysis
import scala.scalanative.linker.LinktimeIntrinsicCallsResolver.FoundServiceProviders
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/** Run partest in this VM. Assumes we're running in a forked VM! */
case class PartestTask(taskDef: TaskDef, args: Array[String]) extends Task {

  // Get scala version through test name
  val scalaVersion = taskDef.fullyQualifiedName.stripPrefix("partest-")

  /** Executes this task, possibly returning to the client new tasks to execute.
   */
  def execute(
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Array[Task] = {
    val forkedCp = scala.util.Properties.javaClassPath
      .split(java.io.File.pathSeparator)
    val classLoader = new URLClassLoader(forkedCp.map(new File(_).toURI.toURL))

    if (Runtime.getRuntime().maxMemory() / (1024 * 1024) < 800)
      loggers foreach (_.warn(
        s"""Low heap size detected (~ ${Runtime
            .getRuntime()
            .maxMemory() / (1024 * 1024)}M). Please add the following to your build.sbt: javaOptions in Test += "-Xmx1G""""
      ))

    val maybeOptions =
      ScalaNativePartestOptions(args, str => loggers.foreach(_.error(str)))
        .map { opts =>
          if (opts.shouldPrecompileLibraries) {
            val forkedClasspath = forkedCp.map(java.nio.file.Paths.get(_)).toSeq
            val paths = precompileLibs(opts, forkedClasspath)
            opts.copy(precompiledLibrariesPaths = paths)
          } else opts
        }

    maybeOptions foreach { options =>
      val runner = SBTRunner(
        partestFingerprint = scala.tools.partest.sbt.Framework.fingerprint,
        eventHandler = eventHandler,
        loggers = loggers,
        testRoot =
          new File(s"../../scala-partest/fetchedSources/${scalaVersion}"),
        testClassLoader = classLoader,
        javaCmd = null,
        javacCmd = null,
        scalacArgs = Array.empty[String],
        args = Array("neg", "pos", "run"),
        options = options,
        scalaVersion = scalaVersion
      )

      try runner.run()
      catch {
        case ex: ClassNotFoundException =>
          loggers foreach { l =>
            l.error(
              "Please make sure partest is running in a forked VM by including the following line in build.sbt:\nfork in Test := true"
            )
          }
          throw ex
      }
    }

    Array()
  }

  type SBTRunner = { def run(): Unit }

  // use reflection to instantiate scala.tools.partest.scalanative.ScalaNativeSBTRunner,
  // casting to the structural type SBTRunner above so that method calls on the result will be invoked reflectively as well
  private def SBTRunner(
      partestFingerprint: Fingerprint,
      eventHandler: EventHandler,
      loggers: Array[Logger],
      testRoot: File,
      testClassLoader: URLClassLoader,
      javaCmd: File,
      javacCmd: File,
      scalacArgs: Array[String],
      args: Array[String],
      options: ScalaNativePartestOptions,
      scalaVersion: String
  ): SBTRunner = {
    val runnerClass =
      Class.forName("scala.tools.partest.scalanative.ScalaNativeSBTRunner")

    // The test root for partest is read out through the system properties,
    // not passed as an argument
    System.setProperty("partest.root", testRoot.getAbsolutePath())

    options.parallelism.foreach { n =>
      System.setProperty("partest.threads", n.toString)
    }

    // Partests take at least 5h. We double, just to be sure. (default is 4 hours)
    System.setProperty("partest.timeout", "10 hours")

    runnerClass.getConstructors.head
      .newInstance(
        partestFingerprint,
        eventHandler,
        loggers,
        testRoot,
        testClassLoader,
        javaCmd,
        javacCmd,
        scalacArgs,
        args,
        options,
        scalaVersion
      )
      .asInstanceOf[SBTRunner]
  }

  /** A possibly zero-length array of string tags associated with this task. */
  def tags: Array[String] = Array()

  def precompileLibs(
      options: ScalaNativePartestOptions,
      forkedClasspath: Seq[java.nio.file.Path]
  ): Seq[java.nio.file.Path] = {
    val config = Defaults.config
      .withBaseDir(Defaults.workDir())
      .withClassPath(options.nativeClasspath ++ forkedClasspath)
      .withCompilerConfig {
        _.withLTO(options.lto)
          .withMode(options.buildMode)
          .withGC(options.gc)
          .withOptimize(options.optimize)
      }

    import scala.collection.mutable
    val analysis = new ReachabilityAnalysis.Result(
      infos = mutable.Map.empty,
      entries = Nil,
      links = Defaults.links,
      preprocessorDefinitions = Nil,
      defns = Nil,
      dynsigs = Nil,
      dynimpls = Nil,
      resolvedVals = mutable.Map.empty,
      foundServiceProviders = new FoundServiceProviders(Map.empty)
    )

    val build = Build.findAndCompileNativeLibraries(config, analysis)
    Await.result(build, Duration.Inf)
  }
}

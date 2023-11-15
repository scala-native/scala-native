package build

import sbt._
import sbt.Keys._

import scala.scalanative.sbtplugin.Utilities._
import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._
import scala.sys.env
import complete.DefaultParsers._

import one.profiler.AsyncProfilerLoader
import one.profiler.AsyncProfiler
import build.OutputType._

object MyScalaNativePlugin extends AutoPlugin {
  override def requires: Plugins = ScalaNativePlugin

  lazy val nativeLinkProfiling =
    inputKey[File]("Running nativeLink with AsyncProfiler.")

  // see: https://github.com/scalameta/metals/blob/0176a491cd209a09852ab33f99fd7de639e8e2dd/metals/src/main/scala/scala/meta/internal/builds/BloopInstall.scala#L81
  final val isGeneratingForIDE =
    env.getOrElse("METALS_ENABLED", "false").toBoolean

  final val enableProfiler =
    env.getOrElse("ENABLE_PROFILER", "false").toBoolean

  final val enableExperimentalCompiler = {
    val ExperimentalCompilerEnv = "ENABLE_EXPERIMENTAL_COMPILER"
    val enabled = env.contains(ExperimentalCompilerEnv)
    println(
      if (enabled)
        s"Found `$ExperimentalCompilerEnv` env var: enabled sub-projects using Scala experimental version ${ScalaVersions.scala3Nightly}, using suffix `3_next`."
      else
        s"Not found `$ExperimentalCompilerEnv` env var: sub-projects using Scala experimental version would not be available."
    )
    enabled
  }

  // Allowed values: 3, 3-next, 2.13, 2.12
  final val ideScalaVersion = if (enableExperimentalCompiler) "3-next" else "3"

  // Would be visible in Metals logs
  if (isGeneratingForIDE)
    println(s"IDE support enabled using Scala $ideScalaVersion")

  private def multithreadingEnabledBySbtSysProps(): Option[Boolean] = {
    /* Started as: sbt -Dscala.scalanative.multithreading.enable=true
     * That idiom is used by Windows Continuous Integration (CI).
     *
     * BEWARE the root project Quirk!
     * This feature is not meant for general use. Anybody using it
     * should understand how it works.
     *
     * Setting multithreading on the command line __will_ override any
     * such setting in a .sbt file in all projects __except_ the root
     * project. "show nativeConfig" will show the value from .sbt files
     * "show sandbox3/nativeConfig" will show the effective value for
     * non-root projects.
     *
     * Someday this quirk will get fixed.
     */
    sys.props.get("scala.scalanative.multithreading.enable") match {
      case Some(v) => Some(java.lang.Boolean.parseBoolean(v))
      case None    => None
    }
  }

  private def nativeLinkProfilingImpl = Def.inputTaskDyn {
    val sbtLogger = streams.value.log
    val logger = sbtLogger.toLogger

    val args = spaceDelimited("<arg>").parsed

    val commands = args.headOption match {
      case None =>
        throw new IllegalArgumentException(
          "usage: nativeLinkProfiling <commands> <output-type>\n" +
            "<commands>: `,` delimited arguments for async-profiler. refer https://github.com/async-profiler/async-profiler/blob/49d08fd068f81f1c952320c4bd082d991e09db97/src/arguments.cpp#L65-L113 \n" +
            "<output-type>: text|collapsed|flamegraph|tree (default: flamegraph) \n" +
            "e.g. `nativeLinkProfiling events=cpu,interval=10000000,threads"
        )
      case Some(value) =>
        value
    }

    val outputType = (for {
      input <- args.tail.headOption.toRight(
        new IllegalArgumentException("Missing output type")
      )
      typ <- OutputType.fromString(input)
    } yield typ) match {
      case Left(ex) =>
        logger.warn(
          s"${ex.getMessage()}, using default output type: `flamegraph`"
        )
        OutputType.Flamegraph
      case Right(value) => value
    }

    val profilerOpt: Option[AsyncProfiler] =
      if (AsyncProfilerLoader.isSupported())
        try {
          Some(AsyncProfilerLoader.load())
        } catch {
          case ex: IllegalStateException => {
            logger.warn(
              s"Couldn't load async-prfiler, restart sbt to workaround the problem. " +
                "This is usually caused because old sbt's classloader loaded the async-profiler DLL. \n" +
                ex.getMessage()
            )
            throw ex
          }
          case e: Throwable => throw e
        }
      else {
        logger.warn(
          "Couldn't load async-prfiler for the current OS, architecture or glibc is unavailable. " +
            "Profiling will not be available." +
            "See the supported platforms https://github.com/jvm-profiling-tools/ap-loader#supported-platforms"
        )
        None
      }

      val module = moduleName.value
      val out =
        (crossTarget.value / s"$module-profile.${outputType.extension}").toString
      profilerOpt match {
        case Some(profiler) =>
          logger.info(
            s"[async-profiler] starting profiler with commands: start,$commands"
          )
          profiler.execute(s"start,$commands")
          Def.task {
            nativeLink.value
          } andFinally {
            logger.info(s"[async-profiler] stop profiler, output to ${out}")
            profiler.execute("stop")
            profiler.execute(s"${outputType.name},file=${out}")
          }
        case None =>
          nativeLink
      }
  }

  override def projectSettings: Seq[Setting[_]] = Def.settings(
    /* Remove libraryDependencies on ourselves; we use .dependsOn() instead
     * inside this build.
     */
    libraryDependencies ~= { libDeps =>
      libDeps.filterNot(_.organization == "org.scala-native")
    },
    nativeConfig ~= { nc =>
      nc.withCheck(true)
        .withCheckFatalWarnings(true)
        .withDump(true)
        .withDebugMetadata(true)
        .withMultithreadingSupport(
          multithreadingEnabledBySbtSysProps()
            .getOrElse(nc.multithreadingSupport)
        )
    },
    // nativeLinkProfiling := nativeLinkProfilingImpl.tag(NativeTags.Link).value,
    scalacOptions ++= {
      // Link source maps to GitHub sources
      val isSnapshot = nativeVersion.endsWith("-SNAPSHOT")
      if (isSnapshot) Nil
      else
        Settings.scalaNativeMapSourceURIOption(
          (LocalProject("scala-native") / baseDirectory).value,
          s"https://raw.githubusercontent.com/scala-native/scala-native/v$nativeVersion/"
        )
    },
    inConfig(Compile) {
      nativeLinkProfiling := nativeLinkProfilingImpl
        .tag(NativeTags.Link)
        .evaluated,
    }
  )
}

sealed abstract class OutputType(val name: String) {
  def extension: String = this match {
    case Text       => "txt"
    case Collapsed  => "csv"
    case Flamegraph => "html"
    case Tree       => "html"
  }
}

object OutputType {
  case object Text extends OutputType("text")
  case object Collapsed extends OutputType("collapsed")
  case object Flamegraph extends OutputType("flamegraph")
  case object Tree extends OutputType("tree")
  def fromString(s: String): Either[IllegalArgumentException, OutputType] =
    s match {
      case Text.name       => Right(Text)
      case Collapsed.name  => Right(Collapsed)
      case Flamegraph.name => Right(Flamegraph)
      case Tree.name       => Right(Tree)
      case _ => Left(new IllegalArgumentException(s"Unknown output type: $s"))
    }
}

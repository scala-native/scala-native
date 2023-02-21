package build

import sbt._
import sbt.Keys._

import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._

object MyScalaNativePlugin extends AutoPlugin {
  override def requires: Plugins = ScalaNativePlugin

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
        .withMultithreadingSupport(
          multithreadingEnabledBySbtSysProps()
            .getOrElse(nc.multithreadingSupport)
        )
    },
    scalacOptions ++= {
      // Link source maps to GitHub sources
      val isSnapshot = nativeVersion.endsWith("-SNAPSHOT")
      if (isSnapshot) Nil
      else
        Settings.scalaNativeMapSourceURIOption(
          (LocalProject("scala-native") / baseDirectory).value,
          s"https://raw.githubusercontent.com/scala-native/scala-native/v$nativeVersion/"
        )
    }
  )
}

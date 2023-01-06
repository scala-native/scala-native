package build

import sbt._
import sbt.Keys._

import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._

object MyScalaNativePlugin extends AutoPlugin {
  override def requires: Plugins = ScalaNativePlugin

  override def projectSettings: Seq[Setting[_]] = Def.settings(
    /* Remove libraryDependencies on ourselves; we use .dependsOn() instead
     * inside this build.
     */
    libraryDependencies ~= { libDeps =>
      libDeps.filterNot(_.organization == "org.scala-native")
    },
    nativeConfig ~= {
      _.withCheck(true)
        .withCheckFatalWarnings(true)
        .withDump(true)
        .withGC(scalanative.build.GC.boehm)
        .withMode(scalanative.build.Mode.releaseFast)
        .withLTO(scalanative.build.LTO.thin)
        .withMultithreadingSupport(
          true
          // sys.props.contains("scala.scalanative.multithreading.enable")
        )
    },
    scalacOptions ++= {
      // Link source maps to GitHub sources
      val revision =
        if (nativeVersion.endsWith("-SNAPSHOT")) "main"
        else s"v$nativeVersion"
      Settings.scalaNativeMapSourceURIOption(
        (LocalProject("scala-native") / baseDirectory).value,
        s"https://raw.githubusercontent.com/scala-native/scala-native/$revision/"
      )
    }
  )
}

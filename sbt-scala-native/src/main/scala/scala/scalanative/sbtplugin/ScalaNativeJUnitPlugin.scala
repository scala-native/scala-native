package scala.scalanative.sbtplugin

import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._

object ScalaNativeJUnitPlugin extends AutoPlugin {
  override def requires: Plugins = ScalaNativePlugin

  val ScalaNativeTestPlugin = config("scala-native-test-plugin").hide

  override def projectSettings: Seq[Setting[_]] = Seq(
    /* The `scala-native-test-plugin` configuration adds a plugin only to the `test`
     * configuration. It is a refinement of the `plugin` configuration which adds
     * it to both `compile` and `test`.
     */
    ivyConfigurations += ScalaNativeTestPlugin,
    libraryDependencies ++= {
      val ver = nativeVersion
      val org = nativeOrgName
      Seq(
        org %%% "junit-runtime" % ver % Test,
        org % "junit-plugin" % ver % ScalaNativeTestPlugin cross CrossVersion.full
      )
    },
    Test / scalacOptions ++= {
      val report = update.value
      val jars = report.select(configurationFilter(ScalaNativeTestPlugin.name))
      for {
        jar <- jars
        jarPath = jar.getPath
        // This is a hack to filter out the dependencies of the plugins
        if jarPath.contains("plugin")
      } yield s"-Xplugin:$jarPath"
    }
  )

}

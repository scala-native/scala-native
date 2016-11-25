import scalanative.tools.LinkerReporter
import scalanative.sbtplugin.ScalaNativePluginInternal.nativeLinkerReporter

ScalaNativePlugin.projectSettings

scalaVersion := "2.11.8"

nativeLinkerReporter := LinkerReporter.toFile(target.value / "out.dot")

lazy val check = taskKey[Unit]("Check that dot file was created.")

check := {
  assert((target.value / "out.dot").exists)
}

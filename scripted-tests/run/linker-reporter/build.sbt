import scalanative.tools.LinkerReporter
import scalanative.sbtplugin.ScalaNativePluginInternal.nativeLinkerReporter

enablePlugins(ScalaNativePlugin)

scalaVersion := "2.11.11"

nativeLinkerReporter in Compile := LinkerReporter.toFile(
  target.value / "out.dot")

lazy val check = taskKey[Unit]("Check that dot file was created.")

check := {
  assert((target.value / "out.dot").exists)
}

import scalanative.tools.OptimizerReporter
import scalanative.sbtplugin.ScalaNativePluginInternal.nativeOptimizerReporter

ScalaNativePlugin.projectSettings

scalaVersion := "2.11.8"

nativeOptimizerReporter := OptimizerReporter.toDirectory(crossTarget.value)

lazy val check = taskKey[Unit]("Check that dot file was created.")

check := {
  assert((crossTarget.value / "out.00.hnir").exists)
}

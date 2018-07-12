import java.util.concurrent.TimeUnit
import java.lang.ProcessBuilder
import java.nio.file.Files

scalaVersion := "2.11.12"

val runTest = taskKey[Unit]("run test")

enablePlugins(ScalaNativePlugin)
runTest := {
  val cmd  = (nativeLink in Compile).value.toString
  val file = Files.createTempFile("foo", "")
  assert(Files.exists(file))
  val proc = new ProcessBuilder(cmd, file.toString).start()
  assert(proc.waitFor(5, TimeUnit.SECONDS))
  assert(proc.exitValue == 0)
  assert(!Files.exists(file))
}

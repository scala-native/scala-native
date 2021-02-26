import java.util.concurrent.TimeUnit
import java.nio.file.Files

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else scalaVersion
}

val runTest = taskKey[Unit]("run test")

enablePlugins(ScalaNativePlugin)
runTest := {
  val cmd  = (Compile / nativeLink).value.toString
  val file = Files.createTempFile("foo", "")
  assert(Files.exists(file))
  val proc = new ProcessBuilder(cmd, file.toString).start()
  assert(proc.waitFor(5, TimeUnit.SECONDS))
  assert(proc.exitValue == 0)
  assert(!Files.exists(file))
}

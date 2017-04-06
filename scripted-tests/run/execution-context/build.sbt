ScalaNativePlugin.projectSettings

scalaVersion := "2.11.8"

lazy val runAndCheck = taskKey[Unit]("...")

runAndCheck := {
  val bin = nativeLink.value
  val out = Process(bin.getAbsolutePath).lines_!
  assert(out.mkString("\n") == "10")
}

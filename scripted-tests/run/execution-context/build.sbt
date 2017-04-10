enablePlugins(ScalaNativePlugin)

scalaVersion := "2.11.8"

lazy val runAndCheck = taskKey[Unit]("...")

runAndCheck := {
  val bin = nativeLink.value
  val out = Process(bin.getAbsolutePath).lines_!.toList
  assert(
    out == List(
      "start main",
      "end main",
      "future 1",
      "future 2",
      "future 3",
      "result: 10"
    ))
}

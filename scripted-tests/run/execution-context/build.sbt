enablePlugins(ScalaNativePlugin)

scalaVersion := "2.12.12"

lazy val runAndCheck = taskKey[Unit]("...")

runAndCheck := {
  import scala.sys.process._

  val bin = (Compile / nativeLink).value
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

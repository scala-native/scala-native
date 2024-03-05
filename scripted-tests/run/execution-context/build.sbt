enablePlugins(ScalaNativePlugin)

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

nativeConfig ~= { _.withMultithreading(false) }

lazy val runAndCheck = taskKey[Unit]("...")

runAndCheck := {
  import scala.sys.process._

  val bin = (Compile / nativeLink).value
  val out = Process(bin.getAbsolutePath).lineStream_!.toList
  assert(
    out == List(
      "start main",
      "end main",
      "future 1",
      "future 2",
      "future 3",
      "result: 10"
    )
  )
}

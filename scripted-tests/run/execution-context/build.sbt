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

import java.util.Locale
val osName = System
  .getProperty("os.name", "unknown")
  .toLowerCase(Locale.ROOT)
val isWindows = osName.startsWith("windows")

lazy val testQueueExecutionContext = taskKey[Unit]("...")
testQueueExecutionContext := {
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

lazy val testQueueExecutionContext2 = taskKey[Unit]("...")
testQueueExecutionContext2 := {
  import java.util.concurrent.TimeUnit
  val bin = (Compile / nativeLink).value
  val proc = new ProcessBuilder(bin.getAbsolutePath).start()
  val finished = proc.waitFor(1, TimeUnit.SECONDS)
  if (!finished) proc.destroyForcibly()
  assert(finished)
}

lazy val testEventLoop = taskKey[Unit]("...")
testEventLoop := Def.taskDyn {
  if (isWindows) Def.task { println("EvenLoop test skipped") }
  else
    Def.task {
      import java.util.concurrent.TimeUnit
      val bin = (Compile / nativeLink).value
      val proc = new ProcessBuilder(bin.getAbsolutePath).start()
      val finished = proc.waitFor(1, TimeUnit.SECONDS)
      if (!finished) proc.destroyForcibly()
      assert(finished)
    }
}.value

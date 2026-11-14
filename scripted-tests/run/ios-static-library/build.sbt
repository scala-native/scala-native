import scala.sys.process.Process
import scala.util.Try

import scala.scalanative.build.Discover

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

// Scala Native code built as a static library for the iOS simulator and used
// from a host program running its own threads, see #4334.
// Requires Xcode with the iOS simulator SDK and a booted simulator device,
// skipped when missing unless IOS_SIMULATOR_UDID is set explicitly.
val simulatorDevice = sys.env.get("IOS_SIMULATOR_UDID").filter(_.nonEmpty)

def output(cmd: String*): Option[String] =
  Try(Process(cmd).!!.trim).toOption.filter(_.nonEmpty)

lazy val missingRequirement: Option[String] =
  if (!sys.props("os.name").toLowerCase.startsWith("mac"))
    Some("not a macOS host")
  else if (output(
        "xcrun",
        "--sdk",
        "iphonesimulator",
        "--show-sdk-path"
      ).isEmpty)
    Some("iOS simulator SDK not found")
  else if (simulatorDevice.isEmpty &&
      !output("xcrun", "simctl", "list", "devices", "booted")
        .exists(_.contains("(Booted)")))
    Some("no booted iOS simulator")
  else None

lazy val simulatorTriple = {
  val arch = sys.props("os.arch") match {
    case "aarch64" | "arm64" => "arm64"
    case _                   => "x86_64"
  }
  s"$arch-apple-ios16.0-simulator"
}

nativeConfig ~= { config =>
  if (missingRequirement.isDefined) config
  else
    config
      .withBuildTarget(scalanative.build.BuildTarget.libraryStatic)
      .withTargetTriple(simulatorTriple)
      .withCompileOptions(Discover.compileOptions(simulatorTriple))
      .withLinkingOptions(Discover.linkingOptions(simulatorTriple))
      .withBaseName("test")
}

lazy val testInSimulator =
  taskKey[Unit]("Use the static library from a program run in iOS simulator")
testInSimulator := Def.taskDyn {
  val log = streams.value.log
  missingRequirement match {
    case Some(reason) if simulatorDevice.isEmpty =>
      Def.task(log.warn(s"Skipping iOS simulator test: $reason"))
    case Some(reason) =>
      Def.task(sys.error(s"Cannot run iOS simulator test: $reason"))
    case None =>
      Def.task {
        val _ = (Compile / nativeLink).value
        val libDir = crossTarget.value
        val host = baseDirectory.value / "host"
        def clang(binary: String, args: String*): Unit = {
          val cmd = Seq(
            "xcrun",
            "--sdk",
            "iphonesimulator",
            binary,
            "-target",
            simulatorTriple
          ) ++ args
          assert(Process(cmd, baseDirectory.value).! == 0, s"failed: $cmd")
        }
        clang("clang", "-c", "src/main/c/host.c", "-o", "host.o")
        clang(
          "clang++",
          "host.o",
          s"-L${libDir.absolutePath}",
          "-ltest",
          "-o",
          host.absolutePath
        )
        val run = Seq(
          "xcrun",
          "simctl",
          "spawn",
          simulatorDevice.getOrElse("booted"),
          host.absolutePath
        )
        val exitCode = Process(run).!
        assert(exitCode == 0, s"host program failed with code $exitCode")
      }
  }
}.value

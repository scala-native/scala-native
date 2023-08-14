import scala.sys.process.Process
import java.util.Locale
import scala.scalanative.build._

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

nativeConfig ~= { c =>
  c.withDebugMetadata(true)
    .withMode(Mode.debug) // otherwise, clang O2 inlines the call stack in Linux
}

lazy val debugBuild = taskKey[Unit]("Compile and run dsymutil if exists")
debugBuild := {
  val path = (Compile / nativeLink).value
  if (System
        .getProperty("os.name")
        .toLowerCase(Locale.ROOT)
        .startsWith("mac")) {
    Process(s"dsymutil ${path.getAbsolutePath()}") !
  }
}

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

import scala.sys.process._

nativeConfig ~= { c =>
  c.withDebugMetadata(true)
}

import scala.sys.process._
lazy val debugBuild = taskKey[Unit]("Compile and run dsymutil if exists")
debugBuild := {
  val path = (Compile / nativeLink).value
  s"./dsymutil.sh $path" !
}

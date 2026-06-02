import sbt.io._

import scala.scalanative.sbtplugin.ScalaNativeCrossVersion

val scalaVersionFromScript = sys.props.getOrElse(
  "scala.version",
  throw new RuntimeException(
    """The system property 'scala.version' is not defined.
      |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
  )
)

def expectedArtifactId(projectName: String, scalaVersion: String): String = {
  val bin = CrossVersion.binaryScalaVersion(scalaVersion)
  s"${projectName}_native${ScalaNativeCrossVersion.currentBinaryVersion}_$bin"
}

lazy val verifyPackagePaths = taskKey[Unit](
  "fail if package produces JVM-style artifact file names for a native project"
)

lazy val verifyNoJvmStyleArtifacts = taskKey[Unit](
  "fail if any JVM-style lib_<scalaBin>-* artifacts exist under target"
)

lazy val lib = project
  .in(file("lib"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    scalaVersion := scalaVersionFromScript,
    organization := "org.scala-native.test",
    version := "0.1.0-SNAPSHOT",
    publishMavenStyle := true,
    verifyPackagePaths := {
      val nativePrefix = expectedArtifactId("lib", scalaVersion.value)
      val wrongPrefix =
        s"lib_${CrossVersion.binaryScalaVersion(scalaVersion.value)}"

      val jar = (Compile / packageBin).value
      assert(
        jar.getName.startsWith(s"$nativePrefix-"),
        s"packageBin file=${jar.getName}, expected prefix '$nativePrefix-'"
      )
      assert(
        !jar.getName.startsWith(s"$wrongPrefix-"),
        s"packageBin uses JVM-style name: ${jar.getName}"
      )
    },
    verifyNoJvmStyleArtifacts := {
      val wrongPrefix =
        s"lib_${CrossVersion.binaryScalaVersion(scalaVersion.value)}-"
      val wrongJars =
        (target.value ** "*.jar").get.filter(_.getName.startsWith(wrongPrefix))
      assert(
        wrongJars.isEmpty,
        s"JVM-style artifacts under target (e.g. from sbt 2 target/out/jvm): ${wrongJars.mkString(", ")}"
      )
    }
  )

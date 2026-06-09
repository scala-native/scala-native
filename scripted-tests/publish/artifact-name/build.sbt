import sbt.io._

import scala.scalanative.sbtplugin.ScalaNativeCrossVersion

val scalaVersionFromScript = {
  val fromScript = sys.props.getOrElse(
    "scala.version",
    throw new RuntimeException(
      """The system property 'scala.version' is not defined.
        |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  )
  // sbt 2 plugin scripted runs pass the meta-build Scala as scala.version, but
  // runtime libs are published for scala3.version (see scriptedLaunchOpts).
  CrossVersion.partialVersion(fromScript) match {
    case Some((3, _)) => sys.props.getOrElse("scala3.version", fromScript)
    case _            => fromScript
  }
}

def expectedArtifactId(projectName: String, scalaVersion: String): String = {
  val bin = CrossVersion.binaryScalaVersion(scalaVersion)
  s"${projectName}_native${ScalaNativeCrossVersion.currentBinaryVersion}_$bin"
}

/** sbt 1: package output is a [[java.io.File]]; sbt 2: virtual file ref. */
def packageOutputFileName(
    output: Any
)(implicit conv: xsbti.FileConverter): String =
  output match {
    case f: java.io.File           => f.getName
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).getFileName.toString
  }

def pathFileName(path: Any)(implicit conv: xsbti.FileConverter): String =
  path match {
    case f: java.io.File           => f.getName
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).getFileName.toString
    case p: java.nio.file.Path     => p.getFileName.toString
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
      implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
      val nativePrefix = expectedArtifactId("lib", scalaVersion.value)
      val wrongPrefix =
        s"lib_${CrossVersion.binaryScalaVersion(scalaVersion.value)}"
      val jarName = packageOutputFileName((Compile / packageBin).value)

      assert(
        jarName.startsWith(s"$nativePrefix-"),
        s"packageBin file=$jarName, expected prefix '$nativePrefix-'"
      )
      assert(
        !jarName.startsWith(s"$wrongPrefix-"),
        s"packageBin uses JVM-style name: $jarName"
      )
    },
    verifyNoJvmStyleArtifacts := {
      implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
      val wrongPrefix =
        s"lib_${CrossVersion.binaryScalaVersion(scalaVersion.value)}-"
      val wrongJars =
        (target.value ** "*.jar")
          .get()
          .filter(pathFileName(_).startsWith(wrongPrefix))
      assert(
        wrongJars.isEmpty,
        s"JVM-style artifacts under target (e.g. from sbt 2 target/out/jvm): ${wrongJars.mkString(", ")}"
      )
    }
  )

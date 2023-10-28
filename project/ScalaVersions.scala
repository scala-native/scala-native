package build

/* Note to Contributors:
 *   Scala Native supports a number of Scala versions. These can be
 *   described as Major.Minor.Path.
 *
 *   Support for Scala 2.12.lowest is provided by binary compatibility with
 *   Scala 2.12.highest.
 *
 *   This means that Continuous Integration (CI) is run using
 *   the highest patch version. Scala Native may or may not build from
 *   from scratch when using lower patch versions.
 *
 *   This information can save time and frustration when preparing
 *   contributions for submission: Build privately using highest,
 *   not lowest, patch version.
 */

object ScalaVersions {
  // Versions of Scala used for publishing compiler plugins
  val crossScala212 = (14 to 18).map(v => s"2.12.$v")
  val crossScala213 = (8 to 12).map(v => s"2.13.$v")
  val crossScala3 = List(
    // windowslib fails to compile with 3.1.{0-1}
    (2 to 3).map(v => s"3.1.$v"),
    (0 to 2).map(v => s"3.2.$v"),
    (0 to 1).map(v => s"3.3.$v")
  ).flatten

  // Scala versions used for publishing libraries
  val scala212: String = crossScala212.last
  val scala213: String = crossScala213.last
  val scala3: String = crossScala3.last

  // The latest version of minimal Scala 3 minor version used to publish artifacts
  val scala3PublishVersion = "3.1.3"

  // List of nightly version can be found here: https://repo1.maven.org/maven2/org/scala-lang/scala3-compiler_3/
  lazy val scala3Nightly = "3.3.2-RC1-bin-20230601-8814760-NIGHTLY"

  // minimum version rationale:
  //   1.5 is required for Scala 3 and
  //   1.5.8 has log4j vulnerability fixed
  //   1.9.0 is required in order to use Java >= 21
  //   1.9.4 fixes (Common Vulnerabilities and Exposures) CVE-2022-46751
  //   1.9.7 fixes sbt IO.unzip vulnerability described in sbt release notes.

  val sbt10Version: String = "1.9.7"
  val sbt10ScalaVersion: String = scala212

  val libCrossScalaVersions: Seq[String] =
    crossScala212 ++ crossScala213 ++ crossScala3 ++ Seq(scala3Nightly)
}

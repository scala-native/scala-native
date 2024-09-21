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
  val crossScala212 = crossScalaVersions("2.12", 14 to 20)
  val crossScala213 = crossScalaVersions("2.13", 8 to 15)
  val crossScala3 = List(
    extraCrossScalaVersion("3.").toList,
    scala3RCVersions,
    // windowslib fails to compile with 3.1.{0-1}
    crossScalaVersions("3.1", 2 to 3),
    crossScalaVersions("3.2", 0 to 2),
    crossScalaVersions("3.3", 0 to 3),
    crossScalaVersions("3.4", 0 to 3),
    crossScalaVersions("3.5", 0 to 1)
  ).flatten.distinct

  // Tested in scheduled nightly CI to check compiler plugins
  // List maintains only upcoming releases, removed from the list after reaching stable status
  lazy val scala3RCVersions = List(
    1.to(4).map(v => s"3.3.4-RC$v"),
    1.to(1).map(v => s"3.5.2-RC$v")
  ).flatten

  // Scala versions used for publishing libraries
  val scala212: String = crossScala212.last
  val scala213: String = crossScala213.last
  val scala3: String = crossScala3.last

  // The latest version of minimal Scala 3 minor version used to publish artifacts
  val scala3PublishVersion = "3.1.3"
  val scala213PublishVersion = crossScala213.head

  // List of nightly version can be found here: https://repo1.maven.org/maven2/org/scala-lang/scala3-compiler_3/
  val scala3Nightly = "3.4.0-RC1-bin-20240114-bfabc31-NIGHTLY"

  // minimum version rationale:
  //   1.5 is required for Scala 3 and
  //   1.5.8 has log4j vulnerability fixed
  //   1.9.0 is required in order to use Java >= 21
  //   1.9.4 fixes (Common Vulnerabilities and Exposures) CVE-2022-46751
  //   1.9.7 fixes sbt IO.unzip vulnerability described in sbt release notes.
  //   1.10.0 Latest sbt version.
  //   1.10.1 Latest sbt version.

  val sbt10Version: String = "1.10.1"
  val sbt10ScalaVersion: String = scala212

  val libCrossScalaVersions: Seq[String] =
    crossScala212 ++ crossScala213 ++ crossScala3 ++ Seq(scala3Nightly)

  private def extraCrossScalaVersion(binVersionPrefix: String) = sys.env
    .get("EXTRA_CROSS_SCALA_VERSION")
    .filter(_.startsWith(binVersionPrefix))

  private def crossScalaVersions(
      baseVersion: String,
      patches: Range.Inclusive
  ): List[String] = {
    patches.map(v => s"$baseVersion.$v") ++
      extraCrossScalaVersion(baseVersion)
  }.distinct.toList
}

package build

import MyScalaNativePlugin.enableExperimentalCompiler

/* Note to Contributors:
 *   Scala Native supports a number of Scala versions. These can be
 *   described as Major.Minor.Path.
 *
 *   Support for Scala 2.12.lowest is provided by binary compatibility with
 *   Scala 2.12.highest.
 *
 *   This means that Continuous Integration (CI) is run using
 *   the highest patch version. Scala Native may or may not build from
 *   scratch when using lower patch versions.
 *
 *   This information can save time and frustration when preparing
 *   contributions for submission: Build privately using highest,
 *   not lowest, patch version.
 */

object ScalaVersions {
  // Versions of Scala used for publishing compiler plugins
  val crossScala212 = crossScalaVersions("2.12", 17 to 21)
  val crossScala213 = crossScalaVersions("2.13", 9 to 18)
  val crossScala3 = List(
    extraCrossScalaVersion("3.").toList,
    scala3RCVersions,
    // windowslib fails to compile with 3.1.{0-1}
    crossScalaVersions("3.1", 2 to 3),
    crossScalaVersions("3.2", 0 to 2),
    crossScalaVersions("3.3", 0 to 7), // LTS
    crossScalaVersions("3.4", 0 to 3),
    crossScalaVersions("3.5", 0 to 2),
    crossScalaVersions("3.6", 2 to 4), // 3.6.0 is broken, 3.6.1 is hotfix
    crossScalaVersions("3.7", 0 to 4),
    crossScalaVersions("3.8", 0 to 1)
  ).flatten.distinct

  // Tested in scheduled nightly CI to check compiler plugins
  // List maintains only upcoming releases, removed from the list after reaching stable status
  lazy val scala3RCVersions = List("3.8.2-RC1")

  // List of nightly versions can be found here: https://repo.scala-lang.org/ui/native/maven-nightlies/org/scala-lang/scala3-compiler_3
  // or check outputs of `scala -O --version -S 3.nightly`
  val scala3Nightly = "3.8.3-RC1-bin-20260123-c5418c8-NIGHTLY"

  // Scala versions used for publishing libraries
  val scala212: String = crossScala212.last
  val scala213: String = crossScala213.last
  val scala3: String = crossScala3.last

  // The latest version of minimal Scala 3 minor version used to publish artifacts
  val scala3PublishVersion = "3.1.3"
  val scala213PublishVersion = crossScala213.head

  // minimum version rationale:
  //   1.5 is required for Scala 3 and
  //   1.5.8 has log4j vulnerability fixed
  //   1.9.0 is required in order to use Java >= 21
  //   1.9.4 fixes (Common Vulnerabilities and Exposures) CVE-2022-46751
  //   1.9.7 fixes sbt IO.unzip vulnerability described in sbt release notes.
  //   1.10.7 Latest sbt version, 1.10.2 had bug, see comment in SN Issue #4126
  //   1.11.5 Scala 3.8 standard library changes support
  val sbt10Version: String = "1.11.5"
  val sbt10ScalaVersion: String = scala212

  val sbt2Version: String = "2.0.0-RC8"
  val sbt2ScalaVersion: String = "3.7.4"

  val crossSbtVersions = Seq(sbt10Version, sbt2Version)
  val crossSbtScalaVersions = Seq(sbt10ScalaVersion, sbt2ScalaVersion)

  val libCrossScalaVersions: Seq[String] = Seq(
    crossScala212,
    crossScala213,
    crossScala3,
    Option(scala3Nightly).filter(_ => enableExperimentalCompiler).toSeq
  ).flatten.distinct

  // Scala 2.13 cannot be used with Scala 3.8 (see, scala3/cross-version-compat)
  val scriptedTestsScala3Version: String = "3.7.4".ensuring(
    !crossScala213.contains("2.13.19"),
    "Update scriptedTestsScala3Version when new Scala 3 version is released"
  )

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

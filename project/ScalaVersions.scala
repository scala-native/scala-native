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
  val crossScala212 = (13 to 18).map(v => s"2.12.$v")
  val crossScala213 = (4 to 12).map(v => s"2.13.$v")
  val crossScala3 = List(
    Seq(scala3Nightly),
    (0 to 3).map(v => s"3.1.$v"),
    (0 to 2).map(v => s"3.2.$v"),
    (0 to 0).map(v => s"3.3.$v")
  ).flatten

  // Version of Scala 3 standard library sources used for publishing
  // Workaround allowing to produce NIR for Scala 3.2.x+ and allowing to consume existing libraries using 3.1.x
  val scala3libSourcesVersion = crossScala3.last

  // Scala versions used for publishing libraries
  val scala212: String = crossScala212.last
  val scala213: String = crossScala213.last
  val scala3: String = "3.1.3"
  // List of nightly version can be found here: https://repo1.maven.org/maven2/org/scala-lang/scala3-compiler_3/
  lazy val scala3Nightly = "3.3.2-RC1-bin-20230601-8814760-NIGHTLY"

  // minimum version - 1.5 is required for Scala 3 and 1.5.8 has log4j vulnerability fixed
  val sbt10Version: String = "1.5.8"
  val sbt10ScalaVersion: String = scala212

  val libCrossScalaVersions: Seq[String] =
    crossScala212 ++ crossScala213 ++ crossScala3
}

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
    // Bug in compiler leads to errors in tests, don't use it as default until RC2
    Seq("3.4.0-RC1"),
    (0 to 3).map(v => s"3.1.$v"),
    (0 to 2).map(v => s"3.2.$v"),
    (0 to 1).map(v => s"3.3.$v")
  ).flatten

  // Version of Scala 3 standard library sources used for publishing
  // Workaround allowing to produce NIR for Scala 3.2.x+ and allowing to consume existing libraries using 3.1.x
  // 3.3.0 is the last version which can be compiled using 3.1.3 compiler
  val scala3libSourcesVersion = "3.3.0"

  // Scala versions used for publishing libraries
  val scala212: String = crossScala212.last
  val scala213: String = crossScala213.last
  val scala3: String = "3.1.3"

  // minimum version rationale:
  //   1.5 is required for Scala 3 and
  //   1.5.8 has log4j vulnerability fixed
  //   1.9.0 is required in order to use Java >= 21
  //   1.9.4 fixes (Common Vulnerabilities and Exposures) CVE-2022-46751
  //   1.9.6 is current

  val sbt10Version: String = "1.9.6"
  val sbt10ScalaVersion: String = scala212

  val libCrossScalaVersions: Seq[String] =
    crossScala212 ++ crossScala213 ++ crossScala3
}

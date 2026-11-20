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
  // Extras and release candidates stay ahead of the stable line, so `scala3`
  // remains the newest stable patch. An extra that is already stable is not
  // prepended: `distinct` would keep that early copy and drop the stable one.
  val crossScala3 = {
    val released = List(
      // windowslib fails to compile with 3.1.{0-1}
      patchVersions("3.1", 2 to 3),
      patchVersions("3.2", 0 to 2),
      patchVersions("3.3", 0 to 8), // LTS
      patchVersions("3.4", 0 to 3),
      patchVersions("3.5", 0 to 2),
      patchVersions("3.6", 2 to 4), // 3.6.0 is broken, 3.6.1 is hotfix
      patchVersions("3.7", 0 to 4),
      patchVersions("3.8", 0 to 4),
      patchVersions("3.9", 0 to 0), // LTS
      patchVersions("3.10", 0 to 1)
    ).flatten
    extraCrossScalaVersion("3.").filterNot(released.contains).toList ++
      scala3RCVersions ++
      released
  }.distinct

  // Tested in scheduled nightly CI to check compiler plugins
  // List maintains only upcoming releases, removed from the list after reaching stable status
  lazy val scala3RCVersions: List[String] = Nil

  // Scala versions used for publishing libraries
  val scala212: String = crossScala212.last
  val scala213: String = crossScala213.last
  val scala3: String = crossScala3.last

  // The latest version of minimal Scala 3 minor version used to publish artifacts
  val scala3PublishVersion = "3.1.3"

  val sbt10Version: String = "1.12.14"
  val sbt10ScalaVersion: String = scala212

  val sbt2Version: String = "2.0.8"
  val sbt2ScalaVersion: String = "3.8.4"

  val crossSbtVersions = Seq(sbt10Version, sbt2Version)
  val crossSbtScalaVersions = Seq(sbt10ScalaVersion, sbt2ScalaVersion)

  val libCrossScalaVersions: Seq[String] = Seq(
    crossScala212,
    crossScala213,
    crossScala3
  ).flatten.distinct

  // Scala 2.13 cannot be used with Scala 3.8 (see, scala3/cross-version-compat)
  val scriptedTestsScala3Version: String = "3.7.4".ensuring(
    !crossScala213.contains("2.13.19"),
    "Update scriptedTestsScala3Version when new Scala 3 version is released"
  )

  private def extraCrossScalaVersion(binVersionPrefix: String) = sys.env
    .get("EXTRA_CROSS_SCALA_VERSION")
    .filter(matchesBinaryPrefix(binVersionPrefix))

  // A shorter minor prefix must not match a later minor. Match at a dot boundary.
  private def matchesBinaryPrefix(binVersionPrefix: String)(version: String) =
    val boundary =
      if binVersionPrefix.endsWith(".") then binVersionPrefix
      else s"$binVersionPrefix."
    version.startsWith(boundary)

  private def patchVersions(
      baseVersion: String,
      patches: Range.Inclusive
  ): List[String] =
    patches.map(v => s"$baseVersion.$v").toList

  private def crossScalaVersions(
      baseVersion: String,
      patches: Range.Inclusive
  ): List[String] = {
    val released = patchVersions(baseVersion, patches)
    released ++ extraCrossScalaVersion(baseVersion).filterNot(released.contains)
  }
}

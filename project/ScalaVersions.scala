package build

object ScalaVersions {
  // Versions of Scala used for publishing compiler plugins
  val crossScala211 = Seq("2.11.12")
  val crossScala212 = Seq("2.12.13", "2.12.14", "2.12.15", "2.12.16", "2.12.17")
  val crossScala213 = Seq("2.13.4", "2.13.5", "2.13.6", "2.13.7", "2.13.8", "2.13.9", "2.13.10")
  val crossScala3 = Seq("3.1.0", "3.1.1", "3.1.2", "3.1.3", "3.2.0")

  // Version of Scala 3 standard library sources used for publishing
  // Workaround allowing to produce NIR for Scala 3.2.x+ and allowing to consume existing libraries using 3.1.x
  val scala3libSourcesVersion = crossScala3.last

  // Scala versions used for publishing libraries
  val scala211: String = crossScala211.last
  val scala212: String = crossScala212.last
  val scala213: String = crossScala213.last
  val scala3: String = "3.1.3"

  val sbt10Version: String = "1.1.6" // minimum version
  val sbt10ScalaVersion: String = scala212

  val libCrossScalaVersions: Seq[String] =
    crossScala211 ++ crossScala212 ++ crossScala213 ++ crossScala3
}

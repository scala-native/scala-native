package build

object ScalaVersions {
  // Versions of Scala used for publishing compiler plugins
  val crossScala211 = Seq("2.11.12")
  val crossScala212 = (13 to 17).map(v => s"2.12.$v")
  val crossScala213 = (4 to 10).map(v => s"2.13.$v")
  val crossScala3 = List(
    Seq(scala3Experimental),
    (0 to 3).map(v => s"3.1.$v"),
    (0 to 1).map(v => s"3.2.$v")
  ).flatten

  // Version of Scala 3 standard library sources used for publishing
  // Workaround allowing to produce NIR for Scala 3.2.x+ and allowing to consume existing libraries using 3.1.x
  val scala3libSourcesVersion = crossScala3.last

  // Scala versions used for publishing libraries
  val scala211: String = crossScala211.last
  val scala212: String = crossScala212.last
  val scala213: String = crossScala213.last
  val scala3: String = "3.1.3"
  lazy val scala3Experimental = "3.3.0-RC1-bin-20221119-786ad3f-NIGHTLY"

  val sbt10Version: String = "1.1.6" // minimum version
  val sbt10ScalaVersion: String = scala212

  val libCrossScalaVersions: Seq[String] =
    crossScala211 ++ crossScala212 ++ crossScala213 ++ crossScala3
}

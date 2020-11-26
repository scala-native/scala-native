package build

object ScalaVersions {
  val scala211: String = "2.11.12"
  val scala212: String = "2.12.12"
  val scala213: String = "2.13.4"

  val sbt10Version: String               = "1.1.6" // minimum version
  val sbt10ScalaVersion: String          = scala212
  val libCrossScalaVersions: Seq[String] = Seq(scala211, scala212, scala213)
}

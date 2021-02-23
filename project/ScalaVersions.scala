package build

object ScalaVersions {
  val crossScala213 = Seq("2.13.4", "2.13.5")

  val scala211: String = "2.11.12"
  val scala212: String = "2.12.13"
  val scala213: String = crossScala213.last

  val sbt10Version: String      = "1.1.6" // minimum version
  val sbt10ScalaVersion: String = scala212

  val libCrossScalaVersions: Seq[String] =
    Seq(scala211, scala212) ++ crossScala213
}

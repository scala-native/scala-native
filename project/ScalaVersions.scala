package build

object ScalaVersions {
  val crossScala211 = Seq("2.11.12")
  val crossScala212 = Seq("2.12.13", "2.12.14", "2.12.15")
  val crossScala213 = Seq("2.13.4", "2.13.5", "2.13.6", "2.13.7", "2.13.8")
  val crossScala3 = Seq("3.1.0", "3.1.1", "3.1.2")

  val scala211: String = crossScala211.last
  val scala212: String = crossScala212.last
  val scala213: String = crossScala213.last
  val scala3: String = crossScala3.last

  val sbt10Version: String = "1.1.6" // minimum version
  val sbt10ScalaVersion: String = scala212

  val libCrossScalaVersions: Seq[String] =
    crossScala211 ++ crossScala212 ++ crossScala213 ++ crossScala3
}

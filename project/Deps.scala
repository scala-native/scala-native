package build

import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._

object Deps {
// scalafmt: { align.preset = more, maxColumn = 120 }
  def ScalaLibrary(version: String) = scalaVersionsDependendent(version) {
    case (2, _) => Seq("org.scala-lang" % "scala-library" % version)
    case (3, _) => Seq("org.scala-lang" % "scala3-library" % version)
  }.headOption.getOrElse(throw new RuntimeException("Unknown Scala versions"))
  def ScalaCompiler(version: String) = scalaVersionsDependendent(version) {
    case (2, _) => Seq("org.scala-lang" % "scala-compiler" % version)
    case (3, _) => Seq("org.scala-lang" %% "scala3-compiler" % version)
  }.headOption.getOrElse(throw new RuntimeException("Unknown Scala versions"))
  def ScalaReflect(version: String) = "org.scala-lang" % "scala-reflect" % version

  def ScalaCheck(scalaVersion: String) = scalaVersionsDependendent(scalaVersion) {
    case (2, 11) => "org.scalacheck" %% "scalacheck" % "1.15.2" :: Nil // Last released version
    case _       => "org.scalacheck" %% "scalacheck" % "1.15.4" :: Nil
  }.headOption.getOrElse(throw new RuntimeException("Unknown Scala versions"))

  lazy val ScalaTest           = "org.scalatest"          %% "scalatest"                  % "3.2.9"
  lazy val ScalaParCollections = "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.3"
  lazy val SbtPlatformDeps     = "org.portable-scala"      % "sbt-platform-deps"          % "1.0.1"
  lazy val SbtTestInterface    = "org.scala-sbt"           % "test-interface"             % "1.0"
  lazy val JUnitInterface      = "com.novocode"            % "junit-interface"            % "0.11"
  lazy val JUnit               = "junit"                   % "junit"                      % "4.13.2"

  def Tools(scalaVersion: String) = {
    List(ScalaCheck(scalaVersion) % "test", ScalaTest % "test") ++
      scalaVersionsDependendent(scalaVersion) {
        case (2, 11 | 12) => Nil
        case _            => ScalaParCollections :: Nil
      }
  }
  def NativeLib(scalaVersion: String) = scalaVersionsDependendent(scalaVersion) {
    case (2, _) => ScalaReflect(scalaVersion) :: Nil
    case _      => Nil
  }
  def ScalaPartest(scalaVersion: String) = List(SbtTestInterface) ++ scalaVersionsDependendent(scalaVersion) {
    case (2, 11) => "org.scala-lang.modules" %% "scala-partest" % "1.0.16" :: Nil
    case (2, _)  => "org.scala-lang"          % "scala-partest" % scalaVersion :: Nil
    case (3, _)  => "org.scala-lang"          % "scala-partest" % ScalaVersions.scala213 :: Nil

  }
  lazy val TestRunner = List(SbtTestInterface, JUnitInterface, JUnit)
  lazy val JUnitJvm   = List(JUnitInterface % "test", JUnit % "test")
  private def scalaVersionsDependendent(
      scalaVersion: String
  )(matching: PartialFunction[(Long, Long), Seq[ModuleID]]): Seq[ModuleID] = {
    CrossVersion
      .partialVersion(scalaVersion)
      .fold(Seq.empty[ModuleID])(matching)
  }

  def compilerPluginDependencies(scalaVersion: String): Seq[ModuleID] =
    scalaVersionsDependendent(scalaVersion) {
      case (2, _) =>
        List(
          ScalaCompiler(scalaVersion),
          ScalaReflect(scalaVersion)
        )
      case (3, _) =>
        ScalaCompiler(scalaVersion) :: Nil
    }
}

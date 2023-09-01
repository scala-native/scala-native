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

  lazy val SbtPlatformDeps  = "org.portable-scala" % "sbt-platform-deps" % "1.0.1"
  lazy val SbtTestInterface = "org.scala-sbt"      % "test-interface"    % "1.0"
  lazy val JUnitInterface   = "com.github.sbt"     % "junit-interface"   % "0.13.3"
  lazy val JUnit            = "junit"              % "junit"             % "4.13.2"

  def NativeLib(scalaVersion: String) = scalaVersionsDependendent(scalaVersion) {
    case (2, _) => ScalaReflect(scalaVersion) :: Nil
    case _      => Nil
  }
  def ScalaPartest(scalaVersion: String) = List(SbtTestInterface) ++ scalaVersionsDependendent(scalaVersion) {
    case (2, _) => "org.scala-lang" % "scala-partest" % scalaVersion :: Nil
    case (3, _) => "org.scala-lang" % "scala-partest" % ScalaVersions.scala213 :: Nil

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

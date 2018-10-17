import sbt._
import Keys.{name, unmanagedResourceDirectories, baseDirectory}
import sbtcrossproject._
import scala.scalanative.build
import build.{Bits, ThirtyTwo, SixtyFour, TargetArchitecture}
import scala.scalanative.sbtplugin.ScalaNativeCrossVersion
import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._

final case class CrossBitsPlatform(bits: Bits) extends Platform {
  def identifier: String = "_" + bits.toString
  def sbtSuffix: String  = "_" + bits.toString
  def enable(project: Project): Project = {
    project
      .settings(
        ScalaNativePlugin.projectSettings.tail
      )
      .settings(
        unmanagedResourceDirectories in Compile += {
          baseDirectory.value / ".." / "shared" / "src/main/resources"
        }
      )
      .settings(
        nativeTargetArchitecture := (bits match {
          case ThirtyTwo => TargetArchitecture.i386
          case SixtyFour => TargetArchitecture.x86_64
        })
      )
  }
}

object CrossBitsPlatform {
  implicit def CrossBitsProjectBuilderOps(
      builder: CrossProject.Builder): CrossBitsProjectOps =
    new CrossBitsProjectOps(builder.crossType(CrossType.Full))

  implicit class CrossBitsProjectOps(project: CrossProject) {
    def crossBits(bits: Bits): Project =
      project.projects(CrossBitsPlatform(bits))

    def bitsSettings(bits: Bits)(ss: Def.SettingsDefinition*): CrossProject =
      bitsConfigure(bits)(_.settings(ss: _*))

    def bitsConfigure(bits: Bits)(
        transformer: Project => Project): CrossProject =
      project.configurePlatform(CrossBitsPlatform(bits))(transformer)
  }
}

import sbt._
import Keys.{name, unmanagedResourceDirectories, baseDirectory}
import sbtcrossproject._
import scala.scalanative.build
import build.Bits
import scala.scalanative.sbtplugin.ScalaNativeCrossVersion
import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._

case class CrossBitsPlatform(bits: Bits,
                                     lib: Boolean = false)
    extends Platform {
  def identifier: String = "_bits-" + bits.toString
  def sbtSuffix: String  = "_bits-" + bits.toString
  def enable(project: Project): Project = {
    (if (lib) {
       project.settings(
         ScalaNativePlugin.projectSettings.tail
       )
     } else {
       project
         .enablePlugins(ScalaNativePlugin)
     })
      .settings(
        unmanagedResourceDirectories in Compile += {
          baseDirectory.value / ".." / "shared" / "src/main/resources"
        }
      )
  }

  @deprecated("Will be removed", "0.3.0")
  val crossBinary: CrossVersion = ScalaNativeCrossVersion.binary(bits)

  @deprecated("Will be removed", "0.3.0")
  val crossFull: CrossVersion = ScalaNativeCrossVersion.full(bits)

  override def equals(other: Any) = other match {
    case CrossBitsPlatform(arch, _) => bits == arch
    case _                                  => false
  }
}

object CrossBitsLibPlatform {
  def apply(arch: Bits) =
    CrossBitsPlatform(arch, lib = true)
}

object CrossBitsPlatform {
  implicit def CrossBitsProjectBuilderOps(
      builder: CrossProject.Builder): CrossBitsProjectOps =
    new CrossBitsProjectOps(builder.crossType(CrossType.Full))

  implicit class CrossBitsProjectOps(project: CrossProject) {
    def crossBits(bits: Bits): Project =
      project.projects(CrossBitsPlatform(bits))

    def bitsSettings(bits: Bits)(
        ss: Def.SettingsDefinition*): CrossProject =
      bitsConfigure(bits)(_.settings(ss: _*))

    def bitsConfigure(bits: Bits)(
        transformer: Project => Project): CrossProject =
      project.configurePlatform(CrossBitsPlatform(bits))(
        transformer)
  }
}

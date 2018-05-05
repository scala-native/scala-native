import sbt._
import Keys.{name, unmanagedResourceDirectories, baseDirectory}
import sbtcrossproject._
import scala.scalanative.build
import scala.scalanative.build.TargetArchitecture
import scala.scalanative.sbtplugin.ScalaNativeCrossVersion
import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._

case class CrossArchitecturePlatform(architecture: TargetArchitecture, lib: Boolean = false) extends Platform {
  def identifier: String = "_arch-" + architecture.toString
  def sbtSuffix: String  = "_arch-" + architecture.toString
  def enable(project: Project): Project = {
    (if (lib) {
      project.settings(
        ScalaNativePlugin.projectSettings.tail ++ Seq(
          targetArchitecture := architecture,
          targetArchitecture in Test := architecture
        )
      )
    } else {
      project.enablePlugins(ScalaNativePlugin).settings(
        targetArchitecture := architecture,
        targetArchitecture in Test := architecture
      )
    }).settings(
      name := name.value.dropRight(architecture.toString.length)
    ).settings(
      unmanagedResourceDirectories in Compile += {
        baseDirectory.value / ".." / "shared" / "src/main/resources"
      }
    )
  }

  @deprecated("Will be removed", "0.3.0")
  val crossBinary: CrossVersion = ScalaNativeCrossVersion.binary(architecture)

  @deprecated("Will be removed", "0.3.0")
  val crossFull: CrossVersion = ScalaNativeCrossVersion.full(architecture)

  override def equals(other: Any) = other match {
    case CrossArchitecturePlatform(arch, _) => architecture == arch
    case _ => false
  }
}

object CrossArchitectureLibPlatform {
  def apply(arch: TargetArchitecture) = CrossArchitecturePlatform(arch, lib = true)
}

object CrossArchitecturePlatform {
  implicit def CrossArchitectureProjectBuilderOps(
      builder: CrossProject.Builder): CrossArchitectureProjectOps =
  new CrossArchitectureProjectOps(builder.crossType(CrossType.Full))

  implicit class CrossArchitectureProjectOps(project: CrossProject) {
    def crossArchitecture(architecture: TargetArchitecture): Project = project.projects(CrossArchitecturePlatform(architecture))

    def architectureSettings(architecture: TargetArchitecture)(ss: Def.SettingsDefinition*): CrossProject =
      architectureConfigure(architecture)(_.settings(ss: _*))

    def architectureConfigure(architecture: TargetArchitecture)(transformer: Project => Project): CrossProject =
      project.configurePlatform(CrossArchitecturePlatform(architecture))(transformer)
  }
}

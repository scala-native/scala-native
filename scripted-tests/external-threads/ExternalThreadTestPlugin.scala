import sbt.Keys._
import sbt._

import scala.scalanative.build.BuildTarget
import scala.scalanative.sbtplugin.ScalaNativePlugin

import ScalaNativePlugin.autoImport._

/** Shared settings copied into each standard scripted fixture. */
object ExternalThreadTestPlugin extends AutoPlugin {
  override def requires = ScalaNativePlugin
  object autoImport {
    val scenarioConfiguration =
      settingKey[String]("Named configuration from configurations.tsv")
    @transient val prepareScenarios =
      taskKey[Unit]("Link the library and build C/C++ hosts")
    @transient val runScenarios = taskKey[Unit]("Run every applicable scenario")
    @transient val verifyScenarioCoverage =
      taskKey[Unit]("Require every scenario and configuration to have run")
    @transient val scenarioFileConverter =
      taskKey[Option[xsbti.FileConverter]]("sbt 2 file converter")
    def asFile(output: Any, converter: Option[xsbti.FileConverter]): File =
      output match {
        case file: File                => file
        case ref: xsbti.VirtualFileRef =>
          converter
            .getOrElse(sys.error("Missing sbt file converter"))
            .toPath(ref)
            .toFile
      }
  }
  import autoImport._
  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    scalaVersion := sys.props("scala.version"),
    Compile / mainClass := None,
    scenarioConfiguration := ExternalThreadScenarios
      .configurations(baseDirectory.value)
      .head
      .name,
    nativeConfig := ExternalThreadScenarios.configuration(
      nativeConfig.value
        .withBuildTarget(BuildTarget.libraryStatic)
        .withBaseName("external"),
      baseDirectory.value,
      scenarioConfiguration.value
    ),
    Compile / unmanagedSources := {
      val enabled =
        ExternalThreadScenarios.virtualThreadsAvailable(scalaVersion.value)
      (Compile / unmanagedSources).value.filter(source =>
        enabled || !source.getName.endsWith("VirtualCallbacks.scala")
      )
    },
    scenarioFileConverter := Project
      .extract(state.value)
      .getOpt(SettingKey[xsbti.FileConverter]("fileConverter")),
    prepareScenarios := ExternalThreadScenarios.prepare(
      baseDirectory.value,
      target.value / "external-scenarios",
      (Compile / nativeLink / nativeConfig).value,
      asFile((Compile / nativeLink).value, scenarioFileConverter.value),
      scenarioConfiguration.value,
      ExternalThreadScenarios.virtualThreadsAvailable(scalaVersion.value)
    ),
    runScenarios := {
      prepareScenarios.value
      ExternalThreadScenarios.runAll(
        baseDirectory.value,
        target.value / "external-scenarios",
        (Compile / nativeLink / nativeConfig).value,
        scenarioConfiguration.value,
        ExternalThreadScenarios.virtualThreadsAvailable(scalaVersion.value),
        sLog.value
      )
    },
    verifyScenarioCoverage := ExternalThreadScenarios.verifyCoverage(
      baseDirectory.value,
      target.value / "external-scenarios",
      (Compile / nativeLink / nativeConfig).value,
      sLog.value
    )
  )
}

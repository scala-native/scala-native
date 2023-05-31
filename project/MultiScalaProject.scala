package build

import sbt._
import Keys._
import Def.SettingsDefinition
import scala.language.implicitConversions

final case class MultiScalaProject private (
    private val projects: Map[String, Project]
) extends CompositeProject {
  import MultiScalaProject._

  def project(v: String) = projects.getOrElse(
    v,
    throw new RuntimeException(
      s"Selected project is not defined for version $v"
    )
  )

  lazy val v2_12: Project = project("2.12")
  lazy val v2_13: Project = project("2.13")
  lazy val v3: Project = project("3")
  lazy val v3Next: Project = project("3-next")
    .settings(
      Settings.experimentalScalaSources,
      Settings.noPublishSettings
    )

  override def componentProjects: Seq[Project] = Seq(v2_12, v2_13, v3, v3Next)

  def mapBinaryVersions(
      mapping: String => Project => Project
  ): MultiScalaProject = {
    copy(projects = projects.map {
      case (binVersion, project) => (binVersion, mapping(binVersion)(project))
    })
  }

  def forBinaryVersion(version: String): Project = project(version)

  def settings(ss: SettingsDefinition*): MultiScalaProject =
    transform(_.settings(ss: _*))

  def enablePlugins(ns: Plugins*): MultiScalaProject =
    transform(_.enablePlugins(ns: _*))

  def dependsOn(deps: ScopedMultiScalaProject*): MultiScalaProject = {
    def classpathDependency(d: ScopedMultiScalaProject) =
      strictMapValues(d.project.projects)(
        ClasspathDependency(_, d.configuration)
      )

    val depsByVersion: Map[String, Seq[ClasspathDependency]] =
      strictMapValues(deps.flatMap(classpathDependency).groupBy(_._1))(
        _.map(_._2)
      )
    zipped(depsByVersion)(_.dependsOn(_: _*))
  }

  def configs(cs: Configuration*): MultiScalaProject =
    transform(_.configs(cs: _*))

  def zippedSettings(that: MultiScalaProject)(
      ss: Project => SettingsDefinition
  ): MultiScalaProject =
    zipped(that.projects)((p, sp) => p.settings(ss(sp)))

  def zippedSettings(project: String)(
      ss: LocalProject => SettingsDefinition
  ): MultiScalaProject =
    zippedSettings(Seq(project))(ps => ss(ps(0)))

  /** Set settings on this MultiScalaProject depending on other
   *  MultiScalaProjects by name.
   *
   *  For every Scala version of this MultiScalaProject, `ss` is invoked onced
   *  with a LocalProjects corresponding to the names in projectNames with a
   *  suffix for that version.
   */
  def zippedSettings(
      projectNames: Seq[String]
  )(ss: Seq[LocalProject] => SettingsDefinition): MultiScalaProject = {
    val ps = for {
      (v, p) <- projects
    } yield {
      val lps = projectNames.map(pn => LocalProject(projectID(pn, v)))
      v -> p.settings(ss(lps))
    }
    copy(projects = ps)
  }

  def %(configuration: String) =
    new ScopedMultiScalaProject(this, Some(configuration))

  private def zipped[T](
      that: Map[String, T]
  )(f: (Project, T) => Project): MultiScalaProject = {
    val ps = for ((v, p) <- projects) yield v -> f(p, that(v))
    copy(projects = ps)
  }

  private def transform(f: Project => Project): MultiScalaProject =
    copy(projects = strictMapValues(projects)(f))
}

final class ScopedMultiScalaProject(
    val project: MultiScalaProject,
    val configuration: Option[String]
)

object ScopedMultiScalaProject {
  implicit def fromMultiScalaProject(
      mp: MultiScalaProject
  ): ScopedMultiScalaProject =
    new ScopedMultiScalaProject(mp, None)
}

object MultiScalaProject {
  private def strictMapValues[K, U, V](v: Map[K, U])(f: U => V): Map[K, V] =
    v.map(v => (v._1, f(v._2)))

  final val scalaCrossVersions = Map[String, Seq[String]](
    "2.12" -> ScalaVersions.crossScala212,
    "2.13" -> ScalaVersions.crossScala213,
    "3" -> ScalaVersions.crossScala3,
    "3-next" -> Seq(ScalaVersions.scala3Nightly)
  )

  final val scalaVersions = Map[String, String](
    "2.12" -> ScalaVersions.scala212,
    "2.13" -> ScalaVersions.scala213,
    "3" -> ScalaVersions.scala3,
    "3-next" -> ScalaVersions.scala3Nightly
  )

  private def projectID(id: String, major: String) =
    major match {
      case "3-next" => id + "3_next"
      case _        => id + major.replace('.', '_')
    }

  def apply(id: String): MultiScalaProject =
    apply(id, file(id))
  def apply(
      id: String,
      base: File
  ): MultiScalaProject = {
    val projects = for {
      (major, minors) <- scalaCrossVersions
    } yield {
      major -> Project(
        id = projectID(id, major),
        base = new File(base, "." + major)
      ).settings(
        Settings.commonSettings,
        name := Settings.projectName(id),
        scalaVersion := scalaVersions(major),
        crossScalaVersions := minors
      )
    }

    new MultiScalaProject(projects).settings(
      sourceDirectory := baseDirectory.value.getParentFile / "src"
    )
  }
}

package build

import sbt.Keys._
import sbt._

import java.nio.file.{Files, Path}

import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaPlugin.autoImport._

import scala.scalanative.io.VirtualDirectory
import scala.scalanative.nir
import scala.scalanative.util.Scope

/** NIR Migration Manager: detects extern symbols removed or changed since a
 *  previous release.
 *
 *  Calls to externs are linked by their NIR name, fixed when the caller is
 *  compiled: owner and `@name` value, or the Scala name without `@name`. MiMa
 *  can't see that name, it compares JVM signatures only. Changing it breaks
 *  libraries compiled against older releases at link time, see
 *  `unsafe.externRedirect` for the way to rename an extern safely.
 *
 *  Doesn't use sbt, to allow extracting it from the build.
 */
object NirMiMa {

  /** Fully qualified extern name mapped to its NIR type. */
  type Externs = Map[String, String]

  sealed abstract class Problem {
    def symbol: String
    def description: String
  }
  final case class Missing(symbol: String, ty: String) extends Problem {
    def description = s"extern $symbol: $ty is missing"
  }
  final case class TypeChanged(symbol: String, from: String, to: String)
      extends Problem {
    def description = s"extern $symbol changed type from $from to $to"
  }

  /** Reads externs from a directory or a jar. Visibility is not a part of NIR,
   *  so internal externs are included too. An alias created using
   *  `unsafe.externRedirect` counts as an extern, it keeps the old name
   *  working.
   */
  def collect(path: Path): Externs = Scope { implicit in =>
    val directory =
      if (Files.isDirectory(path)) VirtualDirectory.real(path)
      else VirtualDirectory.jar(path)
    directory.files.iterator
      .filter(_.toString.endsWith(".nir"))
      .flatMap(nir.serialization.deserializeBinary(directory, _))
      .flatMap(extern)
      .toMap
  }

  private def extern(defn: nir.Defn): Option[(String, String)] = {
    val ty = defn match {
      case defn: nir.Defn.Declare => Some(defn.ty)
      // Exported to C, Scala code calls the method itself
      case defn: nir.Defn.Define if defn.attrs.isExtern => None
      case defn: nir.Defn.Define                        => Some(defn.ty)
      case defn: nir.Defn.Var                           => Some(defn.ty)
      case defn: nir.Defn.Const                         => Some(defn.ty)
      case _                                            => None
    }
    (defn.name, ty) match {
      case (nir.Global.Member(owner, sig), Some(ty)) if sig.isExtern =>
        sig.unmangled match {
          case nir.Sig.Extern(id) => Some(s"${owner.id}.$id" -> ty.show)
          case _                  => None
        }
      case _ => None
    }
  }

  def compare(previous: Externs, current: Externs): Seq[Problem] =
    previous.toSeq.sortBy(_._1).flatMap {
      case (symbol, ty) =>
        current.get(symbol) match {
          case None          => Some(Missing(symbol, ty))
          case Some(`ty`)    => None
          case Some(changed) => Some(TypeChanged(symbol, ty, changed))
        }
    }

  /** Matches `symbol` against a filter, in which `*` stands for any text. */
  def isFiltered(symbol: String, filter: String): Boolean =
    filter
      .split("\\*", -1)
      .map(java.util.regex.Pattern.quote)
      .mkString(".*")
      .r
      .pattern
      .matcher(symbol)
      .matches()
}

/** Reports externs broken since the releases checked by MiMa. */
object NirMiMaPlugin extends AutoPlugin {
  override def requires: Plugins = MimaPlugin
  override def trigger = allRequirements

  object autoImport {
    val nirExternIssueFilters = settingKey[Seq[String]](
      "Fully qualified externs excluded from the report, `*` matches any text."
    )
    val nirExternReportIssues = taskKey[Unit](
      "Fails if an extern of a previous release is missing or has changed type."
    )
  }
  import autoImport._

  private val moduleFilters: Map[String, Seq[String]] = Map(
    // Internal contract between the runtime and its C code
    "nativelib" -> Seq("scala.scalanative.runtime.*")
  )

  override def projectSettings: Seq[Setting[_]] = Seq(
    nirExternIssueFilters := moduleFilters.getOrElse(name.value, Nil),
    nirExternReportIssues := Def.uncached {
      val log = streams.value.log
      val filters = nirExternIssueFilters.value
      val previous = mimaPreviousClassfiles.value
      val outputs = (Compile / products).value
      val projectName = name.value
      // Previous Scala 3 artifacts were compiled with scala3PublishVersion,
      // newer compilers emit externs inherited from traits differently.
      val comparable = scalaBinaryVersion.value != "3" ||
        scalaVersion.value == ScalaVersions.scala3PublishVersion

      if (!comparable)
        log.info(
          s"$projectName: skipped, externs are checked with Scala ${ScalaVersions.scala3PublishVersion}"
        )
      else if (previous.nonEmpty) {
        val current =
          outputs.flatMap(p => NirMiMa.collect(p.toPath)).toMap
        val problems = previous.toSeq.flatMap {
          case (module, file) =>
            val externs = NirMiMa.collect(file.toPath)
            log.info(
              s"$projectName: checked ${externs.size} externs of ${module.revision}"
            )
            NirMiMa
              .compare(externs, current)
              .filterNot { problem =>
                filters.exists(
                  NirMiMa.isFiltered(problem.symbol, _)
                )
              }
              .map(module.revision -> _)
        }
        problems.foreach {
          case (revision, problem) =>
            log.error(s"$projectName: since $revision: ${problem.description}")
        }
        if (problems.nonEmpty)
          throw new MessageOnlyException(
            s"$projectName: ${problems.size} extern incompatibilities, add an " +
              "unsafe.externRedirect or a filter to nirExternIssueFilters"
          )
      }
    }
  )
}

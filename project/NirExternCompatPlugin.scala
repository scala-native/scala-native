package build

import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaPlugin.autoImport._
import sbt.Keys._
import sbt._

/** Reports externs broken since the releases checked by MiMa. */
object NirExternCompatPlugin extends AutoPlugin {
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
    // Contract between the runtime and its C code, not used by libraries
    "nativelib" -> Seq(
      "scala.scalanative.runtime.Continuations$.scalanative_continuation_exception_to_failure",
      "scala.scalanative.runtime.GC$.scalanative_GC_CreateThread",
      "scala.scalanative.runtime.GC$.scalanative_GC_alloc",
      "scala.scalanative.runtime.GC$.scalanative_GC_alloc_array",
      "scala.scalanative.runtime.GC$.scalanative_GC_alloc_large",
      "scala.scalanative.runtime.GC$.scalanative_GC_alloc_small",
      "scala.scalanative.runtime.GC$.scalanative_GC_pthread_create",
      "scala.scalanative.runtime.StringConstants$.snErrorPrefix",
      "scala.scalanative.runtime.StringConstants$.snFatalErrorPrefix",
      "scala.scalanative.runtime.vmoffset$.scalanative_get_vmoffset"
    )
  )

  override def projectSettings: Seq[Setting[_]] = Seq(
    nirExternIssueFilters := moduleFilters.getOrElse(name.value, Nil),
    nirExternReportIssues := Def.uncached {
      val log = streams.value.log
      val filters = nirExternIssueFilters.value
      val previous = mimaPreviousClassfiles.value
      val outputs = (Compile / products).value
      val projectName = name.value

      if (previous.nonEmpty) {
        val current =
          outputs.flatMap(p => NirExternCompat.collect(p.toPath)).toMap
        val problems = previous.toSeq.flatMap {
          case (module, file) =>
            val externs = NirExternCompat.collect(file.toPath)
            log.info(
              s"$projectName: checked ${externs.size} externs of ${module.revision}"
            )
            NirExternCompat
              .compare(externs, current)
              .filterNot { problem =>
                filters.exists(
                  NirExternCompat.isFiltered(problem.symbol, _)
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

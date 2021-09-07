import $ivy.`com.lihaoyi::ammonite-ops:2.3.8`, ammonite.ops._, mainargs._
import $ivy.`org.bitbucket.cowwoc:diff-match-patch:1.2`,
org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch

val ignoredFiles = {
  val scala = os.rel / "scala"

  Set[RelPath](
    scala / "package.scala",
    scala / "Enumeration.scala",
    scala / "Predef.scala",
    scala / "Symbol.scala"
  )
}

@main(doc = """Helper tool created for working with scalalib overrides / patches.
     Accepts one of following commands:
     - create   - Create diffs of Scala sources based on version in $overridesDir and fetched Scala sources
     - recreate - Create override files based on created diffs and fetched Scala sources
     - prune    - Remove override files having associated .patch file""")
def main(
    @arg(doc = "Command to run [create, recreate, prune]")
    cmd: Command,
    @arg(doc = "Scala version used for fetching sources")
    scalaVersion: String,
    @arg(
      doc =
        "Path to directory containing overrides, defaults to scalalib/overrides-$scalaBinaryVersion"
    )
    overridesDir: Option[os.Path] = None
) = {
  val Array(vMajor, vMinor, vPatch) = scalaVersion.split('.')

  implicit val wd: os.Path = pwd

  val sourcesDir = pwd / 'scalalib / 'target / 'scalaSources / scalaVersion
  val overridesDirPath = {
    overridesDir
      .orElse {
        val overridesDir = s"overrides"
        val scalaEpochDir = s"$overridesDir-$vMajor"
        val binaryVersionDir = s"$scalaEpochDir.$vMinor"
        val scalaVersionDir = s"$binaryVersionDir.$vPatch"

        List(scalaVersionDir, binaryVersionDir, scalaEpochDir, overridesDir)
          .map(pwd / 'scalalib / _)
          .find(exists(_))
      }
      .getOrElse(
        sys.error("Not found any existing default scalalib override dir")
      )
  }

  println(s"""
       |Attempting to $cmd with config:
       |Scala version: $scalaVersion
       |Overrides dir: $overridesDirPath
       |Sources dir:   $sourcesDir
       |Blacklisted: 
       | - ${ignoredFiles.mkString("\n - ")}
       |""".stripMargin)

  assert(exists ! overridesDirPath, "Overrides dir does not exists")

  cmd match {
    // Create patches based on fetched Scala sources and it's overrideds
    case CreatePatches =>
      sourcesExistsOrFetch(scalaVersion, sourcesDir)

      for {
        overridePath <- ls.rec ! overridesDirPath |? (_.ext == "scala")
        relativePath = overridePath relativeTo overridesDirPath
        if !ignoredFiles.contains(relativePath)
        sourcePath = sourcesDir / relativePath if exists ! sourcePath
        patchPath = overridePath / up / s"${overridePath.last}.patch"
        _ = if (exists ! patchPath) rm ! patchPath
      } {
        val diff = new DiffMatchPatch()
        diff.patchMargin = 80
        val diffs = diff.diffMain(read(sourcePath), read(overridePath), true)
        if (diffs.isEmpty) {
          System.err.println(
            s"File $relativePath has identical content as original source"
          )
        } else {
          diff.diffCleanupSemantic(diffs)
          val patch = diff.patchMake(diffs)
          write.over(patchPath, diff.patchToText(patch))
          println(s"Created patch for $relativePath")
        }
      }

    // Recreate overrides by re-applying `.scala.patch` files onto Scala sources
    case RecreateOverrides =>
      sourcesExistsOrFetch(scalaVersion, sourcesDir)

      for {
        patchPath <- ls.rec ! overridesDirPath |? (_.ext == "patch")
        overridePath = patchPath / up / patchPath.last.stripSuffix(".patch")
        relativePath = overridePath relativeTo overridesDirPath
        if !ignoredFiles.contains(relativePath)
        sourcePath = sourcesDir / relativePath

        _ = if (exists(overridePath)) rm ! overridePath

      } {
        val Array(patched: String, results: Array[Boolean]) = {
          type PatchList = java.util.LinkedList[DiffMatchPatch.Patch]
          val diff = new DiffMatchPatch()
          val patches = diff.patchFromText(read(patchPath))
          diff.patchApply(patches.asInstanceOf[PatchList], read(sourcePath))
        }
        if (results.forall(_ == true)) {
          println(s"Recreated $overridePath")
          write.over(overridePath, patched)
        } else {
          System.err.println(s"Cannot apply patch for $patchPath")
        }
      }

    // Walk overrides dir and remove all `.scala` sources which has defined `.scala.patch` sibling
    case PruneOverrides =>
      for {
        patchPath <- ls.rec ! overridesDirPath |? (_.ext == "patch")
        overridePath = patchPath / up / patchPath.last.stripSuffix(".patch")
        relativePath = overridePath relativeTo overridesDirPath

        shallPrune = exists(overridePath) &&
          !ignoredFiles.contains(relativePath)
      } {
        if (shallPrune) {
          rm ! overridePath
        }
      }
  }
}

sealed trait Command
case object CreatePatches extends Command
case object PruneOverrides extends Command
case object RecreateOverrides extends Command

implicit object CommandReader
    extends TokensReader[Command](
      "command",
      {
        case Seq("create")   => Right(CreatePatches)
        case Seq("prune")    => Right(PruneOverrides)
        case Seq("recreate") => Right(RecreateOverrides)
        case _               => Left("Expected one of create, prune, recreate")
      }
    )

def sourcesExistsOrFetch(scalaVersion: String, sourcesDir: os.Path)(implicit
    wd: os.Path
) = {
  if (!exists(sourcesDir)) {
    println(s"Fetching Scala $scalaVersion sources")
    %("sbt", s"++ $scalaVersion", "scalalib/fetchScalaSource")
  }
  assert(exists ! sourcesDir, s"Sources at $sourcesDir missing")
}

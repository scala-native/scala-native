//> using dep "io.github.java-diff-utils:java-diff-utils:4.12"
//> using dep "com.lihaoyi::os-lib:0.9.1"
//> using dep "com.lihaoyi::mainargs:0.4.0"

import com.github.difflib.{DiffUtils, UnifiedDiffUtils}
import os._
import mainargs._

import scala.util._

val ignoredFiles = {
  val scala = os.rel / "scala"

  Set[RelPath]()
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
    overridesDir: Option[String] = None
) = {
  val Array(vMajor, vMinor, vPatch) = scalaVersion.split('.')

  implicit val wd: os.Path = pwd

  val sourcesDir = pwd / "scalalib" / "target" / "scalaSources" / scalaVersion
  val overridesDirPath: os.Path =
    overridesDir.map(os.Path(_)).getOrElse {
      {
        val overridesDir = s"overrides"
        val scalaEpochDir = s"$overridesDir-$vMajor"
        val binaryVersionDir = s"$scalaEpochDir.$vMinor"
        val scalaVersionDir = s"$binaryVersionDir.$vPatch"

        List(scalaVersionDir, binaryVersionDir, scalaEpochDir, overridesDir)
          .map(pwd / "scalalib" / _)
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

  assert(os.exists(overridesDirPath), "Overrides dir does not exists")

  cmd match {
    // Create patches based on fetched Scala sources and it's overrideds
    case CreatePatches =>
      sourcesExistsOrFetch(scalaVersion, sourcesDir)

      for {
        overridePath <- os
          .walk(overridesDirPath)
          .filterNot(p => p.ext != "scala" || os.isDir(p))
        relativePath = overridePath relativeTo overridesDirPath
        if !ignoredFiles.contains(relativePath)
        sourcePath = sourcesDir / relativePath if os.exists(sourcePath)
        patchPath = overridePath / up / s"${overridePath.last}.patch"
        _ = if (os.exists(patchPath)) os.remove(patchPath)
      } {
        val originalLines = fileToLines(sourcePath)
        val diff = DiffUtils.diff(
          originalLines,
          fileToLines(overridePath),
          false
        )
        val contextLines = 3
        val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
          (sourcePath relativeTo sourcesDir / up).toString(),
          (overridePath relativeTo overridesDirPath / up).toString(),
          originalLines,
          diff,
          contextLines
        )

        if (unifiedDiff.isEmpty()) {
          System.err.println(
            s"File $relativePath has identical content as original source"
          )
        } else {
          write.over(
            patchPath,
            unifiedDiff
              .stream()
              .reduce(_ + System.lineSeparator() + _)
              .orElse("") + System.lineSeparator()
          )
          println(s"Created patch for $relativePath")
        }
      }

    // Recreate overrides by re-applying `.scala.patch` files onto Scala sources
    case RecreateOverrides =>
      sourcesExistsOrFetch(scalaVersion, sourcesDir)

      for {
        patchPath <- os
          .walk(overridesDirPath)
          .filterNot(p => p.ext != "patch" || os.isDir(p))
        overridePath = patchPath / up / patchPath.last.stripSuffix(".patch")
        relativePath = overridePath relativeTo overridesDirPath
        if !ignoredFiles.contains(relativePath)
        sourcePath = sourcesDir / relativePath

        _ = if (exists(overridePath)) os.remove(overridePath)

      } {
        // There is no JVM library working with diffs which can apply fuzzy
        // patches based on the context, we use build in git command instead.
        val sourceCopyPath = sourcePath / up / (sourcePath.baseName + ".copy")
        os.copy(
          sourcePath,
          sourceCopyPath,
          replaceExisting = true,
          copyAttributes = true
        )
        try {
          os.proc(
            "git",
            "apply",
            "--whitespace=fix",
            "--recount",
            patchPath
          ) call (cwd = sourcesDir)
          os.move(sourcePath, overridePath, replaceExisting = true)
          os.move(sourceCopyPath, sourcePath)
          println(s"Recreated $overridePath")
        } catch {
          case ex: Exception =>
            System.err.println(
              s"Cannot apply patch for $patchPath - ${ex.getMessage()}"
            )
        }
      }

    // Walk overrides dir and remove all `.scala` sources which has defined `.scala.patch` sibling
    case PruneOverrides =>
      for {
        patchPath <- os.walk(
          overridesDirPath,
          skip = _.ext != "patch",
          includeTarget = false
        )
        overridePath = patchPath / up / patchPath.last.stripSuffix(".patch")
        relativePath = overridePath relativeTo overridesDirPath

        shallPrune = exists(overridePath) &&
          !ignoredFiles.contains(relativePath)
      } {
        if (shallPrune) {
          os.remove(overridePath)
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

def fileToLines(path: os.Path) = {
  val list = new java.util.LinkedList[String]()
  read.lines(path).foreach(list.add(_))
  list
}

def sourcesExistsOrFetch(scalaVersion: String, sourcesDir: os.Path)(implicit
    wd: os.Path
) = {
  if (!exists(sourcesDir)) {
    println(s"Fetching Scala $scalaVersion sources")
    os.proc("sbt", s"++ $scalaVersion", "scalalib/fetchScalaSource").call()
  }
  assert(os.exists(sourcesDir), s"Sources at $sourcesDir missing")
}

ParserForMethods(this).runOrThrow(args, allowPositional = true)

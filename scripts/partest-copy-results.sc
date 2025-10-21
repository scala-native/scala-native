import java.io.File

import ammonite.ops._
import mainargs._

import $ivy.`com.lihaoyi::ammonite-ops:2.3.8`

val kinds = List("neg", "run", "pos")

@main(doc = """"
  "Tool used to gather results of failed partests.
  It copies log of failed tests and as well as expected output.
  Allows to create diff files based on expected result""")
def main(
    @arg(doc = "Scala version used for fetching sources")
    scalaVersion: String,
    createDiff: Boolean = true,
    outputDir: os.Path = pwd / "scala-partest-results"
) = {
  implicit val wd: os.Path = pwd

  val resultsDir = outputDir / scalaVersion

  val partestTestsDir = pwd / "scala-partest-tests" /
    RelPath("src/test/resources") /
    RelPath("scala/tools/partest/scalanative") / scalaVersion

  val partestSourcesDir =
    pwd / "scala-partest" / "fetchedSources" / scalaVersion
  val testFiles = partestSourcesDir / "test" / "files"

  def showRelPath(p: os.Path): String =
    s"${p.relativeTo(wd)} ${if (exists(p)) "" else "missing!!!"}"

  println(s"""
             |Scala version:       $scalaVersion
             |Results dir:         ${showRelPath(resultsDir)}
             |Test defintions dir: ${showRelPath(partestTestsDir)}
             |Partest sources dir: ${showRelPath(partestSourcesDir)}
             |Create diffs:        ${createDiff}
             |""".stripMargin)

  val failedNotDenylisted = collection.mutable.Set.empty[String]
  val failed = collection.mutable.Set.empty[String]
  val denylisted = read
    .lines(partestTestsDir / "DenylistedTests.txt")
    .filterNot(_.startsWith("#"))
    .filterNot(_.isEmpty())
    .map(_.stripSuffix(".scala"))
    .toSet

  for {
    kind <- kinds
    outputDir = resultsDir / kind
    _ =
      if (!(exists(outputDir))) mkdir(outputDir)
      else ls(outputDir).foreach(rm)
    logFile <- ls(testFiles / kind) if logFile.ext == "log"

    relPath = logFile.relativeTo(testFiles)
    name = relPath.toString.stripSuffix(s"-$kind.log")

    _ = cp.over(logFile, resultsDir / relPath)
    _ = {
      if (!denylisted.contains(name)) {
        failedNotDenylisted += name
      }
      println(s"${name} failed")
      failed += name
    }

    checkFile = RelPath(s"$name.check")
    expected <- Seq(
      partestTestsDir / checkFile,
      testFiles / checkFile
    ).filter(exists).headOption
    _ = cp.over(expected, outputDir / expected.last)

    diffCmd = sys.process.Process(
      command = Seq(
        "diff",
        "-u",
        (expected relativeTo wd).toString,
        (logFile relativeTo wd).toString
      ),
      cwd = new File(wd.toString)
    ) #> new File((resultsDir / RelPath(s"$name.diff")).toString) if createDiff

  } {
    diffCmd.run().exitValue() match {
      case 0 =>
        System.err.println(s"Log file is identical with check for $name")
      case 1 => ()
      case n =>
        System.err.println(s"Failed to create diff for $name")
    }
  }

  println()
  println(s"Failed tests: ${failed.size}")
  println(s"Failed not denylisted [${failedNotDenylisted.size}]: ")
  failedNotDenylisted.toList.sorted.foreach(println)

}

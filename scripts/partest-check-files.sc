import $ivy.`com.lihaoyi::ammonite-ops:2.3.8`, ammonite.ops._, mainargs._
import java.io.File

@main(
  doc = """" +
  "Tool used to check integrity of files defined in partest tests and 
  actually defined in partest repo. Allow to check which blacklisted files
  are not existing and can suggest correct blacklisted item name.
  Also checks for duplicates in blacklisted items.""")
def main(
    @arg(doc = "Scala version used for fetching sources")
    scalaVersion: String) = {
  implicit val wd: os.Path = pwd

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
             |Test defintions dir: ${showRelPath(partestTestsDir)}
             |Partest sources dir: ${showRelPath(partestSourcesDir)}
             |""".stripMargin)

  if (Seq(partestTestsDir, partestSourcesDir).forall(exists(_))) ()
  else {
    println("Abort: Some paths are missing!")
    sys.exit(1)
  }

  val testNames = collection.mutable.Set.empty[String]

  for {
    (blacklisted, line) <- (read.lines ! partestTestsDir / "BlacklistedTests.txt").zipWithIndex
    if blacklisted.nonEmpty && !blacklisted.startsWith("#")
    testName = {
      val lastDot = blacklisted.lastIndexOf(".")
      if (lastDot > 0) blacklisted.substring(0, lastDot)
      else blacklisted
    }
    _ = if (testNames.contains(testName)) {
      println(s"Duplicated blacklisted test $testName at line $line")
    } else {
      testNames += testName
    }
    source = testFiles / RelPath(blacklisted) if !exists(source)
    asDir  = testFiles / RelPath(testName)
    asFile = testFiles / RelPath(testName + ".scala")
  } {
    println {
      if (asDir != source && exists(asDir)) {
        s"Blacklisted $blacklisted should refer to directory ${asDir.relativeTo(testFiles)}"
      } else if (asFile != source && exists(asFile)) {
        s"Blacklisted $blacklisted should refer to file ${asFile.relativeTo(testFiles)}"
      } else {
        s"Blacklisted $blacklisted does not exist"
      }
    }
  }

  for {
    kindDir <- ls ! partestTestsDir if kindDir.isDir
    file    <- ls ! kindDir
    relativePath = file.relativeTo(partestTestsDir)
    if !exists(testFiles / relativePath)
  } {
    println(s"$relativePath does not exist in upstream")
  }

}

//> using scala "3"
//> using lib "com.lihaoyi::os-lib:0.9.1"

import java.io.File
import os._

val partestSourcesDirs = pwd / "scala-partest" / "fetchedSources"

/** Tool used to check integrity of files defined in partest tests and thoose
 *  actually defined in Scala (partest) repository. It allows to check which
 *  denylisted files are not existing and can suggest correct denylisted item
 *  name. Also checks for duplicates in denylisted items
 */
@main def checkAllFiles() =
  os
    .list(partestSourcesDirs)
    .ensuring(_.nonEmpty, "Not found any Scala sources directories")
    .map(_.last)
    .foreach(checkFiles)

def checkFiles(scalaVersion: String): Unit = {
  println(s"Checking $scalaVersion")
  val partestTestsDir = pwd / "scala-partest-tests" /
    RelPath("src/test/resources") /
    RelPath("scala/tools/partest/scalanative") / scalaVersion

  val partestSourcesDir = partestSourcesDirs / scalaVersion
  val testFiles = partestSourcesDir / "test" / "files"

  def showRelPath(p: os.Path): String =
    s"${p.relativeTo(pwd)} ${if exists(p) then "" else "missing!!!"}"

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
    (denylisted, line) <- read
      .lines(partestTestsDir / "DenylistedTests.txt")
      .zipWithIndex
    if denylisted.nonEmpty && !denylisted.startsWith("#")
    testName = {
      val lastDot = denylisted.lastIndexOf(".")
      if (lastDot > 0) denylisted.substring(0, lastDot)
      else denylisted
    }
    _ =
      if (testNames.contains(testName)) {
        println(s"Duplicated denylisted test $testName at line $line")
      } else {
        testNames += testName
      }
    source = testFiles / RelPath(denylisted) if !exists(source)
    asDir = testFiles / RelPath(testName)
    asFile = testFiles / RelPath(testName + ".scala")
  } {
    println {
      if (asDir != source && exists(asDir)) {
        s"Denylisted $denylisted should refer to directory ${asDir.relativeTo(testFiles)}"
      } else if (asFile != source && exists(asFile)) {
        s"Denylisted $denylisted should refer to file ${asFile.relativeTo(testFiles)}"
      } else {
        s"Denylisted $denylisted does not exist"
      }
    }
  }

  for {
    kindDir <- list(partestTestsDir) if isDir(kindDir)
    file <- list(kindDir)
    relativePath = file.relativeTo(partestTestsDir)
    if !exists(testFiles / relativePath)
  } {
    println(s"$relativePath does not exist in upstream")
  }

}

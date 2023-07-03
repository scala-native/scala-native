//> using scala "3"
//> using lib "com.lihaoyi::os-lib:0.9.1"

import java.io.File
import os._

val partestSourcesDirs = pwd / "scala-partest" / "fetchedSources"

/** Tool used to check integrity of files defined in partest tests and thoose
 *  actually defined in Scala (partest) repository. It allows to check which
 *  blacklisted files are not existing and can suggest correct blacklisted item
 *  name. Also checks for duplicates in blacklisted items
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
    (blacklisted, line) <- read
      .lines(partestTestsDir / "BlacklistedTests.txt")
      .zipWithIndex
    if blacklisted.nonEmpty && !blacklisted.startsWith("#")
    testName = {
      val lastDot = blacklisted.lastIndexOf(".")
      if (lastDot > 0) blacklisted.substring(0, lastDot)
      else blacklisted
    }
    _ =
      if (testNames.contains(testName)) {
        println(s"Duplicated blacklisted test $testName at line $line")
      } else {
        testNames += testName
      }
    source = testFiles / RelPath(blacklisted) if !exists(source)
    asDir = testFiles / RelPath(testName)
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
    kindDir <- list(partestTestsDir) if isDir(kindDir)
    file <- list(kindDir)
    relativePath = file.relativeTo(partestTestsDir)
    if !exists(testFiles / relativePath)
  } {
    println(s"$relativePath does not exist in upstream")
  }

}

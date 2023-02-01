#!/usr/bin/env -S scala-cli shebang
//> using lib "org.virtuslab::toolkit-alpha::0.1.13"
//> using scala "3.2"

// Combine with bash loop
// SJSPath="sjs-abs-path/unit-tests/shared/src/test/scala/org/scalanative"
// SNPath="sn-abs-path  /test-suite/shared/src/test/scala/org/scalajs"
// for file in $(ls ${SJSPath}/javalib/util/function/* ); do
//   ./scripts/portScalaJsSource.scala $SNPath $SJSPath $file;
// done

import scala.util.chaining.scalaUtilChainingOps

@main def portScalaJSSource(
    scalaNativeAbsPath: os.Path,
    scalaJSAbsPath: os.Path,
    fileAbsPath: os.Path
) =
  val relPath = fileAbsPath.relativeTo(scalaJSAbsPath)
  val sjsPath = scalaJSAbsPath / relPath
  val snPath = scalaNativeAbsPath / relPath

  def getShortSha(sjsFile: os.Path): String =
    val format = "// Ported from Scala.js, commit SHA: %h dated: %as"
    val out = os
      .proc("git", "log", "-n1", s"--pretty=format:${format}", sjsFile)
      .call(cwd = scalaJSAbsPath, check = true, stdout = os.Pipe)
    out.out.text()

  println(s"""
  |ScalaNative base dir: $scalaNativeAbsPath
  |ScalaJS     base dir: $scalaJSAbsPath
  |Rel path:             ${relPath}       
  |Porting ${sjsPath} into ${snPath}
  """.stripMargin)

  assert(
    os.exists(scalaNativeAbsPath),
    "Scala Native directory does not exist"
  )
  assert(
    os.exists(scalaNativeAbsPath),
    "Scala JS directory does not exists"
  )
  assert(os.exists(sjsPath), s"ScalaJS file does not exits ${sjsPath}")
  if os.exists(snPath)
  then println(s"ScalaNative file alread exists ${snPath}, skipping")
  else
    val revisionMessage = getShortSha(sjsPath)
    os.write(snPath, revisionMessage + "\n", createFolders = true)
    val sjsSource = os
      .read(sjsPath)
      .pipe(stripHeader)
    os.write.append(snPath, sjsSource)
    os.write.append(snPath, System.lineSeparator())

private def stripHeader(input: String) = {
  val nl = System.lineSeparator()
  val commentsCtrl = Seq("/*", "*/", "*", "//")
  input
    .split(nl)
    .dropWhile { line =>
      val trimmed = line.trim()
      trimmed.isEmpty || commentsCtrl.exists(trimmed.startsWith(_))
    }
    .mkString(nl)
}

import scala.util.CommandLineParser.FromString
private given FromString[os.Path] = { str =>
  val nio = java.nio.file.Paths.get(str)
  os.Path(nio.toRealPath())
}

private given FromString[os.RelPath] = { str =>
  val nio = java.nio.file.Paths.get(str)
  os.RelPath(nio)
}

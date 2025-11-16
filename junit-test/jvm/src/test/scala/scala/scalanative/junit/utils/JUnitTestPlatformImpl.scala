package scala.scalanative.junit.utils

// Ported from Scala.js

import sbt.testing._

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file._
import java.util.LinkedList

import scala.annotation.tailrec
import scala.concurrent.Future

object JUnitTestPlatformImpl {

  def getClassLoader: ClassLoader = getClass.getClassLoader

  @tailrec
  def executeLoop(
      tasks: Array[Task],
      recorder: Logger with EventHandler
  ): Future[Unit] = {
    if (tasks.nonEmpty) {
      executeLoop(tasks.flatMap(_.execute(recorder, Array(recorder))), recorder)
    } else {
      Future.successful(())
    }
  }

  def writeLines(lines: List[String], file: String): Unit = {
    val jLines = new LinkedList[String]()
    lines.foreach(jLines.add)
    Files.write(Paths.get(file), jLines, UTF_8)
  }

  def readLines(file: String): List[String] = {
    val builder = List.newBuilder[String]
    val it = Files.readAllLines(Paths.get(file), UTF_8).iterator()
    while (it.hasNext) {
      builder += it.next()
    }
    builder.result()
  }
}

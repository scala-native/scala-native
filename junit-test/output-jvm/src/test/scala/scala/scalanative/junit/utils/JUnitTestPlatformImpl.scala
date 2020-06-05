package scala.scalanative.junit.utils

// Ported from Scala.js

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file._

import sbt.testing._

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.concurrent.Future

object JUnitTestPlatformImpl {

  def getClassLoader: ClassLoader = getClass.getClassLoader

  @tailrec
  def executeLoop(tasks: Array[Task],
                  recorder: Logger with EventHandler): Future[Unit] = {
    if (tasks.nonEmpty) {
      executeLoop(tasks.flatMap(_.execute(recorder, Array(recorder))), recorder)
    } else {
      Future.successful(())
    }
  }

  def writeLines(lines: List[String], file: String): Unit =
    Files.write(Paths.get(file), lines.asJava, UTF_8)

  def readLines(file: String): List[String] =
    Files.readAllLines(Paths.get(file), UTF_8).asScala.toList
}

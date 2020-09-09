package scala.scalanative.junit.utils

// Ported from Scala.js

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8

import sbt.testing._

import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

object JUnitTestPlatformImpl {

  def getClassLoader: ClassLoader = null

  def executeLoop(tasks: Array[Task],
                  recorder: Logger with EventHandler): Future[Unit] = {
    if (tasks.isEmpty) {
      Future.successful(())
    } else {
      Future
        .traverse(tasks.toList)(executeTask(_, recorder))
        .flatMap(newTasks => executeLoop(newTasks.flatten.toArray, recorder))
    }
  }

  private def executeTask(
      task: Task,
      recorder: Logger with EventHandler): Future[Array[Task]] = {
    val p = Promise[Array[Task]]()
    p.success(task.execute(recorder, Array(recorder)))
    p.future
  }

  def writeLines(lines: List[String], file: String): Unit =
    Files.write(Paths.get(file), lines.asJava, UTF_8)

  def readLines(file: String): List[String] =
    Files.readAllLines(Paths.get(file), UTF_8).asScala.toList
}

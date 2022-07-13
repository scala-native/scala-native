package scala.scalanative.junit.utils

// Ported from Scala.js

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import java.util.LinkedList

import sbt.testing._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

object JUnitTestPlatformImpl {

  def getClassLoader: ClassLoader = null

  def executeLoop(
      tasks: Array[Task],
      recorder: Logger with EventHandler
  ): Future[Unit] = {
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
      recorder: Logger with EventHandler
  ): Future[Array[Task]] = {
    val p = Promise[Array[Task]]()
    task.execute(recorder, Array(recorder), p.success _)
    p.future
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

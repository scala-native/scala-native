package scala.scalanative
package sbtplugin
package testinterface

import sbt.testing.{EventHandler, Logger, Task, TaskDef}

import scala.scalanative.testinterface.serialization._
import Serializer._
import scala.annotation.tailrec

final case class ScalaNativeTask private (
    runner: ScalaNativeRunner,
    taskDef: TaskDef,
    tags: Array[String],
    taskId: Int
) extends Task {
  override def execute(handler: EventHandler,
                       loggers: Array[Logger]): Array[Task] = {

    val colorSupport = loggers.map(_.ansiCodesSupported).toSeq
    val command      = Command.Execute(taskId, colorSupport)

    runner.master.send(command: Command)

    @tailrec
    def receive(): Array[Task] =
      runner.master.receive[Either[Seq[(TaskInfo, Int)], Event]]() match {
        case Left(infos) =>
          infos.map(ScalaNativeTask.fromInfo(runner, _)).toArray
        case Right(ev) =>
          handler.handle(ev)
          receive()
      }

    receive()
  }
}

object ScalaNativeTask {
  private[testinterface] def fromInfo(
      runner: ScalaNativeRunner,
      infoAndId: (TaskInfo, Int)): ScalaNativeTask = {
    val (info, id) = infoAndId
    new ScalaNativeTask(runner,
                        info.taskDef,
                        info.tags.toArray,
                        id)
  }
}

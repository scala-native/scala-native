package scala.scalanative
package sbtplugin
package testinterface

import sbt.MessageOnlyException
import sbt.testing.{EventHandler, Logger, Task, TaskDef}

import scala.scalanative.testinterface.serialization.{
  Command,
  Event,
  TaskInfo,
  TaskInfos
}
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

    runner.send(command)

    @tailrec
    def receive(): Array[Task] =
      runner.receive match {
        case TaskInfos(infos) =>
          infos.map(ScalaNativeTask.fromInfo(runner, _)).toArray
        case ev: Event =>
          handler.handle(ev)
          receive()
        case other =>
          throw new MessageOnlyException(
            s"Unexpected message: ${other.getClass.getName}")
      }

    receive()
  }
}

object ScalaNativeTask {
  private[testinterface] def fromInfo(runner: ScalaNativeRunner,
                                      info: TaskInfo): Task = {
    new ScalaNativeTask(runner, info.taskDef, info.tags.toArray, info.id)
  }
}

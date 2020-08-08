package scala.scalanative.testinterface

// Ported from Scala.js

import sbt.testing._
import scala.scalanative.testinterface.common.{Serializer, TaskInfo}

private[testinterface] object TaskInfoBuilder {
  def detachTask(task: Task, runner: Runner): TaskInfo = {
    def optSerializer(t: TaskDef) =
      if (t == task.taskDef()) ""
      else Serializer.serialize(t)

    new TaskInfo(runner.serializeTask(task, optSerializer),
                 task.taskDef(),
                 task.tags().toList)
  }

  def attachTask(info: TaskInfo, runner: Runner): Task = {
    def optDeserializer(s: String) =
      if (s == "") info.taskDef
      else Serializer.deserialize[TaskDef](s)

    runner.deserializeTask(info.serializedTask, optDeserializer)
  }
}

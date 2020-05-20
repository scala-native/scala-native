package tests

import sbt.testing.{Runner, Task, TaskDef}

class NativeRunner(override val args: Array[String],
                   override val remoteArgs: Array[String])
    extends Runner {
  override def tasks(taskDefs: Array[TaskDef]): Array[Task] =
    taskDefs.map(taskDef => new NativeTask(taskDef))

  override def done(): String = ""
}

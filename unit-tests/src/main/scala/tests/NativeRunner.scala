package tests

import sbt.testing.{Runner, Task, TaskDef}
import scala.scalanative.testinterface.PreloadedClassLoader

class NativeRunner(override val args: Array[String],
                   override val remoteArgs: Array[String],
                   testClassLoader: PreloadedClassLoader)
    extends Runner {
  override def tasks(taskDefs: Array[TaskDef]): Array[Task] =
    taskDefs.map(new NativeTask(_, testClassLoader))

  override def done(): String = ""
}

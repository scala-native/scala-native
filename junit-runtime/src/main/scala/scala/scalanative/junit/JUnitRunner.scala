package scala.scalanative
package junit

// Ported from Scala.js

import sbt.testing._

private[junit] final class JUnitRunner(val args: Array[String],
                                       val remoteArgs: Array[String],
                                       runSettings: RunSettings)
    extends Runner {

  def tasks(taskDefs: Array[TaskDef]): Array[Task] =
    taskDefs.map(new JUnitTask(_, runSettings))

  def done(): String = ""

  def serializeTask(task: Task, serializer: TaskDef => String): String =
    serializer(task.taskDef())

  def deserializeTask(task: String, deserializer: String => TaskDef): Task =
    new JUnitTask(deserializer(task), runSettings)

  def receiveMessage(msg: String): Option[String] = None
}

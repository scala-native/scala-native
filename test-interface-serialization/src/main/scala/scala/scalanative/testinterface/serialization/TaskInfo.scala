package scala.scalanative
package testinterface
package serialization

import sbt.testing.TaskDef

final case class TaskInfo(id: Int, taskDef: TaskDef, tags: Seq[String])

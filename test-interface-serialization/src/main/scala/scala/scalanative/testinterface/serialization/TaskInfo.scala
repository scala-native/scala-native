package scala.scalanative
package testinterface
package serialization

import sbt.testing.{Task, TaskDef}

import Serializer.{serialize => s, deserialize => d, _}

final case class TaskInfo(taskDef: TaskDef,
                          tags: Seq[String])
object TaskInfo {
  implicit val TaskInfoSerializable: Serializable[TaskInfo] =
    new Serializable[TaskInfo] {
      override def name: String = "TaskInfo"
      override def serialize(v: TaskInfo): Iterator[String] =
        s("TaskInfo") ++ s(v.taskDef) ++ s(v.tags)
      override def deserialize(in: Iterator[String]): TaskInfo = {
        if (in.next() != "TaskInfo") throw new IllegalArgumentException()
        TaskInfo(d[TaskDef](in), d[Seq[String]](in))
      }
    }
}

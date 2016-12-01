package scala.scalanative
package test

import sbt.testing.{TaskDef}

class Runner(override val args: Array[String],
             override val remoteArgs: Array[String])
    extends sbt.testing.Runner {

  override def tasks(taskDefs: Array[TaskDef]): Array[sbt.testing.Task] = {
    taskDefs map (new Task(_))
  }

  override def done(): String = {
    "Done!"
  }

}

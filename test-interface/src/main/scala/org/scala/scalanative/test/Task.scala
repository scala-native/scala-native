package scala.scalanative
package test

import sbt.testing.{EventHandler, Logger, TaskDef}

import scala.sys.process.Process

class Task(override val taskDef: TaskDef) extends sbt.testing.Task {

  override def tags(): Array[String] = Array.empty

  override def execute(eventHandler: EventHandler,
                       loggers: Array[Logger]): Array[sbt.testing.Task] = {

    println(sys.props("scala.native.testbinary"))
    Process(
      Seq(sys.props("scala.native.testbinary"), taskDef.fullyQualifiedName)).!
    Array.empty
  }

}

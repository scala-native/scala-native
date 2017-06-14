package tests

import sbt.testing._

import scala.scalanative.testinterface.PreloadedClassLoader

class NativeTask(override val taskDef: TaskDef,
                 classLoader: PreloadedClassLoader)
    extends Task {
  override def execute(eventHandler: EventHandler,
                       loggers: Array[Logger]): Array[Task] = {
    val test = classLoader
      .loadPreloaded(taskDef.fullyQualifiedName())
      .asInstanceOf[Suite]
    test.run(eventHandler, loggers)
    Array.empty
  }

  override def tags(): Array[String] =
    Array.empty
}

package tests

import sbt.testing._

import scala.scalanative.reflect.Reflect

class NativeTask(override val taskDef: TaskDef) extends Task {
  override def execute(eventHandler: EventHandler,
                       loggers: Array[Logger]): Array[Task] = {
    // tests should always be objects
    val fqcn = taskDef.fullyQualifiedName() + "$"

    val test =
      Reflect
        .lookupLoadableModuleClass(fqcn)
        .getOrElse(throw new Exception(s"test object not found: $fqcn"))
        .loadModule()
        .asInstanceOf[Suite]

    test.run(eventHandler, loggers)
    Array.empty
  }

  override def tags(): Array[String] =
    Array.empty
}

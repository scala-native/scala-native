package scala.scalanative
package sbtplugin
package testinterface

import java.io.File

import sbt.Logger
import sbt.testing.{Runner, Task, TaskDef}

import scala.scalanative.testinterface.serialization._, Serializer._

class ScalaNativeRunner(val framework: ScalaNativeFramework,
                        bin: File,
                        logger: Logger,
                        val args: Array[String],
                        val remoteArgs: Array[String])
    extends Runner {

  var master: ComRunner = null

  createRemoteRunner()

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    ensureNotDone()
    master.send(Command.Tasks(taskDefs.toSeq): Command)

    val taskInfos = master.receive[Seq[(TaskInfo, Int)]]()
    taskInfos.map(ScalaNativeTask.fromInfo(this, _)).toArray
  }

  private[testinterface] def send[T: Serializable](msg: T): Unit = {
    ensureNotDone()
    master.send(msg)
  }

  private[testinterface] def receive[T: Serializable](): T = {
    ensureNotDone()
    master.receive[T]()
  }

  private def ensureNotDone(): Unit = {
    if (master == null)
      throw new IllegalStateException("Runner is already done")
  }

  private[this] def createRemoteRunner(): ComRunner = {
    master = new ComRunner(bin, Seq.empty, logger)
    master.send(Command.NewRunner(framework.id, args, remoteArgs): Command)
    master
  }

  override def done(): String = {
    master.send(Command.RunnerDone: Command)
    val summary = master.receive[Seq[String]]().mkString("\n")
    master.close()
    summary
  }
}

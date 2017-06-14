package scala.scalanative
package sbtplugin
package testinterface

import java.io.File

import sbt.Logger
import sbt.testing.{Runner, Task, TaskDef}

import scala.scalanative.testinterface.serialization.{
  Command,
  Message,
  TaskInfos
}

class ScalaNativeRunner(val framework: ScalaNativeFramework,
                        bin: File,
                        logger: Logger,
                        envVars: Map[String, String],
                        val args: Array[String],
                        val remoteArgs: Array[String])
    extends Runner {

  private var master: ComRunner = null

  createRemoteRunner()

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    ensureNotDone()
    val command = Command.Tasks(taskDefs.toSeq)
    send(command)

    val TaskInfos(infos) = receive()
    infos.map(ScalaNativeTask.fromInfo(this, _)).toArray
  }

  private[testinterface] def send(msg: Message): Unit =
    master.send(msg)

  private[testinterface] def receive(): Message =
    master.receive()

  private def ensureNotDone(): Unit = {
    if (master == null)
      throw new IllegalStateException("Runner is already done")
  }

  private[this] def createRemoteRunner(): ComRunner = {
    master = new ComRunner(bin, envVars, Seq.empty, logger)
    val command = Command.NewRunner(framework.id, args, remoteArgs)
    send(command)
    master
  }

  override def done(): String = {
    send(Command.RunnerDone(""))
    val Command.RunnerDone(summary) = receive()
    master.close()
    summary
  }
}

package scala.scalanative
package testinterface

import java.io.File

import sbt.testing.{Runner, Task, TaskDef}

import scalanative.build.Logger
import scalanative.testinterface.serialization.{Command, Message, TaskInfos}

class ScalaNativeRunner(val framework: ScalaNativeFramework,
                        bin: File,
                        logger: Logger,
                        envVars: Map[String, String],
                        val args: Array[String],
                        val remoteArgs: Array[String])
    extends Runner {
  private val comRunner: ComRunner =
    new ComRunner(bin,
                  envVars,
                  Seq(framework.framework.getClass.getName),
                  logger)

  this.send(Command.NewRunner(framework.id, args, remoteArgs))

  private[testinterface] def send(msg: Message): Unit = comRunner.send(msg)

  private[testinterface] def receive(): Message = comRunner.receive()

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    send(Command.Tasks(taskDefs.toSeq))

    val TaskInfos(infos) = receive()
    infos.map(ScalaNativeTask.fromInfo(this, _)).toArray
  }

  // BEWARE: If allowed to get that far, ScalaNativeRunner#done is
  // called unconditionally, success or Exception, by:
  // https://github.com/sbt/sbt/blob/develop/main/src/main/scala/sbt/\
  //     Defaults.scala#L1297

  override def done(): String = {
    send(Command.RunnerDone(""))
    val Command.RunnerDone(summary) = receive()
    comRunner.close()
    summary
  }
}

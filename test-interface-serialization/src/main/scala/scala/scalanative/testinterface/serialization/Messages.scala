package scala.scalanative
package testinterface
package serialization

import scala.compat.Platform.EOL

import sbt.testing.{Event => SbtEvent, _}

sealed trait Message
final case class Event(fullyQualifiedName: String,
                       fingerprint: Fingerprint,
                       selector: Selector,
                       status: Status,
                       throwable: OptionalThrowable,
                       duration: Long)
    extends Message
    with SbtEvent
final case class Log(index: Int,
                     message: String,
                     throwable: Option[Throwable],
                     level: Log.Level)
    extends Message
object Log {
  sealed trait Level
  object Level {
    case object Error extends Level
    case object Warn  extends Level
    case object Info  extends Level
    case object Debug extends Level
    case object Trace extends Level
  }
}
final case class Failure(throwable: Throwable)   extends Message
final case class TaskInfos(infos: Seq[TaskInfo]) extends Message
sealed trait Command                             extends Message
object Command {
  case class SendInfo(fid: Int, frameworkInfo: Option[FrameworkInfo])
      extends Command
  case class NewRunner(fid: Int, args: Seq[String], remoteArgs: Seq[String])
      extends Command
  case class RunnerDone(msg: String)       extends Command
  case class Tasks(taskDefs: Seq[TaskDef]) extends Command
  case class Execute(taskID: Int, loggerColorSupport: Seq[Boolean])
      extends Command

}

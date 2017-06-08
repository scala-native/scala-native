package scala.scalanative
package testinterface
package serialization

import scala.compat.Platform.EOL

import sbt.testing.{Event => SbtEvent, _}

import Serializer.{serialize => s, deserialize => d, _}

sealed trait Message
object Message {
  implicit val MessageSerializable: Serializable[Message] =
    new Serializable[Message] {
      override def name: String = "Message"
      override def serialize(v: Message) = v match {
        case cmd: Command =>
          s("Command") ++ s(cmd)
        case fail: Failure =>
          s("Failure") ++ s(fail.throwable)
        case log: Log =>
          s("Log") ++ s(log)
        case event: Event =>
          s("Event") ++ s(event)
      }
      override def deserialize(in: Iterator[String]): Message =
        in.next() match {
          case "Command" => d[Command](in)
          case "Failure" => Failure(d[Throwable](in))
          case "Log"     => d[Log](in)
          case "Event"   => d[Event](in)
        }
    }
}

final case class Event(fullyQualifiedName: String,
                       fingerprint: Fingerprint,
                       selector: Selector,
                       status: Status,
                       throwable: OptionalThrowable,
                       duration: Long)
    extends Message
    with SbtEvent
object Event {
  implicit val EventSerializable: Serializable[Event] =
    new Serializable[Event] {
      override def name: String = "Event"
      override def serialize(v: Event): Iterator[String] =
        s(v.fullyQualifiedName) ++ s(v.fingerprint) ++ s(v.selector) ++ s(
          v.status.toString) ++ s(
          if (v.throwable.isDefined) Seq(v.throwable.get) else Seq.empty) ++ s(
          v.duration)
      override def deserialize(in: Iterator[String]): Event = {
        val fullyQualifiedName = d[String](in)
        val fingerprint        = d[Fingerprint](in)
        val selector           = d[Selector](in)
        val status             = Status.valueOf(d[String](in))
        val throwable = d[Option[Throwable]](in)
          .map(t => new OptionalThrowable(t))
          .getOrElse(new OptionalThrowable())
        val duration = d[Long](in)
        Event(fullyQualifiedName,
              fingerprint,
              selector,
              status,
              throwable,
              duration)
      }
    }
}

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
    implicit val LevelSerializable: Serializable[Level] =
      new Serializable[Level] {
        override def name: String = "Level"
        override def serialize(v: Level): Iterator[String] =
          Iterator(v.toString)
        override def deserialize(in: Iterator[String]): Level =
          in.next() match {
            case "Error" => Error
            case "Warn"  => Warn
            case "Info"  => Info
            case "Debug" => Debug
            case "Trace" => Trace
          }
      }
  }
  implicit val LogSerializable: Serializable[Log] = new Serializable[Log] {
    override def name: String = "Log"
    override def serialize(v: Log): Iterator[String] =
      s(v.index) ++ s(v.message.lines) ++ s(v.throwable) ++ s(v.level)
    override def deserialize(in: Iterator[String]): Log = {
      val index     = d[Int](in)
      val message   = d[Iterator[String]](in).mkString(EOL)
      val throwable = d[Option[Throwable]](in)
      val level     = d[Level](in)
      Log(index, message, throwable, level)
    }

  }
}
final case class Failure(throwable: Throwable) extends Message
sealed trait Command                           extends Message
object Command {
  implicit val CommandSerializable: Serializable[Command] =
    new Serializable[Command] {
      override def name: String = "Command"
      override def serialize(v: Command): Iterator[String] = v match {
        case SendInfo(fid, frameworkInfo) =>
          s("SendInfo") ++ s(fid) ++ s(frameworkInfo)
        case NewRunner(fid, args, remoteArgs) =>
          s("NewRunner") ++ s(fid) ++ s(args) ++ s(remoteArgs)
        case RunnerDone =>
          s("RunnerDone")
        case Tasks(taskDefs) =>
          s("Tasks") ++ s(taskDefs)
        case Execute(taskID, loggerColorSupport) =>
          s("Execute") ++ s(taskID) ++ s(loggerColorSupport)
      }
      override def deserialize(in: Iterator[String]): Command =
        in.next() match {
          case "SendInfo" =>
            SendInfo(d[Int](in), d[Option[FrameworkInfo]](in))
          case "NewRunner" =>
            NewRunner(d[Int](in), d[Seq[String]](in), d[Seq[String]](in))
          case "RunnerDone" =>
            RunnerDone
          case "Tasks" =>
            Tasks(d[Seq[TaskDef]](in))
          case "Execute" =>
            Execute(d[Int](in), d[Seq[Boolean]](in))
        }
    }

  case class SendInfo(fid: Int, frameworkInfo: Option[FrameworkInfo])
      extends Command
  case class NewRunner(fid: Int, args: Seq[String], remoteArgs: Seq[String])
      extends Command
  case object RunnerDone                   extends Command
  case class Tasks(taskDefs: Seq[TaskDef]) extends Command
  case class Execute(taskID: Int, loggerColorSupport: Seq[Boolean])
      extends Command

}

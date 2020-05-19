package scala.scalanative
package testinterface
package serialization

import java.io.{
  ByteArrayInputStream,
  ByteArrayOutputStream,
  DataInputStream,
  InputStream
}

import sbt.testing._

import scala.scalanative.testinterface.serialization.Command.{
  Execute,
  NewRunner,
  RunnerDone,
  SendInfo,
  Tasks
}
import scala.scalanative.testinterface.serialization.Log.Level

object SerializedInputStream {
  def next[T](in: DataInputStream)(fn: SerializedInputStream => T) = {
    val length = in.readInt()
    val bytes  = new Array[Byte](length)
    var read   = 0
    while (read < length) {
      read += in.read(bytes, read, length - read)
    }
    fn(new SerializedInputStream(new ByteArrayInputStream(bytes)))
  }
}

class SerializedInputStream(in: InputStream) extends DataInputStream(in) {

  val T = Tags

  def readSeq[T](readT: SerializedInputStream => T): Seq[T] =
    Seq.fill(readInt())(readT(this))

  def readString(): String = {
    val length = readInt()
    val buf    = new Array[Byte](length)
    read(buf)
    new String(buf, "UTF-8")
  }

  def readOption[T](readT: SerializedInputStream => T): Option[T] =
    readSeq(readT).headOption

  def readFingerprint(): Fingerprint = readInt() match {
    case T.AnnotatedFingerprint =>
      DeserializedAnnotatedFingerprint(readBoolean(), readString())
    case T.SubclassFingerprint =>
      DeserializedSubclassFingerprint(readBoolean(),
                                      readString(),
                                      readBoolean())
  }

  def readSelector(): Selector = readInt() match {
    case T.SuiteSelector =>
      new SuiteSelector()
    case T.TestSelector =>
      new TestSelector(readString())
    case T.NestedSuiteSelector =>
      new NestedSuiteSelector(readString())
    case T.NestedTestSelector =>
      new NestedTestSelector(readString(), readString())
    case T.TestWildcardSelector =>
      new TestWildcardSelector(readString())
  }

  def readTaskDef(): TaskDef =
    new TaskDef(readString(),
                readFingerprint(),
                readBoolean(),
                readSeq(_.readSelector).toArray)

  def readStackTraceElement(): StackTraceElement =
    new StackTraceElement(readString(),
                          readString(),
                          readOption(_.readString()).orNull,
                          readInt())

  def readThrowable(): Throwable = {
    val ex = new RemoteException(readString(),
                                 readString(),
                                 readOption(_.readThrowable).orNull,
                                 readString())
    ex.setStackTrace(readSeq(_.readStackTraceElement).toArray)
    ex
  }

  def readFrameworkInfo(): FrameworkInfo =
    FrameworkInfo(readString(), readSeq(_.readFingerprint))

  def readCommand(): Command = readInt() match {
    case T.SendInfo =>
      SendInfo(readInt(), readOption(_.readFrameworkInfo))
    case T.NewRunner =>
      NewRunner(readInt(), readSeq(_.readString), readSeq(_.readString))
    case T.RunnerDone =>
      RunnerDone(readString())
    case T.Tasks =>
      Tasks(readSeq(_.readTaskDef))
    case T.Execute =>
      Execute(readInt(), readSeq(_.readBoolean))
  }

  def readLevel(): Level = readInt() match {
    case T.Error => Level.Error
    case T.Warn  => Level.Warn
    case T.Info  => Level.Info
    case T.Debug => Level.Debug
    case T.Trace => Level.Trace
  }

  def readLog(): Log = {
    Log(readInt(), readString(), readOption(_.readThrowable), readLevel())
  }

  def readStatus(): Status =
    Status.valueOf(readString())

  def readEvent(): Event =
    Event(
      readString(),
      readFingerprint(),
      readSelector(),
      readStatus(),
      readOption(_.readThrowable)
        .fold(new OptionalThrowable())(new OptionalThrowable(_)),
      readLong()
    )

  def readMessage(): Message = readInt() match {
    case T.Command =>
      readCommand()
    case T.Failure =>
      Failure(readThrowable())
    case T.Log =>
      readLog()
    case T.Event =>
      readEvent()
    case T.TaskInfos =>
      readTaskInfos()
  }

  def readTaskInfo(): TaskInfo =
    TaskInfo(readInt(), readTaskDef(), readSeq(_.readString))

  def readTaskInfos(): TaskInfos =
    TaskInfos(readSeq(_.readTaskInfo()))
}

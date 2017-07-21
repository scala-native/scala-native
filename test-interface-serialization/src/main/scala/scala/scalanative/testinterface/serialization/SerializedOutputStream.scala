package scala.scalanative
package testinterface
package serialization

import java.io.{ByteArrayOutputStream, DataOutputStream, OutputStream}

import sbt.testing._

import scala.scalanative.testinterface.serialization.Log.Level

object SerializedOutputStream {
  def apply(out: DataOutputStream)(fn: SerializedOutputStream => Unit): Unit = {
    val bos = new ByteArrayOutputStream()
    fn(new SerializedOutputStream(bos))
    val bytes = bos.toByteArray()

    out.writeInt(bytes.length)
    out.write(bytes)
    out.flush()
  }
}

class SerializedOutputStream private (out: OutputStream)
    extends DataOutputStream(out) {

  val T = Tags

  def writeSeq[T](seq: Seq[T])(writeT: T => Unit): Unit = {
    writeInt(seq.length)
    seq.foreach(writeT)
  }

  def writeOption[T](opt: Option[T])(writeT: T => Unit): Unit =
    writeSeq(opt.toSeq)(writeT)

  def writeString(v: String): Unit = {
    val bytes = v.getBytes("UTF-8")
    writeInt(bytes.length)
    write(bytes)
  }

  def writeFingerprint(fingerprint: Fingerprint): Unit = fingerprint match {
    case af: AnnotatedFingerprint =>
      writeInt(T.AnnotatedFingerprint)
      writeBoolean(af.isModule)
      writeString(af.annotationName())
    case sf: SubclassFingerprint =>
      writeInt(T.SubclassFingerprint)
      writeBoolean(sf.isModule)
      writeString(sf.superclassName())
      writeBoolean(sf.requireNoArgConstructor())
  }

  def writeSelector(selector: Selector): Unit = selector match {
    case _: SuiteSelector =>
      writeInt(T.SuiteSelector)
    case ts: TestSelector =>
      writeInt(T.TestSelector)
      writeString(ts.testName())
    case nss: NestedSuiteSelector =>
      writeInt(T.NestedSuiteSelector)
      writeString(nss.suiteId())
    case nts: NestedTestSelector =>
      writeInt(T.NestedTestSelector)
      writeString(nts.suiteId())
      writeString(nts.testName())
    case tws: TestWildcardSelector =>
      writeInt(T.TestWildcardSelector)
      writeString(tws.testWildcard())
  }

  def writeTaskDef(v: TaskDef): Unit = {
    writeString(v.fullyQualifiedName())
    writeFingerprint(v.fingerprint())
    writeBoolean(v.explicitlySpecified())
    writeSeq(v.selectors().toSeq)(writeSelector)
  }

  def writeStackTraceElement(v: StackTraceElement): Unit = {
    writeString(v.getClassName)
    writeString(v.getMethodName)
    writeOption(Option(v.getFileName))(writeString)
    writeInt(v.getLineNumber)
  }

  def writeThrowable(v: Throwable): Unit = {
    writeString(v.getMessage)
    writeString(v.toString)
    writeOption(Option(v.getCause))(writeThrowable)
    writeString(v.getClass.getName)
    writeSeq(v.getStackTrace.toSeq)(writeStackTraceElement)
  }

  def writeFrameworkInfo(frameworkInfo: FrameworkInfo): Unit = {
    writeString(frameworkInfo.name)
    writeSeq(frameworkInfo.fingerprints)(writeFingerprint)
  }

  def writeCommand(command: Command): Unit = command match {
    case Command.SendInfo(fid, frameworkInfo) =>
      writeInt(T.SendInfo)
      writeInt(fid)
      writeOption(frameworkInfo)(writeFrameworkInfo)
    case Command.NewRunner(fid, args, remoteArgs) =>
      writeInt(T.NewRunner)
      writeInt(fid)
      writeSeq(args)(writeString)
      writeSeq(remoteArgs)(writeString)
    case Command.RunnerDone(msg) =>
      writeInt(T.RunnerDone)
      writeString(msg)
    case Command.Tasks(taskDefs) =>
      writeInt(T.Tasks)
      writeSeq(taskDefs)(writeTaskDef)
    case Command.Execute(taskID, loggerColorSupport) =>
      writeInt(T.Execute)
      writeInt(taskID)
      writeSeq(loggerColorSupport)(writeBoolean)
  }

  def writeLevel(level: Level): Unit = {
    val tag =
      level match {
        case Level.Error => T.Error
        case Level.Warn  => T.Warn
        case Level.Info  => T.Info
        case Level.Debug => T.Debug
        case Level.Trace => T.Trace
      }
    writeInt(tag)
  }

  def writeLog(log: Log): Unit = {
    writeInt(log.index)
    writeString(log.message)
    writeOption(log.throwable)(writeThrowable)
    writeLevel(log.level)
  }

  def writeEvent(event: Event): Unit = {
    val throwable =
      if (event.throwable.isDefined) Some(event.throwable.get()) else None
    writeString(event.fullyQualifiedName)
    writeFingerprint(event.fingerprint)
    writeSelector(event.selector)
    writeString(event.status.toString)
    writeOption(throwable)(writeThrowable)
    writeLong(event.duration)
  }

  def writeMessage(v: Message): Unit = v match {
    case cmd: Command =>
      writeInt(T.Command)
      writeCommand(cmd)
    case fail: Failure =>
      writeInt(T.Failure)
      writeThrowable(fail.throwable)
    case log: Log =>
      writeInt(T.Log)
      writeLog(log)
    case event: Event =>
      writeInt(T.Event)
      writeEvent(event)
    case infos: TaskInfos =>
      writeInt(T.TaskInfos)
      writeTaskInfos(infos)
  }

  def writeTaskInfo(v: TaskInfo): Unit = {
    writeInt(v.id)
    writeTaskDef(v.taskDef)
    writeSeq(v.tags)(writeString)
  }

  def writeTaskInfos(v: TaskInfos): Unit =
    writeSeq(v.infos)(writeTaskInfo _)

}

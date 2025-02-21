package java.lang

import java.time.{Duration, Instant}

import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

trait ProcessHandle {

  def children(): Stream[ProcessHandle]

  def compareTo(other: ProcessHandle): scala.Int

  def descendants(): Stream[ProcessHandle]

  def destroy(): scala.Boolean

  def destroyForcibly(): scala.Boolean

  override def equals(other: Any): scala.Boolean

  override def hashCode(): scala.Int

  def info(): ProcessHandle.Info =
    throw new UnsupportedOperationException("ProcessHandle.info()")

  def isAlive(): scala.Boolean

  def onExit(): CompletableFuture[ProcessHandle]

  def parent(): Optional[ProcessHandle]

  def pid(): scala.Long =
    throw new UnsupportedOperationException("ProcessHandle.pid()")

  def supportsNormalTermination(): scala.Boolean
}

object ProcessHandle {

  trait Info {
    def arguments(): Optional[String]

    def command(): Optional[String]

    def commandLine(): Optional[String]

    def startInstant(): Optional[Instant]

    def totalCpuDuration(): Optional[Duration]

    def user(): Optional[String]

  }

  def allProcesses(): Stream[ProcessHandle] =
    throw new UnsupportedOperationException("ProcessHandle.allProcesses()")

  def current(): Stream[ProcessHandle] =
    throw new UnsupportedOperationException("ProcessHandle.current()")

  def of(pid: scala.Long): Optional[ProcessHandle] =
    throw new UnsupportedOperationException("ProcessHandle.of()")
}

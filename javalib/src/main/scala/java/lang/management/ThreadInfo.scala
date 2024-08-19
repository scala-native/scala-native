package java.lang.management

import scala.scalanative.runtime.NativeThread

/** A snapshot of a thread.
 */
trait ThreadInfo {

  /** The id of the thread.
   */
  def getThreadId(): Long

  /** The name of the thread.
   */
  def getThreadName(): String

  /** The state of the thread.
   */
  def getThreadState(): Thread.State

  /** Whether the thread is daemon.
   */
  def isDaemon(): Boolean

  /** The priority of the thread.
   */
  def getPriority(): Int

}

object ThreadInfo {

  /** Creates a thread info using the given native thread.
   *
   *  @note
   *    the stacktrace will be empty if the `maxDepth` is `0`.
   *
   *  @param nativeThread
   *    the thread to create an info from
   */
  @annotation.nowarn // Thread.getId is deprecated since JDK 19
  private[management] def apply(nativeThread: NativeThread): ThreadInfo = {
    val thread = nativeThread.thread

    new Impl(
      thread.getId(),
      thread.getName(),
      thread.getState(),
      thread.isDaemon(),
      thread.getPriority()
    )
  }

  private final class Impl(
      threadId: Long,
      threadName: String,
      threadState: Thread.State,
      daemon: Boolean,
      priority: Int
  ) extends ThreadInfo {

    def getThreadId(): Long = threadId
    def getThreadName(): String = threadName
    def getThreadState(): Thread.State = threadState
    def isDaemon(): Boolean = daemon
    def getPriority(): Int = priority

    override def toString(): String = {
      val daemon = if (isDaemon()) " daemon" else ""
      s""""$threadName"$daemon prio=$priority Id=$threadId $threadState"""
    }
  }

}

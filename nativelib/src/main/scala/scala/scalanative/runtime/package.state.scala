package scala.scalanative.runtime

import java.util.concurrent.locks.LockSupport
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

// Extracted fields from runtime package to ensure it does not require initialization
private[scalanative] object MainThreadShutdownContext {
  @volatile var shutdownThread: Thread = _
  var gracefully: Boolean = true

  def inShutdown: Boolean = shutdownThread != null

  /* Notify that thread has  */
  def onThreadFinished(thread: Thread): Unit = if (!thread.isDaemon()) signal()
  def onTaskEnqueued(): Unit = signal()

  private def signal() =
    if (isMultithreadingEnabled)
      if (inShutdown)
        LockSupport.unpark(shutdownThread)
}

private object ExecInfo {
  var filename: String = null
  var startTime: Long = 0L
}

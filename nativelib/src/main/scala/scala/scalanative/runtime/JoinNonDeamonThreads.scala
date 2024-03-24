package scala.scalanative.runtime

import NativeThread.Registry
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

private[runtime] object JoinNonDaemonThreads {
  private var shouldWait = true
  def skip(): Unit = shouldWait = false

  def run(): Unit = if (isMultithreadingEnabled) if (shouldWait) {
    def pollNonDaemonThreads = Registry.aliveThreads.iterator
      .map(_.thread)
      .filter { thread =>
        thread != Thread.currentThread() && !thread.isDaemon() &&
        thread.isAlive()
      }

    Registry.onMainThreadTermination()
    Iterator
      .continually(pollNonDaemonThreads)
      .takeWhile(_.hasNext && shouldWait)
      .flatten
      .foreach(_.join())
  }
}

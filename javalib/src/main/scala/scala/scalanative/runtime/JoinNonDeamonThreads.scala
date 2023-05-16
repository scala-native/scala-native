package scala.scalanative.runtime

import NativeThread.Registry

object JoinNonDaemonThreads {
  def registerExitHook(): Unit = Shutdown.addHook { () =>
    def pollDaemonThreads = Registry.aliveThreads.iterator
      .map(_.thread)
      .filter { thread =>
        !thread.isDaemon() && thread.isAlive()
      }

    Registry.onMainThreadTermination()
    Iterator
      .continually(pollDaemonThreads)
      .takeWhile(_.hasNext)
      .flatten
      .foreach(_.join())
  }
}

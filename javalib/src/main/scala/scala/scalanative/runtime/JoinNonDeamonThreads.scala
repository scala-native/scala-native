package scala.scalanative.runtime

import NativeThread.Registry
import Thread.MainThread

object JoinNonDeamonThreads {
  def registerExitHook(): Unit = Shutdown.addHook { () =>
    def pollDeamonThreads = Registry.aliveThreads.iterator
      .map(_.thread)
      .filter { thread =>
        !thread.isDaemon() && thread.isAlive()
      }

    Registry.onMainThreadTermination()
    Iterator
      .continually(pollDeamonThreads)
      .takeWhile(_.hasNext)
      .flatten
      .foreach(_.join())
  }
}

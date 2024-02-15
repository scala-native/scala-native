package scala.scalanative.runtime

import NativeThread.Registry

object JoinNonDaemonThreads {
  lazy val registerExitHook =
    try
      Runtime.getRuntime().addShutdownHook {
        val t = new Thread(() => {
          def pollDaemonThreads = Registry.aliveThreads.iterator
            .map(_.thread)
            .filter { thread =>
              thread != Thread.currentThread() && !thread.isDaemon() && thread
                .isAlive()
            }

          Registry.onMainThreadTermination()
          Iterator
            .continually(pollDaemonThreads)
            .takeWhile(_.hasNext)
            .flatten
            .foreach(_.join())
        })
        t.setPriority(Thread.MIN_PRIORITY)
        t.setName("shutdown-hook:join-non-deamon-threads")
        t
      }
    catch { case ex: IllegalStateException => () } // shutdown started
}

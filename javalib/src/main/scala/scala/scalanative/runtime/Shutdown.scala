package scala.scalanative.runtime

import scala.collection.mutable
import scala.scalanative.libc.stdlib.atexit
import scala.scalanative.unsafe._

private[runtime] object Shutdown {
  private val hooks: mutable.ArrayBuffer[() => Unit] = mutable.ArrayBuffer.empty
  def addHook(task: () => Unit): Unit = hooks.synchronized(hooks += task)
  private def runHooks(): Unit =
    hooks.foreach { task =>
      try {
        task()
      } catch {
        case e: Exception => // Maybe add a system property that adds logging of exceptions?
      }
    }
  atexit(() => runHooks())
}

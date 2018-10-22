package scala.scalanative.runtime

import scala.collection.mutable
import scala.scalanative.native._

private[runtime] object Shutdown {
  private val hooks: mutable.ArrayBuffer[() => Unit] = mutable.ArrayBuffer.empty
  def addHook(task: () => Unit)                      = hooks.synchronized(hooks += task)
  private def runHooks(): Unit =
    hooks.foreach { task =>
      try {
        task()
      } catch {
        case e: Exception => // Maybe add a system propery that adds logging of exceptions?
      }
    }
  NativeShutdown.init(CFunctionPtr.fromFunction0(() => runHooks()))
}

@extern
private[runtime] object NativeShutdown {
  @name("scalanative_native_shutdown_init")
  def init(func: CFunctionPtr0[Unit]): Unit = extern
}

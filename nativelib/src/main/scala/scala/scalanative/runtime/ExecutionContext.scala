package scala.scalanative
package runtime

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import scala.concurrent.ExecutionContextExecutor

object ExecutionContext {
  def global: ExecutionContextExecutor = new QueueExecutionContext()

  class Queue {
    private val ref: AtomicReference[List[Runnable]] = new AtomicReference(Nil)
    def enqueue(runnable: Runnable): Unit = {
      var oldValue: List[Runnable] = Nil
      var newValue: List[Runnable] = Nil
      do {
        oldValue = ref.get()
        newValue = oldValue :+ runnable
      } while (!ref.compareAndSet(oldValue, newValue))
    }

    /**
     * @return null if empty
     */
    def dequeue(): Runnable = {
      var item: Runnable           = null
      var oldValue: List[Runnable] = Nil
      var newValue: List[Runnable] = Nil
      do {
        oldValue = ref.get()
        if (!oldValue.isEmpty) {
          newValue = oldValue.tail
          item = oldValue.head
        } else {
          item = null
        }
      } while (!oldValue.isEmpty && !ref.compareAndSet(oldValue, newValue))
      item
    }

    def isEmpty: scala.Boolean = ref.get.isEmpty
  }

  class QueueExecutionContext(
      val numExecutors: Int = Runtime.getRuntime.availableProcessors())
      extends ExecutionContextExecutor {
    private val queue: Queue     = new Queue
    private val started          = new AtomicBoolean(false)
    private val parking: Object  = new Object
    private val doneLock: Object = new Object
    private val threadGroup      = new ThreadGroup("Executor Thread Group")
    threadGroup.setDaemon(true)
    private lazy val threads =
      scala.collection.immutable.Vector.tabulate(numExecutors) { i =>
        new ExecutorThread(i)
      }

    def execute(runnable: Runnable): Unit = {
      queue enqueue runnable
      // for better performance checking with .get() first
      if (!started.get()) {
        if (started.compareAndSet(false, true)) {
          start()
        }
      }
      parking.synchronized {
        parking.notifyAll()
      }
    }
    def reportFailure(t: Throwable): Unit = t.printStackTrace()

    private class ExecutorThread(id: scala.Int)
        extends Thread(threadGroup, "Executor-" + id) {
      var waiting       = false
      def idle: Boolean = !started.get() || waiting
      override def run(): Unit = {
        while (true) {
          val runnable = queue.dequeue()
          if (runnable != null) {
            waiting = false
            try {
              runnable.run()
            } catch {
              case t: Throwable =>
                QueueExecutionContext.this.reportFailure(t)
            }
          } else {
            doneLock.synchronized {
              waiting = true
              doneLock.notifyAll()
            }
            parking.synchronized {
              if (queue.isEmpty) {
                parking.wait()
              }
            }
          }

        }
      }
    }

    private def start(): Unit = {
      threads.foreach { thread: ExecutorThread =>
        thread.setDaemon(true)
        thread.start()
      }
    }

    def isDone: Boolean = queue.isEmpty && threads.forall(_.idle)

    def waitUntilDone(): Unit = {
      while (!isDone) {
        doneLock.synchronized {
          if (!isDone) {
            doneLock.wait()
          }
        }
      }
    }
  }
}

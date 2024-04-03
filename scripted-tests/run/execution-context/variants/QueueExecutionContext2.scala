package scala.scalanative.concurrent

import scala.concurrent.Future
import scala.scalanative.concurrent.{
  NativeExecutionContext,
  QueueExecutionContextImpl
}
import NativeExecutionContext.Implicits.queue

object Test {
  def main(args: Array[String]): Unit = {
    var i = 0
    val runnable = new Runnable {
      def run(): Unit = i += 1
    }

    val queue = NativeExecutionContext.queue
      .asInstanceOf[QueueExecutionContextImpl]
    queue.execute(runnable)
    queue.execute(runnable)

    assert(queue.nonEmpty)
    assert(queue.availableTasks == 2)
    queue.stealWork(1)
    assert(i == 1)

    assert(queue.nonEmpty)
    assert(queue.availableTasks == 1)
    queue.stealWork(1)
    assert(i == 2)

    assert(queue.isEmpty)
    assert(queue.availableTasks == 0)
    queue.stealWork(1)
    assert(i == 2)
  }

}

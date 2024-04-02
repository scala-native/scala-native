package scala.scalanative.concurrent

import scala.concurrent.Future
import scala.scalanative.concurrent.{
  NativeExecutionContext,
  QueueExecutionContextImpl
}
import NativeExecutionContext.Implicits.queue

object Test {
  def main(args: Array[String]): Unit = {
    println("start main")
    chainFutures()
    executeSingleTask()
    println("end main")
  }
  def chainFutures(): Unit = {
    Future {
      println("future 1")
      1 + 2
    }.map { x =>
      println("future 2")
      x + 3
    }.map { x =>
      println("future 3")
      x + 4
    }.foreach { res => println("result: " + res) }
  }

  def executeSingleTask(): Unit = {
    var i = 0
    val runnable = new Runnable {
      def run(): Unit = i += 1
    }

    val queue = NativeExecutionContext.queue
      .asInstanceOf[QueueExecutionContextImpl]
    queue.execute(runnable)
    queue.execute(runnable)

    assert(queue.isWorkStealingPossible)
    assert(queue.availableTasks == 2)
    queue.stealWork(1)
    assert(i == 1)

    assert(queue.isWorkStealingPossible)
    assert(queue.availableTasks == 1)
    queue.stealWork(1)
    assert(i == 2)

    assert(!queue.isWorkStealingPossible)
    assert(queue.availableTasks == 0)
    queue.stealWork(1)
    assert(i == 2)
  }

}

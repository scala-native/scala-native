/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Test, Ignore}
import JSR166Test._

import java.util.concurrent._
import Future.State._

class ExecutorService19Test extends JSR166Test {

  private def testExecutors(test: ExecutorService => Unit) = Seq(
    new DelegatingExecutorService(Executors.newCachedThreadPool()),
    new ForkJoinPool(),
    Executors.newCachedThreadPool(),
    Executors.newFixedThreadPool(1),
    Executors.newCachedThreadPool()
    // TODO: requires Executors.newThreadPerTaskExecutor
    // Executors.newThreadPerTaskExecutor(Executors.defaultThreadFactory())
    // TODO: requires VirtualThreads
    // Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory()),
  ).foreach(usingPoolCleaner(_)(test))

  // Future state/result

  /** Test methods when the task has not completed.
   */
  @Test def testRunningTask(): Unit = testExecutors { executor =>
    val latch = new CountDownLatch(1)
    val future = executor.submit { () =>
      latch.await()
      null
    }

    try {
      assertEquals(RUNNING, future.state());
      assertThrows(classOf[IllegalStateException], () => future.resultNow())
      assertThrows(classOf[IllegalStateException], () => future.exceptionNow())
    } finally latch.countDown()
  }

  /** Test methods when the task has already completed with a result.
   */
  @Test def testCompletedTask1(): Unit = testExecutors { executor =>
    val future = executor.submit { () => "foo" }
    awaitDone(future)
    assertEquals(SUCCESS, future.state())
    assertEquals("foo", future.resultNow())
    assertThrows(classOf[IllegalStateException], () => future.exceptionNow())
  }

  /** Test methods when the task has already completed with null.
   */
  @Test def testCompletedTask2(): Unit = testExecutors { executor =>
    val future = executor.submit { () => null }
    awaitDone(future)
    assertEquals(SUCCESS, future.state())
    assertEquals(null, future.resultNow())
    assertThrows(classOf[IllegalStateException], () => future.exceptionNow())
  }

  /** Test methods when the task has completed with an exception.
   */
  @Test def testFailedTask(): Unit = testExecutors { executor =>
    val future = executor.submit[String] { () =>
      throw new ArithmeticException()
    }
    awaitDone(future)
    assertEquals(FAILED, future.state());
    assertThrows(classOf[IllegalStateException], () => future.resultNow())
    val ex = future.exceptionNow();
    assertTrue(ex.isInstanceOf[ArithmeticException])
  }

  /** Test methods when the task has been cancelled
   *  (mayInterruptIfRunning=false)
   */
  @Test def testCancelledTask1(): Unit = testExecutors { executor =>
    val latch = new CountDownLatch(1)
    val future = executor.submit { () =>
      latch.await()
      null
    }
    future.cancel(false)
    try {
      assertEquals(CANCELLED, future.state())
      assertThrows(classOf[IllegalStateException], () => future.resultNow())
      assertThrows(classOf[IllegalStateException], () => future.exceptionNow())
    } finally latch.countDown()
  }

  /** Test methods when the task has been cancelled (mayInterruptIfRunning=true)
   */
  @Test def testCancelledTask2(): Unit = testExecutors { executor =>
    val latch = new CountDownLatch(1)
    val future = executor.submit { () =>
      latch.await()
      null
    }
    future.cancel(true)
    try {
      assertEquals(CANCELLED, future.state())
      assertThrows(classOf[IllegalStateException], () => future.resultNow())
      assertThrows(classOf[IllegalStateException], () => future.exceptionNow())
    } finally latch.countDown()
  }

  // TODO: requires CompletableFuture
  // /** Test CompletableFuture with the task has not completed.
  //  */
  // @Test def testCompletableFuture1(): Unit = {
  //   val future = new CompletableFuture[String]()
  //   assertEquals(RUNNING, future.state())
  //   assertThrows(classOf[IllegalStateException], () => future.resultNow())
  //   assertThrows(classOf[IllegalStateException], () => future.exceptionNow())
  // }

  // /** Test CompletableFuture with the task has completed with result
  //  */
  // @Test def testCompletableFuture2(): Unit = {
  //   val future = new CompletableFuture[String]()
  //   future.complete("foo")
  //   assertEquals(SUCCESS, future.state())
  //   assertEquals("foo", future.resultNow())
  //   assertThrows(classOf[IllegalStateException], () => future.exceptionNow())
  // }

  // /** Test CompletableFuture with the task has completed with null
  //  */
  // @Test def testCompletableFuture3(): Unit = {
  //   val future = new CompletableFuture[String]()
  //   future.complete(null)
  //   assertEquals(SUCCESS, future.state())
  //   assertEquals(null, future.resultNow())
  //   assertThrows(classOf[IllegalStateException], () => future.exceptionNow())
  // }

  // /** Test CompletableFuture with the task has completed with exception
  //  */
  // @Test def testCompletableFuture4(): Unit = {
  //   val future = new CompletableFuture[String]()
  //   future.completeExceptionally(new ArithmeticException())
  //   assertEquals(FAILED, future.state())
  //   assertThrows(classOf[IllegalStateException], () => future.resultNow())
  //   val ex = future.exceptionNow();
  //   assertTrue(ex.isInstanceOf[ArithmeticException])
  // }

  // /** Test CompletableFuture with the task that was cancelled
  //  */
  // @Test def testCompletableFuture5(): Unit = {
  //   val future = new CompletableFuture[String]()
  //   future.cancel(false)
  //   assertEquals(CANCELLED, future.state())
  //   assertThrows(classOf[IllegalStateException], () => future.resultNow())
  //   assertThrows(classOf[IllegalStateException], () => future.exceptionNow())
  // }

  // Close
  /** Test close with no tasks running.
   */
  @Test def testCloseWithNoTasks(): Unit = testExecutors { executor =>
    executor.close()
    assertTrue(executor.isShutdown)
    assertTrue(executor.isTerminated)
    assertTrue(executor.awaitTermination(10, TimeUnit.MILLISECONDS))
  }

  /** Test close with tasks running.
   */
  @Test def testCloseWithRunningTasks(): Unit = testExecutors { executor =>
    val future: Future[_] = executor.submit(() => {
      Thread.sleep(1000)
      "foo"

    })
    executor.close() // waits for task to complete

    assertTrue(executor.isShutdown)
    assertTrue(executor.isTerminated)
    assertTrue(executor.awaitTermination(10, TimeUnit.MILLISECONDS))
    assertEquals("foo", future.get())
  }

  // TODO: requires Phaser
  // /** Test close when executor is shutdown but not terminated.
  //  */
  // @Test def testShutdownBeforeClose(): Unit = testExecutors { executor =>
  //   val phaser: Phaser = new Phaser(2)
  //   val future: Future[_] = executor.submit(() => {
  //     phaser.arriveAndAwaitAdvance
  //     Thread.sleep(1000)
  //     "foo"

  //   })
  //   phaser.arriveAndAwaitAdvance() // wait for task to start

  //   executor.shutdown() // shutdown, will not immediately terminate

  //   executor.close()
  //   assertTrue(executor.isShutdown)
  //   assertTrue(executor.isTerminated)
  //   assertTrue(executor.awaitTermination(10, TimeUnit.MILLISECONDS))
  //   assertEquals(future.get, "foo")
  // }

  // /** Test invoking close with interrupt status set.
  //  */
  // @Test def testInterruptBeforeClose(): Unit = testExecutors { executor =>
  //   val phaser: Phaser = new Phaser(2)
  //   val future: Future[_] = executor.submit(() => {
  //     phaser.arriveAndAwaitAdvance
  //     Thread.sleep(Int.MaxValue)
  //     null

  //   })
  //   phaser.arriveAndAwaitAdvance // wait for task to start

  //   Thread.currentThread.interrupt
  //   try {
  //     executor.close()
  //     assertTrue(Thread.currentThread.isInterrupted)
  //   } finally {
  //     Thread.interrupted // clear interrupt status

  //   }
  //   assertTrue(executor.isShutdown)
  //   assertTrue(executor.isTerminated)
  //   assertTrue(executor.awaitTermination(10, TimeUnit.MILLISECONDS))
  //   assertThrows(classOf[ExecutionException], () => future.get)
  // }

  /** Test close when terminated.
   */
  @Test def testTerminateBeforeClose(): Unit = testExecutors { executor =>
    executor.shutdown()
    assertTrue(executor.isTerminated)
    executor.close()
    assertTrue(executor.isShutdown)
    assertTrue(executor.isTerminated)
    assertTrue(executor.awaitTermination(10, TimeUnit.MILLISECONDS))
  }

  /** Test interrupting thread blocked in close.
   */
  @Test def testInterruptDuringClose(): Unit = testExecutors { executor =>
    val future: Future[_] = executor.submit(() => {
      Thread.sleep(Int.MaxValue)
      null

    })
    val thread: Thread = Thread.currentThread
    new Thread(() => {
      try Thread.sleep(500)
      catch {
        case ignore: Exception =>
      }
      thread.interrupt()

    }).start()
    try {
      executor.close()
      assertTrue(Thread.currentThread.isInterrupted)
    } finally {
      Thread.interrupted // clear interrupt status

    }
    assertTrue(executor.isShutdown)
    assertTrue(executor.isTerminated)
    assertTrue(executor.awaitTermination(10, TimeUnit.MILLISECONDS))
    assertThrows(classOf[ExecutionException], () => future.get)
  }

  // Utils

  /** Waits for the future to be done.
   */
  private def awaitDone(future: Future[_]): Unit = {
    var interrupted = false
    while (!future.isDone()) {
      try Thread.sleep(10)
      catch { case _: InterruptedException => interrupted = true }
    }
    if (interrupted) {
      Thread.currentThread().interrupt()
    }
  }

}

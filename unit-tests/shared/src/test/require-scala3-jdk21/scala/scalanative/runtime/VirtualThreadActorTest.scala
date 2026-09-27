package scala.scalanative.runtime

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.{
  CompletableFuture, CountDownLatch, LinkedBlockingQueue, TimeUnit
}

import scala.collection.mutable.ArrayBuffer

import org.junit.Assert._
import org.junit.{BeforeClass, Test}

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadActorTest:
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeSupportsVirtualThreads()

class VirtualThreadActorTest:
  private case class Request(
      value: Int,
      result: CompletableFuture[java.lang.Integer]
  )

  // Model the ox #445 workload: one actor drains a queue and completes one
  // future for each virtual-thread requester.
  @Test def oneActorManyVirtualThreadRequesters(): Unit =
    val requests = new LinkedBlockingQueue[Request]()
    val running = new AtomicBoolean(true)
    val failure = new AtomicReference[java.lang.Throwable]()
    val requesters = ArrayBuffer.empty[Thread]
    val futures = ArrayBuffer.empty[CompletableFuture[java.lang.Integer]]

    val actor = Thread
      .ofVirtual()
      .name("ox-reproducer-actor")
      .start(() => {
        try {
          while running.get() do
            val request = requests.poll(100, TimeUnit.MILLISECONDS)
            if request != null then
              request.result
                .complete(java.lang.Integer.valueOf(request.value + 1))
        } catch {
          case _: InterruptedException => ()
          case ex: java.lang.Throwable => failure.compareAndSet(null, ex)
        }
      })

    try
      for count <- List(10, 100, 500, 1000) do
        val finished = new CountDownLatch(count)
        for value <- 0 until count do
          val result = new CompletableFuture[java.lang.Integer]()
          futures += result
          val requester = Thread
            .ofVirtual()
            .name(s"ox-requester-$count-$value")
            .start(() => {
              try {
                requests.put(Request(value, result))
                // The untimed get() follows the path that busy-waited in 0.5.12.
                // The batch latch bounds the wait and cleanup cancels this future.
                val actual = result.get()
                if actual.intValue() != value + 1 then
                  throw new AssertionError(s"request $value returned $actual")
              } catch {
                case ex: java.lang.Throwable => failure.compareAndSet(null, ex)
              } finally {
                finished.countDown()
              }
            })
          requesters += requester

        val completed = finished.await(30, TimeUnit.SECONDS)
        assertTrue(
          s"actor stalled at $count requesters; remaining=${finished.getCount()}",
          completed
        )
        assertNull(s"request failed at $count requesters", failure.get())
    finally
      running.set(false)
      actor.interrupt()
      futures.foreach(_.cancel(true))
      requesters.foreach(_.interrupt())
      actor.join(5000)
      assertFalse("actor did not stop", actor.isAlive())
      val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
      requesters.foreach { thread =>
        val remaining = deadline - System.nanoTime()
        if remaining > 0 then
          thread.join(math.max(1L, TimeUnit.NANOSECONDS.toMillis(remaining)))
      }
      assertTrue(
        "requesters did not stop",
        requesters.forall(t => !t.isAlive())
      )

package scala.scalanative.runtime

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import java.util.concurrent.{CountDownLatch, LinkedBlockingQueue, TimeUnit}

import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.isContinuationsSupported
import scala.scalanative.runtime.Continuations.*

class ContinuationsStressTest:

  @Test def nestedSuspendResumeAcrossThreadsWithObjectReuse(): Unit = {
    if isContinuationsSupported then
      val fibers = 200
      val iterations = 10000
      val nestedDepth = 3
      val workers = math.max(4, Runtime.getRuntime().availableProcessors())
      val reusedSlotsPerFiber = 16
      val reusedPayloadSize = 8
      assertTrue(
        s"workers must be <= 63 for bitmask tracking, got $workers",
        workers <= 63
      )

      val workerNamePrefix = "cont-stress-worker-"
      val ready =
        scala.Array.tabulate(workers)(_ =>
          new LinkedBlockingQueue[() => Unit]()
        )
      val finished = new CountDownLatch(fibers)
      val workersFinished = new CountDownLatch(workers)
      val resumedSuspensions = new AtomicInteger(0)
      val completedIterations = new AtomicInteger(0)
      val pendingTasks = new AtomicInteger(0)
      val enqueued = new AtomicInteger(0)
      val reusedObjectTouches = new AtomicInteger(0)
      val reusedObjectChecksum = new AtomicLong(0L)
      val fiberWorkerMasks = scala.Array.fill(fibers)(new AtomicLong(0L))

      final class ReusedObject(
          val fiberId: Int,
          val slot: Int,
          val payload: scala.Array[Int],
          val anchor: AnyRef
      ):
        var visits = 0
        var checksum = (fiberId + 1) * 131 + slot

      final class FrameObject(
          val fiberId: Int,
          val iteration: Int,
          val level: Int,
          val serial: Int,
          val anchor: AnyRef
      ):
        var touched = 0

      def currentWorkerIndex(): Int =
        val name = Thread.currentThread().getName()
        if name.startsWith(workerNamePrefix) then
          name
            .substring(workerNamePrefix.length)
            .toIntOption
            .getOrElse(-1)
        else -1

      def markFiberWorker(fiberId: Int): Unit =
        val workerIdx = currentWorkerIndex()
        if workerIdx >= 0 then
          val bit = 1L << workerIdx
          val maskRef = fiberWorkerMasks(fiberId)
          var done = false
          while !done do
            val prev = maskRef.get()
            val next = prev | bit
            done = (prev == next) || maskRef.compareAndSet(prev, next)

      def enqueue(fiberId: Int, k: () => Unit): Unit =
        val current = currentWorkerIndex()
        val targetWorker =
          if workers == 1 then 0
          else if current >= 0 then (current + 1) % workers
          else fiberId % workers
        ready(targetWorker).put(k)
        pendingTasks.incrementAndGet()
        enqueued.incrementAndGet()

      def suspendNested(
          fiberId: Int,
          iteration: Int,
          level: Int,
          reusable: scala.Array[ReusedObject],
          anchor: AnyRef
      )(using BoundaryLabel[Unit]): Unit =
        val slot = (iteration + level) % reusable.length
        val reused = reusable(slot)
        val payloadIdx = (iteration * 5 + level) % reused.payload.length
        val frameSerial = (iteration << 8) ^ (level << 2) ^ fiberId
        val frame =
          new FrameObject(fiberId, iteration, level, frameSerial, anchor)

        reused.visits += 1
        reused.checksum =
          reused.checksum ^ reused.payload(payloadIdx) ^ frame.serial
        frame.touched = reused.checksum

        suspend[Unit] { resume =>
          enqueue(fiberId, resume)
        }

        markFiberWorker(fiberId)
        assertTrue(
          s"Frame anchor lost for fiber=$fiberId",
          frame.anchor eq anchor
        )
        assertEquals(
          s"Frame fiber mismatch for fiber=$fiberId",
          fiberId,
          frame.fiberId
        )
        assertEquals(
          s"Frame iteration mismatch for fiber=$fiberId",
          iteration,
          frame.iteration
        )
        assertEquals(
          s"Frame level mismatch for fiber=$fiberId",
          level,
          frame.level
        )
        assertTrue(
          s"Reusable object identity changed for fiber=$fiberId slot=$slot",
          reusable(slot) eq reused
        )
        assertEquals(
          s"Reusable object fiber mismatch for fiber=$fiberId slot=$slot",
          fiberId,
          reused.fiberId
        )
        assertTrue(
          s"Reusable object anchor changed for fiber=$fiberId slot=$slot",
          reused.anchor eq anchor
        )

        reused.payload(payloadIdx) = reused.payload(payloadIdx) + 1
        reused.checksum =
          reused.checksum + reused.payload(payloadIdx) + frame.touched
        resumedSuspensions.incrementAndGet()
        if level > 0 then
          suspendNested(fiberId, iteration, level - 1, reusable, anchor)

      def allQueuesEmpty(): Boolean =
        var i = 0
        while i < workers do
          if !ready(i).isEmpty() then return false
          i += 1
        true

      var fiberId = 0
      while fiberId < fibers do
        val id = fiberId
        boundary[Unit] {
          val anchor = new Object()
          val reusable = scala.Array.tabulate(reusedSlotsPerFiber) { slot =>
            val payload =
              scala.Array.tabulate(reusedPayloadSize)(idx =>
                id * 1000 + slot * 32 + idx
              )
            new ReusedObject(id, slot, payload, anchor)
          }

          var step = 0
          while step < iterations do
            suspendNested(id, step, nestedDepth, reusable, anchor)
            completedIterations.incrementAndGet()
            step += 1

          var visits = 0
          var checksum = 0L
          var slot = 0
          while slot < reusable.length do
            val obj = reusable(slot)
            assertEquals(
              s"Reusable object fiber mismatch at teardown: fiber=$id slot=$slot",
              id,
              obj.fiberId
            )
            assertTrue(
              s"Reusable object anchor mismatch at teardown: fiber=$id slot=$slot",
              obj.anchor eq anchor
            )
            visits += obj.visits
            checksum += obj.checksum.toLong
            slot += 1

          val expectedVisits = iterations * (nestedDepth + 1)
          assertEquals(
            s"Reusable object visits mismatch for fiber=$id",
            expectedVisits,
            visits
          )
          reusedObjectTouches.addAndGet(visits)
          reusedObjectChecksum.addAndGet(checksum)
          finished.countDown()
        }
        fiberId += 1

      def workerLoop(): Unit =
        try
          var keepRunning = true
          while keepRunning do
            val resume =
              ready(currentWorkerIndex()).poll(100, TimeUnit.MILLISECONDS)
            if resume != null then
              pendingTasks.decrementAndGet()
              resume()
              Thread.`yield`()
            else if finished.getCount() == 0 && pendingTasks
                  .get() == 0 && allQueuesEmpty() then keepRunning = false
        finally workersFinished.countDown()

      val pool = scala.Array.tabulate(workers) { idx =>
        val t = new Thread(() => workerLoop(), s"${workerNamePrefix}$idx")
        t.start()
        t
      }
      val _ = pool

      assertTrue(
        s"Timed out waiting for fiber completion; resumed=${resumedSuspensions.get()}, " +
          s"completed=${completedIterations.get()}, pending=${pendingTasks.get()}",
        finished.await(120, TimeUnit.SECONDS)
      )
      assertTrue(
        s"Timed out waiting for workers to stop; pending=${pendingTasks.get()}",
        workersFinished.await(20, TimeUnit.SECONDS)
      )

      val expectedIterations = fibers.toLong * iterations.toLong
      val expectedSuspensions = expectedIterations * (nestedDepth.toLong + 1L)
      assertEquals(expectedIterations, completedIterations.get().toLong)
      assertEquals(expectedSuspensions, resumedSuspensions.get().toLong)
      assertEquals(expectedSuspensions, enqueued.get().toLong)
      assertEquals(expectedSuspensions, reusedObjectTouches.get().toLong)
      assertTrue(
        "Reusable object checksum should not be zero",
        reusedObjectChecksum.get() != 0L
      )

      if workers > 1 then
        var i = 0
        while i < fibers do
          val mask = fiberWorkerMasks(i).get()
          val distinctWorkers = java.lang.Long.bitCount(mask)
          assertTrue(
            s"Fiber $i did not cycle through different carrier threads; " +
              s"mask=0x${java.lang.Long.toHexString(mask)}, distinct=$distinctWorkers",
            distinctWorkers >= 2
          )
          i += 1
  }

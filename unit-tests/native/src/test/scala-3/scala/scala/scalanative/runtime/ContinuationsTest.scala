package scala.scalanative.runtime

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.{CountDownLatch, LinkedBlockingQueue, TimeUnit}

import scala.util.control.ControlThrowable

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.{
  is32BitPlatform, isContinuationsSupported
}

import Continuations._

class ContinuationsTest:
  @Test def canBoundaryNoSuspend() =
    if isContinuationsSupported then
      val res = boundary[Int] {
        val x = 1
        val y = 2
        x + y
      }
      assert(res == 3)

  @Test def canBoundarySuspend() =
    if isContinuationsSupported then
      val res = boundary[Int] {
        val x = 1
        suspend[Int](_ => x + 1)
        ???
      }
      assert(res == 2)

  @Test def canBoundarySuspendImmediateResume() =
    if isContinuationsSupported then
      val r = boundary[Int] {
        1 + suspend[Int, Int](r => r(2)) + suspend[Int, Int](r => r(3)) + 4
      }
      assert(r == 10)

  @Test def canBoundarySuspendCommunicate() =
    if isContinuationsSupported then
      case class Iter(n: Int, nx: Int => Iter)
      val r0 = boundary[Iter] {
        var r = 0
        while (true) {
          r += suspend[Int, Iter](cb => Iter(r, cb))
        }
        ???
      }
      assert(r0.n == 0)
      val r1 = r0.nx(2)
      assert(r1.n == 2)
      val r2 = r1.nx(3)
      assert(r2.n == 5)

  @Test def fibonacci(): Unit = {
    if isContinuationsSupported then
      import scala.collection.mutable.ArrayBuffer

      case class Seqnt[T, R](v: T, nx: R => Seqnt[T, R])
      type Seqn[T] = Seqnt[T, Int]

      def fib = boundary[Seqn[Int]] {
        // these get boxed, so it's not really working different from a generator
        var a = 1
        var b = 1
        while (true) {
          val steps = suspend[Int, Seqn[Int]](c => Seqnt(a, c))
          for (i <- 1 to steps) {
            val c = a + b
            a = b
            b = c
          }
        }
        Seqnt(0, ???)
      }

      val fibs = ArrayBuffer(fib)
      for (step <- 1 to 10) {
        fibs += fibs(step - 1).nx(1)
      }

      val fibList = fibs.map(_.v).toList
      assert(fibList == List(1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89))
  }

  @Test def basic(): Unit = {
    if isContinuationsSupported then
      enum Response[T] {
        case Next(nx: () => Response[T], v: T)
        case End(v: T)
      }
      import Response.*
      val oneThenTwo = boundary[Response[Int]] {
        suspend[Response[Int]](Next(_, 1))
        End(2)
      }

      oneThenTwo match
        case Next(nx, v) =>
          assert(v == 1)
          val v2 = nx()
          assert(v2 == End(2))
        case End(v) =>
          assert(false)
  }

  @Test def nestedResumePropagatesEscapingThrowable(): Unit =
    if isContinuationsSupported then {
      case object EscapesBoundary extends ControlThrowable
      @noinline def captureResumePoint(onResume: => Unit): () => Unit = {
        var resume: () => Unit = null
        boundary[Unit] {
          suspend[Unit] { k =>
            resume = k
          }
          onResume
        }

        if resume == null then
          fail("Failed to capture continuation resume handle")
        resume
      }

      val innerResume = captureResumePoint { () }
      val outerResume = captureResumePoint {
        innerResume()
        throw EscapesBoundary
      }

      try
        outerResume()
        fail("Expected EscapesBoundary to propagate from nested resume")
      catch case EscapesBoundary => ()
    }

  @Test def resumePreservesLocalsAcrossSuspendResume(): Unit = {
    var clobberSink: Int = 0

    @noinline def runOneContinuationCycle(i: Int): Int = {
      var resume: () => Int = null

      val first = boundary[Int] {
        val a = i + 11
        val b = i + 23
        val c = i + 37

        val token: Int = suspend[Int, Int] { k =>
          resume = () => k(123)
          -777
        }
        a + b + c + token
      }

      assertEquals(-777, first)
      clobberSink ^= clobberStackFrames(i)

      val resumed = resume()
      val expected = (i + 11) + (i + 23) + (i + 37) + 123
      assertEquals(expected, resumed)
      resumed
    }

    @noinline def clobberStackFrames(seed: Int): Int =
      clobberStackFramesRec(48, seed)

    @noinline def clobberStackFramesRec(depth: Int, in: Int): Int = {
      val a0 = in + 1
      val a1 = in + 2
      val a2 = in + 3
      val a3 = in + 4
      val a4 = in + 5
      val a5 = in + 6
      val a6 = in + 7
      val a7 = in + 8
      val sum = a0 + a1 + a2 + a3 + a4 + a5 + a6 + a7

      if depth == 0 then sum
      else clobberStackFramesRec(depth - 1, sum ^ depth) + (depth & 1)
    }

    if isContinuationsSupported then {
      var checksum = 0L
      var i = 0

      while i < 5000 do
        checksum += runOneContinuationCycle(i)
        i += 1

      val n = 5000L
      val expectedChecksum = (3L * n * (n - 1) / 2L) + (194L * n)
      assertEquals(expectedChecksum, checksum)
    }
  }

  @Test def stressSuspendResumeAcrossPlatformThreads(): Unit =
    if isContinuationsSupported then {
      val fibers = 50
      val iterations = 1000
      val workers = math.max(4, Runtime.getRuntime().availableProcessors())

      val ready = new LinkedBlockingQueue[() => Unit]()
      val finished = new CountDownLatch(fibers)
      val workersFinished = new CountDownLatch(workers)
      val resumedSteps = new AtomicInteger(0)
      val enqueued = new AtomicInteger(0)

      def enqueue(k: () => Unit): Unit = {
        ready.put(k)
        enqueued.incrementAndGet()
      }

      var fiberId = 0
      while fiberId < fibers do
        boundary[Unit] {
          var step = 0
          while step < iterations do
            suspend[Unit] { resume =>
              enqueue(resume)
            }
            resumedSteps.incrementAndGet()
            step += 1
          finished.countDown()
        }
        fiberId += 1

      def workerLoop(): Unit =
        try
          var keepRunning = true
          while keepRunning do
            val resume = ready.poll(100, TimeUnit.MILLISECONDS)
            if resume != null then
              resume()
              Thread.`yield`()
            else if finished.getCount() == 0 && ready.isEmpty() then
              keepRunning = false
        finally workersFinished.countDown()

      val pool = scala.Array.tabulate(workers) { idx =>
        val t = new Thread(() => workerLoop(), s"cont-stress-worker-$idx")
        t.start()
        t
      }
      val _ = pool

      assertTrue(
        s"Timed out waiting for fiber completion; resumed=${resumedSteps.get()}, queue=${ready.size()}",
        finished.await(60, TimeUnit.SECONDS)
      )
      assertTrue(
        s"Timed out waiting for workers to stop; queue=${ready.size()}",
        workersFinished.await(10, TimeUnit.SECONDS)
      )

      val expected = fibers * iterations
      assertEquals(expected, resumedSteps.get())
      assertEquals(expected, enqueued.get())
    }

  // -----------------------------------------------------------------------
  // Group B: Raw continuation tests (kept for regression — these should
  // always pass, even before delimcc fixes, because the code path is
  // simpler than the VT machinery).
  // -----------------------------------------------------------------------

  @Test def rawContinuationWithClobber(): Unit =
    if isContinuationsSupported then {
      val workers = math.max(4, Runtime.getRuntime().availableProcessors())
      val fibers = 30
      val iterations = 300
      val pool = new WorkerPool("clobber", workers)
      val finished = new CountDownLatch(fibers)
      val errors = new AtomicInteger(0)

      @noinline def clobberStack(seed: Int): Int =
        val a0 = seed ^ 0xdead
        val a1 = seed ^ 0xbeef
        val a2 = seed ^ 0xcafe
        val a3 = seed ^ 0xbabe
        val a4 = seed ^ 0xf00d
        val a5 = seed ^ 0xd00d
        val a6 = seed ^ 0xface
        val a7 = seed ^ 0xace0
        a0 ^ a1 ^ a2 ^ a3 ^ a4 ^ a5 ^ a6 ^ a7

      var fid = 0
      while fid < fibers do
        val id = fid
        boundary[Unit] {
          var step = 0
          while step < iterations do
            val a0 = id * 1000 + step * 1 + 111
            val a1 = id * 1000 + step * 2 + 222
            val a2 = id * 1000 + step * 3 + 333
            val a3 = id * 1000 + step * 4 + 444
            val a4 = id * 1000 + step * 5 + 555
            val a5 = id * 1000 + step * 6 + 666
            val a6 = id * 1000 + step * 7 + 777
            val a7 = id * 1000 + step * 8 + 888
            val a8 = id * 1000 + step * 9 + 999
            val a9 = id * 1000 + step * 10 + 1010
            val a10 = id * 1000 + step * 11 + 1111
            val a11 = id * 1000 + step * 12 + 1212

            suspend[Unit] { resume =>
              clobberStack(id ^ step)
              pool.submit(resume)
            }

            if a0 != id * 1000 + step * 1 + 111 ||
                a1 != id * 1000 + step * 2 + 222 ||
                a2 != id * 1000 + step * 3 + 333 ||
                a3 != id * 1000 + step * 4 + 444 ||
                a4 != id * 1000 + step * 5 + 555 ||
                a5 != id * 1000 + step * 6 + 666 ||
                a6 != id * 1000 + step * 7 + 777 ||
                a7 != id * 1000 + step * 8 + 888 ||
                a8 != id * 1000 + step * 9 + 999 ||
                a9 != id * 1000 + step * 10 + 1010 ||
                a10 != id * 1000 + step * 11 + 1111 ||
                a11 != id * 1000 + step * 12 + 1212
            then errors.incrementAndGet()

            step += 1
          finished.countDown()
        }
        fid += 1

      assertTrue(
        s"rawContinuationWithClobber timed out; errors=${errors.get()}",
        finished.await(45, TimeUnit.SECONDS)
      )
      assertTrue(
        "clobber worker pool did not stop",
        pool.shutdown(5, TimeUnit.SECONDS)
      )
      assertEquals("register corruption", 0, errors.get())
    }

  @Test def rawDeepStackManyMigrations(): Unit =
    if isContinuationsSupported then {
      val workers = math.max(4, Runtime.getRuntime().availableProcessors())
      val depth = 150
      val rounds = 50
      val pool = new WorkerPool("deepmig", workers)
      val finished = new CountDownLatch(rounds)
      val errors = new AtomicInteger(0)

      @noinline def recurse(d: Int, salt: Long)(using
          BoundaryLabel[Unit]
      ): Long =
        val local1 = salt * 31 + d
        val local2 = salt * 37 + d * 3
        val local3 = salt ^ (d.toLong << 16)
        if d == 0 then
          suspend[Unit] { resume => pool.submit(resume) }
          local1 + local2 + local3
        else
          val sub = recurse(d - 1, local1)
          if local1 != salt * 31 + d ||
              local2 != salt * 37 + d * 3 ||
              local3 != (salt ^ (d.toLong << 16))
          then errors.incrementAndGet()
          sub + local1 + local2 + local3

      var round = 0
      while round < rounds do
        boundary[Unit] {
          recurse(depth, round.toLong * 997)
          finished.countDown()
        }
        round += 1

      assertTrue(
        s"rawDeepStackManyMigrations timed out; errors=${errors.get()}",
        finished.await(45, TimeUnit.SECONDS)
      )
      assertTrue(
        "deepmig worker pool did not stop",
        pool.shutdown(5, TimeUnit.SECONDS)
      )
      assertEquals("deep stack corruption", 0, errors.get())
    }

  private final class WorkerPool(name: String, numWorkers: Int):
    private val queue = new LinkedBlockingQueue[() => Unit]()
    private val done = new AtomicBoolean(false)
    private val stopped = new CountDownLatch(numWorkers)

    private val workers = scala.Array.tabulate(numWorkers) { idx =>
      val t = new Thread(() => workerLoop(), s"$name-$idx")
      t.setDaemon(true)
      t.start()
      t
    }

    private val _ = workers

    private def workerLoop(): Unit =
      try
        while !done.get() do
          val task = queue.poll(50, TimeUnit.MILLISECONDS)
          if task != null then task()
      finally stopped.countDown()

    def submit(task: () => Unit): Unit =
      queue.put(task)

    def shutdown(timeout: Long, unit: TimeUnit): Boolean =
      done.set(true)
      stopped.await(timeout, unit)

  @Test def registerPreservationAcrossCarriers(): Unit =
    if isContinuationsSupported then {
      val workers = math.max(4, Runtime.getRuntime().availableProcessors())
      val fibers = 20
      val iterations = 500
      val ready = new LinkedBlockingQueue[() => Unit]()
      val finished = new CountDownLatch(fibers)
      val workersFinished = new CountDownLatch(workers)
      val errors = new AtomicInteger(0)

      var fid = 0
      while fid < fibers do
        val id = fid
        boundary[Unit] {
          var step = 0
          while step < iterations do
            val a0 = id + step * 1
            val a1 = id + step * 2
            val a2 = id + step * 3
            val a3 = id + step * 4
            val a4 = id + step * 5
            val a5 = id + step * 6
            val a6 = id + step * 7
            val a7 = id + step * 8
            val a8 = id + step * 9
            val a9 = id + step * 10
            val a10 = id + step * 11
            val a11 = id + step * 12

            suspend[Unit] { resume => ready.put(resume) }

            if a0 != id + step * 1 || a1 != id + step * 2 ||
                a2 != id + step * 3 || a3 != id + step * 4 ||
                a4 != id + step * 5 || a5 != id + step * 6 ||
                a6 != id + step * 7 || a7 != id + step * 8 ||
                a8 != id + step * 9 || a9 != id + step * 10 ||
                a10 != id + step * 11 || a11 != id + step * 12
            then errors.incrementAndGet()

            step += 1
          finished.countDown()
        }
        fid += 1

      def workerLoop(): Unit =
        try
          var keepRunning = true
          while keepRunning do
            val task = ready.poll(100, TimeUnit.MILLISECONDS)
            if task != null then task()
            else if finished.getCount() == 0 && ready.isEmpty() then
              keepRunning = false
        finally workersFinished.countDown()

      val pool = scala.Array.tabulate(workers) { idx =>
        val t = new Thread(() => workerLoop(), s"regtest-$idx")
        t.start()
        t
      }
      val _ = pool

      assertTrue(
        s"registerPreservation timed out; errors=${errors.get()}",
        finished.await(60, TimeUnit.SECONDS)
      )
      assertTrue(
        "workers did not stop",
        workersFinished.await(10, TimeUnit.SECONDS)
      )
      assertEquals(s"register corruption detected", 0, errors.get())
    }

  @Test def repeatedSuspendResumeNoStackGrowth(): Unit =
    if isContinuationsSupported then {
      val workers = math.max(2, Runtime.getRuntime().availableProcessors())
      val cycles = 5000
      val ready = new LinkedBlockingQueue[() => Unit]()
      val finished = new CountDownLatch(1)
      val workersFinished = new CountDownLatch(workers)

      boundary[Unit] {
        var i = 0
        while i < cycles do
          suspend[Unit] { resume => ready.put(resume) }
          i += 1
        finished.countDown()
      }

      def workerLoop(): Unit =
        try
          var keepRunning = true
          while keepRunning do
            val task = ready.poll(100, TimeUnit.MILLISECONDS)
            if task != null then task()
            else if finished.getCount() == 0 && ready.isEmpty() then
              keepRunning = false
        finally workersFinished.countDown()

      val pool = scala.Array.tabulate(workers) { idx =>
        val t = new Thread(() => workerLoop(), s"stacktest-$idx")
        t.start()
        t
      }
      val _ = pool

      assertTrue(
        "repeatedSuspendResume timed out — possible stack overflow",
        finished.await(60, TimeUnit.SECONDS)
      )
      assertTrue(
        "workers did not stop",
        workersFinished.await(10, TimeUnit.SECONDS)
      )
    }

  @Test def deepStackSuspendResume(): Unit =
    if isContinuationsSupported then {
      val workers = math.max(2, Runtime.getRuntime().availableProcessors())
      val depth = 200
      val rounds = 20
      val ready = new LinkedBlockingQueue[() => Unit]()
      val finished = new CountDownLatch(rounds)
      val workersFinished = new CountDownLatch(workers)
      val errors = new AtomicInteger(0)

      @noinline def recurse(d: Int, salt: Long)(using
          BoundaryLabel[Unit]
      ): Long =
        val local1 = salt * 31 + d
        val local2 = salt * 37 + d * 3
        val local3 = salt ^ (d.toLong << 16)
        if d == 0 then
          suspend[Unit] { resume => ready.put(resume) }
          local1 + local2 + local3
        else
          val sub = recurse(d - 1, local1)
          if local1 != salt * 31 + d ||
              local2 != salt * 37 + d * 3 ||
              local3 != (salt ^ (d.toLong << 16))
          then errors.incrementAndGet()
          sub + local1 + local2 + local3

      var round = 0
      while round < rounds do
        boundary[Unit] {
          recurse(depth, round.toLong * 997)
          finished.countDown()
        }
        round += 1

      def workerLoop(): Unit =
        try
          var keepRunning = true
          while keepRunning do
            val task = ready.poll(100, TimeUnit.MILLISECONDS)
            if task != null then task()
            else if finished.getCount() == 0 && ready.isEmpty() then
              keepRunning = false
        finally workersFinished.countDown()

      val pool = scala.Array.tabulate(workers) { idx =>
        val t = new Thread(() => workerLoop(), s"deeptest-$idx")
        t.start()
        t
      }
      val _ = pool

      assertTrue(
        s"deepStackSuspendResume timed out; errors=${errors.get()}",
        finished.await(60, TimeUnit.SECONDS)
      )
      assertTrue(
        "workers did not stop",
        workersFinished.await(10, TimeUnit.SECONDS)
      )
      assertEquals("deep stack local corruption", 0, errors.get())
    }

end ContinuationsTest

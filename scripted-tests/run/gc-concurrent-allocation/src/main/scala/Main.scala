import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong, AtomicReference}
import java.util.concurrent.{CountDownLatch, TimeUnit}

import scala.scalanative.meta.LinktimeInfo

object Main {
  private val memory = ManagementFactory.getMemoryMXBean()

  private def heapCapacity(): Long = memory.getHeapMemoryUsage().getCommitted()

  private def mixed(): Unit = {
    val sizes = Array(16, 544, 8192, 32768, 98304, 196608)
    val live = new Array[Array[Byte]](sizes.length * 8)

    for (i <- 0 until 4096) {
      val data = new Array[Byte](sizes(i % sizes.length))
      data(0) = i.toByte
      data(data.length - 1) = (i + 1).toByte
      live(i % live.length) = data

      if (i % 128 == 0) System.gc()
    }

    for (i <- 4096 - live.length until 4096) {
      val data = live(i % live.length)
      assert(data.length == sizes(i % sizes.length))
      assert(data(0) == i.toByte)
      assert(data(data.length - 1) == (i + 1).toByte)
    }
  }

  private def joinAll(threads: Array[Thread]): Unit = {
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)

    threads.foreach { thread =>
      val remaining = deadline - System.nanoTime()
      if (remaining > 0)
        thread.join(math.max(1L, TimeUnit.NANOSECONDS.toMillis(remaining)))
    }

    assert(threads.forall(!_.isAlive()), "allocation threads did not finish")
  }

  private def checkPlateau(
      warmed: CountDownLatch,
      chunks: AtomicLong,
      failure: AtomicReference[Throwable]
  ): Unit = {
    if (!warmed.await(15, TimeUnit.SECONDS)) {
      if (failure.get() != null) throw failure.get()
      throw new AssertionError("allocation warmup timed out")
    }

    Thread.sleep(2000L)
    val initial = heapCapacity()
    val progress = chunks.get()

    Thread.sleep(10000L)
    val current = heapCapacity()
    val completed = chunks.get() - progress

    println(s"heap capacity: $initial -> $current; chunks: $completed")
    assert(completed > 0, "allocation stopped making progress")
    assert(current < 1024L * 1024 * 1024, "heap approached its maximum")
    assert(
      current <= initial + 256 * 1024,
      s"heap kept growing: $initial -> $current"
    )
  }

  private def workload(count: Int, plateau: Boolean = false): Unit = {
    val start = new CountDownLatch(1)
    val stop = new AtomicBoolean(false)
    val failure = new AtomicReference[Throwable]()
    val warmed = new CountDownLatch(count)
    val chunks = new AtomicLong(0)

    val workers = Array.tabulate(count) { id =>
      new Thread(() => {
        try {
          val live = new Array[Array[String]](if (plateau) 64 else 512)
          start.await()

          var i = 0
          while (i < 1024 || !stop.get()) {
            live(i % live.length) = Array.tabulate(64)(j => s"worker-$id-$i-$j")
            i += 1

            if (i == 2048) warmed.countDown()
            if (i % 1024 == 0) chunks.addAndGet(1024)
          }

          assert(live.forall(_ != null))
        } catch {
          case t: Throwable =>
            failure.compareAndSet(null, t)
            stop.set(true)
            ()
        }
      })
    }

    workers.foreach(_.start())
    start.countDown()

    try {
      if (plateau) checkPlateau(warmed, chunks, failure)
      else Thread.sleep(3000L)

      val formatted = f"${heapCapacity() / (1024.0 * 1024)}%.1f"
      assert(formatted.nonEmpty)
    } finally {
      stop.set(true)
      joinAll(workers)
    }

    if (failure.get() != null) throw failure.get()
  }

  private def idle(): Unit = {
    val ready = new CountDownLatch(32)
    val release = new CountDownLatch(1)
    val threads = Array.fill(32)(new Thread(() => {
      ready.countDown()
      release.await()
    }))

    threads.foreach(_.start())

    try {
      assert(ready.await(5, TimeUnit.SECONDS), "idle threads did not start")
      workload(1)
    } finally {
      release.countDown()
      joinAll(threads)
    }

  }

  def main(args: Array[String]): Unit = {
    if (LinktimeInfo.isMultithreadingEnabled) {
      args(0) match {
        case "plateau" => workload(16, plateau = true)
        case "stress"  => workload(16)
        case "idle"    => idle()
        case "mixed"   => mixed()
      }
    } else {
      assert(args(0) == "mixed")
      mixed()
    }
    println(s"gc-concurrent-allocation: ${args(0)} OK")
  }
}

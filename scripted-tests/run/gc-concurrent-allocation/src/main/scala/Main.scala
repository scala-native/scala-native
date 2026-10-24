import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong, AtomicReference}
import java.util.concurrent.{CountDownLatch, TimeUnit}

import scala.scalanative.meta.LinktimeInfo

object Main {
  private val memory = ManagementFactory.getMemoryMXBean()
  private def heapCapacity(): Long = memory.getHeapMemoryUsage().getCommitted()

  def mixed(): Unit = {
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

  def workload(count: Int, plateau: Boolean = false): Unit = {
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
      if (plateau) {
        assert(
          warmed.await(60, TimeUnit.SECONDS),
          "allocation warmup timed out"
        )
        Thread.sleep(3000L)
        val initial = heapCapacity()
        val progress = chunks.get()
        for (_ <- 0 until 10) Thread.sleep(1000L)
        val current = heapCapacity()
        println(
          s"heap capacity: $initial -> $current; chunks: ${chunks.get() - progress}"
        )
        assert(chunks.get() > progress, "allocation stopped making progress")
        assert(current < 1024L * 1024 * 1024, "heap approached its maximum")
        assert(
          current <= initial + 256 * 1024,
          s"heap kept growing: $initial -> $current"
        )
      } else Thread.sleep(5000L)
      val formatted =
        f"${heapCapacity() / (1024.0 * 1024)}%.1f"
      assert(formatted.nonEmpty)
    } finally {
      stop.set(true)
      workers.foreach { worker =>
        worker.join(30000L)
        assert(!worker.isAlive(), "allocation worker did not finish")
      }
    }
    if (failure.get() != null) throw failure.get()
  }

  def idle(): Unit = {
    val ready = new CountDownLatch(32)
    val release = new CountDownLatch(1)
    val threads = Array.fill(32)(new Thread(() => {
      ready.countDown()
      release.await()
    }))
    threads.foreach(_.start())
    try {
      assert(ready.await(30, TimeUnit.SECONDS), "idle threads did not start")
      workload(1)
    } finally {
      release.countDown()
      threads.foreach { thread =>
        thread.join(30000L)
        assert(!thread.isAlive(), "idle thread did not finish")
      }
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

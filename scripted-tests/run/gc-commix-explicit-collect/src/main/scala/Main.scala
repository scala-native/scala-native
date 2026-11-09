import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.runtime.GC

object Main {
  private final val DurationMs = 500L
  private final val SampleMs = 20L
  private final val AllocThreads = 4
  // Allocating threads run with a delay, so they keep allocating while
  // collections happen without ever failing an allocation. A failed allocation
  // is real heap pressure and may legitimately grow the heap, which would make
  // the measurement below depend on how fast the machine is.
  private final val AllocDelayMs = 1L
  // Live data is a few 64-byte arrays, so the heap stays near its 4MiB
  // initial size. A runaway grow reaches the 64MiB cap instead.
  private final val MaxReservedBytes = 16L * 1024 * 1024
  // Guards against the test passing because no collection ever ran.
  private final val MinCollections = 50L

  private val running = new AtomicBoolean(true)

  private def startDaemon(name: String, body: Runnable): Thread = {
    val thread = new Thread(body, name)
    thread.setDaemon(true)
    thread.start()
    thread
  }

  private def collectLoop(): Unit =
    while (running.get()) System.gc()

  private def sleepLoop(): Unit =
    while (running.get()) Thread.sleep(1L)

  private def allocLoop(id: Int): Unit = {
    var buf = new Array[Byte](0)
    while (running.get()) {
      buf = new Array[Byte](64)
      buf(0) = id.toByte
      Thread.sleep(AllocDelayMs)
    }
  }

  private def peakReservedHeap(): Long = {
    val deadline = System.currentTimeMillis() + DurationMs
    var peak = GC.getUsedHeapSize().toLong
    while (System.currentTimeMillis() < deadline) {
      Thread.sleep(SampleMs)
      peak = math.max(peak, GC.getUsedHeapSize().toLong)
    }
    peak
  }

  // #2436: many tiny live arrays among fast 1600-byte garbage.
  private def markTimeGrowth(): Unit = {
    val iterations = 500000
    val maxHeap = 384L * 1024 * 1024
    val retained = new scala.collection.mutable.ArrayBuffer[Array[Char]](
      iterations
    )
    var sum = 0
    var peakHeap = 0L
    var i = 0
    while (i < iterations) {
      var j = 0
      while (j < 9) {
        sum += new Array[Byte](1600)(0)
        j += 1
      }
      retained += new Array[Char](0)
      if (i % 4096 == 0)
        peakHeap = math.max(peakHeap, GC.getUsedHeapSize().toLong)
      i += 1
    }
    peakHeap = math.max(peakHeap, GC.getUsedHeapSize().toLong)
    println(s"mark-time: sum=$sum peakMiB=${peakHeap >> 20}")
    if (peakHeap > maxHeap)
      throw new AssertionError(
        s"issue 2436: the heap grew to ${peakHeap >> 20}MiB " +
          s"(limit ${maxHeap >> 20}MiB)"
      )
  }

  def main(args: Array[String]): Unit =
    if (args.headOption.contains("mark-time")) markTimeGrowth()
    else explicitCollectGrowth()

  private def explicitCollectGrowth(): Unit = {
    val startHeap = GC.getUsedHeapSize().toLong
    startDaemon("gc-loop", () => collectLoop())
    startDaemon("blocking", () => sleepLoop())
    val allocators = Array.tabulate(AllocThreads) { id =>
      startDaemon(s"alloc-$id", () => allocLoop(id))
    }

    val sampled = peakReservedHeap()
    running.set(false)
    allocators.foreach(_.join(1000L))

    val endHeap = GC.getUsedHeapSize().toLong
    val peakHeap = math.max(sampled, endHeap)
    val collections = GC.getStatsCollectionTotal().toLong
    println(
      s"startMiB=${startHeap >> 20} peakMiB=${peakHeap >> 20} " +
        s"endMiB=${endHeap >> 20} gcs=$collections " +
        s"maxMiB=${GC.getMaxHeapSize().toLong >> 20}"
    )

    if (collections < MinCollections)
      throw new AssertionError(
        s"the System.gc() loop ran only $collections collections, " +
          s"expected at least $MinCollections"
      )

    if (peakHeap > MaxReservedBytes)
      throw new AssertionError(
        s"issue 5052: a System.gc() loop grew the reserved heap from " +
          s"${startHeap >> 20}MiB to ${peakHeap >> 20}MiB " +
          s"(limit ${MaxReservedBytes >> 20}MiB)"
      )

    println("gc-commix-explicit-collect: OK")
  }
}

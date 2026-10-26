import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, TimeUnit}

// Standalone benchmark; intentionally outside the scripted test's source path.
object AllocationBenchmark {
  private final class Node(val value: Long, val next: Node)

  private final class Worker(
      scenario: String,
      size: Int,
      ready: CountDownLatch,
      start: CountDownLatch,
      failure: AtomicReference[Throwable]
  ) extends Thread {
    @volatile var deadline = 0L
    var allocations = 0L
    var checksum = 0L

    override def run(): Unit = {
      try {
        if (scenario == "objects") objects()
        else arrays()
      } catch {
        case error: Throwable => failure.compareAndSet(null, error)
      }
    }

    private def objects(): Unit = {
      val retained = new Array[Node](1024)
      ready.countDown()
      start.await()

      var count = 0L
      while (System.nanoTime() < deadline) {
        val limit = count + 1024
        while (count < limit) {
          val slot = (count & 1023).toInt
          retained(slot) = new Node(count, null)
          count += 1
        }
      }

      allocations = count
      retained.indices.foreach { slot =>
        val node = retained(slot)
        assert(node.value == count - retained.length + slot)
        assert(node.next == null)
        checksum += node.value
      }
    }

    private def arrays(): Unit = {
      // Bound retained bytes independently of allocation size.
      val slots = math.max(4, math.min(1024, 65536 / size))
      val retained = new Array[Array[Byte]](slots)
      ready.countDown()
      start.await()

      var count = 0L
      var slot = 0
      while (System.nanoTime() < deadline) {
        val limit = count + 1024
        while (count < limit) {
          val data = new Array[Byte](size)
          data(0) = count.toByte
          data(size - 1) = (count >>> 8).toByte
          retained(slot) = data
          slot += 1
          if (slot == slots) slot = 0
          count += 1
        }
      }

      allocations = count
      retained.indices.foreach { slot =>
        val data = retained(slot)
        val last = count - 1 - (count - 1 - slot) % slots
        assert(data(0) == last.toByte)
        assert(data(size - 1) == (last >>> 8).toByte)
        checksum += data(0) + data(size - 1)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val scenario = args(0)
    val count = args(1).toInt
    val seconds = args(2).toInt
    val size = scenario match {
      case "objects" => 0
      case "small"   => 64
      case "medium"  => 544
      case "large"   => 8192
      case "blocks"  => 98304
    }

    val memory = ManagementFactory.getMemoryMXBean()
    val gc = ManagementFactory.getGarbageCollectorMXBeans().get(0)
    val ready = new CountDownLatch(count)
    val start = new CountDownLatch(1)
    val failure = new AtomicReference[Throwable]()
    val workers = Array.fill(count)(
      new Worker(scenario, size, ready, start, failure)
    )

    workers.foreach(_.start())
    assert(ready.await(10, TimeUnit.SECONDS), "benchmark workers did not start")

    val collectionsBefore = gc.getCollectionCount()
    val gcTimeBefore = gc.getCollectionTime()
    val started = System.nanoTime()
    val deadline = started + TimeUnit.SECONDS.toNanos(seconds)
    workers.foreach(_.deadline = deadline)
    start.countDown()
    workers.foreach(_.join())
    val elapsed = (System.nanoTime() - started) / 1e9

    if (failure.get() != null) throw failure.get()

    assert(
      workers.forall(_.allocations > 0),
      "a benchmark worker made no progress"
    )
    val allocations = workers.map(_.allocations).sum
    val checksum = workers.map(_.checksum).sum
    val collections = gc.getCollectionCount() - collectionsBefore
    val gcMillis = gc.getCollectionTime() - gcTimeBefore
    val heap = memory.getHeapMemoryUsage().getCommitted()

    assert(allocations > 0, "benchmark made no progress")
    println(
      f"scenario=$scenario workers=$count allocations=$allocations elapsed=$elapsed%.6f " +
        s"collections=$collections gc_ms=$gcMillis heap_bytes=$heap checksum=$checksum"
    )
  }
}

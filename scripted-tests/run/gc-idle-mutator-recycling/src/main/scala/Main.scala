import java.util.concurrent.{CountDownLatch, TimeUnit}

/* A long-lived list built one node at a time among short-lived garbage, so
 * that every collection leaves partly free blocks: each kept node pins its
 * line while the garbage around it is reclaimed.
 *
 *   idle <threads> <nodes>  builds <nodes> with <threads> idle threads alive;
 *                           the heap cap that holds them must not depend on
 *                           how many threads are idle
 *   medium <bytes>          keeps about <bytes> of 8-32 KiB arrays live among
 *                           discarded ones; must not report exhaustion
 *   full <bytes> <n>        keeps <bytes> live and allocates <n> small
 *                           objects; must not report exhaustion
 *   exhaust                 builds without bound; must end in
 *                           OutOfMemoryError rather than collect forever
 */
object Main {
  final class Node(val next: Node, val key: Int)

  private final val GarbagePerNode = 40

  @volatile private var sink: AnyRef = null

  private def build(count: Int): Node = {
    var kept: Node = null
    var i = 0
    while (i < count) {
      var garbage: Node = null
      var g = 0
      while (g < GarbagePerNode) {
        garbage = new Node(garbage, g)
        g += 1
      }
      sink = garbage
      kept = new Node(kept, i)
      i += 1
    }
    kept
  }

  private def length(list: Node): Int = {
    var n = 0
    var cursor = list
    while (cursor != null) {
      n += 1
      cursor = cursor.next
    }
    n
  }

  private def withIdleThreads[T](count: Int)(body: => T): T = {
    val ready = new CountDownLatch(count)
    val release = new CountDownLatch(1)
    val threads = Array.fill(count)(new Thread(() => {
      ready.countDown()
      release.await()
    }))
    threads.foreach(_.start())
    try {
      if (!ready.await(5, TimeUnit.SECONDS))
        throw new AssertionError("idle threads did not start")
      body
    } finally {
      release.countDown()
      threads.foreach(_.join(5000L))
    }
  }

  def main(args: Array[String]): Unit = {
    args(0) match {
      case "idle" =>
        val threads = args(1).toInt
        val nodes = args(2).toInt
        val list = withIdleThreads(threads)(build(nodes))
        val found = length(list)
        if (found != nodes)
          throw new AssertionError(
            s"kept list has $found nodes, expected $nodes"
          )

      case "medium" =>
        // Live data mostly 8-32 KiB arrays, one in three kept, filling about
        // half of the heap cap: collections leave large superblocks with one
        // survivor and free chunks around it. That free space is still
        // available and must not be mistaken for exhaustion.
        val liveBytes = args(1).toLong
        val sizes = Array(8192, 12288, 16384, 24576, 32768)
        val slots = (liveBytes / 18432L).toInt
        val live = new Array[Array[Byte]](slots)
        var i = 0
        while (i < slots * 12) {
          val data = new Array[Byte](sizes(i % sizes.length))
          data(0) = i.toByte
          if (i % 3 == 0) live((i / 3) % slots) = data
          else sink = data
          i += 1
        }
        if (live.exists(_ == null))
          throw new AssertionError("a live array went missing")

      case "full" =>
        // A heap that stays nearly full and is still valid: 1 MiB live arrays
        // leaving under 2% of the cap free, and a loop of short-lived
        // allocations. Collections free little each time but cost little,
        // so this must run to the end rather than report exhaustion.
        // Just under 1 MiB, so that each chunk and its header fill exactly
        // 32 blocks and leave no free tail behind.
        val chunk = (1 << 20) - 64
        val live = Array.fill(args(1).toInt / (1 << 20))(new Array[Byte](chunk))
        live.foreach(c => c(chunk - 1) = 1)
        var i = 0
        var acc = 0L
        while (i < args(2).toInt) {
          val tmp = new Array[Long](4)
          tmp(0) = i
          acc += tmp(0)
          i += 1
        }
        sink = live
        if (acc < 0) throw new AssertionError("unreachable")

      case "exhaust" =>
        try {
          sink = build(Int.MaxValue)
          throw new AssertionError("the heap never ran out")
        } catch {
          case _: OutOfMemoryError => sink = null
        }
    }
    println(s"gc-idle-mutator-recycling: ${args.mkString(" ")} OK")
  }
}

import java.lang.ref.{ReferenceQueue, SoftReference}

object Main {
  // GC_MAXIMUM_HEAP_SIZE set by the test runner
  private final val MaxHeapSize = 64 * 1024 * 1024
  // Softly reachable arrays take most of the heap
  private final val SoftChunkSize = 1024 * 1024
  private final val SoftCount = 40
  private final val ChunkSize = 64 * 1024
  // More than the heap can hold
  private final val RetainedSlots = 2 * MaxHeapSize / ChunkSize

  type Chunk = Array[Byte]

  @volatile private var sink: AnyRef = null

  private def check(condition: Boolean, message: String): Unit =
    if (!condition) throw new AssertionError(message)

  @noinline private def allocSoftRefs(
      queue: ReferenceQueue[Chunk]
  ): Array[SoftReference[Chunk]] =
    Array.fill(SoftCount)(new SoftReference(new Chunk(SoftChunkSize), queue))

  // Overwrites stale stack slots that could keep referents alive
  @noinline private def clobberStack(n: Int): Int =
    if (n > 0) clobberStack(n - 1) + 1 else 0

  private def softlyReachable(refs: Array[SoftReference[Chunk]]): Int =
    refs.count(_.get() != null)

  // Short-lived garbage: many collections, none of them short of memory
  @noinline private def churn(): Unit = {
    var i = 0
    while (i < 4 * MaxHeapSize / ChunkSize) {
      sink = new Chunk(ChunkSize)
      i += 1
    }
    sink = null
  }

  // Returns how many chunks were allocated before running out of memory
  @noinline private def exhaustHeap(retained: Array[AnyRef]): Int = {
    var index = 0
    try {
      while (index < retained.length) {
        retained(index) = new Chunk(ChunkSize)
        index += 1
      }
      throw new AssertionError(
        s"heap was not exhausted after $RetainedSlots allocations"
      )
    } catch {
      case _: OutOfMemoryError => index
    }
  }

  def main(args: Array[String]): Unit = {
    val queue = new ReferenceQueue[Chunk]()
    val refs = allocSoftRefs(queue)
    clobberStack(10000)

    // Collections that do not run out of memory keep softly reachable objects
    for (_ <- 0 until 3) System.gc()
    churn()
    System.gc()
    check(
      softlyReachable(refs) == SoftCount,
      s"only ${softlyReachable(refs)} of $SoftCount soft referents kept " +
        "while memory was available"
    )
    check(queue.poll() == null, "a soft reference was enqueued too early")

    // Soft references are cleared before an OutOfMemoryError
    val retained = new Array[AnyRef](RetainedSlots)
    val allocated = exhaustHeap(retained)
    check(
      softlyReachable(refs) == 0,
      s"${softlyReachable(refs)} soft referents kept after running out of memory"
    )
    // Without clearing, no more than the heap left by the soft referents
    val retainedBytes = allocated.toLong * ChunkSize
    val freeWithoutClearing = MaxHeapSize.toLong - SoftCount * SoftChunkSize
    check(
      retainedBytes > freeWithoutClearing + 8 * 1024 * 1024,
      s"retained only $retainedBytes bytes, the soft referents were not reused"
    )
    java.util.Arrays.fill(retained, null)
    System.gc()

    // Cleared soft references are enqueued
    val deadline = System.currentTimeMillis() + 30 * 1000L
    var enqueued = Set.empty[AnyRef]
    while (enqueued.size < SoftCount && System.currentTimeMillis() < deadline) {
      val ref = queue.remove(500)
      if (ref != null) enqueued += ref
    }
    check(
      enqueued.size == SoftCount,
      s"only ${enqueued.size} of $SoftCount soft references enqueued"
    )
    check(enqueued == refs.toSet[AnyRef], "unexpected reference enqueued")

    println("gc-soft-references: OK")
  }
}

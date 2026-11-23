import java.lang.ref.{ReferenceQueue, SoftReference, WeakReference}
import java.util.concurrent.{CountDownLatch, TimeUnit}

object Main {
  /* Larger than the maximal heap size configured by the test runner, so the
   * allocation can never be satisfied, no matter how far the heap grows. The
   * heap itself stays almost empty, which keeps the reporting of the error
   * (including symbolication of its stack trace) affordable.
   */
  private final val Oversized = 512 * 1024 * 1024
  private final val ChunkSize = 64 * 1024
  private final val RetainedSlots = 8192
  private final val ConcurrentAllocators = 4

  @volatile private var sink: AnyRef = null

  private def check(condition: Boolean, message: String): Unit =
    if (!condition) throw new AssertionError(message)

  private def allocateOversized(): OutOfMemoryError =
    try {
      sink = new Array[Byte](Oversized)
      throw new AssertionError(s"allocation of $Oversized bytes succeeded")
    } catch {
      case error: OutOfMemoryError => error
    }

  /* Arrays of 4 GiB and more: their sizes do not fit in 32 bits, and must not
   * be cut down to an allocation that does.
   */
  private def allocateBeyond32Bits(): Unit = {
    for (length <- Seq(1 << 29, (1 << 29) + 1, Int.MaxValue)) {
      try {
        sink = new Array[Long](length)
        throw new AssertionError(s"allocation of $length longs succeeded")
      } catch {
        case _: OutOfMemoryError => ()
      }
    }
    try {
      sink = new Array[AnyRef](Int.MaxValue)
      throw new AssertionError(
        "allocation of Int.MaxValue references succeeded"
      )
    } catch {
      case _: OutOfMemoryError => ()
    }
  }

  // The method name is mangled for private methods, match on the suffix.
  private def hasAllocationFrame(error: OutOfMemoryError): Boolean =
    error
      .getStackTrace()
      .exists(_.getMethodName().endsWith("allocateOversized"))

  /* Keeps allocating until the managed heap runs out. The stack trace of the
   * reported error is deliberately not inspected: symbolicating it needs more
   * memory than the exhausted heap can hand out. Returns the number of
   * chunks allocated.
   */
  private def exhaustHeap(retained: Array[AnyRef]): Int = {
    var index = 0
    try {
      while (index < retained.length) {
        retained(index) = new Array[Byte](ChunkSize)
        index += 1
      }
      throw new AssertionError(
        s"heap was not exhausted after $RetainedSlots allocations"
      )
    } catch {
      case _: OutOfMemoryError => index
    }
  }

  private def release(retained: Array[AnyRef]): Unit = {
    var index = 0
    while (index < retained.length) {
      retained(index) = null
      index += 1
    }
    System.gc()
  }

  private final class Allocator(ready: CountDownLatch, start: CountDownLatch)
      extends Thread {
    @volatile var error: OutOfMemoryError = null

    override def run(): Unit = {
      ready.countDown()
      start.await()
      error = allocateOversized()
    }
  }

  private def concurrentErrors(): Array[OutOfMemoryError] = {
    val ready = new CountDownLatch(ConcurrentAllocators)
    val start = new CountDownLatch(1)
    val allocators =
      Array.fill(ConcurrentAllocators)(new Allocator(ready, start))
    allocators.foreach(_.start())
    check(ready.await(30, TimeUnit.SECONDS), "allocator threads did not start")
    start.countDown()
    allocators.foreach(_.join(30000L))
    allocators.map { allocator =>
      check(!allocator.isAlive(), "allocator thread did not finish in time")
      check(allocator.error != null, "allocator thread did not report an error")
      allocator.error
    }
  }

  // Soft referents take 40 of the 64 MiB heap
  private final val SoftCount = 40
  private final val SoftSize = 1024 * 1024

  @noinline private def allocSoftRefs(
      queue: ReferenceQueue[AnyRef]
  ): Array[SoftReference[AnyRef]] =
    Array.fill(SoftCount)(new SoftReference(new Array[Byte](SoftSize), queue))

  @noinline private def clobberStack(n: Int): Int =
    if (n > 0) clobberStack(n - 1) + 1 else 0

  /* Soft referents survive collections while memory is available, and are
   * cleared and enqueued before an OutOfMemoryError.
   */
  private def softReferencesClearedBeforeError(): Unit = {
    val queue = new ReferenceQueue[AnyRef]()
    val refs = allocSoftRefs(queue)
    clobberStack(1000)
    for (_ <- 0 until RetainedSlots) sink = new Array[Byte](ChunkSize)
    sink = null
    System.gc()
    check(refs.forall(_.get() != null), "soft referent cleared too early")

    val retained = new Array[AnyRef](RetainedSlots)
    val allocated = exhaustHeap(retained)
    check(refs.forall(_.get() == null), "soft referent kept until the error")
    // More than fits beside the soft referents in the 64 MiB heap
    val besideSoft = 64L * 1024 * 1024 - SoftCount.toLong * SoftSize
    check(allocated.toLong * ChunkSize > besideSoft, "heap not reused")
    release(retained)

    val deadline = System.currentTimeMillis() + 30000L
    var enqueued = Set.empty[AnyRef]
    while (enqueued.size < SoftCount && System.currentTimeMillis() < deadline)
      Option(queue.remove(500)).foreach(enqueued += _)
    check(enqueued == refs.toSet[AnyRef], "soft references not enqueued")
  }

  // Also before an error for an allocation larger than the whole heap
  private def softReferencesClearedBeforeOversizedError(): Unit = {
    val refs = allocSoftRefs(null)
    clobberStack(1000)
    allocateOversized()
    // A stale pointer left in a register or on the stack (seen on Windows)
    // keeps a referent strongly reachable; without clearing all are kept
    val kept = refs.count(_.get() != null)
    check(kept < SoftCount / 2, s"$kept soft referents kept, oversized")
  }

  /* References cleared or enqueued by the program are dropped by the
   * registry without a collection clearing referents; otherwise these fill
   * the 64 MiB heap.
   */
  private def retiredReferencesReleased(): Unit = {
    val queue = new ReferenceQueue[AnyRef]()
    val referent = new Object
    for (i <- 0 until 4000000) {
      val ref = new WeakReference[AnyRef](referent, queue)
      if (i % 2 == 0) ref.clear()
      else check(ref.enqueue() && (queue.poll() eq ref), "enqueue failed")
    }
    check(referent != null, "referent")
  }

  def main(args: Array[String]): Unit = {
    softReferencesClearedBeforeError()
    softReferencesClearedBeforeOversizedError()

    // A failed allocation reports an OutOfMemoryError instead of aborting the
    // process, and the reserve kept by the GC leaves enough room to record the
    // stack of the allocation that failed.
    val first = allocateOversized()
    check(hasAllocationFrame(first), "first error did not retain its stack")

    // The reserve is refilled by the collection that follows, so the next
    // failure is reported the same way instead of degrading to the fallback.
    val second = allocateOversized()
    check(!(first eq second), "second error reused the first instance")
    check(hasAllocationFrame(second), "refilled reserve lost the stack")

    allocateBeyond32Bits()

    // Running out of the managed heap is recoverable too: the error unwinds to
    // the mutator, and once the retained objects are dropped the program keeps
    // allocating normally.
    val retained = new Array[AnyRef](RetainedSlots)
    exhaustHeap(retained)
    release(retained)
    check(
      hasAllocationFrame(allocateOversized()),
      "error after releasing the heap did not retain its stack"
    )

    // Only one thread at a time can take the reserve. The others fall back to
    // the preallocated error, which is shared and carries no stack trace.
    val errors = concurrentErrors()
    val fallbacks = errors.filter(_.getStackTrace().isEmpty)
    check(
      fallbacks.headOption.forall(shared => fallbacks.forall(_ eq shared)),
      "concurrent OOM used more than one fallback instance"
    )
    errors.filter(_.getStackTrace().nonEmpty).foreach { error =>
      check(
        hasAllocationFrame(error),
        "concurrent OOM reported an unrelated stack"
      )
    }

    retiredReferencesReleased()

    println("gc-out-of-memory-error: OK")
  }
}

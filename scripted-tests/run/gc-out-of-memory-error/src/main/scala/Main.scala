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

  // The method name is mangled for private methods, match on the suffix.
  private def hasAllocationFrame(error: OutOfMemoryError): Boolean =
    error
      .getStackTrace()
      .exists(_.getMethodName().endsWith("allocateOversized"))

  /* Keeps allocating until the managed heap runs out. The stack trace of the
   * reported error is deliberately not inspected: symbolicating it needs more
   * memory than the exhausted heap can hand out.
   */
  private def exhaustHeap(retained: Array[AnyRef]): Unit =
    try {
      var index = 0
      while (index < retained.length) {
        retained(index) = new Array[Byte](ChunkSize)
        index += 1
      }
      throw new AssertionError(
        s"heap was not exhausted after $RetainedSlots allocations"
      )
    } catch {
      case _: OutOfMemoryError => ()
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

  def main(args: Array[String]): Unit = {
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

    println("gc-out-of-memory-error: OK")
  }
}

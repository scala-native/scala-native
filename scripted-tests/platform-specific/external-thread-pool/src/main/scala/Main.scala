import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._

@extern object pool {
  type Task = CFuncPtr1[Ptr[Byte], Unit]
  @name("test_pool_create")
  def create(size: CInt): Ptr[Byte] = extern
  @name("test_pool_submit")
  def submit(pool: Ptr[Byte], task: Task, context: Ptr[Byte]): Unit = extern
  @name("test_pool_destroy") @blocking
  def destroy(pool: Ptr[Byte]): Unit = extern
}

/** Scala callbacks invoked on the threads of a native thread pool, allocating
 *  and collecting concurrently.
 */
object Main {
  val completed = new AtomicLong()
  val failures = new AtomicLong()
  val threads = ConcurrentHashMap.newKeySet[Thread]()
  val threadLocal = new ThreadLocal[Array[Byte]]()
  val threadLocals = new AtomicLong()
  // Scales the work down for a collector which never frees memory.
  var load = 10

  def context(value: Long): Ptr[Byte] =
    fromRawPtr[Byte](Intrinsics.castLongToRawPtr(value))

  def allocate(seed: Long): Long = {
    var total = 0L
    var retained: List[Array[Byte]] = Nil
    var i = 0
    while (i < 500) {
      retained = new Array[Byte](256 + (i & 255)) :: retained
      if (i % 64 == 0) retained = retained.take(8)
      total += retained.head.length + seed.toString.length
      i += 1
    }
    total
  }

  val work: pool.Task = CFuncPtr1.fromScalaFunction { (ctx: Ptr[Byte]) =>
    threads.add(Thread.currentThread())
    if (threadLocal.get() == null) {
      threadLocal.set(new Array[Byte](4096))
      threadLocals.incrementAndGet()
    }
    if (allocate(ctx.toLong) <= 0) failures.incrementAndGet()
    if (ctx.toLong % 97 == 0) System.gc()
    completed.incrementAndGet()
    ()
  }

  /** Workers are reused, each keeps its Scala identity and thread locals. */
  def reuse(): Unit = {
    val size = 8
    val tasks = 2000 * load
    val workers = pool.create(size)
    var i = 0
    while (i < tasks) {
      pool.submit(workers, work, context(i + 1)) // null has no address
      if (i % 500 == 0) allocate(i)
      i += 1
    }
    pool.destroy(workers)
    assert(failures.get() == 0)
    assert(completed.get() == tasks, s"completed ${completed.get()}")
    assert(threads.size() <= size, s"${threads.size()} identities")
    assert(threadLocals.get() == threads.size())
    System.gc()
  }

  @volatile var collecting = true
  val collect: pool.Task = CFuncPtr1.fromScalaFunction { (_: Ptr[Byte]) =>
    while (collecting) {
      // Back to back collections would starve the workers.
      if (load > 1) allocate(1)
      System.gc()
    }
  }

  /** Workers exit while another one collects. */
  def churn(): Unit = {
    val rounds = 30 * load
    val collector = pool.create(1)
    pool.submit(collector, collect, null)
    var round = 0
    while (round < rounds) {
      val workers = pool.create(4)
      var i = 0
      while (i < 16) {
        pool.submit(workers, work, context(i + 1))
        i += 1
      }
      pool.destroy(workers)
      round += 1
    }
    collecting = false
    pool.destroy(collector)
    assert(failures.get() == 0)
    assert(completed.get() == rounds * 16, s"completed ${completed.get()}")
  }

  def main(args: Array[String]): Unit = {
    if (args.length > 1) load = args(1).toInt
    args.headOption.getOrElse("") match {
      case "reuse" => reuse()
      case "churn" => churn()
      case other   =>
        println(s"Unknown scenario: $other")
        System.exit(1)
    }
  }
}

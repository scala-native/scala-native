import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.{CountDownLatch, TimeUnit}

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe._

@extern
object AllocationTest {
  def arm(): Unit = extern
  def checkHandoff(): CInt = extern
  def checkStaleGeneration(): CInt = extern
  def checkRecycledCapacity(): CInt = extern
  def checkReservation(): CInt = extern
}

object Main {
  def checks(): Unit = {
    assert(
      AllocationTest.checkStaleGeneration() == 1,
      "stale collection request"
    )
    assert(AllocationTest.checkRecycledCapacity() == 1, "stranded capacity")
    assert(AllocationTest.checkReservation() == 1, "reservation was stolen")
  }

  def workload(count: Int, handoff: Boolean): Unit = {
    val start = new CountDownLatch(1)
    val stop = new AtomicBoolean(false)
    val failure = new AtomicReference[Throwable]()
    val workers = Array.tabulate(count) { id =>
      new Thread(() => {
        try {
          val live = new Array[Array[String]](512)
          start.await()
          var i = 0
          while (i < 1024 || !stop.get()) {
            live(i % live.length) = Array.tabulate(64)(j => s"worker-$id-$i-$j")
            i += 1
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
    if (handoff) AllocationTest.arm()
    start.countDown()
    try {
      Thread.sleep(if (handoff) 2000L else 5000L)
      val formatted =
        f"${scala.scalanative.runtime.GC.getUsedHeapSize().toLong / (1024.0 * 1024)}%.1f"
      assert(formatted.nonEmpty)
    } finally {
      stop.set(true)
      workers.foreach { worker =>
        worker.join(30000L)
        assert(!worker.isAlive(), "allocation worker did not finish")
      }
    }
    if (failure.get() != null) throw failure.get()
    if (handoff) assert(AllocationTest.checkHandoff() == 1, "collector handoff")
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
      workload(1, false)
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
        case "checks"  => checks()
        case "handoff" => workload(16, true)
        case "stress"  => workload(16, false)
        case "idle"    => idle()
      }
    } else {
      assert(args(0) == "checks")
      checks()
    }
    println(s"gc-immix-concurrent-allocation: ${args(0)} OK")
  }
}

package scala.scalanative.runtime.gc

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

import org.junit.Assert._
import org.junit.{Assume, Test}

import scala.scalanative.junit.utils.AssumesHelper

class IssueTests {

  // Commix GC crash on huge ref array
  @Test def issue4445(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    AssumesHelper.assumeNot32Bit()

    val size = 0x01ffffff
    val arr = new Array[AnyRef](size)
    val stride = 65536
    var i = 0
    while (i < size) {
      arr(i) = "foo"
      i += stride
    }
    arr(size / 2) = "bar"
    arr(size - 1) = "baz"
    System.gc()
    assertEquals("bar", arr(size / 2))
    assertEquals("baz", arr(size - 1))
  }

  // SIGSEGV at 0x10 when LargeAllocator_Alloc returns NULL under multithreaded
  // allocation. The bug only manifests on Immix/Commix with multithreading,
  // but the workload is safe on every GC so we only need the MT guard. With
  // the fix in place this completes cleanly; without it the inlined
  // scalanative_GC_alloc_array call faults at offset 0x10.
  @Test def issue4916(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()

    // Capped so the workload stays bounded on high-core CI runners.
    val nThreads = math.min(Runtime.getRuntime().availableProcessors() * 2, 16)
    val perThread = 2000
    // 4100 * 2 bytes/char > LARGE_BLOCK_SIZE (8K), forcing the
    // LargeAllocator path that the bug lives in.
    val minChars = 4100
    val maxChars = 16384

    val done = new AtomicBoolean(false)
    val total = new AtomicLong(0L)
    implicit val ec: ExecutionContext = ExecutionContext.global

    val futures = (0 until nThreads).map { t =>
      Future {
        var rng = (t * 0xdeadbeefL ^ System.nanoTime()).toInt
        var i = 0
        while (i < perThread && !done.get()) {
          rng = rng * 1103515245 + 12345
          val len = minChars + (math.abs(rng) % (maxChars - minChars + 1))
          val a = new Array[Char](len)
          // Touch the header-adjacent slots so the writes that previously
          // SIGSEGV'd cannot be optimized away.
          a(0) = (i & 0xffff).toChar
          a(len - 1) = ((i >>> 16) & 0xffff).toChar
          total.incrementAndGet()
          i += 1
        }
      }
    }

    try Await.result(Future.sequence(futures), 60.seconds)
    catch {
      case _: java.util.concurrent.TimeoutException =>
        done.set(true)
        fail("issue4916 stress timed out after 60s")
    }
    assertEquals(nThreads.toLong * perThread, total.get())
  }
}

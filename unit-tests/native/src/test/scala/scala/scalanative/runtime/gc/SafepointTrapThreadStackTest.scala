package scala.scalanative.runtime.gc

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.signal._
import scala.scalanative.posix.signalOps._
import scala.scalanative.unsafe._

/* Regression for #5393: with no sigaltstack the kernel puts the trap's signal
 * frame right below the interrupted stack's red zone. The safepoint redirect
 * must not write there, or sigreturn fails (SIGILL in _sigtramp on x86_64
 * macOS) or the thread hangs. Threads normally have an alternate signal stack
 * (stack overflow guards), which hid the bug except while a thread exits.
 */
class SafepointTrapThreadStackTest {
  import YieldpointStressTest.{Control, spin}

  @Test def yieldpointTrapsWithoutAltStack(): Unit =
    if (!LinktimeInfo.isWindows) {
      AssumesHelper.assumeMultithreadingIsEnabled()
      assumeTrue(
        "requires Immix or Commix",
        LinktimeInfo.gc.isImmix || LinktimeInfo.gc.isCommix
      )
      val control = new Control
      val failure = new AtomicReference[Throwable](null)
      val started = new AtomicInteger(0)
      val finished = new AtomicInteger(0)

      def spinWithoutAltStack(): Unit = Zone.acquire { implicit z =>
        val previous = alloc[stack_t]()
        val disabled = alloc[stack_t]()
        assertEquals("sigaltstack query", 0, sigaltstack(null, previous))
        // Darwin rejects SS_DISABLE with ENOMEM unless ss_size >= MINSIGSTKSZ
        disabled.ss_sp = previous.ss_sp
        disabled.ss_size = previous.ss_size
        disabled.ss_flags = SS_DISABLE
        if ((previous.ss_flags & SS_DISABLE) == 0)
          assertEquals("sigaltstack disable", 0, sigaltstack(disabled, null))
        started.incrementAndGet()
        try spin(control)
        finally {
          if ((previous.ss_flags & SS_DISABLE) == 0) sigaltstack(previous, null)
        }
      }

      val threads: Seq[Thread] = Seq.fill(4) {
        val t = new Thread(new Runnable {
          def run(): Unit =
            try spinWithoutAltStack()
            catch { case e: Throwable => failure.compareAndSet(null, e) }
            finally finished.incrementAndGet()
        })
        t.setDaemon(true)
        t.start()
        t
      }

      while (started.get() < threads.size && failure.get() == null)
        Thread.`yield`()
      val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(500)
      var collections = 0
      while (System.nanoTime() < deadline && failure.get() == null) {
        System.gc()
        collections += 1
      }
      control.stop = true
      threads.foreach(_.join(TimeUnit.SECONDS.toMillis(30)))

      if (failure.get() != null) throw failure.get()
      assertEquals("threads finished", threads.size, finished.get())
      assertTrue("collections", collections > 0)
    }
}

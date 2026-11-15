package scala.scalanative.runtime

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

// `Array` alone is scala.scalanative.runtime.Array in this package.
import scala.Array
import scala.annotation.nowarn
import scala.util.Try

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.annotation.nooptimize
import scala.scalanative.meta.LinktimeInfo.{
  isContinuationsSupported, isMultithreadingEnabled
}
import scala.scalanative.unsafe.{CFuncPtr2, CUnsignedLong, Ptr, stackalloc}

import Continuations._

/** One-shot continuations, `Continuations.single`. */
class ContinuationsTest extends ContinuationsApiTest:
  protected val api: Continuations.Api =
    if isContinuationsSupported then Continuations.single else null

  @Test def resumingTwiceThrows(): Unit =
    if isContinuationsSupported then
      var resume: Int => Int = null
      val first = boundary[Int] {
        suspend[Int, Int] { k => resume = k; -1 } + 1
      }
      assertEquals(-1, first)
      assertEquals(2, resume(1))
      assertThrows(classOf[IllegalStateException], () => resume(1))

  @noinline private def suspendForLater(
      store: (Unit => Long) => Unit
  )(using BoundaryLabel[Long]): Unit =
    suspend[Unit, Long] { k => store(k); -1L }

  // A frame with `stackalloc` has a frame pointer and holds addresses of its
  // own stack.
  @noinline private def stackallocAcrossSuspend(
      store: (Unit => Long) => Unit
  )(using BoundaryLabel[Long]): Long =
    val slot = stackalloc[Long]()
    !slot = 1000L
    suspendForLater(store)
    !slot = !slot + 10L
    !slot

  @noinline private def resumeDeeper(depth: Int, resume: Unit => Long): Long =
    if depth == 0 then resume(())
    else resumeDeeper(depth - 1, resume) + depth - depth

  @Test def stackallocSurvivesResumeOnAnotherStack(): Unit =
    if isContinuationsSupported && isMultithreadingEnabled then
      var resume: Unit => Long = null
      def suspended(): Long =
        boundary[Long](stackallocAcrossSuspend(k => resume = k))

      assertEquals(-1L, suspended())
      assertEquals(1010L, resumeDeeper(16, resume))

      assertEquals(-1L, suspended())
      @volatile var result = 0L
      val thread = new Thread(() => result = resumeDeeper(4, resume))
      thread.start()
      thread.join()
      assertEquals(1010L, result)

  @noinline @nooptimize
  private def overflowStack(): Int = 1 + overflowStack()

  @Test def stackOverflowInsideBoundary(): Unit =
    if isContinuationsSupported then
      def overflows(): Unit = assertThrows(
        classOf[StackOverflowError],
        () => boundary[Int](overflowStack())
      )
      overflows()
      var resume: Unit => Int = null
      val first = boundary[Int] {
        suspend[Unit, Int] { k => resume = k; -1 }
        overflowStack()
      }
      assertEquals(-1, first)
      assertThrows(classOf[StackOverflowError], () => resume(()))
      // The guard page is armed again after an overflow.
      overflows()

  @Test def abandonedContinuationsReleaseTheirStacks(): Unit =
    if isContinuationsSupported then
      var i = 0
      while i < 20000 do
        boundary[Int] { suspend[Unit, Int](_ => i); 0 }
        i += 1

  // The suspended stack is the only holder of `kept` while the continuation
  // waits in the queue, and collections run meanwhile.
  @Test def suspendedStacksKeepObjectsAliveAcrossThreads(): Unit =
    if isContinuationsSupported && isMultithreadingEnabled then
      val producers = 4
      val perProducer = 5000
      val queue = new LinkedBlockingQueue[Unit => Long]()
      val errors = new AtomicInteger
      val resumed = new AtomicInteger
      val failure = new AtomicReference[java.lang.Throwable]()
      def worker(body: => Unit) = new Thread(() =>
        try body
        catch
          case _: InterruptedException => ()
          case ex: java.lang.Throwable =>
            failure.compareAndSet(null, ex); ()
      )
      val produce = (0 until producers).map { _ =>
        worker {
          var i = 0
          while i < perProducer do
            val base = i
            boundary[Long] {
              val kept = Array.tabulate(32)(j => Integer.valueOf(base + j))
              suspend[Unit, Long] { k => queue.put(k); 0L }
              var j = 0
              while j < kept.length do
                if kept(j).intValue != base + j then errors.incrementAndGet()
                j += 1
              0L
            }
            // dropped without being resumed
            if i % 7 == 0 then
              boundary[Long] { suspend[Unit, Long](_ => 0L); 0L }
            i += 1
        }
      }
      val consume = (0 until producers).map { _ =>
        worker {
          var n = 0
          while n < perProducer do
            queue.take()(())
            resumed.incrementAndGet()
            n += 1
        }
      }
      val threads = produce ++ consume
      threads.foreach(_.start())
      try
        val deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(2)
        threads.foreach { thread =>
          thread.join(1L.max((deadline - System.nanoTime()) / 1000000))
        }
        assertNull("worker failed", failure.get())
        assertFalse("workers did not finish", threads.exists(_.isAlive))
      finally threads.foreach(_.interrupt())
      assertEquals(producers * perProducer, resumed.get())
      assertEquals(0, errors.get())

  // What the inline `boundary` and `suspend` of 0.5.12 expanded to; libraries
  // compiled against it contain these calls.
  @nowarn("cat=deprecation")
  private def boundaryAsIn0_5_12[T](body: CUnsignedLong => T): T =
    val call: CUnsignedLong => Try[T] = label => Try(body(label))
    val run = Continuations.inline$boundaryBodyFnAny
      .asInstanceOf[CFuncPtr2[CUnsignedLong, CUnsignedLong => Try[T], Try[T]]]
    Continuations.inline$Impl.boundary(run, call).get

  @nowarn("cat=deprecation")
  private def suspendAsIn0_5_12[R, T](
      label: CUnsignedLong
  )(onSuspend: (R => T) => T): R =
    val continuation = new Continuations.Continuation[R, T]()
    val call: Ptr[?] => Try[T] = inner =>
      Continuations.inline$inner_$eq$i1(continuation, inner)
      Try(onSuspend(continuation))
    val run = Continuations.inline$suspendFnAny
      .asInstanceOf[CFuncPtr2[Ptr[?], Ptr[?] => Try[T], Try[T]]]
    try Continuations.inline$Impl.suspend(label, run, call, continuation)
    finally Continuations.inline$reachabilityFence(continuation)

  @Test def codeCompiledAgainst0_5_12StillRuns(): Unit =
    if isContinuationsSupported && isMultithreadingEnabled then
      var resume: Int => Int = null
      val first = boundaryAsIn0_5_12[Int] { label =>
        suspendAsIn0_5_12[Int, Int](label) { k => resume = k; -1 } + 1
      }
      assertEquals(-1, first)
      @volatile var result = 0
      val thread = new Thread(() => result = resume(41))
      thread.start()
      thread.join()
      assertEquals(42, result)
      assertThrows(classOf[IllegalStateException], () => resume(1))

end ContinuationsTest

package scala.scalanative.runtime

import scala.util.control.ControlThrowable

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.{
  is32BitPlatform, isContinuationsSupported
}

import Continuations._

class ContinuationsTest:
  @Test def canBoundaryNoSuspend() =
    if isContinuationsSupported then
      val res = boundary[Int] {
        val x = 1
        val y = 2
        x + y
      }
      assert(res == 3)

  @Test def canBoundarySuspend() =
    if isContinuationsSupported then
      val res = boundary[Int] {
        val x = 1
        suspend[Int](_ => x + 1)
        ???
      }
      assert(res == 2)

  @Test def canBoundarySuspendImmediateResume() =
    if isContinuationsSupported then
      val r = boundary[Int] {
        1 + suspend[Int, Int](r => r(2)) + suspend[Int, Int](r => r(3)) + 4
      }
      assert(r == 10)

  @Test def canBoundarySuspendCommunicate() =
    if isContinuationsSupported then
      case class Iter(n: Int, nx: Int => Iter)
      val r0 = boundary[Iter] {
        var r = 0
        while (true) {
          r += suspend[Int, Iter](cb => Iter(r, cb))
        }
        ???
      }
      assert(r0.n == 0)
      val r1 = r0.nx(2)
      assert(r1.n == 2)
      val r2 = r1.nx(3)
      assert(r2.n == 5)

  @Test def fibonacci(): Unit = {
    if isContinuationsSupported then
      import scala.collection.mutable.ArrayBuffer

      case class Seqnt[T, R](v: T, nx: R => Seqnt[T, R])
      type Seqn[T] = Seqnt[T, Int]

      def fib = boundary[Seqn[Int]] {
        // these get boxed, so it's not really working different from a generator
        var a = 1
        var b = 1
        while (true) {
          val steps = suspend[Int, Seqn[Int]](c => Seqnt(a, c))
          for (i <- 1 to steps) {
            val c = a + b
            a = b
            b = c
          }
        }
        Seqnt(0, ???)
      }

      val fibs = ArrayBuffer(fib)
      for (step <- 1 to 10) {
        fibs += fibs(step - 1).nx(1)
      }

      val fibList = fibs.map(_.v).toList
      assert(fibList == List(1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89))
  }

  @Test def basic(): Unit = {
    if isContinuationsSupported then
      enum Response[T] {
        case Next(nx: () => Response[T], v: T)
        case End(v: T)
      }
      import Response.*
      val oneThenTwo = boundary[Response[Int]] {
        suspend[Response[Int]](Next(_, 1))
        End(2)
      }

      oneThenTwo match
        case Next(nx, v) =>
          assert(v == 1)
          val v2 = nx()
          assert(v2 == End(2))
        case End(v) =>
          assert(false)
  }

  @Test def nestedResumePropagatesEscapingThrowable(): Unit =
    if isContinuationsSupported then {
      case object EscapesBoundary extends ControlThrowable
      @noinline def captureResumePoint(onResume: => Unit): () => Unit = {
        var resume: () => Unit = null
        boundary[Unit] {
          suspend[Unit] { k =>
            resume = k
          }
          onResume
        }

        if resume == null then
          fail("Failed to capture continuation resume handle")
        resume
      }

      val innerResume = captureResumePoint { () }
      val outerResume = captureResumePoint {
        innerResume()
        throw EscapesBoundary
      }

      try
        outerResume()
        fail("Expected EscapesBoundary to propagate from nested resume")
      catch case EscapesBoundary => ()
    }

  @Test def resumePreservesLocalsAcrossSuspendResume(): Unit = {
    var clobberSink: Int = 0

    @noinline def runOneContinuationCycle(i: Int): Int = {
      var resume: () => Int = null

      val first = boundary[Int] {
        val a = i + 11
        val b = i + 23
        val c = i + 37

        val token: Int = suspend[Int, Int] { k =>
          resume = () => k(123)
          -777
        }
        a + b + c + token
      }

      assertEquals(-777, first)
      clobberSink ^= clobberStackFrames(i)

      val resumed = resume()
      val expected = (i + 11) + (i + 23) + (i + 37) + 123
      assertEquals(expected, resumed)
      resumed
    }

    @noinline def clobberStackFrames(seed: Int): Int =
      clobberStackFramesRec(48, seed)

    @noinline def clobberStackFramesRec(depth: Int, in: Int): Int = {
      val a0 = in + 1
      val a1 = in + 2
      val a2 = in + 3
      val a3 = in + 4
      val a4 = in + 5
      val a5 = in + 6
      val a6 = in + 7
      val a7 = in + 8
      val sum = a0 + a1 + a2 + a3 + a4 + a5 + a6 + a7

      if depth == 0 then sum
      else clobberStackFramesRec(depth - 1, sum ^ depth) + (depth & 1)
    }

    if isContinuationsSupported then {
      var checksum = 0L
      var i = 0

      while i < 5000 do
        checksum += runOneContinuationCycle(i)
        i += 1

      val n = 5000L
      val expectedChecksum = (3L * n * (n - 1) / 2L) + (194L * n)
      assertEquals(expectedChecksum, checksum)
    }
  }

end ContinuationsTest

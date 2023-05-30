package scala

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.runtime.Continuations._

class Scala3ContinuationsTests:
  @Test def canBoundaryNoSuspend() =
    val res = boundary[Int] {
      val x = 1
      val y = 2
      x + y
    }
    assert(res == 3)
  @Test def canBoundarySuspend() =
    val res = boundary[Int] {
      val x = 1
      suspend[Int](_ => x + 1)
      ???
    }
    assert(res == 2)
  @Test def canBoundarySuspendImmediateResume() =
    val r = boundary[Int] {
      1 + suspend[Int, Int](r => r(2)) + suspend[Int, Int](r => r(3)) + 4
    }
    assert(r == 10)
  @Test def canBoundarySuspendCommunicate() =
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

  @Test def fibonacci(): Unit =
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

  @Test def basic(): Unit =
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

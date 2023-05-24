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

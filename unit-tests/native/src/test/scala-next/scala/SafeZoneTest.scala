package org.scalanative

import org.junit.Test
import org.junit.Assert._
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scala.language.experimental.captureChecking
import scala.scalanative.runtime.SafeZoneAllocator.allocate
import scala.scalanative.memory.SafeZone
import scala.scalanative.memory.SafeZone._
import scala.util.{Try,Success,Failure}

class SafeZoneTest {
  @Test def `correctly open and close a safe zone`(): Unit = {
    SafeZone { sz ?=> 
      assertTrue(sz.isOpen)
      assertFalse(sz.isClosed)
    }
  }

  /** The followings are instances allocation tests. The syntax of allocating an
   *  instance in the safe zone `sz` is `new T(...)^{sz}`, and it's translated
   *  to internal function call `allocate(sz, new T(...))` by dotty in typer
   *  phase. Instead of testing the syntax directly, here we test the internal
   *  function equaliventlly.
   */

  @Test def `allocate instances in nested safe zones`(): Unit = {
    case class A(v: Int)
    SafeZone { sz0 ?=>
      val a = SafeZone { sz1 ?=>
        val a0 = allocate(sz0, new A(0))
        val a1 = allocate(sz1, new A(1))
        a0
      }
      assertEquals(a.v, 0)
    }
  }

  @Test def `allocate instances with members in different memory areas`(): Unit = {
    case class A()
    case class B(a: A^)
    SafeZone { sz0 ?=>
      SafeZone { sz1 ?=> 
        val aInSz0 = allocate(sz0, new A())
        val aInHeap = new A()
        val b0: B^{sz1, sz0} = allocate(sz1, new B(aInSz0))
        val b1: B^{sz1} = allocate(sz1, new B(aInHeap))
        val b2: B^{sz0} = new B(aInSz0)
        val b3: B = new B(aInHeap)
      }
    }
  }

  @Test def `arrays with elements in different memory areas`(): Unit = {
    case class A()
    SafeZone { sz0 ?=>
      SafeZone { sz1 ?=> 
        val aInSz0 = allocate(sz0, new A())
        val aInHeap = new A()
        val arr0: Array[A^{sz0}]^{sz1} = allocate(sz1, new Array[A^{sz0}](1))
        arr0(0)  = aInSz0
        val arr1: Array[A]^{sz1} = allocate(sz1, new Array[A](1))
        arr1(0) = aInHeap
      }
    }
  }

  @Test def `objects allocated in safe zone is accessible`(): Unit = {
    def assertAccessible(n: Int): Unit = {
      case class A(v: Int)
      SafeZone { sz ?=> 
        val ary = new Array[A^{sz}](n)
        for i <- 0 until n do
          ary(i) = allocate(sz, new A(i))
        var sum = 0
        for i <- n - 1 to 0 by -1 do
          sum += ary(i).v
        assertTrue(sum == (0 until n).sum)
      }
    }
    assertAccessible(10)
  }

  @Test def `allocate a large object in safe zone`(): Unit = {
    case class DoubleWrapper(value: Double)
    SafeZone { sz ?=>
      val kArraySize = 500000
      val array = allocate(sz, new Array[DoubleWrapper^{sz}](kArraySize))
      var i = 0
      while (i < kArraySize / 2) {
        array(i) = allocate(sz, new DoubleWrapper(1.0 / i))
        i += 1
      }
      assertTrue(array(1000).value == 1.0 / 1000)
    }
  }

  @Test def `can use alloc API instead of instance creation expression`(): Unit = {
    case class A(v: Int)
    SafeZone { sz ?=>
      // Using explicit zone.
      val a0: A^{sz} = sz alloc(new A(0))
      val a1: A^{sz}= sz alloc new A(1)
      // Using implicit zone.
      val a2: A^{sz} = alloc(new A(2))
      assertTrue(a0.v + a1.v + a2.v == 3)
    }
    SafeZone {
      // Using implicit zone.
      val a2 = alloc(new A(2))
      // Summon the zone to make it explicit.
      val sz = summon[SafeZone]
      val a0: A^{sz} = sz alloc(new A(0))
      val a1: A^{sz} = sz alloc new A(1)
      assertTrue(a0.v + a1.v + a2.v == 3)
    }
  }

  @Test def `can use the zone API to summon implicit zone`(): Unit = {
    case class A(v: Int)
    SafeZone { 
      val a0: A^{zone} = zone alloc(new A(0))
      val a1: A^{zone} = zone alloc new A(1)
      assertTrue(a0.v + a1.v == 1)
    }
  }
}

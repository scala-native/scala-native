package scala.scalanative.libc
import org.junit.Assert._
import org.junit.Test

import scala.scalanative.libc.inttypes._
import scala.scalanative.unsafe._
class IntTypesTest {
  @Test def testImaxabs(): Unit = {
    val ns = List.fill(100)(scala.util.Random.nextInt(1000) - 500)
    ns.foreach { n =>
      assertEquals(
        imaxabs(n),
        imaxabs(-n)
      )
      assertTrue(
        imaxabs(n) >= n && imaxabs(n) >= -n
      )
      assertTrue(
        imaxabs(n) == n || imaxabs(n) == -n
      )
    }
  }
  @Test def testImaxdiv(): Unit = {
    val res = stackalloc[imaxdiv_t]()
    imaxdiv(45, 7, res)
    assertEquals(6, res._1)
    assertEquals(3, res._2)
  }
  @Test def testStrtoimax(): Unit = {
    Zone.acquire { implicit z =>
      val nptr = toCString("10345134932abc")
      val res = strtoimax(nptr, null, 10)
      assertEquals(res, 10345134932L)
    }
  }
  @Test def testStrtoumax(): Unit = {
    Zone.acquire { implicit z =>
      val res = strtoimax(toCString("10345134932abc"), null, 10)
      assertEquals(res, 10345134932L)
    }
  }
}

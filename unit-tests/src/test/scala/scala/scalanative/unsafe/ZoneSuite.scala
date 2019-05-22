package scala.scalanative
package unsafe

object ZoneSuite extends tests.Suite {
  private def assertAccessible(bptr: Ptr[_], n: Int) {
    val ptr = bptr.asInstanceOf[Ptr[Int]]
    var i   = 0
    while (i < n) {
      ptr(i) = i
      i += 1
    }

    i = 0
    var sum = 0
    while (i < n) {
      sum += ptr(i)
      i += 1
    }

    assert(sum == (0 until n).sum)
  }

  test("zone allocator alloc with apply") {
    Zone { implicit z =>
      val ptr = z.alloc(64 * sizeof[Int])

      assertAccessible(ptr, 64)

      val ptr2 = alloc[Int](128)

      assertAccessible(ptr2, 128)
    }
  }

  test("zone allocator alloc with open") {
    implicit val zone: Zone = Zone.open()
    assert(zone.isOpen)
    assert(!zone.isClosed)

    val ptr = zone.alloc(64 * sizeof[Int])

    assertAccessible(ptr, 64)

    val ptr2 = alloc[Int](128)

    assertAccessible(ptr2, 128)

    zone.close()
    assert(!zone.isOpen)
    assert(zone.isClosed)
  }

  test("alloc throws exception if zone allocator is closed") {
    implicit val zone: Zone = Zone.open()

    zone.alloc(64 * sizeof[Int])

    zone.close()

    assertThrows[IllegalStateException](zone.alloc(64 * sizeof[Int]))
  }
}

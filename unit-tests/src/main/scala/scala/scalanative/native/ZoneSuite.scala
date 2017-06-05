package scala.scalanative.native

object ZoneSuite extends tests.Suite {
  private def assertAccessible(bptr: Ptr[_], n: Int) {
    val ptr = bptr.cast[Ptr[Int]]
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

  test("zone allocator alloc") {
    Zone { implicit z =>
      val ptr = z.alloc(64 * sizeof[Int])

      assertAccessible(ptr, 64)

      val ptr2 = alloc[Int](128)

      assertAccessible(ptr2, 128)
    }
  }
}

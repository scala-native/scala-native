package scala.scalanative.native

object AllocSuite extends tests.Suite {
  def assertAccessible(bptr: Ptr[_], n: Int) {
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

  test("system allocator malloc/realloc/free") {
    val alloc = Alloc.system
    val ptr   = alloc.alloc(64 * sizeof[Int])

    assertAccessible(ptr, 64)

    val ptr2 = alloc.realloc(ptr, 32 * sizeof[Int])

    assertAccessible(ptr, 32)

    val ptr3 = alloc.realloc(ptr2, 128 * sizeof[Int])

    assertAccessible(ptr3, 128)

    alloc.free(ptr3)
  }

  test("zone allocator malloc") {
    Zone { implicit z =>
      val ptr = z.alloc(64 * sizeof[Int])

      assertAccessible(ptr, 64)

      val ptr2 = alloc[Int](128)

      assertAccessible(ptr2, 128)
    }
  }
}

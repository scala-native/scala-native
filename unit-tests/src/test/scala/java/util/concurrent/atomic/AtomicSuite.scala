package java.util.concurrent.atomic

object AtomicSuite extends tests.Suite {

  test("Atomic Boolean") {

    val a = new AtomicBoolean(true)
    assert(a.get())
    assert(a.compareAndSet(true, false))
    assert(!a.get())

  }

  test("Atomic Integer Array") {

    val a = new AtomicIntegerArray(3)

    a.set(0, 0)
    a.set(1, 1)
    a.set(2, 2)

    assert(a.get(0) == 0)
    assert(a.get(1) == 1)
    assert(a.get(2) == 2)

    assert(a.compareAndSet(0, 0, 1))
    assert(a.compareAndSet(1, 1, 2))
    assert(a.compareAndSet(2, 2, 3))

    assert(a.get(0) == 1)
    assert(a.get(1) == 2)
    assert(a.get(2) == 3)

    assertThrows[IndexOutOfBoundsException](a.get(3))
    assertThrows[IndexOutOfBoundsException](a.get(-1))

  }

}

package java.util.concurrent.atomic

object AtomicSuite extends tests.MultiThreadSuite {

  test("Atomic Boolean") {
    val a = new AtomicBoolean(true)
    assert(a.get())
    assert(a.compareAndSet(true, false))
    assert(!a.get())
  }

  test("Atomic Reference") {
    val thing1 = new Object
    val thing2 = new Object
    val a      = new AtomicReference[AnyRef]()

    assertEquals(a.get(), null)
    a.set(thing1)
    assertEquals(a.get(), thing1)

    assertNot(a.compareAndSet(null, thing2))
    assert(a.compareAndSet(thing1, thing2))
    assertEquals(a.get(), thing2)
  }

  test("Atomic Integer") {
    val a = new AtomicInteger()

    assertEquals(a.get(), 0)
    a.set(1)
    assertEquals(a.get(), 1)

    assertNot(a.compareAndSet(0, 2))
    assert(a.compareAndSet(1, 2))
    assertEquals(a.get(), 2)

    assertEquals(a.incrementAndGet(), 3)
    assertEquals(a.getAndIncrement(), 3)
    assertEquals(a.get(), 4)

    assertEquals(a.decrementAndGet(), 3)
    assertEquals(a.getAndDecrement(), 3)
    assertEquals(a.get(), 2)
  }

  test("Atomic Integer is atomic") {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0
      withThreads(numThreads, label = "Atomic Integer CounterExample") { _ =>
        var i = n
        // making this as fast as possible
        while (i > 0) {
          number = number + 1
          i -= 1
        }
      }
      number != (n * numThreads)
    } { n: Int =>
      val number = new AtomicInteger()
      withThreads(numThreads, label = "Atomic Integer Test") { _ =>
        var i = n
        // making this as fast as possible
        while (i > 0) {
          number.addAndGet(1)
          i -= 1
        }
      }

      val value    = number.get()
      val expected = n * numThreads
      Console.out.println(s"value: $value, expected: $expected")
      value == expected
    }
  }

  test("Atomic Long") {
    val a = new AtomicLong()

    assertEquals(a.get(), 0L)
    a.set(1L)
    assertEquals(a.get(), 1L)

    assertNot(a.compareAndSet(0L, 2L))
    assert(a.compareAndSet(1L, 2L))
    assertEquals(a.get(), 2L)

    assertEquals(a.incrementAndGet(), 3L)
    assertEquals(a.getAndIncrement(), 3L)
    assertEquals(a.get(), 4L)

    assertEquals(a.decrementAndGet(), 3L)
    assertEquals(a.getAndDecrement(), 3L)
    assertEquals(a.get(), 2L)
  }

  test("Atomic Long is atomic") {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0L
      withThreads(numThreads, label = "Atomic Long CounterExample") { _ =>
        var i = n
        // making this as fast as possible
        while (i > 0) {
          number = number + 1L
          i -= 1
        }
      }
      number != (n * numThreads)
    } { n: Int =>
      val number = new AtomicLong()
      withThreads(numThreads, label = "Atomic Long Test") { _ =>
        var i = n
        // making this as fast as possible
        while (i > 0) {
          number.addAndGet(1L)
          i -= 1
        }
      }

      val value    = number.get()
      val expected = n.toLong * numThreads
      Console.out.println(s"value: $value, expected: $expected")
      value == expected
    }
  }

  test("Atomic Reference Array") {
    val a = new AtomicReferenceArray[AnyRef](3)

    val thing1 = new Object
    val thing2 = new Object
    val thing3 = new Object

    assertEquals(a.get(0), null)
    assertEquals(a.get(1), null)
    assertEquals(a.get(2), null)

    a.set(1, thing1)
    a.set(2, thing2)

    assertEquals(a.get(0), null)
    assertEquals(a.get(1), thing1)
    assertEquals(a.get(2), thing2)

    // the wrong indexes
    assertNot(a.compareAndSet(0, thing2, thing3))
    assertNot(a.compareAndSet(1, null, thing1))
    assertNot(a.compareAndSet(2, thing1, thing2))

    assert(a.compareAndSet(0, null, thing1))
    assert(a.compareAndSet(1, thing1, thing2))
    assert(a.compareAndSet(2, thing2, thing3))

    assertEquals(a.get(0), thing1)
    assertEquals(a.get(1), thing2)
    assertEquals(a.get(2), thing3)

    assertThrows[IndexOutOfBoundsException](a.get(3))
    assertThrows[IndexOutOfBoundsException](a.get(-1))
  }

  test("Atomic Integer Array") {
    val a = new AtomicIntegerArray(3)

    assertEquals(a.get(0), 0)
    assertEquals(a.get(1), 0)
    assertEquals(a.get(2), 0)

    a.set(0, 0)
    a.set(1, 1)
    a.set(2, 2)

    assertEquals(a.get(0), 0)
    assertEquals(a.get(1), 1)
    assertEquals(a.get(2), 2)

    // the wrong indexes
    assertNot(a.compareAndSet(0, 2, 3))
    assertNot(a.compareAndSet(1, 0, 1))
    assertNot(a.compareAndSet(2, 1, 2))

    assert(a.compareAndSet(0, 0, 1))
    assert(a.compareAndSet(1, 1, 2))
    assert(a.compareAndSet(2, 2, 3))

    assertEquals(a.get(0), 1)
    assertEquals(a.get(1), 2)
    assertEquals(a.get(2), 3)

    assertThrows[IndexOutOfBoundsException](a.get(3))
    assertThrows[IndexOutOfBoundsException](a.get(-1))
  }

  test("Atomic Integer Array is atomic") {
    val numThreads = 4
    testWithMinRepetitions() { n: Int =>
      val array = new Array[Int](2)
      withThreads(numThreads, label = "Atomic Integer Array CounterExample") {
        id: Int =>
          val index = id / 2
          var i     = n
          // making this as fast as possible
          while (i > 0) {
            array(index) = array(index) + 1
            i -= 1
          }
      }
      array.sum != (n * numThreads)
    } { n: Int =>
      val array = new AtomicIntegerArray(2)
      withThreads(numThreads, label = "Atomic Integer Array Test") { id: Int =>
        val index = id / 2
        var i     = n
        // making this as fast as possible
        while (i > 0) {
          array.addAndGet(index, 1)
          i -= 1
        }
      }

      val value    = array.get(0) + array.get(1)
      val expected = n * numThreads
      Console.out.println(s"value: $value, expected: $expected")
      value == expected
    }
  }

  test("Atomic Long Array") {

    val a = new AtomicLongArray(3)

    assertEquals(a.get(0), 0L)
    assertEquals(a.get(1), 0L)
    assertEquals(a.get(2), 0L)

    a.set(0, 0L)
    a.set(1, 1L)
    a.set(2, 2L)

    assertEquals(a.get(0), 0L)
    assertEquals(a.get(1), 1L)
    assertEquals(a.get(2), 2L)

    // the wrong indexes
    assertNot(a.compareAndSet(0, 2L, 3L))
    assertNot(a.compareAndSet(1, 0L, 1L))
    assertNot(a.compareAndSet(2, 1L, 2L))

    assert(a.compareAndSet(0, 0L, 1L))
    assert(a.compareAndSet(1, 1L, 2L))
    assert(a.compareAndSet(2, 2L, 3L))

    assertEquals(a.get(0), 1L)
    assertEquals(a.get(1), 2L)
    assertEquals(a.get(2), 3L)

    assertThrows[IndexOutOfBoundsException](a.get(3))
    assertThrows[IndexOutOfBoundsException](a.get(-1))
  }

  test("Atomic Long Array is atomic") {
    val numThreads = 4
    testWithMinRepetitions() { n: Int =>
      val array = new Array[Long](2)
      withThreads(numThreads, label = "Atomic Long Array CounterExample") {
        id: Int =>
          val index = id / 2
          var i     = n
          // making this as fast as possible
          while (i > 0) {
            array(index) = array(index) + 1L
            i -= 1
          }
      }
      array.sum != (n * numThreads)
    } { n: Int =>
      val array = new AtomicLongArray(2)
      withThreads(numThreads, label = "Atomic Long Array Test") { id: Int =>
        val index = id / 2
        var i     = n
        // making this as fast as possible
        while (i > 0) {
          array.addAndGet(index, 1L)
          i -= 1
        }
      }

      val value    = array.get(0) + array.get(1)
      val expected = n * numThreads
      Console.out.println(s"value: $value, expected: $expected")
      value == expected
    }
  }
}

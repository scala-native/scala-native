package java.lang

object ThreadSuite extends tests.Suite {

  test("Runtime static variables access and currentThread do not crash") {

    val max  = Thread.MAX_PRIORITY
    val min  = Thread.MIN_PRIORITY
    val norm = Thread.NORM_PRIORITY

    val current = Thread.currentThread()

  }

  test("Get/Set Priority work as it should with currentThread") {

    val current = Thread.currentThread()

    current.setPriority(3)
    assert(current.getPriority == 3)

  }

}

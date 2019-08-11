package java.lang

object ThreadLocalSuite extends tests.MultiThreadSuite {
  test("Each thread should have their own copies") {
    val localString = new ThreadLocal[String]
    assertEquals(localString.get(), null)
    localString.set("banana")
    assertEquals(localString.get(), "banana")

    class ThreadLocalTester(str: String) extends Thread {
      override def run() = {
        assertEquals(localString.get(), null)
        localString.set(str)
        assertEquals(localString.get(), str)
        localString.remove()
        assertEquals(localString.get(), null)
        localString.set(str)
        assertEquals(localString.get(), str)
      }
    }
    val appleThread  = new ThreadLocalTester("apple")
    val orangeThread = new ThreadLocalTester("orange")

    appleThread.start()
    orangeThread.start()
    appleThread.join()
    orangeThread.join()

    assertEquals(localString.get(), "banana")
    localString.remove()
    assertEquals(localString.get(), null)
  }
  test("Initial values") {
    val localString = new ThreadLocal[String] {
      override protected def initialValue = "<empty>"
    }
    assertEquals(localString.get(), "<empty>")
    localString.set("banana")

    class ThreadLocalTester(str: String) extends Thread {
      override def run() = {
        assertEquals(localString.get(), "<empty>")
        localString.set(str)
        assertEquals(localString.get(), str)
        localString.remove()
        assertEquals(localString.get(), "<empty>")
        localString.set(str)
        assertEquals(localString.get(), str)
      }
    }
    val appleThread  = new ThreadLocalTester("apple")
    val orangeThread = new ThreadLocalTester("orange")

    appleThread.start()
    orangeThread.start()
    appleThread.join()
    orangeThread.join()

    assertEquals(localString.get(), "banana")
    localString.remove()
    assertEquals(localString.get(), "<empty>")
  }

  test("Initialized not called more than once") {
    var timesInitialized = 0
    val local = new ThreadLocal[Int] {
      override protected def initialValue() = {
        timesInitialized += 1
        42
      }
    }
    assertEquals(local.get(), 42)
    assertEquals(local.get(), 42)
    assertEquals(local.get(), 42)
    assertEquals(local.get(), 42)
    assertEquals(timesInitialized, 1)
  }
}

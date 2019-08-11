package java.lang

import scala.collection.mutable

object ThreadGroupSuite extends tests.MultiThreadSuite {
  test("Constructors") {

    val groupName   = "groupNameGoesHere"
    val threadGroup = new ThreadGroup(groupName)
    assertEquals(threadGroup.getName, groupName)

    val subgroupName = "this is a subgroup"
    val subgroup     = new ThreadGroup(threadGroup, subgroupName)
    assertEquals(subgroup.getName, subgroupName)
    assertEquals(subgroup.getParent, threadGroup)
    assert(threadGroup.parentOf(subgroup))
    assertNot(subgroup.parentOf(threadGroup))
  }

  test("ThreadGroup.checkAccess does not crash") {
    val mainThreadGroup = Thread.currentThread().getThreadGroup
    val threadGroup     = new ThreadGroup("abc")

    mainThreadGroup.checkAccess()
    threadGroup.checkAccess()

    val thread = new Thread {
      override def run() = {
        mainThreadGroup.checkAccess()
        threadGroup.checkAccess()
      }
    }
    thread.start()
    thread.join()
  }

  abstract class Structure[T <: Thread] {
    def makeThread(group: ThreadGroup, name: String): T

    val group = new ThreadGroup("group")
    val groupThreads: Seq[T] = scala.Seq(makeThread(group, "G-1"),
                                         makeThread(group, "G-2"),
                                         makeThread(group, "G-3"))

    val subgroup1 = new ThreadGroup(group, "subgroup1")
    val subgroup1Threads: Seq[T] =
      scala.Seq(makeThread(subgroup1, "SG-1"), makeThread(subgroup1, "SG-2"))

    val subgroup2 = new ThreadGroup(group, "subgroup2")
    val subgroup2Threads: Seq[T] =
      scala.Seq(makeThread(subgroup2, "SG-A"), makeThread(subgroup2, "SG-B"))

    val threads: Seq[T] = groupThreads ++ subgroup1Threads ++ subgroup2Threads

    def destroyAllGroups(): Unit = {
      try {
        if (!group.isDestroyed) group.destroy()
        if (!subgroup1.isDestroyed) subgroup1.destroy()
        if (!subgroup1.isDestroyed) subgroup1.destroy()
      } catch {
        case _: IllegalThreadStateException =>
      }
    }
  }

  test("ThreadGroup.interrupt should interrupt sleep for all threads") {
    var fail = false
    class SleepyThread(group: ThreadGroup, name: String)
        extends Thread(group, name) {
      override def run() = {
        expectThrows(classOf[InterruptedException], {
          Thread.sleep(10 * eternity)
          fail = true
        })
      }
    }
    val structure = new Structure[SleepyThread] {
      def makeThread(group: ThreadGroup, name: String) =
        new SleepyThread(group, name)
    }
    import structure._
    threads.foreach(_.start())
    threads.foreach { thread: Thread =>
      eventuallyEquals(
        label = thread.getName + "is in Thread.State.TIMED_WAITING")(
        Thread.State.TIMED_WAITING,
        thread.getState)
    }
    group.interrupt()
    threads.foreach { thread: Thread =>
      eventuallyEquals(
        label = thread.getName + "is in Thread.State.TERMINATED")(
        Thread.State.TERMINATED,
        thread.getState)
    }

    destroyAllGroups()

    assertNot(fail)
  }

  test("ThreadGroup.destroy") {
    val threadGroup = new ThreadGroup("abc")
    val thread      = new Counter(threadGroup, "active")
    assertNot(threadGroup.isDestroyed)

    thread.start()
    eventually()(thread.count > 1)
    //cannot destroy group with running threads
    assertThrows[IllegalThreadStateException](threadGroup.destroy())
    assertNot(threadGroup.isDestroyed)
    thread.goOn = false
    thread.join()

    threadGroup.destroy()
    assert(threadGroup.isDestroyed)

    // cannot destroy it twice
    assertThrows[IllegalThreadStateException](threadGroup.destroy())
    assert(threadGroup.isDestroyed)

    // cannot add new threads or threadGroups
    assertThrows[IllegalThreadStateException](
      new Thread(threadGroup, "Sad Thread"))
    assertThrows[IllegalThreadStateException](
      new ThreadGroup(threadGroup, "Sad ThreadGroup"))
  }

  test("Thread.setPriority respects threadGroups maxPriority") {
    val fastGroup = new ThreadGroup("fastGroup")
    assertEquals(fastGroup.getMaxPriority, Thread.MAX_PRIORITY)

    val fastThread = new Thread(fastGroup, "fastThread")
    fastThread.setPriority(Thread.MAX_PRIORITY)
    assertEquals(fastThread.getPriority, Thread.MAX_PRIORITY)

    val slowGroup = new ThreadGroup("slowGroup")
    slowGroup.setMaxPriority(Thread.MIN_PRIORITY)
    assertEquals(slowGroup.getMaxPriority, Thread.MIN_PRIORITY)

    val slowThread = new Thread(slowGroup, "slowThread")
    slowThread.setPriority(Thread.MAX_PRIORITY)
    assertEquals(slowThread.getPriority, Thread.MIN_PRIORITY)
  }

  test(
    "A daemon thread group is automatically destroyed when its last thread is stopped or its last thread group is destroyed.") {
    val structure = new Structure[Counter] {
      def makeThread(group: ThreadGroup, name: String) =
        new Counter(group, name)
    }
    import structure._
    threads.foreach(_.start())
    group.setDaemon(true)
    subgroup1.setDaemon(true)
    subgroup2.setDaemon(true)
    assert(group.isDaemon)
    assert(subgroup1.isDaemon)
    assert(subgroup2.isDaemon)

    threads.foreach { thread: Counter =>
      eventually(label = s"$thread.count > 1")(thread.count > 1)
    }

    threads.foreach { thread: Counter =>
      thread.goOn = false
    }
    threads.foreach { thread: Counter =>
      thread.join()
    }
    assert(group.isDestroyed)
    assert(group.isDaemon)

    destroyAllGroups()
  }

  test("activeCount, activeGroupCount, Thread.enumerate and list") {
    val structure = new Structure[Counter] {
      def makeThread(group: ThreadGroup, name: String) =
        new Counter(group, name)
    }
    import structure._
    threads.foreach(_.start())

    threads.foreach { thread: Counter =>
      eventually(label = s"$thread.count > 1")(thread.count > 1)
    }

    assertEquals(group.activeCount(), threads.size)
    assertEquals(subgroup1.activeCount(), subgroup1Threads.size)
    assertEquals(subgroup2.activeCount(), subgroup2Threads.size)

    assertEquals(group.activeGroupCount(), 2)
    assertEquals(subgroup1.activeGroupCount(), 0)
    assertEquals(subgroup2.activeGroupCount(), 0)

    // also test Thread.activeCount and Thread.enumerate
    {
      var count   = 0
      var success = false
      val thread = new Thread(group, "checker") {
        override def run() = {
          count = Thread.activeCount()
          assertEquals(Thread.enumerate(new Array[Thread](0)), 0)

          val partialArray = new Array[Thread](threads.size - 1)
          assertEquals(Thread.enumerate(partialArray), threads.size - 1)
          val correctValuesPartial = mutable.WrappedArray
            .make[Thread](partialArray)
            .forall { t =>
              threads.contains(t) || t == this
            }
          assert(correctValuesPartial)

          val fullArray = new Array[Thread](threads.size + 1)
          assertEquals(Thread.enumerate(fullArray), threads.size + 1)
          val wrappedArrayFull = mutable.WrappedArray.make[Thread](fullArray)
          val correctValuesFull: scala.Boolean = threads.forall(
            wrappedArrayFull.contains) && wrappedArrayFull.contains(this)
          assert(correctValuesFull)

          val largeArray = new Array[Thread](threads.size + 50)
          assertEquals(Thread.enumerate(largeArray), threads.size + 1)
          val wrappedArrayLarge = mutable.WrappedArray.make[Thread](largeArray)
          val (prefix, suffix)  = wrappedArrayLarge.splitAt(threads.size + 1)
          val correctValuesLarge: scala.Boolean = threads.forall(
            prefix.contains) && prefix.contains(this)
          assert(correctValuesLarge)
          assert(suffix.forall(_ == null))

          success = true
        }
      }
      thread.start()
      thread.join()
      assertEquals(count, threads.size + 1)
      assert(success)
    }

    // also test list
    {
      val outputStream = new java.io.ByteArrayOutputStream()
      Console.withOut(outputStream) {
        group.list()
      }
      val string = outputStream.toString
      assert(string.contains(group.getName))
      assert(string.contains(subgroup1.getName))
      assert(string.contains(subgroup2.getName))
      assert(threads.forall(t => string.contains(t.getName)))
    }

    threads.foreach { thread: Counter =>
      thread.goOn = false
    }
    threads.foreach { thread: Counter =>
      thread.join()
    }

    assertEquals(group.activeCount(), 0)
    assertEquals(subgroup1.activeCount(), 0)
    assertEquals(subgroup2.activeCount(), 0)

    assertEquals(group.activeGroupCount(), 2)
    assertEquals(subgroup1.activeGroupCount(), 0)
    assertEquals(subgroup2.activeGroupCount(), 0)

    destroyAllGroups()
  }

  test("toString should contain the group's name") {
    val name        = "a very long and descriptive name"
    val threadGroup = new ThreadGroup(name)
    assert(threadGroup.toString.contains(name))
  }

  test("unhandled exception should be correctly delegated") {
    val thread    = new Thread()
    val exception = new Error("delegate me")
    val detector  = new ExceptionDetector(thread, exception)
    val threadGroup = new ThreadGroup("top") {
      override def uncaughtException(t: Thread, e: Throwable) =
        detector.uncaughtException(t, e)
    }

    val subGroup = new ThreadGroup(threadGroup, "sub")
    subGroup.uncaughtException(thread, exception)

    assert(detector.wasException)
  }

  test("*DEPRECATED*  ThreadGroup.suspend and resume should affect all threads") {

    val structure = new Structure[Counter] {
      def makeThread(group: ThreadGroup, name: String) =
        new Counter(group, name)
    }
    import structure._
    try {
      threads.foreach(_.start())
      threads.foreach { thread: Counter =>
        eventually(label = s"$thread.count > 1")(thread.count > 1)
      }

      group.suspend()
      val countMap: scala.collection.immutable.Map[Counter, scala.Long] =
        threads.map { thread: Counter =>
          thread -> eventuallyConstant()(thread.count).get
        }.toMap
      group.resume()

      threads.foreach { thread: Counter =>
        eventually(label = s"$thread.count > countMap")(
          thread.count > countMap(thread))
      }
    } finally {

      threads.foreach { thread: Counter =>
        thread.goOn = false
      }
      threads.foreach { thread: Counter =>
        thread.join()
      }

      destroyAllGroups()
    }
  }

  test(
    "*DEPRECATED*  ThreadGroup.suspend and resume should respect allowThreadSuspension") {

    val structure = new Structure[Counter] {
      def makeThread(group: ThreadGroup, name: String) =
        new Counter(group, name)
    }
    import structure._
    try {
      threads.foreach(_.start())
      group.allowThreadSuspension(false)

      val countMap1: scala.collection.immutable.Map[Counter, scala.Long] =
        threads.map { thread: Counter =>
          eventually(label = s"$thread.count > 1")(thread.count > 1)
          thread -> thread.count
        }.toMap

      group.suspend()
      val countMap2: scala.collection.immutable.Map[Counter, scala.Long] =
        threads.map { thread: Counter =>
          eventually(label = s"$thread.count > countMap1")(
            thread.count > countMap1(thread))
          thread -> thread.count
        }.toMap
      group.resume()

      group.allowThreadSuspension(true)
      subgroup1.allowThreadSuspension(false)

      val countMap3: scala.collection.immutable.Map[Counter, scala.Long] =
        threads.map { thread: Counter =>
          eventually(label = s"$thread.count > countMap2")(
            thread.count > countMap2(thread))
          thread -> thread.count
        }.toMap

      group.suspend()

      // only subgroup1Threads should go on
      val countMap4a: scala.collection.immutable.Map[Counter, scala.Long] =
        subgroup1Threads.map { thread: Counter =>
          eventually(label = s"$thread.count > countMap3")(
            thread.count > countMap3(thread))
          thread -> thread.count
        }.toMap
      // every other thread should be suspended
      val countMap4b = (groupThreads ++ subgroup2Threads)
        .map { thread: Counter =>
          thread -> eventuallyConstant()(thread.count).get
        }
      val countMap4
        : scala.collection.immutable.Map[Counter, scala.Long] = countMap4a ++ countMap4b

      group.resume()

      threads.foreach { thread: Counter =>
        eventually(label = s"$thread.count > countMap4")(
          thread.count > countMap4(thread))
      }
    } finally {

      threads.foreach { thread: Counter =>
        thread.goOn = false
      }
      threads.foreach { thread: Counter =>
        thread.join()
      }

      destroyAllGroups()
    }
  }

  test("*DEPRECATED* ThreadGroup.stop should stop all threads") {
    val mutex = new Object
    val structure = new Structure[WaitingThread] {
      def makeThread(group: ThreadGroup, name: String) =
        new WaitingThread(mutex, group, name)
    }
    import structure._
    threads.foreach(_.start())
    threads.foreach { thread: Thread =>
      eventuallyEquals(
        label = thread.getName + "is in Thread.State.TIMED_WAITING")(
        Thread.State.WAITING,
        thread.getState
      )
    }
    group.stop()
    threads.foreach { thread: Thread =>
      eventuallyEquals(
        label = thread.getName + "is in Thread.State.TERMINATED")(
        Thread.State.TERMINATED,
        thread.getState)
    }

    destroyAllGroups()
  }
}

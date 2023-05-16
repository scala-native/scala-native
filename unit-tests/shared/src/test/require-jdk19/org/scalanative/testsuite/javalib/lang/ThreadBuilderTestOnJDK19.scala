package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport

import org.junit._
import org.junit.Assert._
import org.junit.{Ignore, BeforeClass}

import scala.scalanative.junit.utils.AssumesHelper

object ThreadBuilderTestOnJDK19 {
  @BeforeClass def checkRuntime(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
  }

  val Local = new ThreadLocal[AnyRef]
  val InheritedLocal = new InheritableThreadLocal[AnyRef]

  class FooException extends RuntimeException {}
}

class ThreadBuilderTestOnJDK19 {
  import ThreadBuilderTestOnJDK19._

  @Test def testPlatformThread(): Unit = {
    val parent = Thread.currentThread()
    val builder = Thread.ofPlatform()
    // unstarted
    val done1 = new AtomicBoolean()
    val thread1 = builder.unstarted(() => done1.set(true))
    assertFalse(thread1.isVirtual)
    assertTrue(thread1.getState() eq Thread.State.NEW)
    assertFalse(thread1.getName().isEmpty)
    assertTrue(thread1.getThreadGroup eq parent.getThreadGroup)
    assertTrue(thread1.isDaemon() == parent.isDaemon())
    assertTrue(thread1.getPriority() == parent.getPriority())
    thread1.start()
    thread1.join()
    assertTrue(done1.get())
    // start
    val done2 = new AtomicBoolean()
    val thread2 = builder.start(() => done2.set(true))
    assertFalse(thread2.isVirtual)
    assertTrue(thread2.getState() ne Thread.State.NEW)
    assertFalse(thread2.getName().isEmpty)
    val group2 = thread2.getThreadGroup
    assertTrue((group2 eq parent.getThreadGroup) || group2 == null)
    assertTrue(thread2.isDaemon() == parent.isDaemon())
    assertTrue(thread2.getPriority() == parent.getPriority())
    thread2.join()
    assertTrue(done2.get())
    // factory
    val done3 = new AtomicBoolean()
    val thread3 = builder.factory.newThread(() => done3.set(true))
    assertFalse(thread3.isVirtual)
    assertTrue(thread3.getState() eq Thread.State.NEW)
    assertFalse(thread3.getName().isEmpty)
    assertTrue(thread3.getThreadGroup eq parent.getThreadGroup)
    assertTrue(thread3.isDaemon() == parent.isDaemon())
    assertTrue(thread3.getPriority() == parent.getPriority())
    thread3.start()
    thread3.join()
    assertTrue(done3.get())
  }

  @Ignore("VirtualThreads unimplemented")
  @Test def testVirtualThread(): Unit = {
    val parent = Thread.currentThread()
    val builder = Thread.ofVirtual()
    // unstarted
    val done1 = new AtomicBoolean()
    val thread1 = builder.unstarted(() => done1.set(true))
    assertTrue(thread1.isVirtual)
    assertEquals(Thread.State.NEW, thread1.getState())
    assertTrue(thread1.getName().isEmpty)
    assertTrue(thread1.isDaemon())
    assertEquals(Thread.NORM_PRIORITY, thread1.getPriority())
    thread1.start()
    thread1.join()
    assertTrue(done1.get())

    // start
    val done2 = new AtomicBoolean()
    val thread2 = builder.start(() => done2.set(true))
    assertTrue(thread2.isVirtual)
    assertNotEquals(Thread.State.NEW, thread2.getState())
    assertTrue(thread2.getName().isEmpty)
    assertTrue(thread2.isDaemon())
    assertEquals(Thread.NORM_PRIORITY, thread2.getPriority())
    thread2.join()
    assertTrue(done2.get())

    // factory
    val done3 = new AtomicBoolean()
    val thread3 = builder.factory.newThread(() => done3.set(true))
    assertTrue(thread3.isVirtual)
    assertEquals(Thread.State.NEW, thread3.getState())
    assertTrue(thread3.getName().isEmpty)
    assertTrue(thread3.isDaemon())
    assertEquals(Thread.NORM_PRIORITY, thread3.getPriority())
    thread3.start()
    thread3.join()
    assertTrue(done3.get())
  }

  @Test def testName1(): Unit = {
    val builder = Thread.ofPlatform().name("foo")
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
    assertTrue(thread1.getName() == "foo")
    assertTrue(thread2.getName() == "foo")
    assertTrue(thread3.getName() == "foo")
  }

  @Ignore("VirtualThreads unimplemented")
  @Test def testName2(): Unit = {
    val builder = Thread.ofVirtual().name("foo")
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
    assertTrue(thread1.getName() == "foo")
    assertTrue(thread2.getName() == "foo")
    assertTrue(thread3.getName() == "foo")
  }

  @Test def testName3(): Unit = {
    val builder = Thread.ofPlatform().name("foo-", 100)
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.unstarted(() => {})
    val thread3 = builder.unstarted(() => {})
    assertTrue(thread1.getName() == "foo-100")
    assertTrue(thread2.getName() == "foo-101")
    assertTrue(thread3.getName() == "foo-102")
    val factory = builder.factory
    val thread4 = factory.newThread(() => {})
    val thread5 = factory.newThread(() => {})
    val thread6 = factory.newThread(() => {})
    assertTrue(thread4.getName() == "foo-103")
    assertTrue(thread5.getName() == "foo-104")
    assertTrue(thread6.getName() == "foo-105")
  }

  @Test def testName4(): Unit = {
    val builder = Thread.ofVirtual().name("foo-", 100)
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.unstarted(() => {})
    val thread3 = builder.unstarted(() => {})
    assertTrue(thread1.getName() == "foo-100")
    assertTrue(thread2.getName() == "foo-101")
    assertTrue(thread3.getName() == "foo-102")
    val factory = builder.factory
    val thread4 = factory.newThread(() => {})
    val thread5 = factory.newThread(() => {})
    val thread6 = factory.newThread(() => {})
    assertTrue(thread4.getName() == "foo-103")
    assertTrue(thread5.getName() == "foo-104")
    assertTrue(thread6.getName() == "foo-105")
  }

  @Test def testThreadGroup1(): Unit = {
    val group = new ThreadGroup("groupies")
    val builder = Thread.ofPlatform().group(group)
    val thread1 = builder.unstarted(() => {})
    val done = new AtomicBoolean()
    val thread2 = builder.start(() => {
      while (!done.get()) LockSupport.park()

    })
    val thread3 = builder.factory.newThread(() => {})
    try {
      assertTrue(thread1.getThreadGroup eq group)
      assertTrue(thread2.getThreadGroup eq group)
      assertTrue(thread3.getThreadGroup eq group)
    } finally {
      done.set(true)
      LockSupport.unpark(thread2)
    }
  }

  @Ignore("VirtualThreads unimplemented")
  @Test def testThreadGroup2(): Unit = {
    val vgroup =
      Thread.ofVirtual().unstarted(() => {}).getThreadGroup
    assertEquals(vgroup.getName(), "VirtualThreads")
    val thread1 = Thread.ofVirtual().unstarted(() => {})
    val thread2 = Thread.ofVirtual().start { () => LockSupport.park() }
    val thread3 = Thread.ofVirtual().factory.newThread(() => {})
    try {
      assertTrue(thread1.getThreadGroup eq vgroup)
      assertTrue(thread2.getThreadGroup eq vgroup)
      assertTrue(thread3.getThreadGroup eq vgroup)
    } finally LockSupport.unpark(thread2)
  }

  @Test def testPriority1(): Unit = {
    val priority = Thread.currentThread().getPriority()
    val builder = Thread.ofPlatform()
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
    assertTrue(thread1.getPriority() == priority)
    assertTrue(thread2.getPriority() == priority)
    assertTrue(thread3.getPriority() == priority)
  }
  @Test def testPriority2(): Unit = {
    val priority = Thread.MIN_PRIORITY
    val builder = Thread.ofPlatform().priority(priority)
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
    assertTrue(thread1.getPriority() == priority)
    assertTrue(thread2.getPriority() == priority)
    assertTrue(thread3.getPriority() == priority)
  }
  @Test def testPriority3(): Unit = {
    val currentThread = Thread.currentThread()
    Assume.assumeFalse(currentThread.isVirtual())

    val maxPriority = currentThread.getThreadGroup.getMaxPriority
    val priority = Math.min(maxPriority + 1, Thread.MAX_PRIORITY)
    val builder = Thread.ofPlatform().priority(priority)
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
    assertTrue(thread1.getPriority() == priority)
    assertTrue(thread2.getPriority() == priority)
    assertTrue(thread3.getPriority() == priority)
  }
  @Test def testPriority4(): Unit = {
    val builder = Thread.ofPlatform()
    assertThrows(
      classOf[IllegalArgumentException],
      () => builder.priority(Thread.MIN_PRIORITY - 1)
    )
  }
  @Test def testPriority5(): Unit = {
    val builder = Thread.ofPlatform()
    assertThrows(
      classOf[IllegalArgumentException],
      () => builder.priority(Thread.MAX_PRIORITY + 1)
    )
  }

  @Test def testDaemon1(): Unit = {
    val builder = Thread.ofPlatform().daemon(false)
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
    assertFalse(thread1.isDaemon())
    assertFalse(thread2.isDaemon())
    assertFalse(thread3.isDaemon())
  }
  @Test def testDaemon2(): Unit = {
    val builder = Thread.ofPlatform().daemon(true)
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
    assertTrue(thread1.isDaemon())
    assertTrue(thread2.isDaemon())
    assertTrue(thread3.isDaemon())
  }
  @Test def testDaemon3(): Unit = {
    val builder = Thread.ofPlatform().daemon
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
    assertTrue(thread1.isDaemon())
    assertTrue(thread2.isDaemon())
    assertTrue(thread3.isDaemon())
  }
  @Test def testDaemon4(): Unit = {
    val builder = Thread.ofPlatform()
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
    // daemon status should be inherited
    val d = Thread.currentThread().isDaemon()
    assertTrue(thread1.isDaemon() == d)
    assertTrue(thread2.isDaemon() == d)
    assertTrue(thread3.isDaemon() == d)
  }

  @Ignore("VirtualThreads unimplemented")
  @Test def testDaemon5(): Unit = {
    val builder = Thread.ofVirtual()
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
    // daemon status should always be true
    assertTrue(thread1.isDaemon())
    assertTrue(thread2.isDaemon())
    assertTrue(thread3.isDaemon())
  }

  @Test def testStackSize1(): Unit = {
    val builder = Thread.ofPlatform().stackSize(1024 * 1024)
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
  }
  @Test def testStackSize2(): Unit = {
    val builder = Thread.ofPlatform().stackSize(0)
    val thread1 = builder.unstarted(() => {})
    val thread2 = builder.start(() => {})
    val thread3 = builder.factory.newThread(() => {})
  }
  @Test def testStackSize3(): Unit = {
    val builder = Thread.ofPlatform()
    assertThrows(
      classOf[IllegalArgumentException],
      () => builder.stackSize(-1)
    )
  }

  @Test def testUncaughtExceptionHandler1(): Unit = {
    val threadRef = new AtomicReference[Thread]
    val exceptionRef =
      new AtomicReference[Throwable]
    val thread = Thread
      .ofPlatform()
      .uncaughtExceptionHandler((t, e) => {
        assertTrue(t eq Thread.currentThread())
        threadRef.set(t)
        exceptionRef.set(e)

      })
      .start(() => {
        throw new FooException

      })
    thread.join()
    assertTrue(threadRef.get eq thread)
    assertTrue(exceptionRef.get.isInstanceOf[FooException])
  }

  @Ignore("VirtualThreads unimplemented")
  @Test def testUncaughtExceptionHandler2(): Unit = {
    val threadRef = new AtomicReference[Thread]
    val exceptionRef =
      new AtomicReference[Throwable]
    val thread = Thread
      .ofVirtual()
      .uncaughtExceptionHandler((t, e) => {
        assertTrue(t eq Thread.currentThread())
        threadRef.set(t)
        exceptionRef.set(e)

      })
      .start(() => {
        throw new FooException

      })
    thread.join()
    assertTrue(threadRef.get eq thread)
    assertTrue(exceptionRef.get.isInstanceOf[FooException])
  }

  @Test def testUncaughtExceptionHandler3(): Unit = {
    val threadRef = new AtomicReference[Thread]
    val exceptionRef =
      new AtomicReference[Throwable]
    val thread = Thread
      .ofPlatform()
      .uncaughtExceptionHandler((t, e) => {
        assertTrue(t eq Thread.currentThread())
        threadRef.set(t)
        exceptionRef.set(e)

      })
      .factory
      .newThread(() => {
        throw new FooException

      })
    thread.start()
    thread.join()
    assertTrue(threadRef.get eq thread)
    assertTrue(exceptionRef.get.isInstanceOf[FooException])
  }
  @Test def testUncaughtExceptionHandler4(): Unit = {
    val threadRef = new AtomicReference[Thread]
    val exceptionRef =
      new AtomicReference[Throwable]
    val thread = Thread
      .ofPlatform()
      .uncaughtExceptionHandler((t, e) => {
        assertTrue(t eq Thread.currentThread())
        threadRef.set(t)
        exceptionRef.set(e)

      })
      .factory
      .newThread(() => {
        throw new FooException

      })
    thread.start()
    thread.join()
    assertTrue(threadRef.get eq thread)
    assertTrue(exceptionRef.get.isInstanceOf[FooException])
  }

  private def testThreadLocals(builder: Thread.Builder): Unit = {
    val done = new AtomicBoolean()
    val task: Runnable = () => {
      val value = new AnyRef
      Local.set(value)
      assertTrue(Local.get eq value)
      done.set(true)

    }
    done.set(false)
    val thread1 = builder.unstarted(task)
    thread1.start()
    thread1.join()
    assertTrue(done.get())
    done.set(false)
    val thread2 = builder.start(task)
    thread2.join()
    assertTrue(done.get())
    done.set(false)
    val thread3 = builder.factory.newThread(task)
    thread3.start()
    thread3.join()
    assertTrue(done.get())
  }

  private def testNoThreadLocals(builder: Thread.Builder): Unit = {
    val done = new AtomicBoolean()
    val task: Runnable = () => {
      try Local.set(new AnyRef)
      catch {
        case expected: UnsupportedOperationException =>
          done.set(true)
      }

    }
    done.set(false)
    val thread1 = builder.unstarted(task)
    thread1.start()
    thread1.join()
    assertTrue(done.get())
    done.set(false)
    val thread2 = builder.start(task)
    thread2.join()
    assertTrue(done.get())
    done.set(false)
    val thread3 = builder.factory.newThread(task)
    thread3.start()
    thread3.join()
    assertTrue(done.get())
  }

  @Test def testThreadLocals1(): Unit = {
    val builder = Thread.ofPlatform()
    testThreadLocals(builder)
  }

  @Ignore("VirtualThreads unimplemented")
  @Test def testThreadLocals2(): Unit = {
    val builder = Thread.ofVirtual()
    testThreadLocals(builder)
  }

  @Test def testThreadLocals3(): Unit = {
    val builder = Thread.ofPlatform()
    // disallow
    builder.allowSetThreadLocals(false)
    testNoThreadLocals(builder)
    // allow
    builder.allowSetThreadLocals(true)
    testThreadLocals(builder)
  }

  @Ignore("VirtualThreads unimplemented")
  @Test def testThreadLocals4(): Unit = {
    val builder = Thread.ofVirtual()
    // disallow
    builder.allowSetThreadLocals(false)
    testNoThreadLocals(builder)
    // allow
    builder.allowSetThreadLocals(true)
    testThreadLocals(builder)
  }

  private def testInheritedThreadLocals(builder: Thread.Builder): Unit = {
    val value = new AnyRef
    InheritedLocal.set(value)
    val done = new AtomicBoolean()
    val task: Runnable = () => {
      assertTrue(InheritedLocal.get eq value)
      done.set(true)

    }
    done.set(false)
    val thread1 = builder.unstarted(task)
    thread1.start()
    thread1.join()
    assertTrue(done.get())
    done.set(false)
    val thread2 = builder.start(task)
    thread2.join()
    assertTrue(done.get())
    done.set(false)
    val thread3 = builder.factory.newThread(task)
    thread3.start()
    thread3.join()
    assertTrue(done.get())
  }

  private def testNoInheritedThreadLocals(builder: Thread.Builder): Unit = {
    val value = new AnyRef
    InheritedLocal.set(value)
    val done = new AtomicBoolean()
    val task: Runnable = () => {
      assertTrue(InheritedLocal.get == null)
      done.set(true)

    }
    done.set(false)
    val thread1 = builder.unstarted(task)
    thread1.start()
    thread1.join()
    assertTrue(done.get())
    done.set(false)
    val thread2 = builder.start(task)
    thread2.join()
    assertTrue(done.get())
    done.set(false)
    val thread3 = builder.factory.newThread(task)
    thread3.start()
    thread3.join()
    assertTrue(done.get())
  }

  @Test def testInheritedThreadLocals1(): Unit = {
    val builder = Thread.ofPlatform()
    testInheritedThreadLocals(builder) // default

    // do no inherit
    builder.inheritInheritableThreadLocals(false)
    testNoInheritedThreadLocals(builder)
    // inherit
    builder.inheritInheritableThreadLocals(true)
    testInheritedThreadLocals(builder)
  }

  @Ignore("VirtualThreads unimplemented")
  @Test def testInheritedThreadLocals2(): Unit = {
    val builder = Thread.ofVirtual()
    testInheritedThreadLocals(builder) // default

    // do no inherit
    builder.inheritInheritableThreadLocals(false)
    testNoInheritedThreadLocals(builder)
    // inherit
    builder.inheritInheritableThreadLocals(true)
    testInheritedThreadLocals(builder)
  }
  @Test def testInheritedThreadLocals3(): Unit = {
    val builder = Thread.ofPlatform()
    // thread locals not allowed
    builder.allowSetThreadLocals(false)
    testNoInheritedThreadLocals(builder)
    builder.inheritInheritableThreadLocals(false)
    testNoInheritedThreadLocals(builder)
    builder.inheritInheritableThreadLocals(true)
    testNoInheritedThreadLocals(builder)
    // thread locals allowed
    builder.allowSetThreadLocals(true)
    builder.inheritInheritableThreadLocals(false)
    testNoInheritedThreadLocals(builder)
    builder.inheritInheritableThreadLocals(true)
    testInheritedThreadLocals(builder)
  }

  @Ignore("VirtualThreads unimplemented")
  @Test def testInheritedThreadLocals4(): Unit = {
    val builder = Thread.ofVirtual()
    // thread locals not allowed
    builder.allowSetThreadLocals(false)
    testNoInheritedThreadLocals(builder)
    builder.inheritInheritableThreadLocals(false)
    testNoInheritedThreadLocals(builder)
    builder.inheritInheritableThreadLocals(true)
    testNoInheritedThreadLocals(builder)
    // thread locals allowed
    builder.allowSetThreadLocals(true)
    builder.inheritInheritableThreadLocals(false)
    testNoInheritedThreadLocals(builder)
    builder.inheritInheritableThreadLocals(true)
    testInheritedThreadLocals(builder)
  }

  @Test def testNulls1(): Unit = {
    val builder = Thread.ofPlatform()
    assertThrows(classOf[NullPointerException], () => builder.group(null))
    assertThrows(classOf[NullPointerException], () => builder.name(null))
    assertThrows(classOf[NullPointerException], () => builder.name(null, 0))
    assertThrows(
      classOf[NullPointerException],
      () => builder.uncaughtExceptionHandler(null)
    )
    assertThrows(classOf[NullPointerException], () => builder.unstarted(null))
    assertThrows(classOf[NullPointerException], () => builder.start(null))
  }

  @Test def testNulls2(): Unit = {
    val builder = Thread.ofVirtual()
    assertThrows(classOf[NullPointerException], () => builder.name(null))
    assertThrows(classOf[NullPointerException], () => builder.name(null, 0))
    assertThrows(
      classOf[NullPointerException],
      () => builder.uncaughtExceptionHandler(null)
    )
    assertThrows(classOf[NullPointerException], () => builder.unstarted(null))
    assertThrows(classOf[NullPointerException], () => builder.start(null))
  }
}

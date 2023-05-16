/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util._
import java.util.concurrent._
import java.util.concurrent.TimeUnit._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.Platform
import JSR166Test._

class ExecutorsTest extends JSR166Test {

  /** A newCachedThreadPool can execute runnables
   */
  @Test def testNewCachedThreadPool1(): Unit =
    usingPoolCleaner(Executors.newCachedThreadPool) { e =>
      e.execute(new NoOpRunnable)
      e.execute(new NoOpRunnable)
      e.execute(new NoOpRunnable)
    }

  /** A newCachedThreadPool with given ThreadFactory can execute runnables
   */
  @Test def testNewCachedThreadPool2(): Unit = usingPoolCleaner(
    Executors.newCachedThreadPool(new SimpleThreadFactory)
  ) { e =>
    e.execute(new NoOpRunnable)
    e.execute(new NoOpRunnable)
    e.execute(new NoOpRunnable)
  }

  /** A newCachedThreadPool with null ThreadFactory throws NPE
   */
  @Test def testNewCachedThreadPool3(): Unit = {
    try {
      val unused = Executors.newCachedThreadPool(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** A new SingleThreadExecutor can execute runnables
   */
  @Test def testNewSingleThreadExecutor1(): Unit =
    usingPoolCleaner(Executors.newSingleThreadExecutor) { e =>
      e.execute(new NoOpRunnable)
      e.execute(new NoOpRunnable)
      e.execute(new NoOpRunnable)
    }

  /** A new SingleThreadExecutor with given ThreadFactory can execute runnables
   */
  @Test def testNewSingleThreadExecutor2(): Unit = usingPoolCleaner(
    Executors.newSingleThreadExecutor(new SimpleThreadFactory)
  ) { e =>
    e.execute(new NoOpRunnable)
    e.execute(new NoOpRunnable)
    e.execute(new NoOpRunnable)
  }

  /** A new SingleThreadExecutor with null ThreadFactory throws NPE
   */
  @Test def testNewSingleThreadExecutor3(): Unit = {
    try {
      val unused = Executors.newSingleThreadExecutor(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** A new SingleThreadExecutor cannot be casted to concrete implementation
   */
  @Test def testCastNewSingleThreadExecutor(): Unit =
    usingPoolCleaner(Executors.newSingleThreadExecutor) { e =>
      try {
        val tpe = e.asInstanceOf[ThreadPoolExecutor]
        shouldThrow()
      } catch {
        case success: ClassCastException => ()
      }
    }

  /** A new newFixedThreadPool can execute runnables
   */
  @Test def testNewFixedThreadPool1(): Unit =
    usingPoolCleaner(Executors.newFixedThreadPool(2)) { e =>
      e.execute(new NoOpRunnable)
      e.execute(new NoOpRunnable)
      e.execute(new NoOpRunnable)
    }

  /** A new newFixedThreadPool with given ThreadFactory can execute runnables
   */
  @Test def testNewFixedThreadPool2(): Unit = usingPoolCleaner(
    Executors.newFixedThreadPool(2, new SimpleThreadFactory)
  ) { e =>
    e.execute(new NoOpRunnable)
    e.execute(new NoOpRunnable)
    e.execute(new NoOpRunnable)
  }

  /** A new newFixedThreadPool with null ThreadFactory throws
   *  NullPointerException
   */
  @Test def testNewFixedThreadPool3(): Unit = {
    try {
      val unused = Executors.newFixedThreadPool(2, null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** A new newFixedThreadPool with 0 threads throws IllegalArgumentException
   */
  @Test def testNewFixedThreadPool4(): Unit = {
    try {
      val unused = Executors.newFixedThreadPool(0)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** An unconfigurable newFixedThreadPool can execute runnables
   */
  @Test def testUnconfigurableExecutorService(): Unit = usingPoolCleaner(
    Executors.unconfigurableExecutorService(Executors.newFixedThreadPool(2))
  ) { e =>
    e.execute(new NoOpRunnable)
    e.execute(new NoOpRunnable)
    e.execute(new NoOpRunnable)
  }

  /** unconfigurableExecutorService(null) throws NPE
   */
  @Test def testUnconfigurableExecutorServiceNPE(): Unit = {
    try {
      val unused =
        Executors.unconfigurableExecutorService(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** unconfigurableScheduledExecutorService(null) throws NPE
   */
  @Test def testUnconfigurableScheduledExecutorServiceNPE(): Unit = {
    try {
      val unused =
        Executors.unconfigurableScheduledExecutorService(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** a newSingleThreadScheduledExecutor successfully runs delayed task
   */
  @throws[Exception]
  @Test def testNewSingleThreadScheduledExecutor(): Unit =
    usingPoolCleaner(Executors.newSingleThreadScheduledExecutor) { p =>
      val proceed = new CountDownLatch(1)
      val task = new CheckedRunnable() {
        override def realRun(): Unit = { await(proceed) }
      }
      val startTime = System.nanoTime
      val f = p.schedule(
        Executors.callable(task, java.lang.Boolean.TRUE),
        timeoutMillis(),
        MILLISECONDS
      )
      assertFalse(f.isDone)
      proceed.countDown()
      assertSame(java.lang.Boolean.TRUE, f.get(LONG_DELAY_MS, MILLISECONDS))
      assertSame(java.lang.Boolean.TRUE, f.get)
      assertTrue(f.isDone)
      assertFalse(f.isCancelled)
      assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    }

  /** a newScheduledThreadPool successfully runs delayed task
   */
  @throws[Exception]
  @Test def testNewScheduledThreadPool(): Unit =
    usingPoolCleaner(Executors.newScheduledThreadPool(2)) { p =>
      val proceed = new CountDownLatch(1)
      val task = new CheckedRunnable() {
        override def realRun(): Unit = { await(proceed) }
      }
      val startTime = System.nanoTime
      val f = p.schedule(
        Executors.callable(task, java.lang.Boolean.TRUE),
        timeoutMillis(),
        MILLISECONDS
      )
      assertFalse(f.isDone)
      proceed.countDown()
      assertSame(java.lang.Boolean.TRUE, f.get(LONG_DELAY_MS, MILLISECONDS))
      assertSame(java.lang.Boolean.TRUE, f.get)
      assertTrue(f.isDone)
      assertFalse(f.isCancelled)
      assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    }

  /** an unconfigurable newScheduledThreadPool successfully runs delayed task
   */
  @throws[Exception]
  @Test def testUnconfigurableScheduledExecutorService(): Unit =
    usingPoolCleaner(
      Executors.unconfigurableScheduledExecutorService(
        Executors.newScheduledThreadPool(2)
      )
    ) { p =>
      val proceed = new CountDownLatch(1)
      val task = new CheckedRunnable() {
        override def realRun(): Unit = { await(proceed) }
      }
      val startTime = System.nanoTime
      val f = p.schedule(
        Executors.callable(task, java.lang.Boolean.TRUE),
        timeoutMillis(),
        MILLISECONDS
      )
      assertFalse(f.isDone)
      proceed.countDown()
      assertSame(java.lang.Boolean.TRUE, f.get(LONG_DELAY_MS, MILLISECONDS))
      assertSame(java.lang.Boolean.TRUE, f.get)
      assertTrue(f.isDone)
      assertFalse(f.isCancelled)
      assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    }

  /** Future.get on submitted tasks will time out if they compute too long.
   */
  @throws[Exception]
  @Test def testTimedCallable(): Unit = {
    val executors = Array(
      Executors.newSingleThreadExecutor,
      Executors.newCachedThreadPool,
      Executors.newFixedThreadPool(2),
      Executors.newScheduledThreadPool(2)
    )
    val done = new CountDownLatch(1)
    val sleeper = new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = { done.await(LONG_DELAY_MS, MILLISECONDS) }
    }
    val threads = new ArrayList[Thread]
    executors.foreach { executor =>
      threads.add(newStartedThread(new CheckedRunnable() {
        override def realRun(): Unit = {
          val future = executor.submit(sleeper)
          assertFutureTimesOut(future)
        }
      }))
    }
    threads.forEach(awaitTermination(_))
    done.countDown()
    executors.foreach(joinPool(_))
  }

  /** ThreadPoolExecutor using defaultThreadFactory has specified group,
   *  priority, daemon status, and name
   */
  @throws[Exception]
  @Test def testDefaultThreadFactory(): Unit = {
    val egroup = Thread.currentThread.getThreadGroup
    val done = new CountDownLatch(1)
    val r = new CheckedRunnable() {
      override def realRun(): Unit = {
        try {
          val current = Thread.currentThread
          assertFalse(current.isDaemon)
          assertTrue(current.getPriority <= Thread.NORM_PRIORITY)
          // val s = System.getSecurityManager
          assertSame(
            current.getThreadGroup,
            // if (s == null)
            egroup
            // else s.getThreadGroup
          )
          assertTrue(current.getName.endsWith("thread-1"))
        } catch {
          case ok: SecurityException =>

          // Also pass if not allowed to change setting
        }
        done.countDown()
      }
    }
    usingPoolCleaner(
      Executors.newSingleThreadExecutor(Executors.defaultThreadFactory)
    ) { e =>
      e.execute(r)
      await(done)
    }
  }

  // @Test def testPrivilegedThreadFactory(): Unit = ???
  // @Test def testCreatePrivilegedCallableUsingCCLWithNoPrivs(): Unit = ???
  // @Test def testPrivilegedCallableUsingCCLWithPrivs(): Unit = ???
  // @Test def testPrivilegedCallableWithNoPrivs(): Unit = ???
  // @Test def testPrivilegedCallableWithPrivs(): Unit = ???

  /** callable(Runnable) returns null when called
   */
  @throws[Exception]
  @Test def testCallable1(): Unit = {
    val c = Executors.callable(new NoOpRunnable)
    assertNull(c.call)
  }

  /** callable(Runnable, result) returns result when called
   */
  @throws[Exception]
  @Test def testCallable2(): Unit = {
    val c =
      Executors.callable(new NoOpRunnable, one)
    assertSame(one, c.call)
  }

  // privilagged callable
  // @Test def testCallable3(): Unit = ???
  // @Test def testCallable4(): Unit = ???

  /** callable(null Runnable) throws NPE
   */
  @Test def testCallableNPE1(): Unit = {
    try {
      val unused = Executors.callable(null.asInstanceOf[Runnable])
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  /** callable(null, result) throws NPE
   */
  @Test def testCallableNPE2(): Unit = {
    try {
      val unused = Executors.callable(null.asInstanceOf[Runnable], one)
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  // privilleged
  // @Test def testCallableNPE3(): Unit = ???
  // @Test def testCallableNPE4(): Unit = ???

  /** callable(runnable, x).toString() contains toString of wrapped task
   */
  @Test def testCallable_withResult_toString(): Unit = {
    if (testImplementationDetails) {
      assumeFalse(
        "Implementation change since JDK 11",
        Platform.executingInJVMOnLowerThenJDK11
      )
      val r: Runnable = () => {
        def foo() = {}
        foo()
      }
      val c = Executors.callable(r, "")
      assertEquals(
        identityString(c) + "[Wrapped task = " + r.toString + "]",
        c.toString
      )
    }
  }

  /** callable(runnable).toString() contains toString of wrapped task
   */
  @Test def testCallable_toString(): Unit = {
    if (testImplementationDetails) {
      assumeFalse(
        "Implementation change since JDK 11",
        Platform.executingInJVMOnLowerThenJDK11
      )
      val r: Runnable = () => {
        def foo() = {}
        foo()
      }
      val c = Executors.callable(r)
      assertEquals(
        identityString(c) + "[Wrapped task = " + r.toString + "]",
        c.toString
      )
    }
  }

  /** privilegedCallable(callable).toString() contains toString of wrapped task
   */
  @deprecated @Test def testPrivilegedCallable_toString(): Unit = {
    if (testImplementationDetails) {
      assumeFalse(
        "Implementation change since JDK 11",
        Platform.executingInJVMOnLowerThenJDK11
      )
      val c: Callable[String] = () => ""
      val priv = Executors.privilegedCallable(c)
      assertEquals(
        identityString(priv) + "[Wrapped task = " + c.toString + "]",
        priv.toString
      )
    }
  }

  /** privilegedCallableUsingCurrentClassLoader(callable).toString() contains
   *  toString of wrapped task
   */
  @deprecated @Test def testPrivilegedCallableUsingCurrentClassLoader_toString()
      : Unit = {
    if (testImplementationDetails) {
      assumeFalse(
        "Implementation change since JDK 11",
        Platform.executingInJVMOnLowerThenJDK11
      )
      val c: Callable[String] = () => ""
      val priv =
        Executors.privilegedCallableUsingCurrentClassLoader(c)
      assertEquals(
        identityString(priv) + "[Wrapped task = " + c.toString + "]",
        priv.toString
      )
    }
  }
}

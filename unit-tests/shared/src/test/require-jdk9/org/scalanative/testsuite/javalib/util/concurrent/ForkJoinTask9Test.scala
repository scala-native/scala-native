/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util
import java.util.concurrent._

import org.junit.Assert._
import org.junit._

class ForkJoinTask9Test extends JSR166Test {
  import ForkJoinTask8Test._
  import JSR166Test._

  /** pollSubmission returns unexecuted submitted task, if present */
  @Test def testPollSubmission(): Unit = {
    val done = new CountDownLatch(1)
    val a = ForkJoinTask.adapt(awaiter(done))
    val b = ForkJoinTask.adapt(awaiter(done))
    val c = ForkJoinTask.adapt(awaiter(done))
    val p = singletonPool
    usingWrappedPoolCleaner(singletonPool)(cleaner(_, done)) { p =>
      val external = new Thread({ () =>
        p.execute(a)
        p.execute(b)
        p.execute(c)
      }: CheckedRunnable)
      val s = new CheckedRecursiveAction() {
        protected def realCompute(): Unit = {
          external.start()
          try external.join()
          catch {
            case ex: Exception =>
              threadUnexpectedException(ex)
          }
          assertTrue(p.hasQueuedSubmissions)
          assertTrue(Thread.currentThread.isInstanceOf[ForkJoinWorkerThread])
          val r = ForkJoinTask.pollSubmission()
          assertTrue((r eq a) || (r eq b) || (r eq c))
          assertFalse(r.isDone)
        }
      }
      p.invoke(s)
    }
  }
}

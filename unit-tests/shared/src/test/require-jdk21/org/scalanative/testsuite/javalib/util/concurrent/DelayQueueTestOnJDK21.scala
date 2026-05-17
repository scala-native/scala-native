/* The Java 8 unit-tests DelayQueueTest was ported from
 * JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 * file: Unknown, chosen by ChatGPT 5.5.
 * revision Unknown, dated: Unknown
 *
 * This file contains original 'post JSR-166' tests for the
 * 'DelayQueue#remove()' overload introduced in JDK 21.
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.{DelayQueue, Delayed, TimeUnit}
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

object DelayQueueTestOnJDK21 {
  import JSR166Test._

  /* SN JDK21: Make class abstract so that Tests below can
   * tune getDelay() to expired or not. Class was final in JSR-166.
   *
   * Rest of class is identical to JDK 8 DelayQueueTest. Leaving the
   * majority of the declaration the same aids tracing if such becomes
   * necessary.
   */
  abstract class PDelay(val pseudodelay: Int) extends Delayed {
    override def compareTo(y: Delayed): Int =
      Integer.compare(this.pseudodelay, y.asInstanceOf[PDelay].pseudodelay)

    override def equals(other: Any): Boolean =
      other.isInstanceOf[PDelay] &&
        this.pseudodelay == other.asInstanceOf[PDelay].pseudodelay

    override def hashCode(): Int = pseudodelay

    override def getDelay(unit: TimeUnit): Long
    Int.MinValue.toLong + pseudodelay

    override def toString(): String = String.valueOf(pseudodelay)
  }
}

class DelayQueueTestOnJDK21 extends JSR166Test {
  import DelayQueueTestOnJDK21._
  import JSR166Test._

  /* From the JDK 26 API documentation:
   *   Retrieves and removes the expired head of this queue, or throws
   *   an exception if this queue has no expired elements.
   */

  @Test def testRemoveJdk21Overload_ExpiredHeadTrue(): Unit = {
    val q = new DelayQueue[PDelay]()

    val pd_1 = new PDelay(1) {
      override def getDelay(unit: TimeUnit): Long =
        jl.Long.MIN_VALUE + 1
    }

    val pd_0 = new PDelay(0) {
      override def getDelay(unit: TimeUnit): Long =
        jl.Long.MIN_VALUE
    }

    // Insert in reverse delay order to check that priority sort is done.
    assertTrue("offer(pd_1)", q.offer(pd_1))
    assertTrue("offer(pd_0)", q.offer(pd_0))

    mustEqual(pd_0, q.remove())
  }

  @Test def testRemoveJdk21OverloadExpiredHeadFalse(): Unit = {
    val q = new DelayQueue[PDelay]()

    val pd = new PDelay(0) {
      override def getDelay(unit: TimeUnit): Long =
        jl.Long.MAX_VALUE
    }

    assertTrue("offer(pd)", q.offer(pd))

    try {
      q.remove()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
  }
}

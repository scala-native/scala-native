/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import java.util.function._

import org.junit.Assert._
import org.junit._

object CountedCompleter8Test {

  /** CountedCompleter class javadoc code sample, version 1. */
  def forEach1[E](array: Array[E], action: Consumer[E]): Unit = {
    class Task(parent: Task, val lo: Int, val hi: Int)
        extends CountedCompleter[Void](parent) {
      override def compute(): Unit = {
        if (hi - lo >= 2) {
          val mid = (lo + hi) >>> 1
          // must set pending count before fork
          setPendingCount(2)
          new Task(this, mid, hi).fork // right child

          new Task(this, lo, mid).fork // left child

        } else if (hi > lo) action.accept(array(lo))
        tryComplete()
      }
    }
    new Task(null, 0, array.length).invoke
  }

  /** CountedCompleter class javadoc code sample, version 2. */
  def forEach2[E](array: Array[E], action: Consumer[E]): Unit = {
    class Task(parent: Task, val lo: Int, val hi: Int)
        extends CountedCompleter[Void](parent) {
      override def compute(): Unit = {
        if (hi - lo >= 2) {
          val mid = (lo + hi) >>> 1
          setPendingCount(1) // looks off by one, but correct!
          new Task(this, mid, hi).fork // right child
          new Task(this, lo, mid).compute() // direct invoke
        } else {
          if (hi > lo) action.accept(array(lo))
          tryComplete()
        }
      }
    }
    new Task(null, 0, array.length).invoke
  }

  /** CountedCompleter class javadoc code sample, version 3. */
  def forEach3[E](array: Array[E], action: Consumer[E]): Unit = {
    class Task(parent: Task, val lo: Int, val hi: Int)
        extends CountedCompleter[Void](parent) {
      override def compute(): Unit = {
        var n = hi - lo

        while (n >= 2) {
          addToPendingCount(1)
          new Task(this, lo + n / 2, lo + n).fork
          n /= 2
        }
        if (n > 0) action.accept(array(lo))
        propagateCompletion()
      }
    }
    new Task(null, 0, array.length).invoke
  }

  /** CountedCompleter class javadoc code sample, version 4. */
  def forEach4[E](array: Array[E], action: Consumer[E]): Unit = {
    class Task(parent: Task, val lo: Int, val hi: Int)
        extends CountedCompleter[Void](
          parent,
          31 - Integer.numberOfLeadingZeros(hi - lo)
        ) {
      override def compute(): Unit = {
        var n = hi - lo
        while (n >= 2) {
          new Task(this, lo + n / 2, lo + n).fork
          n /= 2
        }
        action.accept(array(lo))
        propagateCompletion()
      }
    }
    if (array.length > 0) new Task(null, 0, array.length).invoke
  }
}
class CountedCompleter8Test extends JSR166Test {
  def testRecursiveDecomposition(
      action: BiConsumer[Array[Integer], Consumer[Integer]]
  ): Unit = {
    val n = ThreadLocalRandom.current.nextInt(8)
    val a = Array.tabulate[Integer](n)(_ + 1)
    // val a = new Array[Integer](n)
    // for (i <- 0 until n) { a(i) = i + 1 }
    val ai = new AtomicInteger(0)
    action.accept(a, ai.addAndGet(_))
    assertEquals(n * (n + 1) / 2, ai.get())
  }

  /** Variants of divide-by-two recursive decomposition into leaf tasks, as
   *  described in the CountedCompleter class javadoc code samples
   */
  @Test def testRecursiveDecomposition(): Unit = {
    testRecursiveDecomposition(CountedCompleter8Test.forEach1)
    testRecursiveDecomposition(CountedCompleter8Test.forEach2)
    testRecursiveDecomposition(CountedCompleter8Test.forEach3)
    testRecursiveDecomposition(CountedCompleter8Test.forEach4)
  }
}

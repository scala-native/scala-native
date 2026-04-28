/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util

import java.util.{AbstractQueue, Arrays, Iterator, NoSuchElementException}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.javalib.util.concurrent.{Item, JSR166Test}
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class AbstractQueueTest extends JSR166Test {
  import JSR166Test._

  class Succeed extends AbstractQueue[Item] {
    private val item = itemFor(1)

    override def offer(x: Item): Boolean = {
      if (x == null) throw new NullPointerException()
      true
    }
    override def peek(): Item = item
    override def poll(): Item = item
    override def size(): Int = 0
    override def iterator(): Iterator[Item] = null
  }

  class Fail extends AbstractQueue[Item] {
    override def offer(x: Item): Boolean = {
      if (x == null) throw new NullPointerException()
      false
    }
    override def peek(): Item = null
    override def poll(): Item = null
    override def size(): Int = 0
    override def iterator(): Iterator[Item] = null
  }

  @Test def testAddS(): Unit = {
    val q = new Succeed()
    assertTrue(q.add(two))
  }

  @Test def testAddF(): Unit = {
    val q = new Fail()
    assertThrows(classOf[IllegalStateException], q.add(one))
  }

  @Test def testAddNPE(): Unit = {
    val q = new Succeed()
    assertThrows(classOf[NullPointerException], q.add(null))
  }

  @Test def testRemoveS(): Unit = {
    val q = new Succeed()
    assertSame(itemFor(1), q.remove())
  }

  @Test def testRemoveF(): Unit = {
    val q = new Fail()
    assertThrows(classOf[NoSuchElementException], q.remove())
  }

  @Test def testElementS(): Unit = {
    val q = new Succeed()
    assertSame(itemFor(1), q.element())
  }

  @Test def testElementF(): Unit = {
    val q = new Fail()
    assertThrows(classOf[NoSuchElementException], q.element())
  }

  @Test def testAddAll1(): Unit = {
    val q = new Succeed()
    assertThrows(classOf[NullPointerException], q.addAll(null))
  }

  @Test def testAddAllSelf(): Unit = {
    val q = new Succeed()
    assertThrows(classOf[IllegalArgumentException], q.addAll(q))
  }

  @Test def testAddAll2(): Unit = {
    val q = new Succeed()
    val items = new Array[Item](SIZE)
    assertThrows(classOf[NullPointerException], q.addAll(Arrays.asList(items: _*)))
  }

  @Test def testAddAll3(): Unit = {
    val q = new Succeed()
    val items = new Array[Item](SIZE)
    for (i <- 0 until SIZE - 1) items(i) = itemFor(i)
    assertThrows(classOf[NullPointerException], q.addAll(Arrays.asList(items: _*)))
  }

  @Test def testAddAll4(): Unit = {
    val q = new Fail()
    val items = seqItems(SIZE)
    assertThrows(classOf[IllegalStateException], q.addAll(Arrays.asList(items: _*)))
  }
}

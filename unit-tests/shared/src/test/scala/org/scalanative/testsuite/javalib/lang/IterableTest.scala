// Ported from Scala.js commit: 6819668 dated: 2020-10-07

package org.scalanative.testsuite.javalib.lang

import java.lang.Iterable as JIterable
import java.util as ju
import java.util.function.Consumer

import scala.reflect.ClassTag

import org.junit.Test
import org.junit.Assert.*

/** Tests the implementation of the java standard library Iterable
 */
trait IterableTest {

  def factory: IterableFactory

  @Test def empty(): Unit = {
    val iter = factory.fromElements[Int]()
    var hit = false
    iter.forEach(new Consumer[Int] {
      def accept(x: Int): Unit = hit = true
    })
    assertTrue(!hit)
  }

  @Test def simpleSum(): Unit = {
    val iter = factory.fromElements[Int](42, 50, 12, 0, -45, 102, 32, 75)
    var sum = 0
    iter.forEach(new Consumer[Int] {
      def accept(x: Int): Unit = sum = sum + x
    })
    assertEquals(268, sum)
  }
}

class IterableDefaultTest extends IterableTest {
  def factory: IterableFactory = new IterableFactory {
    override def implementationName: String = "java.lang.Iterable"

    def empty[E: ClassTag]: JIterable[E] = {
      new JIterable[E] {
        override def iterator(): ju.Iterator[E] = {
          new ju.Iterator[E] {
            override def hasNext(): Boolean = false
            override def next(): E = throw new NoSuchElementException()
          }
        }
      }
    }

    def fromElements[E: ClassTag](elems: E*): JIterable[E] = {
      new JIterable[E] {
        override def iterator(): ju.Iterator[E] = {
          val l: Iterator[E] = elems.iterator
          new ju.Iterator[E] {
            override def hasNext(): Boolean = l.hasNext
            override def next(): E = l.next()
          }
        }
      }
    }
  }
}

trait IterableFactory {
  def implementationName: String

  def empty[E: ClassTag]: JIterable[E]

  def fromElements[E: ClassTag](elems: E*): JIterable[E]
}

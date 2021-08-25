// Ported from Scala.js commit: 7fd9ebb dated: 2020=01-06

package org.scalanative.testsuite.javalib.util.function

import java.util.function.Consumer

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssertThrows.assertThrows

class ConsumerTest {
  import ConsumerTest._

  @Test def accept(): Unit = {
    // Side-effects
    var current: Int = 0
    val add = makeConsumer[Int](num => current += num)

    add.accept(1)
    assertTrue(current == 1)

    add.accept(2)
    assertTrue(current == 3)
  }

  @Test def andThen(): Unit = {
    // Side-effects
    var current: Int = 0
    val add = makeConsumer[Int](num => current += num)
    val multiply = makeConsumer[Int](num => current *= num)
    val addAndMultiply = add.andThen(multiply)

    addAndMultiply.accept(2)
    assertTrue(current == 4)

    addAndMultiply.accept(3)
    assertTrue(current == 21)

    // Sequential operations
    val throwingConsumer =
      makeConsumer[Any](x => throw new ThrowingConsumerException(x))
    val dontCallConsumer =
      makeConsumer[Any](x =>
        throw new AssertionError(s"dontCallConsumer.accept($x)")
      )

    assertThrows(
      classOf[ThrowingConsumerException],
      throwingConsumer.andThen(dontCallConsumer).accept(0)
    )

    assertThrows(
      classOf[ThrowingConsumerException],
      add.andThen(throwingConsumer).accept(1)
    )
    assertTrue(current == 22)
  }
}

object ConsumerTest {
  final class ThrowingConsumerException(x: Any)
      extends Exception(s"throwing consumer called with $x")

  def makeConsumer[T](f: T => Unit): Consumer[T] = {
    new Consumer[T] {
      def accept(t: T): Unit = f(t)
    }
  }
}

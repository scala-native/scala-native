// Ported from Scala.js commit: d3a9711 dated: 2020-09-06

package org.scalanative.testsuite.javalib.util.function

import java.util.function.{Function, BiFunction}

import org.junit.Assert._
import org.junit.Test

class BiFunctionTest {
  import BiFunctionTest._

  @Test def createAndApply(): Unit = {
    assertEquals(3, addBiFunc(1, 2))
  }

  @Test def andThen(): Unit = {
    assertEquals(4, addBiFunc.andThen(incFunc)(1, 2))
  }
}

object BiFunctionTest {
  private val addBiFunc: BiFunction[Int, Int, Int] = {
    new BiFunction[Int, Int, Int] {
      def apply(t: Int, u: Int): Int = t + u
    }
  }

  private val incFunc: Function[Int, Int] = {
    new Function[Int, Int] {
      def apply(t: Int): Int = t + 1
    }
  }
}

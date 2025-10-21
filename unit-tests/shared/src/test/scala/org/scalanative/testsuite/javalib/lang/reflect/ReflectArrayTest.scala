/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalanative.testsuite.javalib.lang.reflect

import scala.runtime.BoxedUnit

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform.executingInJVM

class ReflectArrayTest {

  @Test def newInstance(): Unit = {
    testNewInstance(classOf[Int], classOf[Array[Int]], 0)
    testNewInstance(classOf[Char], classOf[Array[Char]], '\u0000')
    testNewInstance(classOf[Long], classOf[Array[Long]], 0L)
    testNewInstance(classOf[Boolean], classOf[Array[Boolean]], false)

    testNewInstance(classOf[BoxedUnit], classOf[Array[Unit]], null) // yes, null

    testNewInstance(classOf[Object], classOf[Array[Object]], null)
    testNewInstance(classOf[String], classOf[Array[String]], null)

    testNewInstance(
      classOf[java.lang.Integer],
      classOf[Array[java.lang.Integer]],
      null
    )
    testNewInstance(
      classOf[java.lang.Long],
      classOf[Array[java.lang.Long]],
      null
    )

    testNewInstance(classOf[Array[Object]], classOf[Array[Array[Object]]], null)
    testNewInstance(classOf[Array[Int]], classOf[Array[Array[Int]]], null)
    testNewInstance(classOf[Array[String]], classOf[Array[Array[String]]], null)
  }

  @Test def newInstanceMultipleDimension(): Unit = {
    testNewInstanceMultipleDims(classOf[Int], classOf[Array[Int]], 0)
    testNewInstanceMultipleDims(classOf[Char], classOf[Array[Char]], '\u0000')
    testNewInstanceMultipleDims(classOf[Long], classOf[Array[Long]], 0L)
    testNewInstanceMultipleDims(
      classOf[Boolean],
      classOf[Array[Boolean]],
      false
    )

    testNewInstanceMultipleDims(
      classOf[BoxedUnit],
      classOf[Array[Unit]],
      null
    ) // yes, null

    testNewInstanceMultipleDims(classOf[Object], classOf[Array[Object]], null)
    testNewInstanceMultipleDims(classOf[String], classOf[Array[String]], null)

    testNewInstanceMultipleDims(
      classOf[java.lang.Integer],
      classOf[Array[java.lang.Integer]],
      null
    )
    testNewInstanceMultipleDims(
      classOf[java.lang.Long],
      classOf[Array[java.lang.Long]],
      null
    )

    testNewInstanceMultipleDims(
      classOf[Array[Object]],
      classOf[Array[Array[Object]]],
      null
    )
    testNewInstanceMultipleDims(
      classOf[Array[Int]],
      classOf[Array[Array[Int]]],
      null
    )
    testNewInstanceMultipleDims(
      classOf[Array[String]],
      classOf[Array[Array[String]]],
      null
    )
  }

  @inline
  def testNewInstanceMultipleDims(
      clazz: Class[_],
      expectedClazz: Class[_],
      sampleElem: Any
  ): Unit = {
    for {
      dims <- List(
        Seq(0),
        Seq(1),
        Seq(1, 2, 3),
        Seq.fill(if (clazz.isArray) 254 else 255)(1) // Maximal dimension
      )
    } {
      testNewInstanceNoInline(clazz, dims, expectedClazz, sampleElem)
      testBase(clazz, dims, expectedClazz, sampleElem)
    }
  }

  @inline
  private def testBase(
      clazz: Class[_],
      length: Int,
      expectedClazz: Class[_],
      sampleElem: Any
  ): Unit = {
    val array =
      java.lang.reflect.Array
        .newInstance(clazz, length)
        .asInstanceOf[Array[_]]
    assertEquals(expectedClazz, array.getClass)
    assertTrue(array.getClass.isArray)
    assertEquals(length, array.length)
    for (i <- 0 until array.length)
      assertEquals(sampleElem, array(i))
  }

  @noinline
  private def testNewInstanceNoInline(
      clazz: Class[_],
      length: Int,
      expectedClazz: Class[_],
      sampleElem: Any
  ): Unit = {
    testBase(clazz, length, expectedClazz, sampleElem)
  }

  @inline
  def testNewInstance(
      clazz: Class[_],
      expectedClazz: Class[_],
      sampleElem: Any
  ): Unit = {
    testNewInstanceNoInline(clazz, length = 2, expectedClazz, sampleElem)
    testBase(clazz, length = 2, expectedClazz, sampleElem)

    testNewInstanceNoInline(clazz, length = 0, expectedClazz, sampleElem)
    testBase(clazz, length = 0, expectedClazz, sampleElem)
  }

  @noinline
  private def testNewInstanceNoInline(
      clazz: Class[_],
      dimensions: Seq[Int],
      expectedClazz: Class[_],
      sampleElem: Any
  ): Unit = {
    testBase(clazz, dimensions, expectedClazz, sampleElem)
  }

  @inline
  private def testBase(
      clazz: Class[_],
      dimensions: Seq[Int],
      expectedClazz: Class[_],
      sampleElem: Any
  ): Unit = {
    val array =
      java.lang.reflect.Array
        .newInstance(clazz, dimensions: _*)
        .asInstanceOf[Array[_]]
    // non tail recursive
    def check(dimensions: List[Int], array: Array[_]): Unit = {
      assertTrue(array.getClass.isArray)
      dimensions match {
        case Nil            => ()
        case current :: Nil =>
          assertEquals(expectedClazz, array.getClass)
          for (i <- 0 until array.length) {
            assertEquals("incorrect array size", current, array.length)
            assertEquals(sampleElem, array(i))
          }
        case current :: next =>
          if (!executingInJVM) {
            // Scala Native does not distinguish Array[Array[Int]] from Array[Array[Array[Int]]]
            // All recursive arrays are stored as Array[AnyRef]
            assertEquals(classOf[Array[AnyRef]], array.getClass)
          }
          for (i <- 0 until array.length) {
            assertEquals("incorrect array size", current, array.length)
            check(next, array(i).asInstanceOf[Array[_]])
          }
      }
    }
    check(dimensions.toList, array)
  }

}

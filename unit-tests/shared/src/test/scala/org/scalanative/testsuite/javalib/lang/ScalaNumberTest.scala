package org.scalanative.testsuite.javalib.lang

import java.lang.*

//  Exercise __scala_==

import org.junit.Test
import org.junit.Assert.*

@deprecated class ScalaNumberTest {

  @Test def bigIntEqualEqualBigInt(): Unit = {
    val token = 2047L
    val sbi1: scala.math.BigInt = scala.math.BigInt(token)
    val sbi2: scala.math.BigInt = scala.math.BigInt(token)

    assertTrue(sbi1 == sbi2)
  }

  @Test def bigIntEqualsBigInt(): Unit = {
    val token = 2047L
    val sbi1: scala.math.BigInt = scala.math.BigInt(token)
    val sbi2: scala.math.BigInt = scala.math.BigInt(token)

    assertTrue(sbi1.equals(sbi2))
  }

  @Test def bigIntDoesNotEqualEqualBigIntWithDifferentValue(): Unit = {
    val token = 2047L
    val sbi1: scala.math.BigInt = scala.math.BigInt(token)
    // avoid powers of 2 because of possible caching.
    val sbi2: scala.math.BigInt = scala.math.BigInt(token + 2)

    assertFalse(sbi1 == sbi2)
  }

  @Test def bigIntEqualEqualJavaLong(): Unit = {
    val token = Int.MaxValue + 2L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val jl: java.lang.Long = new java.lang.Long(token.toString)

    assertTrue(sbi == jl)
  }

  @Test def bigIntDoesNotEqualEqualJavaLongWithDifferentValue(): Unit = {
    val token = Int.MaxValue + 2L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val jl: java.lang.Long = new java.lang.Long((token + 2).toString)

    assertFalse(sbi == jl)
  }

  @Test def javaLongEqualEqualBigInt(): Unit = {
    val token = Int.MaxValue + 2L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val jl: java.lang.Long = new java.lang.Long(token.toString)

    assertTrue(jl == sbi)
  }

  @Test def javaLongDoesNotEqualEqualBigIntWithDifferentValue(): Unit = {
    val token = Int.MaxValue + 2L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val jl: java.lang.Long = new java.lang.Long((token + 2).toString)

    assertFalse(jl == sbi)
  }

  @Test def javaLongEqualEqualJavaLong(): Unit = {
    val token = 2047
    val jl1: java.lang.Long = new java.lang.Long(token)
    val jl2: java.lang.Long = new java.lang.Long(token)

    assertTrue(jl1 == jl2)
  }

  @Test def javaLongDoesNotEqualEqualJavaLongWithDifferentValue(): Unit = {
    val token = 2047
    val jl1: java.lang.Long = new java.lang.Long(token)
    val jl2: java.lang.Long = new java.lang.Long(token + 2)

    assertFalse(jl1 == jl2)
  }

  @Test def bigIntEqualEqualJavaInteger(): Unit = {
    val token = 2047L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val ji: java.lang.Integer = new java.lang.Integer(token.toString)

    assertTrue(sbi == ji)
  }

  @Test def bigIntDoesNotEqualEqualJavaIntegerWithDifferentValue(): Unit = {
    val token = 2047L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val ji: java.lang.Integer = new java.lang.Integer((token + 2).toString)

    assertFalse(sbi == ji)
  }

  @Test def javaIntegerEqualEqualBigInt(): Unit = {
    val token = 2047L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val ji: java.lang.Integer = new java.lang.Integer(token.toString)

    assertTrue(ji == sbi)
  }

  @Test def javaIntegerDoesNotEqualEqualBigIntWithDifferentValue(): Unit = {
    val token = 2047L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val ji: java.lang.Integer = new java.lang.Integer((token + 2).toString)

    assertFalse(ji == sbi)
  }

  @Test def javaIntegerEqualEqualJavaInteger(): Unit = {
    val token = 2047
    val ji1: java.lang.Integer = new java.lang.Integer(token)
    val ji2: java.lang.Integer = new java.lang.Integer(token)

    assertTrue(ji1 == ji2)
  }

  @Test def javaIntegerDoesNotEqualEqualJavaIntegerWithDifferentValue()
      : Unit = {
    val token = 2047
    val ji1: java.lang.Integer = new java.lang.Integer(token)
    val ji2: java.lang.Integer = new java.lang.Integer(token + 2)

    assertFalse(ji1 == ji2)
  }

  @Test def bigDecimalEqualEqualBigDecimal(): Unit = {
    val token = 2046.5
    val sbd1: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val sbd2: scala.math.BigDecimal = scala.math.BigDecimal(token)

    assertTrue(sbd1 == sbd2)
  }

  @Test def bigDecimalEqualsBigDecimal(): Unit = {
    val token = 2046.5
    val sbd1: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val sbd2: scala.math.BigDecimal = scala.math.BigDecimal(token)

    assertTrue(sbd1.equals(sbd2))
  }

  @Test def bigDecimalDoesNotEqualEqualBigDecimalWithDifferentValue(): Unit = {
    val token = 2046.5
    val sbd1: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val sbd2: scala.math.BigDecimal = scala.math.BigDecimal(token - 2.0)

    assertFalse(sbd1 == sbd2)
  }

  @Test def bigDecimalEqualEqualJavaDouble(): Unit = {
    val token = 2046.5
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jd: java.lang.Double = new java.lang.Double(token.toString)

    assertTrue(sbd == jd)
  }

  @Test def bigDecimalDoesNotEqualEqualJavaDoubleWithDifferentValue(): Unit = {
    val token = 2046.5
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jd: java.lang.Double = new java.lang.Double((token - 2.0).toString)

    assertFalse(sbd == jd)
  }

  @Test def javaDoubleEqualEqualBigDecimal(): Unit = {
    val token = 2046.5
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jd: java.lang.Double = new java.lang.Double(token.toString)

    assertTrue(jd == sbd)
  }

  @Test def javaDoubleDoesNotEqualEqualBigDecimalWithDifferentValue(): Unit = {
    val token = 2046.5
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jd: java.lang.Double = new java.lang.Double((token - 2.0).toString)

    assertFalse(sbd == jd)
  }

  @Test def javaDoubleEqualEqualJavaDouble(): Unit = {
    val token = 2046.5
    val jd1: java.lang.Double = new java.lang.Double(token)
    val jd2: java.lang.Double = new java.lang.Double(token)

    assertTrue(jd1 == jd2)
  }

  @Test def javaDoubleDoesNotEqualEqualJavaDoubleWithDifferentValue(): Unit = {
    val token = 2046.5
    val jd1: java.lang.Double = new java.lang.Double(token)
    val jd2: java.lang.Double = new java.lang.Double((token - 2.0).toString)

    assertFalse(jd1 == jd2)
  }

  @Test def bigDecimalEqualEqualJavaFloat(): Unit = {
    val token = 2046.5f
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jf: java.lang.Float = new java.lang.Float(token.toString)

    assertTrue(sbd == jf)
  }

  @Test def bigDecimalDoesNotEqualEqualJavaFloatWithDifferentValue(): Unit = {
    val token = 2046.5f
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jf: java.lang.Float = new java.lang.Float((token - 2.0).toString)

    assertFalse(sbd == jf)
  }

  @Test def javaFloatEqualEqualBigDecimal(): Unit = {
    val token = 2046.5f
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jf: java.lang.Float = new java.lang.Float(token.toString)

    assertTrue(jf == sbd)
  }

  @Test def javaFloatDoesNotEqualEqualBigDecimalWithDifferentValue(): Unit = {
    val token = 2046.5f
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jf: java.lang.Float = new java.lang.Float((token - 2.0).toString)

    assertFalse(jf == sbd)
  }

  @Test def javaFloatEqualEqualJavaFloat(): Unit = {
    val token = 2046.5f
    val jf1: java.lang.Float = new java.lang.Float(token)
    val jf2: java.lang.Float = new java.lang.Float(token)

    assertTrue(jf1 == jf2)
  }

  @Test def javaFloatDoesNotEqualEqualJavaFloatWithDifferentValue(): Unit = {
    val token = 2046.5f
    val jf1: java.lang.Float = new java.lang.Float(token)
    val jf2: java.lang.Float = new java.lang.Float((token - 2.0).toString)

    assertFalse(jf1 == jf2)
  }
}

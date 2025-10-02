package org.scalanative.testsuite.javalib.lang

import java.lang._

import org.junit.{Test, BeforeClass}
import org.junit.Assert._
import org.junit.Assume._

import scala.scalanative.junit.utils.AssumesHelper

object StackTraceElementTest {
  @BeforeClass
  def checkRuntime() = {
    AssumesHelper.assumeSupportsStackTraces()
  }
}

class StackTraceDummy1 @noinline() {
  @noinline def dummy1: StackTraceElement =
    (new Exception).getStackTrace.head

  @noinline def _dummy2: StackTraceElement =
    (new Exception).getStackTrace.head

  @noinline def dummies: List[StackTraceElement] =
    (new Exception).getStackTrace.toList
}

class StackTraceDummy3_:: @noinline() {
  @noinline def dummy3: StackTraceElement =
    (new Exception).getStackTrace.head
}

class StackTraceDummy4 @noinline() {
  val dummy4: StackTraceElement =
    (new Exception).getStackTrace.head
}

class StackTraceElementTest {
  def dummy1 = {
    val dummy = (new StackTraceDummy1).dummy1
    val dummyStr = dummy.toString
    assertTrue(dummyStr, dummyStr.contains("StackTraceDummy"))
    dummy
  }
  def dummy2 = (new StackTraceDummy1)._dummy2
  def dummy3 = (new StackTraceDummy3_::).dummy3
  def dummy4 = (new StackTraceDummy4).dummy4

  @Test def getClassName(): Unit = {
    val prefix = "org.scalanative.testsuite.javalib.lang"
    assertEquals(s"$prefix.StackTraceDummy1", dummy1.getClassName)
    assertEquals(s"$prefix.StackTraceDummy1", dummy2.getClassName)
    assertEquals(
      s"$prefix" + ".StackTraceDummy3_$colon$colon",
      dummy3.getClassName
    )
    assertEquals(s"$prefix.StackTraceDummy4", dummy4.getClassName)
  }

  @Test def getMethodName(): Unit = {
    assertEquals("dummy1", dummy1.getMethodName)
    assertEquals("_dummy2", dummy2.getMethodName)
    assertEquals("dummy3", dummy3.getMethodName)
    assertEquals("<init>", dummy4.getMethodName)
  }

  @Test def isNativeMethod(): Unit = {
    assertFalse(dummy1.isNativeMethod)
    assertFalse(dummy2.isNativeMethod)
    assertFalse(dummy3.isNativeMethod)
    assertFalse(dummy4.isNativeMethod)
  }

  @Test def checkTrace(): Unit = {
    val dummies = (new StackTraceDummy1).dummies
    assertEquals(dummies.map(_.getClassName).mkString(","), "")
    assertEquals(dummies.map(_.getMethodName).mkString(","), "")
  }

}

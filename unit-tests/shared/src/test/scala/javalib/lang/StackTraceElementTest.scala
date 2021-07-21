package javalib.lang

import java.lang._

import org.junit.Test
import org.junit.Assert._

class StackTraceDummy1 @noinline() {
  def dummy1: StackTraceElement =
    (new Exception).getStackTrace
      .filter(_.toString.contains("StackTraceDummy"))
      .head

  def _dummy2: StackTraceElement =
    (new Exception).getStackTrace
      .filter(_.toString.contains("StackTraceDummy"))
      .head
}

class StackTraceDummy3_:: @noinline() {
  def dummy3: StackTraceElement =
    (new Exception).getStackTrace
      .filter(_.toString.contains("StackTraceDummy"))
      .head
}

class StackTraceDummy4 @noinline() {
  val dummy4: StackTraceElement =
    (new Exception).getStackTrace
      .filter(_.toString.contains("StackTraceDummy"))
      .head
}

class StackTraceElementTest {
  def dummy1 = (new StackTraceDummy1).dummy1
  def dummy2 = (new StackTraceDummy1)._dummy2
  def dummy3 = (new StackTraceDummy3_::).dummy3
  def dummy4 = (new StackTraceDummy4).dummy4

  @Test def getClassName(): Unit = {
    assertTrue(dummy1.getClassName == "javalib.lang.StackTraceDummy1")
    assertTrue(dummy2.getClassName == "javalib.lang.StackTraceDummy1")
    assertTrue(
      dummy3.getClassName == "javalib.lang.StackTraceDummy3_$colon$colon"
    )
    assertTrue(dummy4.getClassName == "javalib.lang.StackTraceDummy4")
  }

  @Test def getMethodName(): Unit = {
    assertTrue(dummy1.getMethodName == "dummy1")
    assertTrue(dummy2.getMethodName == "_dummy2")
    assertTrue(dummy3.getMethodName == "dummy3")
    assertTrue(dummy4.getMethodName == "<init>")
  }

  @Test def isNativeMethod(): Unit = {
    assertFalse(dummy1.isNativeMethod)
    assertFalse(dummy2.isNativeMethod)
    assertFalse(dummy3.isNativeMethod)
    assertFalse(dummy4.isNativeMethod)
  }
}

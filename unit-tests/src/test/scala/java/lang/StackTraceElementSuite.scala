package java.lang

class StackTraceDummy1 {
  def dummy1: StackTraceElement =
    (new Exception).getStackTrace.filter(_.toString.contains("dummy")).head

  def _dummy2: StackTraceElement =
    (new Exception).getStackTrace.filter(_.toString.contains("dummy")).head
}

class StackTraceDummy2_:: {
  def dummy3: StackTraceElement =
    (new Exception).getStackTrace.filter(_.toString.contains("dummy")).head
}

object StackTraceElementSuite extends tests.Suite {
  def dummy1 = (new StackTraceDummy1).dummy1
  def dummy2 = (new StackTraceDummy1)._dummy2
  def dummy3 = (new StackTraceDummy2_::).dummy3

  test("getClassName") {
    assert(dummy1.getClassName == "java.lang.StackTraceDummy1")
    assert(dummy2.getClassName == "java.lang.StackTraceDummy1")
    assert(dummy3.getClassName == "java.lang.StackTraceDummy2_$colon$colon")
  }

  test("getMethodName") {
    assert(dummy1.getMethodName == "dummy1")
    assert(dummy2.getMethodName == "$underscore$dummy2")
  }

  test("isNativeMethod") {
    assert(!dummy1.isNativeMethod)
    assert(!dummy2.isNativeMethod)
  }
}

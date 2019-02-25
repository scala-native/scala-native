package java.lang

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

object StackTraceElementSuite extends tests.Suite {
  def dummy1 = (new StackTraceDummy1).dummy1
  def dummy2 = (new StackTraceDummy1)._dummy2
  def dummy3 = (new StackTraceDummy3_::).dummy3
  def dummy4 = (new StackTraceDummy4).dummy4

  test("getClassName") {
    assert(dummy1.getClassName == "java.lang.StackTraceDummy1")
    assert(dummy2.getClassName == "java.lang.StackTraceDummy1")
    assert(dummy3.getClassName == "java.lang.StackTraceDummy3_$colon$colon")
    assert(dummy4.getClassName == "java.lang.StackTraceDummy4")
  }

  test("getMethodName") {
    assert(dummy1.getMethodName == "dummy1")
    assert(dummy2.getMethodName == "_dummy2")
    assert(dummy3.getMethodName == "dummy3")
    assert(dummy4.getMethodName == "<init>")
  }

  test("isNativeMethod") {
    assert(!dummy1.isNativeMethod)
    assert(!dummy2.isNativeMethod)
    assert(!dummy3.isNativeMethod)
    assert(!dummy4.isNativeMethod)
  }
}

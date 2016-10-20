package scala.scalanative.issues

object _260 extends tests.Suite {
  test("https://github.com/scala-native/scala-native/issues/260") {
    def getStr(): String = {
      val bytes = Array('h'.toByte, 'o'.toByte, 'l'.toByte, 'a'.toByte)
      new String(bytes)
    }

    val sz = getStr()

    assert("hola" == sz)
    assert("hola".equals(sz))
  }
}

import java.lang.Integer

object IntegerTest {
  def main(args: Array[String]): Unit = {
    testIntegerToString()
  }

  def testIntegerToString() = {
    // issue #270
    assert(Integer.toString(1).equals("1"))
    assert(Integer.toString(-1).equals("-1"))
    assert(Integer.toString(123).equals("123"))
    assert(Integer.toString(-123).equals("-123"))
    assert(Integer.toString(1234).equals("1234"))
    assert(Integer.toString(-1234).equals("-1234"))
  }
}

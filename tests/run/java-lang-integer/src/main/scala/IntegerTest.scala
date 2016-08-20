import java.lang.Integer

object IntegerTest {
  def main(args: Array[String]): Unit = {
    testIntegerToString()
  }

  def testIntegerToString() = {
    // issue #270
    println(Integer.toString(1).equals("1"))
    println(Integer.toString(-1).equals("1"))
    println(Integer.toString(123).equals("123"))
    println(Integer.toString(-123).equals("-123"))
    println(Integer.toString(1234).equals("1234"))
    println(Integer.toString(-1234).equals("-1234"))
  }
}


import java.lang.{Float => JFloat, Double => JDouble}

object FloatDoubleTest {
  def main(args: Array[String]): Unit = {
    testFloatParseFloat()
    testFloatToString()
    testDoubleParseDouble()
    testDoubleToString()
  }

  def testFloatParseFloat() = {
    assert(JFloat.parseFloat("1.0") == 1.0f)
    assert(JFloat.parseFloat("-1.0") == -1.0f)
    assert(JFloat.parseFloat("0.0") == 0.0f)
    assert(JFloat.parseFloat("-0.0") == -0.0f)
    assert(JFloat.parseFloat("Infinity") == JFloat.POSITIVE_INFINITY)
    assert(JFloat.parseFloat("-Infinity") == JFloat.NEGATIVE_INFINITY)
    assert(JFloat.isNaN(JFloat.parseFloat("NaN")))
  }

  // Not fully JVM compliant yet
  def testFloatToString() = {
    println(JFloat.toString(1.0f).equals("1.000000"))
    println(JFloat.toString(-1.0f).equals("-1.000000"))
    println(JFloat.toString(0.0f).equals("0.000000"))
    println(JFloat.toString(-0.0f).equals("-0.000000"))
    println(JFloat.toString(JFloat.POSITIVE_INFINITY).equals("Infinity"))
    println(JFloat.toString(JFloat.NEGATIVE_INFINITY).equals("-Infinity"))
    println(JFloat.toString(JFloat.NaN).equals("NaN"))
  }

  def testDoubleParseDouble() = {
    assert(JDouble.parseDouble("1.0") == 1.0)
    assert(JDouble.parseDouble("-1.0") == -1.0)
    assert(JDouble.parseDouble("0.0") == 0.0)
    assert(JDouble.parseDouble("-0.0") == -0.0)
    assert(JDouble.parseDouble("Infinity") == JDouble.POSITIVE_INFINITY)
    assert(JDouble.parseDouble("-Infinity") == JDouble.NEGATIVE_INFINITY)
    assert(JDouble.isNaN(JDouble.parseDouble("NaN")))
  }

  // Not fully JVM compliant yet
  def testDoubleToString() = {
    println(JDouble.toString(1.0).equals("1.000000"))
    println(JDouble.toString(-1.0).equals("-1.000000"))
    println(JDouble.toString(0.0).equals("0.000000"))
    println(JDouble.toString(-0.0).equals("-0.000000"))
    println(JDouble.toString(JDouble.POSITIVE_INFINITY).equals("Infinity"))
    println(JDouble.toString(JDouble.NEGATIVE_INFINITY).equals("-Infinity"))
    println(JDouble.toString(JDouble.NaN).equals("NaN"))
  }
}

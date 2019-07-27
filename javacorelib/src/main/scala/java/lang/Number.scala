package java.lang

import scala.math.ScalaNumber

abstract class Number extends java.lang._Object with java.io.Serializable {
  def byteValue(): scala.Byte   = intValue.toByte
  def shortValue(): scala.Short = intValue.toShort
  def intValue(): scala.Int
  def longValue(): scala.Long
  def floatValue(): scala.Float
  def doubleValue(): scala.Double

  @inline override def __scala_==(other: _Object): scala.Boolean = {
    if (other.isInstanceOf[ScalaNumber] && !this.isInstanceOf[ScalaNumber]) {
      other.equals(this)
    } else {
      super.__scala_==(other)
    }
  }
}

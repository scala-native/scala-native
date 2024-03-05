package java.lang

import scala.math.ScalaNumber

abstract class Number extends java.io.Serializable {
  def byteValue(): scala.Byte = intValue().toByte
  def shortValue(): scala.Short = intValue().toShort
  def intValue(): scala.Int
  def longValue(): scala.Long
  def floatValue(): scala.Float
  def doubleValue(): scala.Double

}

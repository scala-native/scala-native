import scalanative.native._, stdio._
import java.nio._
import java.nio.charset._

object Test  {
  def main(args: Array[String]): Unit = {
    assert(java.lang.Byte.toUnsignedInt(1.toByte) == 1)
    assert(java.lang.Byte.toUnsignedLong(1.toByte) == 1L)
    assert(java.lang.Short.toUnsignedInt(1.toShort) == 1)
    assert(java.lang.Short.toUnsignedLong(1.toShort) == 1L)
    assert(java.lang.Integer.toUnsignedLong(1) == 1L)
  }
}

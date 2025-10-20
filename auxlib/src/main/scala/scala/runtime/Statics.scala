package scala.runtime

import scala.scalanative.libc.stdatomic.*
import scala.scalanative.libc.stdatomic.memory_order.*
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

/** Not for public consumption. Usage by the runtime only.
 */
object Statics {
  @inline def mix(hash: Int, data: Int): Int = {
    val h1 = mixLast(hash, data)
    val h2 = Integer.rotateLeft(h1, 13)

    h2 * 5 + 0xe6546b64
  }

  @inline def mixLast(hash: Int, data: Int): Int = {
    val k1 = data
    val k2 = k1 * 0xcc9e2d51
    val k3 = Integer.rotateLeft(k2, 15)
    val k4 = k3 * 0x1b873593

    hash ^ k4
  }

  @inline def finalizeHash(hash: Int, length: Int): Int =
    avalanche(hash ^ length)

  @inline def avalanche(h: Int): Int = {
    val h1 = h ^ (h >>> 16)
    val h2 = h1 * 0x85ebca6b
    val h3 = h2 ^ (h2 >>> 13)
    val h4 = h3 * 0xc2b2ae35
    val h5 = h4 ^ (h4 >>> 16)

    h5
  }

  @inline def longHash(lv: Long): Int =
    if (lv.asInstanceOf[Int] == lv) lv.asInstanceOf[Int]
    else longHashShifted(lv)

  @inline def longHashShifted(lv: Long): Int =
    (lv ^ (lv >>> 32)).asInstanceOf[Int]

  @inline def doubleHash(dv: Double): Int = {
    val iv = dv.asInstanceOf[Int]

    if (iv == dv) iv
    else {
      val fv = dv.asInstanceOf[Float]

      if (fv == dv) java.lang.Float.floatToIntBits(fv)
      else {
        val lv = dv.asInstanceOf[Long]

        if (lv == dv) lv.asInstanceOf[Int]
        else longHashShifted(java.lang.Double.doubleToLongBits(dv))
      }
    }
  }

  @inline def floatHash(fv: Float): Int = {
    val iv = fv.asInstanceOf[Int]

    if (iv == fv) iv
    else {
      val lv = fv.asInstanceOf[Long]

      if (lv == fv) longHashShifted(lv)
      else java.lang.Float.floatToIntBits(fv)
    }
  }

  @inline def anyHash(x: Object): Int = x match {
    case null                => 0
    case x: java.lang.Number => anyHashNumber(x)
    case _                   => x.hashCode
  }

  @inline private def anyHashNumber(x: java.lang.Number): Int = x match {
    case x: java.lang.Long   => longHash(x.longValue)
    case x: java.lang.Double => doubleHash(x.doubleValue)
    case x: java.lang.Float  => floatHash(x.floatValue)
    case _                   => x.hashCode()
  }

  /** Used as a marker object to return from PartialFunctions */
  @inline final def pfMarker: java.lang.Object = PFMarker

  private object PFMarker

  @inline def releaseFence(): Unit =
    if (isMultithreadingEnabled) atomic_thread_fence(memory_order_release)

  /** Just throws an exception.
   *
   *  Used by the synthetic `productElement` and `productElementName` methods in
   *  case classes. Delegating the exception-throwing to this function reduces
   *  the bytecode size of the case class.
   */
  final def ioobe[T](n: Int): T =
    throw new IndexOutOfBoundsException(String.valueOf(n))
}

package scala.runtime

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
    case x: java.lang.Long   => longHash(x.longValue)
    case x: java.lang.Double => doubleHash(x.doubleValue)
    case x: java.lang.Float  => floatHash(x.floatValue)
    case _                   => x.hashCode
  }
}

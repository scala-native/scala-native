package java.nio

import scala.scalanative.runtime.{Intrinsics, toRawPtr}
import scala.scalanative.runtime.Intrinsics.{elemRawPtr, castIntToRawSize}
import scala.scalanative.unsafe._

private[nio] object ByteArrayBits {
  def apply(
      array: Ptr[Byte],
      arrayOffset: Int,
      isBigEndian: Boolean,
      indexMultiplier: Int = 1
  ): ByteArrayBits =
    new ByteArrayBits(array, arrayOffset, isBigEndian, indexMultiplier)
}

@inline
private[nio] final class ByteArrayBits(
    array: Ptr[Byte],
    arrayOffset: Int,
    isBigEndian: Boolean,
    indexMultiplier: Int
) {

  // API

  def loadChar(index: Int): Char = {
    val idx = indexMultiplier * index + arrayOffset
    val loaded =
      Intrinsics.loadChar(elemRawPtr(toRawPtr(array), castIntToRawSize(idx)))
    if (isBigEndian) java.lang.Character.reverseBytes(loaded)
    else loaded
  }
  def loadShort(index: Int): Short = {
    val idx = indexMultiplier * index + arrayOffset
    val loaded =
      Intrinsics.loadShort(elemRawPtr(toRawPtr(array), castIntToRawSize(idx)))
    if (isBigEndian) java.lang.Short.reverseBytes(loaded)
    else loaded
  }
  def loadInt(index: Int): Int = {
    val idx = indexMultiplier * index + arrayOffset
    val loaded =
      Intrinsics.loadInt(elemRawPtr(toRawPtr(array), castIntToRawSize(idx)))
    if (isBigEndian) java.lang.Integer.reverseBytes(loaded)
    else loaded
  }
  def loadLong(index: Int): Long = {
    val idx = indexMultiplier * index + arrayOffset
    val loaded =
      Intrinsics.loadLong(elemRawPtr(toRawPtr(array), castIntToRawSize(idx)))
    if (isBigEndian) java.lang.Long.reverseBytes(loaded)
    else loaded
  }
  def loadFloat(index: Int): Float = {
    val idx = indexMultiplier * index + arrayOffset
    val loaded =
      Intrinsics.loadInt(elemRawPtr(toRawPtr(array), castIntToRawSize(idx)))
    val ordered =
      if (isBigEndian) java.lang.Integer.reverseBytes(loaded)
      else loaded
    java.lang.Float.intBitsToFloat(ordered)
  }
  def loadDouble(index: Int): Double = {
    val idx = indexMultiplier * index + arrayOffset
    val loaded =
      Intrinsics.loadLong(elemRawPtr(toRawPtr(array), castIntToRawSize(idx)))
    val ordered =
      if (isBigEndian) java.lang.Long.reverseBytes(loaded)
      else loaded
    java.lang.Double.longBitsToDouble(ordered)
  }

  def storeChar(index: Int, v: Char): Unit = {
    val idx = indexMultiplier * index + arrayOffset
    val ordered =
      if (isBigEndian) java.lang.Character.reverseBytes(v)
      else v
    Intrinsics.storeChar(
      elemRawPtr(toRawPtr(array), castIntToRawSize(idx)),
      ordered
    )
  }

  def storeShort(index: Int, v: Short): Unit = {
    val idx = indexMultiplier * index + arrayOffset
    val ordered =
      if (isBigEndian) java.lang.Short.reverseBytes(v)
      else v
    Intrinsics.storeShort(
      elemRawPtr(toRawPtr(array), castIntToRawSize(idx)),
      ordered
    )
  }
  def storeInt(index: Int, v: Int): Unit = {
    val idx = indexMultiplier * index + arrayOffset
    val ordered =
      if (isBigEndian) java.lang.Integer.reverseBytes(v)
      else v
    Intrinsics.storeInt(
      elemRawPtr(toRawPtr(array), castIntToRawSize(idx)),
      ordered
    )
  }
  def storeLong(index: Int, v: Long): Unit = {
    val idx = indexMultiplier * index + arrayOffset
    val ordered =
      if (isBigEndian) java.lang.Long.reverseBytes(v)
      else v
    Intrinsics.storeLong(
      elemRawPtr(toRawPtr(array), castIntToRawSize(idx)),
      ordered
    )
  }
  def storeFloat(index: Int, v: Float): Unit = {
    val idx = indexMultiplier * index + arrayOffset
    val asInt = java.lang.Float.floatToIntBits(v)
    val ordered =
      if (isBigEndian) java.lang.Integer.reverseBytes(asInt)
      else asInt
    Intrinsics.storeInt(
      elemRawPtr(toRawPtr(array), castIntToRawSize(idx)),
      ordered
    )
  }
  def storeDouble(index: Int, v: Double): Unit = {
    val idx = indexMultiplier * index + arrayOffset
    val asLong = java.lang.Double.doubleToLongBits(v)
    val ordered =
      if (isBigEndian) java.lang.Long.reverseBytes(asLong)
      else asLong
    Intrinsics.storeLong(
      elemRawPtr(toRawPtr(array), castIntToRawSize(idx)),
      ordered
    )
  }
}

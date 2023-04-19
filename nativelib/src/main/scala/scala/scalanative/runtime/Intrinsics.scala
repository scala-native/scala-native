package scala.scalanative
package runtime

import scalanative.unsafe._

object Intrinsics {

  /** Intrinsified stack allocation of n bytes. */
  def stackalloc(size: RawSize): RawPtr = intrinsic

  /** Intrinsified stack allocation of n bytes. */
  def stackalloc(size: CSize): RawPtr = intrinsic

  /** Intrinsified unsigned devision on ints. */
  def divUInt(l: Int, r: Int): Int = intrinsic

  /** Intrinsified unsigned devision on longs. */
  def divULong(l: Long, r: Long): Long = intrinsic

  /** Intrinsified unsigned remainder on ints. */
  def remUInt(l: Int, r: Int): Int = intrinsic

  /** Intrinsified unsigned remainder on longs. */
  def remULong(l: Long, r: Long): Long = intrinsic

  /** Intrinsified byte to unsigned int converstion. */
  def byteToUInt(b: Byte): Int = intrinsic

  /** Intrinsified byte to unsigned long conversion. */
  def byteToULong(b: Byte): Long = intrinsic

  /** Intrinsified short to unsigned int conversion. */
  def shortToUInt(v: Short): Int = intrinsic

  /** Intrinsified short to unsigned long conversion. */
  def shortToULong(v: Short): Long = intrinsic

  /** Intrinsified int to unsigned long conversion. */
  def intToULong(v: Int): Long = intrinsic

  /** Intrinsified unsigned int to float conversion. */
  def uintToFloat(v: Int): Float = intrinsic

  /** Intrinsified unsigned long to float conversion. */
  def ulongToFloat(v: Long): Float = intrinsic

  /** Intrinsified unsigned int to double conversion. */
  def uintToDouble(v: Int): Double = intrinsic

  /** Intrinsified unsigned long to double conversion. */
  def ulongToDouble(v: Long): Double = intrinsic

  /** Intrinsified raw memory load of boolean. */
  def loadBoolean(rawptr: RawPtr): Boolean = intrinsic

  /** Intrinsified raw memory load of char. */
  def loadChar(rawptr: RawPtr): Char = intrinsic

  /** Intrinsified raw memory load of byte. */
  def loadByte(rawptr: RawPtr): Byte = intrinsic

  /** Intrinsified raw memory load of short. */
  def loadShort(rawptr: RawPtr): Short = intrinsic

  /** Intrinsified raw memory load of int. */
  def loadInt(rawptr: RawPtr): Int = intrinsic

  /** Intrinsified raw memory load of long. */
  def loadLong(rawptr: RawPtr): Long = intrinsic

  /** Intrinsified raw memory load of float. */
  def loadFloat(rawptr: RawPtr): Float = intrinsic

  /** Intrinsified raw memory load of double. */
  def loadDouble(rawptr: RawPtr): Double = intrinsic

  /** Intrinsified raw memory load of rawptr. */
  def loadRawPtr(rawptr: RawPtr): RawPtr = intrinsic

  /** Intrinsified raw memory load of RawSize. */
  def loadRawSize(rawptr: RawPtr): RawSize = intrinsic

  /** Intrinsified raw memory load of object. */
  def loadObject(rawptr: RawPtr): Object = intrinsic

  /** Intrinsified raw memory store of boolean. */
  def storeBoolean(rawptr: RawPtr, value: Boolean): Unit = intrinsic

  def storeRawSize(rawptr: RawPtr, value: RawSize): Unit = intrinsic

  /** Intrinsified raw memory store of char. */
  def storeChar(rawptr: RawPtr, value: Char): Unit = intrinsic

  /** Intrinsified raw memory store of byte. */
  def storeByte(rawptr: RawPtr, value: Byte): Unit = intrinsic

  /** Intrinsified raw memory store of short. */
  def storeShort(rawptr: RawPtr, value: Short): Unit = intrinsic

  /** Intrinsified raw memory store of int. */
  def storeInt(rawptr: RawPtr, value: Int): Unit = intrinsic

  /** Intrinsified raw memory store of long. */
  def storeLong(rawptr: RawPtr, value: Long): Unit = intrinsic

  /** Intrinsified raw memory store of float. */
  def storeFloat(rawptr: RawPtr, value: Float): Unit = intrinsic

  /** Intrinsified raw memory store of double. */
  def storeDouble(rawptr: RawPtr, value: Double): Unit = intrinsic

  /** Intrinsified raw memory store of rawptr. */
  def storeRawPtr(rawptr: RawPtr, value: RawPtr): Unit = intrinsic

  /** Intrinsified raw memory store of object. */
  def storeObject(rawptr: RawPtr, value: Object): Unit = intrinsic

  /** Intrinsified computation of derived raw pointer. */
  def elemRawPtr(rawptr: RawPtr, offset: RawSize): RawPtr =
    intrinsic

  /** Intrinsified cast that reinterprets raw pointer as an object. */
  def castRawPtrToObject(rawptr: RawPtr): Object = intrinsic

  /** Intrinsified cast that reinterprets object as a raw pointers. */
  def castObjectToRawPtr(obj: Object): RawPtr = intrinsic

  /** Intrinsified cast that reinterprets int as a float. */
  def castIntToFloat(int: Int): Float = intrinsic

  /** Intrinsified cast that reinterprets float as an int. */
  def castFloatToInt(float: Float): Int = intrinsic

  /** Intrinsified cast that reinterprets long as a double. */
  def castLongToDouble(long: Long): Double = intrinsic

  /** Intrinsified cast that reinterprets double as a long. */
  def castDoubleToLong(double: Double): Long = intrinsic

  /** Intrinsified cast that reinterprets raw pointer as an int. */
  def castRawPtrToInt(rawptr: RawPtr): Int = intrinsic

  /** Intrinsified cast that reinterprets raw pointer as an long. */
  def castRawPtrToLong(rawptr: RawPtr): Long = intrinsic

  /** Intrinsified cast that reinterprets int as a raw pointer. */
  def castIntToRawPtr(int: Int): RawPtr = intrinsic

  /** Intrinsified cast that reinterprets long as a raw pointer. */
  def castLongToRawPtr(int: Long): RawPtr = intrinsic

  /** Intrinsified cast that reinterprets raw size as an int. */
  def castRawSizeToInt(rawSize: RawSize): Int = intrinsic

  /** Intrinsified cast that reinterprets raw size as a signed long. */
  def castRawSizeToLong(rawSize: RawSize): Long = intrinsic

  /** Intrinsified cast that reinterprets raw size as an unsigned long. */
  def castRawSizeToLongUnsigned(rawSize: RawSize): Long = intrinsic

  /** Intrinsified cast that reinterprets int as a signed raw size. */
  def castIntToRawSize(int: Int): RawSize = intrinsic

  /** Intrinsified cast that reinterprets int as an unsigned raw size. */
  def castIntToRawSizeUnsigned(int: Int): RawSize = intrinsic

  /** Intrinsified cast that reinterprets long as a raw size. */
  def castLongToRawSize(long: Long): RawSize = intrinsic

  /** Intrinsified resolving of class field as a raw pointer */
  def classFieldRawPtr[T <: AnyRef](obj: T, fieldName: String): RawPtr =
    intrinsic

  /** Intrinsified resolving of memory layout size of given type */
  def sizeOf[T]: RawSize = intrinsic

  /** Internal intrinsified resolving of memory layout size of given type
   *  Accepts only class literals. Whenever possible use `sizeOf[T]` instead
   */
  def sizeOf(cls: Class[_]): RawSize = intrinsic

  /** Intrinsified resolving of memory layout alignment of given type */
  def alignmentOf[T]: RawSize = intrinsic

  /** Internal intrinsified resolving of memory layout alignment of given type
   *  Accepts only class literals. Whenever possible use `alignment[T]` instead
   */
  def alignmentOf(cls: Class[_]): RawSize = intrinsic
}

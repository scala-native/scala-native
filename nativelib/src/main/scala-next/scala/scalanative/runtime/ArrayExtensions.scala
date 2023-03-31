// format: off

// BEWARE: This file is generated - direct edits will be lost.
// Do not edit this it directly other than to remove
// personally identifiable information in sourceLocation lines.
// All direct edits to this file will be lost the next time it
// is generated.
//
// See nativelib runtime/Arrays.scala.gyb for details.

package scala.scalanative
package runtime

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.runtime.Intrinsics._
import scala.scalanative.SafeZone
import scala.scalanative.meta.LinktimeInfo.{is32BitPlatform, sizeOfPtr}


object BooleanArrayExtension {
  import BooleanArray.{arrayRawSize, storeArrayInfo}

  @inline def alloc(length: Int, zone: SafeZone): BooleanArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[BooleanArray]
    val arrsize = arrayRawSize(length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeArrayInfo(arr, length)
    castRawPtrToObject(arr).asInstanceOf[BooleanArray]
  }
}

object CharArrayExtension {
  import CharArray.{arrayRawSize, storeArrayInfo}

  @inline def alloc(length: Int, zone: SafeZone): CharArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[CharArray]
    val arrsize = arrayRawSize(length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeArrayInfo(arr, length)
    castRawPtrToObject(arr).asInstanceOf[CharArray]
  }
}

object ByteArrayExtension {
  import ByteArray.{arrayRawSize, storeArrayInfo}

  @inline def alloc(length: Int, zone: SafeZone): ByteArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ByteArray]
    val arrsize = arrayRawSize(length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeArrayInfo(arr, length)
    castRawPtrToObject(arr).asInstanceOf[ByteArray]
  }
}

object ShortArrayExtension {
  import ShortArray.{arrayRawSize, storeArrayInfo}

  @inline def alloc(length: Int, zone: SafeZone): ShortArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ShortArray]
    val arrsize = arrayRawSize(length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeArrayInfo(arr, length)
    castRawPtrToObject(arr).asInstanceOf[ShortArray]
  }
}

object IntArrayExtension {
  import IntArray.{arrayRawSize, storeArrayInfo}

  @inline def alloc(length: Int, zone: SafeZone): IntArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[IntArray]
    val arrsize = arrayRawSize(length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeArrayInfo(arr, length)
    castRawPtrToObject(arr).asInstanceOf[IntArray]
  }
}

object LongArrayExtension {
  import LongArray.{arrayRawSize, storeArrayInfo}

  @inline def alloc(length: Int, zone: SafeZone): LongArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[LongArray]
    val arrsize = arrayRawSize(length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeArrayInfo(arr, length)
    castRawPtrToObject(arr).asInstanceOf[LongArray]
  }
}

object FloatArrayExtension {
  import FloatArray.{arrayRawSize, storeArrayInfo}

  @inline def alloc(length: Int, zone: SafeZone): FloatArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[FloatArray]
    val arrsize = arrayRawSize(length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeArrayInfo(arr, length)
    castRawPtrToObject(arr).asInstanceOf[FloatArray]
  }
}

object DoubleArrayExtension {
  import DoubleArray.{arrayRawSize, storeArrayInfo}

  @inline def alloc(length: Int, zone: SafeZone): DoubleArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[DoubleArray]
    val arrsize = arrayRawSize(length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeArrayInfo(arr, length)
    castRawPtrToObject(arr).asInstanceOf[DoubleArray]
  }
}

object ObjectArrayExtension {
  import ObjectArray.{arrayRawSize, storeArrayInfo}

  @inline def alloc(length: Int, zone: SafeZone): ObjectArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ObjectArray]
    val arrsize = arrayRawSize(length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeArrayInfo(arr, length)
    castRawPtrToObject(arr).asInstanceOf[ObjectArray]
  }
}

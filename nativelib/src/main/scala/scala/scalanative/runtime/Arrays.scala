// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 1)
package scala.scalanative
package runtime

// Note 1:
// Arrays.scala is currently implemented as textual templating that is expanded through project/gyb.py script. 
// Update Arrays.scala.gyb and re-generate the source
// $ ./project/gyb.py \ 
//     nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb > \
//     nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala


// Note 2:
// Array of primitiveTypes don't contain pointers, runtime.allocAtomic() is called for memory allocation
// Array of Object do contain pointers. runtime.alloc() is called for memory allocation

// Note 3:
// PrimitiveArray.helperClone can allocate memory with GC.malloc_atomic() because 
// it will overwrite all data (no need to call llvm.memset)


import native._
import Intrinsics._

@struct class ArrayHeader(val info: Ptr[_], val length: Int)

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 30)

sealed abstract class Array[T]
    extends java.io.Serializable with java.lang.Cloneable {
  /** Number of elements of the array. */
  def length: Int =
    // TODO: Update once we support ptr->field
    !(this.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]]
    
  /** Loads element at i, throws IndexOutOfBoundsException. */
  def apply(i: Int): T

  /** Stores value to element i, throws IndexOutOfBoundsException. */
  def update(i: Int, value: T): Unit

  /** Create a shallow of given array. */
  protected override def clone(): Array[T] = undefined  
}

object Array {
  def copy (from: AnyRef, fromPos: Int, to: AnyRef, toPos: Int, len: Int): Unit = {
    if (from == null)
      throw new NullPointerException()
    
    if (to == null)
      throw new NullPointerException()
    
    val fromTypeId = instanceTypeId(from)
    val toTypeId = instanceTypeId(to)
    
    val stride = if (fromTypeId == arrayObjectTypeId && toTypeId == arrayObjectTypeId)
      sizeof[Object]
    
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 63)

    else if (fromTypeId == arrayBooleanTypeId && toTypeId == arrayBooleanTypeId) 
      sizeof[Boolean]
      
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 63)

    else if (fromTypeId == arrayCharTypeId && toTypeId == arrayCharTypeId) 
      sizeof[Char]
      
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 63)

    else if (fromTypeId == arrayByteTypeId && toTypeId == arrayByteTypeId) 
      sizeof[Byte]
      
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 63)

    else if (fromTypeId == arrayShortTypeId && toTypeId == arrayShortTypeId) 
      sizeof[Short]
      
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 63)

    else if (fromTypeId == arrayIntTypeId && toTypeId == arrayIntTypeId) 
      sizeof[Int]
      
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 63)

    else if (fromTypeId == arrayLongTypeId && toTypeId == arrayLongTypeId) 
      sizeof[Long]
      
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 63)

    else if (fromTypeId == arrayFloatTypeId && toTypeId == arrayFloatTypeId) 
      sizeof[Float]
      
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 63)

    else if (fromTypeId == arrayDoubleTypeId && toTypeId == arrayDoubleTypeId) 
      sizeof[Double]
      
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 68)

    else
      throw new ArrayStoreException ("Invalid array copy.")
      
    validateBoundaries (from.asInstanceOf[Array[_]], fromPos, to.asInstanceOf[Array[_]], toPos, len)
    
    val fromPtr: Ptr[Byte] = (from.cast[Ptr[Byte]] + sizeof[ArrayHeader] + stride * fromPos).cast[Ptr[Byte]]
    
    val toPtr: Ptr[Byte] = (to.cast[Ptr[Byte]] + sizeof[ArrayHeader] + stride * toPos).cast[Ptr[Byte]]
        
    `llvm.memmove.p0i8.p0i8.i64`(toPtr, fromPtr, stride * len, 1, false)
  }
  
  // the id's chosen by the compiler/linker for differents types of array

  val arrayObjectTypeId = typeId (typeof[scalanative.runtime.ObjectArray])
  
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 86)

  val arrayBooleanTypeId = typeId (typeof[scalanative.runtime.BooleanArray])
  
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 86)

  val arrayCharTypeId = typeId (typeof[scalanative.runtime.CharArray])
  
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 86)

  val arrayByteTypeId = typeId (typeof[scalanative.runtime.ByteArray])
  
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 86)

  val arrayShortTypeId = typeId (typeof[scalanative.runtime.ShortArray])
  
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 86)

  val arrayIntTypeId = typeId (typeof[scalanative.runtime.IntArray])
  
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 86)

  val arrayLongTypeId = typeId (typeof[scalanative.runtime.LongArray])
  
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 86)

  val arrayFloatTypeId = typeId (typeof[scalanative.runtime.FloatArray])
  
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 86)

  val arrayDoubleTypeId = typeId (typeof[scalanative.runtime.DoubleArray])
  
// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 90)
    
  private def typeId (ptr : Ptr[Type]): Int = {
    (!ptr).id    
  }
  
  private def instanceTypeId (any : AnyRef): Int = {
    typeId(runtime.getType(any))
  }
  
  @inline private[runtime] def validateBoundaries (from: Array[_], fromPos: Int, to: Array[_], toPos: Int, len: Int): Unit = {
    if (len < 0)
      throw new IndexOutOfBoundsException("length is negative")

    if (fromPos < 0 || fromPos + len > from.length)
      throw new IndexOutOfBoundsException(fromPos.toString)

    if (toPos < 0 || toPos + len > to.length)
      throw new IndexOutOfBoundsException(toPos.toString)    
  }
  
  @inline private[runtime] def pointerAt(arr: Array[_], sizeOneElement: CSize, i: Int): Ptr[_] = {
    if (i < 0 || i >= arr.length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      (arr.cast[Ptr[Byte]] + sizeof[ArrayHeader] + sizeOneElement * i).cast[Ptr[_]]
    }
  }
  
  @inline private[runtime] def helperClone(from: Array[_], length: Int, stride: CSize): Ptr[_] = {
    val arrsize = sizeof[ArrayHeader] + stride * length
    val arr = GC.malloc(arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]], from.cast[Ptr[Byte]], arrsize, 1, false)
    arr        
  }  
  
  def alloc(length: Int, arrinfo:  Ptr[Type], stride: CSize): Ptr[_] = {
    val arrsize = sizeof[ArrayHeader] + stride * length
    val arr = runtime.alloc(arrinfo, arrsize)
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr        
  } 
}

object PrimitiveArray {
  @inline private[runtime] def helperClone(src: Array[_], length: Int, stride: CSize): Ptr[_] = {
    val arrsize = sizeof[ArrayHeader] + stride * length
    val arr = GC.malloc_atomic(arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]], src.cast[Ptr[Byte]], arrsize, 1, false)
    arr
  }
  
  def alloc(length: Int, arrinfo:  Ptr[Type], stride: CSize): Ptr[_] = {
    val arrsize = sizeof[ArrayHeader] + stride * length
    // Primitive arrays don't contain pointers 
    val arr = runtime.allocAtomic(arrinfo, arrsize)
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr        
  }
}

final class ObjectArray private () extends Array[Object] {
  def apply(i: Int): Object = ! (Array.pointerAt(this, sizeof[Object], i).cast[Ptr[Object]])

  def update(i: Int, value: Object): Unit = ! (Array.pointerAt(this, sizeof[Object], i).cast[Ptr[Object]]) = value

  protected override def clone(): ObjectArray = Array.helperClone (this, length, sizeof[Object]).cast[ObjectArray]
}

object ObjectArray {
  def alloc(length: Int): ObjectArray = Array.alloc(length, typeof[ObjectArray], sizeof[Object]).cast[ObjectArray]
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 165)

final class BooleanArray private () extends Array[Boolean] {
  def apply(i: Int): Boolean = ! (Array.pointerAt(this, sizeof[Boolean], i).cast[Ptr[Boolean]])

  def update(i: Int, value: Boolean): Unit = ! (Array.pointerAt(this, sizeof[Boolean], i).cast[Ptr[Boolean]]) = value

  protected override def clone(): BooleanArray = PrimitiveArray.helperClone (this, length, sizeof[Boolean]).cast[BooleanArray]
}

object BooleanArray {
  def alloc(length: Int): BooleanArray = PrimitiveArray.alloc(length, typeof[BooleanArray], sizeof[Boolean]).cast[BooleanArray]  
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 165)

final class CharArray private () extends Array[Char] {
  def apply(i: Int): Char = ! (Array.pointerAt(this, sizeof[Char], i).cast[Ptr[Char]])

  def update(i: Int, value: Char): Unit = ! (Array.pointerAt(this, sizeof[Char], i).cast[Ptr[Char]]) = value

  protected override def clone(): CharArray = PrimitiveArray.helperClone (this, length, sizeof[Char]).cast[CharArray]
}

object CharArray {
  def alloc(length: Int): CharArray = PrimitiveArray.alloc(length, typeof[CharArray], sizeof[Char]).cast[CharArray]  
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 165)

final class ByteArray private () extends Array[Byte] {
  def apply(i: Int): Byte = ! (Array.pointerAt(this, sizeof[Byte], i).cast[Ptr[Byte]])

  def update(i: Int, value: Byte): Unit = ! (Array.pointerAt(this, sizeof[Byte], i).cast[Ptr[Byte]]) = value

  protected override def clone(): ByteArray = PrimitiveArray.helperClone (this, length, sizeof[Byte]).cast[ByteArray]
}

object ByteArray {
  def alloc(length: Int): ByteArray = PrimitiveArray.alloc(length, typeof[ByteArray], sizeof[Byte]).cast[ByteArray]  
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 165)

final class ShortArray private () extends Array[Short] {
  def apply(i: Int): Short = ! (Array.pointerAt(this, sizeof[Short], i).cast[Ptr[Short]])

  def update(i: Int, value: Short): Unit = ! (Array.pointerAt(this, sizeof[Short], i).cast[Ptr[Short]]) = value

  protected override def clone(): ShortArray = PrimitiveArray.helperClone (this, length, sizeof[Short]).cast[ShortArray]
}

object ShortArray {
  def alloc(length: Int): ShortArray = PrimitiveArray.alloc(length, typeof[ShortArray], sizeof[Short]).cast[ShortArray]  
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 165)

final class IntArray private () extends Array[Int] {
  def apply(i: Int): Int = ! (Array.pointerAt(this, sizeof[Int], i).cast[Ptr[Int]])

  def update(i: Int, value: Int): Unit = ! (Array.pointerAt(this, sizeof[Int], i).cast[Ptr[Int]]) = value

  protected override def clone(): IntArray = PrimitiveArray.helperClone (this, length, sizeof[Int]).cast[IntArray]
}

object IntArray {
  def alloc(length: Int): IntArray = PrimitiveArray.alloc(length, typeof[IntArray], sizeof[Int]).cast[IntArray]  
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 165)

final class LongArray private () extends Array[Long] {
  def apply(i: Int): Long = ! (Array.pointerAt(this, sizeof[Long], i).cast[Ptr[Long]])

  def update(i: Int, value: Long): Unit = ! (Array.pointerAt(this, sizeof[Long], i).cast[Ptr[Long]]) = value

  protected override def clone(): LongArray = PrimitiveArray.helperClone (this, length, sizeof[Long]).cast[LongArray]
}

object LongArray {
  def alloc(length: Int): LongArray = PrimitiveArray.alloc(length, typeof[LongArray], sizeof[Long]).cast[LongArray]  
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 165)

final class FloatArray private () extends Array[Float] {
  def apply(i: Int): Float = ! (Array.pointerAt(this, sizeof[Float], i).cast[Ptr[Float]])

  def update(i: Int, value: Float): Unit = ! (Array.pointerAt(this, sizeof[Float], i).cast[Ptr[Float]]) = value

  protected override def clone(): FloatArray = PrimitiveArray.helperClone (this, length, sizeof[Float]).cast[FloatArray]
}

object FloatArray {
  def alloc(length: Int): FloatArray = PrimitiveArray.alloc(length, typeof[FloatArray], sizeof[Float]).cast[FloatArray]  
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 165)

final class DoubleArray private () extends Array[Double] {
  def apply(i: Int): Double = ! (Array.pointerAt(this, sizeof[Double], i).cast[Ptr[Double]])

  def update(i: Int, value: Double): Unit = ! (Array.pointerAt(this, sizeof[Double], i).cast[Ptr[Double]]) = value

  protected override def clone(): DoubleArray = PrimitiveArray.helperClone (this, length, sizeof[Double]).cast[DoubleArray]
}

object DoubleArray {
  def alloc(length: Int): DoubleArray = PrimitiveArray.alloc(length, typeof[DoubleArray], sizeof[Double]).cast[DoubleArray]  
}


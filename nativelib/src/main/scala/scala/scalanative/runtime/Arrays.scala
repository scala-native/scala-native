// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 1)
package scala.scalanative
package runtime

// Note:
// Arrays.scala is currently implemented as textual templating that is expanded through project/gyb.py script. 
// Update Arrays.scala.gyb and re-generate the source

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 12)

import native._

@struct class ArrayHeader(val info: Ptr[_], val length: Int)

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

// note: Array[Ptr[_]] is implemented as a ObjectArray

final class ObjectArray private () extends Array[Object] {
  def apply(i: Int): Object =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Object]]
      headptr(i)
    }

  def update(i: Int, value: Object): Unit =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Object]]
      headptr(i) = value
    }

  protected override def clone(): ObjectArray = {
    val newarr = ObjectArray.alloc(length)
    ObjectArray.copy(this, 0, newarr, 0, length)
    newarr
  }
}

object ObjectArray {
  def copy(from: ObjectArray, fromPos: Int,
           to: ObjectArray, toPos: Int, length: Int): Unit = {
    ???
  }

  def alloc(length: Int): ObjectArray = {
    val arrinfo = infoof[ObjectArray]
    val arrsize = sizeof[ArrayHeader] + sizeof[Object] * length
    // no pointer free -> use runtime.alloc 
    val arr = runtime.alloc(arrinfo, arrsize)    
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr.cast[ObjectArray]        
  }
}

// all primitive Arrays use runtime.allocPointerFree to allocate memory 

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 80)

final class BooleanArray private () extends Array[Boolean] {
  def apply(i: Int): Boolean =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Boolean]]
      headptr(i)
    }

  def update(i: Int, value: Boolean): Unit =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Boolean]]
      headptr(i) = value
    }

  protected override def clone(): BooleanArray = {
    val newarr = BooleanArray.alloc(length)
    BooleanArray.copy(this, 0, newarr, 0, length)
    newarr
  }
}

object BooleanArray {
  def copy(from: BooleanArray, fromPos: Int,
           to: BooleanArray, toPos: Int, length: Int): Unit = {
    ???
  }

  def alloc(length: Int): BooleanArray = {
    val arrinfo = infoof[BooleanArray]
    val arrsize = sizeof[ArrayHeader] + sizeof[Boolean] * length
    // pointer free -> use runtime.allocPointerFree 
    val arr = runtime.allocPointerFree(arrinfo, arrsize)    
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr.cast[BooleanArray]        
  }
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 80)

final class CharArray private () extends Array[Char] {
  def apply(i: Int): Char =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Char]]
      headptr(i)
    }

  def update(i: Int, value: Char): Unit =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Char]]
      headptr(i) = value
    }

  protected override def clone(): CharArray = {
    val newarr = CharArray.alloc(length)
    CharArray.copy(this, 0, newarr, 0, length)
    newarr
  }
}

object CharArray {
  def copy(from: CharArray, fromPos: Int,
           to: CharArray, toPos: Int, length: Int): Unit = {
    ???
  }

  def alloc(length: Int): CharArray = {
    val arrinfo = infoof[CharArray]
    val arrsize = sizeof[ArrayHeader] + sizeof[Char] * length
    // pointer free -> use runtime.allocPointerFree 
    val arr = runtime.allocPointerFree(arrinfo, arrsize)    
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr.cast[CharArray]        
  }
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 80)

final class ByteArray private () extends Array[Byte] {
  def apply(i: Int): Byte =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Byte]]
      headptr(i)
    }

  def update(i: Int, value: Byte): Unit =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Byte]]
      headptr(i) = value
    }

  protected override def clone(): ByteArray = {
    val newarr = ByteArray.alloc(length)
    ByteArray.copy(this, 0, newarr, 0, length)
    newarr
  }
}

object ByteArray {
  def copy(from: ByteArray, fromPos: Int,
           to: ByteArray, toPos: Int, length: Int): Unit = {
    ???
  }

  def alloc(length: Int): ByteArray = {
    val arrinfo = infoof[ByteArray]
    val arrsize = sizeof[ArrayHeader] + sizeof[Byte] * length
    // pointer free -> use runtime.allocPointerFree 
    val arr = runtime.allocPointerFree(arrinfo, arrsize)    
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr.cast[ByteArray]        
  }
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 80)

final class ShortArray private () extends Array[Short] {
  def apply(i: Int): Short =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Short]]
      headptr(i)
    }

  def update(i: Int, value: Short): Unit =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Short]]
      headptr(i) = value
    }

  protected override def clone(): ShortArray = {
    val newarr = ShortArray.alloc(length)
    ShortArray.copy(this, 0, newarr, 0, length)
    newarr
  }
}

object ShortArray {
  def copy(from: ShortArray, fromPos: Int,
           to: ShortArray, toPos: Int, length: Int): Unit = {
    ???
  }

  def alloc(length: Int): ShortArray = {
    val arrinfo = infoof[ShortArray]
    val arrsize = sizeof[ArrayHeader] + sizeof[Short] * length
    // pointer free -> use runtime.allocPointerFree 
    val arr = runtime.allocPointerFree(arrinfo, arrsize)    
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr.cast[ShortArray]        
  }
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 80)

final class IntArray private () extends Array[Int] {
  def apply(i: Int): Int =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Int]]
      headptr(i)
    }

  def update(i: Int, value: Int): Unit =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Int]]
      headptr(i) = value
    }

  protected override def clone(): IntArray = {
    val newarr = IntArray.alloc(length)
    IntArray.copy(this, 0, newarr, 0, length)
    newarr
  }
}

object IntArray {
  def copy(from: IntArray, fromPos: Int,
           to: IntArray, toPos: Int, length: Int): Unit = {
    ???
  }

  def alloc(length: Int): IntArray = {
    val arrinfo = infoof[IntArray]
    val arrsize = sizeof[ArrayHeader] + sizeof[Int] * length
    // pointer free -> use runtime.allocPointerFree 
    val arr = runtime.allocPointerFree(arrinfo, arrsize)    
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr.cast[IntArray]        
  }
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 80)

final class LongArray private () extends Array[Long] {
  def apply(i: Int): Long =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Long]]
      headptr(i)
    }

  def update(i: Int, value: Long): Unit =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Long]]
      headptr(i) = value
    }

  protected override def clone(): LongArray = {
    val newarr = LongArray.alloc(length)
    LongArray.copy(this, 0, newarr, 0, length)
    newarr
  }
}

object LongArray {
  def copy(from: LongArray, fromPos: Int,
           to: LongArray, toPos: Int, length: Int): Unit = {
    ???
  }

  def alloc(length: Int): LongArray = {
    val arrinfo = infoof[LongArray]
    val arrsize = sizeof[ArrayHeader] + sizeof[Long] * length
    // pointer free -> use runtime.allocPointerFree 
    val arr = runtime.allocPointerFree(arrinfo, arrsize)    
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr.cast[LongArray]        
  }
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 80)

final class FloatArray private () extends Array[Float] {
  def apply(i: Int): Float =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Float]]
      headptr(i)
    }

  def update(i: Int, value: Float): Unit =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Float]]
      headptr(i) = value
    }

  protected override def clone(): FloatArray = {
    val newarr = FloatArray.alloc(length)
    FloatArray.copy(this, 0, newarr, 0, length)
    newarr
  }
}

object FloatArray {
  def copy(from: FloatArray, fromPos: Int,
           to: FloatArray, toPos: Int, length: Int): Unit = {
    ???
  }

  def alloc(length: Int): FloatArray = {
    val arrinfo = infoof[FloatArray]
    val arrsize = sizeof[ArrayHeader] + sizeof[Float] * length
    // pointer free -> use runtime.allocPointerFree 
    val arr = runtime.allocPointerFree(arrinfo, arrsize)    
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr.cast[FloatArray]        
  }
}

// ###sourceLocation(file: "/home/francois/proyectos/oss/scala-native-fbd/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 80)

final class DoubleArray private () extends Array[Double] {
  def apply(i: Int): Double =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Double]]
      headptr(i)
    }

  def update(i: Int, value: Double): Unit =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val headptr = (this.cast[Ptr[Byte]] + sizeof[ArrayHeader]).cast[Ptr[Double]]
      headptr(i) = value
    }

  protected override def clone(): DoubleArray = {
    val newarr = DoubleArray.alloc(length)
    DoubleArray.copy(this, 0, newarr, 0, length)
    newarr
  }
}

object DoubleArray {
  def copy(from: DoubleArray, fromPos: Int,
           to: DoubleArray, toPos: Int, length: Int): Unit = {
    ???
  }

  def alloc(length: Int): DoubleArray = {
    val arrinfo = infoof[DoubleArray]
    val arrsize = sizeof[ArrayHeader] + sizeof[Double] * length
    // pointer free -> use runtime.allocPointerFree 
    val arr = runtime.allocPointerFree(arrinfo, arrsize)    
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[_]]).cast[Ptr[Int]] = length    
    arr.cast[DoubleArray]        
  }
}


package java.lang.reflect

import scalanative.runtime.{Array => _, _}
import scala.annotation.tailrec

object Array {
  def newInstance(componentType: Class[_], length: Int): AnyRef = {
    val ty = componentType

    if (ty == classOf[scala.Boolean]) {
      new scala.Array[scala.Boolean](length)
    } else if (ty == classOf[scala.Char]) {
      new scala.Array[scala.Char](length)
    } else if (ty == classOf[scala.Byte]) {
      new scala.Array[scala.Byte](length)
    } else if (ty == classOf[scala.Short]) {
      new scala.Array[scala.Short](length)
    } else if (ty == classOf[scala.Int]) {
      new scala.Array[scala.Int](length)
    } else if (ty == classOf[scala.Long]) {
      new scala.Array[scala.Long](length)
    } else if (ty == classOf[scala.Float]) {
      new scala.Array[scala.Float](length)
    } else if (ty == classOf[scala.Double]) {
      new scala.Array[scala.Double](length)
    } else {
      new scala.Array[Object](length)
    }
  }

  def newInstance(
      componentType: Class[_],
      dimensions: scala.Array[scala.Int]
  ): AnyRef = {
    import scala.scalanative.runtime.{Array => NativeArray, ObjectArray}
    if (componentType eq null)
      throw new NullPointerException()
    if (dimensions.length == 0 || dimensions.length > 255)
      throw new IllegalArgumentException()

    @tailrec def fill(
        idx: Int,
        prevDimension: NativeArray[_]
    ): NativeArray[_] = {
      if (idx < 0) prevDimension
      else {
        val length = dimensions(idx)
        if (length < 0) throw new NegativeArraySizeException()

        // In Scala Native Array[Array[<PrimitiveType>]] and it's higher dimensions are always represented as ObjectArray
        val arr = ObjectArray.alloc(length)
        arr.update(0, prevDimension)
        var i = 1
        while (i != length) {
          arr.update(i, prevDimension.clone())
          i += 1
        }
        fill(idx - 1, arr)
      }
    }

    val lastDimension = newInstance(componentType, dimensions.last)
    if (dimensions.length == 1) lastDimension
    else fill(dimensions.length - 2, lastDimension.asInstanceOf[NativeArray[_]])
  }

  def getLength(array: AnyRef): Int = array match {
    // yes, this is kind of stupid, but that's how it is
    case array: Array[Object]        => array.length
    case array: Array[scala.Boolean] => array.length
    case array: Array[scala.Char]    => array.length
    case array: Array[scala.Byte]    => array.length
    case array: Array[scala.Short]   => array.length
    case array: Array[scala.Int]     => array.length
    case array: Array[scala.Long]    => array.length
    case array: Array[scala.Float]   => array.length
    case array: Array[scala.Double]  => array.length
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def get(array: AnyRef, index: Int): AnyRef = array match {
    case array: Array[Object]        => array(index)
    case array: Array[scala.Boolean] => java.lang.Boolean.valueOf(array(index))
    case array: Array[scala.Char]   => java.lang.Character.valueOf(array(index))
    case array: Array[scala.Byte]   => java.lang.Byte.valueOf(array(index))
    case array: Array[scala.Short]  => java.lang.Short.valueOf(array(index))
    case array: Array[scala.Int]    => java.lang.Integer.valueOf(array(index))
    case array: Array[scala.Long]   => java.lang.Long.valueOf(array(index))
    case array: Array[scala.Float]  => java.lang.Float.valueOf(array(index))
    case array: Array[scala.Double] => java.lang.Double.valueOf(array(index))
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def getBoolean(array: AnyRef, index: Int): Boolean = array match {
    case array: Array[scala.Boolean] => array(index)
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def getChar(array: AnyRef, index: Int): Char = array match {
    case array: Array[scala.Char] => array(index)
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def getByte(array: AnyRef, index: Int): Byte = array match {
    case array: Array[scala.Byte] => array(index)
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def getShort(array: AnyRef, index: Int): Short = array match {
    case array: Array[scala.Short] => array(index)
    case array: Array[scala.Byte]  => array(index)
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def getInt(array: AnyRef, index: Int): Int = array match {
    case array: Array[scala.Int]   => array(index)
    case array: Array[scala.Char]  => array(index)
    case array: Array[scala.Byte]  => array(index)
    case array: Array[scala.Short] => array(index)
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def getLong(array: AnyRef, index: Int): Long = array match {
    case array: Array[scala.Long]  => array(index)
    case array: Array[scala.Char]  => array(index)
    case array: Array[scala.Byte]  => array(index)
    case array: Array[scala.Short] => array(index)
    case array: Array[scala.Int]   => array(index)
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def getFloat(array: AnyRef, index: Int): Float = array match {
    case array: Array[scala.Float] => array(index)
    case array: Array[scala.Char]  => array(index)
    case array: Array[scala.Byte]  => array(index)
    case array: Array[scala.Short] => array(index)
    case array: Array[scala.Int]   => array(index).toFloat
    case array: Array[scala.Long]  => array(index).toFloat
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def getDouble(array: AnyRef, index: Int): Double = array match {
    case array: Array[scala.Double] => array(index)
    case array: Array[scala.Char]   => array(index)
    case array: Array[scala.Byte]   => array(index)
    case array: Array[scala.Short]  => array(index)
    case array: Array[scala.Int]    => array(index)
    case array: Array[scala.Long]   => array(index).toDouble
    case array: Array[scala.Float]  => array(index)
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def set(array: AnyRef, index: Int, value: AnyRef): Unit = array match {
    case array: Array[Object] => array(index) = value
    case _ =>
      (value: Any) match {
        case value: scala.Boolean => setBoolean(array, index, value)
        case value: scala.Char    => setChar(array, index, value)
        case value: scala.Byte    => setByte(array, index, value)
        case value: scala.Short   => setShort(array, index, value)
        case value: scala.Int     => setInt(array, index, value)
        case value: scala.Long    => setLong(array, index, value)
        case value: scala.Float   => setFloat(array, index, value)
        case value: scala.Double  => setDouble(array, index, value)
        case _ =>
          throw new IllegalArgumentException("argument type mismatch")
      }
  }

  def setBoolean(array: AnyRef, index: Int, value: Boolean): Unit =
    array match {
      case array: Array[scala.Boolean] => array(index) = value
      case _ =>
        throw new IllegalArgumentException("argument type mismatch")
    }

  def setChar(array: AnyRef, index: Int, value: Char): Unit = array match {
    case array: Array[scala.Char]   => array(index) = value
    case array: Array[scala.Int]    => array(index) = value
    case array: Array[scala.Long]   => array(index) = value
    case array: Array[scala.Float]  => array(index) = value
    case array: Array[scala.Double] => array(index) = value
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def setByte(array: AnyRef, index: Int, value: Byte): Unit = array match {
    case array: Array[scala.Byte]   => array(index) = value
    case array: Array[scala.Short]  => array(index) = value
    case array: Array[scala.Int]    => array(index) = value
    case array: Array[scala.Long]   => array(index) = value
    case array: Array[scala.Float]  => array(index) = value
    case array: Array[scala.Double] => array(index) = value
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def setShort(array: AnyRef, index: Int, value: Short): Unit = array match {
    case array: Array[scala.Short]  => array(index) = value
    case array: Array[scala.Int]    => array(index) = value
    case array: Array[scala.Long]   => array(index) = value
    case array: Array[scala.Float]  => array(index) = value
    case array: Array[scala.Double] => array(index) = value
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def setInt(array: AnyRef, index: Int, value: Int): Unit = array match {
    case array: Array[scala.Int]    => array(index) = value
    case array: Array[scala.Long]   => array(index) = value
    case array: Array[scala.Float]  => array(index) = value.toFloat
    case array: Array[scala.Double] => array(index) = value
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def setLong(array: AnyRef, index: Int, value: Long): Unit = array match {
    case array: Array[scala.Long]   => array(index) = value
    case array: Array[scala.Float]  => array(index) = value.toFloat
    case array: Array[scala.Double] => array(index) = value.toDouble
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def setFloat(array: AnyRef, index: Int, value: Float): Unit = array match {
    case array: Array[scala.Float]  => array(index) = value
    case array: Array[scala.Double] => array(index) = value
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }

  def setDouble(array: AnyRef, index: Int, value: Double): Unit = array match {
    case array: Array[scala.Double] => array(index) = value
    case _ =>
      throw new IllegalArgumentException("argument type mismatch")
  }
}

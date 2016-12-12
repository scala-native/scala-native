package java.lang

import scalanative.native.Ptr
import scalanative.runtime.{Array => _, _}

final class _Class[A](val ty: Ptr[Type]) {
  def cast(obj: Object): A = ???

  def getComponentType(): _Class[_] = {
    if (ty == typeof[BooleanArray]) classOf[scala.Boolean]
    else if (ty == typeof[CharArray]) classOf[scala.Char]
    else if (ty == typeof[ByteArray]) classOf[scala.Byte]
    else if (ty == typeof[ShortArray]) classOf[scala.Short]
    else if (ty == typeof[IntArray]) classOf[scala.Int]
    else if (ty == typeof[LongArray]) classOf[scala.Long]
    else if (ty == typeof[FloatArray]) classOf[scala.Float]
    else if (ty == typeof[DoubleArray]) classOf[scala.Double]
    else classOf[java.lang.Object]
  }

  def getInterfaces(): Array[_Class[_]] = ???

  def getName(): String =
    (!ty).name

  def getSimpleName(): String =
    getName.split('.').last.split('$').last

  def getSuperclass(): Class[_ >: A] = ???

  def isArray(): scala.Boolean =
    (ty == typeof[BooleanArray] ||
      ty == typeof[CharArray] ||
      ty == typeof[ByteArray] ||
      ty == typeof[ShortArray] ||
      ty == typeof[IntArray] ||
      ty == typeof[LongArray] ||
      ty == typeof[FloatArray] ||
      ty == typeof[DoubleArray] ||
      ty == typeof[ObjectArray])

  def isAssignableFrom(that: Class[_]): scala.Boolean = ???

  def isInstance(obj: Object): scala.Boolean = ???

  def isInterface(): scala.Boolean = ???

  def isPrimitive(): scala.Boolean =
    (ty == typeof[PrimitiveBoolean] ||
      ty == typeof[PrimitiveChar] ||
      ty == typeof[PrimitiveByte] ||
      ty == typeof[PrimitiveShort] ||
      ty == typeof[PrimitiveInt] ||
      ty == typeof[PrimitiveLong] ||
      ty == typeof[PrimitiveFloat] ||
      ty == typeof[PrimitiveDouble] ||
      ty == typeof[PrimitiveUnit])

  override def equals(other: Any): scala.Boolean =
    other match {
      case other: _Class[_] =>
        ty == other.ty
      case _ =>
        false
    }

  override def hashCode: Int =
    ty.cast[scala.Long].##
}

object _Class {
  private[java] implicit def _class2class[A](cls: _Class[A]): Class[A] =
    cls.asInstanceOf[Class[A]]
  private[java] implicit def class2_class[A](cls: Class[A]): _Class[A] =
    cls.asInstanceOf[_Class[A]]
}

package java.lang

import scalanative.native.Ptr
import scalanative.runtime.{Array => _, _}

final class _Class[A](val ty: Ptr[Type]) {
  def getName(): String = (!ty).name

  override def hashCode: Int = ty.cast[Long].##

  override def equals(other: Any): scala.Boolean = other match {
    case other: _Class[_] =>
      ty == other.ty
    case _ =>
      false
  }

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

  // TODO:
  def getInterfaces(): Array[_Class[_]] = ???
  def getSuperclass(): _Class[_]        = ???
  def isArray(): scala.Boolean          = ???
}

object _Class {
  private[java] implicit def _class2class[A](cls: _Class[A]): Class[A] =
    cls.asInstanceOf[Class[A]]
  private[java] implicit def class2_class[A](cls: Class[A]): _Class[A] =
    cls.asInstanceOf[_Class[A]]
}

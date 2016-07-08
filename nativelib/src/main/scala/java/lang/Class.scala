package java.lang

import scala.scalanative.native.Ptr
import scala.scalanative.runtime.Type

final class _Class[A](val ty: Ptr[Type]) {
  def getName(): String                 = ???
  def getInterfaces(): Array[_Class[_]] = ???
  def getSuperclass(): _Class[_]        = ???
  def getComponentType(): _Class[_]     = ???
  def isArray(): scala.Boolean          = ???
}

object _Class {
  private[java] implicit def _class2class[A](cls: _Class[A]): Class[A] =
    cls.asInstanceOf[Class[A]]
  private[java] implicit def class2_class[A](cls: Class[A]): _Class[A] =
    cls.asInstanceOf[_Class[A]]
}

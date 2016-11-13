package java.lang

import scala.scalanative.native.Ptr
import scala.scalanative.runtime.Type

final class _Class[A](val ty: Ptr[Type]) {
  def getName(): String = (!ty).name

  override def hashCode: Int = getName().##

  override def equals(other: Any): scala.Boolean = other match {
    case other: _Class[_] =>
      ty == other.ty
    case _ =>
      false
  }

  // TODO:
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

  // def isAssignableFrom(cls: _Class[_]): scala.Boolean = ???
  // def isInstance(obj: _Object): scala.Boolean = ???
}

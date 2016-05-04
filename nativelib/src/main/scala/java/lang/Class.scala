package java.lang

import scala.scalanative.native.Ptr
import scala.scalanative.runtime.Type

final class _Class[A](ty: Ptr[Type]) {
  def getComponentType(): _Class[_]                          = ???
  def getInterfaces(): Array[_Class[_]]                      = ???
  def getResourceAsStream(name: String): java.io.InputStream = ???
  def getName(): String                                      = ???
  def getSimpleName(): String                                = ???
  def getSuperclass(): _Class[_]                             = ???
  def isArray(): scala.Boolean                               = ???
  def isAssignableFrom(cls: _Class[_]): scala.Boolean        = ???
  def isInstance(obj: Object): scala.Boolean                 = ???
  def isPrimitive(): scala.Boolean                           = ???
  def cast(obj: Any): A                                      = ???
}

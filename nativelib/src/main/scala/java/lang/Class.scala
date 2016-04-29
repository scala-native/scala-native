package java.lang

import scala.scalanative.native.Ptr
import scala.scalanative.runtime.Type

final class Class[A](ty: Ptr[Type]) {
  def getComponentType(): Class[_]                           = ???
  def getInterfaces(): Array[Class[_]]                       = ???
  def getResourceAsStream(name: String): java.io.InputStream = ???
  def getName(): String                                      = ???
  def getSimpleName(): String                                = ???
  def getSuperclass(): Class[_]                              = ???
  def isArray(): scala.Boolean                               = ???
  def isAssignableFrom(cls: Class[_]): scala.Boolean         = ???
  def isInstance(obj: Object): scala.Boolean                 = ???
  def isPrimitive(): scala.Boolean                           = ???
  def cast(obj: Any): A                                      = ???
}

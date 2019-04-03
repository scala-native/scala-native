package java.lang.reflect

import scalanative.native.stub

class Method {

  @stub
  def getDeclaringClass(): java.lang.Class[_] = ???

  @stub
  def getName(): java.lang.String = ???

  @stub
  def getParameterTypes(): scala.Array[java.lang.Class[_]] = ???

  @stub
  def getReturnType(): java.lang.Class[_] = ???

  @stub
  def invoke(obj: java.lang.Object,
             args: scala.Array[Object]): java.lang.Object = ???
}

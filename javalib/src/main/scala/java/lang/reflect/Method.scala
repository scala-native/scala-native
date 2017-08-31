package java.lang.reflect

class Method {
  def getDeclaringClass(): java.lang.Class[_]  = ???
  def getName(): java.lang.String              = ???
  def getParameterTypes(): scala.Array[Object] = ???
  def getReturnType(): java.lang.Class[_]      = ???
  def invoke(obj: java.lang.Object,
             args: scala.Array[Object]): java.lang.Object = ???
}

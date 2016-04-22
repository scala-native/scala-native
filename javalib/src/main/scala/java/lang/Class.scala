package java.lang

final class Class[A] private {
  def getName(): String = ???
  def getComponentType(): Class[_] = ???
  def cast(obj: Any): A = ???
}

object Class

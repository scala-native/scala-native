package a

class A {
  def matchClass(runtimeClass: Class[_]) = {
    var x = 1
    runtimeClass match {
      case java.lang.Byte.TYPE      => x = 1
      case java.lang.Short.TYPE     => x = 2
      case java.lang.Character.TYPE => x = 3
      case java.lang.Integer.TYPE   => x = 4
      case java.lang.Long.TYPE      => x = 5
      case java.lang.Float.TYPE     => x = 6
      case java.lang.Double.TYPE    => x = 7
      case java.lang.Boolean.TYPE   => x = 8
      case java.lang.Void.TYPE      => x = 9
      case _                        => x = 10
    }
    var y = 0
    while (y < x)
      y += 1
    y
  }

}

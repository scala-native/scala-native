package native {
  class builtin extends scala.annotation.StaticAnnotation
}

package object native {
  def builtin: Nothing = throw new Exception
}


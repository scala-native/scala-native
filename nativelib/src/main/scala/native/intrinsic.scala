package native

class intrinsic extends scala.annotation.StaticAnnotation
object intrinsic {
  def impl: Nothing = throw new Exception("unimplemented intrinsic")
}

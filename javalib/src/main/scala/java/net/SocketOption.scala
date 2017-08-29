package java.net

trait SocketOption[T] {
  val name: String
  val `type`: Class[T]
}

private[java] case class SocketOptionImpl[T](val name: String,
                                             val `type`: Class[T])
    extends SocketOption[T]

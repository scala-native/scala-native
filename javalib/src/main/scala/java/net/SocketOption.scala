package java.net

trait SocketOption[T] {
  val name: String
  val `type`: Class[T]
}

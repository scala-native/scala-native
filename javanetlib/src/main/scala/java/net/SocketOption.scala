package java.net

trait SocketOption[T] {

  def name: String
  def `type`: Class[T]

}

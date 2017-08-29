package java.net

trait BooleanOption extends SocketOption[java.lang.Boolean] {
  override val `type` = classOf[java.lang.Boolean]
}

trait IntOption extends SocketOption[Integer] {
  override val `type` = classOf[Integer]
}

case object SoSndBuf extends IntOption {
  override val name = "SO_SNDBUF"
}

case object TcpNoDelay extends BooleanOption {
  override val name = "TCP_NODELAY"
}

object StandardSocketOptions {

  /*val IP_MULTICAST_IF = ???

  val IP_MULTICAST_LOOP =
    new SocketOptionImpl[Boolean]("IP_MULTICAST_LOOP", classOf[Boolean])

  val IP_MULTICAST_TTL =
    new SocketOptionImpl[Int]("IP_MULTICAST_TTL", classOf[Int])

  val IP_TOS =
    new SocketOptionImpl[Int]("IP_TOS", classOf[Int])

  val SO_BROADCAST =
    new SocketOptionImpl[Boolean]("SO_BROADCAST", classOf[Boolean])

  val SO_KEEPALIVE =
    new SocketOptionImpl[Boolean]("SO_KEEPALIVE", classOf[Boolean])

  val SO_LINGER =
    new SocketOptionImpl[Int]("SO_LINGER", classOf[Int])

  val SO_RCVBUF =
    new SocketOptionImpl[Int]("SO_RCVBUF", classOf[Int])

  val SO_REUSEADDR =
    new SocketOptionImpl[Boolean]("SO_REUSEADDR", classOf[Boolean])*/

  def SO_SNDBUF[T]: SocketOption[T] = SoSndBuf.asInstanceOf[SocketOption[T]]

  def TCP_NODELAY[T]: SocketOption[T] =
    TcpNoDelay.asInstanceOf[SocketOption[T]]

}

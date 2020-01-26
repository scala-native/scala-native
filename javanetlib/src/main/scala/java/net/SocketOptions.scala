package java.net

trait SocketOptions {

  def getOption(optID: Int): Object
  def setOption(optID: Int, value: Object): Unit

}

object SocketOptions {

  val IP_MULTICAST_IF: Int = 0

  val IP_MULTICAST_IF2: Int = 1

  val IP_MULTICAST_LOOP: Int = 2

  val IP_TOS: Int = 3

  val SO_BINDADDR: Int = 4

  val SO_BROADCAST: Int = 5

  val SO_KEEPALIVE: Int = 6

  val SO_LINGER: Int = 7

  val SO_OOBINLINE: Int = 8

  val SO_RCVBUF: Int = 9

  val SO_REUSEADDR: Int = 10

  val SO_SNDBUF: Int = 11

  val SO_TIMEOUT: Int = 12

  val TCP_NODELAY: Int = 13

}

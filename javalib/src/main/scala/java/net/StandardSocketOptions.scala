/*
 * Copyright 2022 Arman Bilge
 *
 * Original code is from the armanbilge/epollcat project at
 * https://github.com/armanbilge/epollcat/
 *
 * For full original license notice , see:
 *     https://github.com/scala-native/scala-native/blob/main/LICENSE.md
 *
 * Additional code provided by the Scala Native project carries the
 * Scala Native license, described in the same LICENSE.md.
 */

package java.net

object StandardSocketOptions {

  /* NetworkInterface is not-yet-implemented.
   * IP_MULTICAST_IF is defined for completeness.
   * Any code using it before NetworkInterface is implemented will
   * encounter a 'symbol not found' error at link time.
   */
  val IP_MULTICAST_IF: SocketOption[java.net.NetworkInterface] = // BEWARE!
    new StdSocketOption("IP_MULTICAST_IF", classOf)

  val IP_MULTICAST_LOOP: SocketOption[java.lang.Boolean] =
    new StdSocketOption("IP_MULTICAST_LOOP", classOf)

  val IP_MULTICAST_TTL: SocketOption[java.lang.Integer] =
    new StdSocketOption("IP_MULTICAST_TTL", classOf)

  /* Quoting from both the Java 8 & 17 documentation:
   *   The behavior of this socket option on a stream-oriented socket,
   *   or an IPv6 socket, is not defined in this release.
   */
  val IP_TOS: SocketOption[java.lang.Integer] =
    new StdSocketOption("IP_TOS", classOf)

  val SO_BROADCAST: SocketOption[java.lang.Boolean] =
    new StdSocketOption("SO_BROADCAST", classOf)

  val SO_KEEPALIVE: SocketOption[java.lang.Boolean] =
    new StdSocketOption("SO_KEEPALIVE", classOf)

  val SO_LINGER: SocketOption[java.lang.Integer] =
    new StdSocketOption("SO_LINGER", classOf)

  val SO_RCVBUF: SocketOption[java.lang.Integer] =
    new StdSocketOption("SO_RCVBUF", classOf)

  val SO_REUSEADDR: SocketOption[java.lang.Boolean] =
    new StdSocketOption("SO_REUSEADDR", classOf)

  val SO_REUSEPORT: SocketOption[java.lang.Boolean] =
    new StdSocketOption("SO_REUSEPORT", classOf)

  val SO_SNDBUF: SocketOption[java.lang.Integer] =
    new StdSocketOption("SO_SNDBUF", classOf)

  val TCP_NODELAY: SocketOption[java.lang.Boolean] =
    new StdSocketOption("TCP_NODELAY", classOf)

  private final class StdSocketOption[T](val name: String, val `type`: Class[T])
      extends SocketOption[T] {
    override def toString = name
  }
}

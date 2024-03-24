package scala.scalanative.posix.netinet

import scalanative.unsafe._

@extern
@define("__SCALANATIVE_POSIX_NETINET_TCP")
object tcp {

  @name("scalanative_tcp_nodelay")
  def TCP_NODELAY: CInt = extern
}

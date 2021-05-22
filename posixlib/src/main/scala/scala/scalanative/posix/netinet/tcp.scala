package scala.scalanative.posix.netinet

import scalanative.unsafe._

@extern
object tcp {

  @name("scalanative_tcp_nodelay")
  def TCP_NODELAY: CInt = extern
}

package scala.scalanative.posix.netinet

import scalanative.native._

@extern
object tcp {

  @name("scalanative_TCP_NODELAY")
  def TCP_NODELAY: CInt = extern
}

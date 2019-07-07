package scala.scalanative.posix.sys

import scala.scalanative.unsafe._

@extern
object wait {
  @name("wait")
  def _wait():Int = extern
  def waitpid(pid:Int, status:Ptr[Int], options:Int):Int = extern
}
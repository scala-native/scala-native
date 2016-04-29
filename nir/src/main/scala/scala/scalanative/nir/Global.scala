package scala.scalanative
package nir

import util.sh
import nir.Shows._

sealed abstract class Global {
  def id: String
  def top: Global.Top

  def isIntrinsic: Boolean = this match {
    case Global.Val(id) if id.startsWith("scalanative_") => true
    case _                                               => false
  }

  def member(id: String): Global.Member =
    Global.Member(this, id)

  def tag(tag: String): Global = this match {
    case Global.Val(id)       => Global.Val(s"$tag.$id")
    case Global.Type(id)      => Global.Type(s"$tag.$id")
    case Global.Member(n, id) => Global.Member(n, s"$tag.id")
  }
}
object Global {
  sealed abstract class Top extends Global {
    override def top: Global.Top = this
  }
  final case class Val(override val id: String)  extends Top
  final case class Type(override val id: String) extends Top

  final case class Member(val owner: Global, override val id: String)
      extends Global {
    override def top: Global.Top = owner.top
  }
}

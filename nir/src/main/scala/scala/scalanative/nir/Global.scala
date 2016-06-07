package scala.scalanative
package nir

import util.sh
import nir.Shows._

sealed abstract class Global {
  def id: String
  def top: Global.Top

  def isIntrinsic: Boolean = this match {
    case Global.Top(id) if id.startsWith("scalanative_") => true
    case _                                               => false
  }

  def isTop: Boolean = this.isInstanceOf[Global.Top]

  def member(id: String): Global.Member =
    Global.Member(this, id)

  def tag(tag: String): Global = this match {
    case Global.Top(id)       => Global.Top(s"$tag.$id")
    case Global.Member(n, id) => Global.Member(n, s"$tag.$id")
    case _                    => util.unreachable
  }
}
object Global {
  final case object None extends Global {
    override def id  = throw new Exception("None doesn't have an id.")
    override def top = throw new Exception("None doesn't have a top.")
    override def member(id: String) =
      throw new Exception("None can't have any members.")
    override def tag(id: String) = throw new Exception("None is not taggable.")
  }

  final case class Top(override val id: String) extends Global {
    override def top = this
  }

  final case class Member(val owner: Global, override val id: String)
      extends Global {
    override def top: Global.Top = owner.top
  }
}

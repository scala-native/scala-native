package scala.scalanative
package nir

sealed abstract class Global {
  def top: Global.Top
  def member(sig: Sig): Global.Member
  def member(sig: Sig.Unmangled): Global.Member = member(sig.mangled)

  final def isTop: Boolean =
    this.isInstanceOf[Global.Top]
  final def show: String =
    Show(this)
  final def mangle: String =
    Mangle(this)
}
object Global {
  final case object None extends Global {
    override def top: Global.Top =
      throw new Exception("None doesn't have a top.")
    override def member(sig: Sig) =
      throw new Exception("Global.None can't have any members.")
  }

  final case class Top(val id: String) extends Global {
    override def top: Global.Top =
      this
    override def member(sig: Sig): Global.Member =
      Global.Member(this, sig)
  }

  final case class Member(val owner: Global, val sig: Sig) extends Global {
    override def top: Global.Top =
      owner.top
    override def member(sig: Sig): Global.Member =
      throw new Exception("Global.Member can't have any members.")
  }

  implicit val globalOrdering: Ordering[Global] =
    Ordering.by[Global, (String, String)] {
      case Global.Member(Global.Top(id), sig) =>
        (id, sig.mangle)
      case Global.Top(id) =>
        (id, "")
      case _ =>
        ("", "")
    }
}

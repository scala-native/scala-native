package scala.scalanative
package nir

sealed abstract class Global {
  def id: String
  def top: Global.Top

  def member(id: String): Global.Member =
    Global.Member(this, id)

  def tag(tag: String): Global = this match {
    case Global.Top(id)       => Global.Top(s"$tag.$id")
    case Global.Member(n, id) => Global.Member(n, s"$tag.$id")
    case _                    => util.unreachable
  }

  final def isTop: Boolean = this.isInstanceOf[Global.Top]

  final def show: String = nir.Show(this)
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

  def genSignature(methodName: nir.Global): String =
    genSignature(methodName, proxy = false)

  def genSignature(methodName: nir.Global, proxy: Boolean): String =
    genSignature(methodName.id, proxy)

  def genSignature(fullSignature: String): String =
    genSignature(fullSignature, proxy = false)

  def genSignature(fullSignature: String, proxy: Boolean): String = {
    val index = fullSignature.lastIndexOf("_")
    val signature =
      if (index != -1) {
        fullSignature.substring(0, index)
      } else {
        fullSignature
      }
    if (proxy) {
      toProxySignature(signature)
    } else {
      signature
    }
  }

  def toProxySignature(signature: String) = signature + "_proxy"

  def stripImplClassTrailingDollar(name: Global): Global = name match {
    case Global.None =>
      name
    case Global.Top(id) =>
      if (id.endsWith("$class$")) {
        Global.Top(id.substring(0, id.length - 1))
      } else {
        name
      }
    case Global.Member(subname, id) =>
      Global.Member(stripImplClassTrailingDollar(subname), id)
  }
}

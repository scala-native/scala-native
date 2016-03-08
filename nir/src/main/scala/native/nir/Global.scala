package native
package nir

import Shows._
import native.util.sh

final class Global(val parts: Seq[String], val isType: Boolean) {
  override def hashCode: Int = ("native.nir.Global", parts, isType).hashCode

  override def equals(other: Any) = other match {
    case g: Global =>
      g.isType == isType && g.parts == parts
    case _ =>
      false
  }

  override def toString = {
    val pre = if (isType) "Global.intrinsic" else "Global"
    val args = parts.mkString("(", ", ", ")")
    s"$pre$args"
  }

  def isIntrinsic: Boolean =
    parts.headOption.fold(false)(_ == "nrt")

  def +(tag: String): Global =
    new Global(parts :+ tag, isType)
  def ++(tags: Seq[String]): Global =
    new Global(parts ++ tags, isType)
}
object Global {
  object Val {
    def apply(parts: String*) =
      new Global(parts, isType = false)
    def unapplySeq(g: Global) =
      if (!g.isType) Some(g.parts)
      else None
  }

  object Type {
    def apply(parts: String*) =
      new Global(parts, isType = true)
    def unapplySeq(g: Global) =
      if (g.isType) Some(g.parts)
      else None
  }
}

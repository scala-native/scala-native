package native
package nir

import Shows._
import native.util.sh

final class Global(val parts: Seq[String], val isIntrinsic: Boolean) {
  override def hashCode: Int = ("native.nir.Global", parts, isIntrinsic).hashCode

  override def equals(other: Any) = other match {
    case g: Global =>
      g.isIntrinsic == isIntrinsic && g.parts == parts
    case _ =>
      false
  }

  override def toString = {
    val pre = if (isIntrinsic) "Global.intrinsic" else "Global"
    val args = parts.mkString("(", ", ", ")")
    s"$pre$args"
  }

  def +(tag: String): Global =
    new Global(parts :+ tag, isIntrinsic)
  def ++(tags: Seq[String]): Global =
    new Global(parts ++ tags, isIntrinsic)
  def nrt = {
    assert(isIntrinsic)
    Global(("nrt" +: parts): _*)
  }
}
object Global {
  def apply(parts: String*) =
    new Global(parts, isIntrinsic = false)
  def unapplySeq(g: Global) =
    if (!g.isIntrinsic) Some(g.parts)
    else None

  object intrinsic {
    def apply(parts: String*) =
      new Global(parts, isIntrinsic = true)
    def unapplySeq(g: Global) =
      if (g.isIntrinsic) Some(g.parts)
      else None
  }
}

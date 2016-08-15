package scala.scalanative.nir

/**
 * Created by Kamil on 23.07.2016.
 */
case class Arg(ty: Type, attrs: ArgAttrs = ArgAttrs.empty)

case class ArgAttrs(byval: Option[Type] = None, sret: Option[Type] = None) {

  def toSeq: Seq[ArgAttr] = Seq(
    byval.map(ArgAttr.Byval),
    sret.map(ArgAttr.Sret)
  ).flatten

}

object ArgAttrs {

  val empty = new ArgAttrs

  def fromSeq(attrs: Seq[ArgAttr]): ArgAttrs =
    attrs.foldLeft(ArgAttrs.empty) { (attrs, attr) =>
      attr match {
        case ArgAttr.Byval(ty) => attrs.copy(byval = Some(ty))
        case ArgAttr.Sret(ty) => attrs.copy(sret = Some(ty))
      }
    }

}

sealed abstract class ArgAttr

object ArgAttr {

  case class Byval(ty: Type) extends ArgAttr

  case class Sret(ty: Type) extends ArgAttr

}
